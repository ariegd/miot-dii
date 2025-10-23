# Tema 6: Integración en la Nube

## Introducción

Este documento explora cómo integrar Docker y contenedores en plataformas cloud, aprovechando servicios gestionados y capacidades de escalado automático.

## 1. Visión General de Cloud Computing para Contenedores

### 1.1 Modelos de Servicio

```
┌─────────────────────────────────────────────┐
│         IaaS (Infrastructure as a Service)   │
│  - AWS EC2, Azure VMs, Google Compute       │
│  - Control completo de VMs                  │
│  - Usuario gestiona Docker                  │
└─────────────────────────────────────────────┘
                    ▼
┌─────────────────────────────────────────────┐
│         CaaS (Containers as a Service)       │
│  - AWS ECS, Azure Container Instances       │
│  - Gestión de contenedores                  │
│  - Usuario gestiona aplicaciones            │
└─────────────────────────────────────────────┘
                    ▼
┌─────────────────────────────────────────────┐
│         PaaS (Platform as a Service)         │
│  - Heroku, Google App Engine                │
│  - Usuario solo gestiona código             │
│  - Plataforma gestiona todo lo demás        │
└─────────────────────────────────────────────┘
```

### 1.2 Proveedores Principales

| Proveedor | Servicios de Contenedores | Orquestación Gestionada |
|-----------|---------------------------|-------------------------|
| AWS | ECS, Fargate, ECR | EKS (Kubernetes) |
| Azure | Container Instances, ACR | AKS (Kubernetes) |
| Google Cloud | Cloud Run, GCR | GKE (Kubernetes) |
| IBM Cloud | Code Engine | IKS (Kubernetes) |
| Oracle Cloud | Container Instances | OKE (Kubernetes) |

## 2. Amazon Web Services (AWS)

### 2.1 Amazon ECR (Elastic Container Registry)

Registry privado para almacenar imágenes Docker.

#### Crear Repositorio

```bash
# Crear repositorio
aws ecr create-repository \
    --repository-name mi-app \
    --region us-east-1

# Obtener URI del repositorio
aws ecr describe-repositories \
    --repository-names mi-app \
    --region us-east-1
```

#### Autenticación y Push

```bash
# Autenticarse en ECR
aws ecr get-login-password --region us-east-1 | \
    docker login --username AWS --password-stdin \
    123456789012.dkr.ecr.us-east-1.amazonaws.com

# Tag de imagen para ECR
docker tag mi-app:latest \
    123456789012.dkr.ecr.us-east-1.amazonaws.com/mi-app:latest

# Push a ECR
docker push \
    123456789012.dkr.ecr.us-east-1.amazonaws.com/mi-app:latest
```

#### Políticas de Ciclo de Vida

```json
{
  "rules": [
    {
      "rulePriority": 1,
      "description": "Mantener últimas 10 imágenes",
      "selection": {
        "tagStatus": "any",
        "countType": "imageCountMoreThan",
        "countNumber": 10
      },
      "action": {
        "type": "expire"
      }
    }
  ]
}
```

### 2.2 Amazon ECS (Elastic Container Service)

Servicio de orquestación de contenedores propio de AWS.

#### Conceptos Clave

- **Cluster**: Agrupación lógica de recursos
- **Task Definition**: Blueprint del contenedor (como un pod)
- **Service**: Mantiene tareas ejecutándose
- **Task**: Instancia de una task definition

#### Task Definition (JSON)

```json
{
  "family": "mi-app-task",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "256",
  "memory": "512",
  "containerDefinitions": [
    {
      "name": "mi-app",
      "image": "123456789012.dkr.ecr.us-east-1.amazonaws.com/mi-app:latest",
      "portMappings": [
        {
          "containerPort": 80,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "APP_ENV",
          "value": "production"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/mi-app",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
```

#### Crear Servicio ECS

