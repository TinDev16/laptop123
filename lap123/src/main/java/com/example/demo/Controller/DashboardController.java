package com.example.demo.Controller;

import com.example.demo.Service.ProductionOrderService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
    private final JdbcTemplate jdbc;
    private final ProductionOrderService orders;

    public DashboardController(JdbcTemplate jdbc, ProductionOrderService orders) {
        this.jdbc = jdbc;
        this.orders = orders;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        boolean manager = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_QUAN_LY_KHO".equals(authority.getAuthority()));
        model.addAttribute("manager", manager);
        model.addAttribute("orders", orders.list(authentication.getName(), manager).stream().limit(10).toList());
        if (!manager) {
            String email = authentication.getName();
            model.addAttribute("assignedCount", jdbc.queryForObject("SELECT COUNT(*) FROM yeucausanxuat y JOIN taikhoan t ON t.manv = y.nguoiphutrach WHERE t.email = ?", Integer.class, email));
            model.addAttribute("activeCount", jdbc.queryForObject("SELECT COUNT(*) FROM yeucausanxuat y JOIN taikhoan t ON t.manv = y.nguoiphutrach WHERE t.email = ? AND y.trangthai IN ('DA_GIAO', 'DANG_SAN_XUAT')", Integer.class, email));
            model.addAttribute("pendingMaterialCount", jdbc.queryForObject("SELECT COUNT(*) FROM phieuxuat_nvl p JOIN taikhoan t ON t.manv = p.nguoilap WHERE t.email = ? AND p.trangthai = 'CHO_DUYET'", Integer.class, email));
            model.addAttribute("pendingResultCount", jdbc.queryForObject("SELECT COUNT(*) FROM ketqua_sanxuat k JOIN taikhoan t ON t.manv = k.nguoithuchien WHERE t.email = ? AND k.trangthai = 'CHO_XAC_NHAN'", Integer.class, email));
            model.addAttribute("completedCount", jdbc.queryForObject("SELECT COUNT(*) FROM yeucausanxuat y JOIN taikhoan t ON t.manv = y.nguoiphutrach WHERE t.email = ? AND y.trangthai = 'HOAN_THANH'", Integer.class, email));
        } else {
            model.addAttribute("materials", jdbc.queryForObject("SELECT COUNT(*) FROM nguyenvatlieu", Integer.class));
            model.addAttribute("lowStock", jdbc.queryForObject("SELECT COUNT(*) FROM nguyenvatlieu WHERE soluongton <= ton_toithieu", Integer.class));
            model.addAttribute("finishedStock", jdbc.queryForObject("SELECT COALESCE(SUM(soluongton), 0) FROM sanpham", Long.class));
            model.addAttribute("activeOrders", jdbc.queryForObject("SELECT COUNT(*) FROM yeucausanxuat WHERE trangthai IN ('DA_GIAO', 'DANG_SAN_XUAT')", Integer.class));
            model.addAttribute("pendingMaterials", jdbc.queryForObject("SELECT COUNT(*) FROM phieuxuat_nvl WHERE trangthai = 'CHO_DUYET'", Integer.class));
            model.addAttribute("pendingResults", jdbc.queryForObject("SELECT COUNT(*) FROM ketqua_sanxuat WHERE trangthai = 'CHO_XAC_NHAN'", Integer.class));
            model.addAttribute("pendingProducts", jdbc.queryForObject("SELECT COUNT(*) FROM phieuxuat_thanhpham WHERE trangthai = 'CHO_DUYET'", Integer.class));
        }
        return "mrp/dashboard";
    }
}