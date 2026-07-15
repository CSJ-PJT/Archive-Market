FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN sed -i 's/\r$//' gradlew \
    && chmod +x gradlew \
    && ./gradlew build --no-daemon --console=plain

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/build/libs/archive-market-0.1.0.jar /app/archive-market.jar
EXPOSE 8094
ENTRYPOINT ["java", "-jar", "/app/archive-market.jar"]
