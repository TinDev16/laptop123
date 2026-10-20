package com.example.demo.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
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
                "y.soluonghoanthanh, COALESCE(p.soluongchoduyet, 0) AS soluongchoduyet, " +
                "y.ngayyeucau, y.hanhoanthanh, " +
                "y.trangthai, s.tensp, n.hoten, d.madot, d.madot_code, d.tendot " +
                "FROM yeucausanxuat y " +
                "JOIN sanpham s ON s.masp = y.masp " +
                "LEFT JOIN nhanvien n ON n.manv = y.nguoiphutrach " +
                "LEFT JOIN dot_san_xuat d ON d.madot = y.madot " +
                "LEFT JOIN (" +
                "SELECT maycsx, SUM(soluonghoanthanh) AS soluongchoduyet " +
                "FROM ketqua_sanxuat WHERE trangthai = 'CHO_XAC_NHAN' GROUP BY maycsx" +
                ") p ON p.maycsx = y.maycsx ";

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
        } else if (search != null && !search.isBlank()) {
            String keyword = search.trim().toLowerCase();
            rows.removeIf(row -> !contains(row.get("madot_code"), keyword)
                    && !contains(row.get("tendot"), keyword)
                    && !contains(row.get("mayeucau"), keyword)
                    && !contains("YCSX-" + row.get("mayeucau"), keyword)
                    && !contains(row.get("tensp"), keyword)
                    && !contains(row.get("hoten"), keyword));
        }

        return rows;
    }

    public List<Map<String, Object>> listGrouped(
            String email,
            boolean manager,
            String search
    ) {
        String sql = """
        SELECT
            COALESCE(y.madot, -y.maycsx) AS group_key,
            d.madot,
            d.madot_code,
            d.tendot,
            MIN(y.maycsx) AS mayeucau,
            GROUP_CONCAT(
                DISTINCT s.tensp
                ORDER BY s.tensp SEPARATOR ', '
            ) AS tensp,
            COUNT(DISTINCT y.maycsx) AS soyeucau,
SUM(
    CASE
        WHEN y.trangthai <> 'HUY' OR y.trangthai IS NULL
        THEN y.soluongyeucau
        ELSE 0
    END
) AS soluongyeucau,

SUM(
    CASE
        WHEN y.trangthai <> 'HUY' OR y.trangthai IS NULL
        THEN y.soluonghoanthanh
        ELSE 0
    END
) AS soluonghoanthanh,

SUM(
    CASE
        WHEN y.trangthai <> 'HUY' OR y.trangthai IS NULL
        THEN COALESCE(p.soluongchoduyet, 0)
        ELSE 0
    END
) AS soluongchoduyet,
            GROUP_CONCAT(
                DISTINCT n.hoten
                ORDER BY n.hoten SEPARATOR ', '
            ) AS hoten,
            MAX(
                        CASE
                            WHEN y.trangthai <> 'HUY' OR y.trangthai IS NULL
                            THEN y.hanhoanthanh
                            ELSE NULL
                        END
                    ) AS hanhoanthanh,
CASE
    -- Tất cả sản phẩm đều bị hủy
    WHEN SUM(
        CASE
            WHEN y.trangthai = 'HUY' THEN 0
            ELSE 1
        END
    ) = 0 THEN 'HUY'

    -- Tất cả sản phẩm còn hiệu lực đã hoàn thành
    WHEN SUM(
        CASE
            WHEN y.trangthai <> 'HUY' OR y.trangthai IS NULL
            THEN GREATEST(
                COALESCE(y.soluongyeucau, 0)
                - COALESCE(y.soluonghoanthanh, 0),
                0
            )
            ELSE 0
        END
    ) = 0 THEN 'HOAN_THANH'

    ELSE 'DANG_THUC_HIEN'
END AS trangthai
        FROM yeucausanxuat y
        JOIN sanpham s ON s.masp = y.masp
        LEFT JOIN nhanvien n ON n.manv = y.nguoiphutrach
        LEFT JOIN dot_san_xuat d ON d.madot = y.madot
        LEFT JOIN (
            SELECT maycsx, SUM(soluonghoanthanh) AS soluongchoduyet
            FROM ketqua_sanxuat
            WHERE trangthai = 'CHO_XAC_NHAN'
            GROUP BY maycsx
        ) p ON p.maycsx = y.maycsx
        """;

        List<Map<String, Object>> rows;

        if (manager) {
            rows = jdbc.queryForList(
                    sql + """
                GROUP BY
                    COALESCE(y.madot, -y.maycsx),
                    d.madot, d.madot_code, d.tendot
                ORDER BY MAX(y.maycsx) DESC
                """
            );
        } else {
            rows = jdbc.queryForList(
                    sql + """
                JOIN taikhoan t ON t.manv = y.nguoiphutrach
                WHERE t.email = ?
                GROUP BY
                    COALESCE(y.madot, -y.maycsx),
                    d.madot, d.madot_code, d.tendot
                ORDER BY MAX(y.maycsx) DESC
                """,
                    email
            );
        }

        for (Map<String, Object> row : rows) {

            // Chỉ tính tình trạng NVL cho tài khoản nhân viên.
            if (!manager) {
                row.put("tinhtrangnvl", getMaterialStatus(email, row));
            }

            int requested = ((Number) row.get("soluongyeucau")).intValue();
            int confirmed = ((Number) row.get("soluonghoanthanh")).intValue();
            int pending = ((Number) row.get("soluongchoduyet")).intValue();

            row.put("soluongdahoanthanh", confirmed + pending);
            row.put(
                    "soluongconphaisanxuat",
                    Math.max(requested - confirmed - pending, 0)
            );

            // Tính quá hạn theo tổng số lượng của đợt.
            addOverdueInfo(row);

            // Giữ tương thích với template và cách hiển thị cũ.
            row.put(
                    "tenhienthi",
                    row.get("tendot") != null
                            ? row.get("tendot")
                            : "Yêu cầu sản xuất cũ"
            );
        }

        if (search == null || search.isBlank()) {
            return rows;
        }

        String keyword = search.trim().toLowerCase();

        if ("quá hạn".equalsIgnoreCase(keyword)) {
            rows.removeIf(row ->
                    !(row.get("overdueDays") instanceof Number value)
                            || value.longValue() <= 0
            );
            return rows;
        }

        rows.removeIf(row ->
                !contains(row.get("madot_code"), keyword)
                        && !contains(row.get("tendot"), keyword)
                        && !contains(row.get("tensp"), keyword)
                        && !contains(row.get("hoten"), keyword)
                        && !contains("YCSX-" + row.get("mayeucau"), keyword)
        );

        return rows;
    }

    public List<Map<String, Object>> getBatchOrders(
            long batchId,
            String email,
            boolean manager
    ) {
        List<Long> ids;

        if (manager) {
            ids = jdbc.queryForList(
                    """
                    SELECT maycsx
                    FROM yeucausanxuat
                    WHERE madot = ?
                    ORDER BY maycsx
                    """,
                    Long.class,
                    batchId
            );
        } else {
            ids = jdbc.queryForList(
                    """
                    SELECT y.maycsx
                    FROM yeucausanxuat y
                    JOIN taikhoan t ON t.manv = y.nguoiphutrach
                    WHERE y.madot = ?
                      AND t.email = ?
                    ORDER BY y.maycsx
                    """,
                    Long.class,
                    batchId,
                    email
            );
        }

        if (ids.isEmpty()) {
            throw new IllegalArgumentException(
                    "Không tìm thấy đợt sản xuất hoặc bạn không có quyền xem đợt này."
            );
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (Long id : ids) {
            Map<String, Object> order = get(id, email, manager);
            order.put("needs", materialNeeds(order));
            result.add(order);
        }

        return result;
    }

    public List<Map<String, Object>> aggregateMaterialShortages(String email) {
        return jdbc.queryForList(
                """
                SELECT
                    requirements.manvl,
                    requirements.manvl_code,
                    requirements.tennvl,
                    requirements.donvitinh,
                    requirements.soyeucau,
                    requirements.tongnhucau,
                    requirements.tongdangdenghi,
                    COALESCE(k.soluongton, 0) AS soluongton,
                    GREATEST(
                        requirements.tongnhucau
                            - COALESCE(k.soluongton, 0)
                            - requirements.tongdangdenghi,
                        0
                    ) AS tongthieu
                FROM (
                    SELECT
                        n.manvl,
                        n.manvl_code,
                        n.tennvl,
                        n.donvitinh,
                        COUNT(DISTINCT y.maycsx) AS soyeucau,
                        SUM(
                            CEIL(
                                GREATEST(
                                    y.soluongyeucau
                                    - GREATEST(
                                        COALESCE(g.daghi, 0),
                                        y.soluonghoanthanh + COALESCE(r.dangcho, 0)
                                    ),
                                    0
                                )
                                * c.soluong_dinhmuc
                                * (1 + c.tyle_haohut / 100)
                            )
                        ) AS tongnhucau,
                        SUM(COALESCE(pending.soluong, 0)) AS tongdangdenghi
                    FROM yeucausanxuat y
                    JOIN taikhoan t ON t.manv = y.nguoiphutrach
                    JOIN bom_chitiet c ON c.mabom = y.mabom
                    JOIN nguyenvatlieu n ON n.manvl = c.manvl
                    LEFT JOIN (
                        SELECT maycsx, SUM(soluong) AS daghi
                        FROM ghi_nhan_san_xuat
                        GROUP BY maycsx
                    ) g ON g.maycsx = y.maycsx
                    LEFT JOIN (
                        SELECT maycsx, SUM(soluonghoanthanh) AS dangcho
                        FROM ketqua_sanxuat
                        WHERE trangthai = 'CHO_XAC_NHAN'
                        GROUP BY maycsx
                    ) r ON r.maycsx = y.maycsx
                    LEFT JOIN (
                        SELECT p.maycsx, c.manvl, SUM(c.soluong) AS soluong
                        FROM phieuxuat_nvl p
                        JOIN ct_phieuxuat_nvl c ON c.mapx = p.mapx
                        WHERE p.trangthai IN ('CHO_DUYET', 'DA_DUYET')
                        GROUP BY p.maycsx, c.manvl
                    ) pending ON pending.maycsx = y.maycsx
                             AND pending.manvl = n.manvl
                    WHERE t.email = ?
                      AND y.trangthai NOT IN ('HOAN_THANH', 'HUY')
                      AND GREATEST(
                            COALESCE(g.daghi, 0),
                            y.soluonghoanthanh + COALESCE(r.dangcho, 0)
                          ) < y.soluongyeucau
                    GROUP BY n.manvl, n.manvl_code, n.tennvl, n.donvitinh
                ) requirements
                LEFT JOIN kho_nvl_san_xuat k ON k.manvl = requirements.manvl
                WHERE requirements.tongnhucau
                        > COALESCE(k.soluongton, 0) + requirements.tongdangdenghi
                ORDER BY tongthieu DESC, requirements.tennvl
                """,
                email
        );
    }

    // =========================================================
    // CHI TIẾT YÊU CẦU
    // =========================================================

    public Map<String, Object> get(long id, String email, boolean manager) {
        String sql = "SELECT y.maycsx AS mayeucau, y.masp, y.mabom, " +
                "y.nguoiphutrach AS manv, y.soluongyeucau, " +
                "y.soluonghoanthanh, y.ngayyeucau, y.hanhoanthanh, " +
                "y.trangthai, y.ghichu, " +
                "COALESCE(g.soluongdaghinhan, 0) AS soluongdaghinhan, " +
                "COALESCE(p.soluongchoduyet, 0) AS soluongchoduyet, " +
                "s.tensp, b.phienban, CONCAT(s.tensp, ' / BOM ', b.phienban) AS tenbom, " +
                "n.hoten, d.madot, d.madot_code, d.tendot, d.ghichu AS ghichu_dot " +
                "FROM yeucausanxuat y " +
                "JOIN sanpham s ON s.masp = y.masp " +
                "JOIN bom b ON b.mabom = y.mabom " +
                "LEFT JOIN nhanvien n ON n.manv = y.nguoiphutrach " +
                "LEFT JOIN dot_san_xuat d ON d.madot = y.madot " +
                "LEFT JOIN (SELECT maycsx, SUM(soluong) AS soluongdaghinhan " +
                "FROM ghi_nhan_san_xuat GROUP BY maycsx) g ON g.maycsx = y.maycsx " +
                "LEFT JOIN (SELECT maycsx, SUM(soluonghoanthanh) AS soluongchoduyet " +
                "FROM ketqua_sanxuat WHERE trangthai = 'CHO_XAC_NHAN' GROUP BY maycsx) p " +
                "ON p.maycsx = y.maycsx ";

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

        int requested = ((Number) order.get("soluongyeucau")).intValue();
        int confirmed = ((Number) order.get("soluonghoanthanh")).intValue();
        int recorded = ((Number) order.get("soluongdaghinhan")).intValue();
        int pendingResult = ((Number) order.get("soluongchoduyet")).intValue();
        int effectiveProduced = Math.max(recorded, confirmed + pendingResult);

        order.put("soluongdasanxuat", effectiveProduced);
        order.put(
                "soluongchuaguiketqua",
                Math.max(recorded - confirmed - pendingResult, 0)
        );
        order.put(
                "soluongconphaisanxuat",
                Math.max(requested - effectiveProduced, 0)
        );

        addOverdueInfo(order);

        return order;
    }

    @Transactional
    public long repairMissingProductionResult(
            long orderId,
            String email,
            boolean manager
    ) {
        String baseSql = """
                SELECT
                    y.maycsx,
                    y.masp,
                    y.mabom,
                    y.nguoiphutrach,
                    y.soluongyeucau,
                    y.soluonghoanthanh,
                    y.trangthai
                FROM yeucausanxuat y
                """;

        List<Map<String, Object>> orders = manager
                ? jdbc.queryForList(
                        baseSql + " WHERE y.maycsx = ? FOR UPDATE",
                        orderId
                )
                : jdbc.queryForList(
                        baseSql +
                                " JOIN taikhoan t ON t.manv = y.nguoiphutrach " +
                                " WHERE y.maycsx = ? AND t.email = ? FOR UPDATE",
                        orderId,
                        email
                );

        if (orders.isEmpty()) {
            throw new IllegalArgumentException(
                    "Không tìm thấy yêu cầu sản xuất hoặc bạn không có quyền xử lý."
            );
        }

        Map<String, Object> order = orders.get(0);
        String status = String.valueOf(order.get("trangthai"));
        if ("HUY".equals(status) || "HOAN_THANH".equals(status)) {
            throw new IllegalStateException(
                    "Yêu cầu sản xuất đã kết thúc, không thể khôi phục kết quả."
            );
        }

        int requested = ((Number) order.get("soluongyeucau")).intValue();
        int confirmed = ((Number) order.get("soluonghoanthanh")).intValue();

        Integer recorded = jdbc.queryForObject(
                "SELECT COALESCE(SUM(soluong), 0) FROM ghi_nhan_san_xuat WHERE maycsx = ?",
                Integer.class,
                orderId
        );
        Integer pending = jdbc.queryForObject(
                "SELECT COALESCE(SUM(soluonghoanthanh), 0) FROM ketqua_sanxuat " +
                        "WHERE maycsx = ? AND trangthai = 'CHO_XAC_NHAN'",
                Integer.class,
                orderId
        );

        int recordedValue = recorded == null ? 0 : recorded;
        int pendingValue = pending == null ? 0 : pending;
        int unmatchedRecorded = Math.max(
                recordedValue - confirmed - pendingValue,
                0
        );
        int remainingResultCapacity = Math.max(
                requested - confirmed - pendingValue,
                0
        );
        int quantity = Math.min(unmatchedRecorded, remainingResultCapacity);

        if (quantity <= 0) {
            throw new IllegalStateException(
                    "Không còn sản lượng ghi nhận cũ nào cần tạo kết quả chờ duyệt."
            );
        }

        int productId = ((Number) order.get("masp")).intValue();
        int bomId = ((Number) order.get("mabom")).intValue();
        Number stockValue = jdbc.queryForObject(
                "SELECT COALESCE(soluongton, 0) FROM kho_thanhpham_san_xuat " +
                        "WHERE masp = ? AND mabom = ? FOR UPDATE",
                Number.class,
                productId,
                bomId
        );
        int productionStock = stockValue == null ? 0 : stockValue.intValue();
        if (productionStock < quantity) {
            throw new IllegalStateException(
                    "Kho thành phẩm sản xuất không còn đủ số lượng để khôi phục kết quả."
            );
        }

        List<Integer> workers = jdbc.queryForList(
                "SELECT nguoighi FROM ghi_nhan_san_xuat " +
                        "WHERE maycsx = ? ORDER BY ngayghi DESC, id DESC LIMIT 1",
                Integer.class,
                orderId
        );
        int workerId = workers.isEmpty()
                ? ((Number) order.get("nguoiphutrach")).intValue()
                : workers.get(0);

        jdbc.update(
                "INSERT INTO ketqua_sanxuat " +
                        "(maycsx, nguoithuchien, soluonghoanthanh, soluongloi, trangthai, ghichu) " +
                        "VALUES (?, ?, ?, 0, 'CHO_XAC_NHAN', ?)",
                orderId,
                workerId,
                quantity,
                "Khôi phục từ sản lượng đã ghi nhận tại xưởng trước khi hợp nhất quy trình."
        );

        Long resultId = jdbc.queryForObject(
                "SELECT LAST_INSERT_ID()",
                Long.class
        );
        if (resultId == null) {
            throw new IllegalStateException("Không tạo được kết quả sản xuất chờ duyệt.");
        }
        return resultId;
    }

    public int pendingResultCount(long orderId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM ketqua_sanxuat " +
                        "WHERE maycsx = ? AND trangthai = 'CHO_XAC_NHAN'",
                Integer.class,
                orderId
        );
        return count == null ? 0 : count;
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

    public List<Map<String, Object>> activeProducts() {
        return jdbc.queryForList("""
        SELECT DISTINCT s.masp, s.tensp
        FROM sanpham s
        JOIN bom b ON b.masp = s.masp
        WHERE b.trangthai = 'HIEU_LUC'
          AND NOT EXISTS (
              SELECT 1
              FROM bom_chitiet bc
              JOIN nguyenvatlieu n ON n.manvl = bc.manvl
              JOIN loai_nguyen_vat_lieu l ON l.maloai = n.maloai
              WHERE bc.mabom = b.mabom
                AND (n.trangthai <> 1 OR l.trangthai <> 1)
          )
        ORDER BY s.tensp
        """);
    }

    public List<Map<String, Object>> activeBomsByProduct(long productId) {
        return jdbc.queryForList("""
        SELECT b.mabom, b.phienban, s.tensp
        FROM bom b
        JOIN sanpham s ON s.masp = b.masp
        WHERE b.masp = ?
          AND b.trangthai = 'HIEU_LUC'
          AND NOT EXISTS (
              SELECT 1
              FROM bom_chitiet bc
              JOIN nguyenvatlieu n ON n.manvl = bc.manvl
              JOIN loai_nguyen_vat_lieu l ON l.maloai = n.maloai
              WHERE bc.mabom = b.mabom
                AND (n.trangthai <> 1 OR l.trangthai <> 1)
          )
        ORDER BY b.phienban DESC
        """, productId);
    }

    // =========================================================
    // NHÂN VIÊN
    // =========================================================

// =========================================================
// NHÂN VIÊN PHỤ TRÁCH
// Chỉ hiển thị nhân viên:
// 1. Đang làm việc
// 2. Có tài khoản
// 3. Tài khoản chưa bị khóa
// 4. Có vai trò NHAN_VIEN
// =========================================================

    public List<Map<String, Object>> employees() {
        return jdbc.queryForList(
                """
                SELECT DISTINCT
                    n.manv,
                    n.hoten
                FROM nhanvien n
                JOIN taikhoan t
                    ON t.manv = n.manv
                JOIN vaitro v
                    ON v.vaitro_id = t.vaitro_id
                WHERE n.trangthai = 0
                  AND t.locked = false
                  AND v.ma_vaitro = 'NHAN_VIEN'
                ORDER BY n.hoten
                """
        );
    }

    // =========================================================
    // TẠO YÊU CẦU SẢN XUẤT
    // =========================================================

// =========================================================
// TẠO ĐỢT SẢN XUẤT - MỖI DÒNG CÓ NHÂN VIÊN VÀ HẠN RIÊNG
// =========================================================

    @Transactional
    public int createBatch(
            String creatorEmail,
            String batchName,
            List<Long> bomIds,
            List<Integer> employeeIds,
            List<Integer> quantities,
            List<LocalDate> deadlines,
            String note
    ) {
        // 1. Kiểm tra tên đợt và ghi chú
        String normalizedName = batchName == null
                ? ""
                : batchName.trim();

        if (normalizedName.length() < 3
                || normalizedName.length() > 150) {
            throw new IllegalArgumentException(
                    "Tên đợt sản xuất phải từ 3 đến 150 ký tự."
            );
        }

        if (note != null && note.length() > 255) {
            throw new IllegalArgumentException(
                    "Ghi chú tối đa 255 ký tự."
            );
        }

        // 2. Kiểm tra số lượng dòng gửi lên
        if (bomIds == null || bomIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Yêu cầu phải có ít nhất một thành phẩm."
            );
        }

        int size = bomIds.size();

        if (employeeIds == null || employeeIds.size() != size) {
            throw new IllegalArgumentException(
                    "Thông tin nhân viên phụ trách không đầy đủ."
            );
        }

        if (quantities == null || quantities.size() != size) {
            throw new IllegalArgumentException(
                    "Thông tin số lượng thành phẩm không đầy đủ."
            );
        }

        if (deadlines == null || deadlines.size() != size) {
            throw new IllegalArgumentException(
                    "Thông tin hạn hoàn thành không đầy đủ."
            );
        }

        // 3. Kiểm tra dữ liệu riêng của từng dòng
        LocalDate today = LocalDate.now();

        for (int index = 0; index < size; index++) {
            Long bomId = bomIds.get(index);
            Integer employeeId = employeeIds.get(index);
            Integer quantity = quantities.get(index);
            LocalDate deadline = deadlines.get(index);

            if (bomId == null || bomId <= 0) {
                throw new IllegalArgumentException(
                        "Vui lòng chọn BOM cho thành phẩm thứ "
                                + (index + 1) + "."
                );
            }

            if (employeeId == null || employeeId <= 0) {
                throw new IllegalArgumentException(
                        "Vui lòng chọn nhân viên cho thành phẩm thứ "
                                + (index + 1) + "."
                );
            }

            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException(
                        "Số lượng thành phẩm thứ "
                                + (index + 1)
                                + " phải lớn hơn 0."
                );
            }

            if (deadline == null || deadline.isBefore(today)) {
                throw new IllegalArgumentException(
                        "Hạn hoàn thành của thành phẩm thứ "
                                + (index + 1)
                                + " không được ở quá khứ."
                );
            }
        }

        // Không cấm trùng BOM:
        // Cùng BOM có thể được giao cho nhiều nhân viên khác nhau,
        // mỗi dòng có số lượng và hạn hoàn thành riêng.

        // 4. Xác định người tạo đợt
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

        // 5. Tạo đợt sản xuất dùng chung
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            var statement = connection.prepareStatement(
                    """
                    INSERT INTO dot_san_xuat
                        (tendot, nguoitao, ghichu)
                    VALUES (?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS
            );

            statement.setString(1, normalizedName);
            statement.setInt(2, creator);
            statement.setString(3, note);

            return statement;
        }, keyHolder);

        Number generatedKey = keyHolder.getKey();

        if (generatedKey == null) {
            throw new IllegalStateException(
                    "Không thể tạo mã đợt sản xuất."
            );
        }

        long batchId = generatedKey.longValue();

        String batchCode = "SX-"
                + today.format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-"
                + String.format("%06d", batchId);

        jdbc.update(
                "UPDATE dot_san_xuat SET madot_code = ? WHERE madot = ?",
                batchCode,
                batchId
        );

        // 6. Tạo từng yêu cầu con với dữ liệu tương ứng theo chỉ số
        for (int index = 0; index < size; index++) {
            createInternal(
                    creatorEmail,
                    bomIds.get(index),
                    employeeIds.get(index),
                    quantities.get(index),
                    deadlines.get(index),
                    note,
                    batchId
            );
        }

        // Transaction sẽ rollback toàn bộ nếu một dòng tạo thất bại.
        return size;
    }

    @Transactional
    public void create(
            String creatorEmail,
            long bomId,
            int employeeId,
            int quantity,
            LocalDate deadline,
            String note
    ) {
        createInternal(creatorEmail, bomId, employeeId, quantity, deadline, note, null);
    }

    private void createInternal(
            String creatorEmail,
            long bomId,
            int employeeId,
            int quantity,
            LocalDate deadline,
            String note,
            Long batchId
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

        Integer employeeCount = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM nhanvien n
                JOIN taikhoan t
                    ON t.manv = n.manv
                JOIN vaitro v
                    ON v.vaitro_id = t.vaitro_id
                WHERE n.manv = ?
                  AND n.trangthai = 0
                  AND t.locked = false
                  AND v.ma_vaitro = 'NHAN_VIEN'
                """,
                Integer.class,
                employeeId
        );

        if (employeeCount == null || employeeCount == 0) {
            throw new IllegalArgumentException(
                    "Nhân viên phụ trách không còn hoạt động hoặc tài khoản đã bị khóa."
            );
        }

        jdbc.update(
                "INSERT INTO yeucausanxuat " +
                        "(masp, mabom, nguoitao, nguoiphutrach, " +
                        "soluongyeucau, hanhoanthanh, trangthai, ghichu, madot) " +
                        "VALUES (?, ?, ?, ?, ?, ?, 'DA_GIAO', ?, ?)",
                productIds.get(0),
                bomId,
                creator,
                employeeId,
                quantity,
                deadline,
                note,
                batchId
        );
    }

    private boolean contains(Object value, String keyword) {
        return value != null && String.valueOf(value).toLowerCase().contains(keyword);
    }

    // =========================================================
    // LẤY THÔNG TIN CHI TIẾT ĐỂ HIỆN FORM SỬA
    // =========================================================

    public Map<String, Object> getForEdit(long id) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                """
                SELECT
                    y.maycsx AS mayeucau,
                    y.masp,
                    y.mabom,
                    y.madot,
                    y.nguoiphutrach AS manv,
                    y.soluongyeucau,
                    y.soluonghoanthanh,
                    y.ngayyeucau,
                    y.hanhoanthanh,
                    y.trangthai,
                    y.ghichu,
                    s.tensp,
                    n.hoten,
                    d.madot_code,
                    d.tendot
                FROM yeucausanxuat y
                JOIN sanpham s
                    ON s.masp = y.masp
                LEFT JOIN nhanvien n
                    ON n.manv = y.nguoiphutrach
                LEFT JOIN dot_san_xuat d
                    ON d.madot = y.madot
                WHERE y.maycsx = ?
                """,
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
                        "SET trangthai_truoc_khi_huy = trangthai, " +
                        "    trangthai = 'HUY', " +
                        "    lydohuy = ?, " +
                        "    ngayhuy = NOW() " +
                        "WHERE maycsx = ?",
                reason,
                id
        );
    }

    @Transactional
    public void cancelBatch(
            String editorEmail,
            Long batchId,
            String reason
    ) {
        // -----------------------------------------------------
        // 1. Kiểm tra lý do hủy
        // -----------------------------------------------------

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập lý do hủy đợt sản xuất."
            );
        }

        reason = reason.trim();

        if (reason.length() > 255) {
            throw new IllegalArgumentException(
                    "Lý do hủy tối đa 255 ký tự."
            );
        }

        // -----------------------------------------------------
        // 2. Kiểm tra đợt sản xuất tồn tại
        // -----------------------------------------------------

        Integer batchExists = jdbc.queryForObject(
                "SELECT COUNT(*) FROM dot_san_xuat WHERE madot = ?",
                Integer.class,
                batchId
        );

        if (batchExists == null || batchExists == 0) {
            throw new IllegalArgumentException(
                    "Không tìm thấy đợt sản xuất."
            );
        }

        // -----------------------------------------------------
        // 3. Lấy tất cả yêu cầu trong đợt và khóa các dòng
        // -----------------------------------------------------

        List<Map<String, Object>> orders = jdbc.queryForList(
                "SELECT maycsx, trangthai " +
                        "FROM yeucausanxuat " +
                        "WHERE madot = ? " +
                        "FOR UPDATE",
                batchId
        );

        if (orders.isEmpty()) {
            throw new IllegalArgumentException(
                    "Đợt sản xuất này không có yêu cầu nào."
            );
        }

