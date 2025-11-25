# Sử dụng JDK 17 để build (cho Spring Boot)
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Copy toàn bộ project
COPY . .

# Build project (tạo file .jar)
RUN ./mvnw clean package -DskipTests

# -----------------------------

# Image chạy thực tế (nhẹ hơn)
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy file JAR từ bước build
COPY --from=build /app/target/*.jar app.jar

# Expose port Spring Boot
EXPOSE 8080

# Lệnh chạy
ENTRYPOINT ["java", "-jar", "app.jar"]
