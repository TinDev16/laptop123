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

        if (name == null || name.isBlank()
                || unit == null || unit.isBlank()) {

            throw new IllegalArgumentException(
                    "Tên và đơn vị tính không được trống."
            );
        }

        if (typeId == null) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn loại nguyên vật liệu."
            );
        }

        // Tạo mới: backend tự sinh mã, không sử dụng mã do trình duyệt gửi lên.
        if (id == null) {
            code = generateMaterialCode(typeId);
        } else {
            // Sửa NVL: giữ mã hiện tại, không cho phép để trống.
            if (code == null || code.isBlank()) {
                throw new IllegalArgumentException("Mã NVL không được trống.");
            }

            code = code.trim();
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

        // =====================================================
        // THÊM MỚI NVL
        // =====================================================

        if (id == null) {

            /*
             * Bước 1:
             * Tạo NVL ở kho chính.
             *
             * soluongton = 0
             */
            long newMaterialId = insertMaterial(
                    code.trim(),
                    name.trim(),
                    typeId,
                    unit.trim(),
                    price,
                    minimum,
                    statusValue,
                    note
            );

            /*
             * Bước 2:
             * Tạo dòng tương ứng trong kho NVL sản xuất.
             *
             * soluongton = 0
             *
             * id và ngaycapnhat:
             * database tự sinh.
             */
            int inserted = jdbc.update(
                    """
                    INSERT INTO kho_nvl_san_xuat
                    (
                        manvl,
                        soluongton
                    )
                    VALUES
                    (
                        ?,
                        0
                    )
                    """,
                    newMaterialId
            );

            if (inserted != 1) {
                throw new IllegalStateException(
                        "Không thể tạo tồn kho nguyên vật liệu nơi sản xuất."
                );
            }

        }

        // =====================================================
        // CẬP NHẬT NVL
        // =====================================================

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

            /*
             * Khi sửa NVL:
             *
             * KHÔNG reset soluongton kho sản xuất.
             * KHÔNG tạo thêm dòng kho sản xuất.
             *
             * Chỉ cập nhật thông tin trong nguyenvatlieu.
             */
        }
    }

    private long insertMaterial(
            String code,
            String name,
            Integer typeId,
            String unit,
            BigDecimal price,
            int minimum,
            int statusValue,
            String note) {

        org.springframework.jdbc.support.GeneratedKeyHolder keyHolder =
                new org.springframework.jdbc.support.GeneratedKeyHolder();

        jdbc.update(connection -> {

            java.sql.PreparedStatement ps =
                    connection.prepareStatement(
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
                            VALUES
                            (?, ?, ?, ?, ?, 0, ?, 0, ?, ?)
                            """,
                            java.sql.Statement.RETURN_GENERATED_KEYS
                    );

            ps.setString(1, code);
            ps.setString(2, name);
            ps.setInt(3, typeId);
            ps.setString(4, unit);
            ps.setBigDecimal(5, price);
            ps.setInt(6, minimum);
            ps.setInt(7, statusValue);
            ps.setString(8, note);

            return ps;

        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException(
                    "Không lấy được mã nguyên vật liệu vừa tạo."
            );
        }

        return key.longValue();
    }

    // =========================
// TỰ ĐỘNG SINH MÃ NVL
// =========================

    public String generateMaterialCode(Integer typeId) {

        if (typeId == null) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn loại nguyên vật liệu."
            );
        }

        String typeName = jdbc.query(
                """
                SELECT tenloai
                FROM loai_nguyen_vat_lieu
                WHERE maloai = ?
                  AND trangthai = 1
                """,
                rs -> {
                    if (!rs.next()) {
                        throw new IllegalArgumentException(
                                "Loại nguyên vật liệu không tồn tại hoặc đã ngừng sử dụng."
                        );
                    }
                    return rs.getString("tenloai");
                },
                typeId
        );

        // Chuẩn hóa tên loại thành phần mã.
        String normalized = java.text.Normalizer.normalize(
                typeName,
                java.text.Normalizer.Form.NFD
        );

        normalized = normalized
                .replace("đ", "d")
                .replace("Đ", "D")
                .replaceAll("\\p{M}", "")
                .toUpperCase(java.util.Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "-")
                .replaceAll("^-|-$", "");

        String prefix = "NVL-" + normalized + "-";

        // Lấy các mã hiện có của loại này.
        List<String> existingCodes = jdbc.queryForList(
                """
                SELECT manvl_code
                FROM nguyenvatlieu
                WHERE manvl_code LIKE ?
                """,
                String.class,
                prefix + "%"
        );

        java.util.Set<Integer> usedNumbers = new java.util.HashSet<>();

        for (String existingCode : existingCodes) {
            if (existingCode == null ||
                    !existingCode.startsWith(prefix)) {
                continue;
            }

            String suffix = existingCode.substring(prefix.length());

            if (suffix.matches("\\d+")) {
                try {
                    usedNumbers.add(Integer.parseInt(suffix));
                } catch (NumberFormatException ignored) {
                    // Bỏ qua hậu tố số vượt giới hạn int.
                }
            }
        }

        // Tìm số thứ tự nhỏ nhất chưa được sử dụng.
        int number = 1;

        while (usedNumbers.contains(number)) {
            number++;
        }

        String code = prefix + String.format(
                java.util.Locale.ROOT,
                "%03d",
                number
        );

        if (code.length() > 30) {
            throw new IllegalArgumentException(
                    "Tên loại quá dài để tạo mã NVL. Vui lòng rút gọn tên loại."
            );
        }

        return code;
    }
}