```bash
# Crear cluster
aws ecs create-cluster --cluster-name mi-cluster

# Registrar task definition
aws ecs register-task-definition --cli-input-json file://task-definition.json

# Crear servicio
aws ecs create-service \
    --cluster mi-cluster \
    --service-name mi-app-service \
    --task-definition mi-app-task \
    --desired-count 2 \
    --launch-type FARGATE \
    --network-configuration "awsvpcConfiguration={subnets=[subnet-12345],securityGroups=[sg-12345],assignPublicIp=ENABLED}"
```

### 2.3 AWS Fargate

Serverless compute para contenedores (sin gestión de servidores).

#### Ventajas

- No gestionar instancias EC2
- Pago por uso (CPU/memoria por segundo)
- Escalado automático
- Seguridad mejorada (aislamiento)

#### Ejemplo con Fargate

```bash
# Task definition para Fargate
aws ecs register-task-definition \
    --family mi-app \
    --requires-compatibilities FARGATE \
    --cpu 256 \
    --memory 512 \
    --network-mode awsvpc \
    --container-definitions '[
      {
        "name": "mi-app",
        "image": "nginx:alpine",
        "portMappings": [{"containerPort": 80}]
      }
    ]'

# Ejecutar task
aws ecs run-task \
    --cluster mi-cluster \
    --task-definition mi-app \
    --launch-type FARGATE \
    --network-configuration "awsvpcConfiguration={subnets=[subnet-12345],securityGroups=[sg-12345]}"
```

### 2.4 Amazon EKS (Elastic Kubernetes Service)

Kubernetes gestionado en AWS.

#### Crear Cluster EKS

```bash
# Usando eksctl
eksctl create cluster \
    --name mi-cluster \
    --version 1.28 \
    --region us-east-1 \
    --nodegroup-name standard-workers \
    --node-type t3.medium \
    --nodes 3 \
    --nodes-min 1 \
    --nodes-max 4

# Configurar kubectl
aws eks update-kubeconfig --name mi-cluster --region us-east-1

# Verificar
kubectl get nodes
```

#### Desplegar Aplicación

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mi-app
spec:
  replicas: 3
  selector:
    matchLabels:
      app: mi-app
  template:
    metadata:
      labels:
        app: mi-app
    spec:
      containers:
      - name: mi-app
        image: 123456789012.dkr.ecr.us-east-1.amazonaws.com/mi-app:latest
        ports:
        - containerPort: 80
        resources:
          requests:
            memory: "128Mi"
            cpu: "250m"
          limits:
            memory: "256Mi"
            cpu: "500m"
```

```bash
kubectl apply -f deployment.yaml
kubectl expose deployment mi-app --type=LoadBalancer --port=80
```

## 3. Microsoft Azure

### 3.1 Azure Container Registry (ACR)

```bash
# Crear registro
az acr create \
    --resource-group mi-grupo \
    --name miregistro \
    --sku Basic

# Login
az acr login --name miregistro

# Tag y push
docker tag mi-app:latest miregistro.azurecr.io/mi-app:latest
docker push miregistro.azurecr.io/mi-app:latest

# Listar imágenes
az acr repository list --name miregistro --output table
```

### 3.2 Azure Container Instances (ACI)

Ejecución rápida de contenedores sin orquestación.

```bash
# Crear container instance
az container create \
    --resource-group mi-grupo \
    --name mi-app-container \
    --image miregistro.azurecr.io/mi-app:latest \
    --cpu 1 \
    --memory 1 \
    --registry-login-server miregistro.azurecr.io \
    --registry-username $USERNAME \
    --registry-password $PASSWORD \
    --dns-name-label mi-app-unica \
    --ports 80

# Ver logs
az container logs --resource-group mi-grupo --name mi-app-container

# Ver estado
az container show \
    --resource-group mi-grupo \
    --name mi-app-container \
    --query "{FQDN:ipAddress.fqdn,ProvisioningState:provisioningState}" \
    --out table
