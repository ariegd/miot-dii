# Tema 5: Creación y Administración de Imágenes y Contenedores

## Introducción

Este documento cubre los aspectos prácticos de trabajar con Docker: desde la creación de imágenes hasta la administración de contenedores en producción.

## 1. Imágenes Docker

### 1.1 ¿Qué es una Imagen Docker?

Una imagen es una plantilla de solo lectura que contiene:
- Sistema de archivos
- Aplicación y dependencias
- Configuración
- Metadatos

### 1.2 Obtener Imágenes

#### Desde Docker Hub

```bash
# Buscar imágenes
docker search nginx

# Descargar imagen (última versión)
docker pull nginx

# Descargar versión específica
docker pull nginx:1.25-alpine

# Descargar todas las etiquetas
docker pull --all-tags nginx
```

#### Desde Otros Registries

```bash
# Google Container Registry
docker pull gcr.io/google-samples/hello-app:1.0

# Amazon ECR
docker pull 123456789012.dkr.ecr.us-east-1.amazonaws.com/my-app:latest

# GitHub Container Registry
docker pull ghcr.io/usuario/mi-app:v1.0
```

### 1.3 Listar y Gestionar Imágenes

```bash
# Listar imágenes locales
docker images
docker image ls

# Listar con filtros
docker images --filter "dangling=true"
docker images --filter "reference=nginx:*"

# Mostrar detalles de una imagen
docker image inspect nginx:alpine

# Ver historial de capas
docker history nginx:alpine

# Ver espacio usado por imágenes
docker system df
docker system df -v
```

### 1.4 Eliminar Imágenes

```bash
# Eliminar imagen por nombre
docker rmi nginx:alpine

# Eliminar por ID
docker rmi abc123def456

# Eliminar imágenes sin usar (dangling)
docker image prune

# Eliminar todas las imágenes sin usar
docker image prune -a

# Forzar eliminación
docker rmi -f nginx:alpine
```

## 2. Creación de Imágenes con Dockerfile

### 2.1 Estructura de un Dockerfile

```dockerfile
# Imagen base
FROM alpine:3.18

# Metadatos
LABEL maintainer="tu-email@example.com"
LABEL version="1.0"
LABEL description="Mi aplicación en contenedor"

# Variables de entorno
ENV APP_HOME=/app \
    APP_USER=appuser

# Instalar dependencias
RUN apk add --no-cache python3 py3-pip

# Crear usuario no privilegiado
RUN addgroup -g 1000 ${APP_USER} && \
    adduser -D -u 1000 -G ${APP_USER} ${APP_USER}

# Establecer directorio de trabajo
WORKDIR ${APP_HOME}

# Copiar archivos
COPY requirements.txt .
RUN pip3 install --no-cache-dir -r requirements.txt

COPY app.py .

# Cambiar a usuario no privilegiado
USER ${APP_USER}

# Exponer puerto
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/health || exit 1

# Volumen para datos persistentes
VOLUME ["/app/data"]

# Comando por defecto
CMD ["python3", "app.py"]
```

### 2.2 Instrucciones de Dockerfile

#### FROM

```dockerfile
# Imagen base
FROM ubuntu:22.04

# Multi-stage build
FROM golang:1.21 AS builder
FROM alpine:3.18
```

#### RUN

```dockerfile
# Shell form (ejecuta con /bin/sh -c)
RUN apt-get update && apt-get install -y curl

# Exec form (sin shell)
RUN ["/bin/bash", "-c", "echo hello"]

# Múltiples comandos
RUN apt-get update && \
    apt-get install -y \
        curl \
        vim \
        git && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
```

#### COPY vs ADD

```dockerfile
# COPY - preferido para archivos locales
COPY app.py /app/
COPY --chown=appuser:appgroup app.py /app/

# ADD - características adicionales (descomprimir, URLs)
ADD archive.tar.gz /app/
ADD http://example.com/file.txt /app/

# Mejor práctica: usar COPY cuando sea posible
```

#### ENV y ARG

```dockerfile
# ARG - solo en build time
ARG VERSION=1.0
ARG BUILD_DATE

# ENV - disponible en build y runtime
ENV APP_VERSION=${VERSION}
ENV NODE_ENV=production

# Usar en RUN
RUN echo "Building version ${VERSION}"
```

#### WORKDIR

```dockerfile
# Establecer directorio de trabajo
WORKDIR /app

# Crear directorio si no existe
WORKDIR /app/src

# Usar variables
ENV APP_DIR=/myapp
WORKDIR ${APP_DIR}
```

#### USER

```dockerfile
# Cambiar a usuario específico
USER 1000:1000
USER appuser
USER appuser:appgroup

# Crear usuario primero
RUN useradd -m -u 1000 appuser
USER appuser
```

