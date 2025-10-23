# Tema 1: Evolución de los Servicios Web

## Introducción

Este documento describe la evolución de los servicios web desde los servidores físicos tradicionales hasta la adopción generalizada de contenedores en la actualidad.

## 1. Servidores Físicos (Década de 1990 - 2000)

### Características
- Hardware dedicado para cada aplicación o servicio
- Instalación directa del sistema operativo en el hardware
- Recursos fijos y no compartidos
- Tiempo de aprovisionamiento: días o semanas

### Ventajas
- Control total sobre el hardware
- Máximo rendimiento para aplicaciones específicas
- Aislamiento físico completo

### Desventajas
- Baja utilización de recursos (típicamente 5-15%)
- Alto coste de adquisición y mantenimiento
- Escalado lento y costoso
- Desperdicio de energía y espacio
- Dificultad para la recuperación ante desastres

## 2. Virtualización (Década de 2000 - 2010)

### Características
- Múltiples máquinas virtuales (VMs) en un mismo servidor físico
- Hipervisores (VMware, Hyper-V, KVM, Xen)
- Cada VM ejecuta su propio sistema operativo completo
- Tiempo de aprovisionamiento: minutos a horas

### Ventajas
- Mejor utilización de recursos (60-80%)
- Consolidación de servidores
- Aislamiento entre aplicaciones
- Facilita migración y backup
- Mejora la recuperación ante desastres

### Desventajas
- Overhead del sistema operativo en cada VM
- Mayor consumo de recursos (CPU, RAM, almacenamiento)
- Tiempo de arranque relativamente lento
- Gestión compleja de múltiples sistemas operativos

## 3. Cloud Computing (Década de 2010)

### Características
- Infraestructura como Servicio (IaaS)
- Plataforma como Servicio (PaaS)
- Software como Servicio (SaaS)
- Proveedores: AWS, Azure, Google Cloud, etc.

### Ventajas
- Pago por uso
- Escalado rápido y automático
- Alcance global
- Reducción de costes de capital

### Desventajas
- Dependencia de proveedores
- Costes variables y potencialmente altos
- Preocupaciones de seguridad y privacidad
- Latencia de red

## 4. Contenedores (2013 - Actualidad)

### Características
- Compartición del kernel del sistema operativo host
- Aislamiento a nivel de proceso
- Incluyen aplicación y todas sus dependencias
- Tiempo de arranque: segundos
- Docker (2013) populariza la tecnología

### Ventajas
- Ligeros comparados con VMs (MBs vs GBs)
- Arranque casi instantáneo
- Alta densidad de despliegue
- Portabilidad entre entornos
- Consistencia del entorno de desarrollo a producción
- Eficiencia en el uso de recursos

### Desventajas
- Menor aislamiento que VMs
- Comparten el kernel con el host
- Complejidad en la orquestación a escala

## 5. Adopción Generalizada de Contenedores

### Factores que Impulsaron la Adopción

#### 5.1 Estandarización
- **Docker** (2013): Simplificó la creación y gestión de contenedores
- **Open Container Initiative (OCI)**: Estándares para runtime y formato de imágenes
- **Kubernetes** (2014): Orquestación de contenedores a escala

#### 5.2 DevOps y CI/CD
- Integración perfecta en pipelines de CI/CD
- "Build once, run anywhere"
- Entornos reproducibles
- Facilita la cultura DevOps

#### 5.3 Microservicios
- Arquitectura ideal para contenedores
- Cada servicio en su propio contenedor
- Escalado independiente
- Despliegue y actualización independiente

#### 5.4 Ecosistema y Herramientas
- Registros de imágenes (Docker Hub, Harbor, etc.)
- Orquestadores (Kubernetes, Docker Swarm, Nomad)
- Monitorización y logging
- Redes y almacenamiento para contenedores

### Estado Actual

#### Estadísticas de Adopción
- Más del 75% de las organizaciones usan contenedores en producción
- Kubernetes es el orquestador dominante
- Todos los principales proveedores cloud ofrecen servicios gestionados de contenedores

#### Casos de Uso Principales
- Aplicaciones web y APIs
- Microservicios
- Procesamiento por lotes
- Machine Learning y AI
- Aplicaciones sin servidor (serverless)

## 6. Comparación de Tecnologías

| Aspecto | Servidor Físico | Máquina Virtual | Contenedor |
|---------|----------------|-----------------|------------|
| Tiempo de arranque | Minutos | Minutos | Segundos |
| Tamaño | N/A | GBs | MBs |
| Aislamiento | Hardware | SO Completo | Proceso |
| Portabilidad | Baja | Media | Alta |
| Eficiencia de recursos | Baja (5-15%) | Media (60-80%) | Alta (90%+) |
| Densidad | Baja | Media | Alta |
| Overhead | Ninguno | Alto | Mínimo |

## 7. Tendencias Futuras

### Contenedores sin Servidor (Serverless Containers)
- AWS Fargate, Azure Container Instances, Google Cloud Run
- Eliminación de la gestión de infraestructura
- Pago por uso real

### Contenedores en el Edge
- IoT y edge computing
- Contenedores ligeros (Alpine Linux)
- Kubernetes en el edge (K3s, MicroK8s)

### WebAssembly (WASM)
- Complemento o posible sucesor de contenedores
- Mayor rendimiento y seguridad
- Portabilidad completa entre plataformas

### Seguridad Mejorada
- Contenedores rootless
- gVisor, Kata Containers (mayor aislamiento)
- Firmas y verificación de imágenes

## Referencias

- Docker Documentation: https://docs.docker.com/
- Kubernetes Documentation: https://kubernetes.io/docs/
- Open Container Initiative: https://opencontainers.org/
- CNCF Cloud Native Landscape: https://landscape.cncf.io/
