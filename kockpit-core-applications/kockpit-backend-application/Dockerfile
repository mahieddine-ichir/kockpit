FROM amazoncorretto:21-al2023-headless

COPY target/kockpit-backend-application-*.jar /app/api.jar

# 65%: at 80% the JVM's off-heap memory went over the container limit and the pod was OOMKilled
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=65.0", "-jar", "/app/api.jar"]

