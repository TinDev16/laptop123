package com.example.demo.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaterialCatalogService {

    private final JdbcTemplate jdbc;

    public MaterialCatalogService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // =========================
    // DANH SÁCH NGUYÊN VẬT LIỆU
    // =========================

    public List<Map<String, Object>> list(String keyword) {

        return jdbc.queryForList(
                """
                SELECT
                    n.*,
                    l.tenloai,
                    CASE
                        WHEN n.trangthai = 1 THEN 'Hoạt động'
                        ELSE 'Ngừng sử dụng'
                    END AS trangthai_label
                FROM nguyenvatlieu n
                LEFT JOIN loai_nguyen_vat_lieu l
                    ON l.maloai = n.maloai
                WHERE n.tennvl LIKE ?
                   OR n.manvl_code LIKE ?
                ORDER BY
                    CASE WHEN l.tenloai IS NULL THEN 1 ELSE 0 END,
                    l.tenloai ASC,
                    n.tennvl ASC,
                    n.manvl DESC
                """,
                "%" + keyword + "%",
                "%" + keyword + "%"
        );
    }

    public Map<String, List<Map<String, Object>>> groupByType(
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

    // =========================
    // LẤY 1 NGUYÊN VẬT LIỆU
    // =========================

    public Map<String, Object> get(long id) {

        List<Map<String, Object>> rows =
                jdbc.queryForList(
                        """
                        SELECT *
                        FROM nguyenvatlieu
                        WHERE manvl = ?
                        """,
                        id
                );

        if (rows.isEmpty()) {
            throw new IllegalArgumentException(
                    "Không tìm thấy nguyên vật liệu."
            );
        }

        return rows.get(0);
    }

    // =========================
    // DANH SÁCH TẤT CẢ LOẠI NVL
    // =========================

    /*
     * Dùng cho:
     * - Trang quản lý loại NVL
     * - Các nơi cần xem toàn bộ loại
     */
    public List<Map<String, Object>> types() {

        return jdbc.queryForList(
                """
                SELECT
                    maloai,
                    tenloai,
                    trangthai
                FROM loai_nguyen_vat_lieu
                ORDER BY tenloai
                """
        );
    }

    // =========================
    // LOẠI NVL ĐANG HOẠT ĐỘNG
    // =========================

    /*
     * Dùng cho FORM TẠO NVL MỚI.
     *
     * Chỉ cho phép chọn loại:
     * trangthai = 1
     */
    public List<Map<String, Object>> activeTypes() {

        return jdbc.queryForList(
                """
                SELECT
                    maloai,
                    tenloai,
                    trangthai
                FROM loai_nguyen_vat_lieu
                WHERE trangthai = 1
                ORDER BY tenloai
                """
        );
    }

    // =========================
    // LOẠI NVL DÙNG CHO FORM SỬA
    // =========================

    /*
     * Dùng cho FORM SỬA NVL.
     *
     * Lấy:
     * - Tất cả loại đang hoạt động
     * - Loại hiện tại của NVL đang sửa
     *
     * Nếu loại hiện tại đã ngừng sử dụng
     * thì vẫn phải hiển thị để giữ dữ liệu cũ.
     */
    public List<Map<String, Object>> typesForEdit(long materialId) {

        return jdbc.queryForList(
                """
                SELECT
                    l.maloai,
                    l.tenloai,
                    l.trangthai
                FROM loai_nguyen_vat_lieu l
                WHERE l.trangthai = 1
                   OR l.maloai = (
                        SELECT n.maloai
                        FROM nguyenvatlieu n
                        WHERE n.manvl = ?
                   )
                ORDER BY l.tenloai
                """,
                materialId
        );
    }

    // =========================
    // LẤY 1 LOẠI NVL
    // =========================

    public Map<String, Object> getType(int id) {

        List<Map<String, Object>> rows =
                jdbc.queryForList(
                        """
                        SELECT
                            maloai,
                            tenloai,
                            trangthai
                        FROM loai_nguyen_vat_lieu
                        WHERE maloai = ?
                        """,
                        id
                );

        if (rows.isEmpty()) {
            throw new IllegalArgumentException(
                    "Không tìm thấy loại nguyên vật liệu."
            );
        }

        return rows.get(0);
    }

    // =========================
    // THÊM LOẠI NVL
    // =========================

    @Transactional
    public void addType(String name) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Tên loại nguyên vật liệu không được trống."
            );
        }

        name = name.trim();

        if (name.length() > 255) {
            throw new IllegalArgumentException(
                    "Tên loại nguyên vật liệu không được vượt quá 255 ký tự."
            );
        }

        Integer count = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM loai_nguyen_vat_lieu
                WHERE LOWER(tenloai) = LOWER(?)
                """,
                Integer.class,
                name
        );

        if (count != null && count > 0) {
            throw new IllegalArgumentException(
                    "Loại nguyên vật liệu này đã tồn tại."
            );
        }

        jdbc.update(
                """
                INSERT INTO loai_nguyen_vat_lieu
                (
                    tenloai,
                    trangthai
                )
                VALUES (?, 1)
                """,
                name
        );
    }

    // =========================
    // CẬP NHẬT LOẠI NVL
    // =========================

    @Transactional
    public void updateType(
            int id,
            String name,
            int status) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Tên loại nguyên vật liệu không được trống."
            );
        }

        name = name.trim();

        if (name.length() > 255) {
            throw new IllegalArgumentException(
                    "Tên loại nguyên vật liệu không được vượt quá 255 ký tự."
            );
        }

        if (status != 0 && status != 1) {
            throw new IllegalArgumentException(
                    "Trạng thái loại nguyên vật liệu không hợp lệ."
            );
        }

        // Kiểm tra loại có tồn tại
        Integer exists = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM loai_nguyen_vat_lieu
                WHERE maloai = ?
                """,
                Integer.class,
                id
        );

        if (exists == null || exists == 0) {
            throw new IllegalArgumentException(
                    "Không tìm thấy loại nguyên vật liệu."
            );
        }

        // Kiểm tra trùng tên với loại khác
        Integer duplicate = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM loai_nguyen_vat_lieu
                WHERE LOWER(tenloai) = LOWER(?)
                  AND maloai <> ?
                """,
                Integer.class,
                name,
                id
        );

        if (duplicate != null && duplicate > 0) {
            throw new IllegalArgumentException(
                    "Tên loại nguyên vật liệu này đã tồn tại."
            );
        }

        /*
         * Chỉ cập nhật loại.
         *
         * KHÔNG cập nhật trạng thái
         * của các NVL thuộc loại này.
         */
        int updated = jdbc.update(
                """
                UPDATE loai_nguyen_vat_lieu
                SET
                    tenloai = ?,
                    trangthai = ?
                WHERE maloai = ?
                """,
                name,
                status,
                id
        );

        if (updated == 0) {
            throw new IllegalArgumentException(
                    "Không thể cập nhật loại nguyên vật liệu."
            );
        }
    }

    // =========================
    // LƯU NGUYÊN VẬT LIỆU
    // =========================

    @Transactional
    public void save(
            Long id,
            String code,
            String name,
            Integer typeId,
            String unit,
            BigDecimal price,
            int minimum,
            String status,
            String note) {

        // =========================
        // KIỂM TRA DỮ LIỆU CƠ BẢN
        // =========================

        if (code == null || code.isBlank()
                || name == null || name.isBlank()
                || unit == null || unit.isBlank()) {

            throw new IllegalArgumentException(
                    "Mã, tên và đơn vị tính không được trống."
            );
        }

        if (typeId == null) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn loại nguyên vật liệu."
            );
        }

        if (code.length() > 30
                || name.length() > 255
                || unit.length() > 50
                || (note != null && note.length() > 255)) {

            throw new IllegalArgumentException(
                    "Thông tin NVL vượt độ dài cho phép."
            );
        }

        if (price == null
                || price.signum() < 0
                || minimum < 0) {

            throw new IllegalArgumentException(
                    "Giá và tồn kho tối thiểu không được âm."
            );
        }

        if (!"HOAT_DONG".equals(status)
                && !"NGUNG_SU_DUNG".equals(status)) {

            throw new IllegalArgumentException(
                    "Trạng thái NVL không hợp lệ."
            );
        }

        // =========================
        // KIỂM TRA LOẠI NVL
        // =========================

        Integer typeStatus = jdbc.queryForObject(
                """
                SELECT trangthai
                FROM loai_nguyen_vat_lieu
                WHERE maloai = ?
                """,
                Integer.class,
                typeId
        );

        if (typeStatus == null) {
            throw new IllegalArgumentException(
                    "Loại nguyên vật liệu không tồn tại."
            );
        }

        // =========================
        // TẠO MỚI
        // =========================

        /*
         * Khi tạo NVL mới:
         *
         * Chỉ được chọn loại đang hoạt động.
         */
        if (id == null && typeStatus != 1) {

            throw new IllegalArgumentException(
                    "Không thể chọn loại nguyên vật liệu đã ngừng sử dụng."
            );
        }

        // =========================
        // CẬP NHẬT
        // =========================

        /*
         * Khi sửa:
         *
         * - Giữ nguyên loại hiện tại:
         *   Cho phép, kể cả loại đã ngừng.
         *
         * - Đổi sang loại khác:
         *   Loại mới bắt buộc phải đang hoạt động.
         */
        if (id != null) {

            Integer currentTypeId = jdbc.queryForObject(
                    """
                    SELECT maloai
                    FROM nguyenvatlieu
                    WHERE manvl = ?
                    """,
                    Integer.class,
                    id
            );

            if (currentTypeId == null) {
                throw new IllegalArgumentException(
                        "Không tìm thấy nguyên vật liệu."
                );
            }

            boolean changingType =
                    !currentTypeId.equals(typeId);

            if (changingType && typeStatus != 1) {

                throw new IllegalArgumentException(
                        "Không thể chuyển sang loại nguyên vật liệu đã ngừng sử dụng."
                );
            }
        }

        int statusValue =
                "HOAT_DONG".equals(status) ? 1 : 0;

        // =========================
        // THÊM MỚI
        // =========================

        if (id == null) {

            jdbc.update(
                    """
                    INSERT INTO nguyenvatlieu
                    (
                        manvl_code,
                        tennvl,
                        maloai,
                        donvitinh,
                        dongianhap,
                        soluongton,
                        ton_toithieu,
                        lead_time_ngay,
                        trangthai,
                        ghichu
                    )
                    VALUES (?, ?, ?, ?, ?, 0, ?, 0, ?, ?)
                    """,
                    code.trim(),
                    name.trim(),
                    typeId,
                    unit.trim(),
                    price,
                    minimum,
                    statusValue,
                    note
            );

        }

        // =========================
        // CẬP NHẬT
        // =========================

        else {

            int updated = jdbc.update(
                    """
                    UPDATE nguyenvatlieu
                    SET
                        manvl_code = ?,
                        tennvl = ?,
                        maloai = ?,
                        donvitinh = ?,
                        dongianhap = ?,
                        ton_toithieu = ?,
                        trangthai = ?,
                        ghichu = ?
                    WHERE manvl = ?
                    """,
                    code.trim(),
                    name.trim(),
                    typeId,
                    unit.trim(),
                    price,
                    minimum,
                    statusValue,
                    note,
                    id
            );

            if (updated == 0) {
                throw new IllegalArgumentException(
                        "Không tìm thấy nguyên vật liệu."
                );
            }
        }
    }
}