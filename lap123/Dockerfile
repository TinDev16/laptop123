# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml và thư mục src từ context
COPY pom.xml /app/pom.xml
COPY src /app/src

# Build project (bỏ qua test)
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17
WORKDIR /app

# Copy jar từ stage build
COPY --from=build /app/target/*.jar /app/app.jar

# Chạy ứng dụng
CMD ["java", "-jar", "app.jar"]