```

### 3.3 Azure Kubernetes Service (AKS)

```bash
# Crear cluster AKS
az aks create \
    --resource-group mi-grupo \
    --name mi-aks-cluster \
    --node-count 3 \
    --node-vm-size Standard_B2s \
    --enable-addons monitoring \
    --generate-ssh-keys

# Obtener credenciales
az aks get-credentials \
    --resource-group mi-grupo \
    --name mi-aks-cluster

# Conectar ACR con AKS
az aks update \
    --name mi-aks-cluster \
    --resource-group mi-grupo \
    --attach-acr miregistro

# Desplegar
kubectl apply -f deployment.yaml
```

### 3.4 Azure App Service para Contenedores

```bash
# Crear App Service Plan (Linux)
az appservice plan create \
    --name mi-plan \
    --resource-group mi-grupo \
    --is-linux \
    --sku B1

# Crear Web App con contenedor personalizado
az webapp create \
    --resource-group mi-grupo \
    --plan mi-plan \
    --name mi-app-webapp \
    --deployment-container-image-name miregistro.azurecr.io/mi-app:latest

# Configurar registro
az webapp config container set \
    --name mi-app-webapp \
    --resource-group mi-grupo \
    --docker-custom-image-name miregistro.azurecr.io/mi-app:latest \
    --docker-registry-server-url https://miregistro.azurecr.io \
    --docker-registry-server-user $USERNAME \
    --docker-registry-server-password $PASSWORD

# Ver logs
az webapp log tail --name mi-app-webapp --resource-group mi-grupo
```

## 4. Google Cloud Platform (GCP)

### 4.1 Google Container Registry (GCR) y Artifact Registry

```bash
# Autenticarse
gcloud auth configure-docker

# Tag para GCR
docker tag mi-app:latest gcr.io/mi-proyecto/mi-app:latest

# Push a GCR
docker push gcr.io/mi-proyecto/mi-app:latest

# Listar imágenes
gcloud container images list --repository=gcr.io/mi-proyecto

# Artifact Registry (más nuevo, recomendado)
gcloud artifacts repositories create mi-repo \
    --repository-format=docker \
    --location=us-central1

# Tag para Artifact Registry
docker tag mi-app:latest \
    us-central1-docker.pkg.dev/mi-proyecto/mi-repo/mi-app:latest

# Push
docker push us-central1-docker.pkg.dev/mi-proyecto/mi-repo/mi-app:latest
```

### 4.2 Google Cloud Run

Serverless para contenedores HTTP (ideal para APIs y web apps).

```bash
# Desplegar desde imagen
gcloud run deploy mi-app \
    --image gcr.io/mi-proyecto/mi-app:latest \
    --platform managed \
    --region us-central1 \
    --allow-unauthenticated \
    --memory 512Mi \
    --cpu 1 \
    --min-instances 0 \
    --max-instances 10

# Actualizar variables de entorno
gcloud run services update mi-app \
    --set-env-vars "APP_ENV=production,DB_HOST=localhost"

# Ver logs
gcloud run logs read mi-app --region us-central1 --limit 50

# Obtener URL del servicio
gcloud run services describe mi-app \
    --region us-central1 \
    --format 'value(status.url)'
```

#### Características de Cloud Run

- **Escalado a cero**: No paga cuando no hay tráfico
- **Escalado automático**: Hasta 1000 instancias
- **HTTPS por defecto**: Certificados gestionados
- **Múltiples revisiones**: Canary deployments, traffic splitting

```bash
# Traffic splitting entre revisiones
gcloud run services update-traffic mi-app \
    --to-revisions mi-app-v2=50,mi-app-v1=50 \
    --region us-central1
```

### 4.3 Google Kubernetes Engine (GKE)

```bash
# Crear cluster
gcloud container clusters create mi-cluster \
    --zone us-central1-a \
    --num-nodes 3 \
    --machine-type n1-standard-2 \
    --enable-autoscaling \
    --min-nodes 1 \
    --max-nodes 5

