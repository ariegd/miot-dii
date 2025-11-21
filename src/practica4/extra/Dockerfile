FROM eclipse-temurin:21

WORKDIR /app

COPY jax-rs-libs/ /app/jax-rs-libs/
COPY classes/ /app/classes/

ENV CLASSPATH /app/classes/:/app/jax-rs-libs/*

EXPOSE 8080
CMD ["java", "ServidorJersey"]
