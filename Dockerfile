# Runtime image for the To-Do app.
# The jar is built and tested by Maven in the CI/CD pipeline first
# (mvn clean verify -> target/todo-app.jar); Docker only packages it.
FROM eclipse-temurin:17-jre-alpine

# Build-time values; the CI/CD pipeline passes the real ones with --build-arg
ARG BUILD_VERSION=local
ARG GIT_COMMIT=unknown
ARG BUILD_DATE=unknown

# Image metadata (labels) - visible with: docker inspect <image>
LABEL org.opencontainers.image.title="todo-app" \
      org.opencontainers.image.description="Simple To-Do list (Spring Boot) for CI/CD demo" \
      org.opencontainers.image.authors="Ssivani006" \
      org.opencontainers.image.source="https://github.com/Ssivani006/devopsdemo" \
      org.opencontainers.image.version="${BUILD_VERSION}" \
      org.opencontainers.image.revision="${GIT_COMMIT}" \
      org.opencontainers.image.created="${BUILD_DATE}" \
      project="CIA 2 - Cloud CI/CD Pipeline" \
      environment="production"

WORKDIR /app

# Run as a non-root user (security best practice).
# A numeric UID is required so Kubernetes "runAsNonRoot" can verify it.
RUN addgroup -S -g 10001 app && adduser -S -u 10001 -G app app

COPY target/todo-app.jar app.jar
USER 10001

EXPOSE 8080
ENV APP_VERSION=docker-local

# Container-aware JVM memory sizing
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
