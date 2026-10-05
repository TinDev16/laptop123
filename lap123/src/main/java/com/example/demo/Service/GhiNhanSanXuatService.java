package com.example.demo.Service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class GhiNhanSanXuatService {
    private final JdbcTemplate jdbc;
    public GhiNhanSanXuatService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Map<String, Object>> getOrders(String email) {
        return jdbc.queryForList("""
            SELECT y.maycsx, CONCAT('YCSX-', y.maycsx) AS maycsxCode,
                   y.masp, y.mabom, s.tensp AS tenSp,
                   y.soluongyeucau,
                   LEAST(y.soluongyeucau, y.soluonghoanthanh + COALESCE(g.daghi,0)) AS soluonghoanthanh,
                   y.soluonghoanthanh AS soluonghoanthanh_da_xac_nhan,
                   y.trangthai, b.phienban,
                   COALESCE(g.daghi,0) AS soluong_daghi_nhan,
                   GREATEST(y.soluongyeucau - y.soluonghoanthanh - COALESCE(g.daghi,0),0) AS soluong_con_lai
            FROM yeucausanxuat y
            JOIN sanpham s ON s.masp = y.masp
            JOIN bom b ON b.mabom = y.mabom
            JOIN taikhoan t ON t.manv = y.nguoiphutrach
            LEFT JOIN (SELECT maycsx, SUM(soluong) daghi FROM ghi_nhan_san_xuat GROUP BY maycsx) g ON g.maycsx=y.maycsx
            WHERE t.email=? AND y.trangthai IN ('DA_GIAO','DANG_SAN_XUAT')
              AND y.soluonghoanthanh + COALESCE(g.daghi,0) < y.soluongyeucau
            ORDER BY y.maycsx DESC
            """, email);
    }

    public List<Map<String,Object>> getBomDetails(long bomId) {
        return jdbc.queryForList("""
            SELECT c.manvl, n.manvl_code, n.tennvl, n.donvitinh,
                   c.soluong_dinhmuc, c.tyle_haohut, COALESCE(k.soluongton,0) AS soluongton
            FROM bom_chitiet c JOIN nguyenvatlieu n ON n.manvl=c.manvl
            LEFT JOIN kho_nvl_san_xuat k ON k.manvl=c.manvl
            WHERE c.mabom=? ORDER BY n.tennvl
            """, bomId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void recordProduction(String email, long orderId, long bomId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Số lượng sản xuất phải lớn hơn 0.");

        Map<String,Object> order = jdbc.queryForMap("""
            SELECT y.maycsx,y.masp,y.mabom,y.soluongyeucau,y.soluonghoanthanh,y.trangthai
            FROM yeucausanxuat y JOIN taikhoan t ON t.manv=y.nguoiphutrach
            WHERE y.maycsx=? AND t.email=? FOR UPDATE
            """, orderId,email);
        String status=String.valueOf(order.get("trangthai"));
        if ("HOAN_THANH".equals(status)) throw new IllegalStateException("Yêu cầu sản xuất đã hoàn thành.");
        if ("HUY".equals(status)) throw new IllegalStateException("Yêu cầu sản xuất đã bị hủy.");
        long productId=((Number)order.get("masp")).longValue();
        long orderBomId=((Number)order.get("mabom")).longValue();
        if(orderBomId!=bomId) throw new IllegalArgumentException("BOM được chọn không thuộc yêu cầu sản xuất này.");
        int requested=((Number)order.get("soluongyeucau")).intValue();
        int confirmed=((Number)order.get("soluonghoanthanh")).intValue();
        Integer recorded=jdbc.queryForObject("SELECT COALESCE(SUM(soluong),0) FROM ghi_nhan_san_xuat WHERE maycsx=?",Integer.class,orderId);
        if(recorded==null) recorded=0;
        int remaining=requested-confirmed-recorded;
        if(remaining<=0) throw new IllegalStateException("Yêu cầu không còn số lượng chưa ghi nhận.");
        if(quantity>remaining) throw new IllegalArgumentException("Số lượng ghi nhận ("+quantity+") vượt quá số lượng còn có thể sản xuất ("+remaining+").");

        Map<String,Object> bom=jdbc.queryForMap("SELECT masp,trangthai FROM bom WHERE mabom=?",bomId);
        if(((Number)bom.get("masp")).longValue()!=productId) throw new IllegalStateException("BOM không thuộc sản phẩm của yêu cầu.");
        if(!"HIEU_LUC".equals(String.valueOf(bom.get("trangthai")))) throw new IllegalStateException("BOM của yêu cầu sản xuất không còn hiệu lực.");

        List<Map<String,Object>> lines=jdbc.queryForList("""
            SELECT c.manvl,c.soluong_dinhmuc,c.tyle_haohut,n.tennvl
            FROM bom_chitiet c JOIN nguyenvatlieu n ON n.manvl=c.manvl
            WHERE c.mabom=? ORDER BY c.manvl
            """,bomId);
        if(lines.isEmpty()) throw new IllegalStateException("BOM không có nguyên vật liệu.");

        for(Map<String,Object> line:lines){
            long materialId=((Number)line.get("manvl")).longValue();
            BigDecimal base=decimal(line.get("soluong_dinhmuc"));
            BigDecimal waste=decimal(line.get("tyle_haohut"));
            BigDecimal required=BigDecimal.valueOf(quantity).multiply(base)
                    .multiply(BigDecimal.ONE.add(waste.divide(BigDecimal.valueOf(100),10,RoundingMode.HALF_UP)))
                    .setScale(0,RoundingMode.CEILING);
            Number stockNumber=jdbc.queryForObject("SELECT COALESCE(soluongton,0) FROM kho_nvl_san_xuat WHERE manvl=? FOR UPDATE",Number.class,materialId);
            BigDecimal stock=stockNumber==null?BigDecimal.ZERO:decimal(stockNumber);
            if (stock.compareTo(required) < 0) {
                throw new IllegalStateException("NVL \"" + line.get("tennvl") + "\" không đủ tồn kho sản xuất. Cần: " + required.stripTrailingZeros().toPlainString() + ", tồn: " + stock.stripTrailingZeros().toPlainString());
            }
            jdbc.update("UPDATE kho_nvl_san_xuat SET soluongton=soluongton-? WHERE manvl=? AND soluongton>=?",required,materialId,required);
        }

        jdbc.update("""
            INSERT INTO kho_thanhpham_san_xuat(masp,mabom,soluongton) VALUES(?,?,?)
            ON DUPLICATE KEY UPDATE soluongton=soluongton+VALUES(soluongton)
            """,productId,bomId,quantity);

        jdbc.update("INSERT INTO ghi_nhan_san_xuat(maycsx,masp,mabom,soluong,nguoighi,ngayghi) VALUES(?,?,?,?,?,CURRENT_TIMESTAMP)",
                orderId,productId,bomId,quantity,employeeId(email));
    }

    private int employeeId(String email){
        List<Integer> ids=jdbc.query("SELECT manv FROM taikhoan WHERE email=? AND manv IS NOT NULL",(rs,row)->rs.getInt(1),email);
        if(ids.isEmpty()) throw new IllegalArgumentException("Không tìm thấy nhân viên đang đăng nhập.");
        return ids.get(0);
    }
    private BigDecimal decimal(Object value){ return value==null?BigDecimal.ZERO:new BigDecimal(value.toString()); }
}
