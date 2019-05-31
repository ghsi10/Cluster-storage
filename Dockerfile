FROM openjdk:8-jre

EXPOSE 8080/tcp

ARG JAR_FILE

COPY entrypoint entrypoint
COPY ${JAR_FILE} app.jar

ENV id=""

ENTRYPOINT ["sh", "./entrypoint"]
