# Tema 4: Arquitectura de Docker

## Introducción

Docker es una plataforma para desarrollar, enviar y ejecutar aplicaciones en contenedores. Este documento explora la arquitectura de Docker, sus componentes clave y las decisiones de diseño que permiten construir contenedores ligeros, seguros y escalables.

## 1. Visión General de la Arquitectura

### 1.1 Arquitectura Cliente-Servidor

Docker utiliza una arquitectura cliente-servidor:

```
┌─────────────┐         ┌──────────────────┐
│   Cliente   │◄────────┤   Docker Host    │
│   Docker    │  REST   │                  │
└─────────────┘   API   │  ┌────────────┐  │
                        │  │   Daemon   │  │
                        │  │  (dockerd) │  │
                        │  └────────────┘  │
                        │         │        │
                        │         ▼        │
                        │  ┌────────────┐  │
                        │  │ containerd │  │
                        │  └────────────┘  │
                        │         │        │
                        │         ▼        │
                        │  ┌────────────┐  │
                        │  │    runc    │  │
                        │  └────────────┘  │
                        └──────────────────┘

┌─────────────────────────────────┐
│      Registry (Docker Hub)       │
│   - Almacena imágenes           │
│   - Distribución de imágenes    │
└─────────────────────────────────┘
```

## 2. Componentes Principales

### 2.1 Docker Client (docker)

El cliente de Docker es la interfaz principal para interactuar con Docker.

#### Características
- Interfaz de línea de comandos (CLI)
- Envía comandos al daemon Docker mediante REST API
- Puede comunicarse con múltiples daemons
- Comandos: `docker run`, `docker build`, `docker pull`, etc.

#### Ejemplo de Uso

```bash
# El cliente envía este comando al daemon
docker run -d -p 80:80 nginx

# Proceso interno:
# 1. Cliente parsea el comando
# 2. Envía petición REST al daemon
# 3. Daemon procesa la petición
# 4. Daemon responde al cliente
# 5. Cliente muestra el resultado
```

#### Comunicación

```bash
# Local via socket Unix
docker -H unix:///var/run/docker.sock ps

# Remoto via TCP
docker -H tcp://192.168.1.10:2375 ps

# Remoto via TLS (seguro)
docker -H tcp://192.168.1.10:2376 --tlsverify ps
```

### 2.2 Docker Daemon (dockerd)

El daemon es el corazón de Docker, responsable de gestionar objetos Docker.

#### Responsabilidades

1. **Gestión de Imágenes**
   - Construcción de imágenes
   - Pull/push desde/hacia registries
   - Almacenamiento local
   - Gestión de capas

2. **Gestión de Contenedores**
   - Crear y eliminar contenedores
   - Iniciar y detener contenedores
   - Delegar ejecución a containerd

3. **Gestión de Redes**
   - Crear redes virtuales
   - Asignar IPs a contenedores
   - Configurar bridges y routing

4. **Gestión de Volúmenes**
   - Crear y eliminar volúmenes
   - Montar volúmenes en contenedores
   - Gestionar drivers de almacenamiento

5. **API REST**
   - Exponer API para clientes
   - Autenticación y autorización
   - Validación de peticiones

#### Configuración

```json
// /etc/docker/daemon.json
{
  "storage-driver": "overlay2",
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  },
  "default-runtime": "runc",
  "runtimes": {
    "nvidia": {
      "path": "/usr/bin/nvidia-container-runtime"
    }
  }
}
```

### 2.3 containerd

containerd es un runtime de contenedores de alto nivel, disponible como daemon separado.

#### Historia
- Extraído de Docker en 2016
- Donado a CNCF (Cloud Native Computing Foundation)
- Utilizado por Docker, Kubernetes (vía CRI)

#### Responsabilidades

1. **Gestión del Ciclo de Vida**
   - Crear, iniciar, detener contenedores
   - Pausar y reanudar contenedores
   - Snapshots de contenedores

2. **Gestión de Imágenes**
   - Pull de imágenes
   - Descomprimir y desempaquetar
   - Gestión de sistema de archivos por capas

3. **Ejecución de Contenedores**
   - Delega a runc (u otro runtime OCI)
   - Supervisa procesos de contenedores
   - Recolecta métricas

