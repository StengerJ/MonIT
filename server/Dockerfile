FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml /build/pom.xml
COPY common /build/common
COPY server /build/server
RUN mvn -q -N install \
 && mvn -q -f common/pom.xml install -DskipTests \
 && mvn -q -f server/pom.xml package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /build/server/target/monit-server-0.1.0-SNAPSHOT.jar /app/server.jar
ENTRYPOINT ["java", "-jar", "/app/server.jar"]
