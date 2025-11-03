## Paso 1: Crear y construir el contenedor PostgreSQL
1. Construir, lanzar y probar el contenedor en modo host
```
docker build -t bd-congreso .
docker run -d --rm --name bd-congreso --network host bd-congreso
```
2. Inspeccionar el arranque de PostgreSQL
* /docker-entrypoint-initdb.d/init.sql podemos crear script que se inicializa cuando se ejecute PostgreSQL
```
docker run --name pgtest -e POSTGRES_PASSWORD=clave123 postgres:16 # -d background
docker exec -it pgtest /bin/bash
ls /usr/local/bin/docker-entrypoint.sh
```
3. Podemos conectarnos directamente desde el exterior a la BD con los comandos, ya que está en modo host:
```
sudo apt install postgresql-client
psql -h localhost -U organizador -d congresodb
```
## Paso 2: Crear el servicio REST Python con Flask
1. requierements.tx se le pone las librerias que va a necesitar python
* Si lo hacemos con pip, instalamos. Fernando: Siempre antes de contenizar hay que probarlo:
```
pip install Flask
pip install psycopg2
```
2. Lanzar los contenedores en modo host
```
docker build -t app-congreso .
docker run --rm --name app-congreso --network host app-congreso
```
3.  Podemos insertar un registro a través del servicio web usando curl:
```
curl -X POST http://localhost:5000/asistentes \
                -H "Content-Type: application/json" \
                -d '{"nombre": "Ana", "email": "ana@example.com"}'
# meterte por 
psql -h loalhost -U organizador -d congresodb
```
## Paso 3: Crear una red personalizada para los contenedores
1. Crear la red personalizada
```
docker network create congreso-net
docker network ls
```
2. Parar los contenedores en modo host
```
docker stop bd-congreso
docker stop app-congreso
```
3. Lanzar la BD en la red personalizada
```
docker run --rm --name bd-congreso --network congreso-net bd-congreso
```
4. Lanzar la aplicación Flask en la red personalizada
* Como está en modo bridge hay que abrir puertos
* DB_HOST hay que pasarle el nombre del contenedor que es el que actua como dns ya no es localhost
```
docker run --rm --name app-congreso --network congreso-net \
                            -p 5000:5000 \
                            -e DB_HOST=bd-congreso \
                            app-congreso
```
5. Probar el funcionamiento
```
curl -X POST http://localhost:5000/asistentes \
                    -H "Content-Type: application/json" \
                    -d '{"nombre": "Pedro", "email": "pedro@example.com"}'
                    
curl http://localhost:5000/asistentes
```
6. Cerrar los contenedores:
```
docker stop app-congreso
docker stop bd-congreso
```
## Paso 4: El cliente Cordova funciona  en plataforma browser: permite listar e insertar asistentes en el backend REST.
CORS son las siglas de Cross-Origin Resource Sharing (Intercambio de Recursos de Origen Cruzado). Es un mecanismo de seguridad implementado por los navegadores web para controlar si una página web tiene permiso para acceder a recursos (como datos, fuentes o imágenes) que se encuentran en un dominio, protocolo o puerto diferente al de la página que hace la solicitud.
 1. Modificar app.py 
```
from flask_cors import CORS
CORS(app)  #  permite peticiones desde apps locales o Cordova
```
2. Modificar requirements.txt
```
flask_cors # <-nuevo
```