4. **Plugins**
   - Snapshotter (sistema de archivos)
   - Content (almacenamiento de contenido)
   - Differ (diferencias entre capas)

#### Arquitectura de containerd

```
┌─────────────────────────────────┐
│        Docker Daemon            │
└────────────┬────────────────────┘
             │ gRPC API
             ▼
┌─────────────────────────────────┐
│         containerd              │
│  ┌─────────────────────────┐   │
│  │   Container Lifecycle   │   │
│  └─────────────────────────┘   │
│  ┌─────────────────────────┐   │
│  │    Image Management     │   │
│  └─────────────────────────┘   │
│  ┌─────────────────────────┐   │
│  │    Storage (Snapshots)  │   │
│  └─────────────────────────┘   │
└────────────┬────────────────────┘
             │
             ▼
      ┌────────────┐
      │    runc    │
      └────────────┘
```

#### Comandos ctr

containerd incluye la herramienta `ctr`:

```bash
# Listar contenedores
sudo ctr containers list

# Listar imágenes
sudo ctr images list

# Ejecutar contenedor
sudo ctr run docker.io/library/alpine:latest my-container

# Ver tareas (contenedores en ejecución)
sudo ctr tasks list
```

### 2.4 runc

runc es el runtime de bajo nivel que ejecuta contenedores según el estándar OCI.

#### Características

- **Runtime OCI**: Implementación de referencia de OCI runtime spec
- **Ligero**: ~10MB, escrito en Go
- **Sin daemon**: Ejecuta y sale
- **Bajo nivel**: Interactúa directamente con el kernel

#### Proceso de Ejecución

1. Lee el archivo `config.json` (OCI bundle)
2. Crea los namespaces necesarios
3. Configura cgroups
4. Aplica perfiles de seguridad (seccomp, AppArmor)
5. Ejecuta el proceso del contenedor
6. Monitorea el proceso

#### Estructura de un OCI Bundle

```
my-container/
├── config.json      # Configuración del contenedor
└── rootfs/          # Sistema de archivos del contenedor
    ├── bin/
    ├── etc/
    ├── lib/
    └── ...
```

#### config.json Ejemplo

```json
{
  "ociVersion": "1.0.0",
  "process": {
    "terminal": true,
    "user": {
      "uid": 0,
      "gid": 0
    },
    "args": ["/bin/sh"],
    "env": ["PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"],
    "cwd": "/"
  },
  "root": {
    "path": "rootfs",
    "readonly": false
  },
  "hostname": "my-container",
  "mounts": [...],
  "linux": {
    "namespaces": [
      {"type": "pid"},
      {"type": "network"},
      {"type": "ipc"},
      {"type": "uts"},
      {"type": "mount"}
    ],
    "resources": {
      "memory": {
        "limit": 536870912
      },
      "cpu": {
        "quota": 100000,
        "period": 100000
      }
    }
  }
}
```

#### Uso Directo de runc

```bash
# Crear un bundle OCI
mkdir -p /tmp/mycontainer/rootfs
docker export $(docker create alpine) | tar -C /tmp/mycontainer/rootfs -xf -

# Generar config.json
cd /tmp/mycontainer
runc spec

# Ejecutar contenedor
sudo runc run mycontainer

# Listar contenedores
sudo runc list
```

### 2.5 shim

El shim es un proceso intermedio entre containerd y runc.

#### Propósito

1. **Desacoplamiento**: Permite que containerd reinicie sin afectar contenedores
2. **Gestión de stdio**: Maneja stdin/stdout/stderr del contenedor
3. **Reaping**: Recoge procesos zombie
4. **Estado**: Mantiene información de estado del contenedor

#### Arquitectura con shim

```
containerd
    │
    └── containerd-shim
            │
            └── runc
                    │
                    └── Proceso del contenedor
```

Cuando un contenedor se inicia:
1. containerd crea un shim
2. shim ejecuta runc
3. runc configura el contenedor y sale
4. shim supervisa el proceso del contenedor

### 2.6 Registry (Registro de Imágenes)

El registry almacena y distribuye imágenes Docker.

#### Docker Hub

- Registry público por defecto
- Millones de imágenes
- Imágenes oficiales y de la comunidad
- Planes gratuitos y de pago

