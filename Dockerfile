# Stage 1: Buil
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=prod
COPY app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
