package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.demo.Entity.Taikhoan;
import com.example.demo.Entity.Nhanvien;
import com.example.demo.Entity.Vaitro;
import com.example.demo.config.CustomEmployee;
import org.junit.jupiter.api.Test;

class TwoActorUserDetailsTest {
    @Test
    void onlyOfficialRolesHaveAuthoritiesAndLockedAccountsCannotAuthenticate() {
        Taikhoan account = new Taikhoan();
        Vaitro role = new Vaitro();
        account.setVaitro(role);
        account.setLocked(true);
        role.setMaVaitro("NHAN_VIEN");
        CustomEmployee details = new CustomEmployee(account);
        assertEquals("ROLE_NHAN_VIEN", details.getAuthorities().iterator().next().getAuthority());
        assertEquals(1, details.getAuthorities().size());
        assertFalse(details.isAccountNonLocked());

        role.setMaVaitro("QUAN_LY_KHO");
        assertEquals("ROLE_QUAN_LY_KHO", details.getAuthorities().iterator().next().getAuthority());
        role.setMaVaitro("UNKNOWN");
        assertThrows(IllegalStateException.class, details::getAuthorities);
    }
    @Test
    void formerStaffAccountIsDisabledWhileExistingManagerAccountStaysUsable() {
        Nhanvien employee = new Nhanvien();
        employee.setTRANGTHAI(1);
        Vaitro role = new Vaitro();
        role.setMaVaitro("NHAN_VIEN");
        Taikhoan account = new Taikhoan();
        account.setNhanvien(employee);
        account.setVaitro(role);
        CustomEmployee details = new CustomEmployee(account);
        assertFalse(details.isEnabled());

        role.setMaVaitro("QUAN_LY_KHO");
        org.junit.jupiter.api.Assertions.assertTrue(details.isEnabled());
    }
}