#### EXPOSE

```dockerfile
# Documentar puertos (no los publica)
EXPOSE 8080
EXPOSE 8080/tcp
EXPOSE 53/udp
```

#### VOLUME

```dockerfile
# Declarar puntos de montaje
VOLUME ["/data"]
VOLUME /var/log /var/db

# Los datos en estos directorios persisten
```

#### CMD vs ENTRYPOINT

```dockerfile
# CMD - puede ser sobrescrito
CMD ["python3", "app.py"]
CMD python3 app.py  # shell form

# ENTRYPOINT - punto de entrada fijo
ENTRYPOINT ["python3"]
CMD ["app.py"]  # argumentos por defecto

# ENTRYPOINT + CMD (mejor práctica)
ENTRYPOINT ["/usr/bin/myapp"]
CMD ["--help"]

# Ejecutar: docker run myimage --version
# Ejecuta: /usr/bin/myapp --version
```

### 2.3 Build de Imágenes

#### Build Básico

```bash
# Build en directorio actual
docker build -t mi-app:v1.0 .

# Build con contexto específico
docker build -t mi-app:v1.0 -f docker/Dockerfile .

# Build sin caché
docker build --no-cache -t mi-app:v1.0 .

# Build con argumentos
docker build --build-arg VERSION=2.0 -t mi-app:v2.0 .
```

#### Build con BuildKit

```bash
# Habilitar BuildKit
export DOCKER_BUILDKIT=1

# O por comando
DOCKER_BUILDKIT=1 docker build -t mi-app:v1.0 .

# Características de BuildKit:
# - Builds paralelos
# - Cache inteligente
# - Secrets seguros
# - SSH forwarding
```

#### Build Context

```bash
# .dockerignore - excluir archivos del contexto
cat > .dockerignore << EOF
node_modules
.git
*.log
.env
tmp/
EOF

# Ver tamaño del contexto
docker build --no-cache --progress=plain -t test .
```

### 2.4 Multi-Stage Builds

#### Ejemplo: Aplicación Go

```dockerfile
# Etapa 1: Build
FROM golang:1.21-alpine AS builder

WORKDIR /build

# Dependencias
COPY go.mod go.sum ./
RUN go mod download

# Build
COPY . .
RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o app .

# Etapa 2: Runtime
FROM alpine:3.18

RUN apk --no-cache add ca-certificates

WORKDIR /root/

# Copiar binario desde etapa builder
COPY --from=builder /build/app .

CMD ["./app"]
```

#### Ejemplo: Aplicación Node.js

```dockerfile
# Etapa 1: Dependencias
FROM node:18-alpine AS dependencies
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production

# Etapa 2: Build
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Etapa 3: Runtime
FROM node:18-alpine
WORKDIR /app

# Copiar dependencias de producción
COPY --from=dependencies /app/node_modules ./node_modules

# Copiar artefactos de build
COPY --from=builder /app/dist ./dist

# Copiar configuración
COPY package*.json ./

USER node
CMD ["node", "dist/index.js"]
```

### 2.5 Tagging y Versionado

```bash
# Tag simple
docker tag mi-app:v1.0 mi-app:latest

# Tag para registry
docker tag mi-app:v1.0 registry.example.com/mi-app:v1.0

# Múltiples tags
docker tag mi-app:v1.0 mi-app:1.0.5
docker tag mi-app:v1.0 mi-app:1.0
docker tag mi-app:v1.0 mi-app:1
docker tag mi-app:v1.0 mi-app:latest
```

**Estrategias de versionado:**

```bash
# Semantic versioning
mi-app:1.2.3
mi-app:1.2
mi-app:1
mi-app:latest

# Git commit SHA
mi-app:abc123def456

# Fecha
mi-app:2025-01-15

# Entorno
mi-app:production
mi-app:staging
mi-app:development
```

### 2.6 Publicar Imágenes

#### Docker Hub

```bash
# Login
docker login

# Tag con usuario
docker tag mi-app:v1.0 miusuario/mi-app:v1.0

# Push
docker push miusuario/mi-app:v1.0

# Logout
docker logout
```

#### Registry Privado

```bash
# Login a registry privado
docker login registry.example.com

# Tag
docker tag mi-app:v1.0 registry.example.com/proyecto/mi-app:v1.0

# Push
docker push registry.example.com/proyecto/mi-app:v1.0
```

## 3. Contenedores

### 3.1 Ejecutar Contenedores

#### Modo Básico

