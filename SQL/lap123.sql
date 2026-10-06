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
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bom`
--

LOCK TABLES `bom` WRITE;
/*!40000 ALTER TABLE `bom` DISABLE KEYS */;
INSERT INTO `bom` VALUES (1,1,'1.0','2026-09-18 21:33:20',1,'HIEU_LUC','BOM chuẩn cho Laptop Lauriel Pro 15'),(2,1,'V2.0','2026-09-26 14:05:26',9,'HIEU_LUC',''),(3,1,'V3.0','2026-09-26 16:43:40',9,'HIEU_LUC',''),(4,1,'V4.0','2026-09-26 16:44:04',9,'NGUNG_SU_DUNG',''),(5,2,'mặc định','2026-10-02 01:49:16',9,'HIEU_LUC',''),(6,2,'nâng cấp 1','2026-10-04 02:59:28',9,'HIEU_LUC',''),(7,2,'nâng cấp 2','2026-10-05 01:33:06',9,'HIEU_LUC',''),(8,3,'mặc định','2026-10-05 03:03:07',9,'HIEU_LUC',''),(9,3,'nâng cấp 3','2026-10-05 05:36:44',9,'HIEU_LUC',''),(10,3,'nâng cấp 5','2026-10-06 06:07:31',9,'HIEU_LUC',''),(11,3,'nâng cấp 4','2026-10-06 08:27:09',9,'HIEU_LUC','');
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
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bom_chitiet`
--

