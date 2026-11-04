-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: lap123
-- ------------------------------------------------------
-- Server version	9.7.0

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
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

-- SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '3b6d01a6-4b56-11f1-bebb-d03957e471cc:1-2151';

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
INSERT INTO `bom` VALUES (1,3,'2026','2026-10-20 17:11:30',9,'HIEU_LUC','');
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
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bom_chitiet`
--

LOCK TABLES `bom_chitiet` WRITE;
/*!40000 ALTER TABLE `bom_chitiet` DISABLE KEYS */;
INSERT INTO `bom_chitiet` VALUES (1,1,1,1.0000,0.00,NULL),(2,1,3,1.0000,0.00,NULL),(3,1,6,1.0000,0.00,NULL),(4,1,8,1.0000,0.00,NULL),(5,1,10,1.0000,0.00,NULL),(6,1,13,1.0000,0.00,NULL),(7,1,14,1.0000,0.00,NULL),(8,1,16,1.0000,0.00,NULL),(9,1,19,1.0000,0.00,NULL),(10,1,20,1.0000,0.00,NULL),(11,1,22,1.0000,0.00,NULL),(12,1,25,1.0000,0.00,NULL),(13,1,27,1.0000,0.00,NULL);
/*!40000 ALTER TABLE `bom_chitiet` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ct_phieunhap`
--

