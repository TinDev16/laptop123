package com.example.demo.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductionOrderService {

    private final JdbcTemplate jdbc;

    public ProductionOrderService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // =========================================================
    // DANH SÁCH YÊU CẦU SẢN XUẤT
    // =========================================================

    public List<Map<String, Object>> list(
            String email,
            boolean manager,
            String search
    ) {
        String sql = "SELECT y.maycsx AS mayeucau, y.soluongyeucau, " +
                "y.soluonghoanthanh, y.ngayyeucau, y.hanhoanthanh, " +
                "y.trangthai, s.tensp, n.hoten " +
                "FROM yeucausanxuat y " +
                "JOIN sanpham s ON s.masp = y.masp " +
                "LEFT JOIN nhanvien n ON n.manv = y.nguoiphutrach ";

        List<Map<String, Object>> rows;

        if (manager) {
            rows = jdbc.queryForList(sql + "ORDER BY y.maycsx DESC");
        } else {
            rows = jdbc.queryForList(
                    sql +
                            "JOIN taikhoan t ON t.manv = y.nguoiphutrach " +
                            "WHERE t.email = ? ORDER BY y.maycsx DESC",
                    email
            );
        }

        for (Map<String, Object> row : rows) {
            addOverdueInfo(row);
        }

        if (search != null &&
                search.trim().equalsIgnoreCase("quá hạn")) {

            rows.removeIf(row -> {
                Object value = row.get("overdueDays");

                if (!(value instanceof Number)) {
                    return true;
                }

                return ((Number) value).longValue() <= 0;
            });
        }

        return rows;
    }

    // =========================================================
    // CHI TIẾT YÊU CẦU
    // =========================================================

    public Map<String, Object> get(long id, String email, boolean manager) {
        String sql = "SELECT y.maycsx AS mayeucau, y.masp, y.mabom, " +
                "y.nguoiphutrach AS manv, y.soluongyeucau, " +
                "y.soluonghoanthanh, y.ngayyeucau, y.hanhoanthanh, " +
                "y.trangthai, y.ghichu, " +
                "s.tensp, n.hoten " +
                "FROM yeucausanxuat y " +
                "JOIN sanpham s ON s.masp = y.masp " +
                "LEFT JOIN nhanvien n ON n.manv = y.nguoiphutrach ";

        List<Map<String, Object>> rows = manager
                ? jdbc.queryForList(
                sql + "WHERE y.maycsx = ?",
                id
        )
                : jdbc.queryForList(
                sql +
                "JOIN taikhoan t ON t.manv = y.nguoiphutrach " +
                "WHERE y.maycsx = ? AND t.email = ?",
                id,
                email
        );

        if (rows.isEmpty()) {
            if (!manager) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Không có quyền xem yêu cầu sản xuất này."
                );
            }

            throw new IllegalArgumentException(
                    "Không tìm thấy yêu cầu sản xuất."
            );
        }

        Map<String, Object> order = rows.get(0);

        addOverdueInfo(order);

        return order;
    }

    // =========================================================
    // BOM
    // =========================================================

    public List<Map<String, Object>> activeBoms() {
        return jdbc.queryForList(
                """
                SELECT
                    b.mabom,
                    b.phienban,
                    s.tensp
                FROM bom b
                JOIN sanpham s
                    ON s.masp = b.masp
                WHERE b.trangthai = 'HIEU_LUC'
                  AND NOT EXISTS (
                        SELECT 1
                        FROM bom_chitiet bc
                        JOIN nguyenvatlieu n
                            ON n.manvl = bc.manvl
                        JOIN loai_nguyen_vat_lieu l
                            ON l.maloai = n.maloai
                        WHERE bc.mabom = b.mabom
                          AND (
                                n.trangthai <> 1
                                OR l.trangthai <> 1
                          )
                  )
                ORDER BY s.tensp
                """
        );
    }

    // =========================================================
    // NHÂN VIÊN
    // =========================================================

    public List<Map<String, Object>> employees() {
        return jdbc.queryForList(
                "SELECT DISTINCT n.manv, n.hoten " +
                        "FROM nhanvien n " +
                        "JOIN taikhoan t ON t.manv = n.manv " +
                        "JOIN vaitro v ON v.vaitro_id = t.vaitro_id " +
                        "WHERE v.ma_vaitro = 'NHAN_VIEN' " +
                        "ORDER BY n.hoten"
        );
    }

    // =========================================================
    // TẠO YÊU CẦU SẢN XUẤT
    // =========================================================

    @Transactional
    public void create(
            String creatorEmail,
            long bomId,
            int employeeId,
            int quantity,
            LocalDate deadline,
            String note
    ) {
        if (note != null && note.length() > 255) {
            throw new IllegalArgumentException(
                    "Ghi chú tối đa 255 ký tự."
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Số lượng sản xuất phải lớn hơn 0."
            );
        }

        if (deadline == null || deadline.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Hạn hoàn thành không hợp lệ."
            );
        }

        List<Integer> productIds = jdbc.query(
                """
                SELECT b.masp
                FROM bom b
                WHERE b.mabom = ?
                  AND b.trangthai = 'HIEU_LUC'
                  AND NOT EXISTS (
                        SELECT 1
                        FROM bom_chitiet bc
                        JOIN nguyenvatlieu n
                            ON n.manvl = bc.manvl
                        JOIN loai_nguyen_vat_lieu l
                            ON l.maloai = n.maloai
                        WHERE bc.mabom = b.mabom
                          AND (
                                n.trangthai <> 1
                                OR l.trangthai <> 1
                          )
                  )
                """,
                (rs, row) -> rs.getInt(1),
                bomId
        );

        if (productIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "BOM không hiệu lực, không tồn tại hoặc có nguyên vật liệu đã ngừng sử dụng."
            );
        }

        Integer creator = jdbc.queryForObject(
                "SELECT manv FROM taikhoan WHERE email = ?",
                Integer.class,
                creatorEmail
        );

        if (creator == null) {
            throw new IllegalArgumentException(
                    "Tài khoản quản lý chưa liên kết với nhân viên."
            );
        }

        jdbc.update(
                "INSERT INTO yeucausanxuat " +
                        "(masp, mabom, nguoitao, nguoiphutrach, " +
                        "soluongyeucau, hanhoanthanh, trangthai, ghichu) " +
                        "VALUES (?, ?, ?, ?, ?, ?, 'DA_GIAO', ?)",
                productIds.get(0),
                bomId,
                creator,
                employeeId,
                quantity,
                deadline,
                note
        );
    }

    // =========================================================
    // LẤY THÔNG TIN CHI TIẾT ĐỂ HIỆN FORM SỬA
    // =========================================================

    public Map<String, Object> getForEdit(long id) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT y.maycsx AS mayeucau, " +
                        "y.masp, y.mabom, " +
                        "y.nguoiphutrach AS manv, " +
                        "y.soluongyeucau, " +
                        "y.soluonghoanthanh, " +
                        "y.ngayyeucau, " +
                        "y.hanhoanthanh, " +
                        "y.trangthai, " +
                        "y.ghichu, " +
                        "s.tensp, n.hoten " +
                        "FROM yeucausanxuat y " +
                        "JOIN sanpham s ON s.masp = y.masp " +
                        "LEFT JOIN nhanvien n ON n.manv = y.nguoiphutrach " +
                        "WHERE y.maycsx = ?",
                id
        );

        if (rows.isEmpty()) {
            throw new IllegalArgumentException(
                    "Không tìm thấy yêu cầu sản xuất."
            );
        }

        return rows.get(0);
    }

    // =========================================================
    // SỬA YÊU CẦU SẢN XUẤT
    // =========================================================

    @Transactional
    public void update(
            String editorEmail,
            long id,
            int newQuantity,
            LocalDate newDeadline,
            int newEmployeeId,
            String newNote
    ) {
        // -----------------------------------------------------
        // Kiểm tra dữ liệu đầu vào
        // -----------------------------------------------------

        if (newQuantity <= 0) {
            throw new IllegalArgumentException(
                    "Số lượng sản xuất phải lớn hơn 0."
            );
        }

        if (newDeadline == null ||
                newDeadline.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Hạn hoàn thành không hợp lệ."
            );
        }

        if (newNote != null && newNote.length() > 255) {
            throw new IllegalArgumentException(
                    "Ghi chú tối đa 255 ký tự."
            );
        }

        // -----------------------------------------------------
        // Lấy yêu cầu hiện tại
        // -----------------------------------------------------

        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT maycsx, soluongyeucau, soluonghoanthanh, " +
                        "hanhoanthanh, nguoiphutrach, trangthai, ghichu " +
                        "FROM yeucausanxuat " +
                        "WHERE maycsx = ?",
                id
        );

        if (rows.isEmpty()) {
            throw new IllegalArgumentException(
                    "Không tìm thấy yêu cầu sản xuất."
            );
        }

        Map<String, Object> current = rows.get(0);

        String status = String.valueOf(
                current.get("trangthai")
        );

        // -----------------------------------------------------
        // Không cho sửa yêu cầu đã hoàn thành / đã hủy
        // -----------------------------------------------------

        if ("HOAN_THANH".equals(status)) {
            throw new IllegalArgumentException(
                    "Không thể sửa yêu cầu đã hoàn thành."
            );
        }

        if ("HUY".equals(status)) {
            throw new IllegalArgumentException(
                    "Không thể sửa yêu cầu đã hủy."
            );
        }

        // -----------------------------------------------------
        // Không được giảm số lượng xuống thấp hơn số lượng
        // đã hoàn thành
        // -----------------------------------------------------

        int completed = ((Number) current.get(
                "soluonghoanthanh"
        )).intValue();

        if (newQuantity < completed) {
            throw new IllegalArgumentException(
                    "Số lượng yêu cầu không được nhỏ hơn " +
                            "số lượng đã hoàn thành (" +
                            completed + ")."
            );
        }

        // -----------------------------------------------------
        // Không cho giảm số lượng yêu cầu xuống thấp hơn
        // tổng số lượng kết quả sản xuất đang CHỜ XÁC NHẬN
        // -----------------------------------------------------

        Integer pendingProduction = jdbc.queryForObject(
                "SELECT COALESCE(SUM(soluonghoanthanh), 0) " +
                        "FROM ketqua_sanxuat " +
                        "WHERE maycsx = ? " +
                        "AND trangthai = 'CHO_XAC_NHAN'",
                Integer.class,
                id
        );

        if (pendingProduction != null &&
                completed + pendingProduction > newQuantity) {

            throw new IllegalArgumentException(
                    "Không thể giảm số lượng yêu cầu xuống " +
                            newQuantity +
                            " vì đã hoàn thành " +
                            completed +
                            " và đang có " +
                            pendingProduction +
                            " sản phẩm chờ xác nhận."
            );
        }

        // -----------------------------------------------------
        // Lấy nhân viên thực hiện chỉnh sửa
        // -----------------------------------------------------

        Integer editorId = jdbc.queryForObject(
                "SELECT manv FROM taikhoan WHERE email = ?",
                Integer.class,
                editorEmail
        );

        if (editorId == null) {
            throw new IllegalArgumentException(
                    "Tài khoản quản lý chưa liên kết với nhân viên."
            );
        }

        // -----------------------------------------------------
        // Kiểm tra nhân viên mới tồn tại
        // -----------------------------------------------------

        Integer employeeCount = jdbc.queryForObject(
                "SELECT COUNT(*) " +
                        "FROM nhanvien n " +
                        "JOIN taikhoan t ON t.manv = n.manv " +
                        "JOIN vaitro v ON v.vaitro_id = t.vaitro_id " +
                        "WHERE n.manv = ? " +
                        "AND v.ma_vaitro = 'NHAN_VIEN'",
                Integer.class,
                newEmployeeId
        );

        if (employeeCount == null || employeeCount == 0) {
            throw new IllegalArgumentException(
                    "Nhân viên phụ trách không hợp lệ."
            );
        }

        // -----------------------------------------------------
        // Giá trị cũ
        // -----------------------------------------------------

        int oldQuantity = ((Number) current.get(
                "soluongyeucau"
        )).intValue();

        int oldEmployee = ((Number) current.get(
                "nguoiphutrach"
        )).intValue();

        LocalDate oldDeadline = toLocalDate(
                current.get("hanhoanthanh")
        );

        // -----------------------------------------------------
        // Ghi lịch sử chỉnh sửa
        // -----------------------------------------------------

        StringBuilder changes = new StringBuilder();

        if (oldQuantity != newQuantity) {
            changes.append("Số lượng: ")
                    .append(oldQuantity)
                    .append(" → ")
                    .append(newQuantity)
                    .append(". ");
        }

        if (!oldEmployeeEquals(oldEmployee, newEmployeeId)) {
            changes.append("Nhân viên phụ trách: ")
                    .append(oldEmployee)
                    .append(" → ")
                    .append(newEmployeeId)
                    .append(". ");
        }

        if (!sameDate(oldDeadline, newDeadline)) {
            changes.append("Hạn hoàn thành: ")
                    .append(oldDeadline)
                    .append(" → ")
                    .append(newDeadline)
                    .append(". ");
        }

        if (changes.length() == 0 &&
                java.util.Objects.equals(
                        current.get("ghichu"),
                        newNote
                )) {
            throw new IllegalArgumentException(
                    "Không có thay đổi nào để cập nhật."
            );
        }

        // Chỉ ghi lịch sử khi có thay đổi thuộc các trường
        // được theo dõi trong bảng lịch sử.
        if (changes.length() > 0) {
            jdbc.update(
                    "INSERT INTO lichsu_dieuchinh_yeucau " +
                            "(maycsx, nguoisua, soluongcu, soluongmoi, " +
                            "han_cu, han_moi, nhanvien_cu, nhanvien_moi, " +
                            "lydodieuchinh) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    id,
                    editorId,
                    oldQuantity,
                    newQuantity,
                    oldDeadline,
                    newDeadline,
                    oldEmployee,
                    newEmployeeId,
                    changes.toString().trim()
            );
        }

        // -----------------------------------------------------
        // Cập nhật yêu cầu
        // -----------------------------------------------------

        jdbc.update(
                "UPDATE yeucausanxuat " +
                        "SET soluongyeucau = ?, " +
                        "hanhoanthanh = ?, " +
                        "nguoiphutrach = ?, " +
                        "ghichu = ? " +
                        "WHERE maycsx = ?",
                newQuantity,
                newDeadline,
                newEmployeeId,
                newNote,
                id
        );
    }

    // =========================================================
    // HỦY YÊU CẦU SẢN XUẤT
    // =========================================================

    @Transactional
    public void cancel(
            String editorEmail,
            long id,
            String reason
    ) {
        // -----------------------------------------------------
        // Kiểm tra lý do
        // -----------------------------------------------------

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập lý do hủy yêu cầu."
            );
        }

        reason = reason.trim();

        if (reason.length() > 255) {
            throw new IllegalArgumentException(
                    "Lý do hủy tối đa 255 ký tự."
            );
        }

        // -----------------------------------------------------
        // Lấy yêu cầu
        // -----------------------------------------------------

        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT maycsx, soluongyeucau, soluonghoanthanh, " +
                        "hanhoanthanh, nguoiphutrach, trangthai " +
                        "FROM yeucausanxuat " +
                        "WHERE maycsx = ?",
                id
        );

        if (rows.isEmpty()) {
            throw new IllegalArgumentException(
                    "Không tìm thấy yêu cầu sản xuất."
            );
        }

        Map<String, Object> order = rows.get(0);

        String status = String.valueOf(
                order.get("trangthai")
        );

        // -----------------------------------------------------
        // Kiểm tra trạng thái
        // -----------------------------------------------------

        if ("HOAN_THANH".equals(status)) {
            throw new IllegalArgumentException(
                    "Không thể hủy yêu cầu đã hoàn thành."
            );
        }

        if ("HUY".equals(status)) {
            throw new IllegalArgumentException(
                    "Yêu cầu này đã được hủy trước đó."
            );
        }

        // -----------------------------------------------------
        // Xác định người hủy
        // -----------------------------------------------------

        Integer editorId = jdbc.queryForObject(
                "SELECT manv FROM taikhoan WHERE email = ?",
                Integer.class,
                editorEmail
        );

        if (editorId == null) {
            throw new IllegalArgumentException(
                    "Tài khoản quản lý chưa liên kết với nhân viên."
            );
        }

        // -----------------------------------------------------
        // Hủy yêu cầu
        //
        // Không tự động hoàn trả NVL ở đây.
        // Nếu trước đó đã xuất NVL thì phải xử lý bằng
        // nghiệp vụ nhập trả / hoàn trả riêng.
        // -----------------------------------------------------

        jdbc.update(
                "UPDATE yeucausanxuat " +
                        "SET trangthai = 'HUY', " +
                        "lydohuy = ?, " +
                        "ngayhuy = NOW() " +
                        "WHERE maycsx = ?",
                reason,
                id
        );
    }

    // =========================================================
    // LỊCH SỬ CHỈNH SỬA
    // =========================================================

    public List<Map<String, Object>> history(long id) {
        // Kiểm tra yêu cầu tồn tại
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) " +
                        "FROM yeucausanxuat " +
                        "WHERE maycsx = ?",
                Integer.class,
                id
        );

        if (count == null || count == 0) {
            throw new IllegalArgumentException(
                    "Không tìm thấy yêu cầu sản xuất."
            );
        }

        return jdbc.queryForList(
                "SELECT l.id, " +
                        "l.maycsx, " +
                        "l.nguoisua, " +
                        "ns.hoten AS tennguoisua, " +
                        "l.thoigian, " +
                        "l.soluongcu, " +
                        "l.soluongmoi, " +
                        "l.han_cu, " +
                        "l.han_moi, " +
                        "l.nhanvien_cu, " +
                        "l.nhanvien_moi, " +
                        "ncu.hoten AS ten_nhanvien_cu, " +
                        "nmoi.hoten AS ten_nhanvien_moi, " +
                        "l.lydodieuchinh " +
                        "FROM lichsu_dieuchinh_yeucau l " +
                        "JOIN nhanvien ns ON ns.manv = l.nguoisua " +
                        "LEFT JOIN nhanvien ncu ON ncu.manv = l.nhanvien_cu " +
                        "LEFT JOIN nhanvien nmoi ON nmoi.manv = l.nhanvien_moi " +
                        "WHERE l.maycsx = ? " +
                        "ORDER BY l.thoigian DESC, l.id DESC",
                id
        );
    }

    // =========================================================
    // NHU CẦU NGUYÊN VẬT LIỆU
    // =========================================================

    public List<MaterialNeed> materialNeeds(
            Map<String, Object> order
    ) {
        long bomId = ((Number) order.get(
                "mabom"
        )).longValue();

        int remaining =
                ((Number) order.get(
                        "soluongyeucau"
                )).intValue()
                        -
                        ((Number) order.get(
                                "soluonghoanthanh"
                        )).intValue();

        List<Map<String, Object>> lines =
                jdbc.queryForList(
                        "SELECT n.manvl_code, n.tennvl, n.donvitinh, " +
                                "n.soluongton, " +
                                "c.soluong_dinhmuc, " +
                                "c.tyle_haohut " +
                                "FROM bom_chitiet c " +
                                "JOIN nguyenvatlieu n " +
                                "ON n.manvl = c.manvl " +
                                "WHERE c.mabom = ? " +
                                "ORDER BY n.tennvl",
                        bomId
                );

        List<MaterialNeed> result = new ArrayList<>();

        for (Map<String, Object> line : lines) {
            BigDecimal standard =
                    (BigDecimal) line.get(
                            "soluong_dinhmuc"
                    );

            BigDecimal waste =
                    (BigDecimal) line.get(
                            "tyle_haohut"
                    );

            BigDecimal needed =
                    standard
                            .multiply(
                                    BigDecimal.valueOf(
                                            Math.max(remaining, 0)
                                    )
                            )
                            .multiply(
                                    BigDecimal.ONE.add(
                                            waste.movePointLeft(2)
                                    )
                            )
                            .setScale(
                                    0,
                                    RoundingMode.CEILING
                            );

            BigDecimal available =
                    (BigDecimal) line.get(
                            "soluongton"
                    );

            result.add(
                    new MaterialNeed(
                            (String) line.get("manvl_code"),
                            (String) line.get("tennvl"),
                            (String) line.get("donvitinh"),
                            standard,
                            waste,
                            needed,
                            available,
                            needed.subtract(available)
                                    .max(BigDecimal.ZERO)
                    )
            );
        }

        return result;
    }

    // =========================================================
    // OVERDUE
    // =========================================================

    private void addOverdueInfo(
            Map<String, Object> order
    ) {
        Object deadlineValue =
                order.get("hanhoanthanh");

        if (deadlineValue == null) {
            order.put("overdueDays", 0L);
            return;
        }

        LocalDate deadline =
                toLocalDate(deadlineValue);

        boolean completed =
                "HOAN_THANH".equals(
                        order.get("trangthai")
                );

        boolean cancelled =
                "HUY".equals(
                        order.get("trangthai")
                );

        long overdueDays = 0;

        if (!completed &&
                !cancelled &&
                LocalDate.now().isAfter(deadline)) {

            overdueDays =
                    ChronoUnit.DAYS.between(
                            deadline,
                            LocalDate.now()
                    );
        }

        order.put("overdueDays", overdueDays);
    }

    // =========================================================
    // HELPER
    // =========================================================

    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof java.sql.Date date) {
            return date.toLocalDate();
        }

        if (value instanceof LocalDate date) {
            return date;
        }

        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }

        return LocalDate.parse(
                value.toString()
        );
    }

    private boolean sameDate(
            LocalDate first,
            LocalDate second
    ) {
        if (first == null && second == null) {
            return true;
        }

        if (first == null || second == null) {
            return false;
        }

        return first.equals(second);
    }

    private boolean oldEmployeeEquals(
            int oldEmployee,
            int newEmployee
    ) {
        return oldEmployee == newEmployee;
    }

    // =========================================================
    // DTO NHU CẦU NVL
    // =========================================================

    public record MaterialNeed(
            String code,
            String name,
            String unit,
            BigDecimal standard,
            BigDecimal waste,
            BigDecimal needed,
            BigDecimal available,
            BigDecimal shortage
    ) {
    }
}