```bash
# Ejecutar y eliminar al salir
docker run --rm alpine echo "Hola Mundo"

# Ejecutar en segundo plano
docker run -d nginx

# Ejecutar con nombre
docker run -d --name mi-nginx nginx

# Ejecutar interactivamente
docker run -it ubuntu bash

# Ejecutar con TTY
docker run -t ubuntu
```

#### Mapeo de Puertos

```bash
# Publicar puerto específico
docker run -d -p 8080:80 nginx

# Publicar en interfaz específica
docker run -d -p 127.0.0.1:8080:80 nginx

# Publicar puerto aleatorio
docker run -d -P nginx

# Múltiples puertos
docker run -d -p 80:80 -p 443:443 nginx
```

#### Volúmenes y Montajes

```bash
# Volumen anónimo
docker run -d -v /app/data nginx

# Volumen nombrado
docker run -d -v mi-volumen:/app/data nginx

# Bind mount (directorio host)
docker run -d -v /home/user/data:/app/data nginx

# Mount con opciones
docker run -d \
  --mount type=bind,source=/host/data,target=/app/data,readonly \
  nginx

# tmpfs (memoria)
docker run -d --tmpfs /app/temp nginx
```

#### Variables de Entorno

```bash
# Variable simple
docker run -e APP_ENV=production mi-app

# Múltiples variables
docker run \
  -e DB_HOST=localhost \
  -e DB_PORT=5432 \
  -e DB_NAME=mydb \
  mi-app

# Desde archivo
docker run --env-file .env mi-app

# Archivo .env
cat > .env << EOF
DB_HOST=localhost
DB_PORT=5432
DB_NAME=mydb
EOF
```

#### Networking

```bash
# Red por defecto (bridge)
docker run -d nginx

# Red específica
docker network create mi-red
docker run -d --network mi-red nginx

# Red del host
docker run -d --network host nginx

# Sin red
docker run -d --network none nginx

# Alias de red
docker run -d --network mi-red --network-alias web nginx
```

#### Límites de Recursos

```bash
# Limitar memoria
docker run -d --memory="512m" nginx

# Limitar CPU
docker run -d --cpus="1.5" nginx

# Limitar PIDs
docker run -d --pids-limit=100 nginx

# Limitar I/O
docker run -d \
  --device-read-bps /dev/sda:1mb \
  --device-write-bps /dev/sda:1mb \
  nginx
```

#### Reinicio Automático

```bash
# No reiniciar (por defecto)
docker run -d --restart no nginx

# Siempre reiniciar
docker run -d --restart always nginx

# Reiniciar en caso de fallo
docker run -d --restart on-failure nginx

# Reiniciar con límite de intentos
docker run -d --restart on-failure:5 nginx

# Reiniciar a menos que se detenga manualmente
docker run -d --restart unless-stopped nginx
```

### 3.2 Gestión de Contenedores

#### Listar Contenedores

```bash
# Listar contenedores en ejecución
docker ps
docker container ls

# Listar todos (incluso detenidos)
docker ps -a
docker container ls -a

# Listar últimos N contenedores
docker ps -n 5

# Listar IDs solamente
docker ps -q

# Con filtros
docker ps --filter "status=running"
docker ps --filter "name=nginx"
docker ps --filter "ancestor=nginx:alpine"
```

#### Controlar Contenedores

```bash
# Detener contenedor
docker stop mi-contenedor
docker stop abc123def456

# Detener con timeout
docker stop -t 30 mi-contenedor

# Iniciar contenedor detenido
docker start mi-contenedor

# Reiniciar contenedor
docker restart mi-contenedor

# Pausar (congelar) contenedor
docker pause mi-contenedor

# Reanudar contenedor pausado
docker unpause mi-contenedor

# Matar contenedor (SIGKILL)
docker kill mi-contenedor

# Enviar señal específica
docker kill --signal=SIGHUP mi-contenedor
```

#### Inspeccionar Contenedores

```bash
# Información completa
docker inspect mi-contenedor

# Filtrar información específica
docker inspect -f '{{.State.Status}}' mi-contenedor
docker inspect -f '{{.NetworkSettings.IPAddress}}' mi-contenedor

# Logs del contenedor
docker logs mi-contenedor

# Logs en tiempo real
docker logs -f mi-contenedor

# Últimas N líneas
docker logs --tail 100 mi-contenedor

# Logs con timestamps
docker logs -t mi-contenedor

# Estadísticas en tiempo real
docker stats

# Estadísticas de contenedor específico
docker stats mi-contenedor

# Top (procesos del contenedor)
docker top mi-contenedor

# Eventos del contenedor
docker events --filter 'container=mi-contenedor'
```

#### Ejecutar Comandos en Contenedores

