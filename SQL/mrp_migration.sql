-- Schema MRP matching the existing lap123 MySQL database.
-- Run only after importing SQL/lap123.sql on a NEW database. Existing installations already containing these tables need no migration.
-- This file creates tables only; it never drops or overwrites data.
-- After this schema, run SQL/migrate_two_actors.sql to retain accounts and enforce the two-actor model.
USE lap123;

CREATE TABLE IF NOT EXISTS `nguyenvatlieu` (
  `manvl` int NOT NULL AUTO_INCREMENT,
  `mancc` int DEFAULT NULL,
  `manvl_code` varchar(30) NOT NULL,
  `tennvl` varchar(255) NOT NULL,
  `donvitinh` varchar(50) NOT NULL DEFAULT 'Cái',
  `dongianhap` decimal(18,2) NOT NULL DEFAULT '0.00',
  `soluongton` decimal(18,2) NOT NULL DEFAULT '0.00',
  `ton_toithieu` decimal(18,2) NOT NULL DEFAULT '0.00',
  `lead_time_ngay` int NOT NULL DEFAULT '0' COMMENT 'Số ngày dự kiến cần để nhập bổ sung',
  `trangthai` tinyint NOT NULL DEFAULT '1',
  `ghichu` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`manvl`),
  UNIQUE KEY `manvl_code` (`manvl_code`),
  KEY `fk_nvl_ncc` (`mancc`),
  CONSTRAINT `fk_nvl_ncc` FOREIGN KEY (`mancc`) REFERENCES `ncc` (`mancc`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `ck_nvl_leadtime` CHECK ((`lead_time_ngay` >= 0)),
  CONSTRAINT `ck_nvl_ton` CHECK ((`soluongton` >= 0)),
  CONSTRAINT `ck_nvl_ton_toithieu` CHECK ((`ton_toithieu` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `bom` (
  `mabom` int NOT NULL AUTO_INCREMENT,
  `masp` int NOT NULL,
  `phienban` varchar(30) NOT NULL DEFAULT '1.0',
  `ngaytao` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `nguoitao` int NOT NULL,
  `trangthai` varchar(20) NOT NULL DEFAULT 'HIEU_LUC' COMMENT 'HIEU_LUC, NGUNG_SU_DUNG',
  `ghichu` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`mabom`),
  UNIQUE KEY `uk_bom_sanpham_phienban` (`masp`,`phienban`),
  KEY `fk_bom_nguoitao` (`nguoitao`),
  CONSTRAINT `fk_bom_nguoitao` FOREIGN KEY (`nguoitao`) REFERENCES `nhanvien` (`manv`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_bom_sanpham` FOREIGN KEY (`masp`) REFERENCES `sanpham` (`masp`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `ck_bom_trangthai` CHECK ((`trangthai` in (_utf8mb4'HIEU_LUC',_utf8mb4'NGUNG_SU_DUNG')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `bom_chitiet` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `mabom` int NOT NULL,
  `manvl` int NOT NULL,
  `soluong_dinhmuc` decimal(18,4) NOT NULL,
  `tyle_haohut` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT 'Phần trăm hao hụt, ví dụ 2.5 = 2.5%',
  `ghichu` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bom_nvl` (`mabom`,`manvl`),
  KEY `fk_bomct_nvl` (`manvl`),
  CONSTRAINT `fk_bomct_bom` FOREIGN KEY (`mabom`) REFERENCES `bom` (`mabom`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_bomct_nvl` FOREIGN KEY (`manvl`) REFERENCES `nguyenvatlieu` (`manvl`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `ck_bomct_haohut` CHECK (((`tyle_haohut` >= 0) and (`tyle_haohut` <= 100))),
  CONSTRAINT `ck_bomct_soluong` CHECK ((`soluong_dinhmuc` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `yeucausanxuat` (
  `maycsx` bigint NOT NULL AUTO_INCREMENT,
  `masp` int NOT NULL,
  `mabom` int NOT NULL,
  `soluongyeucau` int NOT NULL,
  `soluonghoanthanh` int NOT NULL DEFAULT '0',
  `nguoitao` int NOT NULL COMMENT 'Quản lý tạo yêu cầu',
  `nguoiphutrach` int DEFAULT NULL COMMENT 'Nhân viên nhận yêu cầu',
  `ngayyeucau` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `hanhoanthanh` date DEFAULT NULL,
  `trangthai` varchar(30) NOT NULL DEFAULT 'MOI_TAO' COMMENT 'MOI_TAO, DA_GIAO, DANG_SAN_XUAT, HOAN_THANH, HUY',
  `ghichu` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`maycsx`),
  KEY `fk_ycsx_sanpham` (`masp`),
  KEY `fk_ycsx_bom` (`mabom`),
  KEY `fk_ycsx_nguoitao` (`nguoitao`),
  KEY `fk_ycsx_nguoiphutrach` (`nguoiphutrach`),
  KEY `idx_ycsx_trangthai` (`trangthai`),
  KEY `idx_ycsx_ngay` (`ngayyeucau`),
  CONSTRAINT `fk_ycsx_bom` FOREIGN KEY (`mabom`) REFERENCES `bom` (`mabom`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_ycsx_nguoiphutrach` FOREIGN KEY (`nguoiphutrach`) REFERENCES `nhanvien` (`manv`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_ycsx_nguoitao` FOREIGN KEY (`nguoitao`) REFERENCES `nhanvien` (`manv`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_ycsx_sanpham` FOREIGN KEY (`masp`) REFERENCES `sanpham` (`masp`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `ck_ycsx_sl` CHECK ((`soluongyeucau` > 0)),
  CONSTRAINT `ck_ycsx_slht` CHECK (((`soluonghoanthanh` >= 0) and (`soluonghoanthanh` <= `soluongyeucau`))),
  CONSTRAINT `ck_ycsx_trangthai` CHECK ((`trangthai` in (_utf8mb4'MOI_TAO',_utf8mb4'DA_GIAO',_utf8mb4'DANG_SAN_XUAT',_utf8mb4'HOAN_THANH',_utf8mb4'HUY')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `phieunhap_nvl` (
  `mapn` bigint NOT NULL AUTO_INCREMENT,
  `mancc` int NOT NULL,
  `nguoilap` int NOT NULL,
  `ngaylap` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `tongtien` decimal(18,2) NOT NULL DEFAULT '0.00',
  `trangthai` varchar(20) NOT NULL DEFAULT 'DA_NHAP' COMMENT 'NHAP_NHAP, DA_NHAP, HUY',
  `ghichu` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`mapn`),
  KEY `fk_pnnvl_ncc` (`mancc`),
  KEY `fk_pnnvl_nguoilap` (`nguoilap`),
  CONSTRAINT `fk_pnnvl_ncc` FOREIGN KEY (`mancc`) REFERENCES `ncc` (`mancc`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_pnnvl_nguoilap` FOREIGN KEY (`nguoilap`) REFERENCES `nhanvien` (`manv`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `ck_pnnvl_trangthai` CHECK ((`trangthai` in (_utf8mb4'NHAP_NHAP',_utf8mb4'DA_NHAP',_utf8mb4'HUY')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `ct_phieunhap_nvl` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `mapn` bigint NOT NULL,
  `manvl` int NOT NULL,
  `soluong` decimal(18,2) NOT NULL,
  `dongia` decimal(18,2) NOT NULL,
  `thanhtien` decimal(18,2) GENERATED ALWAYS AS ((`soluong` * `dongia`)) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pnnvl_nvl` (`mapn`,`manvl`),
  KEY `fk_ctpnnvl_nvl` (`manvl`),
  CONSTRAINT `fk_ctpnnvl_nvl` FOREIGN KEY (`manvl`) REFERENCES `nguyenvatlieu` (`manvl`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_ctpnnvl_pn` FOREIGN KEY (`mapn`) REFERENCES `phieunhap_nvl` (`mapn`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `ck_ctpnnvl_dg` CHECK ((`dongia` >= 0)),
  CONSTRAINT `ck_ctpnnvl_sl` CHECK ((`soluong` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `phieuxuat_nvl` (
  `mapx` bigint NOT NULL AUTO_INCREMENT,
  `maycsx` bigint NOT NULL,
  `nguoilap` int NOT NULL COMMENT 'Nhân viên đề nghị',
  `nguoiduyet` int DEFAULT NULL COMMENT 'Quản lý phê duyệt',
  `ngaylap` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ngayduyet` datetime DEFAULT NULL,
  `trangthai` varchar(20) NOT NULL DEFAULT 'CHO_DUYET' COMMENT 'CHO_DUYET, DA_DUYET, TU_CHOI, DA_XUAT, HUY',
  `lydo` varchar(255) DEFAULT NULL,
  `ghichu` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`mapx`),
  KEY `fk_pxnvl_ycsx` (`maycsx`),
  KEY `fk_pxnvl_nguoilap` (`nguoilap`),
  KEY `fk_pxnvl_nguoiduyet` (`nguoiduyet`),
  KEY `idx_pxnvl_trangthai` (`trangthai`),
  KEY `idx_pxnvl_ngaylap` (`ngaylap`),
  CONSTRAINT `fk_pxnvl_nguoiduyet` FOREIGN KEY (`nguoiduyet`) REFERENCES `nhanvien` (`manv`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_pxnvl_nguoilap` FOREIGN KEY (`nguoilap`) REFERENCES `nhanvien` (`manv`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_pxnvl_ycsx` FOREIGN KEY (`maycsx`) REFERENCES `yeucausanxuat` (`maycsx`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `ck_pxnvl_trangthai` CHECK ((`trangthai` in (_utf8mb4'CHO_DUYET',_utf8mb4'DA_DUYET',_utf8mb4'TU_CHOI',_utf8mb4'DA_XUAT',_utf8mb4'HUY')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `ct_phieuxuat_nvl` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `mapx` bigint NOT NULL,
  `manvl` int NOT NULL,
  `soluong` decimal(18,2) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pxnvl_nvl` (`mapx`,`manvl`),
  KEY `fk_ctpxnvl_nvl` (`manvl`),
  CONSTRAINT `fk_ctpxnvl_nvl` FOREIGN KEY (`manvl`) REFERENCES `nguyenvatlieu` (`manvl`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_ctpxnvl_px` FOREIGN KEY (`mapx`) REFERENCES `phieuxuat_nvl` (`mapx`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `ck_ctpxnvl_sl` CHECK ((`soluong` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `ketqua_sanxuat` (
  `makqsx` bigint NOT NULL AUTO_INCREMENT,
  `maycsx` bigint NOT NULL,
  `nguoithuchien` int NOT NULL,
  `soluonghoanthanh` int NOT NULL,
  `soluongloi` int NOT NULL DEFAULT '0',
  `ngayhoanthanh` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `trangthai` varchar(20) NOT NULL DEFAULT 'DA_NHAP_KHO' COMMENT 'CHO_XAC_NHAN, DA_NHAP_KHO, HUY',
  `ghichu` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`makqsx`),
  KEY `fk_kqsx_ycsx` (`maycsx`),
  KEY `fk_kqsx_nhanvien` (`nguoithuchien`),
  KEY `idx_kqsx_ngay` (`ngayhoanthanh`),
  CONSTRAINT `fk_kqsx_nhanvien` FOREIGN KEY (`nguoithuchien`) REFERENCES `nhanvien` (`manv`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_kqsx_ycsx` FOREIGN KEY (`maycsx`) REFERENCES `yeucausanxuat` (`maycsx`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `ck_kqsx_slht` CHECK ((`soluonghoanthanh` > 0)),
  CONSTRAINT `ck_kqsx_slloi` CHECK ((`soluongloi` >= 0)),
  CONSTRAINT `ck_kqsx_trangthai` CHECK ((`trangthai` in (_utf8mb4'CHO_XAC_NHAN',_utf8mb4'DA_NHAP_KHO',_utf8mb4'HUY')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `phieuxuat_thanhpham` (
  `mapxtp` bigint NOT NULL AUTO_INCREMENT,
  `makh` int DEFAULT NULL,
  `nguoilap` int NOT NULL COMMENT 'Nhân viên lập đề nghị',
  `nguoiduyet` int DEFAULT NULL COMMENT 'Quản lý phê duyệt',
  `ngaylap` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ngayduyet` datetime DEFAULT NULL,
  `trangthai` varchar(20) NOT NULL DEFAULT 'CHO_DUYET' COMMENT 'CHO_DUYET, DA_DUYET, TU_CHOI, DA_XUAT, HUY',
  `lydo` varchar(255) DEFAULT NULL,
  `ghichu` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`mapxtp`),
  KEY `fk_pxtp_khachhang` (`makh`),
  KEY `fk_pxtp_nguoilap` (`nguoilap`),
  KEY `fk_pxtp_nguoiduyet` (`nguoiduyet`),
  KEY `idx_pxtp_trangthai` (`trangthai`),
  KEY `idx_pxtp_ngaylap` (`ngaylap`),
  CONSTRAINT `fk_pxtp_khachhang` FOREIGN KEY (`makh`) REFERENCES `khachhang` (`makh`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_pxtp_nguoiduyet` FOREIGN KEY (`nguoiduyet`) REFERENCES `nhanvien` (`manv`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_pxtp_nguoilap` FOREIGN KEY (`nguoilap`) REFERENCES `nhanvien` (`manv`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `ck_pxtp_trangthai` CHECK ((`trangthai` in (_utf8mb4'CHO_DUYET',_utf8mb4'DA_DUYET',_utf8mb4'TU_CHOI',_utf8mb4'DA_XUAT',_utf8mb4'HUY')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `ct_phieuxuat_thanhpham` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `mapxtp` bigint NOT NULL,
  `masp` int NOT NULL,
  `soluong` int NOT NULL,
  `dongia` decimal(18,2) NOT NULL DEFAULT '0.00',
  `thanhtien` decimal(18,2) GENERATED ALWAYS AS ((`soluong` * `dongia`)) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pxtp_sp` (`mapxtp`,`masp`),
  KEY `fk_ctpxtp_sp` (`masp`),
  CONSTRAINT `fk_ctpxtp_px` FOREIGN KEY (`mapxtp`) REFERENCES `phieuxuat_thanhpham` (`mapxtp`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_ctpxtp_sp` FOREIGN KEY (`masp`) REFERENCES `sanpham` (`masp`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `ck_ctpxtp_dg` CHECK ((`dongia` >= 0)),
  CONSTRAINT `ck_ctpxtp_sl` CHECK ((`soluong` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `lich_su_kho` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `loai_hang` varchar(20) NOT NULL COMMENT 'NVL hoặc THANH_PHAM',
  `ma_hang` int NOT NULL COMMENT 'manvl hoặc masp tùy loai_hang',
  `loai_giaodich` varchar(30) NOT NULL COMMENT 'NHAP_NVL, XUAT_NVL, NHAP_THANH_PHAM, XUAT_THANH_PHAM',
  `soluong` decimal(18,2) NOT NULL,
  `don_gia` decimal(18,2) NOT NULL DEFAULT '0.00',
  `thoi_gian` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `nguoi_thuchien` int DEFAULT NULL,
  `bang_tham_chieu` varchar(50) DEFAULT NULL,
  `ma_tham_chieu` bigint DEFAULT NULL,
  `ghichu` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_lsk_nhanvien` (`nguoi_thuchien`),
  KEY `idx_lsk_thoigian` (`thoi_gian`),
  KEY `idx_lsk_loaihang_mahang` (`loai_hang`,`ma_hang`),
  KEY `idx_lsk_giaodich` (`loai_giaodich`),
  CONSTRAINT `fk_lsk_nhanvien` FOREIGN KEY (`nguoi_thuchien`) REFERENCES `nhanvien` (`manv`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `ck_lsk_giaodich` CHECK ((`loai_giaodich` in (_utf8mb4'NHAP_NVL',_utf8mb4'XUAT_NVL',_utf8mb4'NHAP_THANH_PHAM',_utf8mb4'XUAT_THANH_PHAM'))),
  CONSTRAINT `ck_lsk_loaihang` CHECK ((`loai_hang` in (_utf8mb4'NVL',_utf8mb4'THANH_PHAM'))),
  CONSTRAINT `ck_lsk_soluong` CHECK ((`soluong` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

