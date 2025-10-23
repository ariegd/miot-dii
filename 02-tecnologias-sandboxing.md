# Tema 2: Tecnologías para el Sandboxing de Contenedores

## Introducción

El sandboxing (aislamiento) es fundamental para la seguridad y estabilidad de los contenedores. Este documento explora las tecnologías que permiten el aislamiento de contenedores en sistemas Linux.

## 1. Conceptos Fundamentales

### ¿Qué es el Sandboxing?
El sandboxing es el proceso de aislar procesos y recursos para que las aplicaciones ejecutándose en contenedores:
- No puedan interferir entre sí
- Tengan acceso limitado a recursos del sistema
- No puedan comprometer la seguridad del host
- Aparezcan como sistemas independientes

## 2. Namespaces de Linux

Los namespaces proporcionan aislamiento a nivel de recursos del sistema, permitiendo que diferentes procesos tengan vistas diferentes del sistema.

### 2.1 Tipos de Namespaces

#### PID Namespace (Process ID)
- **Función**: Aísla el árbol de procesos
- **Efecto**: Los procesos en el contenedor tienen sus propios PIDs
- **Beneficio**: El proceso principal en el contenedor tiene PID 1, como si fuera el único sistema

```bash
# Ejemplo: Ver procesos desde dentro de un contenedor
docker run -it alpine ps aux
# Solo muestra procesos del contenedor
```

#### Network Namespace
- **Función**: Aísla la red (interfaces, tablas de rutas, reglas de firewall)
- **Efecto**: Cada contenedor tiene su propia pila de red
- **Beneficio**: Los contenedores pueden tener sus propias configuraciones de red sin conflictos

```bash
# Contenedor con su propia interfaz de red
docker run -it alpine ip addr show
```

#### Mount Namespace
- **Función**: Aísla puntos de montaje del sistema de archivos
- **Efecto**: Los contenedores tienen su propia jerarquía de sistema de archivos
- **Beneficio**: Cambios en montajes no afectan al host o a otros contenedores

#### UTS Namespace (Unix Timesharing System)
- **Función**: Aísla hostname y domain name
- **Efecto**: Cada contenedor puede tener su propio hostname
- **Beneficio**: Identificación única de contenedores

```bash
docker run -it --hostname mi-contenedor alpine hostname
# Muestra: mi-contenedor
```

#### IPC Namespace (Inter-Process Communication)
- **Función**: Aísla recursos de comunicación entre procesos (colas de mensajes, semáforos)
- **Efecto**: Los contenedores no pueden comunicarse vía IPC a menos que se configure
- **Beneficio**: Mayor seguridad y aislamiento

#### User Namespace
- **Función**: Aísla IDs de usuarios y grupos
- **Efecto**: Un usuario root en el contenedor puede ser un usuario sin privilegios en el host
- **Beneficio**: Seguridad mejorada mediante mapeo de UIDs

```bash
# Contenedor con mapeo de usuarios
docker run --user 1000:1000 alpine whoami
```

#### Cgroup Namespace
- **Función**: Aísla la vista de cgroups
- **Efecto**: Los contenedores solo ven sus propios cgroups
- **Beneficio**: Oculta información del host

## 3. Control Groups (cgroups)

Los cgroups limitan y monitorizan el uso de recursos de los contenedores.

### 3.1 Tipos de Recursos Controlados

#### CPU
- Limita el uso de CPU
- Puede establecer prioridades
- Configura cuotas de tiempo de CPU

```bash
# Limitar contenedor a 50% de una CPU
docker run -it --cpus="0.5" alpine sh
```

#### Memoria
- Limita el uso de RAM
- Previene que un contenedor consuma toda la memoria
- Puede configurar límites de swap

```bash
# Limitar contenedor a 512MB de RAM
docker run -it --memory="512m" alpine sh
```

#### Block I/O
- Limita lectura/escritura en disco
- Controla IOPS (operaciones por segundo)

```bash
# Limitar velocidad de escritura
docker run -it --device-write-bps /dev/sda:1mb alpine sh
```

#### Network
- Limita ancho de banda de red
- Control de prioridad de tráfico

#### Devices
- Controla acceso a dispositivos
- Whitelist/blacklist de dispositivos

### 3.2 Jerarquía de Cgroups

```
/sys/fs/cgroup/
├── cpu/
│   └── docker/
│       └── <container-id>/
├── memory/
│   └── docker/
│       └── <container-id>/
└── blkio/
    └── docker/
        └── <container-id>/
```

## 4. Capabilities de Linux

Las capabilities dividen los privilegios de root en unidades discretas que pueden otorgarse o denegarse individualmente.

### 4.1 Capabilities Comunes

- **CAP_CHOWN**: Cambiar propietario de archivos
- **CAP_NET_BIND_SERVICE**: Vincular a puertos < 1024
- **CAP_NET_RAW**: Usar sockets RAW
- **CAP_SYS_ADMIN**: Operaciones administrativas
- **CAP_SYS_CHROOT**: Usar chroot()
- **CAP_KILL**: Enviar señales a procesos

### 4.2 Docker y Capabilities

Por defecto, Docker elimina la mayoría de capabilities peligrosas:

```bash
# Ver capabilities de un contenedor
docker run --rm -it alpine sh -c 'apk add -U libcap; capsh --print'

# Añadir una capability específica
docker run --cap-add=NET_ADMIN alpine ip link add dummy0 type dummy

# Eliminar una capability
docker run --cap-drop=CHOWN alpine chown nobody /tmp
```

## 5. Seccomp (Secure Computing Mode)

