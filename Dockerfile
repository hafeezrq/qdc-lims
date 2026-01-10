# Stage 1: Build the Application
# We use a Maven image to compile the code inside the container
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Build the JAR file (skip tests to speed it up)
RUN mvn clean package -DskipTests

# Stage 2: Run the Application
# We use a lightweight Java image for the actual running app
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# The command to start the app
ENTRYPOINT ["java", "-jar", "app.jar"]