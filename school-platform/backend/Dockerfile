FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY .mvn/settings.xml /root/.m2/settings.xml
COPY pom.xml .
RUN for i in 1 2 3 4 5; do \
      mvn -B -q -DskipTests dependency:go-offline && break || \
      (echo "Maven download failed, retry $i"; sleep 20); \
    done

COPY src src
RUN for i in 1 2 3 4 5; do \
      mvn -B -q -DskipTests package && break || \
      (echo "Maven package failed, retry $i"; sleep 20); \
    done

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/classday-0.1.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
