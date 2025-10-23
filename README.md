# miot-dii
Diseño de Infraestructura Inteligente para IOT (DII) - Curso 2025-2026

## Contenedores Docker y Arquitecturas Cloud

Este repositorio contiene material educativo sobre contenedores Docker, desde conceptos fundamentales hasta su integración en plataformas cloud.

## Contenido del Curso

### [Tema 1: Evolución de los Servicios Web](01-evolucion-servicios-web.md)
Recorrido histórico desde servidores físicos hasta la adopción generalizada de contenedores.

**Temas cubiertos:**
- Servidores físicos y sus limitaciones
- Virtualización y sus beneficios
- Cloud computing (IaaS, PaaS, SaaS)
- Contenedores: características y ventajas
- Factores que impulsaron la adopción de contenedores
- Comparación de tecnologías
- Tendencias futuras

### [Tema 2: Tecnologías para el Sandboxing de Contenedores](02-tecnologias-sandboxing.md)
Exploración en profundidad de las tecnologías que permiten el aislamiento y seguridad de contenedores.

**Temas cubiertos:**
- Namespaces de Linux (PID, Network, Mount, UTS, IPC, User, Cgroup)
- Control Groups (cgroups) para limitación de recursos
- Capabilities de Linux
- Seccomp (Secure Computing Mode)
- AppArmor y SELinux
- Contenedores rootless
- Tecnologías avanzadas: gVisor, Kata Containers, Firecracker
- Mejores prácticas de seguridad

### [Tema 4: Arquitectura de Docker](04-arquitectura-docker.md)
Análisis detallado de la arquitectura de Docker y sus componentes.

**Temas cubiertos:**
- Arquitectura cliente-servidor
- Docker Client (CLI)
- Docker Daemon (dockerd)
- containerd: runtime de alto nivel
- runc: runtime OCI de bajo nivel
- shim: proceso intermedio
- Registry de imágenes (Docker Hub y privados)
- Decisiones de diseño para contenedores ligeros, seguros y escalables
- Sistema de archivos por capas
- Multi-stage builds
- Flujo completo: de Dockerfile a contenedor en ejecución

### [Tema 5: Creación y Administración de Imágenes y Contenedores](05-creacion-administracion.md)
Guía práctica completa para trabajar con Docker.

**Temas cubiertos:**
- Gestión de imágenes (pull, push, tag, inspect)
- Dockerfile: instrucciones y mejores prácticas
- Multi-stage builds para optimización
- Creación y publicación de imágenes
- Ejecución y gestión de contenedores
- Networking, volúmenes y persistencia
- Límites de recursos y políticas de reinicio
- Comandos esenciales de Docker CLI
- Mejores prácticas de seguridad y optimización

### [Tema 6: Integración en la Nube](06-integracion-nube.md)
Uso de contenedores en plataformas cloud y servicios gestionados.

**Temas cubiertos:**
- Modelos de servicio cloud (IaaS, CaaS, PaaS)
- **AWS**: ECR, ECS, Fargate, EKS
- **Azure**: ACR, Container Instances, AKS, App Service
- **Google Cloud**: GCR, Artifact Registry, Cloud Run, GKE
- Docker Compose en la nube
- CI/CD con contenedores (GitHub Actions, GitLab CI, AWS CodePipeline)
- Monitorización y logging
- Mejores prácticas: seguridad, optimización de costes, auto-escalado
- Comparación de servicios entre proveedores

## Estructura del Repositorio

```
miot-dii/
├── README.md                           # Este archivo
├── 01-evolucion-servicios-web.md      # Tema 1
├── 02-tecnologias-sandboxing.md       # Tema 2
├── 04-arquitectura-docker.md          # Tema 4
├── 05-creacion-administracion.md      # Tema 5
└── 06-integracion-nube.md             # Tema 6
```

## Prerequisitos

Para seguir este curso, es recomendable tener:

- Conocimientos básicos de Linux y línea de comandos
- Comprensión de conceptos de redes (TCP/IP, HTTP)
- Familiaridad con desarrollo de software
- (Opcional) Cuenta en un proveedor cloud (AWS, Azure, o GCP)

## Recursos Adicionales

### Documentación Oficial
- [Docker Documentation](https://docs.docker.com/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Open Container Initiative](https://opencontainers.org/)

### Plataformas Cloud
- [AWS Container Services](https://aws.amazon.com/containers/)
- [Azure Container Services](https://azure.microsoft.com/products/category/containers/)
- [Google Cloud Container Services](https://cloud.google.com/containers)

### Herramientas Útiles
- [Docker Hub](https://hub.docker.com/)
- [Play with Docker](https://labs.play-with-docker.com/)
- [Katacoda](https://www.katacoda.com/courses/docker)

## Licencia

Este material está disponible bajo la licencia especificada en el archivo [LICENSE](LICENSE).

## Contribuciones

Este es un repositorio educativo. Para sugerencias o correcciones, por favor abre un issue o pull request.
