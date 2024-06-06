FROM amazoncorretto:11

COPY target/*.jar server.jar

ENTRYPOINT ["java", "-jar", "/server.jar"]

EXPOSE 9090