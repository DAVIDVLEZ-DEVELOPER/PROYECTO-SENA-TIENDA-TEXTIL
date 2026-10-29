FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY src ./src
COPY lib ./lib
RUN mkdir -p bin && \
    find src -name "*.java" > sources.txt && \
    javac -encoding UTF-8 -cp "lib/*" -d bin @sources.txt

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/bin ./bin
COPY --from=builder /app/lib ./lib
EXPOSE 8081
CMD ["java", "-cp", "bin:lib/*", "App"]
