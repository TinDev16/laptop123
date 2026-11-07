package com.example.demo.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.util.Map;
import org.springframework.dao.EmptyResultDataAccessException;

@Service
public class DatabaseBackupService {

    private final JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUsername;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    @Value("${app.backup.mysqldump-path:mysqldump}")
    private String mysqldumpPath;

    public DatabaseBackupService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String taoBanSaoLuu(
            String tenBanSaoLuu,
            String loaiSaoLuu
    ) throws Exception {

        // 1. Kiểm tra tên bản sao lưu
        if (tenBanSaoLuu == null || tenBanSaoLuu.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Tên bản sao lưu không được để trống."
            );
        }

        tenBanSaoLuu = tenBanSaoLuu.trim();

        if (tenBanSaoLuu.length() > 100) {
            throw new IllegalArgumentException(
                    "Tên bản sao lưu không được vượt quá 100 ký tự."
            );
        }

        // 2. Xác định thư mục Desktop/saoluu
        Path backupDirectory = Path.of(
                System.getProperty("user.home"),
                "Desktop",
                "saoluu"
        );

        Files.createDirectories(backupDirectory);

        // 3. Tạo tên file an toàn từ tên người dùng nhập
        String safeName = taoTenFileAnToan(tenBanSaoLuu);

        // Thêm thời gian để tránh trùng tên file
        String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS")
        );

        String fileName = safeName + "_" + timestamp + ".sql";

        Path backupFile = backupDirectory.resolve(fileName);

        // 4. Lấy thông tin kết nối MySQL
        URI uri = URI.create(
                databaseUrl.replaceFirst("^jdbc:", "")
        );

        String host = uri.getHost();
        int port = uri.getPort() == -1 ? 3306 : uri.getPort();

        String databaseName = "lap123";

        // 5. Tạo file cấu hình tạm
        Path optionFile = Files.createTempFile(
                "mysql-backup-", ".cnf"
        );

        Path logFile = Files.createTempFile(
                "mysql-backup-", ".log"
        );

        try {

            String config = """
                    [client]
                    host="%s"
                    port=%d
                    user="%s"
                    password="%s"
                    """.formatted(
                    escapeOptionValue(host),
                    port,
                    escapeOptionValue(databaseUsername),
                    escapeOptionValue(databasePassword)
            );

            Files.writeString(optionFile, config);

            // 6. Chạy mysqldump
            ProcessBuilder processBuilder = new ProcessBuilder(
                    mysqldumpPath,
                    "--defaults-extra-file=" + optionFile,
                    "--single-transaction",
                    "--routines",
                    "--triggers",
                    "--events",
                    "--databases",
                    databaseName,
                    "--ignore-table=" + databaseName + ".lich_su_sao_luu",
                    "--result-file=" + backupFile
            );

            processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput(logFile.toFile());

            Process process = processBuilder.start();

            boolean completed = process.waitFor(
                    300,
                    TimeUnit.SECONDS
            );

            if (!completed) {
                process.destroyForcibly();
                process.waitFor();

                throw new RuntimeException(
                        "Quá thời gian chờ sao lưu database."
                );
            }

            String processLog = Files.readString(logFile);

            if (process.exitValue() != 0) {
                throw new RuntimeException(
                        "mysqldump thực hiện thất bại. " + processLog
                );
            }

            // 7. Kiểm tra file sao lưu
            if (!Files.exists(backupFile)
                    || Files.size(backupFile) == 0) {

                throw new RuntimeException(
                        "File sao lưu không tồn tại hoặc bị rỗng."
                );
            }

            // 8. Lấy dung lượng file
            long fileSize = Files.size(backupFile);

            // 9. Ghi lịch sử sao lưu vào database
            jdbcTemplate.update("""
        INSERT INTO lich_su_sao_luu
        (
            ten_ban_sao_luu,
            duong_dan,
            dung_luong,
            thoi_gian_sao_luu,
            trang_thai,
            mo_ta,
            loai_sao_luu
        )
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """,
                    tenBanSaoLuu,
                    backupFile.toAbsolutePath().toString(),
                    fileSize,
                    LocalDateTime.now(),
                    "THANH_CONG",
                    "Sao lưu database lap123 thành công",
                    loaiSaoLuu
            );

            return fileName;

        } catch (Exception e) {

            // Nếu thất bại thì xóa file sao lưu dở dang
            Files.deleteIfExists(backupFile);

            throw e;

        } finally {

            // Xóa file cấu hình và log tạm
            Files.deleteIfExists(optionFile);
            Files.deleteIfExists(logFile);
        }
    }

    // Chuyển tên người dùng nhập thành tên file an toàn
    private String taoTenFileAnToan(String ten) {

        String normalized = Normalizer.normalize(
                ten,
                Normalizer.Form.NFD
        );

        normalized = normalized
                .replaceAll("\\p{M}+", "")
                .replace("đ", "d")
                .replace("Đ", "D");

        normalized = normalized.replaceAll(
                "[^a-zA-Z0-9_-]+",
                "_"
        );

        normalized = normalized.replaceAll("_+", "_");

        normalized = normalized.replaceAll(
                "^[_-]+|[_-]+$",
                ""
        );

        if (normalized.isEmpty()) {
            normalized = "ban_sao_luu";
        }

        return normalized;
    }

    // Escape giá trị trong file cấu hình MySQL
    private String escapeOptionValue(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", "");
    }

    @Value("${app.backup.mysql-path:mysql}")
    private String mysqlPath;

    // ======================================================
