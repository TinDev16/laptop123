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
Java (version 25)
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
       For Maven:
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
