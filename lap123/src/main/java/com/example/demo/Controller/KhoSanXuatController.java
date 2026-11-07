package com.example.demo.Controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.example.demo.excel.service.ProductionWarehouseExcelService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/kho-san-xuat")
public class KhoSanXuatController {

    private final JdbcTemplate jdbc;
    private final ProductionWarehouseExcelService excelService;

    public KhoSanXuatController(JdbcTemplate jdbc, ProductionWarehouseExcelService excelService) {
        this.jdbc = jdbc;
        this.excelService = excelService;
    }


    /**
     * Kho nguyên vật liệu tại nơi sản xuất
     */
    @GetMapping("/nvl")
    public String khoNvlSanXuat(Model model) {

        List<Map<String, Object>> materials = jdbc.queryForList(
                """
                SELECT
                    k.id,
                    k.manvl,
                    n.manvl_code,
                    n.tennvl,
                    n.donvitinh,
                    k.soluongton,
                    n.ton_toithieu,
                    n.dongianhap,
                    l.tenloai,
                    n.trangthai AS trangthai_nvl,
                    l.trangthai AS trangthai_loai
                FROM kho_nvl_san_xuat k
                JOIN nguyenvatlieu n
                    ON n.manvl = k.manvl
                JOIN loai_nguyen_vat_lieu l
                    ON l.maloai = n.maloai
                ORDER BY l.tenloai, n.tennvl
                """
        );

        Map<String, List<Map<String, Object>>> materialGroups =
                materials.stream()
                        .collect(Collectors.groupingBy(
                                m -> String.valueOf(m.get("tenloai")),
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        model.addAttribute("materials", materials);
        model.addAttribute("materialGroups", materialGroups);

        return "kho-san-xuat/nvl";
    }


    /**
     * Kho thành phẩm tại nơi sản xuất
     *
     * Chỉ lấy danh sách sản phẩm.
     * BOM được tải riêng qua API khi người dùng
     * bấm nút "Phiên bản".
     */
    @GetMapping("/thanh-pham")
    public String khoThanhPhamSanXuat(Model model) {

        List<Map<String, Object>> products = jdbc.queryForList(
                """
                SELECT
                    s.masp,
                    s.tensp,
                    s.hinhanhsp,
                    s.donvitinh,
                    s.trangthai AS trangthai_sp,
        
                    COALESCE(
                        (
                            SELECT SUM(k2.soluongton)
                            FROM kho_thanhpham_san_xuat k2
                            WHERE k2.masp = s.masp
                        ),
                        0
                    ) AS tong_san_pham
        
                FROM sanpham s
        
                WHERE EXISTS (
                    SELECT 1
                    FROM kho_thanhpham_san_xuat k
                    WHERE k.masp = s.masp
                )
        
                ORDER BY s.tensp
                """
        );

        model.addAttribute("products", products);

        return "kho-san-xuat/thanh-pham";
    }


    /**
     * Lấy các phiên bản BOM của một sản phẩm.
     *
     * URL:
     * /kho-san-xuat/thanh-pham/bom?productId=1
     *
     * Trả về:
     * - Mã BOM
     * - Phiên bản BOM
     * - Trạng thái BOM
     * - Tồn kho tại nơi sản xuất
     * - Tổng giá NVL của BOM
     */
    @GetMapping("/thanh-pham/bom")
    @ResponseBody
    public List<Map<String, Object>> bomTheoSanPham(
            @RequestParam int productId) {

        return jdbc.queryForList(
                """
                SELECT
                    b.mabom,
                    b.masp,
                    b.phienban,
                    b.trangthai AS trangthai_bom,

                    COALESCE(
                        k.soluongton,
                        0
                    ) AS soluongton,

                    COALESCE(
                        (
                            SELECT SUM(
                                COALESCE(
                                    bct.soluong_dinhmuc,
                                    0
                                )
                                *
                                COALESCE(
                                    nvl.dongianhap,
                                    0
                                )
                            )
                            FROM bom_chitiet bct
                            JOIN nguyenvatlieu nvl
                                ON nvl.manvl = bct.manvl
                            WHERE bct.mabom = b.mabom
                        ),
                        0
                    ) AS tong_gia_nvl

                FROM bom b

                LEFT JOIN kho_thanhpham_san_xuat k
                    ON k.masp = b.masp
                   AND k.mabom = b.mabom

                WHERE b.masp = ?

                ORDER BY b.mabom
                """,
                productId
        );
    }

    /**
     * Kho lỗi sản xuất
     */
    @GetMapping("/loi")
    public String khoLoiSanXuat(Model model) {

        List<Map<String, Object>> products = jdbc.queryForList(
                """
                SELECT
                    s.masp,
                    s.tensp,
                    s.hinhanhsp,
                    s.donvitinh,
                    s.trangthai AS trangthai_sp,
        
                    COALESCE(
                        (
                            SELECT SUM(k.soluongton)
                            FROM kho_loi_san_xuat k
                            WHERE k.masp = s.masp
                        ),
                        0
                    ) AS tong_san_pham,

                    COALESCE(
                        (
                            SELECT GROUP_CONCAT(DISTINCT l.lydo ORDER BY l.thoi_gian DESC SEPARATOR '; ')
                            FROM lich_su_kho_loi_san_xuat l
                            WHERE l.masp = s.masp AND l.lydo IS NOT NULL AND TRIM(l.lydo) != ''
                        ),
                        (
                            SELECT GROUP_CONCAT(DISTINCT kq.ghichu ORDER BY kq.ngayhoanthanh DESC SEPARATOR '; ')
                            FROM ketqua_sanxuat kq
                            JOIN yeucausanxuat y ON y.maycsx = kq.maycsx
                            WHERE y.masp = s.masp AND kq.soluongloi > 0 AND kq.ghichu IS NOT NULL AND TRIM(kq.ghichu) != ''
                        ),
                        'Lỗi kiểm tra chất lượng (QC)'
                    ) AS ly_do_loi
        
                FROM sanpham s
        
                WHERE EXISTS (
                    SELECT 1
                    FROM kho_loi_san_xuat k
                    WHERE k.masp = s.masp
                )
        
                ORDER BY s.tensp
                """
        );

        model.addAttribute("products", products);

        return "kho-san-xuat/loi";
    }


    /**
     * Lấy các phiên bản BOM và số lượng lỗi theo từng BOM.
     *
     * URL:
     * /kho-san-xuat/loi/bom?productId=1
     */
    @GetMapping("/loi/bom")
    @ResponseBody
    public List<Map<String, Object>> bomKhoLoiTheoSanPham(
            @RequestParam int productId) {

        return jdbc.queryForList(
                """
                SELECT
                    b.mabom,
                    b.masp,
                    b.phienban,
                    b.trangthai AS trangthai_bom,
        
                    COALESCE(k.soluongton, 0) AS soluongton,
        
                    COALESCE(
                        (
                            SELECT SUM(
                                COALESCE(bct.soluong_dinhmuc, 0)
                                * COALESCE(nvl.dongianhap, 0)
                            )
                            FROM bom_chitiet bct
                            JOIN nguyenvatlieu nvl
                                ON nvl.manvl = bct.manvl
                            WHERE bct.mabom = b.mabom
                        ),
                        0
                    ) AS tong_gia_nvl
        
                FROM bom b
        
                LEFT JOIN (
                    SELECT
                        masp,
                        mabom,
                        SUM(soluongton) AS soluongton
                    FROM kho_loi_san_xuat
                    GROUP BY masp, mabom
                ) k
                    ON k.masp = b.masp
                   AND k.mabom = b.mabom
        
                WHERE b.masp = ?
        
                ORDER BY b.mabom
                """,
                productId
        );
    }



    // =====================================================
    // XUẤT EXCEL: TỒN KHO NVL NƠI SẢN XUẤT
    // =====================================================
    @GetMapping("/nvl/excel/export")
    public ResponseEntity<byte[]> exportNvlExcel() {
        try {
            List<Map<String, Object>> materials = jdbc.queryForList(
                    """
                    SELECT
                        k.id,
                        k.manvl,
                        n.manvl_code,
                        n.tennvl,
                        n.donvitinh,
                        k.soluongton,
                        n.ton_toithieu,
                        n.dongianhap,
                        l.tenloai,
                        n.trangthai AS trangthai_nvl,
                        l.trangthai AS trangthai_loai
                    FROM kho_nvl_san_xuat k
                    JOIN nguyenvatlieu n ON n.manvl = k.manvl
                    JOIN loai_nguyen_vat_lieu l ON l.maloai = n.maloai
                    ORDER BY l.tenloai, n.tennvl
                    """
            );
            byte[] content = excelService.exportMaterials(materials);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Ton_Kho_NVL_San_Xuat_" + System.currentTimeMillis() + ".xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // =====================================================
    // XUẤT EXCEL: TỒN KHO THÀNH PHẨM NƠI SẢN XUẤT
    // =====================================================
    @GetMapping("/thanh-pham/excel/export")
    public ResponseEntity<byte[]> exportThanhPhamExcel() {
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(
                    """
                    SELECT
                        s.masp,
                        s.tensp,
                        s.donvitinh,
                        s.trangthai AS trangthai_sp,
                        b.mabom,
                        b.phienban,
                        b.trangthai AS trangthai_bom,
                        COALESCE(k.soluongton, 0) AS soluongton,
                        COALESCE(
                            (
                                SELECT SUM(k2.soluongton)
                                FROM kho_thanhpham_san_xuat k2
                                WHERE k2.masp = s.masp
                            ),
                            0
                        ) AS tong_san_pham,
                        COALESCE(
                            (
                                SELECT SUM(COALESCE(bct.soluong_dinhmuc, 0) * COALESCE(nvl.dongianhap, 0))
                                FROM bom_chitiet bct
                                JOIN nguyenvatlieu nvl ON nvl.manvl = bct.manvl
                                WHERE bct.mabom = b.mabom
                            ),
                            0
                        ) AS tong_gia_nvl
                    FROM sanpham s
                    JOIN bom b ON b.masp = s.masp
                    LEFT JOIN kho_thanhpham_san_xuat k ON k.masp = b.masp AND k.mabom = b.mabom
                    WHERE EXISTS (
                        SELECT 1 FROM kho_thanhpham_san_xuat k2 WHERE k2.masp = s.masp
                    ) OR EXISTS (
                        SELECT 1 FROM bom b2 WHERE b2.masp = s.masp
                    )
                    ORDER BY s.tensp, b.mabom
                    """
            );
            byte[] content = excelService.exportProducts(rows);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Ton_Kho_Thanh_Pham_San_Xuat_" + System.currentTimeMillis() + ".xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // =====================================================
    // XUẤT EXCEL: KHO LỖI SẢN XUẤT
    // =====================================================
    @GetMapping("/loi/excel/export")
    public ResponseEntity<byte[]> exportLoiExcel() {
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(
                    """
                    SELECT
                        s.masp,
                        s.tensp,
                        s.donvitinh,
                        s.trangthai AS trangthai_sp,
                        COALESCE(b.mabom, k.mabom) AS mabom,
                        b.phienban,
                        b.trangthai AS trangthai_bom,
                        COALESCE(k.soluongton, 0) AS soluongton,
                        COALESCE(
                            (
                                SELECT GROUP_CONCAT(DISTINCT l.lydo ORDER BY l.thoi_gian DESC SEPARATOR '; ')
                                FROM lich_su_kho_loi_san_xuat l
                                WHERE l.masp = s.masp AND l.mabom = k.mabom AND l.lydo IS NOT NULL AND TRIM(l.lydo) != ''
                            ),
                            (
                                SELECT GROUP_CONCAT(DISTINCT l.lydo ORDER BY l.thoi_gian DESC SEPARATOR '; ')
                                FROM lich_su_kho_loi_san_xuat l
                                WHERE l.masp = s.masp AND l.lydo IS NOT NULL AND TRIM(l.lydo) != ''
                            ),
                            'Lỗi kiểm tra chất lượng (QC)'
                        ) AS ly_do_loi,
                        COALESCE(
                            (
                                SELECT SUM(COALESCE(bct.soluong_dinhmuc, 0) * COALESCE(nvl.dongianhap, 0))
                                FROM bom_chitiet bct
                                JOIN nguyenvatlieu nvl ON nvl.manvl = bct.manvl
                                WHERE bct.mabom = k.mabom
                            ),
                            0
                        ) AS tong_gia_nvl
                    FROM sanpham s
                    JOIN kho_loi_san_xuat k ON k.masp = s.masp
                    LEFT JOIN bom b ON b.mabom = k.mabom
                    ORDER BY s.tensp, b.phienban
                    """
            );
            byte[] content = excelService.exportDefects(rows);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Kho_Loi_San_Xuat_" + System.currentTimeMillis() + ".xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
