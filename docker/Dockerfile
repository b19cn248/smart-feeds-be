# Build stage
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

# Set working directory
WORKDIR /app

# Copy pom files first for better caching
COPY pom.xml .
COPY feeds-core/pom.xml feeds-core/
COPY feeds-core/core-exception/pom.xml feeds-core/core-exception/
COPY feeds-core/core-utils/pom.xml feeds-core/core-utils/
COPY feeds-core/core-kafka/pom.xml feeds-core/core-kafka/
COPY feeds-core/core-redis/pom.xml feeds-core/core-redis/
COPY feeds-core/core-websocket/pom.xml feeds-core/core-websocket/
COPY feeds-core/core-email/pom.xml feeds-core/core-email/
COPY feeds-dao/pom.xml feeds-dao/
COPY feeds-dto/pom.xml feeds-dto/
COPY feeds-service/pom.xml feeds-service/
COPY feeds-api/pom.xml feeds-api/

# Download dependencies (for better caching)
RUN mvn dependency:go-offline -B

# Copy source code
COPY feeds-core feeds-core/
COPY feeds-dao feeds-dao/
COPY feeds-dto feeds-dto/
COPY feeds-service feeds-service/
COPY feeds-api feeds-api/

# Build the application
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Set working directory
WORKDIR /app

# Install necessary tools for health checks
RUN apk add --no-cache curl

# Copy JAR from builder stage
COPY --from=builder /app/feeds-api/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8888

# JVM optimization flags for container environment
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication"

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8888/actuator/health || exit 1

# Start application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]