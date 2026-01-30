FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copia POMs (pai + todos os módulos para resolver dependências)
COPY pom.xml .
COPY ms-common/pom.xml ./ms-common/
COPY ms-user/pom.xml ./ms-user/
COPY ms-transaction/pom.xml ./ms-transaction/
COPY ms-processor/pom.xml ./ms-processor/

RUN mvn dependency:go-offline -pl ms-transaction -am

# Copia apenas o código necessário para este módulo
COPY ms-common/src ./ms-common/src
COPY ms-transaction/src ./ms-transaction/src

RUN mvn package -DskipTests -pl ms-transaction -am

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /app/ms-transaction/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