```bash
# Pull desde Docker Hub
docker pull nginx

# Push a Docker Hub
docker tag mi-app mi-usuario/mi-app:v1.0
docker push mi-usuario/mi-app:v1.0
```

#### Registry Privado

```bash
# Ejecutar registry local
docker run -d -p 5000:5000 --name registry registry:2

# Tag y push a registry local
docker tag mi-app localhost:5000/mi-app:v1.0
docker push localhost:5000/mi-app:v1.0

# Pull desde registry local
docker pull localhost:5000/mi-app:v1.0
```

#### Otros Registries

- **Amazon ECR** (Elastic Container Registry)
- **Google Container Registry** (GCR)
- **Azure Container Registry** (ACR)
- **Harbor** (CNCF, open source)
- **Quay** (Red Hat)
- **GitLab Container Registry**

## 3. Decisiones de Diseño para Contenedores Ligeros

### 3.1 Sistema de Archivos por Capas

#### Union Filesystem

Docker utiliza sistemas de archivos en capas (overlay2, aufs, btrfs):

```
┌─────────────────────────┐
│   Capa de Contenedor    │ ← Read/Write
├─────────────────────────┤
│   Capa de Aplicación    │ ← Read-Only
├─────────────────────────┤
│   Capa de Dependencias  │ ← Read-Only
├─────────────────────────┤
│   Capa Base (Alpine)    │ ← Read-Only
└─────────────────────────┘
```

#### Ventajas

1. **Reutilización**: Las capas se comparten entre imágenes
2. **Eficiencia**: Solo se descargan capas nuevas
3. **Versionado**: Fácil rollback a versiones anteriores
4. **Tamaño**: Reduce espacio en disco

#### Copy-on-Write (CoW)

- Capas de imagen: read-only
- Capa de contenedor: read-write
- Al modificar un archivo, se copia a la capa superior

```bash
# Ver capas de una imagen
docker history nginx:alpine

# Inspeccionar capas
docker inspect nginx:alpine | jq '.[0].RootFS.Layers'
```

### 3.2 Imágenes Base Ligeras

#### Alpine Linux

- ~5MB de tamaño base
- Usa musl libc en lugar de glibc
- Gestor de paquetes apk
- Ideal para producción

```dockerfile
FROM alpine:3.18
RUN apk add --no-cache python3
```

#### Distroless

- Solo runtime, sin shell ni utilidades
- Máxima seguridad
- Mínima superficie de ataque

```dockerfile
FROM gcr.io/distroless/python3
COPY app.py /
CMD ["/app.py"]
```

#### Scratch

- Imagen vacía
- Para binarios estáticos
- ~0 bytes

```dockerfile
FROM scratch
COPY my-static-binary /
CMD ["/my-static-binary"]
```

### 3.3 Multi-Stage Builds

Permite imágenes de producción pequeñas sin dependencias de compilación.

```dockerfile
# Etapa 1: Compilación
FROM golang:1.21 AS builder
WORKDIR /app
COPY . .
RUN CGO_ENABLED=0 go build -o myapp

# Etapa 2: Producción
FROM alpine:3.18
COPY --from=builder /app/myapp /
CMD ["/myapp"]
```

**Beneficios:**
- Imagen final: ~10MB (vs ~800MB con Go incluido)
- Sin herramientas de compilación en producción
- Mayor seguridad

### 3.4 Optimización de Capas

#### Malas Prácticas

```dockerfile
# Crea muchas capas innecesarias
RUN apt-get update
RUN apt-get install -y python3
RUN apt-get clean
RUN rm -rf /var/lib/apt/lists/*
```

#### Buenas Prácticas

```dockerfile
# Una sola capa optimizada
RUN apt-get update && \
    apt-get install -y python3 && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
```

#### Orden de Capas

```dockerfile
# Capas que cambian menos primero
FROM node:18-alpine
WORKDIR /app

# Dependencias (cambian raramente)
COPY package*.json ./
RUN npm ci --only=production

# Código (cambia frecuentemente)
COPY . .

CMD ["node", "index.js"]
```

## 4. Decisiones de Diseño para Seguridad

### 4.1 Aislamiento por Defecto

