FROM openjdk:17-jdk-slim
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
COPY src/main/resources/Interns_2025_SWIFT_CODES.xlsx /app/
ENTRYPOINT ["java","-jar","/app.jar"]