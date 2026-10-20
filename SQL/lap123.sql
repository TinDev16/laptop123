CREATE DATABASE  IF NOT EXISTS `lap123` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `lap123`;
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bom`
--

LOCK TABLES `bom` WRITE;
/*!40000 ALTER TABLE `bom` DISABLE KEYS */;
INSERT INTO `bom` VALUES (1,1,'mặc định','2026-10-19 04:15:00',9,'HIEU_LUC',''),(2,1,'nâng cấp 1','2026-10-19 04:15:18',9,'HIEU_LUC',''),(3,2,'mặc định','2026-10-19 04:19:25',9,'HIEU_LUC','');
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bom_chitiet`
--

LOCK TABLES `bom_chitiet` WRITE;
/*!40000 ALTER TABLE `bom_chitiet` DISABLE KEYS */;
INSERT INTO `bom_chitiet` VALUES (1,1,1,3.0000,0.00,NULL),(2,2,1,5.0000,0.00,NULL),(3,3,1,3.0000,0.00,NULL),(4,3,2,3.0000,0.00,NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ct_phieunhap_nvl`
--

LOCK TABLES `ct_phieunhap_nvl` WRITE;
/*!40000 ALTER TABLE `ct_phieunhap_nvl` DISABLE KEYS */;
INSERT INTO `ct_phieunhap_nvl` (`id`, `mapn`, `manvl`, `soluong`, `dongia`) VALUES (1,1,1,1000.00,232332.00),(2,2,2,1000.00,2424.00);
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
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ct_phieuxuat_nvl`
--

LOCK TABLES `ct_phieuxuat_nvl` WRITE;
/*!40000 ALTER TABLE `ct_phieuxuat_nvl` DISABLE KEYS */;
INSERT INTO `ct_phieuxuat_nvl` VALUES (1,1,1,9.00),(2,1,2,6.00),(3,2,1,2.00),(4,3,1,2.00),(5,4,1,5.00),(6,4,2,3.00),(7,5,1,9.00),(8,5,2,6.00),(9,6,1,9.00),(10,7,1,15.00),(11,7,2,3.00);
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
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dot_san_xuat`
--

LOCK TABLES `dot_san_xuat` WRITE;
/*!40000 ALTER TABLE `dot_san_xuat` DISABLE KEYS */;
INSERT INTO `dot_san_xuat` VALUES (1,'SX-20261019-000001','test',9,'2026-10-19 04:15:40',''),(2,'SX-20261019-000002','test1',9,'2026-10-19 04:19:44',''),(3,'SX-20261019-000003','test10',9,'2026-10-19 08:12:23',''),(4,'SX-20261019-000004','test2',9,'2026-10-19 08:13:47',''),(5,'SX-20261019-000005','test3',9,'2026-10-19 08:14:20',''),(6,'SX-20261019-000006','test23',9,'2026-10-19 09:04:59',''),(7,'SX-20261020-000007','kotest',9,'2026-10-20 05:14:51','');
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
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ghi_nhan_san_xuat`
--

LOCK TABLES `ghi_nhan_san_xuat` WRITE;
/*!40000 ALTER TABLE `ghi_nhan_san_xuat` DISABLE KEYS */;
INSERT INTO `ghi_nhan_san_xuat` VALUES (1,4,1,1,1,10,'2026-10-19 13:40:45',NULL),(2,3,2,3,2,10,'2026-10-19 13:40:45',NULL),(3,9,1,2,1,10,'2026-10-19 14:13:57',NULL),(6,1,1,1,2,10,'2026-10-20 04:48:53',NULL);
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
  PRIMARY KEY (`makqsx`),
  KEY `fk_kqsx_ycsx` (`maycsx`),
  KEY `fk_kqsx_nhanvien` (`nguoithuchien`),
  KEY `idx_kqsx_ngay` (`ngayhoanthanh`),
  CONSTRAINT `fk_kqsx_nhanvien` FOREIGN KEY (`nguoithuchien`) REFERENCES `nhanvien` (`manv`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_kqsx_ycsx` FOREIGN KEY (`maycsx`) REFERENCES `yeucausanxuat` (`maycsx`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `ck_kqsx_slht` CHECK ((`soluonghoanthanh` > 0)),
  CONSTRAINT `ck_kqsx_slloi` CHECK ((`soluongloi` >= 0)),
  CONSTRAINT `ck_kqsx_trangthai` CHECK ((`trangthai` in (_utf8mb4'CHO_XAC_NHAN',_utf8mb4'DA_NHAP_KHO',_utf8mb4'HUY')))
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ketqua_sanxuat`
--

LOCK TABLES `ketqua_sanxuat` WRITE;
/*!40000 ALTER TABLE `ketqua_sanxuat` DISABLE KEYS */;
INSERT INTO `ketqua_sanxuat` VALUES (1,4,10,1,0,'2026-10-19 13:40:45','CHO_XAC_NHAN',NULL,NULL),(2,3,10,2,0,'2026-10-19 13:40:45','CHO_XAC_NHAN',NULL,NULL),(3,9,10,1,0,'2026-10-19 14:13:57','CHO_XAC_NHAN',NULL,'f7e84e4c-9cc9-4775-a342-15ba3830dc4a'),(4,8,10,2,0,'2026-10-19 14:13:57','DA_NHAP_KHO',NULL,'f7e84e4c-9cc9-4775-a342-15ba3830dc4a'),(5,8,10,1,0,'2026-10-19 16:29:09','DA_NHAP_KHO',NULL,'da77d638-2904-4ee6-b6a8-bc8cbed40217'),(6,1,10,2,0,'2026-10-20 04:48:53','CHO_XAC_NHAN',NULL,'a388eff6-c697-40ce-855a-2070b25e2d0e'),(7,10,10,3,0,'2026-10-20 05:16:21','DA_NHAP_KHO',NULL,'7b818258-1a87-47bc-b382-968312740f01');
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `khachhang`
--

LOCK TABLES `khachhang` WRITE;
/*!40000 ALTER TABLE `khachhang` DISABLE KEYS */;
INSERT INTO `khachhang` VALUES (1,'ewqeqw','eqwe',NULL,NULL,0,NULL,'Nam','2026-10-07 17:00:00.000000','2026-10-20 17:00:00.000000');
/*!40000 ALTER TABLE `khachhang` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `kho_nvl_san_xuat`
--

LOCK TABLES `kho_nvl_san_xuat` WRITE;
/*!40000 ALTER TABLE `kho_nvl_san_xuat` DISABLE KEYS */;
INSERT INTO `kho_nvl_san_xuat` VALUES (1,1,3.00,'2026-10-20 05:16:21'),(2,2,3.00,'2026-10-20 05:15:43');
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
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `kho_thanhpham_san_xuat`
--

LOCK TABLES `kho_thanhpham_san_xuat` WRITE;
/*!40000 ALTER TABLE `kho_thanhpham_san_xuat` DISABLE KEYS */;
INSERT INTO `kho_thanhpham_san_xuat` VALUES (1,1,1,3,'2026-10-20 04:48:53'),(2,1,2,1,'2026-10-20 05:21:43'),(3,2,3,2,'2026-10-20 02:41:29');
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
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lich_su_kho`
--

LOCK TABLES `lich_su_kho` WRITE;
/*!40000 ALTER TABLE `lich_su_kho` DISABLE KEYS */;
INSERT INTO `lich_su_kho` VALUES (1,'NVL',1,'NHAP_NVL',1000.00,232332.00,'2026-10-19 04:14:27',9,'phieunhap_nvl',1,NULL),(2,'NVL',2,'NHAP_NVL',1000.00,2424.00,'2026-10-19 04:18:57',9,'phieunhap_nvl',2,NULL),(3,'NVL',1,'XUAT_NVL',9.00,0.00,'2026-10-19 04:29:03',9,'phieuxuat_nvl',1,NULL),(4,'NVL',2,'XUAT_NVL',6.00,0.00,'2026-10-19 04:29:03',9,'phieuxuat_nvl',1,NULL),(5,'NVL',1,'XUAT_NVL',5.00,0.00,'2026-10-19 14:12:18',9,'phieuxuat_nvl',4,NULL),(6,'NVL',2,'XUAT_NVL',3.00,0.00,'2026-10-19 14:12:18',9,'phieuxuat_nvl',4,NULL),(7,'NVL',1,'XUAT_NVL',9.00,0.00,'2026-10-19 14:13:42',9,'phieuxuat_nvl',5,NULL),(8,'NVL',2,'XUAT_NVL',6.00,0.00,'2026-10-19 14:13:42',9,'phieuxuat_nvl',5,NULL),(9,'THANH_PHAM',2,'NHAP_THANH_PHAM',1.00,0.00,'2026-10-20 02:41:02',9,'ketqua_sanxuat',5,NULL),(10,'THANH_PHAM',2,'NHAP_THANH_PHAM',2.00,0.00,'2026-10-20 02:41:29',9,'ketqua_sanxuat',4,NULL),(11,'NVL',1,'XUAT_NVL',9.00,0.00,'2026-10-20 04:45:04',9,'phieuxuat_nvl',6,NULL),(12,'NVL',1,'XUAT_NVL',15.00,0.00,'2026-10-20 05:15:43',9,'phieuxuat_nvl',7,NULL),(13,'NVL',2,'XUAT_NVL',3.00,0.00,'2026-10-20 05:15:43',9,'phieuxuat_nvl',7,NULL),(14,'THANH_PHAM',1,'NHAP_THANH_PHAM',3.00,0.00,'2026-10-20 05:21:43',9,'ketqua_sanxuat',7,NULL);
/*!40000 ALTER TABLE `lich_su_kho` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lich_su_kho_san_xuat`
--

LOCK TABLES `lich_su_kho_san_xuat` WRITE;
/*!40000 ALTER TABLE `lich_su_kho_san_xuat` DISABLE KEYS */;
INSERT INTO `lich_su_kho_san_xuat` VALUES (1,'NVL_SAN_XUAT','NVL',1,'NHAP_NVL_SAN_XUAT',9.00,0.00,9.00,9,'2026-10-19 04:29:03','phieuxuat_nvl',1,NULL),(2,'NVL_SAN_XUAT','NVL',2,'NHAP_NVL_SAN_XUAT',6.00,0.00,6.00,9,'2026-10-19 04:29:03','phieuxuat_nvl',1,NULL),(3,'NVL_SAN_XUAT','NVL',1,'XUAT_NVL_SAN_XUAT',3.00,9.00,6.00,10,'2026-10-19 13:40:45','ghi_nhan_san_xuat',1,NULL),(4,'THANH_PHAM_SAN_XUAT','THANH_PHAM',1,'NHAP_THANH_PHAM_SAN_XUAT',1.00,0.00,1.00,10,'2026-10-19 13:40:45','ghi_nhan_san_xuat',1,NULL),(5,'NVL_SAN_XUAT','NVL',1,'XUAT_NVL_SAN_XUAT',6.00,6.00,0.00,10,'2026-10-19 13:40:45','ghi_nhan_san_xuat',2,NULL),(6,'NVL_SAN_XUAT','NVL',2,'XUAT_NVL_SAN_XUAT',6.00,6.00,0.00,10,'2026-10-19 13:40:45','ghi_nhan_san_xuat',2,NULL),(7,'THANH_PHAM_SAN_XUAT','THANH_PHAM',2,'NHAP_THANH_PHAM_SAN_XUAT',2.00,0.00,2.00,10,'2026-10-19 13:40:45','ghi_nhan_san_xuat',2,NULL),(8,'NVL_SAN_XUAT','NVL',1,'NHAP_NVL_SAN_XUAT',5.00,0.00,5.00,9,'2026-10-19 14:12:18','phieuxuat_nvl',4,NULL),(9,'NVL_SAN_XUAT','NVL',2,'NHAP_NVL_SAN_XUAT',3.00,0.00,3.00,9,'2026-10-19 14:12:18','phieuxuat_nvl',4,NULL),(10,'NVL_SAN_XUAT','NVL',1,'NHAP_NVL_SAN_XUAT',9.00,5.00,14.00,9,'2026-10-19 14:13:42','phieuxuat_nvl',5,NULL),(11,'NVL_SAN_XUAT','NVL',2,'NHAP_NVL_SAN_XUAT',6.00,3.00,9.00,9,'2026-10-19 14:13:42','phieuxuat_nvl',5,NULL),(12,'NVL_SAN_XUAT','NVL',1,'XUAT_NVL_SAN_XUAT',5.00,14.00,9.00,10,'2026-10-19 14:13:57','ghi_nhan_san_xuat',3,NULL),(13,'THANH_PHAM_SAN_XUAT','THANH_PHAM',1,'NHAP_THANH_PHAM_SAN_XUAT',1.00,0.00,1.00,10,'2026-10-19 14:13:57','ghi_nhan_san_xuat',3,NULL),(14,'NVL_SAN_XUAT','NVL',1,'XUAT_NVL_SAN_XUAT',6.00,9.00,3.00,10,'2026-10-19 14:13:57','ghi_nhan_san_xuat',4,NULL),(15,'NVL_SAN_XUAT','NVL',2,'XUAT_NVL_SAN_XUAT',6.00,9.00,3.00,10,'2026-10-19 14:13:57','ghi_nhan_san_xuat',4,NULL),(16,'THANH_PHAM_SAN_XUAT','THANH_PHAM',2,'NHAP_THANH_PHAM_SAN_XUAT',2.00,2.00,4.00,10,'2026-10-19 14:13:57','ghi_nhan_san_xuat',4,NULL),(17,'NVL_SAN_XUAT','NVL',1,'XUAT_NVL_SAN_XUAT',3.00,3.00,0.00,10,'2026-10-19 16:29:09','ghi_nhan_san_xuat',5,NULL),(18,'NVL_SAN_XUAT','NVL',2,'XUAT_NVL_SAN_XUAT',3.00,3.00,0.00,10,'2026-10-19 16:29:09','ghi_nhan_san_xuat',5,NULL),(19,'THANH_PHAM_SAN_XUAT','THANH_PHAM',2,'NHAP_THANH_PHAM_SAN_XUAT',1.00,4.00,5.00,10,'2026-10-19 16:29:09','ghi_nhan_san_xuat',5,NULL),(20,'THANH_PHAM_SAN_XUAT','THANH_PHAM',2,'XUAT_THANH_PHAM_SAN_XUAT',1.00,5.00,4.00,9,'2026-10-20 02:41:02','ketqua_sanxuat',5,NULL),(21,'THANH_PHAM_SAN_XUAT','THANH_PHAM',2,'XUAT_THANH_PHAM_SAN_XUAT',2.00,4.00,2.00,9,'2026-10-20 02:41:29','ketqua_sanxuat',4,NULL),(22,'NVL_SAN_XUAT','NVL',1,'NHAP_NVL_SAN_XUAT',9.00,0.00,9.00,9,'2026-10-20 04:45:04','phieuxuat_nvl',6,NULL),(23,'NVL_SAN_XUAT','NVL',1,'XUAT_NVL_SAN_XUAT',6.00,9.00,3.00,10,'2026-10-20 04:48:53','ghi_nhan_san_xuat',6,NULL),(24,'THANH_PHAM_SAN_XUAT','THANH_PHAM',1,'NHAP_THANH_PHAM_SAN_XUAT',2.00,1.00,3.00,10,'2026-10-20 04:48:53','ghi_nhan_san_xuat',6,NULL),(25,'NVL_SAN_XUAT','NVL',1,'NHAP_NVL_SAN_XUAT',15.00,3.00,18.00,9,'2026-10-20 05:15:43','phieuxuat_nvl',7,NULL),(26,'NVL_SAN_XUAT','NVL',2,'NHAP_NVL_SAN_XUAT',3.00,0.00,3.00,9,'2026-10-20 05:15:43','phieuxuat_nvl',7,NULL),(27,'NVL_SAN_XUAT','NVL',1,'XUAT_NVL_SAN_XUAT',15.00,18.00,3.00,10,'2026-10-20 05:16:21','ghi_nhan_san_xuat',7,NULL),(28,'THANH_PHAM_SAN_XUAT','THANH_PHAM',1,'NHAP_THANH_PHAM_SAN_XUAT',3.00,1.00,4.00,10,'2026-10-20 05:16:21','ghi_nhan_san_xuat',7,NULL),(29,'THANH_PHAM_SAN_XUAT','THANH_PHAM',1,'XUAT_THANH_PHAM_SAN_XUAT',3.00,4.00,1.00,9,'2026-10-20 05:21:43','ketqua_sanxuat',7,NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `loai_nguyen_vat_lieu`
--

LOCK TABLES `loai_nguyen_vat_lieu` WRITE;
/*!40000 ALTER TABLE `loai_nguyen_vat_lieu` DISABLE KEYS */;
INSERT INTO `loai_nguyen_vat_lieu` VALUES (1,'áadsa',1),(2,'fdsf',1);
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ncc`
--

LOCK TABLES `ncc` WRITE;
/*!40000 ALTER TABLE `ncc` DISABLE KEYS */;
INSERT INTO `ncc` VALUES (1,'sầ','sầ','fasfa',NULL,0,NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nguyenvatlieu`
--

LOCK TABLES `nguyenvatlieu` WRITE;
/*!40000 ALTER TABLE `nguyenvatlieu` DISABLE KEYS */;
INSERT INTO `nguyenvatlieu` VALUES (1,1,'NVL-AADSA-001','MH3','cái',232332.00,953.00,0.00,0,1,''),(2,2,'NVL-FDSF-001','fdsf','Ổ',2424.00,982.00,0.00,0,1,'');
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
INSERT INTO `nhanvien` VALUES (1,'Nguyễn Văn Quản Lý','Nam','1995-01-09 17:00:00.000000','0900000001','quanly@lap123.local','2025-12-31 17:00:00.000000',1,'gà'),(2,'Trần Văn Nhân Viên','Khác','2003-05-19 17:00:00.000000','0900000002','nhanvien@lap123.local','2025-12-31 17:00:00.000000',1,'f'),(6,'Nguyễn Văn Anh','Nam','2112-12-11 17:00:00.000000','12321434',NULL,'2321-12-11 17:00:00.000000',0,''),(7,'Phan Văn Tre','Nữ','2012-12-20 17:00:00.000000','12121212',NULL,'2032-12-11 17:00:00.000000',1,'gà'),(8,'Lê Tra','Nữ','2021-12-30 17:00:00.000000','12313',NULL,'2002-12-10 17:00:00.000000',1,'k'),(9,'Quản trị viên a','Nam',NULL,'',NULL,NULL,0,NULL),(10,'Nguyễn VĂN s','Nam','2012-02-05 17:00:00.000000','012345689',NULL,NULL,0,'quá gà'),(11,'NV','Nam','2005-02-19 17:00:00.000000','12321455346','aaaad@gmail.com','2026-10-10 17:00:00.000000',0,NULL);
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
INSERT INTO `phieunhap_nvl` VALUES (1,1,9,'2026-10-19 04:14:24',232332000.00,'DA_NHAP','                            '),(2,1,9,'2026-10-19 04:18:56',2424000.00,'DA_NHAP','                            ');
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
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieuxuat_nvl`
--

LOCK TABLES `phieuxuat_nvl` WRITE;
/*!40000 ALTER TABLE `phieuxuat_nvl` DISABLE KEYS */;
INSERT INTO `phieuxuat_nvl` VALUES (1,3,10,9,'2026-10-19 04:25:08','2026-10-19 04:29:03','DA_XUAT',NULL,''),(2,1,10,9,'2026-10-19 04:36:48','2026-10-19 04:39:09','TU_CHOI','a',''),(3,1,10,NULL,'2026-10-19 08:11:09',NULL,'CHO_DUYET',NULL,''),(4,8,10,9,'2026-10-19 09:05:39','2026-10-19 14:12:18','DA_XUAT',NULL,''),(5,8,10,9,'2026-10-19 14:13:26','2026-10-19 14:13:42','DA_XUAT',NULL,''),(6,1,10,9,'2026-10-20 04:43:38','2026-10-20 04:45:04','DA_XUAT',NULL,''),(7,10,10,9,'2026-10-20 05:15:08','2026-10-20 05:15:43','DA_XUAT',NULL,'');
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
INSERT INTO `phieuxuat_nvl_ycsx` VALUES (2,1),(3,1),(6,1),(2,2),(3,2),(6,2),(1,3),(1,4),(4,8),(5,8),(4,9),(5,9),(7,10),(7,11);
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sanpham`
--

LOCK TABLES `sanpham` WRITE;
/*!40000 ALTER TABLE `sanpham` DISABLE KEYS */;
INSERT INTO `sanpham` VALUES (1,1,'adwaw','Cái','1792358084329_1790001828294_ssss_13__1_12.webp','ffsdfs',NULL,'Windows',NULL,0.00,3,0,NULL),(2,1,'fhfgf','Cái','1792358279070_1790001828294_ssss_13__1_12.webp','gfhfgh',NULL,'Windows',NULL,0.00,3,0,NULL);
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
INSERT INTO `taikhoan` VALUES (1,1,2,'quanly@lap123.local','$2a$10$PLACEHOLDER_HASH_FOR_MANAGER',1,NULL,_binary ''),(2,2,2,'nhanvien@lap123.local','$2a$10$PLACEHOLDER_HASH_FOR_EMPLOYEE',1,NULL,_binary ''),(6,6,5,'ad','$2a$10$in3KlNF0odwSm8Q6O3Ee9O6sLzkeAKT0pb1y.kwpVGy6fTCXkgaia',1,NULL,_binary '\0'),(7,7,1,'chien@gmail.com','$2a$10$uz35qMlz/c1VWzXVqur1FOcGqxdrmb7H9TWKnBhXdIVRwT2kgmFcO',1,NULL,_binary ''),(8,8,2,'df@gmial.com','$2a$10$uz35qMlz/c1VWzXVqur1FOcGqxdrmb7H9TWKnBhXdIVRwT2kgmFcO',1,NULL,_binary ''),(9,9,1,'a','$2a$10$in3KlNF0odwSm8Q6O3Ee9O6sLzkeAKT0pb1y.kwpVGy6fTCXkgaia',1,NULL,_binary '\0'),(10,10,2,'N','$2a$10$X7ygq7e5PkNFFLw4Wkyi8ec/uz4KuOHXunXbQB0a7HhOmFYmIDi1W',1,NULL,_binary '\0'),(11,11,2,'NV','$2a$10$in3KlNF0odwSm8Q6O3Ee9O6sLzkeAKT0pb1y.kwpVGy6fTCXkgaia',1,NULL,_binary '\0');
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
INSERT INTO `thuonghieu` VALUES (1,'eqwewq',0,NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yeucausanxuat`
--

LOCK TABLES `yeucausanxuat` WRITE;
/*!40000 ALTER TABLE `yeucausanxuat` DISABLE KEYS */;
INSERT INTO `yeucausanxuat` VALUES (1,1,1,2,0,9,10,'2026-10-19 04:15:40','2026-10-19','DA_GIAO','',NULL,NULL,1,NULL),(2,1,2,1,0,9,10,'2026-10-19 04:15:40','2026-10-19','DA_GIAO','',NULL,NULL,1,NULL),(3,2,3,2,0,9,10,'2026-10-19 04:19:44','2026-10-19','DA_GIAO','',NULL,NULL,2,NULL),(4,1,1,1,0,9,10,'2026-10-19 04:19:44','2026-10-19','DA_GIAO','',NULL,NULL,2,NULL),(5,2,3,5,0,9,10,'2026-10-19 08:12:23','2026-10-19','HUY','','a','2026-10-19 08:13:08',3,'DA_GIAO'),(6,1,1,2,0,9,10,'2026-10-19 08:13:47','2026-10-19','HUY','','A','2026-10-19 08:13:59',4,'DA_GIAO'),(7,1,1,10,0,9,10,'2026-10-19 08:14:20','2026-10-19','DA_GIAO','',NULL,NULL,5,NULL),(8,2,3,3,3,9,10,'2026-10-19 09:04:59','2026-10-19','HOAN_THANH','',NULL,NULL,6,NULL),(9,1,2,1,0,9,10,'2026-10-19 09:04:59','2026-10-19','DA_GIAO','',NULL,NULL,6,NULL),(10,1,2,3,3,9,10,'2026-10-20 05:14:51','2026-10-20','HOAN_THANH','','a','2026-10-20 05:21:33',7,'DA_GIAO'),(11,2,3,1,0,9,10,'2026-10-20 05:14:51','2026-10-20','DA_GIAO','',NULL,NULL,7,NULL);
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

-- Dump completed on 2026-10-20  7:53:34