# Obtener credenciales
gcloud container clusters get-credentials mi-cluster --zone us-central1-a

# Autopilot mode (totalmente gestionado)
gcloud container clusters create-auto mi-autopilot-cluster \
    --region us-central1

# Desplegar
kubectl apply -f deployment.yaml

# Exponer servicio
kubectl expose deployment mi-app --type LoadBalancer --port 80
```

#### GKE Autopilot

- Gestión completa de nodos
- Pago por pod (no por nodo)
- Optimización automática
- Seguridad mejorada

## 5. Docker Compose en la Nube

### 5.1 Docker Compose para Multi-Contenedor

```yaml
# docker-compose.yml
version: '3.8'

services:
  web:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./html:/usr/share/nginx/html
    depends_on:
      - api
    networks:
      - app-network

  api:
    image: miregistro.azurecr.io/mi-api:latest
    environment:
      - DB_HOST=db
      - DB_PORT=5432
    depends_on:
      - db
    networks:
      - app-network

  db:
    image: postgres:15-alpine
    environment:
      - POSTGRES_PASSWORD=secreto
      - POSTGRES_DB=midb
    volumes:
      - db-data:/var/lib/postgresql/data
    networks:
      - app-network

volumes:
  db-data:

networks:
  app-network:
    driver: bridge
```

### 5.2 Azure Container Instances con Docker Compose

```bash
# Crear contexto ACI
docker context create aci mi-contexto-aci

# Usar contexto ACI
docker context use mi-contexto-aci

# Desplegar con Compose
docker compose up

# Ver servicios
docker ps

# Logs
docker logs <container-id>

# Eliminar
docker compose down
```

## 6. CI/CD con Contenedores

### 6.1 GitHub Actions

```yaml
# .github/workflows/docker-build.yml
name: Build and Push Docker Image

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up Docker Buildx
      uses: docker/setup-buildx-action@v2
    
    - name: Login to Docker Hub
      uses: docker/login-action@v2
      with:
        username: ${{ secrets.DOCKER_USERNAME }}
        password: ${{ secrets.DOCKER_PASSWORD }}
    
    - name: Build and push
      uses: docker/build-push-action@v4
      with:
        context: .
        push: true
        tags: |
          miusuario/mi-app:latest
          miusuario/mi-app:${{ github.sha }}
        cache-from: type=registry,ref=miusuario/mi-app:buildcache
        cache-to: type=registry,ref=miusuario/mi-app:buildcache,mode=max
```

### 6.2 GitLab CI/CD

```yaml
# .gitlab-ci.yml
stages:
  - build
  - test
  - deploy

variables:
  DOCKER_IMAGE: registry.gitlab.com/$CI_PROJECT_PATH
  DOCKER_TAG: $CI_COMMIT_SHORT_SHA

build:
  stage: build
  image: docker:latest
  services:
    - docker:dind
  before_script:
    - docker login -u $CI_REGISTRY_USER -p $CI_REGISTRY_PASSWORD $CI_REGISTRY
  script:
    - docker build -t $DOCKER_IMAGE:$DOCKER_TAG .
    - docker push $DOCKER_IMAGE:$DOCKER_TAG

deploy_production:
  stage: deploy
  image: google/cloud-sdk:alpine
  script:
    - echo $GCP_SERVICE_KEY | base64 -d > ${HOME}/gcp-key.json
    - gcloud auth activate-service-account --key-file ${HOME}/gcp-key.json
    - gcloud run deploy mi-app 
        --image $DOCKER_IMAGE:$DOCKER_TAG 
        --platform managed 
        --region us-central1
  only:
    - main
```

### 6.3 AWS CodePipeline

```yaml
# buildspec.yml
version: 0.2