DROP TABLE IF EXISTS `ct_phieunhap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ct_phieunhap` (
  `id` int NOT NULL AUTO_INCREMENT,
  `gianhap` decimal(38,2) DEFAULT NULL,
  `soluong` int DEFAULT NULL,
  `trangthai` int DEFAULT NULL,
  `maphieunhap` int DEFAULT NULL,
  `masp` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4xe4kbd6vohet84patmiu76g2` (`maphieunhap`),
  KEY `FKnbjcscb15621phrb7t05lkea` (`masp`),
  CONSTRAINT `FK4xe4kbd6vohet84patmiu76g2` FOREIGN KEY (`maphieunhap`) REFERENCES `phieunhap` (`maphieunhap`),
  CONSTRAINT `FKnbjcscb15621phrb7t05lkea` FOREIGN KEY (`masp`) REFERENCES `sanpham` (`masp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ct_phieunhap`
--

LOCK TABLES `ct_phieunhap` WRITE;
/*!40000 ALTER TABLE `ct_phieunhap` DISABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ct_phieunhap_nvl`
--

LOCK TABLES `ct_phieunhap_nvl` WRITE;
/*!40000 ALTER TABLE `ct_phieunhap_nvl` DISABLE KEYS */;
INSERT INTO `ct_phieunhap_nvl` (`id`, `mapn`, `manvl`, `soluong`, `dongia`) VALUES (1,1,2,100.00,150000.00),(2,1,1,100.00,200000.00),(3,1,4,100.00,200000.00),(4,1,3,100.00,200000.00),(5,1,7,100.00,500000.00),(6,1,5,100.00,500000.00),(7,1,6,100.00,800000.00),(8,1,9,100.00,4000000.00),(9,1,8,100.00,3000000.00),(10,1,10,100.00,200000.00),(11,1,11,100.00,300000.00),(12,1,13,100.00,2000000.00),(13,1,12,100.00,700000.00),(14,1,14,100.00,1000000.00),(15,1,15,100.00,5000000.00),(16,1,16,100.00,500000.00),(17,1,17,100.00,1000000.00),(18,1,19,100.00,5000000.00),(19,1,18,100.00,5000000.00),(20,1,20,100.00,5000000.00),(21,1,21,100.00,4000000.00),(22,1,22,100.00,1000000.00),(23,1,23,100.00,700000.00),(24,1,25,100.00,600000.00),(25,1,24,100.00,1000000.00),(26,2,27,300.00,2000000.00);
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
  `trangthai` int DEFAULT NULL,
  `giaxuat` decimal(38,2) DEFAULT NULL,
  `soluong` int DEFAULT NULL,
  `maphieuxuat` int DEFAULT NULL,
  `masp` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKcp0wv1qq6bpwt40cg2u4ctjb9` (`maphieuxuat`),
  KEY `FKdf4l71f62xx9a5ogj8eo23jw7` (`masp`),
  CONSTRAINT `FKcp0wv1qq6bpwt40cg2u4ctjb9` FOREIGN KEY (`maphieuxuat`) REFERENCES `phieuxuat` (`maphieuxuat`),
  CONSTRAINT `FKdf4l71f62xx9a5ogj8eo23jw7` FOREIGN KEY (`masp`) REFERENCES `sanpham` (`masp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ct_phieuxuat`
--

LOCK TABLES `ct_phieuxuat` WRITE;
/*!40000 ALTER TABLE `ct_phieuxuat` DISABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ct_phieuxuat_nvl`
--

LOCK TABLES `ct_phieuxuat_nvl` WRITE;
/*!40000 ALTER TABLE `ct_phieuxuat_nvl` DISABLE KEYS */;
INSERT INTO `ct_phieuxuat_nvl` VALUES (1,1,1,10.00),(2,1,3,10.00),(3,1,6,10.00),(4,1,8,10.00),(5,1,10,10.00),(6,1,13,10.00),(7,1,14,10.00),(8,1,16,10.00),(9,1,19,10.00),(10,1,20,10.00),(11,1,22,10.00),(12,1,25,10.00),(13,1,27,10.00),(14,2,1,2.00),(15,2,3,2.00),(16,2,6,2.00),(17,2,8,2.00),(18,2,10,2.00),(19,2,13,2.00),(20,2,14,2.00),(21,2,16,2.00),(22,2,19,2.00),(23,2,20,2.00),(24,2,22,2.00),(25,2,25,2.00),(26,2,27,2.00),(27,3,10,1.00),(28,4,20,10.00),(29,5,1,1.00);
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
  `mabom` int DEFAULT NULL,
  `soluong` int NOT NULL,
  `dongia` decimal(18,2) NOT NULL DEFAULT '0.00',
  `gia_da_fix` tinyint(1) NOT NULL DEFAULT '0',
  `thanhtien` decimal(18,2) GENERATED ALWAYS AS ((`soluong` * `dongia`)) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pxtp_sp` (`mapxtp`,`masp`),
  KEY `fk_ctpxtp_sp` (`masp`),
  CONSTRAINT `fk_ctpxtp_px` FOREIGN KEY (`mapxtp`) REFERENCES `phieuxuat_thanhpham` (`mapxtp`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_ctpxtp_sp` FOREIGN KEY (`masp`) REFERENCES `sanpham` (`masp`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `ck_ctpxtp_dg` CHECK ((`dongia` >= 0)),
  CONSTRAINT `ck_ctpxtp_sl` CHECK ((`soluong` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ct_phieuxuat_thanhpham`
--

LOCK TABLES `ct_phieuxuat_thanhpham` WRITE;
/*!40000 ALTER TABLE `ct_phieuxuat_thanhpham` DISABLE KEYS */;
/*!40000 ALTER TABLE `ct_phieuxuat_thanhpham` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dot_san_xuat`
--

DROP TABLE IF EXISTS `dot_san_xuat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dot_san_xuat` (
  `madot` bigint NOT NULL AUTO_INCREMENT,
  `madot_code` varchar(30) DEFAULT NULL,
  `tendot` varchar(150) NOT NULL,
  `nguoitao` int NOT NULL,
  `ngaytao` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ghichu` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`madot`),
  UNIQUE KEY `uk_dot_san_xuat_code` (`madot_code`),
  KEY `idx_dot_san_xuat_ngaytao` (`ngaytao`),
  KEY `fk_dot_san_xuat_nguoitao` (`nguoitao`),
  CONSTRAINT `fk_dot_san_xuat_nguoitao` FOREIGN KEY (`nguoitao`) REFERENCES `nhanvien` (`manv`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dot_san_xuat`
--

LOCK TABLES `dot_san_xuat` WRITE;
/*!40000 ALTER TABLE `dot_san_xuat` DISABLE KEYS */;
INSERT INTO `dot_san_xuat` VALUES (1,'SX-20261020-000001','Sản xuất asus T10',9,'2026-10-20 17:17:37',''),(2,'SX-20261021-000002','Sản xuất bổ sung T10',9,'2026-10-21 16:48:47','');
/*!40000 ALTER TABLE `dot_san_xuat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ghi_nhan_san_xuat`
--

DROP TABLE IF EXISTS `ghi_nhan_san_xuat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ghi_nhan_san_xuat` (
  `id` int NOT NULL AUTO_INCREMENT,
  `maycsx` bigint NOT NULL,
  `masp` int NOT NULL,
  `mabom` int NOT NULL,
  `soluong` int NOT NULL,
  `nguoighi` int NOT NULL,
  `ngayghi` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ghichu` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_ghi_nhan_ycsx` (`maycsx`),
  KEY `fk_ghi_nhan_sp` (`masp`),
  KEY `fk_ghi_nhan_bom` (`mabom`),
  KEY `fk_ghi_nhan_nv` (`nguoighi`),
  CONSTRAINT `fk_ghi_nhan_bom` FOREIGN KEY (`mabom`) REFERENCES `bom` (`mabom`),
  CONSTRAINT `fk_ghi_nhan_nv` FOREIGN KEY (`nguoighi`) REFERENCES `nhanvien` (`manv`),
  CONSTRAINT `fk_ghi_nhan_sp` FOREIGN KEY (`masp`) REFERENCES `sanpham` (`masp`),
  CONSTRAINT `fk_ghi_nhan_ycsx` FOREIGN KEY (`maycsx`) REFERENCES `yeucausanxuat` (`maycsx`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ghi_nhan_san_xuat`
--

LOCK TABLES `ghi_nhan_san_xuat` WRITE;
/*!40000 ALTER TABLE `ghi_nhan_san_xuat` DISABLE KEYS */;
/*!40000 ALTER TABLE `ghi_nhan_san_xuat` ENABLE KEYS */;
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
  `ma_dot_ghi_nhan` char(36) DEFAULT NULL,
  `lydotuchoi` varchar(500) DEFAULT NULL,
  `nguoiduyet` int DEFAULT NULL,
  `ngayduyet` datetime DEFAULT NULL,
  `id_ghi_nhan` int DEFAULT NULL,
  PRIMARY KEY (`makqsx`),
  UNIQUE KEY `uq_ketqua_ghi_nhan` (`id_ghi_nhan`),
  KEY `fk_kqsx_ycsx` (`maycsx`),
  KEY `fk_kqsx_nhanvien` (`nguoithuchien`),
  KEY `idx_kqsx_ngay` (`ngayhoanthanh`),
  KEY `idx_kqsx_nguoiduyet` (`nguoiduyet`),
  CONSTRAINT `fk_kqsx_nguoiduyet` FOREIGN KEY (`nguoiduyet`) REFERENCES `nhanvien` (`manv`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_kqsx_nhanvien` FOREIGN KEY (`nguoithuchien`) REFERENCES `nhanvien` (`manv`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_kqsx_ycsx` FOREIGN KEY (`maycsx`) REFERENCES `yeucausanxuat` (`maycsx`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `ck_kqsx_slht` CHECK ((`soluonghoanthanh` > 0)),
  CONSTRAINT `ck_kqsx_slloi` CHECK ((`soluongloi` >= 0)),
  CONSTRAINT `ck_kqsx_trangthai` CHECK ((`trangthai` in (_utf8mb4'CHO_XAC_NHAN',_utf8mb4'DA_NHAP_KHO',_utf8mb4'TU_CHOI_LOI',_utf8mb4'TU_CHOI_SAI_YEU_CAU',_utf8mb4'HUY')))
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ketqua_sanxuat`
--

LOCK TABLES `ketqua_sanxuat` WRITE;
/*!40000 ALTER TABLE `ketqua_sanxuat` DISABLE KEYS */;
INSERT INTO `ketqua_sanxuat` VALUES (1,1,10,5,0,'2026-10-21 14:34:36','DA_NHAP_KHO','sản xuất trước 5 cái','d7fd0008-0fbd-45af-b43b-9469040b4ea5',NULL,NULL,NULL,NULL),(2,1,10,5,0,'2026-10-21 16:30:07','DA_NHAP_KHO',NULL,'880825e3-3277-40eb-81d8-a81fa37c9490',NULL,NULL,NULL,NULL),(3,2,10,2,0,'2026-10-21 17:11:34','DA_NHAP_KHO',NULL,'6017c3f0-4dcf-4268-89e2-8ee5cba77b55',NULL,NULL,NULL,NULL);
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
INSERT INTO `khachhang` VALUES (1,'Tin','0935991456',NULL,NULL,0,NULL,'Nam','2012-12-04 17:00:00.000000','2026-12-09 17:00:00.000000'),(2,'Chiến','031326599',NULL,NULL,0,NULL,'Nam','2024-12-22 17:00:00.000000',NULL);
/*!40000 ALTER TABLE `khachhang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `kho_loi_san_xuat`
--

DROP TABLE IF EXISTS `kho_loi_san_xuat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `kho_loi_san_xuat` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `masp` int NOT NULL,
  `mabom` int NOT NULL,
  `soluongton` int NOT NULL DEFAULT '0',
  `ngaycapnhat` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_kho_loi_sp_bom` (`masp`,`mabom`),
  KEY `fk_kho_loi_bom` (`mabom`),
  CONSTRAINT `fk_kho_loi_bom` FOREIGN KEY (`mabom`) REFERENCES `bom` (`mabom`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_kho_loi_sp` FOREIGN KEY (`masp`) REFERENCES `sanpham` (`masp`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `ck_kho_loi_ton` CHECK ((`soluongton` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `kho_loi_san_xuat`
--

LOCK TABLES `kho_loi_san_xuat` WRITE;
/*!40000 ALTER TABLE `kho_loi_san_xuat` DISABLE KEYS */;
/*!40000 ALTER TABLE `kho_loi_san_xuat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `kho_nvl_san_xuat`
--

DROP TABLE IF EXISTS `kho_nvl_san_xuat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `kho_nvl_san_xuat` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `manvl` int NOT NULL,
  `soluongton` decimal(18,2) NOT NULL DEFAULT '0.00',
  `ngaycapnhat` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_kho_nvl_sx_manvl` (`manvl`),
  KEY `idx_kho_nvl_sx_manvl` (`manvl`),
  CONSTRAINT `fk_kho_nvl_sx_nvl` FOREIGN KEY (`manvl`) REFERENCES `nguyenvatlieu` (`manvl`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `ck_kho_nvl_sx_ton` CHECK ((`soluongton` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `kho_nvl_san_xuat`
--

LOCK TABLES `kho_nvl_san_xuat` WRITE;
/*!40000 ALTER TABLE `kho_nvl_san_xuat` DISABLE KEYS */;
INSERT INTO `kho_nvl_san_xuat` VALUES (1,1,0.00,'2026-10-21 17:11:34'),(2,2,0.00,'2026-10-19 12:49:57'),(3,3,0.00,'2026-10-21 17:11:34'),(4,4,0.00,'2026-10-20 16:46:57'),(5,5,0.00,'2026-10-20 16:47:53'),(6,6,0.00,'2026-10-21 17:11:34'),(7,7,0.00,'2026-10-20 16:49:12'),(8,8,0.00,'2026-10-21 17:11:34'),(9,9,0.00,'2026-10-20 16:50:10'),(10,10,0.00,'2026-10-21 17:47:08'),(11,11,0.00,'2026-10-20 16:50:49'),(12,12,0.00,'2026-10-20 16:51:22'),(13,13,0.00,'2026-10-21 17:11:34'),(14,14,0.00,'2026-10-21 17:11:34'),(15,15,0.00,'2026-10-20 16:53:12'),(16,16,0.00,'2026-10-21 17:11:34'),(17,17,0.00,'2026-10-20 16:54:15'),(18,18,0.00,'2026-10-20 16:54:55'),(19,19,0.00,'2026-10-21 17:11:34'),(20,20,0.00,'2026-10-21 17:11:34'),(21,21,0.00,'2026-10-20 16:56:10'),(22,22,0.00,'2026-10-21 17:11:34'),(23,23,0.00,'2026-10-20 16:57:11'),(24,24,0.00,'2026-10-20 16:57:47'),(25,25,0.00,'2026-10-21 17:11:34'),(26,26,0.00,'2026-10-20 16:58:53'),(27,27,0.00,'2026-10-21 17:11:34');
/*!40000 ALTER TABLE `kho_nvl_san_xuat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `kho_thanhpham_san_xuat`
--

DROP TABLE IF EXISTS `kho_thanhpham_san_xuat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `kho_thanhpham_san_xuat` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `masp` int NOT NULL,
  `mabom` int NOT NULL,
  `soluongton` int NOT NULL DEFAULT '0',
  `ngaycapnhat` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_kho_tp_sx_sp_bom` (`masp`,`mabom`),
  KEY `idx_kho_tp_sx_sp` (`masp`),
  KEY `idx_kho_tp_sx_bom` (`mabom`),
  CONSTRAINT `fk_kho_tp_sx_bom` FOREIGN KEY (`mabom`) REFERENCES `bom` (`mabom`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_kho_tp_sx_sp` FOREIGN KEY (`masp`) REFERENCES `sanpham` (`masp`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `ck_kho_tp_sx_ton` CHECK ((`soluongton` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `kho_thanhpham_san_xuat`
--

LOCK TABLES `kho_thanhpham_san_xuat` WRITE;
/*!40000 ALTER TABLE `kho_thanhpham_san_xuat` DISABLE KEYS */;
INSERT INTO `kho_thanhpham_san_xuat` VALUES (1,3,1,0,'2026-10-21 17:43:47');
/*!40000 ALTER TABLE `kho_thanhpham_san_xuat` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=58 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lich_su_kho`
--

LOCK TABLES `lich_su_kho` WRITE;
/*!40000 ALTER TABLE `lich_su_kho` DISABLE KEYS */;
INSERT INTO `lich_su_kho` VALUES (1,'NVL',1,'NHAP_NVL',100.00,200000.00,'2026-10-20 17:02:16',9,'phieunhap_nvl',1,NULL),(2,'NVL',2,'NHAP_NVL',100.00,150000.00,'2026-10-20 17:02:16',9,'phieunhap_nvl',1,NULL),(3,'NVL',3,'NHAP_NVL',100.00,200000.00,'2026-10-20 17:02:16',9,'phieunhap_nvl',1,NULL),(4,'NVL',4,'NHAP_NVL',100.00,200000.00,'2026-10-20 17:02:16',9,'phieunhap_nvl',1,NULL),(5,'NVL',5,'NHAP_NVL',100.00,500000.00,'2026-10-20 17:02:16',9,'phieunhap_nvl',1,NULL),(6,'NVL',6,'NHAP_NVL',100.00,800000.00,'2026-10-20 17:02:16',9,'phieunhap_nvl',1,NULL),(7,'NVL',7,'NHAP_NVL',100.00,500000.00,'2026-10-20 17:02:16',9,'phieunhap_nvl',1,NULL),(8,'NVL',8,'NHAP_NVL',100.00,3000000.00,'2026-10-20 17:02:16',9,'phieunhap_nvl',1,NULL),(9,'NVL',9,'NHAP_NVL',100.00,4000000.00,'2026-10-20 17:02:16',9,'phieunhap_nvl',1,NULL),(10,'NVL',10,'NHAP_NVL',100.00,200000.00,'2026-10-20 17:02:16',9,'phieunhap_nvl',1,NULL),(11,'NVL',11,'NHAP_NVL',100.00,300000.00,'2026-10-20 17:02:16',9,'phieunhap_nvl',1,NULL),(12,'NVL',12,'NHAP_NVL',100.00,700000.00,'2026-10-20 17:02:16',9,'phieunhap_nvl',1,NULL),(13,'NVL',13,'NHAP_NVL',100.00,2000000.00,'2026-10-20 17:02:16',9,'phieunhap_nvl',1,NULL),(14,'NVL',14,'NHAP_NVL',100.00,1000000.00,'2026-10-20 17:02:16',9,'phieunhap_nvl',1,NULL),(15,'NVL',15,'NHAP_NVL',100.00,5000000.00,'2026-10-20 17:02:16',9,'phieunhap_nvl',1,NULL),(16,'NVL',16,'NHAP_NVL',100.00,500000.00,'2026-10-20 17:02:16',9,'phieunhap_nvl',1,NULL),(17,'NVL',17,'NHAP_NVL',100.00,1000000.00,'2026-10-20 17:02:16',9,'phieunhap_nvl',1,NULL),(18,'NVL',18,'NHAP_NVL',100.00,5000000.00,'2026-10-20 17:02:16',9,'phieunhap_nvl',1,NULL),(19,'NVL',19,'NHAP_NVL',100.00,5000000.00,'2026-10-20 17:02:16',9,'phieunhap_nvl',1,NULL),(20,'NVL',20,'NHAP_NVL',100.00,5000000.00,'2026-10-20 17:02:16',9,'phieunhap_nvl',1,NULL),(21,'NVL',21,'NHAP_NVL',100.00,4000000.00,'2026-10-20 17:02:16',9,'phieunhap_nvl',1,NULL),(22,'NVL',22,'NHAP_NVL',100.00,1000000.00,'2026-10-20 17:02:16',9,'phieunhap_nvl',1,NULL),(23,'NVL',23,'NHAP_NVL',100.00,700000.00,'2026-10-20 17:02:16',9,'phieunhap_nvl',1,NULL),(24,'NVL',24,'NHAP_NVL',100.00,1000000.00,'2026-10-20 17:02:16',9,'phieunhap_nvl',1,NULL),(25,'NVL',25,'NHAP_NVL',100.00,600000.00,'2026-10-20 17:02:16',9,'phieunhap_nvl',1,NULL),(26,'NVL',27,'NHAP_NVL',300.00,2000000.00,'2026-10-21 14:33:03',9,'phieunhap_nvl',2,NULL),(27,'NVL',1,'XUAT_NVL',10.00,0.00,'2026-10-21 14:34:00',9,'phieuxuat_nvl',1,NULL),(28,'NVL',3,'XUAT_NVL',10.00,0.00,'2026-10-21 14:34:00',9,'phieuxuat_nvl',1,NULL),(29,'NVL',6,'XUAT_NVL',10.00,0.00,'2026-10-21 14:34:00',9,'phieuxuat_nvl',1,NULL),(30,'NVL',8,'XUAT_NVL',10.00,0.00,'2026-10-21 14:34:00',9,'phieuxuat_nvl',1,NULL),(31,'NVL',10,'XUAT_NVL',10.00,0.00,'2026-10-21 14:34:00',9,'phieuxuat_nvl',1,NULL),(32,'NVL',13,'XUAT_NVL',10.00,0.00,'2026-10-21 14:34:00',9,'phieuxuat_nvl',1,NULL),(33,'NVL',14,'XUAT_NVL',10.00,0.00,'2026-10-21 14:34:00',9,'phieuxuat_nvl',1,NULL),(34,'NVL',16,'XUAT_NVL',10.00,0.00,'2026-10-21 14:34:00',9,'phieuxuat_nvl',1,NULL),(35,'NVL',19,'XUAT_NVL',10.00,0.00,'2026-10-21 14:34:00',9,'phieuxuat_nvl',1,NULL),(36,'NVL',20,'XUAT_NVL',10.00,0.00,'2026-10-21 14:34:00',9,'phieuxuat_nvl',1,NULL),(37,'NVL',22,'XUAT_NVL',10.00,0.00,'2026-10-21 14:34:00',9,'phieuxuat_nvl',1,NULL),(38,'NVL',25,'XUAT_NVL',10.00,0.00,'2026-10-21 14:34:00',9,'phieuxuat_nvl',1,NULL),(39,'NVL',27,'XUAT_NVL',10.00,0.00,'2026-10-21 14:34:00',9,'phieuxuat_nvl',1,NULL),(40,'THANH_PHAM',3,'NHAP_THANH_PHAM',5.00,0.00,'2026-10-21 14:35:31',9,'ketqua_sanxuat',1,NULL),(41,'THANH_PHAM',3,'NHAP_THANH_PHAM',5.00,0.00,'2026-10-21 16:30:37',9,'ketqua_sanxuat',2,NULL),(42,'NVL',1,'XUAT_NVL',2.00,0.00,'2026-10-21 16:50:37',9,'phieuxuat_nvl',2,NULL),(43,'NVL',3,'XUAT_NVL',2.00,0.00,'2026-10-21 16:50:37',9,'phieuxuat_nvl',2,NULL),(44,'NVL',6,'XUAT_NVL',2.00,0.00,'2026-10-21 16:50:37',9,'phieuxuat_nvl',2,NULL),(45,'NVL',8,'XUAT_NVL',2.00,0.00,'2026-10-21 16:50:37',9,'phieuxuat_nvl',2,NULL),(46,'NVL',10,'XUAT_NVL',2.00,0.00,'2026-10-21 16:50:37',9,'phieuxuat_nvl',2,NULL),(47,'NVL',13,'XUAT_NVL',2.00,0.00,'2026-10-21 16:50:37',9,'phieuxuat_nvl',2,NULL),(48,'NVL',14,'XUAT_NVL',2.00,0.00,'2026-10-21 16:50:37',9,'phieuxuat_nvl',2,NULL),(49,'NVL',16,'XUAT_NVL',2.00,0.00,'2026-10-21 16:50:37',9,'phieuxuat_nvl',2,NULL),(50,'NVL',19,'XUAT_NVL',2.00,0.00,'2026-10-21 16:50:37',9,'phieuxuat_nvl',2,NULL),(51,'NVL',20,'XUAT_NVL',2.00,0.00,'2026-10-21 16:50:37',9,'phieuxuat_nvl',2,NULL),(52,'NVL',22,'XUAT_NVL',2.00,0.00,'2026-10-21 16:50:37',9,'phieuxuat_nvl',2,NULL),(53,'NVL',25,'XUAT_NVL',2.00,0.00,'2026-10-21 16:50:37',9,'phieuxuat_nvl',2,NULL),(54,'NVL',27,'XUAT_NVL',2.00,0.00,'2026-10-21 16:50:37',9,'phieuxuat_nvl',2,NULL),(55,'NVL',10,'XUAT_NVL',1.00,0.00,'2026-10-21 17:11:12',9,'phieuxuat_nvl',3,NULL),(56,'NVL',1,'XUAT_NVL',1.00,0.00,'2026-10-21 17:40:47',9,'phieuxuat_nvl',5,NULL),(57,'THANH_PHAM',3,'NHAP_THANH_PHAM',2.00,0.00,'2026-10-21 17:43:47',9,'ketqua_sanxuat',3,NULL);
/*!40000 ALTER TABLE `lich_su_kho` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lich_su_kho_loi_san_xuat`
--

DROP TABLE IF EXISTS `lich_su_kho_loi_san_xuat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lich_su_kho_loi_san_xuat` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `makqsx` bigint NOT NULL,
  `masp` int NOT NULL,
  `mabom` int NOT NULL,
  `soluong` int NOT NULL,
  `ton_truoc` int NOT NULL,
  `ton_sau` int NOT NULL,
  `lydo` varchar(500) NOT NULL,
  `nguoi_thuchien` int DEFAULT NULL,
  `thoi_gian` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_lskl_makqsx` (`makqsx`),
  KEY `idx_lskl_sp_bom` (`masp`,`mabom`),
  KEY `idx_lskl_thoigian` (`thoi_gian`),
  KEY `fk_lskl_bom` (`mabom`),
  KEY `fk_lskl_nhanvien` (`nguoi_thuchien`),
  CONSTRAINT `fk_lskl_bom` FOREIGN KEY (`mabom`) REFERENCES `bom` (`mabom`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_lskl_kqsx` FOREIGN KEY (`makqsx`) REFERENCES `ketqua_sanxuat` (`makqsx`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_lskl_nhanvien` FOREIGN KEY (`nguoi_thuchien`) REFERENCES `nhanvien` (`manv`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_lskl_sp` FOREIGN KEY (`masp`) REFERENCES `sanpham` (`masp`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `ck_lskl_soluong` CHECK ((`soluong` > 0)),
  CONSTRAINT `ck_lskl_ton` CHECK (((`ton_truoc` >= 0) and (`ton_sau` >= 0)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lich_su_kho_loi_san_xuat`
--

LOCK TABLES `lich_su_kho_loi_san_xuat` WRITE;
/*!40000 ALTER TABLE `lich_su_kho_loi_san_xuat` DISABLE KEYS */;
/*!40000 ALTER TABLE `lich_su_kho_loi_san_xuat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lich_su_kho_san_xuat`
--

DROP TABLE IF EXISTS `lich_su_kho_san_xuat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lich_su_kho_san_xuat` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `loai_kho` varchar(20) NOT NULL,
  `loai_hang` varchar(20) NOT NULL,
  `ma_hang` int NOT NULL,
  `loai_giao_dich` varchar(40) NOT NULL,
  `soluong` decimal(18,2) NOT NULL,
  `ton_truoc` decimal(18,2) NOT NULL DEFAULT '0.00',
  `ton_sau` decimal(18,2) NOT NULL DEFAULT '0.00',
  `nguoi_thuchien` int DEFAULT NULL,
  `thoi_gian` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `bang_tham_chieu` varchar(50) DEFAULT NULL,
  `ma_tham_chieu` bigint DEFAULT NULL,
  `ghichu` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_lskxs_thoigian` (`thoi_gian`),
  KEY `idx_lskxs_hang` (`loai_hang`,`ma_hang`),
  KEY `idx_lskxs_kho` (`loai_kho`),
  KEY `idx_lskxs_giaodich` (`loai_giao_dich`),
  KEY `fk_lskxs_nhanvien` (`nguoi_thuchien`),
  CONSTRAINT `fk_lskxs_nhanvien` FOREIGN KEY (`nguoi_thuchien`) REFERENCES `nhanvien` (`manv`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `ck_lskxs_loai_hang` CHECK ((`loai_hang` in (_utf8mb4'NVL',_utf8mb4'THANH_PHAM'))),
  CONSTRAINT `ck_lskxs_loai_kho` CHECK ((`loai_kho` in (_utf8mb4'NVL_SAN_XUAT',_utf8mb4'THANH_PHAM_SAN_XUAT'))),
  CONSTRAINT `ck_lskxs_soluong` CHECK ((`soluong` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=76 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lich_su_kho_san_xuat`
--

LOCK TABLES `lich_su_kho_san_xuat` WRITE;
/*!40000 ALTER TABLE `lich_su_kho_san_xuat` DISABLE KEYS */;
INSERT INTO `lich_su_kho_san_xuat` VALUES (1,'NVL_SAN_XUAT','NVL',1,'NHAP_NVL_SAN_XUAT',10.00,0.00,10.00,9,'2026-10-21 14:34:00','phieuxuat_nvl',1,NULL),(2,'NVL_SAN_XUAT','NVL',3,'NHAP_NVL_SAN_XUAT',10.00,0.00,10.00,9,'2026-10-21 14:34:00','phieuxuat_nvl',1,NULL),(3,'NVL_SAN_XUAT','NVL',6,'NHAP_NVL_SAN_XUAT',10.00,0.00,10.00,9,'2026-10-21 14:34:00','phieuxuat_nvl',1,NULL),(4,'NVL_SAN_XUAT','NVL',8,'NHAP_NVL_SAN_XUAT',10.00,0.00,10.00,9,'2026-10-21 14:34:00','phieuxuat_nvl',1,NULL),(5,'NVL_SAN_XUAT','NVL',10,'NHAP_NVL_SAN_XUAT',10.00,0.00,10.00,9,'2026-10-21 14:34:00','phieuxuat_nvl',1,NULL),(6,'NVL_SAN_XUAT','NVL',13,'NHAP_NVL_SAN_XUAT',10.00,0.00,10.00,9,'2026-10-21 14:34:00','phieuxuat_nvl',1,NULL),(7,'NVL_SAN_XUAT','NVL',14,'NHAP_NVL_SAN_XUAT',10.00,0.00,10.00,9,'2026-10-21 14:34:00','phieuxuat_nvl',1,NULL),(8,'NVL_SAN_XUAT','NVL',16,'NHAP_NVL_SAN_XUAT',10.00,0.00,10.00,9,'2026-10-21 14:34:00','phieuxuat_nvl',1,NULL),(9,'NVL_SAN_XUAT','NVL',19,'NHAP_NVL_SAN_XUAT',10.00,0.00,10.00,9,'2026-10-21 14:34:00','phieuxuat_nvl',1,NULL),(10,'NVL_SAN_XUAT','NVL',20,'NHAP_NVL_SAN_XUAT',10.00,0.00,10.00,9,'2026-10-21 14:34:00','phieuxuat_nvl',1,NULL),(11,'NVL_SAN_XUAT','NVL',22,'NHAP_NVL_SAN_XUAT',10.00,0.00,10.00,9,'2026-10-21 14:34:00','phieuxuat_nvl',1,NULL),(12,'NVL_SAN_XUAT','NVL',25,'NHAP_NVL_SAN_XUAT',10.00,0.00,10.00,9,'2026-10-21 14:34:00','phieuxuat_nvl',1,NULL),(13,'NVL_SAN_XUAT','NVL',27,'NHAP_NVL_SAN_XUAT',10.00,0.00,10.00,9,'2026-10-21 14:34:00','phieuxuat_nvl',1,NULL),(14,'NVL_SAN_XUAT','NVL',1,'XUAT_NVL_SAN_XUAT',5.00,10.00,5.00,10,'2026-10-21 14:34:36','ghi_nhan_san_xuat',1,NULL),(15,'NVL_SAN_XUAT','NVL',3,'XUAT_NVL_SAN_XUAT',5.00,10.00,5.00,10,'2026-10-21 14:34:36','ghi_nhan_san_xuat',1,NULL),(16,'NVL_SAN_XUAT','NVL',6,'XUAT_NVL_SAN_XUAT',5.00,10.00,5.00,10,'2026-10-21 14:34:36','ghi_nhan_san_xuat',1,NULL),(17,'NVL_SAN_XUAT','NVL',8,'XUAT_NVL_SAN_XUAT',5.00,10.00,5.00,10,'2026-10-21 14:34:36','ghi_nhan_san_xuat',1,NULL),(18,'NVL_SAN_XUAT','NVL',10,'XUAT_NVL_SAN_XUAT',5.00,10.00,5.00,10,'2026-10-21 14:34:36','ghi_nhan_san_xuat',1,NULL),(19,'NVL_SAN_XUAT','NVL',13,'XUAT_NVL_SAN_XUAT',5.00,10.00,5.00,10,'2026-10-21 14:34:36','ghi_nhan_san_xuat',1,NULL),(20,'NVL_SAN_XUAT','NVL',14,'XUAT_NVL_SAN_XUAT',5.00,10.00,5.00,10,'2026-10-21 14:34:36','ghi_nhan_san_xuat',1,NULL),(21,'NVL_SAN_XUAT','NVL',16,'XUAT_NVL_SAN_XUAT',5.00,10.00,5.00,10,'2026-10-21 14:34:36','ghi_nhan_san_xuat',1,NULL),(22,'NVL_SAN_XUAT','NVL',19,'XUAT_NVL_SAN_XUAT',5.00,10.00,5.00,10,'2026-10-21 14:34:36','ghi_nhan_san_xuat',1,NULL),(23,'NVL_SAN_XUAT','NVL',20,'XUAT_NVL_SAN_XUAT',5.00,10.00,5.00,10,'2026-10-21 14:34:36','ghi_nhan_san_xuat',1,NULL),(24,'NVL_SAN_XUAT','NVL',22,'XUAT_NVL_SAN_XUAT',5.00,10.00,5.00,10,'2026-10-21 14:34:36','ghi_nhan_san_xuat',1,NULL),(25,'NVL_SAN_XUAT','NVL',25,'XUAT_NVL_SAN_XUAT',5.00,10.00,5.00,10,'2026-10-21 14:34:36','ghi_nhan_san_xuat',1,NULL),(26,'NVL_SAN_XUAT','NVL',27,'XUAT_NVL_SAN_XUAT',5.00,10.00,5.00,10,'2026-10-21 14:34:36','ghi_nhan_san_xuat',1,NULL),(27,'THANH_PHAM_SAN_XUAT','THANH_PHAM',3,'NHAP_THANH_PHAM_SAN_XUAT',5.00,0.00,5.00,10,'2026-10-21 14:34:36','ghi_nhan_san_xuat',1,NULL),(28,'THANH_PHAM_SAN_XUAT','THANH_PHAM',3,'XUAT_THANH_PHAM_SAN_XUAT',5.00,5.00,0.00,9,'2026-10-21 14:35:31','ketqua_sanxuat',1,NULL),(29,'NVL_SAN_XUAT','NVL',1,'XUAT_NVL_SAN_XUAT',5.00,5.00,0.00,10,'2026-10-21 16:30:07','ghi_nhan_san_xuat',2,NULL),(30,'NVL_SAN_XUAT','NVL',3,'XUAT_NVL_SAN_XUAT',5.00,5.00,0.00,10,'2026-10-21 16:30:07','ghi_nhan_san_xuat',2,NULL),(31,'NVL_SAN_XUAT','NVL',6,'XUAT_NVL_SAN_XUAT',5.00,5.00,0.00,10,'2026-10-21 16:30:07','ghi_nhan_san_xuat',2,NULL),(32,'NVL_SAN_XUAT','NVL',8,'XUAT_NVL_SAN_XUAT',5.00,5.00,0.00,10,'2026-10-21 16:30:07','ghi_nhan_san_xuat',2,NULL),(33,'NVL_SAN_XUAT','NVL',10,'XUAT_NVL_SAN_XUAT',5.00,5.00,0.00,10,'2026-10-21 16:30:07','ghi_nhan_san_xuat',2,NULL),(34,'NVL_SAN_XUAT','NVL',13,'XUAT_NVL_SAN_XUAT',5.00,5.00,0.00,10,'2026-10-21 16:30:07','ghi_nhan_san_xuat',2,NULL),(35,'NVL_SAN_XUAT','NVL',14,'XUAT_NVL_SAN_XUAT',5.00,5.00,0.00,10,'2026-10-21 16:30:07','ghi_nhan_san_xuat',2,NULL),(36,'NVL_SAN_XUAT','NVL',16,'XUAT_NVL_SAN_XUAT',5.00,5.00,0.00,10,'2026-10-21 16:30:07','ghi_nhan_san_xuat',2,NULL),(37,'NVL_SAN_XUAT','NVL',19,'XUAT_NVL_SAN_XUAT',5.00,5.00,0.00,10,'2026-10-21 16:30:07','ghi_nhan_san_xuat',2,NULL),(38,'NVL_SAN_XUAT','NVL',20,'XUAT_NVL_SAN_XUAT',5.00,5.00,0.00,10,'2026-10-21 16:30:07','ghi_nhan_san_xuat',2,NULL),(39,'NVL_SAN_XUAT','NVL',22,'XUAT_NVL_SAN_XUAT',5.00,5.00,0.00,10,'2026-10-21 16:30:07','ghi_nhan_san_xuat',2,NULL),(40,'NVL_SAN_XUAT','NVL',25,'XUAT_NVL_SAN_XUAT',5.00,5.00,0.00,10,'2026-10-21 16:30:07','ghi_nhan_san_xuat',2,NULL),(41,'NVL_SAN_XUAT','NVL',27,'XUAT_NVL_SAN_XUAT',5.00,5.00,0.00,10,'2026-10-21 16:30:07','ghi_nhan_san_xuat',2,NULL),(42,'THANH_PHAM_SAN_XUAT','THANH_PHAM',3,'NHAP_THANH_PHAM_SAN_XUAT',5.00,0.00,5.00,10,'2026-10-21 16:30:07','ghi_nhan_san_xuat',2,NULL),(43,'THANH_PHAM_SAN_XUAT','THANH_PHAM',3,'XUAT_THANH_PHAM_SAN_XUAT',5.00,5.00,0.00,9,'2026-10-21 16:30:37','ketqua_sanxuat',2,NULL),(44,'NVL_SAN_XUAT','NVL',1,'NHAP_NVL_SAN_XUAT',2.00,0.00,2.00,9,'2026-10-21 16:50:37','phieuxuat_nvl',2,NULL),(45,'NVL_SAN_XUAT','NVL',3,'NHAP_NVL_SAN_XUAT',2.00,0.00,2.00,9,'2026-10-21 16:50:37','phieuxuat_nvl',2,NULL),(46,'NVL_SAN_XUAT','NVL',6,'NHAP_NVL_SAN_XUAT',2.00,0.00,2.00,9,'2026-10-21 16:50:37','phieuxuat_nvl',2,NULL),(47,'NVL_SAN_XUAT','NVL',8,'NHAP_NVL_SAN_XUAT',2.00,0.00,2.00,9,'2026-10-21 16:50:37','phieuxuat_nvl',2,NULL),(48,'NVL_SAN_XUAT','NVL',10,'NHAP_NVL_SAN_XUAT',2.00,0.00,2.00,9,'2026-10-21 16:50:37','phieuxuat_nvl',2,NULL),(49,'NVL_SAN_XUAT','NVL',13,'NHAP_NVL_SAN_XUAT',2.00,0.00,2.00,9,'2026-10-21 16:50:37','phieuxuat_nvl',2,NULL),(50,'NVL_SAN_XUAT','NVL',14,'NHAP_NVL_SAN_XUAT',2.00,0.00,2.00,9,'2026-10-21 16:50:37','phieuxuat_nvl',2,NULL),(51,'NVL_SAN_XUAT','NVL',16,'NHAP_NVL_SAN_XUAT',2.00,0.00,2.00,9,'2026-10-21 16:50:37','phieuxuat_nvl',2,NULL),(52,'NVL_SAN_XUAT','NVL',19,'NHAP_NVL_SAN_XUAT',2.00,0.00,2.00,9,'2026-10-21 16:50:37','phieuxuat_nvl',2,NULL),(53,'NVL_SAN_XUAT','NVL',20,'NHAP_NVL_SAN_XUAT',2.00,0.00,2.00,9,'2026-10-21 16:50:37','phieuxuat_nvl',2,NULL),(54,'NVL_SAN_XUAT','NVL',22,'NHAP_NVL_SAN_XUAT',2.00,0.00,2.00,9,'2026-10-21 16:50:37','phieuxuat_nvl',2,NULL),(55,'NVL_SAN_XUAT','NVL',25,'NHAP_NVL_SAN_XUAT',2.00,0.00,2.00,9,'2026-10-21 16:50:37','phieuxuat_nvl',2,NULL),(56,'NVL_SAN_XUAT','NVL',27,'NHAP_NVL_SAN_XUAT',2.00,0.00,2.00,9,'2026-10-21 16:50:37','phieuxuat_nvl',2,NULL),(57,'NVL_SAN_XUAT','NVL',10,'NHAP_NVL_SAN_XUAT',1.00,2.00,3.00,9,'2026-10-21 17:11:12','phieuxuat_nvl',3,NULL),(58,'NVL_SAN_XUAT','NVL',1,'XUAT_NVL_SAN_XUAT',2.00,2.00,0.00,10,'2026-10-21 17:11:34','ghi_nhan_san_xuat',3,NULL),(59,'NVL_SAN_XUAT','NVL',3,'XUAT_NVL_SAN_XUAT',2.00,2.00,0.00,10,'2026-10-21 17:11:34','ghi_nhan_san_xuat',3,NULL),(60,'NVL_SAN_XUAT','NVL',6,'XUAT_NVL_SAN_XUAT',2.00,2.00,0.00,10,'2026-10-21 17:11:34','ghi_nhan_san_xuat',3,NULL),(61,'NVL_SAN_XUAT','NVL',8,'XUAT_NVL_SAN_XUAT',2.00,2.00,0.00,10,'2026-10-21 17:11:34','ghi_nhan_san_xuat',3,NULL),(62,'NVL_SAN_XUAT','NVL',10,'XUAT_NVL_SAN_XUAT',2.00,3.00,1.00,10,'2026-10-21 17:11:34','ghi_nhan_san_xuat',3,NULL),(63,'NVL_SAN_XUAT','NVL',13,'XUAT_NVL_SAN_XUAT',2.00,2.00,0.00,10,'2026-10-21 17:11:34','ghi_nhan_san_xuat',3,NULL),(64,'NVL_SAN_XUAT','NVL',14,'XUAT_NVL_SAN_XUAT',2.00,2.00,0.00,10,'2026-10-21 17:11:34','ghi_nhan_san_xuat',3,NULL),(65,'NVL_SAN_XUAT','NVL',16,'XUAT_NVL_SAN_XUAT',2.00,2.00,0.00,10,'2026-10-21 17:11:34','ghi_nhan_san_xuat',3,NULL),(66,'NVL_SAN_XUAT','NVL',19,'XUAT_NVL_SAN_XUAT',2.00,2.00,0.00,10,'2026-10-21 17:11:34','ghi_nhan_san_xuat',3,NULL),(67,'NVL_SAN_XUAT','NVL',20,'XUAT_NVL_SAN_XUAT',2.00,2.00,0.00,10,'2026-10-21 17:11:34','ghi_nhan_san_xuat',3,NULL),(68,'NVL_SAN_XUAT','NVL',22,'XUAT_NVL_SAN_XUAT',2.00,2.00,0.00,10,'2026-10-21 17:11:34','ghi_nhan_san_xuat',3,NULL),(69,'NVL_SAN_XUAT','NVL',25,'XUAT_NVL_SAN_XUAT',2.00,2.00,0.00,10,'2026-10-21 17:11:34','ghi_nhan_san_xuat',3,NULL),(70,'NVL_SAN_XUAT','NVL',27,'XUAT_NVL_SAN_XUAT',2.00,2.00,0.00,10,'2026-10-21 17:11:34','ghi_nhan_san_xuat',3,NULL),(71,'THANH_PHAM_SAN_XUAT','THANH_PHAM',3,'NHAP_THANH_PHAM_SAN_XUAT',2.00,0.00,2.00,10,'2026-10-21 17:11:34','ghi_nhan_san_xuat',3,NULL),(72,'NVL_SAN_XUAT','NVL',1,'NHAP_NVL_SAN_XUAT',1.00,0.00,1.00,9,'2026-10-21 17:40:47','phieuxuat_nvl',5,NULL),(73,'NVL_SAN_XUAT','NVL',1,'XUAT_NVL_SAN_XUAT',1.00,1.00,0.00,9,'2026-10-21 17:40:47','phieuxuat_nvl',5,NULL),(74,'THANH_PHAM_SAN_XUAT','THANH_PHAM',3,'XUAT_THANH_PHAM_SAN_XUAT',2.00,2.00,0.00,9,'2026-10-21 17:43:47','ketqua_sanxuat',3,NULL),(75,'NVL_SAN_XUAT','NVL',10,'XUAT_NVL_SAN_XUAT',1.00,1.00,0.00,9,'2026-10-21 17:47:08','phieuxuat_nvl',3,'Xuat huy can tru linh kien hong theo phieu xuat bu #3');
/*!40000 ALTER TABLE `lich_su_kho_san_xuat` ENABLE KEYS */;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lichsu_dieuchinh_yeucau`
--

LOCK TABLES `lichsu_dieuchinh_yeucau` WRITE;
/*!40000 ALTER TABLE `lichsu_dieuchinh_yeucau` DISABLE KEYS */;
/*!40000 ALTER TABLE `lichsu_dieuchinh_yeucau` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `loai_nguyen_vat_lieu`
--

DROP TABLE IF EXISTS `loai_nguyen_vat_lieu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `loai_nguyen_vat_lieu` (
  `maloai` int NOT NULL AUTO_INCREMENT,
  `tenloai` varchar(255) NOT NULL,
  `trangthai` tinyint NOT NULL DEFAULT '1',
  PRIMARY KEY (`maloai`),
  UNIQUE KEY `uk_loai_nvl_ten` (`tenloai`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `loai_nguyen_vat_lieu`
--

LOCK TABLES `loai_nguyen_vat_lieu` WRITE;
/*!40000 ALTER TABLE `loai_nguyen_vat_lieu` DISABLE KEYS */;
INSERT INTO `loai_nguyen_vat_lieu` VALUES (1,'Pin',1),(2,'Vỏ máy',1),(3,'Màn hình',1),(4,'Bàn phím',1),(5,'CPU',1),(6,'Touchpad',1),(7,'Mainboard',1),(8,'RAM',1),(9,'Ổ cứng',1),(10,'GPU',1),(11,'Tản nhiệt',1),(12,'Card mạng',1),(13,'Loa',1);
/*!40000 ALTER TABLE `loai_nguyen_vat_lieu` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ncc`
--

LOCK TABLES `ncc` WRITE;
/*!40000 ALTER TABLE `ncc` DISABLE KEYS */;
INSERT INTO `ncc` VALUES (1,'Điện máy xanh','Go vap','021265365',NULL,0,NULL),(2,'Phong Vũ','quận 5','0115102619',NULL,0,NULL);
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
  `maloai` int DEFAULT NULL,
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
  KEY `fk_nvl_loai` (`maloai`),
  CONSTRAINT `fk_nvl_loai` FOREIGN KEY (`maloai`) REFERENCES `loai_nguyen_vat_lieu` (`maloai`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `ck_nvl_leadtime` CHECK ((`lead_time_ngay` >= 0)),
  CONSTRAINT `ck_nvl_ton` CHECK ((`soluongton` >= 0)),
  CONSTRAINT `ck_nvl_ton_toithieu` CHECK ((`ton_toithieu` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nguyenvatlieu`
--

LOCK TABLES `nguyenvatlieu` WRITE;
/*!40000 ALTER TABLE `nguyenvatlieu` DISABLE KEYS */;
INSERT INTO `nguyenvatlieu` VALUES (1,4,'NVL-BAN-PHIM-001','Bàn phím led','cái',200000.00,87.00,5.00,0,1,''),(2,4,'NVL-BAN-PHIM-002','Bàn phím','cái',150000.00,100.00,5.00,0,1,''),(3,12,'NVL-CARD-MANG-001','Card mạng không dây','cái',200000.00,88.00,0.00,0,1,''),(4,12,'NVL-CARD-MANG-002','Card mạng có dây','cái',200000.00,100.00,0.00,0,1,''),(5,5,'NVL-CPU-001','CPU Intel Core i5 12th','cái',500000.00,100.00,0.00,0,1,''),(6,5,'NVL-CPU-002','CPU Intel Core i7 5th','cái',800000.00,88.00,0.00,0,1,''),(7,5,'NVL-CPU-003','AMD Ryzen 3 7320U','cái',500000.00,100.00,0.00,0,1,''),(8,10,'NVL-GPU-001','NVIDIA GeForce RTX 4050','cái',3000000.00,88.00,0.00,0,1,''),(9,10,'NVL-GPU-002','AMD Radeon RX 7600M','cái',4000000.00,100.00,0.00,0,1,''),(10,13,'NVL-LOA-001','Logitech Z313','cái',200000.00,87.00,0.00,0,1,''),(11,13,'NVL-LOA-002','Microlab B26','cái',300000.00,100.00,0.00,0,1,''),(12,7,'NVL-MAINBOARD-001','Mainboard Dell Inspiron 15 3511','cái',700000.00,100.00,0.00,0,1,''),(13,7,'NVL-MAINBOARD-002','Mainboard Asus TUF Gaming F15 (FX506)','cái',2000000.00,88.00,0.00,0,1,''),(14,3,'NVL-MAN-HINH-001','14.5 inch','cái',1000000.00,88.00,0.00,0,1,''),(15,3,'NVL-MAN-HINH-002','IPS PureSight Pro 120Hz','cái',5000000.00,100.00,0.00,0,1,''),(16,9,'NVL-O-CUNG-001','HDD 2.5 inch SATA','cái',500000.00,88.00,0.00,0,1,''),(17,9,'NVL-O-CUNG-002','SSD M.2 SATA','cái',1000000.00,100.00,0.00,0,1,''),(18,1,'NVL-PIN-001','Pin Lithium-ion','cái',5000000.00,100.00,0.00,0,1,''),(19,1,'NVL-PIN-002','Lithium-Polymer','cái',5000000.00,88.00,0.00,0,1,''),(20,8,'NVL-RAM-001','Kingston 8GB DDR4 3200MHz SO-DIMM','cái',5000000.00,88.00,0.00,0,1,''),(21,8,'NVL-RAM-002','Kingston Fury Impact 16GB DDR5 4800MHz SO-DIMM','cái',4000000.00,100.00,0.00,0,1,''),(22,11,'NVL-TAN-NHIET-001','Cooler Master L1','cái',1000000.00,88.00,0.00,0,1,''),(23,11,'NVL-TAN-NHIET-002','Deepcool M3','cái',700000.00,100.00,0.00,0,1,''),(24,6,'NVL-TOUCHPAD-001','Microsoft Precision Touchpad','cái',1000000.00,100.00,0.00,0,1,''),(25,6,'NVL-TOUCHPAD-002','ClickPad','cái',600000.00,88.00,0.00,0,1,''),(26,2,'NVL-VO-MAY-001','Vỏ lenovo','bộ',3000000.00,0.00,0.00,0,1,''),(27,2,'NVL-VO-MAY-002','Vỏ Asus','bộ',2000000.00,288.00,0.00,0,1,'');
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
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nhanvien`
--

LOCK TABLES `nhanvien` WRITE;
/*!40000 ALTER TABLE `nhanvien` DISABLE KEYS */;
INSERT INTO `nhanvien` VALUES (1,'Nguyễn Văn Quản Lý','Nam','1995-01-09 17:00:00.000000','0900000001','quanly@lap123.local','2025-12-31 17:00:00.000000',1,'gà'),(2,'Trần Văn Nhân Viên','Khác','2003-05-19 17:00:00.000000','0900000002','nhanvien@lap123.local','2025-12-31 17:00:00.000000',1,'f'),(6,'Nguyễn Văn Anh','Nam','2112-12-11 17:00:00.000000','12321434',NULL,'2321-12-11 17:00:00.000000',0,''),(7,'Phan Văn Tre','Nữ','2012-12-20 17:00:00.000000','12121212',NULL,'2032-12-11 17:00:00.000000',1,'gà'),(8,'Lê Tra','Nữ','2021-12-30 17:00:00.000000','12313',NULL,'2002-12-10 17:00:00.000000',1,'k'),(9,'Quản trị viên a','Nam',NULL,'',NULL,NULL,0,NULL),(10,'Nguyễn Văn Nhật','Nam','2012-02-05 17:00:00.000000','012345689','','2023-12-30 17:00:00.000000',0,'quá gà'),(11,'Lê Minh ','Nam','2005-02-19 17:00:00.000000','12321455346','aaaad@gmail.com','2026-10-10 17:00:00.000000',0,NULL);
/*!40000 ALTER TABLE `nhanvien` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `phieunhap`
--

DROP TABLE IF EXISTS `phieunhap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `phieunhap` (
  `maphieunhap` int NOT NULL AUTO_INCREMENT,
  `thoigian` date DEFAULT NULL,
  `tongtien` decimal(38,2) DEFAULT NULL,
  `trangthai` int DEFAULT NULL,
  `mancc` int DEFAULT NULL,
  `manv` int DEFAULT NULL,
  PRIMARY KEY (`maphieunhap`),
  KEY `FKqk7uxuhmox5x8hh2mpt9t3wnf` (`mancc`),
  KEY `FKkedc0d3kfjdphsqb31rkvjebw` (`manv`),
  CONSTRAINT `FKkedc0d3kfjdphsqb31rkvjebw` FOREIGN KEY (`manv`) REFERENCES `nhanvien` (`manv`),
  CONSTRAINT `FKqk7uxuhmox5x8hh2mpt9t3wnf` FOREIGN KEY (`mancc`) REFERENCES `ncc` (`mancc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieunhap`
--

LOCK TABLES `phieunhap` WRITE;
/*!40000 ALTER TABLE `phieunhap` DISABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieunhap_nvl`
--

LOCK TABLES `phieunhap_nvl` WRITE;
/*!40000 ALTER TABLE `phieunhap_nvl` DISABLE KEYS */;
INSERT INTO `phieunhap_nvl` VALUES (1,1,9,'2026-10-20 17:01:27',4255000000.00,'DA_NHAP','                            '),(2,1,9,'2026-10-21 14:33:00',600000000.00,'DA_NHAP','                            ');
/*!40000 ALTER TABLE `phieunhap_nvl` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `phieuxuat`
--

DROP TABLE IF EXISTS `phieuxuat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `phieuxuat` (
  `maphieuxuat` int NOT NULL AUTO_INCREMENT,
  `thoigian` date DEFAULT NULL,
  `tongtien` decimal(38,2) DEFAULT NULL,
  `trangthai` int DEFAULT NULL,
  `makh` int DEFAULT NULL,
  `manv` int DEFAULT NULL,
  PRIMARY KEY (`maphieuxuat`),
  KEY `FKjd6uefgt2bd9d4mtmqk74n028` (`makh`),
  KEY `FKf0xueec76ekuc457mjng6ll6h` (`manv`),
  CONSTRAINT `FKf0xueec76ekuc457mjng6ll6h` FOREIGN KEY (`manv`) REFERENCES `nhanvien` (`manv`),
  CONSTRAINT `FKjd6uefgt2bd9d4mtmqk74n028` FOREIGN KEY (`makh`) REFERENCES `khachhang` (`makh`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieuxuat`
--

LOCK TABLES `phieuxuat` WRITE;
/*!40000 ALTER TABLE `phieuxuat` DISABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieuxuat_nvl`
--

LOCK TABLES `phieuxuat_nvl` WRITE;
/*!40000 ALTER TABLE `phieuxuat_nvl` DISABLE KEYS */;
INSERT INTO `phieuxuat_nvl` VALUES (1,1,10,9,'2026-10-21 14:32:11','2026-10-21 14:34:00','DA_XUAT',NULL,''),(2,2,10,9,'2026-10-21 16:50:10','2026-10-21 16:50:37','DA_XUAT',NULL,''),(3,2,10,9,'2026-10-21 17:10:40','2026-10-21 17:11:12','DA_XUAT',NULL,'[XUẤT BÙ HƯ HỎNG] Loa bị hỏng do lỗi nhà sản xuất'),(4,2,10,9,'2026-10-21 17:26:57','2026-10-21 17:29:19','TU_CHOI','Không đủ để xuất, để giành bán','[XUẤT BÙ HƯ HỎNG] Hong thanh RAM khi lap rap laptop may so 5'),(5,2,10,9,'2026-10-21 17:30:47','2026-10-21 17:40:47','DA_XUAT',NULL,'[XUẤT BÙ HƯ HỎNG] hỏng do lỗi nhà sản xuất');
/*!40000 ALTER TABLE `phieuxuat_nvl` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `phieuxuat_nvl_ycsx`
--

DROP TABLE IF EXISTS `phieuxuat_nvl_ycsx`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `phieuxuat_nvl_ycsx` (
  `mapx` bigint NOT NULL,
  `maycsx` bigint NOT NULL,
  PRIMARY KEY (`mapx`,`maycsx`),
  KEY `fk_px_ycsx_yeucau` (`maycsx`),
  CONSTRAINT `fk_px_ycsx_phieu` FOREIGN KEY (`mapx`) REFERENCES `phieuxuat_nvl` (`mapx`),
  CONSTRAINT `fk_px_ycsx_yeucau` FOREIGN KEY (`maycsx`) REFERENCES `yeucausanxuat` (`maycsx`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieuxuat_nvl_ycsx`
--

LOCK TABLES `phieuxuat_nvl_ycsx` WRITE;
/*!40000 ALTER TABLE `phieuxuat_nvl_ycsx` DISABLE KEYS */;
INSERT INTO `phieuxuat_nvl_ycsx` VALUES (1,1),(2,2),(3,2),(4,2),(5,2);
/*!40000 ALTER TABLE `phieuxuat_nvl_ycsx` ENABLE KEYS */;
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
  `diachi_giaohang` varchar(255) DEFAULT NULL,
  `nguoilap` int NOT NULL COMMENT 'Nhân viên lập đề nghị',
  `nguoiduyet` int DEFAULT NULL COMMENT 'Quản lý phê duyệt',
  `ngaylap` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ngayduyet` datetime DEFAULT NULL,
  `trangthai` varchar(20) NOT NULL DEFAULT 'CHO_DUYET' COMMENT 'CHO_DUYET, DA_DUYET, TU_CHOI, DA_XUAT, HUY',
  `lydo` varchar(255) DEFAULT NULL,
  `ghichu` varchar(255) DEFAULT NULL,
  `ten_dot_de_nghi` varchar(150) DEFAULT NULL,
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieuxuat_thanhpham`
--

LOCK TABLES `phieuxuat_thanhpham` WRITE;
/*!40000 ALTER TABLE `phieuxuat_thanhpham` DISABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sanpham`
--

LOCK TABLES `sanpham` WRITE;
/*!40000 ALTER TABLE `sanpham` DISABLE KEYS */;
INSERT INTO `sanpham` VALUES (1,1,'Laptop ASUS TUF Gaming A15','Cái','1792388367806_product-12_3.webp','China',NULL,'Windows',NULL,0.00,0,0,NULL),(2,2,'Laptop Lenovo Gaming LOQ 15IRX9','Cái','1792388437677_lenovo-loq-15irx9-i5-83dv003cvn-new-2-638839537401174909.jpg','China',NULL,'Windows',NULL,0.00,0,0,NULL),(3,1,'Laptop ASUS ROG Strix G16','Cái','1792388493559_x_m_2_207.png','China',NULL,'Windows',NULL,0.00,12,0,NULL);
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
  `trangthai` int NOT NULL,
  `reset_token` varchar(255) DEFAULT NULL,
  `locked` bit(1) NOT NULL,
  PRIMARY KEY (`taikhoan_id`),
  UNIQUE KEY `email` (`email`),
  KEY `fk_taikhoan_nhanvien` (`manv`),
  KEY `fk_taikhoan_vaitro` (`vaitro_id`),
  CONSTRAINT `fk_taikhoan_nhanvien` FOREIGN KEY (`manv`) REFERENCES `nhanvien` (`manv`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_taikhoan_vaitro` FOREIGN KEY (`vaitro_id`) REFERENCES `vaitro` (`vaitro_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `taikhoan`
--

LOCK TABLES `taikhoan` WRITE;
/*!40000 ALTER TABLE `taikhoan` DISABLE KEYS */;
INSERT INTO `taikhoan` VALUES (1,1,2,'quanly@lap123.local','$2a$10$PLACEHOLDER_HASH_FOR_MANAGER',1,NULL,_binary ''),(2,2,2,'nhanvien@lap123.local','$2a$10$PLACEHOLDER_HASH_FOR_EMPLOYEE',1,NULL,_binary ''),(6,6,5,'ad','$2a$10$in3KlNF0odwSm8Q6O3Ee9O6sLzkeAKT0pb1y.kwpVGy6fTCXkgaia',1,NULL,_binary '\0'),(7,7,1,'chien@gmail.com','$2a$10$uz35qMlz/c1VWzXVqur1FOcGqxdrmb7H9TWKnBhXdIVRwT2kgmFcO',1,NULL,_binary ''),(8,8,2,'df@gmial.com','$2a$10$uz35qMlz/c1VWzXVqur1FOcGqxdrmb7H9TWKnBhXdIVRwT2kgmFcO',1,NULL,_binary ''),(9,9,1,'a','$2a$10$in3KlNF0odwSm8Q6O3Ee9O6sLzkeAKT0pb1y.kwpVGy6fTCXkgaia',1,NULL,_binary '\0'),(10,10,2,'N','$2a$10$X7ygq7e5PkNFFLw4Wkyi8ec/uz4KuOHXunXbQB0a7HhOmFYmIDi1W',1,NULL,_binary '\0'),(11,11,2,'NV@gmail.com','$2a$10$k8m6zvOOWyY1ex8Ucv1gaOKZctJVWDotMFnuOkalTQ5ltE9yx2rKu',1,NULL,_binary '\0');
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `thuonghieu`
--

LOCK TABLES `thuonghieu` WRITE;
/*!40000 ALTER TABLE `thuonghieu` DISABLE KEYS */;
INSERT INTO `thuonghieu` VALUES (1,'Asus',0,NULL),(2,'Lenovo',0,NULL),(3,'Dell',0,NULL),(4,'Acer',0,NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vaitro`
--

LOCK TABLES `vaitro` WRITE;
/*!40000 ALTER TABLE `vaitro` DISABLE KEYS */;
INSERT INTO `vaitro` VALUES (1,'QUAN_LY_KHO','Người quản lý kho',NULL),(2,'NHAN_VIEN','Nhân viên sản xuất / kho',NULL),(5,'ADMIN','ADMIN','Administrator');
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
  `madot` bigint DEFAULT NULL,
  `trangthai_truoc_khi_huy` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`maycsx`),
  KEY `fk_ycsx_sanpham` (`masp`),
  KEY `fk_ycsx_bom` (`mabom`),
  KEY `fk_ycsx_nguoitao` (`nguoitao`),
  KEY `fk_ycsx_nguoiphutrach` (`nguoiphutrach`),
  KEY `idx_ycsx_trangthai` (`trangthai`),
  KEY `idx_ycsx_ngay` (`ngayyeucau`),
  KEY `fk_ycsx_dot_san_xuat` (`madot`),
  CONSTRAINT `fk_ycsx_bom` FOREIGN KEY (`mabom`) REFERENCES `bom` (`mabom`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_ycsx_dot_san_xuat` FOREIGN KEY (`madot`) REFERENCES `dot_san_xuat` (`madot`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_ycsx_nguoiphutrach` FOREIGN KEY (`nguoiphutrach`) REFERENCES `nhanvien` (`manv`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_ycsx_nguoitao` FOREIGN KEY (`nguoitao`) REFERENCES `nhanvien` (`manv`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_ycsx_sanpham` FOREIGN KEY (`masp`) REFERENCES `sanpham` (`masp`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `ck_ycsx_sl` CHECK ((`soluongyeucau` > 0)),
  CONSTRAINT `ck_ycsx_slht` CHECK (((`soluonghoanthanh` >= 0) and (`soluonghoanthanh` <= `soluongyeucau`))),
  CONSTRAINT `ck_ycsx_trangthai` CHECK ((`trangthai` in (_utf8mb4'MOI_TAO',_utf8mb4'DA_GIAO',_utf8mb4'DANG_SAN_XUAT',_utf8mb4'HOAN_THANH',_utf8mb4'HUY')))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yeucausanxuat`
--

LOCK TABLES `yeucausanxuat` WRITE;
/*!40000 ALTER TABLE `yeucausanxuat` DISABLE KEYS */;
INSERT INTO `yeucausanxuat` VALUES (1,3,1,10,10,9,10,'2026-10-20 17:17:37','2026-10-31','HOAN_THANH','',NULL,NULL,1,NULL),(2,3,1,2,2,9,10,'2026-10-21 16:48:47','2026-10-22','HOAN_THANH','',NULL,NULL,2,NULL);
/*!40000 ALTER TABLE `yeucausanxuat` ENABLE KEYS */;
UNLOCK TABLES;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-21 20:58:05
