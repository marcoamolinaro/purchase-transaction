FROM openjdk:17

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} purchase-transaction.jar

ENTRYPOINT ["java", "-jar", "/purchase-transaction.jar"]

EXPOSE 8080