- **Namespaces**: Activados automáticamente
- **Cgroups**: Límites de recursos configurables
- **Capabilities**: Conjunto mínimo por defecto
- **Seccomp**: Perfil de seguridad aplicado

### 4.2 Rootless Mode

```bash
# Instalar Docker en modo rootless
dockerd-rootless-setuptool.sh install

# Ejecutar contenedor sin ser root
docker run -d nginx
```

### 4.3 Content Trust

Firma y verificación de imágenes:

```bash
# Habilitar content trust
export DOCKER_CONTENT_TRUST=1

# Push firmará automáticamente
docker push mi-usuario/mi-app:v1.0

# Pull verificará la firma
docker pull mi-usuario/mi-app:v1.0
```

### 4.4 Usuario No Privilegiado

```dockerfile
FROM alpine:3.18
RUN addgroup -g 1000 appgroup && \
    adduser -D -u 1000 -G appgroup appuser
USER appuser
COPY app /home/appuser/
CMD ["/home/appuser/app"]
```

## 5. Decisiones de Diseño para Escalabilidad

### 5.1 Stateless Containers

- Contenedores sin estado
- Estado en volúmenes o bases de datos externas
- Facilita escalado horizontal

### 5.2 Health Checks

```dockerfile
FROM nginx:alpine
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget --quiet --tries=1 --spider http://localhost/ || exit 1
```

### 5.3 Resource Limits

```bash
docker run -d \
  --memory="512m" \
  --cpus="1.0" \
  --restart=unless-stopped \
  nginx
```

### 5.4 Networking Modes

- **Bridge**: Red privada por defecto
- **Host**: Comparte red con el host (mayor rendimiento)
- **Overlay**: Para Docker Swarm (multi-host)
- **Macvlan**: Asigna MAC address al contenedor

## 6. Flujo Completo: De Dockerfile a Contenedor en Ejecución

### Paso 1: Build de Imagen

```bash
docker build -t mi-app:v1.0 .
```

**Proceso interno:**
1. Cliente envía Dockerfile y contexto a dockerd
2. dockerd parsea Dockerfile
3. Para cada instrucción:
   - Crea contenedor temporal
   - Ejecuta la instrucción
   - Commit de cambios como nueva capa
4. Etiqueta la imagen final

### Paso 2: Push a Registry

```bash
docker push mi-app:v1.0
```

**Proceso interno:**
1. dockerd comprime las capas
2. Envía capas al registry (solo las nuevas)
3. Registry almacena y genera manifest

### Paso 3: Pull de Imagen

```bash
docker pull mi-app:v1.0
```

**Proceso interno:**
1. dockerd descarga manifest
2. Verifica capas locales existentes
3. Descarga solo capas faltantes
4. Extrae y almacena capas

### Paso 4: Run de Contenedor

```bash
docker run -d --name app mi-app:v1.0
```

**Proceso interno:**
1. **dockerd**:
   - Valida imagen y parámetros
   - Crea configuración de contenedor
   - Llama a containerd vía gRPC

2. **containerd**:
   - Prepara el bundle OCI
   - Configura snapshots del filesystem
   - Inicia shim

3. **shim**:
   - Ejecuta runc
   - Supervisa el proceso

4. **runc**:
   - Crea namespaces
   - Configura cgroups
   - Aplica seguridad (seccomp, capabilities)
   - Ejecuta el proceso del contenedor
   - Sale (shim continúa supervisando)

## 7. Comparación con Otras Soluciones

| Aspecto | Docker | Podman | LXC | systemd-nspawn |
|---------|--------|--------|-----|----------------|
| Daemon | Sí | No | Sí | No |
| Root requerido | Opcional | No | Sí | Sí |
| OCI compatible | Sí | Sí | No | Parcial |
| Registry | Docker Hub | Cualquiera | N/A | N/A |
| Orquestación | Swarm | Pods | No | No |
| Madurez | Alta | Media | Alta | Media |

## Referencias

- Docker Architecture: https://docs.docker.com/get-started/overview/
- containerd: https://containerd.io/
- runc: https://github.com/opencontainers/runc
- OCI Specifications: https://opencontainers.org/
- Docker Registry: https://docs.docker.com/registry/
