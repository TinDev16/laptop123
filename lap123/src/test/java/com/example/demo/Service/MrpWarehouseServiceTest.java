package com.example.demo.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

class MrpWarehouseServiceTest {
    private JdbcTemplate jdbc;
    private TransactionTemplate transaction;
    private MrpWarehouseService service;

    @BeforeEach
    void setup() {
        DriverManagerDataSource source = new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        jdbc = new JdbcTemplate(source);
        transaction = new TransactionTemplate(new DataSourceTransactionManager(source));
        service = new MrpWarehouseService(jdbc);
        jdbc.execute("CREATE TABLE nguyenvatlieu (manvl BIGINT PRIMARY KEY, tennvl VARCHAR(100), soluongton DECIMAL(18,2))");
        jdbc.execute("CREATE TABLE phieuxuat_nvl (mapx BIGINT PRIMARY KEY, trangthai VARCHAR(30), lydo VARCHAR(255), nguoiduyet BIGINT, ngayduyet TIMESTAMP)");
        jdbc.execute("CREATE TABLE ct_phieuxuat_nvl (mapx BIGINT, manvl BIGINT, soluong DECIMAL(18,2))");
        jdbc.execute("CREATE TABLE lich_su_kho (loai_hang VARCHAR(30), ma_hang BIGINT, loai_giaodich VARCHAR(30), soluong DECIMAL(18,2), don_gia DECIMAL(18,2), nguoi_thuchien BIGINT, bang_tham_chieu VARCHAR(50), ma_tham_chieu BIGINT)");
        jdbc.execute("CREATE TABLE ketqua_sanxuat (makqsx BIGINT PRIMARY KEY, maycsx BIGINT, soluonghoanthanh INT, trangthai VARCHAR(30))");
        jdbc.execute("CREATE TABLE yeucausanxuat (maycsx BIGINT PRIMARY KEY, masp BIGINT, soluongyeucau INT, soluonghoanthanh INT, trangthai VARCHAR(30))");
        jdbc.execute("CREATE TABLE sanpham (masp BIGINT PRIMARY KEY, soluongton INT)");
    }

    @Test
    void issueMaterialsDecreasesStockAndWritesHistory() {
        jdbc.update("INSERT INTO nguyenvatlieu VALUES (1, 'RAM', 10)");
        jdbc.update("INSERT INTO phieuxuat_nvl (mapx, trangthai) VALUES (1, 'CHO_DUYET')");
        jdbc.update("INSERT INTO ct_phieuxuat_nvl VALUES (1, 1, 4)");
        transaction.executeWithoutResult(status -> service.issueMaterials(1));
        assertEquals(6, count("SELECT soluongton FROM nguyenvatlieu WHERE manvl = 1"));
        assertEquals("DA_XUAT", jdbc.queryForObject("SELECT trangthai FROM phieuxuat_nvl WHERE mapx = 1", String.class));
        assertEquals(1, count("SELECT COUNT(*) FROM lich_su_kho"));
    }

    @Test
    void shortageLeavesAllMaterialsUnchanged() {
        jdbc.update("INSERT INTO nguyenvatlieu VALUES (1, 'RAM', 10), (2, 'SSD', 2)");
        jdbc.update("INSERT INTO phieuxuat_nvl (mapx, trangthai) VALUES (1, 'CHO_DUYET')");
        jdbc.update("INSERT INTO ct_phieuxuat_nvl VALUES (1, 1, 5), (1, 2, 4)");
        assertThrows(IllegalStateException.class,
                () -> transaction.executeWithoutResult(status -> service.issueMaterials(1)));
        assertEquals(10, count("SELECT soluongton FROM nguyenvatlieu WHERE manvl = 1"));
        assertEquals(2, count("SELECT soluongton FROM nguyenvatlieu WHERE manvl = 2"));
        assertEquals(0, count("SELECT COUNT(*) FROM lich_su_kho"));
    }

    @Test
    void secondSubmissionDoesNotDecreaseStockAgain() {
        jdbc.update("INSERT INTO nguyenvatlieu VALUES (1, 'RAM', 10)");
        jdbc.update("INSERT INTO phieuxuat_nvl (mapx, trangthai) VALUES (1, 'CHO_DUYET')");
        jdbc.update("INSERT INTO ct_phieuxuat_nvl VALUES (1, 1, 4)");
        transaction.executeWithoutResult(status -> service.issueMaterials(1));
        assertThrows(IllegalStateException.class,
                () -> transaction.executeWithoutResult(status -> service.issueMaterials(1)));
        assertEquals(6, count("SELECT soluongton FROM nguyenvatlieu WHERE manvl = 1"));
        assertEquals(1, count("SELECT COUNT(*) FROM lich_su_kho"));
    }

    @Test
    void productionConfirmationUpdatesProgressAndCannotRepeat() {
        jdbc.update("INSERT INTO sanpham VALUES (1, 0)");
        jdbc.update("INSERT INTO yeucausanxuat VALUES (1, 1, 10, 0, 'DA_GIAO')");
        jdbc.update("INSERT INTO ketqua_sanxuat VALUES (1, 1, 6, 'CHO_XAC_NHAN')");
        transaction.executeWithoutResult(status -> service.confirmProduction(1));
        assertEquals(6, count("SELECT soluongton FROM sanpham WHERE masp = 1"));
        assertEquals(6, count("SELECT soluonghoanthanh FROM yeucausanxuat WHERE maycsx = 1"));
        assertThrows(IllegalStateException.class,
                () -> transaction.executeWithoutResult(status -> service.confirmProduction(1)));
        assertEquals(6, count("SELECT soluongton FROM sanpham WHERE masp = 1"));
    }

    @Test
    void productionCannotExceedOrder() {
        jdbc.update("INSERT INTO sanpham VALUES (1, 0)");
        jdbc.update("INSERT INTO yeucausanxuat VALUES (1, 1, 10, 8, 'DANG_SAN_XUAT')");
        jdbc.update("INSERT INTO ketqua_sanxuat VALUES (1, 1, 3, 'CHO_XAC_NHAN')");
        assertThrows(IllegalStateException.class,
                () -> transaction.executeWithoutResult(status -> service.confirmProduction(1)));
        assertEquals(0, count("SELECT soluongton FROM sanpham WHERE masp = 1"));
        assertEquals(8, count("SELECT soluonghoanthanh FROM yeucausanxuat WHERE maycsx = 1"));
    }

    private int count(String sql) {
        return jdbc.queryForObject(sql, Integer.class);
    }
}