-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: qldean
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
-- Table structure for table `dean`
--

DROP TABLE IF EXISTS `dean`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dean` (
  `MADA` varchar(6) NOT NULL,
  `TENDA` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `DDIEM_DA` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `PHONG` varchar(45) NOT NULL,
  `NGBD_DK` datetime DEFAULT NULL,
  `NGKT_DK` datetime DEFAULT NULL,
  PRIMARY KEY (`MADA`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dean`
--

LOCK TABLES `dean` WRITE;
/*!40000 ALTER TABLE `dean` DISABLE KEYS */;
INSERT INTO `dean` VALUES ('DT001','Dao tao 1','Nha Trang','DH','2002-02-01 00:00:00','2006-02-01 00:00:00'),('DT002','Dao tao 2','Ha Noi','DH','2002-02-01 00:00:00','2006-02-01 00:00:00'),('TH001','Tin hoc hoa 1','Ha Noi','NC','2003-02-01 00:00:00','2004-02-01 00:00:00'),('TH002','Tin hoc hoa 2','TP HCM','NC','2003-06-04 00:00:00','2004-02-01 00:00:00');
/*!40000 ALTER TABLE `dean` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `diadiem_phg`
--

DROP TABLE IF EXISTS `diadiem_phg`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `diadiem_phg` (
  `MAPHG` varchar(4) NOT NULL,
  `DIADIEM` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  PRIMARY KEY (`MAPHG`,`DIADIEM`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `diadiem_phg`
--

LOCK TABLES `diadiem_phg` WRITE;
/*!40000 ALTER TABLE `diadiem_phg` DISABLE KEYS */;
INSERT INTO `diadiem_phg` VALUES ('DH','Ha Noi'),('DH','Nha Trang'),('DH','TP HCM'),('NC','Ha Noi'),('NC','TP HCM'),('QL','TPHCM');
/*!40000 ALTER TABLE `diadiem_phg` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nhanvien`
--

DROP TABLE IF EXISTS `nhanvien`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nhanvien` (
  `MANV` varchar(8) NOT NULL,
  `HONV` varchar(45) NOT NULL,
  `TENLOT` varchar(45) DEFAULT NULL,
  `TENNV` varchar(45) NOT NULL,
  `NGSINH` varchar(45) DEFAULT NULL,
  `PHAI` varchar(5) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `DCHI` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `MA_NGL` varchar(45) DEFAULT NULL,
  `PHONG` varchar(45) DEFAULT NULL,
  `MLUONG` double DEFAULT NULL,
  PRIMARY KEY (`MANV`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nhanvien`
--

LOCK TABLES `nhanvien` WRITE;
/*!40000 ALTER TABLE `nhanvien` DISABLE KEYS */;
INSERT INTO `nhanvien` VALUES ('NV001','Vuong','Ngoc','Quyen','1957-10-22','Nu','450 Trung Vuong, Ha Noi',NULL,'QL',10000000),('NV002','Nguyen','Thanh','Tung','1955-01-09','Nam','731 Tran Hung Dao, Q1, TP HCM','NV001','NC',10000000),('NV003','Le','Thi','Nhan','1960-12-18','Nu','291 Ho Van Hue, QPN, TP HCM','NV001','DH',7500000),('NV004','Dinh','Ba','Tien','1968-01-09','Nam','638 Nguyen Van Cu, Q5, TP HCM','NV002','NC',10000000),('NV005','Bui','Thuy','Vu','1972-07-19','Nam','332 Nguyen Thai Hoc, Q1, TP HCM','NV003','DH',80000000),('NV006','Nguyen','Manh','Hung','1973-09-15','Nam','978 Ba Ria, Vung Tau','NV002','NC',10000000),('NV007','Tran','Thanh','Tam','1975-07-31','Nam','543 Mai Thi Luu, Q1, TP HCM','NV002','NC',10000000),('NV008','Tran','Hong','Van','1976-07-04','Nu','980 Le Hong Phong, Q10, TP HCM','NV004','NC',10000000);
/*!40000 ALTER TABLE `nhanvien` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `phancong`
--

DROP TABLE IF EXISTS `phancong`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `phancong` (
  `MANV` varchar(8) NOT NULL,
  `MADA` varchar(45) NOT NULL,
  `THOIGIAN` double DEFAULT NULL,
  PRIMARY KEY (`MANV`,`MADA`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phancong`
--

LOCK TABLES `phancong` WRITE;
/*!40000 ALTER TABLE `phancong` DISABLE KEYS */;
INSERT INTO `phancong` VALUES ('NV001','TH001',30),('NV001','TH002',12.5),('NV002','DT001',10),('NV002','DT002',10),('NV002','TH001',10),('NV002','TH002',10),('NV003','TH001',37.5),('NV004','DT001',22.5),('NV004','DT002',10),('NV006','DT001',30.5),('NV007','TH001',20),('NV007','TH002',10),('NV008','DT002',12.5),('NV008','TH001',10);
/*!40000 ALTER TABLE `phancong` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `phongban`
--

DROP TABLE IF EXISTS `phongban`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `phongban` (
  `MAPHG` varchar(4) NOT NULL,
  `TENPHG` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `TRPHG` varchar(45) DEFAULT NULL,
  `NGNC` datetime DEFAULT NULL,
  PRIMARY KEY (`MAPHG`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phongban`
--

LOCK TABLES `phongban` WRITE;
/*!40000 ALTER TABLE `phongban` DISABLE KEYS */;
INSERT INTO `phongban` VALUES ('DH','Dieu hanh','NV003','2002-10-10 00:00:00'),('NC','Nghien cuu','NV002','2002-03-15 00:00:00'),('QL','Quan ly','NV001','2000-05-22 00:00:00');
/*!40000 ALTER TABLE `phongban` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `thannhan`
--

DROP TABLE IF EXISTS `thannhan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `thannhan` (
  `MANV` varchar(8) NOT NULL,
  `MATN` varchar(45) NOT NULL,
  `TENTN` varchar(45) NOT NULL,
  `PHAI` varchar(5) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `NGSINH` datetime DEFAULT NULL,
  `QUANHE` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`MANV`,`MATN`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `thannhan`
--

LOCK TABLES `thannhan` WRITE;
/*!40000 ALTER TABLE `thannhan` DISABLE KEYS */;
INSERT INTO `thannhan` VALUES ('NV001','1','Le Nhat Minh','Nam','1955-04-27 00:00:00','Vo chong'),('NV002','1','Le Hoai Thuong','Nu','1960-12-05 00:00:00','Vo chong'),('NV003','1','Tran Minh Tien','Nam','1990-12-11 00:00:00','Con'),('NV003','2','Tran Ngoc Linh','Nu','1993-03-10 00:00:00','Con'),('NV003','3','Tran Minh Long','Nam','1957-10-10 00:00:00','Vo chong'),('NV004','1','Le Phi Nhung','Nu','1972-12-23 00:00:00','Vo chong'),('NV005','1','Tran Thu Hong','Nu','1978-04-11 00:00:00','Vo chong'),('NV005','2','Nguyen Manh Tam','Nam','2003-01-13 00:00:00','Con'),('NV006','1','Nguyen Thanh Hung','Nam','2003-01-13 00:00:00','Con');
/*!40000 ALTER TABLE `thannhan` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-17 10:49:22
