-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: lap123
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `bom`
--

DROP TABLE IF EXISTS `bom`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bom` (
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bom`
--

LOCK TABLES `bom` WRITE;
/*!40000 ALTER TABLE `bom` DISABLE KEYS */;
INSERT INTO `bom` VALUES (1,1,'1.0','2026-09-18 21:33:20',1,'HIEU_LUC','BOM chuẩn cho Laptop Lauriel Pro 15');
/*!40000 ALTER TABLE `bom` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bom_chitiet`
--

DROP TABLE IF EXISTS `bom_chitiet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bom_chitiet` (
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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bom_chitiet`
--

LOCK TABLES `bom_chitiet` WRITE;
/*!40000 ALTER TABLE `bom_chitiet` DISABLE KEYS */;
INSERT INTO `bom_chitiet` VALUES (1,1,1,1.0000,0.00,NULL),(2,1,2,1.0000,0.00,NULL),(3,1,3,1.0000,0.00,NULL),(4,1,4,1.0000,0.00,NULL),(5,1,5,1.0000,0.00,NULL),(6,1,6,1.0000,0.00,NULL);
/*!40000 ALTER TABLE `bom_chitiet` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chucnang`
--

DROP TABLE IF EXISTS `chucnang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chucnang` (
  `chucnang_id` int NOT NULL AUTO_INCREMENT,
  `ten` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`chucnang_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chucnang`
--

LOCK TABLES `chucnang` WRITE;
/*!40000 ALTER TABLE `chucnang` DISABLE KEYS */;
INSERT INTO `chucnang` VALUES (1,'QL_san_pham'),(2,'QL_phieu_nhap'),(3,'QL_phieu_xuat'),(4,'QL_khach_hang'),(5,'QL_nha_cung_cap'),(6,'QL_nhan_vien'),(7,'QL_ton_kho'),(8,'QL_thuong_hieu'),(9,'QL_tai_khoan');
/*!40000 ALTER TABLE `chucnang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ct_phieunhap`
--

DROP TABLE IF EXISTS `ct_phieunhap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ct_phieunhap` (
  `id` int NOT NULL AUTO_INCREMENT,
  `MAPHIEUNHAP` int DEFAULT NULL,
  `MASP` int DEFAULT NULL,
  `SOLUONG` int DEFAULT NULL,
  `gianhap` decimal(38,2) DEFAULT NULL,
  `TRANGTHAI` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4xe4kbd6vohet84patmiu76g2` (`MAPHIEUNHAP`),
  KEY `FKnbjcscb15621phrb7t05lkea` (`MASP`),
  CONSTRAINT `FK4xe4kbd6vohet84patmiu76g2` FOREIGN KEY (`MAPHIEUNHAP`) REFERENCES `phieunhap` (`MAPHIEUNHAP`),
  CONSTRAINT `FKnbjcscb15621phrb7t05lkea` FOREIGN KEY (`MASP`) REFERENCES `sanpham` (`masp`)
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ct_phieunhap`
--

LOCK TABLES `ct_phieunhap` WRITE;
/*!40000 ALTER TABLE `ct_phieunhap` DISABLE KEYS */;
INSERT INTO `ct_phieunhap` VALUES (16,43,18,10,32000000.00,0),(17,43,27,14,35000000.00,0),(22,47,27,2,20000000.00,0),(24,49,27,2,25000000.00,0),(26,51,27,5,10000000.00,0),(27,52,18,2,50000000.00,0),(28,53,25,9,2000000000.00,0),(29,53,20,9,5000000000.00,0),(30,54,27,7,7000000000.00,0),(33,57,28,2,200000.00,0),(34,58,28,2,100.00,0),(35,59,28,1,1100.00,0),(36,60,28,2,100.00,0),(37,61,28,3,2000000000.00,0),(38,62,28,5,20000000.00,0),(39,63,27,3,10000000.00,0),(40,64,22,1,100.00,0),(41,65,27,1,100.00,0),(42,66,27,1,100.00,0),(43,67,27,1,1.00,0),(44,68,27,1,100.00,0),(45,69,27,1,100.00,0),(46,70,27,1,1.00,0),(47,71,27,1,1.00,0),(48,72,27,1,1.00,0),(49,73,27,1,1.00,0),(50,73,25,1,10.00,0),(52,75,27,1,1.00,0),(53,76,22,1,1.00,0),(54,77,27,1,1.00,0),(57,80,27,1,1.00,0),(58,81,25,1,10.00,0),(59,81,22,1,1.00,0),(60,81,18,4,50000000.00,0),(61,81,20,1,5000000000.00,0);
/*!40000 ALTER TABLE `ct_phieunhap` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ct_phieunhap_nvl`
--

DROP TABLE IF EXISTS `ct_phieunhap_nvl`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ct_phieunhap_nvl` (
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ct_phieunhap_nvl`
--

LOCK TABLES `ct_phieunhap_nvl` WRITE;
/*!40000 ALTER TABLE `ct_phieunhap_nvl` DISABLE KEYS */;
INSERT INTO `ct_phieunhap_nvl` (`id`, `mapn`, `manvl`, `soluong`, `dongia`) VALUES (1,1,5,1.00,3000000.00);
/*!40000 ALTER TABLE `ct_phieunhap_nvl` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ct_phieuxuat`
--

DROP TABLE IF EXISTS `ct_phieuxuat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ct_phieuxuat` (
  `id` int NOT NULL AUTO_INCREMENT,
  `MAPHIEUXUAT` int DEFAULT NULL,
  `MASP` int DEFAULT NULL,
  `SOLUONG` int DEFAULT NULL,
  `giaxuat` decimal(38,2) DEFAULT NULL,
  `TRANGTHAI` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKdf4l71f62xx9a5ogj8eo23jw7` (`MASP`),
  KEY `FKcp0wv1qq6bpwt40cg2u4ctjb9` (`MAPHIEUXUAT`),
  CONSTRAINT `FKcp0wv1qq6bpwt40cg2u4ctjb9` FOREIGN KEY (`MAPHIEUXUAT`) REFERENCES `phieuxuat` (`MAPHIEUXUAT`),
  CONSTRAINT `FKdf4l71f62xx9a5ogj8eo23jw7` FOREIGN KEY (`MASP`) REFERENCES `sanpham` (`masp`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ct_phieuxuat`
--

LOCK TABLES `ct_phieuxuat` WRITE;
/*!40000 ALTER TABLE `ct_phieuxuat` DISABLE KEYS */;
INSERT INTO `ct_phieuxuat` VALUES (1,2,28,14,2000000000.00,0),(2,3,18,10,600000000.00,0),(4,5,18,1,120.00,0),(5,5,27,1,12.00,0),(6,6,22,1,100.00,0),(7,7,27,9,10.00,0),(10,9,27,1,4.00,0),(11,10,27,3,1.00,0),(12,11,22,1,1.00,0),(14,13,18,2,600000000.00,0);
/*!40000 ALTER TABLE `ct_phieuxuat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ct_phieuxuat_nvl`
--

DROP TABLE IF EXISTS `ct_phieuxuat_nvl`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ct_phieuxuat_nvl` (
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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ct_phieuxuat_nvl`
--

LOCK TABLES `ct_phieuxuat_nvl` WRITE;
/*!40000 ALTER TABLE `ct_phieuxuat_nvl` DISABLE KEYS */;
INSERT INTO `ct_phieuxuat_nvl` VALUES (1,1,1,2.00),(2,1,4,2.00),(3,1,5,2.00),(4,1,2,2.00),(5,1,3,2.00),(6,1,6,2.00);
/*!40000 ALTER TABLE `ct_phieuxuat_nvl` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ct_phieuxuat_thanhpham`
--

DROP TABLE IF EXISTS `ct_phieuxuat_thanhpham`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ct_phieuxuat_thanhpham` (
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ct_phieuxuat_thanhpham`
--

LOCK TABLES `ct_phieuxuat_thanhpham` WRITE;
/*!40000 ALTER TABLE `ct_phieuxuat_thanhpham` DISABLE KEYS */;
INSERT INTO `ct_phieuxuat_thanhpham` (`id`, `mapxtp`, `masp`, `soluong`, `dongia`) VALUES (1,1,1,3,0.00);
/*!40000 ALTER TABLE `ct_phieuxuat_thanhpham` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ketqua_sanxuat`
--

DROP TABLE IF EXISTS `ketqua_sanxuat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ketqua_sanxuat` (
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ketqua_sanxuat`
--

LOCK TABLES `ketqua_sanxuat` WRITE;
/*!40000 ALTER TABLE `ketqua_sanxuat` DISABLE KEYS */;
INSERT INTO `ketqua_sanxuat` VALUES (1,2,10,1,0,'2026-09-20 16:13:04','DA_NHAP_KHO',''),(2,2,10,2,0,'2026-09-22 07:33:50','DA_NHAP_KHO',''),(3,4,10,5,0,'2026-09-26 12:02:35','DA_NHAP_KHO',''),(4,5,10,5,0,'2026-09-26 12:06:47','CHO_XAC_NHAN',''),(5,6,10,5,0,'2026-09-26 12:38:02','CHO_XAC_NHAN','');
/*!40000 ALTER TABLE `ketqua_sanxuat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `khachhang`
--

DROP TABLE IF EXISTS `khachhang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `khachhang` (
  `makh` int NOT NULL AUTO_INCREMENT,
  `hoten` varchar(255) NOT NULL,
  `sdt` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `diachi` varchar(255) DEFAULT NULL,
  `trangthai` int DEFAULT NULL,
  `ghichu` varchar(255) DEFAULT NULL,
  `gioitinh` varchar(255) DEFAULT NULL,
  `ngaysinh` datetime(6) DEFAULT NULL,
  `ngaythamgia` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`makh`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `khachhang`
--

LOCK TABLES `khachhang` WRITE;
/*!40000 ALTER TABLE `khachhang` DISABLE KEYS */;
INSERT INTO `khachhang` VALUES (1,'Nguyễn Văn Giau','0321564975',NULL,NULL,0,NULL,'Nam','2026-09-01 17:00:00.000000',NULL),(2,'Lê Văn A','0316461926',NULL,NULL,0,NULL,'Nam','2021-04-07 17:00:00.000000',NULL);
/*!40000 ALTER TABLE `khachhang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lich_su_kho`
--

DROP TABLE IF EXISTS `lich_su_kho`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lich_su_kho` (
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lich_su_kho`
--

LOCK TABLES `lich_su_kho` WRITE;
/*!40000 ALTER TABLE `lich_su_kho` DISABLE KEYS */;
INSERT INTO `lich_su_kho` VALUES (1,'THANH_PHAM',1,'NHAP_THANH_PHAM',1.00,0.00,'2026-09-20 16:13:37',9,'ketqua_sanxuat',1,NULL),(2,'NVL',5,'NHAP_NVL',1.00,3000000.00,'2026-09-21 21:40:22',9,'phieunhap_nvl',1,NULL),(3,'THANH_PHAM',1,'NHAP_THANH_PHAM',2.00,0.00,'2026-09-22 07:35:28',9,'ketqua_sanxuat',2,NULL),(4,'THANH_PHAM',1,'NHAP_THANH_PHAM',5.00,0.00,'2026-09-26 12:03:00',9,'ketqua_sanxuat',3,NULL);
/*!40000 ALTER TABLE `lich_su_kho` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lichsu_dieuchinh_yeucau`
--

DROP TABLE IF EXISTS `lichsu_dieuchinh_yeucau`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lichsu_dieuchinh_yeucau` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `maycsx` bigint NOT NULL,
  `nguoisua` int NOT NULL,
  `thoigian` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `soluongcu` int NOT NULL,
  `soluongmoi` int NOT NULL,
  `han_cu` date DEFAULT NULL,
  `han_moi` date DEFAULT NULL,
  `nhanvien_cu` int DEFAULT NULL,
  `nhanvien_moi` int DEFAULT NULL,
  `lydodieuchinh` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_ls_ycsx` (`maycsx`),
  KEY `fk_ls_nguoisua` (`nguoisua`),
  CONSTRAINT `fk_ls_nguoisua` FOREIGN KEY (`nguoisua`) REFERENCES `nhanvien` (`manv`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_ls_ycsx` FOREIGN KEY (`maycsx`) REFERENCES `yeucausanxuat` (`maycsx`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lichsu_dieuchinh_yeucau`
--

LOCK TABLES `lichsu_dieuchinh_yeucau` WRITE;
/*!40000 ALTER TABLE `lichsu_dieuchinh_yeucau` DISABLE KEYS */;
INSERT INTO `lichsu_dieuchinh_yeucau` VALUES (1,4,9,'2026-09-26 11:51:38',12,6,'2026-09-27','2026-09-27',10,8,'Số lượng: 12 → 6. Nhân viên phụ trách: 10 → 8.'),(2,4,9,'2026-09-26 12:01:59',6,6,'2026-09-27','2026-09-27',8,10,'Nhân viên phụ trách: 8 → 10.'),(3,5,9,'2026-09-26 12:07:45',6,4,'2026-09-27','2026-09-27',10,10,'Số lượng: 6 → 4.'),(4,6,9,'2026-09-26 12:39:19',6,8,'2026-09-28','2026-09-28',10,10,'Số lượng: 6 → 8.'),(5,6,9,'2026-09-26 12:49:02',8,9,'2026-09-28','2026-09-28',10,10,'Số lượng: 8 → 9.'),(6,1,9,'2026-09-26 13:31:10',10,10,'2026-09-25','2026-09-30',2,2,'Hạn hoàn thành: 2026-09-25 → 2026-09-30.');
/*!40000 ALTER TABLE `lichsu_dieuchinh_yeucau` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ncc`
--

DROP TABLE IF EXISTS `ncc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ncc` (
  `mancc` int NOT NULL AUTO_INCREMENT,
  `tenncc` varchar(255) NOT NULL,
  `diachi` varchar(255) DEFAULT NULL,
  `sdt` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `trangthai` int DEFAULT NULL,
  `ghichu` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`mancc`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ncc`
--

LOCK TABLES `ncc` WRITE;
/*!40000 ALTER TABLE `ncc` DISABLE KEYS */;
INSERT INTO `ncc` VALUES (1,'Nhà cung cấp linh kiện ABC','TP.HCM','0280000001','sales@abc.local',0,'');
/*!40000 ALTER TABLE `ncc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nguyenvatlieu`
--

DROP TABLE IF EXISTS `nguyenvatlieu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nguyenvatlieu` (
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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nguyenvatlieu`
--

LOCK TABLES `nguyenvatlieu` WRITE;
/*!40000 ALTER TABLE `nguyenvatlieu` DISABLE KEYS */;
INSERT INTO `nguyenvatlieu` VALUES (1,1,'NVL-MAIN-001','Mainboard Laptop','Cái',4500000.00,20.00,5.00,7,1,NULL),(2,1,'NVL-RAM-001','RAM 16GB DDR5','Thanh',1200000.00,30.00,10.00,5,1,NULL),(3,1,'NVL-SSD-001','SSD NVMe 512GB','Ổ',1100000.00,30.00,10.00,5,1,NULL),(4,1,'NVL-SCR-001','Màn hình 15.6 inch','Cái',2500000.00,20.00,5.00,7,1,NULL),(5,1,'NVL-BAT-001','Pin Laptop','Cái',900000.00,21.00,5.00,7,1,''),(6,1,'NVL-CASE-001','Vỏ Laptop','Bộ',1500000.00,20.00,5.00,7,1,NULL);
/*!40000 ALTER TABLE `nguyenvatlieu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nhanvien`
--

DROP TABLE IF EXISTS `nhanvien`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nhanvien` (
  `manv` int NOT NULL AUTO_INCREMENT,
  `hoten` varchar(255) NOT NULL,
  `gioitinh` varchar(255) DEFAULT NULL,
  `ngaysinh` datetime(6) DEFAULT NULL,
  `sdt` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `ngaythamgia` datetime(6) DEFAULT NULL,
  `trangthai` int DEFAULT NULL,
  `ghichu` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`manv`),
  UNIQUE KEY `uk_nhanvien_email` (`email`),
  UNIQUE KEY `uk_nhanvien_sdt` (`sdt`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nhanvien`
--

LOCK TABLES `nhanvien` WRITE;
/*!40000 ALTER TABLE `nhanvien` DISABLE KEYS */;
INSERT INTO `nhanvien` VALUES (1,'Nguyễn Văn Quản Lý','Nam','1995-01-09 17:00:00.000000','0900000001','quanly@lap123.local','2025-12-31 17:00:00.000000',0,''),(2,'Trần Văn Nhân Viên','Nam','2003-05-19 17:00:00.000000','0900000002','nhanvien@lap123.local','2025-12-31 17:00:00.000000',0,''),(6,'Nguyễn Văn Anh','Nam','2112-12-11 17:00:00.000000','12321434',NULL,'2321-12-11 17:00:00.000000',0,''),(7,'Phan Văn Tre','Nữ','2012-12-20 17:00:00.000000','12121212',NULL,'2032-12-11 17:00:00.000000',0,''),(8,'Lê Tra','Nữ','2021-12-30 17:00:00.000000','12313',NULL,'2002-12-10 17:00:00.000000',1,'k'),(9,'Quản trị viên a',NULL,NULL,NULL,NULL,NULL,1,NULL),(10,'Nguyễn VĂN s','Nam','2012-02-05 17:00:00.000000','012345689',NULL,NULL,0,NULL);
/*!40000 ALTER TABLE `nhanvien` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `phanquyen`
--

DROP TABLE IF EXISTS `phanquyen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `phanquyen` (
  `vaitro_id` int NOT NULL,
  `chucnang_id` int NOT NULL,
  PRIMARY KEY (`vaitro_id`,`chucnang_id`),
  KEY `vaitro_id_idx` (`vaitro_id`),
  KEY `chucnang_id_idx` (`chucnang_id`),
  CONSTRAINT `chucnang_id` FOREIGN KEY (`chucnang_id`) REFERENCES `chucnang` (`chucnang_id`),
  CONSTRAINT `vaitro_id` FOREIGN KEY (`vaitro_id`) REFERENCES `vaitro` (`vaitro_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phanquyen`
--

LOCK TABLES `phanquyen` WRITE;
/*!40000 ALTER TABLE `phanquyen` DISABLE KEYS */;
INSERT INTO `phanquyen` VALUES (1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(2,1),(2,2),(2,3),(2,4),(2,5),(2,6),(2,7),(2,8),(3,2),(3,3),(3,7);
/*!40000 ALTER TABLE `phanquyen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `phieunhap`
--

DROP TABLE IF EXISTS `phieunhap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `phieunhap` (
  `MAPHIEUNHAP` int NOT NULL AUTO_INCREMENT,
  `MANCC` int DEFAULT NULL,
  `MANV` int DEFAULT NULL,
  `thoigian` date DEFAULT NULL,
  `tongtien` decimal(38,2) DEFAULT NULL,
  `TRANGTHAI` int DEFAULT NULL,
  `GHICHU` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`MAPHIEUNHAP`),
  KEY `FKqk7uxuhmox5x8hh2mpt9t3wnf` (`MANCC`),
  KEY `MANV_idx` (`MANV`),
  CONSTRAINT `FKqk7uxuhmox5x8hh2mpt9t3wnf` FOREIGN KEY (`MANCC`) REFERENCES `ncc` (`mancc`),
  CONSTRAINT `MANV` FOREIGN KEY (`MANV`) REFERENCES `nhanvien` (`manv`)
) ENGINE=InnoDB AUTO_INCREMENT=82 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieunhap`
--

LOCK TABLES `phieunhap` WRITE;
/*!40000 ALTER TABLE `phieunhap` DISABLE KEYS */;
INSERT INTO `phieunhap` VALUES (43,1,4,'2025-10-13',810000000.00,1,NULL),(47,2,24,'2025-10-14',40000000.00,1,NULL),(49,1,15,'2025-10-14',50000000.00,1,NULL),(51,1,24,'2025-10-15',50000000.00,1,NULL),(52,2,24,'2025-10-16',100000000.00,1,NULL),(53,2,6,'2025-10-15',63000000000.00,1,NULL),(54,2,4,'2025-10-15',49000000000.00,1,NULL),(57,2,4,'2025-10-15',400000.00,1,NULL),(58,1,4,'2025-10-15',200.00,1,NULL),(59,1,4,'2025-10-15',1100.00,1,NULL),(60,2,4,'2025-10-15',200.00,1,NULL),(61,1,4,'2025-10-15',6000000000.00,1,NULL),(62,1,4,'2025-10-15',100000000.00,1,NULL),(63,4,24,'2025-10-17',30000000.00,1,NULL),(64,1,4,'2025-10-17',100.00,1,NULL),(65,1,4,'2025-10-17',100.00,1,NULL),(66,1,4,'2025-10-17',100.00,1,NULL),(67,1,6,'2025-10-17',1.00,1,NULL),(68,1,6,'2025-10-17',100.00,1,NULL),(69,1,6,'2025-10-17',100.00,1,NULL),(70,1,4,'2025-10-17',1.00,1,NULL),(71,1,4,'2025-10-17',1.00,1,NULL),(72,1,6,'2025-10-17',1.00,1,NULL),(73,2,4,'2025-10-17',11.00,1,NULL),(75,1,4,'2025-10-18',1.00,1,NULL),(76,4,4,'2025-10-18',1.00,1,NULL),(77,2,4,'2025-10-18',1.00,1,NULL),(80,2,25,'2025-10-18',1.00,0,NULL),(81,1,6,'2026-09-17',5200000011.00,0,NULL);
/*!40000 ALTER TABLE `phieunhap` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `phieunhap_nvl`
--

DROP TABLE IF EXISTS `phieunhap_nvl`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `phieunhap_nvl` (
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieunhap_nvl`
--

LOCK TABLES `phieunhap_nvl` WRITE;
/*!40000 ALTER TABLE `phieunhap_nvl` DISABLE KEYS */;
INSERT INTO `phieunhap_nvl` VALUES (1,1,9,'2026-09-21 21:40:10',3000000.00,'DA_NHAP','');
/*!40000 ALTER TABLE `phieunhap_nvl` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `phieuxuat`
--

DROP TABLE IF EXISTS `phieuxuat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `phieuxuat` (
  `MAPHIEUXUAT` int NOT NULL AUTO_INCREMENT,
  `MAKH` int DEFAULT NULL,
  `MANV` int DEFAULT NULL,
  `thoigian` date DEFAULT NULL,
  `tongtien` decimal(38,2) DEFAULT NULL,
  `TRANGTHAI` int DEFAULT NULL,
  `GHICHU` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`MAPHIEUXUAT`),
  KEY `FKjd6uefgt2bd9d4mtmqk74n028` (`MAKH`),
  KEY `MANV_idx` (`MANV`),
  CONSTRAINT `FKf0xueec76ekuc457mjng6ll6h` FOREIGN KEY (`MANV`) REFERENCES `nhanvien` (`manv`),
  CONSTRAINT `FKjd6uefgt2bd9d4mtmqk74n028` FOREIGN KEY (`MAKH`) REFERENCES `khachhang` (`makh`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieuxuat`
--

LOCK TABLES `phieuxuat` WRITE;
/*!40000 ALTER TABLE `phieuxuat` DISABLE KEYS */;
INSERT INTO `phieuxuat` VALUES (2,3,4,'2025-10-15',28000000000.00,1,NULL),(3,3,24,'2025-10-18',6000000000.00,1,NULL),(5,3,4,'2025-10-17',132.00,1,NULL),(6,3,4,'2025-10-17',100.00,1,NULL),(7,4,6,'2025-10-17',90.00,1,NULL),(9,4,4,'2025-10-18',4.00,1,NULL),(10,4,4,'2025-10-18',3.00,1,NULL),(11,3,6,'2025-10-18',1.00,1,NULL),(13,3,6,'2026-09-17',1200000000.00,1,NULL);
/*!40000 ALTER TABLE `phieuxuat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `phieuxuat_nvl`
--

DROP TABLE IF EXISTS `phieuxuat_nvl`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `phieuxuat_nvl` (
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieuxuat_nvl`
--

LOCK TABLES `phieuxuat_nvl` WRITE;
/*!40000 ALTER TABLE `phieuxuat_nvl` DISABLE KEYS */;
INSERT INTO `phieuxuat_nvl` VALUES (1,2,10,9,'2026-09-21 22:05:52','2026-09-22 09:52:37','TU_CHOI','Không đủ để xuất, để giành bán','');
/*!40000 ALTER TABLE `phieuxuat_nvl` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `phieuxuat_thanhpham`
--

DROP TABLE IF EXISTS `phieuxuat_thanhpham`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `phieuxuat_thanhpham` (
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieuxuat_thanhpham`
--

LOCK TABLES `phieuxuat_thanhpham` WRITE;
/*!40000 ALTER TABLE `phieuxuat_thanhpham` DISABLE KEYS */;
INSERT INTO `phieuxuat_thanhpham` VALUES (1,NULL,10,NULL,'2026-09-21 22:13:55',NULL,'CHO_DUYET',NULL,'');
/*!40000 ALTER TABLE `phieuxuat_thanhpham` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sanpham`
--

DROP TABLE IF EXISTS `sanpham`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sanpham` (
  `masp` int NOT NULL AUTO_INCREMENT,
  `mathuonghieu` int DEFAULT NULL,
  `tensp` varchar(255) NOT NULL,
  `donvitinh` varchar(50) NOT NULL DEFAULT 'Cái',
  `hinhanhsp` varchar(255) DEFAULT NULL,
  `xuatxu` varchar(255) DEFAULT NULL,
  `chip` varchar(255) DEFAULT NULL,
  `hedieuhanh` varchar(255) DEFAULT NULL,
  `kichthuocman` varchar(255) DEFAULT NULL,
  `giaban` decimal(18,2) DEFAULT '0.00',
  `soluongton` int NOT NULL DEFAULT '0',
  `trangthai` int DEFAULT NULL,
  `ghichu` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`masp`),
  KEY `fk_sanpham_thuonghieu` (`mathuonghieu`),
  CONSTRAINT `fk_sanpham_thuonghieu` FOREIGN KEY (`mathuonghieu`) REFERENCES `thuonghieu` (`mathuonghieu`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `ck_sanpham_ton` CHECK ((`soluongton` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sanpham`
--

LOCK TABLES `sanpham` WRITE;
/*!40000 ALTER TABLE `sanpham` DISABLE KEYS */;
INSERT INTO `sanpham` VALUES (1,1,'Laptop Lenovo LOQ','Cái','1790001828294_ssss_13__1_12.webp','Việt Nam','Intel Core i5','Windows','15.6 inch',19990000.00,8,0,'');
/*!40000 ALTER TABLE `sanpham` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `taikhoan`
--

DROP TABLE IF EXISTS `taikhoan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `taikhoan` (
  `taikhoan_id` int NOT NULL AUTO_INCREMENT,
  `manv` int NOT NULL,
  `vaitro_id` int NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `trangthai` tinyint NOT NULL DEFAULT '1' COMMENT '1: hoạt động, 0: khóa',
  `reset_token` varchar(255) DEFAULT NULL,
  `locked` bit(1) NOT NULL,
  PRIMARY KEY (`taikhoan_id`),
  UNIQUE KEY `email` (`email`),
  KEY `fk_taikhoan_nhanvien` (`manv`),
  KEY `fk_taikhoan_vaitro` (`vaitro_id`),
  CONSTRAINT `fk_taikhoan_nhanvien` FOREIGN KEY (`manv`) REFERENCES `nhanvien` (`manv`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_taikhoan_vaitro` FOREIGN KEY (`vaitro_id`) REFERENCES `vaitro` (`vaitro_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `taikhoan`
--

LOCK TABLES `taikhoan` WRITE;
/*!40000 ALTER TABLE `taikhoan` DISABLE KEYS */;
INSERT INTO `taikhoan` VALUES (1,1,1,'quanly@lap123.local','$2a$10$PLACEHOLDER_HASH_FOR_MANAGER',1,NULL,_binary ''),(2,2,2,'nhanvien@lap123.local','$2a$10$PLACEHOLDER_HASH_FOR_EMPLOYEE',1,NULL,_binary ''),(6,6,1,'admin@mail.com','$2a$10$uz35qMlz/c1VWzXVqur1FOcGqxdrmb7H9TWKnBhXdIVRwT2kgmFcO',1,NULL,_binary ''),(7,7,1,'chien@gmail.com','$2a$10$uz35qMlz/c1VWzXVqur1FOcGqxdrmb7H9TWKnBhXdIVRwT2kgmFcO',1,NULL,_binary ''),(8,8,2,'df@gmial.com','$2a$10$uz35qMlz/c1VWzXVqur1FOcGqxdrmb7H9TWKnBhXdIVRwT2kgmFcO',1,NULL,_binary ''),(9,9,1,'a','$2a$10$in3KlNF0odwSm8Q6O3Ee9O6sLzkeAKT0pb1y.kwpVGy6fTCXkgaia',1,NULL,_binary '\0'),(10,10,2,'N','$2a$10$X7ygq7e5PkNFFLw4Wkyi8ec/uz4KuOHXunXbQB0a7HhOmFYmIDi1W',1,NULL,_binary '\0');
/*!40000 ALTER TABLE `taikhoan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `thuonghieu`
--

DROP TABLE IF EXISTS `thuonghieu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `thuonghieu` (
  `mathuonghieu` int NOT NULL AUTO_INCREMENT,
  `tenthuonghieu` varchar(255) NOT NULL,
  `trangthai` int DEFAULT NULL,
  `ghichu` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`mathuonghieu`),
  UNIQUE KEY `tenthuonghieu` (`tenthuonghieu`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `thuonghieu`
--

LOCK TABLES `thuonghieu` WRITE;
/*!40000 ALTER TABLE `thuonghieu` DISABLE KEYS */;
INSERT INTO `thuonghieu` VALUES (1,'Lenovo',0,'');
/*!40000 ALTER TABLE `thuonghieu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vaitro`
--

DROP TABLE IF EXISTS `vaitro`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vaitro` (
  `vaitro_id` int NOT NULL AUTO_INCREMENT,
  `ma_vaitro` varchar(255) DEFAULT NULL,
  `ten_vaitro` varchar(100) NOT NULL,
  `ten` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`vaitro_id`),
  UNIQUE KEY `ma_vaitro` (`ma_vaitro`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vaitro`
--

LOCK TABLES `vaitro` WRITE;
/*!40000 ALTER TABLE `vaitro` DISABLE KEYS */;
INSERT INTO `vaitro` VALUES (1,'QUAN_LY_KHO','Người quản lý kho',NULL),(2,'NHAN_VIEN','Nhân viên sản xuất / kho',NULL);
/*!40000 ALTER TABLE `vaitro` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yeucausanxuat`
--

DROP TABLE IF EXISTS `yeucausanxuat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `yeucausanxuat` (
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
  `lydohuy` varchar(255) DEFAULT NULL,
  `ngayhuy` datetime DEFAULT NULL,
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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yeucausanxuat`
--

LOCK TABLES `yeucausanxuat` WRITE;
/*!40000 ALTER TABLE `yeucausanxuat` DISABLE KEYS */;
INSERT INTO `yeucausanxuat` VALUES (1,1,1,10,0,1,2,'2026-09-18 21:33:20','2026-09-30','DA_GIAO','Sản xuất 10 laptop theo BOM phiên bản 1.0',NULL,NULL),(2,1,1,3,3,9,10,'2026-09-20 16:10:37','2026-09-30','HOAN_THANH','',NULL,NULL),(3,1,1,11,0,9,8,'2026-09-26 11:46:06','2026-09-27','HUY','','hihi','2026-09-26 11:47:14'),(4,1,1,6,5,9,10,'2026-09-26 11:51:11','2026-09-27','HUY','','hihi','2026-09-26 12:03:33'),(5,1,1,4,0,9,10,'2026-09-26 12:06:28','2026-09-27','HUY','','hihi','2026-09-26 12:08:09'),(6,1,1,9,0,9,10,'2026-09-26 12:37:41','2026-09-28','HUY','','hihh','2026-09-26 13:13:29');
/*!40000 ALTER TABLE `yeucausanxuat` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-26 13:45:12
