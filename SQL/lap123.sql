-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: lap123
-- ------------------------------------------------------
-- Server version	8.0.43

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
-- Table structure for table `chucnang`
--

DROP TABLE IF EXISTS `chucnang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chucnang` (
  `chucnang_id` int NOT NULL AUTO_INCREMENT,
  `ten` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`chucnang_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chucnang`
--

LOCK TABLES `chucnang` WRITE;
/*!40000 ALTER TABLE `chucnang` DISABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ct_phieunhap`
--

LOCK TABLES `ct_phieunhap` WRITE;
/*!40000 ALTER TABLE `ct_phieunhap` DISABLE KEYS */;
INSERT INTO `ct_phieunhap` VALUES (16,43,18,10,32000000.00,0),(17,43,27,14,35000000.00,0),(22,47,27,2,20000000.00,0),(24,49,27,2,25000000.00,0),(26,51,27,5,10000000.00,0),(27,52,18,2,50000000.00,0),(28,53,25,9,2000000000.00,0),(29,53,20,9,5000000000.00,0),(30,54,27,7,7000000000.00,0);
/*!40000 ALTER TABLE `ct_phieunhap` ENABLE KEYS */;
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
  `GIAXUAT` float DEFAULT NULL,
  `TRANGTHAI` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKdf4l71f62xx9a5ogj8eo23jw7` (`MASP`),
  KEY `FKcp0wv1qq6bpwt40cg2u4ctjb9` (`MAPHIEUXUAT`),
  CONSTRAINT `FKcp0wv1qq6bpwt40cg2u4ctjb9` FOREIGN KEY (`MAPHIEUXUAT`) REFERENCES `phieuxuat` (`MAPHIEUXUAT`),
  CONSTRAINT `FKdf4l71f62xx9a5ogj8eo23jw7` FOREIGN KEY (`MASP`) REFERENCES `sanpham` (`masp`)
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
-- Table structure for table `ct_phieuxuat_seq`
--

DROP TABLE IF EXISTS `ct_phieuxuat_seq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ct_phieuxuat_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ct_phieuxuat_seq`
--

LOCK TABLES `ct_phieuxuat_seq` WRITE;
/*!40000 ALTER TABLE `ct_phieuxuat_seq` DISABLE KEYS */;
INSERT INTO `ct_phieuxuat_seq` VALUES (1);
/*!40000 ALTER TABLE `ct_phieuxuat_seq` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `khachhang`
--

DROP TABLE IF EXISTS `khachhang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `khachhang` (
  `makh` int NOT NULL AUTO_INCREMENT,
  `hoten` varchar(255) DEFAULT NULL,
  `gioitinh` varchar(255) DEFAULT NULL,
  `ngaysinh` datetime(6) DEFAULT NULL,
  `sdt` varchar(255) DEFAULT NULL,
  `ngaythamgia` datetime(6) DEFAULT NULL,
  `TRANGTHAI` int DEFAULT NULL,
  `ghichu` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`makh`),
  UNIQUE KEY `sdt_UNIQUE` (`sdt`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `khachhang`
--

LOCK TABLES `khachhang` WRITE;
/*!40000 ALTER TABLE `khachhang` DISABLE KEYS */;
INSERT INTO `khachhang` VALUES (1,'Lê Văn a','Nam','1970-01-01 00:00:00.000000','12345678','1970-01-01 00:00:00.000000',1,NULL),(3,'Lê Anh Thi','Nữ','2021-12-30 17:00:00.000000','1123312','2122-02-02 17:00:00.000000',0,NULL),(4,'Lê Thị Tre','Nữ','2005-05-11 17:00:00.000000','2312432','2004-05-17 17:00:00.000000',0,NULL),(5,'Lê Tin','Nam','2005-05-11 17:00:00.000000','213123221','2004-05-17 17:00:00.000000',0,NULL),(7,'test','Nam',NULL,'',NULL,1,'Vi phạm\r\n');
/*!40000 ALTER TABLE `khachhang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ncc`
--

DROP TABLE IF EXISTS `ncc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ncc` (
  `mancc` int NOT NULL AUTO_INCREMENT,
  `tenncc` varchar(255) DEFAULT NULL,
  `diachi` varchar(255) DEFAULT NULL,
  `sdt` varchar(255) DEFAULT NULL,
  `TRANGTHAI` int DEFAULT NULL,
  `ghichu` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`mancc`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ncc`
--

LOCK TABLES `ncc` WRITE;
/*!40000 ALTER TABLE `ncc` DISABLE KEYS */;
INSERT INTO `ncc` VALUES (1,'Thế giới di động','HN','03123456789',0,''),(2,'Điện máy xanh','HCM','091223321',0,NULL),(4,'test','','',1,'Ngừng hợp tác');
/*!40000 ALTER TABLE `ncc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nhanvien`
--

DROP TABLE IF EXISTS `nhanvien`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nhanvien` (
  `manv` bigint NOT NULL AUTO_INCREMENT,
  `hoten` varchar(255) DEFAULT NULL,
  `gioitinh` varchar(255) DEFAULT NULL,
  `ngaysinh` datetime(6) DEFAULT NULL,
  `sdt` varchar(255) DEFAULT NULL,
  `ngaythamgia` datetime(6) DEFAULT NULL,
  `TRANGTHAI` int DEFAULT NULL,
  `ghichu` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `vaitro_id` int DEFAULT NULL,
  `taikhoan_id` int DEFAULT NULL,
  PRIMARY KEY (`manv`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nhanvien`
--

LOCK TABLES `nhanvien` WRITE;
/*!40000 ALTER TABLE `nhanvien` DISABLE KEYS */;
INSERT INTO `nhanvien` VALUES (4,'Nguyễn Văn Anh','Nam','2112-12-11 17:00:00.000000','12321434','2321-12-11 17:00:00.000000',0,NULL,NULL,NULL,NULL),(6,'Phan Văn Tre','Nữ','2012-12-20 17:00:00.000000','12121212','2032-12-11 17:00:00.000000',0,NULL,NULL,NULL,NULL),(7,'Lê Tra','Nữ','2021-12-30 17:00:00.000000','12313','2002-12-10 17:00:00.000000',0,NULL,NULL,NULL,NULL),(15,'Nguyễn Thanh Tri','Khác','2023-12-11 17:00:00.000000','121321','2034-12-11 17:00:00.000000',0,NULL,NULL,NULL,NULL),(24,'Dương Thị Anh Thi','Nữ','2005-02-04 17:00:00.000000','01234567','2025-10-04 17:00:00.000000',0,NULL,NULL,NULL,NULL),(25,'test','Nam',NULL,'',NULL,1,'nghỉ việc',NULL,NULL,NULL);
/*!40000 ALTER TABLE `nhanvien` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `phanquyen`
--

DROP TABLE IF EXISTS `phanquyen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `phanquyen` (
  `vaitro_id` int DEFAULT NULL,
  `chucnang_id` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phanquyen`
--

LOCK TABLES `phanquyen` WRITE;
/*!40000 ALTER TABLE `phanquyen` DISABLE KEYS */;
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
  CONSTRAINT `FKqk7uxuhmox5x8hh2mpt9t3wnf` FOREIGN KEY (`MANCC`) REFERENCES `ncc` (`mancc`)
) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieunhap`
--

LOCK TABLES `phieunhap` WRITE;
/*!40000 ALTER TABLE `phieunhap` DISABLE KEYS */;
INSERT INTO `phieunhap` VALUES (43,1,4,'2025-10-13',810000000.00,1,NULL),(47,2,24,'2025-10-14',40000000.00,1,NULL),(49,1,15,'2025-10-14',50000000.00,1,NULL),(51,1,24,'2025-10-15',50000000.00,1,NULL),(52,2,24,'2025-10-16',100000000.00,1,NULL),(53,2,6,'2025-10-15',63000000000.00,1,NULL),(54,2,4,'2025-10-15',49000000000.00,1,NULL);
/*!40000 ALTER TABLE `phieunhap` ENABLE KEYS */;
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
  `thoigian` datetime(6) DEFAULT NULL,
  `TONGTIEN` float DEFAULT NULL,
  `TRANGTHAI` int DEFAULT NULL,
  `GHICHU` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`MAPHIEUXUAT`),
  KEY `FKjd6uefgt2bd9d4mtmqk74n028` (`MAKH`),
  CONSTRAINT `FKjd6uefgt2bd9d4mtmqk74n028` FOREIGN KEY (`MAKH`) REFERENCES `khachhang` (`makh`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieuxuat`
--

LOCK TABLES `phieuxuat` WRITE;
/*!40000 ALTER TABLE `phieuxuat` DISABLE KEYS */;
INSERT INTO `phieuxuat` VALUES (1,1,1,NULL,5000,1,NULL);
/*!40000 ALTER TABLE `phieuxuat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sanpham`
--

DROP TABLE IF EXISTS `sanpham`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sanpham` (
  `masp` int NOT NULL AUTO_INCREMENT,
  `TENSP` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `HINHANHSP` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `xuatxu` varchar(255) DEFAULT NULL,
  `chip` varchar(255) DEFAULT NULL,
  `hedieuhanh` varchar(255) DEFAULT NULL,
  `kichthuocman` varchar(255) DEFAULT NULL,
  `TRANGTHAI` int DEFAULT NULL,
  `MATHUONGHIEU` int DEFAULT NULL,
  `ghichu` varchar(255) DEFAULT NULL,
  `soluongton` int DEFAULT NULL,
  PRIMARY KEY (`masp`),
  KEY `FKja468m75y1j0w9j0pmncoqpo6` (`MATHUONGHIEU`),
  CONSTRAINT `FKja468m75y1j0w9j0pmncoqpo6` FOREIGN KEY (`MATHUONGHIEU`) REFERENCES `thuonghieu` (`mathuonghieu`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sanpham`
--

LOCK TABLES `sanpham` WRITE;
/*!40000 ALTER TABLE `sanpham` DISABLE KEYS */;
INSERT INTO `sanpham` VALUES (18,'Macbook air','1759592778283_440426110_404991259118437_5169100339175499606_n.jpg','China','i7 17th','macOS','21 inch',0,2,NULL,230),(20,'Apple MacBook Air M2 2024 Chính hãng Apple Việt Nam','1759650784448_vn_mac_1_2.webp','USA','8 nhân GPU, 16 nhân Neural Engine','macOS','21 inch',0,2,NULL,230),(21,'test','1759651379395_image_1425_2.webp','qư','ưq','Windows','ưq',1,3,'Nhập sai dữ liệu\r\n',NULL),(22,'Lenovo Legion 5 Ryzen Series 5/ RTX 3050/ ','1759854075546_anh-chup-man-hinh-2025-08-20-183546.webp','China','i7 17th','Windows','15.6 inch',0,2,'',20),(23,'ấ','1759722753890_vn_mac_1_2.webp','dsa','dsa','Windows','21',1,7,'nhập sai dữ liệu',NULL),(24,'sd','1759810598888_image_1425_2.webp','sa','SA','Linux','WWQED',1,1,'nhập sai dữ liệu',NULL),(25,'Lenovo Lecoo Fighter 7000 2025','1759833083130_anh-chup-man-hinh-2025-08-20-183546.webp','China','Core i7-14650HX','Windows','21 inch',0,3,NULL,30),(26,'ưq','1759833116605_anh-chup-man-hinh-2025-08-20-183546.webp','','','Windows','',1,2,'nhập sai dữ liệu',0),(27,'Laptop Gaming Lenovo Legion 5 R7000 AHP9 NK','1760281476855__new_-anh-sp-web_2f3716d0b1ad4d84930fba0c644ceebb_compact.png','China','AMD Ryzen 7-8745H (8 nhân 16 luồng, 3.8GHz - 4.9GHz, bộ nhớ đệm 8MB L2 / 16MB L3, 4nm, TDP 35-54W)','Windows','21 inch',0,3,NULL,40);
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
  `email` varchar(45) DEFAULT NULL,
  `matkhau` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`taikhoan_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `taikhoan`
--

LOCK TABLES `taikhoan` WRITE;
/*!40000 ALTER TABLE `taikhoan` DISABLE KEYS */;
INSERT INTO `taikhoan` VALUES (1,'admin@mail.com','123123'),(2,'chien@gmail.com','1234'),(3,'df@gmial.com','123123');
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
  `tenthuonghieu` varchar(255) DEFAULT NULL,
  `TRANGTHAI` int DEFAULT NULL,
  `ghichu` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`mathuonghieu`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `thuonghieu`
--

LOCK TABLES `thuonghieu` WRITE;
/*!40000 ALTER TABLE `thuonghieu` DISABLE KEYS */;
INSERT INTO `thuonghieu` VALUES (1,'Samsung',0,NULL),(2,'Apple',0,NULL),(3,'Lenovo',0,NULL),(4,'Dell',0,NULL),(5,'Gigabyte',0,NULL),(6,'Asus',0,NULL),(7,'test',1,'Ngưng nhận\r\n'),(9,'test',1,'test'),(10,'d',1,'gf');
/*!40000 ALTER TABLE `thuonghieu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tonkho`
--

DROP TABLE IF EXISTS `tonkho`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tonkho` (
  `id` int NOT NULL AUTO_INCREMENT,
  `MASP` int DEFAULT NULL,
  `SOLUONGTON` int DEFAULT NULL,
  `GIANHAP` int DEFAULT NULL,
  `GIABAN` int DEFAULT NULL,
  `NGAYCAPNHAT` date DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tonkho`
--

LOCK TABLES `tonkho` WRITE;
/*!40000 ALTER TABLE `tonkho` DISABLE KEYS */;
/*!40000 ALTER TABLE `tonkho` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vaitro`
--

DROP TABLE IF EXISTS `vaitro`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vaitro` (
  `vaitro_id` int NOT NULL AUTO_INCREMENT,
  `ten` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`vaitro_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vaitro`
--

LOCK TABLES `vaitro` WRITE;
/*!40000 ALTER TABLE `vaitro` DISABLE KEYS */;
/*!40000 ALTER TABLE `vaitro` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-15 10:28:57
