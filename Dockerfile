FROM maven:3.8.4-openjdk-17 as builder
WORKDIR /build
COPY . /build/.
RUN mvn clean install -P prod

FROM eclipse-temurin:17-jre-alpine
ARG JAR_FILE=target/*.jar
COPY --from=build /build/${JAR_FILE} /jira.jar
COPY ./resources /app/resources
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/jira.jar"]