phases:
  pre_build:
    commands:
      - echo Logging in to Amazon ECR...
      - aws ecr get-login-password --region $AWS_DEFAULT_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com
  build:
    commands:
      - echo Build started on `date`
      - docker build -t $IMAGE_REPO_NAME:$IMAGE_TAG .
      - docker tag $IMAGE_REPO_NAME:$IMAGE_TAG $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/$IMAGE_REPO_NAME:$IMAGE_TAG
  post_build:
    commands:
      - echo Build completed on `date`
      - docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/$IMAGE_REPO_NAME:$IMAGE_TAG
      - printf '[{"name":"mi-app","imageUri":"%s"}]' $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/$IMAGE_REPO_NAME:$IMAGE_TAG > imagedefinitions.json

artifacts:
  files: imagedefinitions.json
```

## 7. Monitorización y Logging en la Nube

### 7.1 AWS CloudWatch

```bash
# Ver logs de ECS
aws logs tail /ecs/mi-app --follow

# Crear métrica personalizada
aws cloudwatch put-metric-data \
    --namespace MiApp \
    --metric-name RequestCount \
    --value 1

# Crear alarma
aws cloudwatch put-metric-alarm \
    --alarm-name cpu-alta \
    --alarm-description "CPU usage > 80%" \
    --metric-name CPUUtilization \
    --namespace AWS/ECS \
    --statistic Average \
    --period 300 \
    --threshold 80 \
    --comparison-operator GreaterThanThreshold
```

### 7.2 Azure Monitor

```bash
# Ver logs de Container Instance
az container logs \
    --resource-group mi-grupo \
    --name mi-container \
    --follow

# Habilitar Application Insights para AKS
az aks enable-addons \
    --resource-group mi-grupo \
    --name mi-cluster \
    --addons monitoring
```

### 7.3 Google Cloud Monitoring

```bash
# Ver logs
gcloud logging read "resource.type=cloud_run_revision" \
    --limit 50 \
    --format json

# Crear métrica
gcloud logging metrics create request_count \
    --description="Count of requests" \
    --log-filter='resource.type="cloud_run_revision"'
```

## 8. Mejores Prácticas para Cloud

### 8.1 Seguridad

```bash
# Escanear vulnerabilidades con Trivy
trivy image --severity HIGH,CRITICAL mi-app:latest

# AWS - Escaneo en ECR
aws ecr start-image-scan \
    --repository-name mi-app \
    --image-id imageTag=latest

# Azure - Escaneo en ACR con Defender
az acr task create \
    --registry miregistro \
    --name escaneo-seguridad \
    --cmd 'trivy image --exit-code 1 {{.Values.image}}' \
    --context /dev/null
```

### 8.2 Optimización de Costes

```yaml
# Usar spot instances para cargas tolerantes a interrupciones
# GKE
gcloud container node-pools create spot-pool \
    --cluster mi-cluster \
    --spot \
    --num-nodes 3

# EKS - Managed Node Group con Spot
eksctl create nodegroup \
    --cluster mi-cluster \
    --spot \
    --instance-types t3.medium,t3a.medium
```

### 8.3 Auto-escalado

```yaml
# Kubernetes HPA (Horizontal Pod Autoscaler)
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: mi-app-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: mi-app
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

## 9. Comparación de Servicios Cloud

| Característica | AWS | Azure | GCP |
|----------------|-----|-------|-----|
| Registry | ECR | ACR | GCR/Artifact Registry |
| Serverless | Fargate | Container Instances | Cloud Run |
| Kubernetes | EKS | AKS | GKE |
| Orquestación propia | ECS | - | - |
| PaaS | Elastic Beanstalk | App Service | App Engine |
| Precio serverless | Por seg (CPU+mem) | Por seg | Por petición |
| Escalado a cero | No (Fargate) | Sí (ACI) | Sí (Cloud Run) |

## Referencias

- AWS Container Services: https://aws.amazon.com/containers/
- Azure Container Services: https://azure.microsoft.com/en-us/products/category/containers/
- Google Cloud Container Services: https://cloud.google.com/containers
- Docker Documentation: https://docs.docker.com/cloud/
- Kubernetes Documentation: https://kubernetes.io/docs/
