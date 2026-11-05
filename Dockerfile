# Runtime image for the To-Do app.
# The jar is built and tested by Maven in the CI/CD pipeline first
# (mvn clean verify -> target/todo-app.jar); Docker only packages it.
FROM eclipse-temurin:17-jre-alpine

LABEL org.opencontainers.image.title="todo-app" \
      org.opencontainers.image.description="Simple To-Do list (Spring Boot) for CI/CD demo"

WORKDIR /app

# Run as a non-root user (security best practice)
RUN addgroup -S app && adduser -S app -G app

COPY target/todo-app.jar app.jar
USER app

EXPOSE 8080
ENV APP_VERSION=docker-local

# Container-aware JVM memory sizing
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
