FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml /build/pom.xml
COPY common /build/common
COPY agent /build/agent
RUN mvn -q -N install \
 && mvn -q -f common/pom.xml install -DskipTests \
 && mvn -q -f agent/pom.xml package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /build/agent/target/monit-agent-0.1.0-SNAPSHOT.jar /app/agent.jar
ENTRYPOINT ["java", "-jar", "/app/agent.jar"]
