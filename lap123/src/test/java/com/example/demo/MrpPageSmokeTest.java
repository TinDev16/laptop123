package com.example.demo;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import com.example.demo.config.CustomEmployeeDetailService;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=none")
@AutoConfigureMockMvc
class MrpPageSmokeTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private CustomEmployeeDetailService employeeDetails;

    @Test
    void managerPagesRender() throws Exception {
        for (String path : new String[]{"/dashboard", "/bom", "/bom/create", "/nguyen-vat-lieu",
                "/nguyen-vat-lieu/create", "/kho/nhap-nvl", "/kho/nhap-nvl/create",
                "/kho/xuat-nvl", "/san-xuat/yeu-cau", "/san-xuat/yeu-cau/create",
                "/san-xuat/ket-qua", "/kho/xuat-thanh-pham", "/kho/lich-su", "/baocao"}) {
            mvc.perform(get(path).with(user("manager@test.local").roles("QUAN_LY_KHO")))
                    .andExpect(status().isOk());
        }
        Long bomId = jdbc.queryForObject("SELECT MIN(mabom) FROM bom", Long.class);
        if (bomId != null) {
            mvc.perform(get("/bom/" + bomId).with(user("manager@test.local").roles("QUAN_LY_KHO")))
                    .andExpect(status().isOk());
        }
        Long orderId = jdbc.queryForObject("SELECT MIN(maycsx) FROM yeucausanxuat", Long.class);
        if (orderId != null) {
            mvc.perform(get("/san-xuat/yeu-cau/" + orderId).with(user("manager@test.local").roles("QUAN_LY_KHO")))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void executiveMenuListsAndAccountPageRenderTables() throws Exception {
        for (String path : new String[]{"/dashboard", "/nguyen-vat-lieu", "/kho/nhap-nvl",
                "/kho/xuat-nvl", "/kho/lich-su", "/bom", "/san-xuat/yeu-cau",
                "/san-xuat/ket-qua", "/sanpham", "/kho/xuat-thanh-pham",
                "/ncc", "/khachhang", "/thuonghieu", "/nhanvien"}) {
            mvc.perform(get(path).with(user("manager@test.local").roles("QUAN_LY_KHO")))
                    .andExpect(status().isOk())
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                            .string(org.hamcrest.Matchers.containsString("<table")));
        }
        mvc.perform(get("/quanlytaikhoan").with(user("admin@test.local").authorities(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_QUAN_LY_KHO"))))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString("<table")));
    }
    @Test
    void reportDetailsOnlyRenderAfterSelectingSummaryCard() throws Exception {
        var manager = user("manager@test.local").roles("QUAN_LY_KHO");
        mvc.perform(get("/baocao").with(manager))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("giao-dich-chi-tiet"))))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("<table"))));

        mvc.perform(get("/baocao").param("type", "NHAP_NVL").with(manager))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString("giao-dich-chi-tiet")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString("<table")));
    }

    @Test
    void partnerPagesKeepDashboardShell() throws Exception {
        for (String path : new String[]{"/ncc", "/khachhang", "/thuonghieu",
                "/ncc/create", "/khachhang/create", "/thuonghieu/create"}) {
            mvc.perform(get(path).with(user("manager@test.local").roles("QUAN_LY_KHO")))
                    .andExpect(status().isOk())
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                            .string(org.hamcrest.Matchers.containsString("class=\"sidebar\"")))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                            .string(org.hamcrest.Matchers.containsString("/sidebar.js")))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                            .string(org.hamcrest.Matchers.containsString("Đối tác /")));
        }
    }
    @Test
    void employeeCannotOpenManagerPages() throws Exception {
        mvc.perform(get("/bom").with(user("employee@test.local").roles("NHAN_VIEN")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/kho/nhap-nvl").with(user("employee@test.local").roles("NHAN_VIEN")))
                .andExpect(status().isForbidden());
        for (String path : new String[]{"/san-xuat/yeu-cau", "/kho/xuat-nvl/create",
                "/san-xuat/ket-qua/create", "/kho/xuat-thanh-pham/create"}) {
            mvc.perform(get(path).with(user("employee@test.local").roles("NHAN_VIEN")))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void refreshedInputPagesAndResultDetailRender() throws Exception {
        mvc.perform(get("/san-xuat/ket-qua/create")
                        .with(user("employee@test.local").roles("NHAN_VIEN")))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString("id=\"completed\"")));
        mvc.perform(get("/kho/xuat-thanh-pham/create")
                        .with(user("employee@test.local").roles("NHAN_VIEN")))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString("id=\"quantity\"")));
        Long resultId = jdbc.queryForObject("SELECT MIN(makqsx) FROM ketqua_sanxuat", Long.class);
        if (resultId != null) {
            mvc.perform(get("/san-xuat/ket-qua/" + resultId)
                            .with(user("manager@test.local").roles("QUAN_LY_KHO")))
                    .andExpect(status().isOk())
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                            .string(org.hamcrest.Matchers.containsString("ceo-detail-grid")));
        }
    }

    @Test
    void assignedOrderIsVisibleOnlyToItsEmployee() throws Exception {
        var assigned = jdbc.queryForList("SELECT y.maycsx, t.email FROM yeucausanxuat y " +
                "JOIN taikhoan t ON t.manv = y.nguoiphutrach ORDER BY y.maycsx LIMIT 1");
        if (assigned.isEmpty()) return;
        long orderId = ((Number) assigned.get(0).get("maycsx")).longValue();
        String owner = (String) assigned.get(0).get("email");
        mvc.perform(get("/san-xuat/yeu-cau/" + orderId).with(user(owner).roles("NHAN_VIEN")))
                .andExpect(status().isOk());
        mvc.perform(get("/kho/xuat-nvl/create").param("orderId", Long.toString(orderId))
                        .with(user(owner).roles("NHAN_VIEN")))
                .andExpect(status().isOk());
        mvc.perform(get("/san-xuat/yeu-cau/" + orderId)
                        .with(user("another-employee@test.local").roles("NHAN_VIEN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void employeeDashboardAndProtectedRoutes() throws Exception {
        mvc.perform(get("/dashboard").with(user("employee@test.local").roles("NHAN_VIEN")))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString("Phiếu NVL chờ duyệt")));
        for (String path : new String[]{"/bom", "/nguyen-vat-lieu", "/kho/nhap-nvl",
                "/nhanvien", "/quanlytaikhoan", "/baocao"}) {
            mvc.perform(get(path).with(user("employee@test.local").roles("NHAN_VIEN")))
                    .andExpect(status().isForbidden());
        }
        for (String path : new String[]{"/kho/xuat-nvl/1/duyet", "/kho/xuat-nvl/1/tu-choi",
                "/san-xuat/ket-qua/1/confirm", "/kho/xuat-thanh-pham/1/duyet",
                "/kho/xuat-thanh-pham/1/tu-choi"}) {
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(path)
                    .with(user("employee@test.local").roles("NHAN_VIEN")))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void employeeCannotReadOtherWorkersDocuments() throws Exception {
        String stranger = "another-employee@test.local";
        for (String[] target : new String[][]{{"phieuxuat_nvl", "mapx", "/kho/xuat-nvl/"},
                {"ketqua_sanxuat", "makqsx", "/san-xuat/ket-qua/"},
                {"phieuxuat_thanhpham", "mapxtp", "/kho/xuat-thanh-pham/"}}) {
            Long id = jdbc.queryForObject("SELECT MIN(" + target[1] + ") FROM " + target[0], Long.class);
            if (id != null) {
                mvc.perform(get(target[2] + id).with(user(stranger).roles("NHAN_VIEN")))
                        .andExpect(status().isForbidden());
            }
        }
    }
    @Test
    void migratedManagerCanSignInAndEmployeeCanSeeProfile() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/userLogin")
                        .param("username", "a").param("password", "a"))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl("/dashboard"));
        String employeeEmail = jdbc.queryForObject("SELECT t.email FROM taikhoan t JOIN vaitro v ON v.vaitro_id = t.vaitro_id "
                + "WHERE v.ma_vaitro = 'NHAN_VIEN' ORDER BY t.taikhoan_id LIMIT 1", String.class);
        mvc.perform(get("/tai-khoan/ca-nhan").with(user(employeeEmail).roles("NHAN_VIEN")))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString(employeeEmail)));
    }
    @Test
    void databaseRolesMapToSpringAuthorities() {
        org.junit.jupiter.api.Assertions.assertEquals(2,
                jdbc.queryForObject("SELECT COUNT(*) FROM vaitro", Integer.class));
        var accounts = jdbc.queryForList("SELECT t.email, COALESCE(v.ma_vaitro, v.ten) role_code FROM taikhoan t " +
                "JOIN vaitro v ON v.vaitro_id = t.vaitro_id WHERE t.manv IS NOT NULL");
        for (var account : accounts) {
            String code = (String) account.get("role_code");
            if (code == null) continue;
            String expected = "NHAN_VIEN".equals(code)
                    ? "ROLE_NHAN_VIEN" : "ROLE_QUAN_LY_KHO";
            var details = employeeDetails.loadUserByUsername((String) account.get("email"));
            org.junit.jupiter.api.Assertions.assertTrue(details.getAuthorities().stream()
                    .anyMatch(authority -> expected.equals(authority.getAuthority())));
        }
    }
}