// PHỤC HỒI DATABASE TỪ BẢN SAO LƯU
// ======================================================

    public synchronized void phucHoiBanSaoLuu(Long id) throws Exception {

        // 1. Lấy thông tin bản sao lưu theo ID
        Map<String, Object> backup;

        try {
            backup = jdbcTemplate.queryForMap("""
                SELECT
                    id,
                    ten_ban_sao_luu,
                    duong_dan,
                    trang_thai
                FROM lich_su_sao_luu
                WHERE id = ?
                """, id);

        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException(
                    "Không tìm thấy bản sao lưu được chọn."
            );
        }

        // 2. Chỉ cho phép phục hồi bản sao lưu thành công
        String trangThai = String.valueOf(
                backup.get("trang_thai")
        );

        if (!"THANH_CONG".equals(trangThai)) {
            throw new IllegalArgumentException(
                    "Bản sao lưu này không có trạng thái thành công."
            );
        }

        String tenBanSaoLuu = String.valueOf(
                backup.get("ten_ban_sao_luu")
        );

        String duongDan = String.valueOf(
                backup.get("duong_dan")
        );

        // 3. Kiểm tra đường dẫn file sao lưu
        Path backupDirectory = Path.of(
                System.getProperty("user.home"),
                "Desktop",
                "saoluu"
        ).toRealPath();

        Path backupFile = Path.of(duongDan)
                .toRealPath();

        // Không cho phép phục hồi file bên ngoài thư mục sao lưu
        if (!backupFile.startsWith(backupDirectory)
                || !Files.isRegularFile(backupFile)
                || !backupFile.getFileName().toString()
                .toLowerCase().endsWith(".sql")
                || Files.size(backupFile) == 0) {

            throw new IllegalArgumentException(
                    "File sao lưu không tồn tại hoặc không hợp lệ."
            );
        }

        // 4. Tạo bản sao lưu an toàn trước khi phục hồi
        String thoiGian = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        );

        String tenBackupAnToan =
                "Truoc_phuc_hoi_" + thoiGian;

        taoBanSaoLuu(
                tenBackupAnToan,
                "TU_DONG"
        );

        // 5. Lấy thông tin kết nối MySQL
        URI uri = URI.create(
                databaseUrl.replaceFirst("^jdbc:", "")
        );

        String host = uri.getHost();

        int port = uri.getPort() == -1
                ? 3306
                : uri.getPort();

        String databaseName = "lap123";

        // 6. Tạo file cấu hình kết nối tạm
        Path optionFile = Files.createTempFile(
                "mysql-restore-", ".cnf"
        );

        Path logFile = Files.createTempFile(
                "mysql-restore-", ".log"
        );

        try {

            String config = """
                [client]
                host="%s"
                port=%d
                user="%s"
                password="%s"
                """.formatted(
                    escapeOptionValue(host),
                    port,
                    escapeOptionValue(databaseUsername),
                    escapeOptionValue(databasePassword)
            );

            Files.writeString(optionFile, config);

            // 7. Chạy MySQL client để phục hồi database
            ProcessBuilder processBuilder = new ProcessBuilder(
                    mysqlPath,
                    "--defaults-extra-file=" + optionFile,
                    "--default-character-set=utf8mb4",
                    "--database=" + databaseName
            );

            // Đưa nội dung file SQL vào MySQL
            processBuilder.redirectInput(backupFile.toFile());

            // Ghi log quá trình phục hồi
            processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput(logFile.toFile());

            Process process = processBuilder.start();

            boolean completed = process.waitFor(
                    600,
                    TimeUnit.SECONDS
            );

            if (!completed) {

                process.destroyForcibly();
                process.waitFor();

                throw new RuntimeException(
                        "Quá thời gian chờ phục hồi database."
                );
            }

            String processLog = Files.readString(logFile);

            if (process.exitValue() != 0) {

                throw new RuntimeException(
                        "MySQL phục hồi thất bại. " + processLog
                );
            }

            // 8. Phục hồi thành công
            System.out.println(
                    "Đã phục hồi database từ bản sao lưu: "
                            + tenBanSaoLuu
            );

        } finally {

            // 9. Xóa file cấu hình và log tạm
            Files.deleteIfExists(optionFile);
            Files.deleteIfExists(logFile);
        }
    }

    // ======================================================
// KIỂM TRA BẢN SAO LƯU CÓ FILE THỰC TẾ HAY KHÔNG
// ======================================================

    public boolean kiemTraFileSaoLuu(String duongDan) {

        try {

            // 1. Kiểm tra đường dẫn
            if (duongDan == null || duongDan.trim().isEmpty()) {
                return false;
            }

            // 2. Xác định thư mục Desktop/saoluu
            Path backupDirectory = Path.of(
                    System.getProperty("user.home"),
                    "Desktop",
                    "saoluu"
            );

            // Thư mục không tồn tại hoặc không phải thư mục
            if (!Files.isDirectory(backupDirectory)) {
                return false;
            }

            // Lấy đường dẫn thực tế của thư mục
            Path realDirectory = backupDirectory.toRealPath();

            // 3. Kiểm tra file có tồn tại
            Path backupFile = Path.of(duongDan).toRealPath();

            // 4. Kiểm tra file có nằm trong thư mục sao lưu
            if (!backupFile.startsWith(realDirectory)) {
                return false;
            }

            // 5. Kiểm tra file hợp lệ
            if (!Files.isRegularFile(backupFile)) {
                return false;
            }

            // 6. Kiểm tra định dạng SQL
            if (!backupFile.getFileName()
                    .toString()
                    .toLowerCase()
                    .endsWith(".sql")) {

                return false;
            }

            // 7. Kiểm tra dung lượng file
            if (Files.size(backupFile) <= 0) {
                return false;
            }

            return true;

        } catch (Exception e) {

            // Không tìm thấy file hoặc xảy ra lỗi đường dẫn
            return false;
        }
    }
}