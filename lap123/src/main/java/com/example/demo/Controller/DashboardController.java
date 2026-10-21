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

    public DashboardController(
            JdbcTemplate jdbc,
            ProductionOrderService orders
    ) {
        this.jdbc = jdbc;
        this.orders = orders;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            Authentication authentication,
            Model model
    ) {

        boolean manager =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                "ROLE_QUAN_LY_KHO".equals(
                                        authority.getAuthority()
                                )
                        );

        model.addAttribute(
                "manager",
                manager
        );

        model.addAttribute(
                "orders",
                orders.listGrouped(
                                authentication.getName(),
                                manager,
                                ""
                        ).stream()
                        .limit(15)
                        .toList()
        );

        if (!manager) {

            String email =
                    authentication.getName();

            model.addAttribute(
                    "assignedCount",
                    jdbc.queryForObject(
                            "SELECT COUNT(*) " +
                                    "FROM yeucausanxuat y " +
                                    "JOIN taikhoan t " +
                                    "ON t.manv = y.nguoiphutrach " +
                                    "WHERE t.email = ?",
                            Integer.class,
                            email
                    )
            );

            model.addAttribute(
                    "activeCount",
                    jdbc.queryForObject(
                            "SELECT COUNT(*) " +
                                    "FROM yeucausanxuat y " +
                                    "JOIN taikhoan t " +
                                    "ON t.manv = y.nguoiphutrach " +
                                    "WHERE t.email = ? " +
                                    "AND y.trangthai IN " +
                                    "('DA_GIAO', 'DANG_SAN_XUAT')",
                            Integer.class,
                            email
                    )
            );

            model.addAttribute(
                    "pendingMaterialCount",
                    jdbc.queryForObject(
                            "SELECT COUNT(*) " +
                                    "FROM phieuxuat_nvl p " +
                                    "JOIN taikhoan t " +
                                    "ON t.manv = p.nguoilap " +
                                    "WHERE t.email = ? " +
                                    "AND p.trangthai = 'CHO_DUYET'",
                            Integer.class,
                            email
                    )
            );

            model.addAttribute(
                    "pendingResultCount",
                    jdbc.queryForObject(
                            "SELECT COUNT(*) " +
                                    "FROM ketqua_sanxuat k " +
                                    "JOIN taikhoan t " +
                                    "ON t.manv = k.nguoithuchien " +
                                    "WHERE t.email = ? " +
                                    "AND k.trangthai = 'CHO_XAC_NHAN'",
                            Integer.class,
                            email
                    )
            );

            model.addAttribute(
                    "completedCount",
                    jdbc.queryForObject(
                            "SELECT COUNT(*) " +
                                    "FROM yeucausanxuat y " +
                                    "JOIN taikhoan t " +
                                    "ON t.manv = y.nguoiphutrach " +
                                    "WHERE t.email = ? " +
                                    "AND y.trangthai = 'HOAN_THANH'",
                            Integer.class,
                            email
                    )
            );

            // YÊU CẦU QUÁ HẠN CỦA NHÂN VIÊN
            model.addAttribute(
                    "overdueOrders",
                    jdbc.queryForObject(
                            "SELECT COUNT(*) " +
                                    "FROM yeucausanxuat y " +
                                    "JOIN taikhoan t " +
                                    "ON t.manv = y.nguoiphutrach " +
                                    "WHERE t.email = ? " +
                                    "AND y.trangthai NOT IN ('HOAN_THANH', 'HUY') " +
                                    "AND y.soluonghoanthanh < y.soluongyeucau " +
                                    "AND y.hanhoanthanh IS NOT NULL " +
                                    "AND y.hanhoanthanh < CURDATE()",
                            Integer.class,
                            email
                    )
            );

        } else {

            model.addAttribute(
                    "materials",
                    jdbc.queryForObject(
                            "SELECT COUNT(*) " +
                                    "FROM nguyenvatlieu",
                            Integer.class
                    )
            );

            model.addAttribute(
                    "lowStock",
                    jdbc.queryForObject(
                            "SELECT COUNT(*) " +
                                    "FROM nguyenvatlieu " +
                                    "WHERE soluongton <= ton_toithieu",
                            Integer.class
                    )
            );

            model.addAttribute(
                    "finishedStock",
                    jdbc.queryForObject(
                            "SELECT COALESCE(SUM(soluongton), 0) " +
                                    "FROM sanpham",
                            Long.class
                    )
            );

            model.addAttribute(
                    "activeOrders",
                    jdbc.queryForObject(
                            "SELECT COUNT(*) FROM (" +
                                    "    SELECT COALESCE(y.madot, -y.maycsx) AS nhom_dot " +
                                    "    FROM yeucausanxuat y " +
                                    "    GROUP BY COALESCE(y.madot, -y.maycsx) " +
                                    "    HAVING SUM(" +
                                    "        CASE " +
                                    "            WHEN y.trangthai NOT IN ('HUY', 'HOAN_THANH') " +
                                    "             AND y.soluonghoanthanh < y.soluongyeucau " +
                                    "            THEN y.soluongyeucau - y.soluonghoanthanh " +
                                    "            ELSE 0 " +
                                    "        END" +
                                    "    ) > 0 " +
                                    ") active_batches",
                            Integer.class
                    )
            );

            model.addAttribute(
                    "pendingMaterialIssues",
                    jdbc.queryForObject(
                            "SELECT COUNT(*) " +
                                    "FROM phieuxuat_nvl " +
                                    "WHERE trangthai = 'CHO_DUYET'",
                            Integer.class
                    )
            );

            model.addAttribute(
                    "pendingResults",
                    jdbc.queryForObject(
                            "SELECT COUNT(*) " +
                                    "FROM ketqua_sanxuat " +
                                    "WHERE trangthai = 'CHO_XAC_NHAN'",
                            Integer.class
                    )
            );

            model.addAttribute(
                    "pendingFinishedProductIssues",
                    jdbc.queryForObject(
                            "SELECT COUNT(*) " +
                                    "FROM phieuxuat_thanhpham " +
                                    "WHERE trangthai = 'CHO_DUYET'",
                            Integer.class
                    )
            );

// TỔNG ĐỢT SẢN XUẤT QUÁ HẠN
            model.addAttribute(
                    "overdueOrders",
                    jdbc.queryForObject(
                            "SELECT COUNT(*) FROM (" +
                                    "    SELECT " +
                                    "        COALESCE(y.madot, -y.maycsx) AS nhom_dot " +
                                    "    FROM yeucausanxuat y " +
                                    "    WHERE y.trangthai NOT IN ('HOAN_THANH', 'HUY') " +
                                    "      AND y.hanhoanthanh IS NOT NULL " +
                                    "      AND y.hanhoanthanh < CURDATE() " +
                                    "    GROUP BY COALESCE(y.madot, -y.maycsx) " +
                                    "    HAVING SUM(" +
                                    "        CASE " +
                                    "            WHEN y.soluonghoanthanh < y.soluongyeucau " +
                                    "            THEN 1 ELSE 0 " +
                                    "        END" +
                                    "    ) > 0 " +
                                    ") overdue_batches",
                            Integer.class
                    )
            );
        }

        return "mrp/dashboard";
    }
}