Seccomp filtra las llamadas al sistema (syscalls) que un proceso puede realizar.

### 5.1 Funcionamiento

- Crea una lista blanca de syscalls permitidas
- Bloquea syscalls peligrosas por defecto
- Reduce la superficie de ataque

### 5.2 Perfil de Seccomp de Docker

Docker aplica un perfil seccomp por defecto que bloquea ~44 de las ~300 syscalls:

```json
{
  "defaultAction": "SCMP_ACT_ERRNO",
  "syscalls": [
    {
      "names": ["accept", "accept4", "access", "..."],
      "action": "SCMP_ACT_ALLOW"
    }
  ]
}
```

```bash
# Deshabilitar seccomp (no recomendado)
docker run --security-opt seccomp=unconfined alpine sh

# Usar perfil personalizado
docker run --security-opt seccomp=/path/to/profile.json alpine sh
```

## 6. AppArmor y SELinux

Sistemas de control de acceso obligatorio (MAC - Mandatory Access Control).

### 6.1 AppArmor

- Basado en perfiles
- Común en Ubuntu/Debian
- Más fácil de configurar que SELinux

```bash
# Ver perfil AppArmor de un contenedor
docker run --rm alpine cat /proc/self/attr/current

# Usar perfil personalizado
docker run --security-opt apparmor=mi-perfil alpine sh
```

### 6.2 SELinux

- Basado en contextos de seguridad
- Común en RHEL/CentOS/Fedora
- Más granular pero más complejo

```bash
# Ver contexto SELinux
docker run --rm alpine cat /proc/self/attr/current

# Ejecutar con contexto específico
docker run --security-opt label=level:s0:c100,c200 alpine sh
```

## 7. Rootless Containers

Contenedores que no requieren privilegios de root para ejecutarse.

### 7.1 Ventajas

- Mayor seguridad
- Reducción de superficie de ataque
- No requiere daemon con privilegios
- Útil en entornos multi-tenant

### 7.2 Implementación

```bash
# Docker en modo rootless
dockerd-rootless-setuptool.sh install

# Podman (rootless por defecto)
podman run -it alpine sh
```

### 7.3 Limitaciones

- Menor rendimiento en algunos casos
- No todos los recursos disponibles
- Restricciones en networking

## 8. Tecnologías de Sandboxing Avanzadas

### 8.1 gVisor

- Kernel de espacio de usuario para contenedores
- Intercepta syscalls antes de llegar al kernel del host
- Proporciona mayor aislamiento que contenedores tradicionales

```bash
# Ejecutar con gVisor (runsc)
docker run --runtime=runsc alpine sh
```

**Ventajas:**
- Fuerte aislamiento
- Compatible con Docker/Kubernetes

**Desventajas:**
- Overhead de rendimiento (10-20%)
- No todas las syscalls soportadas

### 8.2 Kata Containers

- Contenedores ligeros que se ejecutan en VMs
- Combina velocidad de contenedores con aislamiento de VMs
- Cada contenedor o pod en su propia VM

```bash
# Ejecutar con Kata Containers
docker run --runtime=kata-runtime alpine sh
```

**Ventajas:**
- Aislamiento a nivel de hardware
- Mayor seguridad

**Desventajas:**
- Mayor consumo de recursos que contenedores tradicionales
- Arranque más lento

### 8.3 Firecracker

- MicroVM desarrollada por AWS
- Diseñada para serverless y multi-tenancy
- Arranque en ~125ms

**Características:**
- Extremadamente ligera
- Fuerte aislamiento
- Diseñada para densidad

## 9. Mejores Prácticas de Sandboxing

### 9.1 Principio de Mínimo Privilegio

```bash
# Ejecutar como usuario no-root
docker run --user 1000:1000 alpine sh

# Eliminar capabilities innecesarias
docker run --cap-drop=ALL --cap-add=NET_BIND_SERVICE app

# Usar filesystem de solo lectura
docker run --read-only alpine sh
```

### 9.2 Limitar Recursos

```bash
docker run \
  --memory="512m" \
  --cpus="1.0" \
  --pids-limit=100 \
  alpine sh
```

### 9.3 Networking Seguro

```bash
# Sin acceso a red
docker run --network=none alpine sh

# Red personalizada
docker network create --driver bridge mi-red
docker run --network=mi-red alpine sh
```

### 9.4 Escaneo de Vulnerabilidades

```bash
# Escanear imagen con Trivy
trivy image alpine:latest

# Escanear con Clair
docker run -v /var/run/docker.sock:/var/run/docker.sock \
  arminc/clair-scanner alpine:latest
```

## 10. Comparación de Tecnologías

| Tecnología | Nivel de Aislamiento | Overhead | Complejidad | Uso |
|------------|---------------------|----------|-------------|-----|
| Namespaces | Medio | Mínimo | Baja | Estándar |
| Cgroups | N/A (límites) | Mínimo | Baja | Estándar |
| Capabilities | Medio | Mínimo | Media | Estándar |
| Seccomp | Alto | Mínimo | Media | Estándar |
| AppArmor/SELinux | Alto | Bajo | Alta | Estándar |
| gVisor | Muy Alto | Medio | Media | Seguridad alta |
| Kata Containers | Máximo | Alto | Media | Multi-tenancy |

## Referencias

- Linux Namespaces: https://man7.org/linux/man-pages/man7/namespaces.7.html
- Cgroups Documentation: https://www.kernel.org/doc/Documentation/cgroup-v2.txt
- Docker Security: https://docs.docker.com/engine/security/
- gVisor: https://gvisor.dev/
- Kata Containers: https://katacontainers.io/
