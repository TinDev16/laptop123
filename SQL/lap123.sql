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
) ENGINE=InnoDB AUTO_INCREMENT=58 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ct_phieunhap`
--

LOCK TABLES `ct_phieunhap` WRITE;
/*!40000 ALTER TABLE `ct_phieunhap` DISABLE KEYS */;
INSERT INTO `ct_phieunhap` VALUES (16,43,18,10,32000000.00,0),(17,43,27,14,35000000.00,0),(22,47,27,2,20000000.00,0),(24,49,27,2,25000000.00,0),(26,51,27,5,10000000.00,0),(27,52,18,2,50000000.00,0),(28,53,25,9,2000000000.00,0),(29,53,20,9,5000000000.00,0),(30,54,27,7,7000000000.00,0),(33,57,28,2,200000.00,0),(34,58,28,2,100.00,0),(35,59,28,1,1100.00,0),(36,60,28,2,100.00,0),(37,61,28,3,2000000000.00,0),(38,62,28,5,20000000.00,0),(39,63,27,3,10000000.00,0),(40,64,22,1,100.00,0),(41,65,27,1,100.00,0),(42,66,27,1,100.00,0),(43,67,27,1,1.00,0),(44,68,27,1,100.00,0),(45,69,27,1,100.00,0),(46,70,27,1,1.00,0),(47,71,27,1,1.00,0),(48,72,27,1,1.00,0),(49,73,27,1,1.00,0),(50,73,25,1,10.00,0),(52,75,27,1,1.00,0),(53,76,22,1,1.00,0),(54,77,27,1,1.00,0),(57,80,27,1,1.00,0);
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
  `giaxuat` decimal(38,2) DEFAULT NULL,
  `TRANGTHAI` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKdf4l71f62xx9a5ogj8eo23jw7` (`MASP`),
  KEY `FKcp0wv1qq6bpwt40cg2u4ctjb9` (`MAPHIEUXUAT`),
  CONSTRAINT `FKcp0wv1qq6bpwt40cg2u4ctjb9` FOREIGN KEY (`MAPHIEUXUAT`) REFERENCES `phieuxuat` (`MAPHIEUXUAT`),
  CONSTRAINT `FKdf4l71f62xx9a5ogj8eo23jw7` FOREIGN KEY (`MASP`) REFERENCES `sanpham` (`masp`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ct_phieuxuat`
--

LOCK TABLES `ct_phieuxuat` WRITE;
/*!40000 ALTER TABLE `ct_phieuxuat` DISABLE KEYS */;
INSERT INTO `ct_phieuxuat` VALUES (1,2,28,14,2000000000.00,0),(2,3,18,10,600000000.00,0),(4,5,18,1,120.00,0),(5,5,27,1,12.00,0),(6,6,22,1,100.00,0),(7,7,27,9,10.00,0),(10,9,27,1,4.00,0),(11,10,27,3,1.00,0),(12,11,22,1,1.00,0),(13,12,25,1,1.00,0);
/*!40000 ALTER TABLE `ct_phieuxuat` ENABLE KEYS */;
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
INSERT INTO `khachhang` VALUES (1,'Lê Văn a','Nam','1970-01-01 00:00:00.000000','12345678','1970-01-01 00:00:00.000000',1,NULL),(3,'Lê Anh Thi','Nữ','2021-12-30 17:00:00.000000','1123312','2122-02-02 17:00:00.000000',0,'d'),(4,'Lê Thị Tre','Nữ','2005-05-11 17:00:00.000000','2312432','2004-05-17 17:00:00.000000',0,''),(5,'Lê Tin','Nam','2005-05-11 17:00:00.000000','213123221','2004-05-17 17:00:00.000000',0,NULL);
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
INSERT INTO `ncc` VALUES (1,'Thế giới di động','HN','03123456789',0,'Ása'),(2,'Điện máy xanh','HCM','091223321',0,'d'),(4,'test','HN','0123456',1,'Ngừng hợp tác');
/*!40000 ALTER TABLE `ncc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nhanvien`
--

DROP TABLE IF EXISTS `nhanvien`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nhanvien` (
  `manv` int NOT NULL AUTO_INCREMENT,
  `hoten` varchar(255) DEFAULT NULL,
  `gioitinh` varchar(255) DEFAULT NULL,
  `ngaysinh` datetime(6) DEFAULT NULL,
  `sdt` varchar(255) DEFAULT NULL,
  `ngaythamgia` datetime(6) DEFAULT NULL,
  `TRANGTHAI` int DEFAULT NULL,
  `ghichu` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`manv`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nhanvien`
--

LOCK TABLES `nhanvien` WRITE;
/*!40000 ALTER TABLE `nhanvien` DISABLE KEYS */;
INSERT INTO `nhanvien` VALUES (4,'Nguyễn Văn Anh','Nam','2112-12-11 17:00:00.000000','12321434','2321-12-11 17:00:00.000000',1,'',NULL),(6,'Phan Văn Tre','Nữ','2012-12-20 17:00:00.000000','12121212','2032-12-11 17:00:00.000000',0,'',NULL),(7,'Lê Tra','Nữ','2021-12-30 17:00:00.000000','12313','2002-12-10 17:00:00.000000',1,'k',NULL),(15,'Nguyễn Thanh Tri','Khác','2023-12-11 17:00:00.000000','121321','2034-12-11 17:00:00.000000',0,NULL,NULL),(24,'Dương Thị Anh Thi','Nữ','2005-02-04 17:00:00.000000','01234567','2025-10-04 17:00:00.000000',0,NULL,NULL),(25,'test','Nam','2001-11-30 17:00:00.000000','023569875','2004-12-01 17:00:00.000000',1,'DS',NULL);
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
INSERT INTO `phanquyen` VALUES (1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(2,2),(2,3),(2,7);
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
) ENGINE=InnoDB AUTO_INCREMENT=81 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieunhap`
--

LOCK TABLES `phieunhap` WRITE;
/*!40000 ALTER TABLE `phieunhap` DISABLE KEYS */;
INSERT INTO `phieunhap` VALUES (43,1,4,'2025-10-13',810000000.00,1,NULL),(47,2,24,'2025-10-14',40000000.00,1,NULL),(49,1,15,'2025-10-14',50000000.00,1,NULL),(51,1,24,'2025-10-15',50000000.00,1,NULL),(52,2,24,'2025-10-16',100000000.00,1,NULL),(53,2,6,'2025-10-15',63000000000.00,1,NULL),(54,2,4,'2025-10-15',49000000000.00,1,NULL),(57,2,4,'2025-10-15',400000.00,1,NULL),(58,1,4,'2025-10-15',200.00,1,NULL),(59,1,4,'2025-10-15',1100.00,1,NULL),(60,2,4,'2025-10-15',200.00,1,NULL),(61,1,4,'2025-10-15',6000000000.00,1,NULL),(62,1,4,'2025-10-15',100000000.00,1,NULL),(63,4,24,'2025-10-17',30000000.00,1,NULL),(64,1,4,'2025-10-17',100.00,1,NULL),(65,1,4,'2025-10-17',100.00,1,NULL),(66,1,4,'2025-10-17',100.00,1,NULL),(67,1,6,'2025-10-17',1.00,1,NULL),(68,1,6,'2025-10-17',100.00,1,NULL),(69,1,6,'2025-10-17',100.00,1,NULL),(70,1,4,'2025-10-17',1.00,1,NULL),(71,1,4,'2025-10-17',1.00,1,NULL),(72,1,6,'2025-10-17',1.00,1,NULL),(73,2,4,'2025-10-17',11.00,1,NULL),(75,1,4,'2025-10-18',1.00,1,NULL),(76,4,4,'2025-10-18',1.00,1,NULL),(77,2,4,'2025-10-18',1.00,1,NULL),(80,2,25,'2025-10-18',1.00,0,NULL);
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
  `thoigian` date DEFAULT NULL,
  `tongtien` decimal(38,2) DEFAULT NULL,
  `TRANGTHAI` int DEFAULT NULL,
  `GHICHU` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`MAPHIEUXUAT`),
  KEY `FKjd6uefgt2bd9d4mtmqk74n028` (`MAKH`),
  KEY `MANV_idx` (`MANV`),
  CONSTRAINT `FKf0xueec76ekuc457mjng6ll6h` FOREIGN KEY (`MANV`) REFERENCES `nhanvien` (`manv`),
  CONSTRAINT `FKjd6uefgt2bd9d4mtmqk74n028` FOREIGN KEY (`MAKH`) REFERENCES `khachhang` (`makh`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieuxuat`
--

LOCK TABLES `phieuxuat` WRITE;
/*!40000 ALTER TABLE `phieuxuat` DISABLE KEYS */;
INSERT INTO `phieuxuat` VALUES (2,3,4,'2025-10-15',28000000000.00,1,NULL),(3,3,24,'2025-10-18',6000000000.00,1,NULL),(5,3,4,'2025-10-17',132.00,1,NULL),(6,3,4,'2025-10-17',100.00,1,NULL),(7,4,6,'2025-10-17',90.00,1,NULL),(9,4,4,'2025-10-18',4.00,1,NULL),(10,4,4,'2025-10-18',3.00,1,NULL),(11,3,6,'2025-10-18',1.00,1,NULL),(12,5,7,'2025-10-19',1.00,0,NULL);
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
  `MATHUONGHIEU` int DEFAULT NULL,
  `TENSP` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `HINHANHSP` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `xuatxu` varchar(255) DEFAULT NULL,
  `chip` varchar(255) DEFAULT NULL,
  `hedieuhanh` varchar(255) DEFAULT NULL,
  `kichthuocman` varchar(255) DEFAULT NULL,
  `TRANGTHAI` int DEFAULT NULL,
  `ghichu` varchar(255) DEFAULT NULL,
  `soluongton` int DEFAULT NULL,
  PRIMARY KEY (`masp`),
  KEY `FKja468m75y1j0w9j0pmncoqpo6` (`MATHUONGHIEU`),
  CONSTRAINT `FKja468m75y1j0w9j0pmncoqpo6` FOREIGN KEY (`MATHUONGHIEU`) REFERENCES `thuonghieu` (`mathuonghieu`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sanpham`
--

LOCK TABLES `sanpham` WRITE;
/*!40000 ALTER TABLE `sanpham` DISABLE KEYS */;
INSERT INTO `sanpham` VALUES (18,2,'Macbook air','1759592778283_440426110_404991259118437_5169100339175499606_n.jpg','China','i7 17th','macOS','21 inch',0,NULL,219),(20,2,'Apple MacBook Air M2 2024 Chính hãng Apple Việt Nam','1759650784448_vn_mac_1_2.webp','USA','8 nhân GPU, 16 nhân Neural Engine','macOS','21 inch',0,NULL,230),(22,2,'Lenovo Legion 5 Ryzen Series 5/ RTX 3050/ ','1759854075546_anh-chup-man-hinh-2025-08-20-183546.webp','China','i7 17th','Windows','15.6 inch',0,'a',20),(25,3,'Lenovo Lecoo Fighter 7000 2025','1759833083130_anh-chup-man-hinh-2025-08-20-183546.webp','China','Core i7-14650HX','Windows','21 inch',0,NULL,31),(27,3,'Laptop Gaming Lenovo Legion 5 R7000 AHP9 NK','1760281476855__new_-anh-sp-web_2f3716d0b1ad4d84930fba0c644ceebb_compact.png','China','AMD Ryzen 7-8745H ','Windows','21 inch',0,'',40),(28,3,'Laptop lenovo pro 5','1760500552290__new_-anh-sp-web_2f3716d0b1ad4d84930fba0c644ceebb_compact.png','China','i7 1200HX','Windows','21 inch',1,'ưa',1),(29,4,'dsa','1760802028111_QLDA.sql','','','Windows','',1,'dá',0);
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
  `email` varchar(255) DEFAULT NULL,
  `vaitro_id` int DEFAULT NULL,
  `manv` int DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`taikhoan_id`),
  KEY `fk_taikhoan_vaitro` (`vaitro_id`),
  KEY `fk_taikhoan_nhanvien` (`manv`),
  CONSTRAINT `fk_taikhoan_nhanvien` FOREIGN KEY (`manv`) REFERENCES `nhanvien` (`manv`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_taikhoan_vaitro` FOREIGN KEY (`vaitro_id`) REFERENCES `vaitro` (`vaitro_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `taikhoan`
--

LOCK TABLES `taikhoan` WRITE;
/*!40000 ALTER TABLE `taikhoan` DISABLE KEYS */;
INSERT INTO `taikhoan` VALUES (1,'admin@mail.com',1,4,'$2a$10$uz35qMlz/c1VWzXVqur1FOcGqxdrmb7H9TWKnBhXdIVRwT2kgmFcO'),(2,'chien@gmail.com',1,6,'$2a$10$uz35qMlz/c1VWzXVqur1FOcGqxdrmb7H9TWKnBhXdIVRwT2kgmFcO'),(3,'df@gmial.com',2,7,'$2a$10$uz35qMlz/c1VWzXVqur1FOcGqxdrmb7H9TWKnBhXdIVRwT2kgmFcO'),(8,'n',2,NULL,'$2a$10$Z4.jFULwRXs7t0s9mZQWre/txiUydiQru7KcCsb1LThyhjWkcLLYC'),(9,'q',1,NULL,'$2a$10$3wddomiRO4uWIisigXVhKO.JHRbVfvqRT3PnvNWcc/J4ZmrfetLVy'),(10,'a',1,NULL,'$2a$10$Er1A91EBjSxXQi9gK1xHc.dHetBhQKEJpXSUsOIVXd8.sKKUfr5Yu');
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
INSERT INTO `thuonghieu` VALUES (1,'Samsung',0,NULL),(2,'Apple',0,''),(3,'Lenovo',0,NULL),(4,'Dell',0,NULL),(5,'Gigabyte',0,NULL),(6,'Asus',0,NULL),(7,'ABC',1,'Ngưng nhận'),(9,'CDF',1,'test'),(10,'d',1,'gf');
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
  `ten` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`vaitro_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vaitro`
--

LOCK TABLES `vaitro` WRITE;
/*!40000 ALTER TABLE `vaitro` DISABLE KEYS */;
INSERT INTO `vaitro` VALUES (1,'QUAN_LY_KHO'),(2,'NHAN_VIEN');
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

-- Dump completed on 2025-10-20 15:47:14
