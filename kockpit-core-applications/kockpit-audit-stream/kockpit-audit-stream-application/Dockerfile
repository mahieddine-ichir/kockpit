FROM amazoncorretto:21-al2023-headless

COPY target/kockpit-audit-stream-application-*.jar /app/api.jar

# InitialRAMPercentage=MinRAMPercentage=MaxRAMPercentage=80 used to pin the heap at ~80% of the
# task's memory from startup, leaving only ~20% for everything the JVM doesn't count as heap:
# Netty direct buffers for the Kinesis EFO/DynamoDB/CloudWatch async clients, Apache HttpClient5
# for OpenSearch, KCL's own thread stacks, metaspace, code cache. On a 4096MB task that's ~800MB
# of headroom - too tight for that many native-memory-heavy clients, and the failure mode is a
# cgroup OOM kill (exit 137, no JVM stack trace - the heap itself is never in danger) rather than
# a diagnosable java.lang.OutOfMemoryError. 50/60 leaves real headroom without starving the heap.
ENTRYPOINT ["java", "-XX:InitialRAMPercentage=50.0", "-XX:MaxRAMPercentage=60.0", "-jar", "/app/api.jar"]
