FROM amazoncorretto:21-alpine AS build
RUN apk add --no-cache maven
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM amazoncorretto:21-alpine-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app_tareas.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app_tareas.jar"]