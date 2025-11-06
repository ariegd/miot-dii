## Paso 1: Instalar Docker Compose
* Se instala docker compose v1 ya que el v2 no es estable
```
sudo apt install -y docker.io docker-compose # Debian Compose v1

docker-compose version # Debian
```
## Paso 2: Crear el archivo yml
* Revisar si tiene errores
```
docker-compose config
```
## Paso 3: Lanzar un proyecto Docker Compose
```
docker-compose up -d --build

# Se quede leyendo continuamente
docker-compose logs -f

curl -X POST http://localhost:25000/asistentes \
    -H "Content-Type: application/json" \
    -d '{"nombre": "Ana", "email": "ana@example.com"}'
```
## Paso 4: Detener un proyecto Docker Compose
```
docker-compose down
```
## Paso 5: Crear GitHub Action
1. El directorio con esta forma
```
tu-proyecto/
├── .github/
│   └── workflows/
│       └── ci.yml   <-- Aquí irá tu flujo de trabajo de GitHub Actions
├── bd-congreso/
│   └── Dockerfile   <-- El Dockerfile para la DB
├── app-congreso/
│   └── Dockerfile   <-- El Dockerfile para la App
└── docker-compose.yml <-- El archivo que nos mostraste
```
2. Crear archivo para el GitHub Action
```
name: CI con Docker Compose

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  test_services:
    runs-on: ubuntu-latest  # El ejecutor ya trae Docker y Docker Compose preinstalados

    steps:
      - name: Checkout del código
        # Descarga el código de tu repositorio
        uses: actions/checkout@v4

      - name: Construir y levantar servicios con Docker Compose
        # Usa el comando 'up' para construir las imágenes (ya que usas 'build' en tu .yml) 
        # y arrancar todos los servicios en segundo plano (-d).
        # Nota: Puedes usar 'docker compose' (nuevo) o 'docker-compose' (antiguo).
        run: docker compose up -d

      # Este es el paso crucial para esperar a que la DB esté lista.
      # La aplicación depende de la DB, y el arranque de la DB a veces es lento.
      - name: Esperar a que la Base de Datos se inicialice
        # Usamos 'sleep' para dar tiempo, pero es mejor usar una herramienta de "health check"
        run: sleep 10 

      - name: Ejecutar Pruebas (Ejemplo)
        # Una vez que los servicios están listos, puedes ejecutar comandos
        # dentro de tu contenedor 'app' para lanzar tus pruebas unitarias o de integración.
        run: docker compose exec app python run_tests.py # (Ajusta este comando a tu lenguaje real)

      - name: Detener servicios de Docker Compose (limpieza)
        # Detiene y elimina los contenedores y la red creada.
        # Esto es importante para una buena limpieza del entorno de CI.
        if: always() # Se ejecuta incluso si las pruebas fallan
        run: docker compose down
```
