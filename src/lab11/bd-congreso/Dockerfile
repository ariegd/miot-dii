FROM postgres:16
ENV POSTGRES_USER=organizador
ENV POSTGRES_PASSWORD=clave123
ENV POSTGRES_DB=congresodb
RUN echo "CREATE TABLE IF NOT EXISTS asistentes (\
id SERIAL PRIMARY KEY, \
nombre VARCHAR(100), \
email VARCHAR(100));" \
> /docker-entrypoint-initdb.d/init.sql
EXPOSE 5432

