FROM openjdk:22-slim

COPY target/kockpit-audit-stream-application-kafka-*.jar /app/api.jar

ENTRYPOINT ["java", "-XX:InitialRAMPercentage=80.0", "-XX:MinRAMPercentage=80.0", "-XX:MaxRAMPercentage=80.0", "-jar", "/app/api.jar"]
