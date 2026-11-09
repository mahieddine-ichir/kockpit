FROM amazoncorretto:21-al2023-headless

COPY target/kockpit-gateway-application-*.jar /app/gateway.jar

# 65%: leaves the JVM's off-heap memory room under the container limit
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=65.0", "-jar", "/app/gateway.jar"]
