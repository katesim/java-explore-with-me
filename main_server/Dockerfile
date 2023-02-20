FROM amazoncorretto:11-alpine-jdk
RUN apk add --no-cache tzdata
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
