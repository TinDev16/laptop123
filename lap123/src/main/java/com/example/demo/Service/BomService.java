package com.example.demo.Service;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BomService {

    private final JdbcTemplate jdbc;

    public BomService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> list() {
        return jdbc.queryForList(
                "SELECT b.mabom, b.phienban, b.trangthai, b.ghichu, s.tensp " +
                        "FROM bom b " +
                        "JOIN sanpham s ON s.masp = b.masp " +
                        "ORDER BY b.mabom DESC"
        );
    }

    public List<Map<String, Object>> products() {
        return jdbc.queryForList(
                "SELECT masp, tensp " +
                        "FROM sanpham " +
                        "ORDER BY tensp"
        );
    }

    // =========================================================
    // CHỈ LẤY NVL:
    // NVL = HOẠT ĐỘNG
    // AND
    // LOẠI NVL = HOẠT ĐỘNG
    // =========================================================
    public List<Map<String, Object>> materials() {
        return jdbc.queryForList(
                """
                SELECT
                    n.manvl,
                    n.manvl_code,
                    n.tennvl,
                    n.donvitinh,
                    l.maloai,
                    l.tenloai
                FROM nguyenvatlieu n
                JOIN loai_nguyen_vat_lieu l
                    ON l.maloai = n.maloai
                WHERE n.trangthai = 1
                  AND l.trangthai = 1
                ORDER BY l.tenloai, n.tennvl
                """
        );
    }

    public Map<String, List<Map<String, Object>>> groupMaterialsByType(
            List<Map<String, Object>> materials) {

        Map<String, List<Map<String, Object>>> groups =
                new java.util.LinkedHashMap<>();

        for (Map<String, Object> material : materials) {

            String typeName =
                    material.get("tenloai") != null
                            ? material.get("tenloai").toString()
                            : "Chưa phân loại";

            groups
                    .computeIfAbsent(
                            typeName,
                            key -> new java.util.ArrayList<>()
                    )
                    .add(material);
        }

        return groups;
    }

    public List<Map<String, Object>> versions(int productId) {

        return jdbc.queryForList(
                """
                SELECT
                    b.mabom,
                    b.phienban,
                    b.trangthai,
    
                    /*
                     * Tổng thành phẩm đã được quản lý xác nhận nhập kho
                     * theo BOM.
                     */
                    COALESCE((
                        SELECT SUM(k.soluonghoanthanh)
                        FROM yeucausanxuat y
                        JOIN ketqua_sanxuat k
                            ON k.maycsx = y.maycsx
                        WHERE y.mabom = b.mabom
                          AND k.trangthai = 'DA_NHAP_KHO'
                    ), 0)
    
                    -
    
                    /*
                     * Trừ số lượng đã xuất thành phẩm
                     * theo chính BOM đó.
                     */
                    COALESCE((
                        SELECT SUM(c.soluong)
                        FROM phieuxuat_thanhpham p
                        JOIN ct_phieuxuat_thanhpham c
                            ON c.mapxtp = p.mapxtp
                        WHERE c.mabom = b.mabom
                          AND p.trangthai = 'DA_XUAT'
                    ), 0)
    
                    AS soluong_thanhpham,
    
                    /*
                     * =====================================================
                     * TÍNH GIÁ NGUYÊN VẬT LIỆU CỦA BOM
                     *
                     * Nếu cùng một NVL xuất hiện nhiều dòng:
                     *
                     *   Tổng định mức = SUM(soluong_dinhmuc)
                     *
                     * Sau đó:
                     *
                     *   Tổng định mức × đơn giá nhập
                     *
                     * Cuối cùng cộng tất cả NVL.
                     *
                     * dongianhap = đơn giá nhập hiện tại của NVL.
                     * =====================================================
                     */
                    COALESCE((
                        SELECT SUM(
                            material_total.total_quantity
                            * material_total.dongianhap
                        )
                        FROM (
                            SELECT
                                c.manvl,
                                SUM(c.soluong_dinhmuc) AS total_quantity,
                                n.dongianhap
                            FROM bom_chitiet c
                            JOIN nguyenvatlieu n
                                ON n.manvl = c.manvl
                            WHERE c.mabom = b.mabom
                            GROUP BY
                                c.manvl,
                                n.dongianhap
                        ) material_total
                    ), 0) AS tong_gia_nvl
    
                FROM bom b
    
                WHERE b.masp = ?
    
                ORDER BY b.mabom DESC
                """,
                productId
        );
    }

    public List<Map<String, Object>> materialsByBom(long bomId) {
        return jdbc.queryForList(
                "SELECT c.manvl, " +
                        "n.manvl_code, " +
                        "n.tennvl, " +
                        "n.donvitinh, " +
                        "c.soluong_dinhmuc, " +
                        "c.tyle_haohut AS ty_le_hao_hut " +
                        "FROM bom_chitiet c " +
                        "JOIN nguyenvatlieu n ON n.manvl = c.manvl " +
                        "WHERE c.mabom = ? " +
                        "ORDER BY c.id",
                bomId
        );
    }

    public Map<String, Object> get(long id) {
        return jdbc.queryForMap(
                "SELECT b.mabom, b.masp, b.phienban, b.trangthai, " +
                        "b.ghichu, s.tensp " +
                        "FROM bom b " +
                        "JOIN sanpham s ON s.masp = b.masp " +
                        "WHERE b.mabom = ?",
                id
        );
    }

    public List<Map<String, Object>> details(long id) {
        return jdbc.queryForList(
                """
                SELECT
                    c.manvl,
                    n.manvl_code,
                    n.tennvl,
                    l.tenloai,
                    n.donvitinh,
                    c.soluong_dinhmuc,
                    c.tyle_haohut AS ty_le_hao_hut
                FROM bom_chitiet c
                JOIN nguyenvatlieu n
                    ON n.manvl = c.manvl
                LEFT JOIN loai_nguyen_vat_lieu l
                    ON l.maloai = n.maloai
                WHERE c.mabom = ?
                ORDER BY c.id
                """,
                id
        );
    }

    // =========================================================
    // TẠO BOM
    // =========================================================
    @Transactional
    public long create(
            String email,
            int productId,
            String version,
            String note,
            List<Long> materialIds,
            List<BigDecimal> quantities,
            List<BigDecimal> wastePercentages) {

        if (version == null
                || version.isBlank()
                || version.length() > 30) {

            throw new IllegalArgumentException(
                    "Phiên bản BOM không được trống."
            );
        }

        if (note != null && note.length() > 255) {
            throw new IllegalArgumentException(
                    "Ghi chú tối đa 255 ký tự."
            );
        }

        if (materialIds == null
                || materialIds.isEmpty()
                || quantities == null
                || wastePercentages == null
                || materialIds.size() != quantities.size()
                || materialIds.size() != wastePercentages.size()) {

            throw new IllegalArgumentException(
                    "Chi tiết BOM không hợp lệ."
            );
        }

        Set<Long> unique = new HashSet<>();

        for (int i = 0; i < materialIds.size(); i++) {

            if (materialIds.get(i) == null
                    || !unique.add(materialIds.get(i))) {

                throw new IllegalArgumentException(
                        "Nguyên vật liệu bị trùng trong BOM."
                );
            }

            if (quantities.get(i) == null
                    || quantities.get(i).signum() <= 0
                    || wastePercentages.get(i) == null
                    || wastePercentages.get(i).signum() < 0
                    || wastePercentages.get(i)
                    .compareTo(BigDecimal.valueOf(100)) > 0) {

                throw new IllegalArgumentException(
                        "Định mức phải lớn hơn 0 và hao hụt không được âm."
                );
            }
        }

        // =====================================================
        // QUAN TRỌNG:
        // KIỂM TRA LẠI NVL Ở BACKEND
        //
        // NVL phải hoạt động
        // AND
        // loại NVL phải hoạt động
        // =====================================================
        validateActiveMaterials(materialIds);

        Integer creator = jdbc.queryForObject(
                "SELECT manv " +
                        "FROM taikhoan " +
                        "WHERE email = ?",
                Integer.class,
                email
        );

        if (creator == null) {
            throw new IllegalArgumentException(
                    "Tài khoản chưa liên kết với nhân viên."
            );
        }

        GeneratedKeyHolder key = new GeneratedKeyHolder();

        jdbc.update(connection -> {

            PreparedStatement statement =
                    connection.prepareStatement(
                            "INSERT INTO bom " +
                                    "(masp, phienban, nguoitao, ghichu, trangthai) " +
                                    "VALUES (?, ?, ?, ?, 'HIEU_LUC')",
                            Statement.RETURN_GENERATED_KEYS
                    );

            statement.setInt(1, productId);
            statement.setString(2, version.trim());
            statement.setInt(3, creator);
            statement.setString(4, note);

            return statement;

        }, key);

        long id = key.getKey().longValue();

        for (int i = 0; i < materialIds.size(); i++) {

            jdbc.update(
                    "INSERT INTO bom_chitiet " +
                            "(mabom, manvl, soluong_dinhmuc, tyle_haohut) " +
                            "VALUES (?, ?, ?, ?)",
                    id,
                    materialIds.get(i),
                    quantities.get(i),
                    wastePercentages.get(i)
            );
        }

// =====================================================
// TẠO DÒNG KHO THÀNH PHẨM SẢN XUẤT
//
// Mỗi BOM có một dòng tồn kho riêng.
// Khi mới tạo BOM:
// soluongton = 0
// =====================================================

        int inserted = jdbc.update(
                """
                INSERT INTO kho_thanhpham_san_xuat
                (
                    masp,
                    mabom,
                    soluongton
                )
                VALUES
                (
                    ?,
                    ?,
                    0
                )
                """,
                productId,
                id
        );

        if (inserted != 1) {
            throw new IllegalStateException(
                    "Không thể tạo kho thành phẩm sản xuất cho BOM."
            );
        }

        return id;
    }

    // =========================================================
    // KIỂM TRA NVL CÓ ĐƯỢC PHÉP DÙNG CHO NGHIỆP VỤ MỚI KHÔNG
    //
    // Điều kiện:
    //
    // nguyenvatlieu.trangthai = 1
    // AND
    // loai_nguyen_vat_lieu.trangthai = 1
    // =========================================================
    private void validateActiveMaterials(List<Long> materialIds) {

        String sql =
                """
                SELECT
                    n.manvl,
                    n.tennvl,
                    n.trangthai AS nvl_trangthai,
                    l.trangthai AS loai_trangthai
                FROM nguyenvatlieu n
                JOIN loai_nguyen_vat_lieu l
                    ON l.maloai = n.maloai
                WHERE n.manvl = ?
                """;

        for (Long materialId : materialIds) {

            List<Map<String, Object>> rows =
                    jdbc.queryForList(sql, materialId);

            if (rows.isEmpty()) {

                throw new IllegalArgumentException(
                        "Nguyên vật liệu không tồn tại."
                );
            }

            Map<String, Object> row = rows.get(0);

            int materialStatus =
                    ((Number) row.get("nvl_trangthai"))
                            .intValue();

            int typeStatus =
                    ((Number) row.get("loai_trangthai"))
                            .intValue();

            String materialName =
                    String.valueOf(row.get("tennvl"));

            // NVL đã ngừng
            if (materialStatus != 1) {

                throw new IllegalArgumentException(
                        "Nguyên vật liệu \""
                                + materialName
                                + "\" đã ngừng sử dụng."
                );
            }

            // Loại NVL đã ngừng
            if (typeStatus != 1) {

                throw new IllegalArgumentException(
                        "Loại của nguyên vật liệu \""
                                + materialName
                                + "\" đã ngừng sử dụng."
                );
            }
        }
    }

    // =========================================================
    // NGỪNG SỬ DỤNG BOM
    // =========================================================
    @Transactional
    public void disable(long id) {

        int count = jdbc.update(
                "UPDATE bom " +
                        "SET trangthai = 'NGUNG_SU_DUNG' " +
                        "WHERE mabom = ? " +
                        "AND trangthai = 'HIEU_LUC'",
                id
        );

        if (count == 0) {
            throw new IllegalArgumentException(
                    "BOM không tồn tại hoặc đã ngừng sử dụng."
            );
        }
    }
}