LOCK TABLES `bom_chitiet` WRITE;
/*!40000 ALTER TABLE `bom_chitiet` DISABLE KEYS */;
INSERT INTO `bom_chitiet` VALUES (1,1,1,1.0000,0.00,NULL),(2,1,2,1.0000,0.00,NULL),(3,1,3,1.0000,0.00,NULL),(4,1,4,1.0000,0.00,NULL),(5,1,5,1.0000,0.00,NULL),(6,1,6,1.0000,0.00,NULL),(7,2,2,5.0000,0.00,NULL),(8,3,2,5.0000,0.00,NULL),(9,3,5,3.0000,0.00,NULL),(10,4,2,1.0000,0.00,NULL),(11,5,9,3.0000,0.00,NULL),(12,5,1,1.0000,0.00,NULL),(13,6,9,3.0000,0.00,NULL),(14,6,1,1.0000,0.00,NULL),(15,6,15,1.0000,0.00,NULL),(16,7,19,1.0000,0.00,NULL),(17,7,9,3.0000,0.00,NULL),(18,7,1,1.0000,0.00,NULL),(19,7,4,1.0000,0.00,NULL),(20,7,15,1.0000,0.00,NULL),(21,8,19,5.0000,0.00,NULL),(22,9,19,1.0000,0.00,NULL),(23,10,19,2.0000,0.00,NULL),(24,10,9,3.0000,0.00,NULL),(25,11,13,1.0000,0.00,NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ct_phieunhap_nvl`
--

LOCK TABLES `ct_phieunhap_nvl` WRITE;
/*!40000 ALTER TABLE `ct_phieunhap_nvl` DISABLE KEYS */;
INSERT INTO `ct_phieunhap_nvl` (`id`, `mapn`, `manvl`, `soluong`, `dongia`) VALUES (1,1,5,1.00,3000000.00),(2,2,1,1.00,500000.00),(3,3,1,1.00,2220222.00),(4,4,7,6.00,0.00),(5,5,5,3.00,40000.00),(6,5,1,2.00,50000.00),(7,6,5,1.00,32424.00),(8,6,1,3.00,2343242.00),(9,7,8,4.00,0.00),(10,8,1,3.00,67677.00),(11,9,10,2.00,0.00),(12,10,18,4.00,100000.00),(13,10,2,1.00,1200000.00),(14,10,7,1.00,789789.00),(15,10,17,1.00,100000.00),(16,11,19,10.00,500000.00),(17,11,9,10.00,676767.00),(18,12,19,10.00,500000.00),(19,12,9,10.00,676767.00),(20,12,1,6.00,4500000.00),(21,13,15,15.00,100000.00),(22,14,19,14.00,500000.00),(23,15,19,16.00,500000.00),(24,16,19,12.00,500000.00),(25,17,19,8.00,500000.00),(26,18,19,1.00,500000.00),(27,19,19,20.00,500000.00),(28,19,9,1.00,676767.00),(29,20,19,20.00,500000.00),(30,20,9,30.00,676767.00),(31,21,13,11.00,100000.00),(32,22,19,23234.00,500000.00),(33,23,19,23234.00,500000.00);
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
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ct_phieuxuat_nvl`
--

LOCK TABLES `ct_phieuxuat_nvl` WRITE;
/*!40000 ALTER TABLE `ct_phieuxuat_nvl` DISABLE KEYS */;
INSERT INTO `ct_phieuxuat_nvl` VALUES (1,1,1,2.00),(2,1,4,2.00),(3,1,5,2.00),(4,1,2,2.00),(5,1,3,2.00),(6,1,6,2.00),(7,2,1,10.00),(8,2,4,10.00),(9,2,5,10.00),(10,2,2,10.00),(11,2,3,10.00),(12,2,6,10.00),(13,3,1,5.00),(14,3,9,15.00),(15,4,1,3.00),(16,4,9,9.00),(17,5,19,5.00),(18,5,1,5.00),(19,5,4,5.00),(20,5,9,15.00),(21,5,15,5.00),(22,6,19,5.00),(23,6,1,5.00),(24,6,4,5.00),(25,6,9,15.00),(26,6,15,5.00),(27,7,19,5.00),(28,8,19,5.00),(29,9,19,13.00),(30,10,19,2.00),(31,11,19,2.00),(32,12,19,1.00),(33,13,19,2.00),(34,14,19,10.00),(35,15,19,5.00),(36,16,19,9.00),(37,17,19,10.00),(38,18,19,10.00),(39,19,19,5.00),(40,22,19,5.00),(41,23,19,7.00),(42,25,19,14.00),(43,27,13,5.00),(44,28,9,9.00),(45,29,9,6.00),(46,30,13,9.00);
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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ct_phieuxuat_thanhpham`
--

LOCK TABLES `ct_phieuxuat_thanhpham` WRITE;
/*!40000 ALTER TABLE `ct_phieuxuat_thanhpham` DISABLE KEYS */;
INSERT INTO `ct_phieuxuat_thanhpham` (`id`, `mapxtp`, `masp`, `mabom`, `soluong`, `dongia`, `gia_da_fix`) VALUES (3,3,1,1,10,0.00,0),(4,4,2,5,3,0.00,0),(5,5,2,6,6,6630301.00,0),(6,6,2,6,6,7000000.00,1);
/*!40000 ALTER TABLE `ct_phieuxuat_thanhpham` ENABLE KEYS */;
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
  CONSTRAINT `fk_ghi_nhan_nv` FOREIGN KEY (`nguoighi`) REFERENCES `taikhoan` (`manv`),
  CONSTRAINT `fk_ghi_nhan_sp` FOREIGN KEY (`masp`) REFERENCES `sanpham` (`masp`),
  CONSTRAINT `fk_ghi_nhan_ycsx` FOREIGN KEY (`maycsx`) REFERENCES `yeucausanxuat` (`maycsx`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ghi_nhan_san_xuat`
--

LOCK TABLES `ghi_nhan_san_xuat` WRITE;
/*!40000 ALTER TABLE `ghi_nhan_san_xuat` DISABLE KEYS */;
INSERT INTO `ghi_nhan_san_xuat` VALUES (1,18,3,9,2,10,'2026-10-05 07:05:02',NULL),(2,16,3,9,1,10,'2026-10-05 07:06:01',NULL),(3,17,3,9,1,10,'2026-10-05 07:08:37',NULL),(4,17,3,9,1,10,'2026-10-05 07:08:47',NULL),(5,16,3,9,1,10,'2026-10-05 07:08:53',NULL),(6,19,3,9,5,10,'2026-10-05 07:11:55',NULL),(7,19,3,9,5,10,'2026-10-05 07:13:48',NULL),(11,23,3,9,6,10,'2026-10-05 09:23:32',NULL),(12,24,3,9,5,10,'2026-10-06 06:03:44',NULL);
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
  PRIMARY KEY (`makqsx`),
  KEY `fk_kqsx_ycsx` (`maycsx`),
  KEY `fk_kqsx_nhanvien` (`nguoithuchien`),
  KEY `idx_kqsx_ngay` (`ngayhoanthanh`),
  CONSTRAINT `fk_kqsx_nhanvien` FOREIGN KEY (`nguoithuchien`) REFERENCES `nhanvien` (`manv`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_kqsx_ycsx` FOREIGN KEY (`maycsx`) REFERENCES `yeucausanxuat` (`maycsx`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `ck_kqsx_slht` CHECK ((`soluonghoanthanh` > 0)),
  CONSTRAINT `ck_kqsx_slloi` CHECK ((`soluongloi` >= 0)),
  CONSTRAINT `ck_kqsx_trangthai` CHECK ((`trangthai` in (_utf8mb4'CHO_XAC_NHAN',_utf8mb4'DA_NHAP_KHO',_utf8mb4'HUY')))
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ketqua_sanxuat`
--

LOCK TABLES `ketqua_sanxuat` WRITE;
/*!40000 ALTER TABLE `ketqua_sanxuat` DISABLE KEYS */;
INSERT INTO `ketqua_sanxuat` VALUES (1,2,10,1,0,'2026-09-20 16:13:04','DA_NHAP_KHO',''),(2,2,10,2,0,'2026-09-22 07:33:50','DA_NHAP_KHO',''),(3,4,10,5,0,'2026-09-26 12:02:35','DA_NHAP_KHO',''),(4,5,10,5,0,'2026-09-26 12:06:47','CHO_XAC_NHAN',''),(5,6,10,5,0,'2026-09-26 12:38:02','DA_NHAP_KHO',''),(6,7,10,4,0,'2026-09-26 16:45:10','DA_NHAP_KHO',''),(7,8,10,2,0,'2026-09-26 16:46:45','DA_NHAP_KHO',''),(8,6,10,4,0,'2026-09-27 16:42:33','DA_NHAP_KHO',''),(9,1,10,10,0,'2026-09-27 16:48:52','DA_NHAP_KHO',''),(10,1,10,1,0,'2026-09-30 15:40:36','DA_NHAP_KHO',''),(11,9,10,2,0,'2026-09-30 16:23:02','DA_NHAP_KHO','                            '),(12,9,10,1,0,'2026-09-30 16:31:36','CHO_XAC_NHAN','                            '),(13,1,10,1,0,'2026-10-01 10:52:36','CHO_XAC_NHAN','                            '),(14,10,10,3,0,'2026-10-04 02:00:36','DA_NHAP_KHO','                            '),(15,11,10,5,0,'2026-10-04 02:15:17','DA_NHAP_KHO','                            '),(16,11,10,2,0,'2026-10-04 02:16:52','DA_NHAP_KHO','                            '),(17,15,10,8,0,'2026-10-05 06:06:10','DA_NHAP_KHO','                            '),(18,14,10,1,0,'2026-10-05 06:09:04','DA_NHAP_KHO','                            '),(19,13,10,5,0,'2026-10-05 06:09:18','DA_NHAP_KHO','                            '),(20,16,10,8,0,'2026-10-05 06:41:06','CHO_XAC_NHAN','                            '),(21,20,10,10,0,'2026-10-05 08:40:41','DA_NHAP_KHO','                            '),(22,22,10,5,0,'2026-10-05 08:51:15','DA_NHAP_KHO','                            '),(23,23,10,6,0,'2026-10-05 09:23:55','CHO_XAC_NHAN','                            '),(24,24,10,5,0,'2026-10-06 06:05:24','DA_NHAP_KHO','                            ');
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
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `kho_nvl_san_xuat`
--

LOCK TABLES `kho_nvl_san_xuat` WRITE;
/*!40000 ALTER TABLE `kho_nvl_san_xuat` DISABLE KEYS */;
INSERT INTO `kho_nvl_san_xuat` VALUES (1,1,0.00,'2026-10-05 05:21:38'),(2,2,0.00,'2026-10-04 20:37:34'),(3,3,0.00,'2026-10-04 20:37:34'),(4,4,0.00,'2026-10-05 05:21:38'),(5,5,0.00,'2026-10-04 20:37:34'),(6,6,0.00,'2026-10-04 20:37:34'),(7,7,0.00,'2026-10-04 20:37:34'),(8,8,0.00,'2026-10-04 20:37:34'),(9,9,0.00,'2026-10-05 05:21:38'),(10,10,0.00,'2026-10-04 20:37:34'),(11,11,0.00,'2026-10-04 20:37:34'),(12,12,0.00,'2026-10-04 20:37:34'),(13,13,9.00,'2026-10-06 09:35:57'),(14,14,0.00,'2026-10-04 20:37:34'),(15,15,0.00,'2026-10-05 05:21:38'),(16,16,0.00,'2026-10-04 20:37:34'),(17,17,0.00,'2026-10-04 20:37:34'),(18,18,0.00,'2026-10-04 20:37:34'),(19,19,10.00,'2026-10-06 06:03:44');
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
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `kho_thanhpham_san_xuat`
--

LOCK TABLES `kho_thanhpham_san_xuat` WRITE;
/*!40000 ALTER TABLE `kho_thanhpham_san_xuat` DISABLE KEYS */;
INSERT INTO `kho_thanhpham_san_xuat` VALUES (1,1,1,0,'2026-10-04 20:42:13'),(2,1,2,0,'2026-10-04 20:42:13'),(3,1,3,0,'2026-10-04 20:42:13'),(4,1,4,0,'2026-10-04 20:42:13'),(5,2,5,0,'2026-10-04 20:42:13'),(6,2,6,0,'2026-10-04 20:42:13'),(7,2,7,0,'2026-10-05 06:09:31'),(8,3,8,0,'2026-10-05 06:09:35'),(12,3,9,36,'2026-10-06 06:05:48'),(30,3,10,0,'2026-10-06 06:07:31'),(31,3,11,0,'2026-10-06 08:27:09');
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
) ENGINE=InnoDB AUTO_INCREMENT=81 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lich_su_kho`
--

LOCK TABLES `lich_su_kho` WRITE;
/*!40000 ALTER TABLE `lich_su_kho` DISABLE KEYS */;
INSERT INTO `lich_su_kho` VALUES (1,'THANH_PHAM',1,'NHAP_THANH_PHAM',1.00,0.00,'2026-09-20 16:13:37',9,'ketqua_sanxuat',1,NULL),(2,'NVL',5,'NHAP_NVL',1.00,3000000.00,'2026-09-21 21:40:22',9,'phieunhap_nvl',1,NULL),(3,'THANH_PHAM',1,'NHAP_THANH_PHAM',2.00,0.00,'2026-09-22 07:35:28',9,'ketqua_sanxuat',2,NULL),(4,'THANH_PHAM',1,'NHAP_THANH_PHAM',5.00,0.00,'2026-09-26 12:03:00',9,'ketqua_sanxuat',3,NULL),(5,'THANH_PHAM',1,'NHAP_THANH_PHAM',5.00,0.00,'2026-09-26 13:59:04',9,'ketqua_sanxuat',5,NULL),(6,'THANH_PHAM',1,'NHAP_THANH_PHAM',4.00,0.00,'2026-09-26 16:45:22',9,'ketqua_sanxuat',6,NULL),(8,'THANH_PHAM',1,'NHAP_THANH_PHAM',2.00,0.00,'2026-09-26 16:47:07',9,'ketqua_sanxuat',7,NULL),(9,'THANH_PHAM',1,'NHAP_THANH_PHAM',4.00,0.00,'2026-09-27 16:43:35',9,'ketqua_sanxuat',8,NULL),(10,'THANH_PHAM',1,'NHAP_THANH_PHAM',10.00,0.00,'2026-09-27 16:49:27',9,'ketqua_sanxuat',9,NULL),(12,'THANH_PHAM',1,'XUAT_THANH_PHAM',10.00,0.00,'2026-09-27 17:38:32',9,'phieuxuat_thanhpham',3,NULL),(13,'NVL',1,'XUAT_NVL',10.00,0.00,'2026-09-27 18:30:05',9,'phieuxuat_nvl',2,NULL),(14,'NVL',2,'XUAT_NVL',10.00,0.00,'2026-09-27 18:30:05',9,'phieuxuat_nvl',2,NULL),(15,'NVL',3,'XUAT_NVL',10.00,0.00,'2026-09-27 18:30:05',9,'phieuxuat_nvl',2,NULL),(16,'NVL',4,'XUAT_NVL',10.00,0.00,'2026-09-27 18:30:05',9,'phieuxuat_nvl',2,NULL),(17,'NVL',5,'XUAT_NVL',10.00,0.00,'2026-09-27 18:30:05',9,'phieuxuat_nvl',2,NULL),(18,'NVL',6,'XUAT_NVL',10.00,0.00,'2026-09-27 18:30:05',9,'phieuxuat_nvl',2,NULL),(19,'THANH_PHAM',1,'NHAP_THANH_PHAM',1.00,0.00,'2026-09-30 15:40:50',9,'ketqua_sanxuat',10,NULL),(20,'THANH_PHAM',1,'NHAP_THANH_PHAM',2.00,0.00,'2026-09-30 16:23:16',9,'ketqua_sanxuat',11,NULL),(21,'NVL',1,'NHAP_NVL',1.00,500000.00,'2026-09-30 17:46:47',9,'phieunhap_nvl',2,NULL),(22,'NVL',1,'NHAP_NVL',1.00,2220222.00,'2026-09-30 17:48:10',9,'phieunhap_nvl',3,NULL),(23,'NVL',7,'NHAP_NVL',6.00,0.00,'2026-09-30 17:51:18',9,'phieunhap_nvl',4,NULL),(24,'NVL',1,'NHAP_NVL',2.00,50000.00,'2026-10-01 09:12:50',9,'phieunhap_nvl',5,NULL),(25,'NVL',5,'NHAP_NVL',3.00,40000.00,'2026-10-01 09:12:50',9,'phieunhap_nvl',5,NULL),(26,'NVL',1,'NHAP_NVL',3.00,2343242.00,'2026-10-01 10:35:17',9,'phieunhap_nvl',6,NULL),(27,'NVL',5,'NHAP_NVL',1.00,32424.00,'2026-10-01 10:35:17',9,'phieunhap_nvl',6,NULL),(28,'NVL',8,'NHAP_NVL',4.00,0.00,'2026-10-01 13:39:15',9,'phieunhap_nvl',7,NULL),(29,'NVL',1,'NHAP_NVL',3.00,67677.00,'2026-10-01 13:45:24',9,'phieunhap_nvl',8,NULL),(30,'NVL',10,'NHAP_NVL',2.00,0.00,'2026-10-03 22:56:02',9,'phieunhap_nvl',9,NULL),(31,'NVL',2,'NHAP_NVL',1.00,1200000.00,'2026-10-04 01:16:19',9,'phieunhap_nvl',10,NULL),(32,'NVL',7,'NHAP_NVL',1.00,789789.00,'2026-10-04 01:16:19',9,'phieunhap_nvl',10,NULL),(33,'NVL',17,'NHAP_NVL',1.00,100000.00,'2026-10-04 01:16:19',9,'phieunhap_nvl',10,NULL),(34,'NVL',18,'NHAP_NVL',4.00,100000.00,'2026-10-04 01:16:19',9,'phieunhap_nvl',10,NULL),(35,'THANH_PHAM',2,'NHAP_THANH_PHAM',3.00,0.00,'2026-10-04 02:00:59',9,'ketqua_sanxuat',14,NULL),(36,'THANH_PHAM',2,'XUAT_THANH_PHAM',3.00,0.00,'2026-10-04 02:05:20',9,'phieuxuat_thanhpham',4,NULL),(37,'THANH_PHAM',2,'NHAP_THANH_PHAM',5.00,0.00,'2026-10-04 02:15:54',9,'ketqua_sanxuat',15,NULL),(38,'THANH_PHAM',2,'NHAP_THANH_PHAM',2.00,0.00,'2026-10-04 02:17:13',9,'ketqua_sanxuat',16,NULL),(39,'NVL',9,'NHAP_NVL',10.00,676767.00,'2026-10-05 02:47:42',9,'phieunhap_nvl',11,NULL),(40,'NVL',19,'NHAP_NVL',10.00,500000.00,'2026-10-05 02:47:43',9,'phieunhap_nvl',11,NULL),(41,'NVL',1,'NHAP_NVL',6.00,4500000.00,'2026-10-05 02:49:51',9,'phieunhap_nvl',12,NULL),(42,'NVL',9,'NHAP_NVL',10.00,676767.00,'2026-10-05 02:49:51',9,'phieunhap_nvl',12,NULL),(43,'NVL',19,'NHAP_NVL',10.00,500000.00,'2026-10-05 02:49:51',9,'phieunhap_nvl',12,NULL),(44,'NVL',15,'NHAP_NVL',15.00,100000.00,'2026-10-05 03:01:21',9,'phieunhap_nvl',13,NULL),(45,'NVL',1,'XUAT_NVL',5.00,0.00,'2026-10-05 03:01:27',9,'phieuxuat_nvl',6,NULL),(46,'NVL',4,'XUAT_NVL',5.00,0.00,'2026-10-05 03:01:27',9,'phieuxuat_nvl',6,NULL),(47,'NVL',9,'XUAT_NVL',15.00,0.00,'2026-10-05 03:01:27',9,'phieuxuat_nvl',6,NULL),(48,'NVL',15,'XUAT_NVL',5.00,0.00,'2026-10-05 03:01:27',9,'phieuxuat_nvl',6,NULL),(49,'NVL',19,'XUAT_NVL',5.00,0.00,'2026-10-05 03:01:27',9,'phieuxuat_nvl',6,NULL),(50,'NVL',19,'XUAT_NVL',5.00,0.00,'2026-10-05 03:05:01',9,'phieuxuat_nvl',7,NULL),(51,'NVL',19,'XUAT_NVL',5.00,0.00,'2026-10-05 03:06:29',9,'phieuxuat_nvl',8,NULL),(52,'NVL',19,'NHAP_NVL',14.00,500000.00,'2026-10-05 05:37:20',9,'phieunhap_nvl',14,NULL),(53,'NVL',19,'XUAT_NVL',13.00,0.00,'2026-10-05 05:42:54',9,'phieuxuat_nvl',9,NULL),(54,'THANH_PHAM',3,'NHAP_THANH_PHAM',8.00,0.00,'2026-10-05 06:06:23',9,'ketqua_sanxuat',17,NULL),(55,'THANH_PHAM',2,'NHAP_THANH_PHAM',5.00,0.00,'2026-10-05 06:09:31',9,'ketqua_sanxuat',19,NULL),(56,'THANH_PHAM',3,'NHAP_THANH_PHAM',1.00,0.00,'2026-10-05 06:09:35',9,'ketqua_sanxuat',18,NULL),(57,'NVL',19,'XUAT_NVL',2.00,0.00,'2026-10-05 06:38:39',9,'phieuxuat_nvl',10,NULL),(58,'NVL',19,'NHAP_NVL',16.00,500000.00,'2026-10-05 07:03:46',9,'phieunhap_nvl',15,NULL),(59,'NVL',19,'XUAT_NVL',1.00,0.00,'2026-10-05 07:07:30',9,'phieuxuat_nvl',12,NULL),(60,'NVL',19,'XUAT_NVL',2.00,0.00,'2026-10-05 07:07:34',9,'phieuxuat_nvl',11,NULL),(61,'NVL',19,'XUAT_NVL',10.00,0.00,'2026-10-05 07:11:09',9,'phieuxuat_nvl',14,NULL),(62,'NVL',19,'NHAP_NVL',12.00,500000.00,'2026-10-05 07:25:20',9,'phieunhap_nvl',16,NULL),(63,'NVL',19,'XUAT_NVL',10.00,0.00,'2026-10-05 07:25:42',9,'phieuxuat_nvl',17,NULL),(64,'NVL',19,'XUAT_NVL',5.00,0.00,'2026-10-05 07:33:22',9,'phieuxuat_nvl',19,NULL),(65,'THANH_PHAM',3,'NHAP_THANH_PHAM',10.00,0.00,'2026-10-05 08:41:49',9,'ketqua_sanxuat',21,NULL),(66,'NVL',19,'NHAP_NVL',8.00,500000.00,'2026-10-05 08:50:09',9,'phieunhap_nvl',17,NULL),(67,'NVL',19,'XUAT_NVL',5.00,0.00,'2026-10-05 08:50:14',9,'phieuxuat_nvl',22,NULL),(68,'THANH_PHAM',3,'NHAP_THANH_PHAM',5.00,0.00,'2026-10-05 08:56:30',9,'ketqua_sanxuat',22,NULL),(69,'NVL',19,'XUAT_NVL',7.00,0.00,'2026-10-05 09:13:08',9,'phieuxuat_nvl',23,NULL),(70,'NVL',19,'NHAP_NVL',1.00,500000.00,'2026-10-06 03:58:52',9,'phieunhap_nvl',18,NULL),(71,'NVL',9,'NHAP_NVL',1.00,676767.00,'2026-10-06 06:00:46',9,'phieunhap_nvl',19,NULL),(72,'NVL',19,'NHAP_NVL',20.00,500000.00,'2026-10-06 06:00:46',9,'phieunhap_nvl',19,NULL),(73,'NVL',19,'XUAT_NVL',14.00,0.00,'2026-10-06 06:01:29',9,'phieuxuat_nvl',25,NULL),(74,'THANH_PHAM',3,'NHAP_THANH_PHAM',5.00,0.00,'2026-10-06 06:05:48',9,'ketqua_sanxuat',24,NULL),(75,'NVL',9,'NHAP_NVL',30.00,676767.00,'2026-10-06 06:08:36',9,'phieunhap_nvl',20,NULL),(76,'NVL',19,'NHAP_NVL',20.00,500000.00,'2026-10-06 06:08:36',9,'phieunhap_nvl',20,NULL),(77,'NVL',13,'NHAP_NVL',11.00,100000.00,'2026-10-06 08:27:36',9,'phieunhap_nvl',21,NULL),(78,'NVL',13,'XUAT_NVL',9.00,0.00,'2026-10-06 09:35:57',9,'phieuxuat_nvl',30,NULL),(79,'NVL',19,'NHAP_NVL',23234.00,500000.00,'2026-10-06 10:07:09',9,'phieunhap_nvl',22,NULL),(80,'NVL',19,'NHAP_NVL',23234.00,500000.00,'2026-10-06 10:13:55',9,'phieunhap_nvl',23,NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lich_su_kho_san_xuat`
--

LOCK TABLES `lich_su_kho_san_xuat` WRITE;
/*!40000 ALTER TABLE `lich_su_kho_san_xuat` DISABLE KEYS */;
INSERT INTO `lich_su_kho_san_xuat` VALUES (1,'NVL_SAN_XUAT','NVL',19,'NHAP_NVL_SAN_XUAT',14.00,6.00,20.00,9,'2026-10-06 06:01:29','phieuxuat_nvl',25,NULL),(2,'NVL_SAN_XUAT','NVL',19,'XUAT_NVL_SAN_XUAT',10.00,20.00,10.00,10,'2026-10-06 06:03:44','ghi_nhan_san_xuat',12,NULL),(3,'THANH_PHAM_SAN_XUAT','THANH_PHAM',3,'NHAP_THANH_PHAM_SAN_XUAT',10.00,31.00,41.00,10,'2026-10-06 06:03:44','ghi_nhan_san_xuat',12,NULL),(4,'THANH_PHAM_SAN_XUAT','THANH_PHAM',3,'XUAT_THANH_PHAM_SAN_XUAT',5.00,41.00,36.00,9,'2026-10-06 06:05:48','ketqua_sanxuat',24,NULL),(5,'NVL_SAN_XUAT','NVL',13,'NHAP_NVL_SAN_XUAT',9.00,0.00,9.00,9,'2026-10-06 09:35:57','phieuxuat_nvl',30,NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lichsu_dieuchinh_yeucau`
--

LOCK TABLES `lichsu_dieuchinh_yeucau` WRITE;
/*!40000 ALTER TABLE `lichsu_dieuchinh_yeucau` DISABLE KEYS */;
INSERT INTO `lichsu_dieuchinh_yeucau` VALUES (1,4,9,'2026-09-26 11:51:38',12,6,'2026-09-27','2026-09-27',10,8,'Số lượng: 12 → 6. Nhân viên phụ trách: 10 → 8.'),(2,4,9,'2026-09-26 12:01:59',6,6,'2026-09-27','2026-09-27',8,10,'Nhân viên phụ trách: 8 → 10.'),(3,5,9,'2026-09-26 12:07:45',6,4,'2026-09-27','2026-09-27',10,10,'Số lượng: 6 → 4.'),(4,6,9,'2026-09-26 12:39:19',6,8,'2026-09-28','2026-09-28',10,10,'Số lượng: 6 → 8.'),(5,6,9,'2026-09-26 12:49:02',8,9,'2026-09-28','2026-09-28',10,10,'Số lượng: 8 → 9.'),(6,1,9,'2026-09-26 13:31:10',10,10,'2026-09-25','2026-09-30',2,2,'Hạn hoàn thành: 2026-09-25 → 2026-09-30.'),(7,1,9,'2026-09-26 13:49:59',10,10,'2026-09-30','2026-09-30',2,10,'Nhân viên phụ trách: 2 → 10.'),(8,1,9,'2026-09-26 13:51:05',10,12,'2026-09-30','2026-09-30',10,10,'Số lượng: 10 → 12.'),(9,10,9,'2026-10-04 02:00:01',5,5,'2026-10-03','2026-10-05',10,10,'Hạn hoàn thành: 2026-10-03 → 2026-10-05.');
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
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `loai_nguyen_vat_lieu`
--

LOCK TABLES `loai_nguyen_vat_lieu` WRITE;
/*!40000 ALTER TABLE `loai_nguyen_vat_lieu` DISABLE KEYS */;
INSERT INTO `loai_nguyen_vat_lieu` VALUES (1,'màn hình',1),(2,'card',1),(3,'Ram',1),(4,'mainboard',1),(5,'ssd',1),(6,'vỏ',1),(7,'pin',1),(8,'dây điện',1);
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
INSERT INTO `ncc` VALUES (1,'Nhà cung cấp linh kiện ABC','TP.HCM','0280000001','sales@abc.local',0,''),(2,'lmc','lmc','123456789',NULL,0,NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nguyenvatlieu`
--

LOCK TABLES `nguyenvatlieu` WRITE;
/*!40000 ALTER TABLE `nguyenvatlieu` DISABLE KEYS */;
INSERT INTO `nguyenvatlieu` VALUES (1,4,'NVL-MAIN-001','Mainboard Laptop','Cái',4500000.00,21.00,5.00,7,1,''),(2,3,'NVL-RAM-001','RAM 16GB DDR5','Thanh',1200000.00,21.00,10.00,5,1,''),(3,5,'NVL-SSD-001','SSD NVMe 512GB','Ổ',1100000.00,20.00,10.00,5,1,''),(4,1,'NVL-SCR-001','Màn hình 15.6 inch','Cái',2500000.00,5.00,5.00,7,1,''),(5,7,'NVL-BAT-001','Pin Laptop','Cái',900000.00,15.00,5.00,7,1,''),(6,6,'NVL-CASE-001','Vỏ Laptop','Bộ',1500000.00,10.00,5.00,7,1,''),(7,3,'NVL-RAM-002','RAM-test','Thanh',789789.00,7.00,3.00,3,1,''),(8,1,'NVL-MH-002','MH','cái',455454.00,4.00,2.00,0,1,''),(9,8,'NVL-MH-003','MH3','cái',676767.00,36.00,2.00,0,1,''),(10,3,'NVL-RAM-003','Ram3','cái',100000.00,2.00,3.00,0,1,''),(11,3,'NVL-RAM-004','Ram4','Thanh',100000.00,0.00,0.00,0,1,''),(12,3,'NVL-RAM-005','Ram5','Thanh',100000.00,0.00,0.00,0,1,''),(13,3,'NVL-RAM-006','Ram6','Thanh',100000.00,2.00,0.00,0,1,''),(14,3,'NVL-RAM-007','Ram7','Thanh',100000.00,0.00,0.00,0,1,''),(15,3,'NVL-RAM-008','Ram8','Thanh',100000.00,10.00,0.00,0,1,''),(16,3,'NVL-RAM-009','Ram9','Thanh',100000.00,0.00,0.00,0,1,''),(17,3,'NVL-RAM-010','RAM10','Thanh',100000.00,1.00,0.00,0,1,''),(18,3,'NVL-RAM-011','Ram','Thanh',100000.00,4.00,0.00,0,1,''),(19,8,'NVL-DD-001','dây điện cacbon','cuộn',500000.00,46495.00,7.00,0,1,'');
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
INSERT INTO `nhanvien` VALUES (1,'Nguyễn Văn Quản Lý','Nam','1995-01-09 17:00:00.000000','0900000001','quanly@lap123.local','2025-12-31 17:00:00.000000',0,''),(2,'Trần Văn Nhân Viên','Nam','2003-05-19 17:00:00.000000','0900000002','nhanvien@lap123.local','2025-12-31 17:00:00.000000',0,''),(6,'Nguyễn Văn Anh','Nam','2112-12-11 17:00:00.000000','12321434',NULL,'2321-12-11 17:00:00.000000',0,''),(7,'Phan Văn Tre','Nữ','2012-12-20 17:00:00.000000','12121212',NULL,'2032-12-11 17:00:00.000000',1,'gà'),(8,'Lê Tra','Nữ','2021-12-30 17:00:00.000000','12313',NULL,'2002-12-10 17:00:00.000000',1,'k'),(9,'Quản trị viên a',NULL,NULL,NULL,NULL,NULL,1,NULL),(10,'Nguyễn VĂN s','Nam','2012-02-05 17:00:00.000000','012345689',NULL,NULL,0,NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieunhap_nvl`
--

LOCK TABLES `phieunhap_nvl` WRITE;
/*!40000 ALTER TABLE `phieunhap_nvl` DISABLE KEYS */;
INSERT INTO `phieunhap_nvl` VALUES (1,1,9,'2026-09-21 21:40:10',3000000.00,'DA_NHAP',''),(2,1,9,'2026-09-30 17:46:44',500000.00,'DA_NHAP',''),(3,1,9,'2026-09-30 17:47:54',2220222.00,'DA_NHAP',''),(4,1,9,'2026-09-30 17:51:08',0.00,'DA_NHAP',''),(5,1,9,'2026-10-01 09:12:48',220000.00,'DA_NHAP',''),(6,1,9,'2026-10-01 10:35:12',7062150.00,'DA_NHAP',''),(7,1,9,'2026-10-01 13:39:11',0.00,'DA_NHAP',''),(8,2,9,'2026-10-01 13:45:21',203031.00,'DA_NHAP',''),(9,1,9,'2026-10-03 22:56:00',0.00,'DA_NHAP',''),(10,1,9,'2026-10-04 01:16:16',2489789.00,'DA_NHAP','                            '),(11,1,9,'2026-10-05 02:47:40',11767670.00,'DA_NHAP','                            '),(12,1,9,'2026-10-05 02:49:50',38767670.00,'DA_NHAP','                            '),(13,1,9,'2026-10-05 03:01:19',1500000.00,'DA_NHAP','                            '),(14,1,9,'2026-10-05 05:37:17',7000000.00,'DA_NHAP','                            '),(15,1,9,'2026-10-05 07:03:42',8000000.00,'DA_NHAP','                            '),(16,1,9,'2026-10-05 07:25:18',6000000.00,'DA_NHAP','                            '),(17,1,9,'2026-10-05 08:50:07',4000000.00,'DA_NHAP','                            '),(18,1,9,'2026-10-06 03:58:49',500000.00,'DA_NHAP','                            '),(19,1,9,'2026-10-06 06:00:44',10676767.00,'DA_NHAP','                            '),(20,1,9,'2026-10-06 06:08:33',30303010.00,'DA_NHAP','                            '),(21,1,9,'2026-10-06 08:27:33',1100000.00,'DA_NHAP','                            '),(22,1,9,'2026-10-06 10:07:04',11617000000.00,'DA_NHAP','                            '),(23,1,9,'2026-10-06 10:13:51',11617000000.00,'DA_NHAP','                            ');
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
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieuxuat_nvl`
--

LOCK TABLES `phieuxuat_nvl` WRITE;
/*!40000 ALTER TABLE `phieuxuat_nvl` DISABLE KEYS */;
INSERT INTO `phieuxuat_nvl` VALUES (1,2,10,9,'2026-09-21 22:05:52','2026-09-22 09:52:37','TU_CHOI','Không đủ để xuất, để giành bán',''),(2,1,10,9,'2026-09-27 18:29:44','2026-09-27 18:30:05','DA_XUAT',NULL,''),(3,10,10,9,'2026-10-02 01:50:41','2026-10-02 01:51:34','TU_CHOI','Loại nguyên vật liệu \"dây điện\" đã ngừng sử dụng.',''),(4,11,10,NULL,'2026-10-04 02:22:14',NULL,'CHO_DUYET',NULL,''),(5,13,10,NULL,'2026-10-05 01:41:51',NULL,'CHO_DUYET',NULL,''),(6,13,10,9,'2026-10-05 02:13:14','2026-10-05 03:01:27','DA_XUAT',NULL,''),(7,14,10,9,'2026-10-05 03:04:41','2026-10-05 03:05:01','DA_XUAT',NULL,''),(8,14,10,9,'2026-10-05 03:06:06','2026-10-05 03:06:29','DA_XUAT',NULL,''),(9,15,10,9,'2026-10-05 05:42:38','2026-10-05 05:42:54','DA_XUAT',NULL,''),(10,18,10,9,'2026-10-05 06:38:28','2026-10-05 06:38:39','DA_XUAT',NULL,''),(11,17,10,9,'2026-10-05 07:06:50','2026-10-05 07:07:34','DA_XUAT',NULL,''),(12,16,10,9,'2026-10-05 07:07:00','2026-10-05 07:07:30','DA_XUAT',NULL,''),(13,17,10,9,'2026-10-05 07:07:07','2026-10-05 07:07:25','TU_CHOI','á',''),(14,19,10,9,'2026-10-05 07:10:48','2026-10-05 07:11:09','DA_XUAT',NULL,''),(15,19,10,9,'2026-10-05 07:13:08','2026-10-05 07:24:54','TU_CHOI','a',''),(16,20,10,9,'2026-10-05 07:23:36','2026-10-05 07:24:42','TU_CHOI','a',''),(17,20,10,9,'2026-10-05 07:23:59','2026-10-05 07:25:42','DA_XUAT',NULL,''),(18,20,10,NULL,'2026-10-05 07:26:15',NULL,'CHO_DUYET',NULL,''),(19,20,10,9,'2026-10-05 07:26:37','2026-10-05 07:33:22','DA_XUAT',NULL,''),(22,22,10,9,'2026-10-05 08:49:28','2026-10-05 08:50:14','DA_XUAT',NULL,''),(23,23,10,9,'2026-10-05 09:12:42','2026-10-05 09:13:08','DA_XUAT',NULL,''),(25,24,10,9,'2026-10-06 05:59:43','2026-10-06 06:01:29','DA_XUAT',NULL,''),(27,26,10,NULL,'2026-10-06 08:28:47',NULL,'CHO_DUYET',NULL,''),(28,25,10,NULL,'2026-10-06 08:38:29',NULL,'CHO_DUYET',NULL,''),(29,25,10,NULL,'2026-10-06 08:40:23',NULL,'CHO_DUYET',NULL,''),(30,26,10,9,'2026-10-06 09:34:49','2026-10-06 09:35:57','DA_XUAT',NULL,'');
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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieuxuat_thanhpham`
--

LOCK TABLES `phieuxuat_thanhpham` WRITE;
/*!40000 ALTER TABLE `phieuxuat_thanhpham` DISABLE KEYS */;
INSERT INTO `phieuxuat_thanhpham` VALUES (3,NULL,NULL,10,9,'2026-09-27 17:37:42','2026-09-27 17:38:32','DA_XUAT',NULL,''),(4,NULL,NULL,10,9,'2026-10-04 02:05:04','2026-10-04 02:05:20','DA_XUAT',NULL,''),(5,2,'SGU',10,NULL,'2026-10-04 09:15:35',NULL,'CHO_DUYET',NULL,''),(6,1,'SGU',10,9,'2026-10-04 09:35:23','2026-10-04 09:53:17','TU_CHOI','hihi','');
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
INSERT INTO `sanpham` VALUES (1,1,'Laptop Lenovo LOQ','Cái','1790001828294_ssss_13__1_12.webp','Việt Nam123','Intel Core i5','Windows','15.6 inch',19990000.00,26,0,''),(2,1,'Macbook','Cái','1790763678276_1790232647647_01_6_4.webp','mỹ',NULL,'Windows',NULL,0.00,12,0,'fake'),(3,1,'maxbuck','Cái','1791138976969_1790763678276_1790232647647_01_6_4.webp','trung quốc',NULL,'macOS',NULL,0.00,29,0,NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yeucausanxuat`
--

LOCK TABLES `yeucausanxuat` WRITE;
/*!40000 ALTER TABLE `yeucausanxuat` DISABLE KEYS */;
INSERT INTO `yeucausanxuat` VALUES (1,1,1,12,11,1,10,'2026-09-18 21:33:20','2026-09-30','HUY','Sản xuất 10 laptop theo BOM phiên bản 1.0','hủy','2026-10-04 02:08:56'),(2,1,1,3,3,9,10,'2026-09-20 16:10:37','2026-09-30','HOAN_THANH','',NULL,NULL),(3,1,1,11,0,9,8,'2026-09-26 11:46:06','2026-09-27','HUY','','hihi','2026-09-26 11:47:14'),(4,1,1,6,5,9,10,'2026-09-26 11:51:11','2026-09-27','HUY','','hihi','2026-09-26 12:03:33'),(5,1,1,4,0,9,10,'2026-09-26 12:06:28','2026-09-27','HUY','','hihi','2026-09-26 12:08:09'),(6,1,1,9,9,9,10,'2026-09-26 12:37:41','2026-09-28','HOAN_THANH','','hihh','2026-09-26 13:13:29'),(7,1,3,4,4,9,10,'2026-09-26 16:44:31','2026-10-12','HOAN_THANH','',NULL,NULL),(8,1,3,2,2,9,10,'2026-09-26 16:46:29','2026-10-23','HOAN_THANH','',NULL,NULL),(9,1,2,4,2,9,10,'2026-09-30 16:15:07','2026-10-01','HUY','','hủy','2026-10-04 02:08:48'),(10,2,5,5,3,9,10,'2026-10-02 01:50:19','2026-10-05','HUY','','fakelaptop','2026-10-04 02:08:14'),(11,2,5,10,7,9,10,'2026-10-04 02:11:53','2026-10-05','HUY','','h','2026-10-05 05:36:18'),(12,1,1,12,0,9,10,'2026-10-05 01:40:13','2026-10-05','HUY','','h','2026-10-05 05:36:09'),(13,2,7,5,5,9,10,'2026-10-05 01:41:24','2026-10-05','HOAN_THANH','',NULL,NULL),(14,3,8,1,1,9,10,'2026-10-05 03:03:28','2027-11-01','HOAN_THANH','',NULL,NULL),(15,3,9,8,8,9,10,'2026-10-05 05:37:59','2027-11-01','HOAN_THANH','',NULL,NULL),(16,3,9,8,6,9,10,'2026-10-05 06:09:59','2027-11-01','HUY','','a','2026-10-05 07:09:57'),(17,3,9,2,0,9,10,'2026-10-05 06:32:59','2026-11-01','HUY','','a','2026-10-05 07:09:50'),(18,3,9,5,3,9,10,'2026-10-05 06:36:27','2026-11-01','HUY','','a','2026-10-05 07:09:42'),(19,3,9,10,0,9,10,'2026-10-05 07:10:21','2027-11-01','HUY','','a','2026-10-05 07:23:07'),(20,3,9,10,10,9,10,'2026-10-05 07:22:25','2089-10-01','HOAN_THANH','',NULL,NULL),(21,3,9,4,0,9,10,'2026-10-05 08:39:52','2056-10-02','DA_GIAO','',NULL,NULL),(22,3,9,10,5,9,10,'2026-10-05 08:48:21','2067-12-01','DANG_SAN_XUAT','',NULL,NULL),(23,3,9,12,0,9,10,'2026-10-05 09:08:34','2046-02-01','DA_GIAO','',NULL,NULL),(24,3,9,20,5,9,10,'2026-10-06 05:59:19','2026-10-07','DANG_SAN_XUAT','',NULL,NULL),(25,3,10,3,0,9,10,'2026-10-06 06:08:12','2026-10-06','DA_GIAO','',NULL,NULL),(26,3,11,5,0,9,10,'2026-10-06 08:28:29','2026-10-06','DA_GIAO','',NULL,NULL);
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

-- Dump completed on 2026-10-07  0:10:50
