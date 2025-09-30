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
-- Table structure for table `ct_phieunhap`
--

DROP TABLE IF EXISTS `ct_phieunhap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ct_phieunhap` (
  `MAPHIEUNHAP` varchar(10) DEFAULT NULL,
  `MASP` varchar(10) DEFAULT NULL,
  `SOLUONG` int DEFAULT NULL,
  `GIANHAP` float DEFAULT NULL,
  `TRANGTHAI` int DEFAULT NULL
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
-- Table structure for table `ct_phieuxuat`
--

DROP TABLE IF EXISTS `ct_phieuxuat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ct_phieuxuat` (
  `MAPHIEUXUAT` varchar(10) DEFAULT NULL,
  `MASP` varchar(10) DEFAULT NULL,
  `SOLUONG` int DEFAULT NULL,
  `GIAXUAT` float DEFAULT NULL,
  `TRANGTHAI` int DEFAULT NULL
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
-- Table structure for table `khachhang`
--

DROP TABLE IF EXISTS `khachhang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `khachhang` (
  `MAKH` varchar(10) NOT NULL,
  `HOTEN` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `GIOITINH` varchar(10) DEFAULT NULL,
  `NGAYSINH` date DEFAULT NULL,
  `SDT` varchar(15) DEFAULT NULL,
  `NGAYTHAMGIA` date DEFAULT NULL,
  `TRANGTHAI` int DEFAULT NULL,
  PRIMARY KEY (`MAKH`),
  UNIQUE KEY `SDT` (`SDT`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `khachhang`
--

LOCK TABLES `khachhang` WRITE;
/*!40000 ALTER TABLE `khachhang` DISABLE KEYS */;
INSERT INTO `khachhang` VALUES ('1','Lê Văn a','Nam','1970-01-01','12345678',NULL,NULL);
/*!40000 ALTER TABLE `khachhang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ncc`
--

DROP TABLE IF EXISTS `ncc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ncc` (
  `MANCC` varchar(10) NOT NULL,
  `TENNCC` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_german2_ci DEFAULT NULL,
  `DIACHI` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `SDT` varchar(11) DEFAULT NULL,
  `TRANGTHAI` int DEFAULT NULL,
  PRIMARY KEY (`MANCC`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ncc`
--

LOCK TABLES `ncc` WRITE;
/*!40000 ALTER TABLE `ncc` DISABLE KEYS */;
INSERT INTO `ncc` VALUES ('1','Điện máy xanh','hcm','03123456789',NULL);
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
  PRIMARY KEY (`manv`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nhanvien`
--

LOCK TABLES `nhanvien` WRITE;
/*!40000 ALTER TABLE `nhanvien` DISABLE KEYS */;
INSERT INTO `nhanvien` VALUES (1,'Lê A','Nam','2112-12-12 00:00:00.000000','03123456789','2112-12-12 00:00:00.000000',1),(2,'Lê b','nam','2112-12-12 00:00:00.000000','12345','2112-12-12 00:00:00.000000',2),(3,'Dương Thị Anh Thi','Nữ','2112-12-12 00:00:00.000000','123456','2112-12-12 00:00:00.000000',1),(4,'ẻ','ưq','2112-12-12 00:00:00.000000','qew','2321-12-12 00:00:00.000000',0),(5,'Dương Anh Tin','Nam','2021-12-05 00:00:00.000000','0312645','2004-12-02 00:00:00.000000',0),(6,'asa','Nữ','2012-12-21 00:00:00.000000','1212','2032-12-12 00:00:00.000000',0),(7,'qưq','Nữ','2021-12-31 00:00:00.000000','12313','2002-12-11 00:00:00.000000',0),(14,NULL,'Nữ',NULL,NULL,NULL,0),(15,'qư','Nữ','2023-12-11 17:00:00.000000','121321','2034-12-11 17:00:00.000000',0),(16,NULL,'Khác',NULL,NULL,NULL,0);
/*!40000 ALTER TABLE `nhanvien` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `phieunhap`
--

DROP TABLE IF EXISTS `phieunhap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `phieunhap` (
  `MAPHIEUNHAP` varchar(10) NOT NULL,
  `MANCC` varchar(10) DEFAULT NULL,
  `MANV` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `THOIGIAN` datetime DEFAULT NULL,
  `TONGTIEN` float DEFAULT NULL,
  `PHUONGTHUCNHAP` int DEFAULT NULL,
  `TRANGTHAI` int DEFAULT NULL,
  PRIMARY KEY (`MAPHIEUNHAP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieunhap`
--

LOCK TABLES `phieunhap` WRITE;
/*!40000 ALTER TABLE `phieunhap` DISABLE KEYS */;
INSERT INTO `phieunhap` VALUES ('1','1','1',NULL,121122000,1,NULL);
/*!40000 ALTER TABLE `phieunhap` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `phieuxuat`
--

DROP TABLE IF EXISTS `phieuxuat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `phieuxuat` (
  `MAPHIEUXUAT` varchar(10) NOT NULL,
  `MAKH` varchar(10) DEFAULT NULL,
  `MANV` varchar(10) DEFAULT NULL,
  `THOIGIAN` datetime DEFAULT NULL,
  `TONGTIEN` float DEFAULT NULL,
  `TRANGTHAI` int DEFAULT NULL,
  PRIMARY KEY (`MAPHIEUXUAT`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieuxuat`
--

LOCK TABLES `phieuxuat` WRITE;
/*!40000 ALTER TABLE `phieuxuat` DISABLE KEYS */;
INSERT INTO `phieuxuat` VALUES ('1','1','1',NULL,5000,NULL);
/*!40000 ALTER TABLE `phieuxuat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sanpham`
--

DROP TABLE IF EXISTS `sanpham`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sanpham` (
  `masp` bigint NOT NULL AUTO_INCREMENT,
  `MANCC` varchar(10) DEFAULT NULL,
  `TENSP` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `HINHANHSP` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `xuatxu` varchar(255) DEFAULT NULL,
  `chip` varchar(255) DEFAULT NULL,
  `hedieuhanh` varchar(255) DEFAULT NULL,
  `kichthuocman` varchar(255) DEFAULT NULL,
  `thuonghieu` varchar(255) DEFAULT NULL,
  `TRANGTHAI` int DEFAULT NULL,
  PRIMARY KEY (`masp`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sanpham`
--

LOCK TABLES `sanpham` WRITE;
/*!40000 ALTER TABLE `sanpham` DISABLE KEYS */;
INSERT INTO `sanpham` VALUES (1,NULL,NULL,'mac.jpg',NULL,NULL,NULL,NULL,NULL,0),(2,NULL,'Mac i5','images','American','i5','Macbook','21','Apple',1),(3,NULL,'Mac pro','images','USA','i7','Macbook','21','Apple',1),(5,NULL,NULL,'1758947788562_440426110_404991259118437_5169100339175499606_n.jpg',NULL,NULL,NULL,NULL,NULL,0),(7,NULL,NULL,'1759079637431_Screenshot 2025-09-26 220323.png',NULL,NULL,NULL,NULL,NULL,0);
/*!40000 ALTER TABLE `sanpham` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `thuonghieu`
--

DROP TABLE IF EXISTS `thuonghieu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `thuonghieu` (
  `MATHUONGHIEU` varchar(10) NOT NULL,
  `TENTHUONGHIEU` varchar(45) DEFAULT NULL,
  `TRANGTHAI` int DEFAULT NULL,
  PRIMARY KEY (`MATHUONGHIEU`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `thuonghieu`
--

LOCK TABLES `thuonghieu` WRITE;
/*!40000 ALTER TABLE `thuonghieu` DISABLE KEYS */;
INSERT INTO `thuonghieu` VALUES ('1','Apple',NULL);
/*!40000 ALTER TABLE `thuonghieu` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-09-29 21:24:31
