# 1. Build stage (кешування залежностей)
FROM maven:3.9.4-eclipse-temurin-21 AS builder

WORKDIR /app

# Копіюємо лише pom.xml і завантажуємо залежності (кешується)
COPY pom.xml .
RUN mvn dependency:go-offline

# Копіюємо решту проєкту
COPY src ./src

# Збираємо проект
RUN mvn clean package -DskipTests

# 2. Run stage
FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=builder /app/target/MedVisionSpring-0.0.1-SNAPSHOT.jar ./app.jar
COPY svm-models/svm_full_model.xml svm-models/svm_patch_model.xml ./svm-models/

# Відкриваємо порт
EXPOSE 8081

# Запускаємо застосунок
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Dserver.port=8081", "-jar", "app.jar"]