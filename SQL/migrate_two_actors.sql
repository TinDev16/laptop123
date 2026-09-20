-- Run once after the base schema is present. Safe to rerun; preserves accounts and BCrypt hashes.
USE lap123;

SET @ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'vaitro' AND column_name = 'ma_vaitro') = 0,
  'ALTER TABLE vaitro ADD COLUMN ma_vaitro VARCHAR(40) NULL', 'SELECT 1');
PREPARE ddl_stmt FROM @ddl; EXECUTE ddl_stmt; DEALLOCATE PREPARE ddl_stmt;
SET @ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'vaitro' AND column_name = 'ten_vaitro') = 0,
  'ALTER TABLE vaitro ADD COLUMN ten_vaitro VARCHAR(255) NULL', 'SELECT 1');
PREPARE ddl_stmt FROM @ddl; EXECUTE ddl_stmt; DEALLOCATE PREPARE ddl_stmt;
SET @ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'taikhoan' AND column_name = 'locked') = 0,
  'ALTER TABLE taikhoan ADD COLUMN locked TINYINT(1) NOT NULL DEFAULT 0', 'SELECT 1');
PREPARE ddl_stmt FROM @ddl; EXECUTE ddl_stmt; DEALLOCATE PREPARE ddl_stmt;

START TRANSACTION;
UPDATE vaitro SET ma_vaitro = ten WHERE ma_vaitro IS NULL AND ten IN ('QUAN_LY_KHO', 'NHAN_VIEN');
INSERT INTO vaitro (ma_vaitro, ten_vaitro, ten)
SELECT 'QUAN_LY_KHO', 'Quản lý kho', 'QUAN_LY_KHO'
WHERE NOT EXISTS (SELECT 1 FROM vaitro WHERE ma_vaitro = 'QUAN_LY_KHO');
INSERT INTO vaitro (ma_vaitro, ten_vaitro, ten)
SELECT 'NHAN_VIEN', 'Nhân viên', 'NHAN_VIEN'
WHERE NOT EXISTS (SELECT 1 FROM vaitro WHERE ma_vaitro = 'NHAN_VIEN');
SET @manager_role_id = (SELECT vaitro_id FROM vaitro WHERE ma_vaitro = 'QUAN_LY_KHO' ORDER BY vaitro_id LIMIT 1);
SET @employee_role_id = (SELECT vaitro_id FROM vaitro WHERE ma_vaitro = 'NHAN_VIEN' ORDER BY vaitro_id LIMIT 1);

-- Keep all legacy permissions when merging rows, then remove old references.
INSERT IGNORE INTO phanquyen (vaitro_id, chucnang_id)
SELECT CASE WHEN COALESCE(v.ma_vaitro, v.ten) IN ('ADMIN', 'QLy') THEN @manager_role_id ELSE @employee_role_id END,
       p.chucnang_id
FROM phanquyen p JOIN vaitro v ON v.vaitro_id = p.vaitro_id
WHERE COALESCE(v.ma_vaitro, v.ten) IN ('ADMIN', 'QLy', 'NV_nhap_xuat');
DELETE p FROM phanquyen p JOIN vaitro v ON v.vaitro_id = p.vaitro_id
WHERE COALESCE(v.ma_vaitro, v.ten) IN ('ADMIN', 'QLy', 'NV_nhap_xuat');
UPDATE taikhoan t JOIN vaitro v ON v.vaitro_id = t.vaitro_id
SET t.vaitro_id = CASE WHEN COALESCE(v.ma_vaitro, v.ten) IN ('ADMIN', 'QLy') THEN @manager_role_id ELSE @employee_role_id END
WHERE COALESCE(v.ma_vaitro, v.ten) IN ('ADMIN', 'QLy', 'NV_nhap_xuat');
DELETE FROM vaitro WHERE COALESCE(ma_vaitro, ten) IN ('ADMIN', 'QLy', 'NV_nhap_xuat');
UPDATE vaitro SET ten = 'QUAN_LY_KHO', ten_vaitro = 'Quản lý kho' WHERE ma_vaitro = 'QUAN_LY_KHO';
UPDATE vaitro SET ten = 'NHAN_VIEN', ten_vaitro = 'Nhân viên' WHERE ma_vaitro = 'NHAN_VIEN';
COMMIT;

SELECT vaitro_id, ma_vaitro, ten_vaitro FROM vaitro ORDER BY vaitro_id;