// -----------------------------------------------------
// 4. Kiểm tra trạng thái các yêu cầu
//    - Đã hoàn thành: không cho hủy đợt
//    - Đã hủy: bỏ qua
//    - Còn lại: tiếp tục xử lý
// -----------------------------------------------------

        boolean hasOrderToCancel = false;

        for (Map<String, Object> order : orders) {
            String status = String.valueOf(order.get("trangthai"));

            if ("HOAN_THANH".equals(status)) {
                throw new IllegalArgumentException(
                        "Không thể hủy đợt vì có yêu cầu đã hoàn thành."
                );
            }

            if (!"HUY".equals(status)) {
                hasOrderToCancel = true;
            }
        }

        if (!hasOrderToCancel) {
            throw new IllegalArgumentException(
                    "Tất cả yêu cầu trong đợt này đã bị hủy trước đó."
            );
        }

        // -----------------------------------------------------
        // 5. Xác định tài khoản người hủy
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
// 6. Chỉ hủy những yêu cầu chưa bị hủy
//    Không tự động hoàn trả nguyên vật liệu.
// -----------------------------------------------------

        int updated = jdbc.update(
                "UPDATE yeucausanxuat " +
                        "SET trangthai_truoc_khi_huy = trangthai, " +
                        "    trangthai = 'HUY', " +
                        "    lydohuy = ?, " +
                        "    ngayhuy = NOW() " +
                        "WHERE madot = ? " +
                        "AND (trangthai IS NULL OR trangthai <> 'HUY')",
                reason,
                batchId
        );

        if (updated != orders.stream()
                .filter(order -> !"HUY".equals(
                        String.valueOf(order.get("trangthai"))
                ))
                .count()) {
            throw new IllegalStateException(
                    "Số lượng yêu cầu được hủy không khớp. " +
                            "Vui lòng tải lại trang và thử lại."
            );
        }
    }

    @Transactional
    public void uncancelBatch(
            String editorEmail,
            Long batchId
    ) {
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

        List<Map<String, Object>> orders = jdbc.queryForList(
                "SELECT maycsx, trangthai, trangthai_truoc_khi_huy " +
                        "FROM yeucausanxuat WHERE madot = ? FOR UPDATE",
                batchId
        );

        if (orders.isEmpty()) {
            throw new IllegalArgumentException(
                    "Không tìm thấy yêu cầu trong đợt sản xuất."
            );
        }

        for (Map<String, Object> order : orders) {
            if (!"HUY".equals(
                    String.valueOf(order.get("trangthai"))
            )) {
                throw new IllegalArgumentException(
                        "Không thể bỏ hủy vì có yêu cầu chưa bị hủy."
                );
            }

            if (order.get("trangthai_truoc_khi_huy") == null) {
                throw new IllegalArgumentException(
                        "Yêu cầu này đã bị hủy trước khi có chức năng lưu trạng thái cũ. " +
                                "Không thể khôi phục chính xác trạng thái ban đầu."
                );
            }
        }

        for (Map<String, Object> order : orders) {
            jdbc.update(
                    "UPDATE yeucausanxuat " +
                            "SET trangthai = trangthai_truoc_khi_huy, " +
                            "    trangthai_truoc_khi_huy = NULL, " +
                            "    lydohuy = NULL, " +
                            "    ngayhuy = NULL " +
                            "WHERE maycsx = ? AND trangthai = 'HUY'",
                    order.get("maycsx")
            );
        }
    }

    @Transactional
    public void uncancel(String editorEmail, long id) {
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

        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT maycsx, trangthai, trangthai_truoc_khi_huy " +
                        "FROM yeucausanxuat WHERE maycsx = ? FOR UPDATE",
                id
        );

        if (rows.isEmpty()) {
            throw new IllegalArgumentException(
                    "Không tìm thấy yêu cầu sản xuất."
            );
        }

        Map<String, Object> order = rows.get(0);

        if (!"HUY".equals(String.valueOf(order.get("trangthai")))) {
            throw new IllegalArgumentException(
                    "Yêu cầu này chưa bị hủy."
            );
        }

        Object oldStatus = order.get("trangthai_truoc_khi_huy");

        if (oldStatus == null || String.valueOf(oldStatus).isBlank()) {
            throw new IllegalArgumentException(
                    "Không có trạng thái trước khi hủy để khôi phục. " +
                            "Yêu cầu này có thể đã bị hủy trước khi có chức năng lưu trạng thái cũ."
            );
        }

        int updated = jdbc.update(
                "UPDATE yeucausanxuat " +
                        "SET trangthai = trangthai_truoc_khi_huy, " +
                        "    trangthai_truoc_khi_huy = NULL, " +
                        "    lydohuy = NULL, " +
                        "    ngayhuy = NULL " +
                        "WHERE maycsx = ? AND trangthai = 'HUY'",
                id
        );

        if (updated != 1) {
            throw new IllegalStateException(
                    "Không thể bỏ hủy yêu cầu sản xuất."
            );
        }
    }

    // =========================================================
    // LỊCH SỬ HOẠT ĐỘNG
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
                """
                SELECT timeline.*
                FROM (
                    SELECT
                        CONCAT('CREATE-', y.maycsx) AS event_key,
                        'TAO_YEU_CAU' AS loaisukien,
                        y.ngayyeucau AS thoigian,
                        creator.hoten AS tennguoithuchien,
                        'Tạo yêu cầu sản xuất' AS tieude,
                        CONCAT('Giao sản xuất ', y.soluongyeucau, ' ', s.tensp,
                               IF(assignee.hoten IS NULL, '', CONCAT(' cho ', assignee.hoten)), '.') AS noidung,
                        y.soluongyeucau AS soluong,
                        'DA_TAO' AS trangthai
                    FROM yeucausanxuat y
                    JOIN sanpham s ON s.masp = y.masp
                    LEFT JOIN nhanvien creator ON creator.manv = y.nguoitao
                    LEFT JOIN nhanvien assignee ON assignee.manv = y.nguoiphutrach
                    WHERE y.maycsx = ?

                    UNION ALL

                    SELECT
                        CONCAT('EDIT-', l.id),
                        'DIEU_CHINH',
                        l.thoigian,
                        editor.hoten,
                        'Điều chỉnh yêu cầu',
                        l.lydodieuchinh,
                        NULL,
                        'DA_CAP_NHAT'
                    FROM lichsu_dieuchinh_yeucau l
                    LEFT JOIN nhanvien editor ON editor.manv = l.nguoisua
                    WHERE l.maycsx = ?

                    UNION ALL

                    SELECT
                        CONCAT('RECORD-', g.id),
                        'GHI_NHAN_SAN_XUAT',
                        g.ngayghi,
                        worker.hoten,
                        'Ghi nhận sản lượng tại xưởng',
                        CONCAT('Đã ghi nhận ', g.soluong, ' thành phẩm hoàn thành',
                               IF(g.ghichu IS NULL OR TRIM(g.ghichu) = '', '.', CONCAT('. Ghi chú: ', TRIM(g.ghichu)))),
                        g.soluong,
                        'DA_GHI_NHAN'
                    FROM ghi_nhan_san_xuat g
                    LEFT JOIN nhanvien worker ON worker.manv = g.nguoighi
                    WHERE g.maycsx = ?

                    UNION ALL

                    SELECT
                        CONCAT('RESULT-', k.makqsx),
                        'KET_QUA_SAN_XUAT',
                        k.ngayhoanthanh,
                        worker.hoten,
                        CASE k.trangthai
                            WHEN 'CHO_XAC_NHAN' THEN 'Gửi kết quả chờ xác nhận'
                            WHEN 'DA_NHAP_KHO' THEN 'Kết quả đã được xác nhận'
                            WHEN 'HUY' THEN 'Kết quả đã hủy'
                            ELSE 'Cập nhật kết quả sản xuất'
                        END,
                        CONCAT('Số lượng hoàn thành: ', k.soluonghoanthanh,
                               IF(k.ghichu IS NULL OR TRIM(k.ghichu) = '', '.', CONCAT('. Ghi chú: ', TRIM(k.ghichu)))),
                        k.soluonghoanthanh,
                        k.trangthai
                    FROM ketqua_sanxuat k
                    LEFT JOIN nhanvien worker ON worker.manv = k.nguoithuchien
                    WHERE k.maycsx = ?

                    UNION ALL

                    SELECT
                        CONCAT('CANCEL-', y.maycsx),
                        'HUY_YEU_CAU',
                        y.ngayhuy,
                        NULL,
                        'Hủy yêu cầu sản xuất',
                        CONCAT('Lý do: ', COALESCE(y.lydohuy, 'Không có lý do')),
                        NULL,
                        'HUY'
                    FROM yeucausanxuat y
                    WHERE y.maycsx = ? AND y.ngayhuy IS NOT NULL
                ) timeline
                ORDER BY timeline.thoigian DESC, timeline.event_key DESC
                """,
                id, id, id, id, id
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
        long orderId = ((Number) order.get(
                "mayeucau"
        )).longValue();

        int requestedQuantity =
                ((Number) order.get(
                        "soluongyeucau"
                )).intValue();

        int confirmedQuantity =
                ((Number) order.get(
                        "soluonghoanthanh"
                )).intValue();

        Integer recordedQuantity = jdbc.queryForObject(
                "SELECT COALESCE(SUM(soluong), 0) FROM ghi_nhan_san_xuat WHERE maycsx = ?",
                Integer.class,
                ((Number) order.get("mayeucau")).longValue()
        );

        Integer pendingResultQuantity = jdbc.queryForObject(
                "SELECT COALESCE(SUM(soluonghoanthanh), 0) " +
                        "FROM ketqua_sanxuat " +
                        "WHERE maycsx = ? AND trangthai = 'CHO_XAC_NHAN'",
                Integer.class,
                orderId
        );

        int effectiveProduced = Math.max(
                recordedQuantity == null ? 0 : recordedQuantity,
                confirmedQuantity + (pendingResultQuantity == null ? 0 : pendingResultQuantity)
        );

        int remaining = Math.max(
                requestedQuantity - effectiveProduced,
                0
        );

        List<Map<String, Object>> lines =
                jdbc.queryForList(
                        "SELECT n.manvl_code, n.tennvl, n.donvitinh, " +
                                "COALESCE(k.soluongton, 0) AS soluongton, " +
                                "COALESCE(pending.soluong, 0) AS dang_de_nghi, " +
                                "c.soluong_dinhmuc, " +
                                "c.tyle_haohut " +
                                "FROM bom_chitiet c " +
                                "JOIN nguyenvatlieu n " +
                                "ON n.manvl = c.manvl " +
                                "LEFT JOIN kho_nvl_san_xuat k " +
                                "ON k.manvl = c.manvl " +
                                "LEFT JOIN (" +
                                "SELECT cpx.manvl, SUM(cpx.soluong) AS soluong " +
                                "FROM phieuxuat_nvl px " +
                                "JOIN ct_phieuxuat_nvl cpx ON cpx.mapx = px.mapx " +
                                "WHERE px.maycsx = ? AND px.trangthai IN ('CHO_DUYET', 'DA_DUYET') " +
                                "GROUP BY cpx.manvl" +
                                ") pending ON pending.manvl = c.manvl " +
                                "WHERE c.mabom = ? " +
                                "ORDER BY n.tennvl",
                        orderId,
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

            BigDecimal totalNeeded =
                    standard
                            .multiply(
                                    BigDecimal.valueOf(requestedQuantity)
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
            BigDecimal pending =
                    (BigDecimal) line.get(
                            "dang_de_nghi"
                    );

            result.add(
                    new MaterialNeed(
                            (String) line.get("manvl_code"),
                            (String) line.get("tennvl"),
                            (String) line.get("donvitinh"),
                            standard,
                            waste,
                            totalNeeded,
                            needed,
                            available,
                            pending,
                            needed.subtract(available).subtract(pending)
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
                ) || isQuantityCompleted(order);

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

    private boolean isQuantityCompleted(Map<String, Object> order) {
        Object requestedValue = order.get("soluongyeucau");
        Object completedValue = order.get("soluonghoanthanh");

        if (!(requestedValue instanceof Number requested) ||
                !(completedValue instanceof Number completed)) {
            return false;
        }

        return completed.doubleValue() >= requested.doubleValue();
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
            BigDecimal totalNeeded,
            BigDecimal needed,
            BigDecimal available,
            BigDecimal pending,
            BigDecimal shortage
    ) {
    }

    public Map<String, Object> getBatchInfo(
            long batchId,
            String email,
            boolean manager
    ) {
        String sql = """
        SELECT
            d.madot,
            d.madot_code,
            d.tendot,
            d.ghichu
        FROM dot_san_xuat d
        WHERE d.madot = ?
        """;

        List<Map<String, Object>> rows = jdbc.queryForList(
                sql,
                batchId
        );

        if (rows.isEmpty()) {
            throw new IllegalArgumentException(
                    "Không tìm thấy đợt sản xuất."
            );
        }

        // Xác minh người dùng có quyền xem ít nhất một yêu cầu
        // thuộc đợt sản xuất này.
        if (!manager) {
            Integer count = jdbc.queryForObject(
                    """
                    SELECT COUNT(*)
                    FROM yeucausanxuat y
                    JOIN taikhoan t ON t.manv = y.nguoiphutrach
                    WHERE y.madot = ?
                      AND t.email = ?
                    """,
                    Integer.class,
                    batchId,
                    email
            );

            if (count == null || count == 0) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Bạn không có quyền xem đợt sản xuất này."
                );
            }
        }

        return rows.get(0);
    }

    // =========================================================
// TÌNH TRẠNG NVL CỦA TOÀN BỘ YÊU CẦU TRONG ĐỢT
// =========================================================

    private String getMaterialStatus(
            String email,
            Map<String, Object> order
    ) {
        String status = String.valueOf(order.get("trangthai"));

        if ("HUY".equals(status) || "HOAN_THANH".equals(status)) {
            return "KHONG_CAN";
        }

        List<Map<String, Object>> materials;

        Object batchValue = order.get("madot");

        if (batchValue instanceof Number batchNumber) {
            // Đợt có mã: tổng hợp tất cả yêu cầu trong đợt
            // mà nhân viên hiện tại phụ trách.
            materials = jdbc.queryForList(
                    """
                    SELECT
                        c.manvl,
                        SUM(
                            CEIL(
                                GREATEST(
                                    y.soluongyeucau
                                    - GREATEST(
                                        COALESCE(g.daghi, 0),
                                        y.soluonghoanthanh
                                            + COALESCE(r.dangcho, 0)
                                    ),
                                    0
                                )
                                * c.soluong_dinhmuc
                                * (1 + c.tyle_haohut / 100)
                            )
                        ) AS nhu_cau,
                        COALESCE(k.soluongton, 0) AS ton_kho,
                        SUM(COALESCE(px.soluong, 0)) AS dang_cho_xuat
                    FROM yeucausanxuat y
                    JOIN taikhoan t
                        ON t.manv = y.nguoiphutrach
                    JOIN bom_chitiet c
                        ON c.mabom = y.mabom
                    LEFT JOIN (
                        SELECT maycsx, SUM(soluong) AS daghi
                        FROM ghi_nhan_san_xuat
                        GROUP BY maycsx
                    ) g ON g.maycsx = y.maycsx
                    LEFT JOIN (
                        SELECT maycsx, SUM(soluonghoanthanh) AS dangcho
                        FROM ketqua_sanxuat
                        WHERE trangthai = 'CHO_XAC_NHAN'
                        GROUP BY maycsx
                    ) r ON r.maycsx = y.maycsx
                    LEFT JOIN (
                        SELECT
                            p.maycsx,
                            ct.manvl,
                            SUM(ct.soluong) AS soluong
                        FROM phieuxuat_nvl p
                        JOIN ct_phieuxuat_nvl ct
                            ON ct.mapx = p.mapx
                        WHERE p.trangthai = 'CHO_DUYET'
                        GROUP BY p.maycsx, ct.manvl
                    ) px ON px.maycsx = y.maycsx
                        AND px.manvl = c.manvl
                    LEFT JOIN kho_nvl_san_xuat k
                        ON k.manvl = c.manvl
                    WHERE y.madot = ?
                      AND t.email = ?
                      AND (y.trangthai IS NULL
                           OR y.trangthai NOT IN ('HUY', 'HOAN_THANH'))
                    GROUP BY c.manvl, k.soluongton
                    """,
                    batchNumber.longValue(),
                    email
            );
        } else {
            // Yêu cầu cũ chưa thuộc đợt sản xuất.
            Object orderValue = order.get("mayeucau");

            if (!(orderValue instanceof Number orderNumber)) {
                return "KHONG_CAN";
            }

            materials = jdbc.queryForList(
                    """
                    SELECT
                        c.manvl,
                        SUM(
                            CEIL(
                                GREATEST(
                                    y.soluongyeucau
                                    - GREATEST(
                                        COALESCE(g.daghi, 0),
                                        y.soluonghoanthanh
                                            + COALESCE(r.dangcho, 0)
                                    ),
                                    0
                                )
                                * c.soluong_dinhmuc
                                * (1 + c.tyle_haohut / 100)
                            )
                        ) AS nhu_cau,
                        COALESCE(k.soluongton, 0) AS ton_kho,
                        SUM(COALESCE(px.soluong, 0)) AS dang_cho_xuat
                    FROM yeucausanxuat y
                    JOIN taikhoan t
                        ON t.manv = y.nguoiphutrach
                    JOIN bom_chitiet c
                        ON c.mabom = y.mabom
                    LEFT JOIN (
                        SELECT maycsx, SUM(soluong) AS daghi
                        FROM ghi_nhan_san_xuat
                        GROUP BY maycsx
                    ) g ON g.maycsx = y.maycsx
                    LEFT JOIN (
                        SELECT maycsx, SUM(soluonghoanthanh) AS dangcho
                        FROM ketqua_sanxuat
                        WHERE trangthai = 'CHO_XAC_NHAN'
                        GROUP BY maycsx
                    ) r ON r.maycsx = y.maycsx
                    LEFT JOIN (
                        SELECT
                            p.maycsx,
                            ct.manvl,
                            SUM(ct.soluong) AS soluong
                        FROM phieuxuat_nvl p
                        JOIN ct_phieuxuat_nvl ct
                            ON ct.mapx = p.mapx
                        WHERE p.trangthai = 'CHO_DUYET'
                        GROUP BY p.maycsx, ct.manvl
                    ) px ON px.maycsx = y.maycsx
                        AND px.manvl = c.manvl
                    LEFT JOIN kho_nvl_san_xuat k
                        ON k.manvl = c.manvl
                    WHERE y.maycsx = ?
                      AND t.email = ?
                      AND (y.trangthai IS NULL
                           OR y.trangthai NOT IN ('HUY', 'HOAN_THANH'))
                    GROUP BY c.manvl, k.soluongton
                    """,
                    orderNumber.longValue(),
                    email
            );
        }

        if (materials.isEmpty()) {
            return "KHONG_CAN";
        }

        boolean hasDemand = false;
        boolean hasPending = false;

        for (Map<String, Object> material : materials) {
            Number demandValue = (Number) material.get("nhu_cau");
            Number stockValue = (Number) material.get("ton_kho");
            Number pendingValue = (Number) material.get("dang_cho_xuat");

            double demand = demandValue == null
                    ? 0 : demandValue.doubleValue();

            double stock = stockValue == null
                    ? 0 : stockValue.doubleValue();

            double pending = pendingValue == null
                    ? 0 : pendingValue.doubleValue();

            if (demand <= 0) {
                continue;
            }

            hasDemand = true;

            // Kho sản xuất đã có đủ NVL thực tế.
            if (stock >= demand) {
                continue;
            }

            // Chưa đủ NVL nhưng có phiếu xuất đang chờ duyệt.
            if (pending > 0) {
                hasPending = true;
            } else {
                // Thiếu NVL và không có phiếu nào chờ duyệt.
                return "THIEU";
            }
        }

        if (!hasDemand) {
            return "KHONG_CAN";
        }

// Chỉ hiển thị đủ khi tồn kho thực tế đáp ứng đủ nhu cầu.
        if (hasPending) {
            return "DANG_CHO_DUYET";
        }

        return "DU";
    }
}
