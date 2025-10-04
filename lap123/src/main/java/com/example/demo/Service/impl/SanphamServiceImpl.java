package com.example.demo.Service.impl;

import com.example.demo.Entity.Nhanvien;
import com.example.demo.Entity.Sanpham;
import com.example.demo.Repository.SanphamRepository;
import com.example.demo.Service.SanphamService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SanphamServiceImpl implements SanphamService {

    private SanphamRepository sanphamRepository;

    public SanphamServiceImpl(SanphamRepository sanphamRepository) {
        super();
        this.sanphamRepository = sanphamRepository;
    }

    @Override
    public List<Sanpham> getAllSanpham() {
        return sanphamRepository.findAll();
    }

    @Override
    public Sanpham createSanpham(Sanpham sanpham) {
        return sanphamRepository.save(sanpham);
    }

    @Override
    public Sanpham saveSanpham(Sanpham sanpham) {
        return sanphamRepository.save(sanpham);
    }

    @Override
    public Sanpham getSanphamById(int MASP) {
        Optional<Sanpham> sanpham = sanphamRepository.findById(MASP);
        if (sanpham.isPresent()) {
            return sanpham.get();
        } else {
            return null;
        }
    }

    @Override
    public Sanpham updateSanpham(Sanpham sanpham) {
        // Lấy sản phẩm cũ từ DB
        Sanpham existing = sanphamRepository.findById(sanpham.getMASP())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id=" + sanpham.getMASP()));

        // Gán lại các field từ form (chỉ update những field bạn muốn sửa)
        existing.setTENSP(sanpham.getTENSP());
        existing.setXUATXU(sanpham.getXUATXU());
        existing.setCHIP(sanpham.getCHIP());
        existing.setHEDIEUHANH(sanpham.getHEDIEUHANH());
        existing.setKICHTHUOCMAN(sanpham.getKICHTHUOCMAN());
        existing.setThuonghieu(sanpham.getThuonghieu());
        existing.setHEDIEUHANH(sanpham.getHEDIEUHANH());
        existing.setTRANGTHAI(sanpham.getTRANGTHAI());

        // Nếu trong form có upload ảnh mới, thì xử lý update ảnh ở đây
        // còn nếu không có, giữ nguyên existing.getHINHANHSP()

        return sanphamRepository.save(existing);
    }

    @Override
    public void deleteSanpham(int id) {
        sanphamRepository.deleteById(id);
    }


}
