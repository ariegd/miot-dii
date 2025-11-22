# Extra (notable/sobresaliente)
## objetivos
1) [Aplicación correctamente contenedorizada con Docker](#paso1 )
2) [Implementa un recurso distinto de LibrosResource.java visto en el lab](#paso2)
3) [Incluye persistencia con MongoDB](#paso3)

<a name="paso1"></a>
## Paso 1: Aplicación correctamente contenedorizada con Docker
1. Crear `Dockerfile`
```
FROM eclipse-temurin:21

WORKDIR /app

COPY jax-rs-libs/ /app/jax-rs-libs/
COPY classes/ /app/classes/

ENV CLASSPATH /app/classes/:/app/jax-rs-libs/*

EXPOSE 8080

CMD ["java", "ServidorJersey"]
```
<a name="paso2"></a>
## Paso 2: Implementa un recurso distinto de LibrosResource.java visto en el lab
Ambos siguen el mismo patrón que el recurso de libros, pero manejan diferentes tipos de datos. Es crucial que ambos archivos comiencen con package rest; para que el servidor Jersey (que escanea el paquete "rest") los detecte automáticamente sin cambiar el código del servidor.

1. Crear `AlumnosResource.java`.  Este recurso gestiona estudiantes con campos `id`, `nombre` y `curso`.
```
extra/
|-- ServidorJerser.java
|-- rest/
|       |-- LibrosResource.java
|       |-- AlumnosResource.java
|-- jax-rs-libs/
|       |-- jakarta.inject-api-2.0.1.jar
|       |-- ...
```
2. Crear `VerdurasResource.java`. Este recurso gestiona verduras e introduce un campo numérico de tipo `double` para el `precio`.
```
extra/
|-- ServidorJerser.java
|-- rest/
|       |-- LibrosResource.java
|       |-- AlumnosResource.java
|       |-- VerdurasResource.java
|-- jax-rs-libs/
|       |-- jakarta.inject-api-2.0.1.jar
|       |-- ...
```
<a name="paso3"></a>
## Paso 3: Incluye persistencia con MongoDB
Para lograr que la aplicación Java (ServidorJersey) guarde los datos en MongoDB en lugar de la memoria RAM, y que ambos corran en contenedores Docker separados, necesitamos hacer 4 cambios principales:
1. Descargar las librerías (JARs) del driver de MongoDB.
```
extra/
|-- ServidorJerser.java
|-- rest/
|       |-- LibrosResource.java
|       |-- AlumnosResource.java
|       |-- VerdurasResource.java
|-- jax-rs-libs/
|       |-- mongodb-driver-sync-5.6.1.jar
|       |-- mongodb-driver-core-5.6.1.jar
|       |-- bson-5.6.1.jar
|       |-- ...
```
2. Modificar el código Java (ServidorJerser.java) para conectar a la base de datos.
```
....
    // Variable para mantener el cliente vivo
    private static MongoClient mongoClient;
....    
        // 1. Crear la conexión a Mongo (UNA SOLA VEZ)
        // Usamos "mongo-db" porque es el nombre del servicio en Docker
        String connectionString = "mongodb://mongo-db:27017";
        mongoClient = MongoClients.create(connectionString);
        
        // Obtenemos la base de datos "biblioteca"
        MongoDatabase db = mongoClient.getDatabase("biblioteca");
    
        // 2. REGISTRAR LA INYECCIÓN DE DEPENDENCIAS
        // Esto hace que 'db' esté disponible para cualquier Resource con @Inject
        rc.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(db).to(MongoDatabase.class);
            }
        });
....
        // Parada limpia
        if (mongoClient != null) {
                    mongoClient.close(); // Cerramos conexión a BD al salir
                    System.out.println("Conexión MongoDB cerrada.");
        }
```
3. Crear una red Docker para que los contenedores se "vean" entre sí. 
```
#  Nota: El Dockerfile es el mismo de la primera parte
javac -cp "jax-rs-libs/*" -d classes ServidorJersey.java rest/*.java
docker build -t mi-servicio-mongo .
```
4. Ejecutar los contenedores en el orden correcto.
```
#a) Crear una red:
docker network create red-servicio-mongo

#b) Iniciar MongoDB
docker run -d --name mongo-db --network red-servicio-mongo mongo:latest

#c) Iniciar tu App Jersey
docker run -it --rm -p 8080:8080 --network red-servicio-mongo --name jersey-app mi-servicio-mongo

#d) prueba con curl
# libros
curl http://localhost:8080/rest/libros -w "\n"
curl -i -X PUT "http://localhost:8080/rest/libros?id=3&titulo=Who%20Is%20Who?"
curl -i -X POST "http://localhost:8080/rest/libros?id=3&titulo=Un%20lío%20de%20millones"
curl -i -X DELETE "http://localhost:8080/rest/libros?id=3"

# alumnos
curl http://localhost:8080/rest/alumnos -w "\n"
curl -i -X PUT "http://localhost:8080/rest/alumnos?id=3&nombre=Felix%20Bolaño&curso=J2EE"
curl -i -X POST "http://localhost:8080/rest/alumnos?id=3&curso=C"
curl -i -X DELETE "http://localhost:8080/rest/alumnos?id=3"

# verduras
curl http://localhost:8080/rest/verduras -w "\n"
curl -i -X PUT "http://localhost:8080/rest/verduras?id=3&nombre=tomate&precio=2.56"
curl -i -X POST "http://localhost:8080/rest/verduras?id=3&precio=1.99"
curl -i -X DELETE "http://localhost:8080/rest/verduras?id=3"
```
5.  Para el guardado persistentemente en el contenedor de mongo
- SÍ se guarda si solo detenemos (docker stop) y volvemos a iniciar (docker start) el mismo contenedor.
- NO se guarda si borramos el contenedor (docker rm), cosa que ocurre a menudo al limpiar o actualizar versiones. Si borramos el contenedor mongo-db, perderemos todos los libros.
```
# Solución: Persistencia Real (Volúmenes)
# a) Crear una carpeta en tu PC para guardar los datos (opcional, Docker la crea si no existe)
mkdir -p ./datos_mongo

# b) Ejecutar Mongo mapeando la carpeta local (./datos_mongo) a la interna (/data/db)
docker run -d \
  --network host \
  --name mongo-db \
  -v "$(pwd)/datos_mongo:/data/db" \
  mongo:latest
```
