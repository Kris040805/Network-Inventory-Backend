FROM docker.io/library/maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src src
RUN mvn package -DskipTests

FROM docker.io/library/eclipse-temurin:21-jre

RUN groupadd --system app && useradd --system --gid app app

WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]