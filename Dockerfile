FROM openjdk:17-jdk-slim AS build

WORKDIR /app

COPY gradlew .
COPY gradle/wrapper/gradle-wrapper.jar gradle/wrapper/
COPY build.gradle.kts settings.gradle.kts ./

COPY src ./src

RUN chmod +x gradlew

RUN ./gradlew bootJar --no-daemon

# Create the smaller runtime image
# Use a smaller JRE-only base image for the final production image.
# This significantly reduces the size of the deployed container.
FROM openjdk:17-jre-slim

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

# Optional: Add an argument for JVM memory settings.
# This allows you to configure memory limits for the JVM.
# CMD ["-Dspring.profiles.active=prod", "-Xmx256m"] # Example: activate 'prod' profile and set max heap to 256MB
