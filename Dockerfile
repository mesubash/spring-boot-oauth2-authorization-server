
# Build stage
FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./

RUN chmod +x mvnw

# Cache Maven dependencies separately from source changes.
RUN ./mvnw \
    --batch-mode \
    --no-transfer-progress \
    -DskipTests \
    dependency:go-offline

COPY src src

RUN ./mvnw \
    --batch-mode \
    --no-transfer-progress \
    -DskipTests \
    clean package

# Copy the executable Spring Boot jar to a predictable name.
RUN JAR_FILE="$(find target \
        -maxdepth 1 \
        -type f \
        -name '*.jar' \
        ! -name 'original-*' \
        ! -name '*sources*' \
        ! -name '*javadoc*' \
        | head -n 1)" \
    && test -n "$JAR_FILE" \
    && cp "$JAR_FILE" /workspace/app.jar


# ---------------------------------------------------------
# Runtime stage
# ---------------------------------------------------------
FROM eclipse-temurin:25-jre

WORKDIR /app

RUN groupadd --system app \
    && useradd \
        --system \
        --gid app \
        --home-dir /app \
        --shell /usr/sbin/nologin \
        app

COPY --from=build \
    --chown=app:app \
    /workspace/app.jar \
    /app/app.jar

USER app

EXPOSE 9000

ENTRYPOINT ["java", "-jar", "/app/app.jar"]