package com.example.demo.Controller;

import com.example.demo.Entity.Sanpham;
import com.example.demo.Service.SanphamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class MrpController {

    @Autowired
    private SanphamService sanphamService;

    // 1. Quản lý Định mức sản xuất (BOM - Bill of Materials) - Dành cho Quản lý
    @GetMapping("/bom")
    public String viewBomPage(Model model) {
        List<Sanpham> danhSachSp = sanphamService.getAllSanpham();
        model.addAttribute("sanphams", danhSachSp);
        return "mrp/bom";
    }

    // 2. Kế hoạch sản xuất & Tính toán MRP tự động - Dành cho Quản lý
    @GetMapping("/mrp")
    public String viewMrpEnginePage(@RequestParam(value = "sanphamId", required = false) Integer sanphamId,
                                    @RequestParam(value = "soluong", defaultValue = "10") Integer soluong,
                                    Model model) {
        List<Sanpham> danhSachSp = sanphamService.getAllSanpham();
        model.addAttribute("sanphams", danhSachSp);
        model.addAttribute("selectedSpId", sanphamId);
        model.addAttribute("soluongCanLap", soluong);
        return "mrp/mrp-engine";
    }

    // 3. XƯỞNG LẮP RÁP CỦA NHÂN VIÊN (Tiếp nhận lệnh, Đề xuất xuất linh kiện, Báo hoàn thành)
    @GetMapping("/xuong-lap-rap")
    public String viewXuongLapRap(Model model) {
        List<Sanpham> danhSachSp = sanphamService.getAllSanpham();
        model.addAttribute("sanphams", danhSachSp);
        return "mrp/xuong-lap-rap";
    }

    // 4. TRUNG TÂM PHÊ DUYỆT PHIẾU KHO - Dành cho Quản lý
    @GetMapping("/duyet-kho")
    public String viewDuyetKhoPage(Model model) {
        return "mrp/duyet-kho";
    }

    // 5. Báo cáo thống kê Xuất - Nhập - Tồn MRP - Dành cho Quản lý
    @GetMapping("/baocao")
    public String viewBaoCaoPage(Model model) {
        List<Sanpham> danhSachSp = sanphamService.getAllSanpham();
        model.addAttribute("sanphams", danhSachSp);
        return "mrp/baocao";
    }
}
