# Lap123
## Overview

Lap123 is a website for managing inventory of laptop stores. It allows managing import and export of goods more easily and transparently instead of using Excel as usual. In addition, it is also multi-tasking in using various types of management accounts.

## Features

- Add, edit, delete products, suppliers, customers.
- Manage import & export of items.
- Assign permissions to Admin, managers, employees.
- High security.

## Technologies Used:

Spring Boot (version 3.5.5)
Java (version 17)
Build tool (Maven)
Database (MySQL)
Other relevant libraries or frameworks (Spring Security).

## Prerequisites:

Java Development Kit (JDK)
Maven
An IDE (e.g., IntelliJ IDEA, Eclipse)
Database client or server (if applicable).

## Getting Started

1. Clone the repository:
    ```bash
    git clone https://github.com/TinDev16/laptop123
    ```

2. Building the Project:
   ```bash
   mvn clean install
   ```
3. Setup your account DB in application.properties:
    ```bash
    spring.datasource.username= yourUsername
    spring.datasource.password= yourPassword
    ```
4. Start the development server:
    ```bash
    mvn spring-boot:run
    ```
## Contributing

Contributions are welcome! Please open issues or submit pull requests for improvements and bug fixes.

## License

This project is licensed under the MIT License.

## MRP database setup

For a new database, import `SQL/lap123.sql` into MySQL database `lap123`, then run `SQL/mrp_migration.sql` and `SQL/migrate_two_actors.sql`. If an existing database already has the MRP tables, run only `SQL/migrate_two_actors.sql` after a backup. The original dump contains `DROP TABLE` statements and must never be imported over existing data. Back up the database before any schema change.

The project uses Java 17 and reads MySQL settings from `lap123/src/main/resources/application.properties`.

## MRP demo

The application has exactly two actors: `QUAN_LY_KHO` and `NHAN_VIEN`. `SQL/migrate_two_actors.sql` maps legacy roles to these actors, preserves account password hashes and permissions, and removes the retired roles. Accounts used for MRP tasks need a linked `nhanvien` row through `taikhoan.manv`.

Local demo accounts: `a` (manager; password `a`, created for this workspace) and `nhanvien@lap123.local` (employee; use its existing password). Change the short manager password before deploying outside this local project.

1. Manager: `/nguyen-vat-lieu` to review materials, `/bom` to create a BOM, `/san-xuat/yeu-cau` to assign an order.
2. Manager: `/kho/nhap-nvl` to receive enough materials.
3. Employee: `/san-xuat/yeu-cau` to see the assigned order and needs; `/kho/xuat-nvl` to request materials.
4. Manager: `/kho/xuat-nvl` to approve and issue materials.
5. Employee: `/san-xuat/ket-qua` to record completed units. Manager confirms them on the same page.
6. Employee: `/kho/xuat-thanh-pham` to request finished goods. Manager approves and issues them.
7. Manager: `/kho/lich-su`, `/baocao`, and `/dashboard` to verify inventory and reporting.

Run `mvnw.cmd clean compile` and `mvnw.cmd test` from `lap123` with JDK 17. The warehouse and end-to-end service tests use H2; page smoke tests read the configured MySQL database and require the MRP schema; detail checks run only when BOM/order rows exist.
