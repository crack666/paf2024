FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Install dos2unix (using apt-get; adjust if using another package manager)
RUN apt-get update && apt-get install -y dos2unix

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Convert line endings to Unix format
RUN dos2unix mvnw

# Ensure the mvnw file is executable
RUN chmod +x ./mvnw

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]