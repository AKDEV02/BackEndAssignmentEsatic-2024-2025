# Étape 1 : Build avec Gradle
FROM gradle:8.2.1-jdk17 AS build
COPY --chown=gradle:gradle . /home/app
WORKDIR /home/app
RUN gradle build --no-daemon

# Étape 2 : Image finale avec JDK
FROM eclipse-temurin:17-jdk
EXPOSE 8080
COPY --from=build /home/app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