```bash
# Ejecutar comando en contenedor en ejecución
docker exec mi-contenedor ls -la

# Ejecutar interactivamente
docker exec -it mi-contenedor bash

# Como usuario específico
docker exec -u root mi-contenedor whoami

# Con variables de entorno
docker exec -e VAR=value mi-contenedor env

# Con directorio de trabajo
docker exec -w /app mi-contenedor pwd
```

#### Copiar Archivos

```bash
# Del host al contenedor
docker cp archivo.txt mi-contenedor:/app/

# Del contenedor al host
docker cp mi-contenedor:/app/logs/app.log ./

# Copiar directorio completo
docker cp mi-contenedor:/app/data ./data

# Con preservación de permisos
docker cp -a mi-contenedor:/app/config ./config
```

#### Exportar e Importar

```bash
# Exportar contenedor como tar
docker export mi-contenedor > contenedor.tar

# Importar tar como imagen
docker import contenedor.tar mi-imagen:v1.0

# Guardar imagen como tar
docker save mi-app:v1.0 > imagen.tar

# Cargar imagen desde tar
docker load < imagen.tar
```

### 3.3 Eliminar Contenedores

```bash
# Eliminar contenedor detenido
docker rm mi-contenedor

# Forzar eliminación (contenedor en ejecución)
docker rm -f mi-contenedor

# Eliminar múltiples contenedores
docker rm contenedor1 contenedor2 contenedor3

# Eliminar todos los contenedores detenidos
docker container prune

# Eliminar con filtro
docker container prune --filter "until=24h"

# Eliminar todos los contenedores (¡peligroso!)
docker rm -f $(docker ps -aq)
```

## 4. Volúmenes

### 4.1 Gestión de Volúmenes

```bash
# Crear volumen
docker volume create mi-volumen

# Crear con driver específico
docker volume create --driver local mi-volumen

# Listar volúmenes
docker volume ls

# Inspeccionar volumen
docker volume inspect mi-volumen

# Eliminar volumen
docker volume rm mi-volumen

# Eliminar volúmenes sin usar
docker volume prune
```

### 4.2 Usar Volúmenes

```bash
# Montar volumen en contenedor
docker run -d -v mi-volumen:/app/data nginx

# Volumen de solo lectura
docker run -d -v mi-volumen:/app/data:ro nginx

# Backup de volumen
docker run --rm \
  -v mi-volumen:/source \
  -v $(pwd):/backup \
  alpine tar czf /backup/backup.tar.gz -C /source .

# Restaurar volumen
docker run --rm \
  -v mi-volumen:/target \
  -v $(pwd):/backup \
  alpine tar xzf /backup/backup.tar.gz -C /target
```

## 5. Redes

### 5.1 Gestión de Redes

```bash
# Listar redes
docker network ls

# Crear red
docker network create mi-red

# Crear red con subred específica
docker network create --subnet=172.18.0.0/16 mi-red

# Inspeccionar red
docker network inspect mi-red

# Conectar contenedor a red
docker network connect mi-red mi-contenedor

# Desconectar contenedor de red
docker network disconnect mi-red mi-contenedor

# Eliminar red
docker network rm mi-red

# Eliminar redes sin usar
docker network prune
```

### 5.2 Tipos de Redes

```bash
# Bridge (por defecto)
docker network create --driver bridge mi-bridge

# Host (compartir red con host)
docker run -d --network host nginx

# Overlay (para Docker Swarm)
docker network create --driver overlay mi-overlay

# Macvlan (dirección MAC propia)
docker network create -d macvlan \
  --subnet=192.168.1.0/24 \
  --gateway=192.168.1.1 \
  -o parent=eth0 \
  mi-macvlan
```

## 6. Mejores Prácticas

### 6.1 Dockerfile

- Usar imágenes base oficiales
- Minimizar número de capas
- Usar multi-stage builds
- No ejecutar como root
- Usar .dockerignore
- Escanear vulnerabilidades
- Versionar imágenes correctamente

### 6.2 Contenedores

- Un proceso por contenedor
- Contenedores efímeros (stateless)
- Logs a stdout/stderr
- Usar health checks
- Configurar límites de recursos
- Usar restart policies apropiadas

### 6.3 Seguridad

- No incluir secretos en imágenes
- Usar imágenes de fuentes confiables
- Mantener imágenes actualizadas
- Ejecutar como usuario no privilegiado
- Limitar capabilities
- Usar read-only root filesystem cuando sea posible

## Referencias

- Docker CLI Reference: https://docs.docker.com/engine/reference/commandline/cli/
- Dockerfile Reference: https://docs.docker.com/engine/reference/builder/
- Best Practices: https://docs.docker.com/develop/dev-best-practices/
