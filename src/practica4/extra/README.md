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
2. Construir la Imagen.
```
docker build -t mi-servicio-jersey .
```
3. Ejecutar el Contenedor
```
# Ahora lanza el contenedor añadiendo -it. Esto evitará que System.in.read() se salte y cierre el servidor.
docker run -it --rm -p 8080:8080 --name servidor-libros mi-servicio-jersey
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
3. Compilar. 
```
# Opción explícita (recomendada para estar seguros)
javac -cp "jax-rs-libs/*" -d classes ServidorJersey.java rest/LibrosResource.java rest/AlumnosResource.java rest/VerdurasResource.java
```
4. Ejecutar
```
java -cp "jax-rs-libs/*:classes" ServidorJersey
```
5. Ahora tenemos 3 endpoints disponibles.
* Libros
* Alumnos
* Verduras
```
# GET
curl http://localhost:8080/rest/libros
curl http://localhost:8080/rest/alumnos
curl http://localhost:8080/rest/verduras
# PUT
curl -i -X PUT "http://localhost:8080/rest/libros?id=3&titulo=Who%20Is%20Who?"
curl -i -X PUT "http://localhost:8080/rest/alumnos?id=99&nombre=Pepito&curso=Master"
curl -i -X PUT "http://localhost:8080/rest/verduras?id=3&nombre=tomate&precio=1.99"
# POST
curl -i -X POST "http://localhost:8080/rest/libros?id=3&titulo=Un%20lío%20de%20millones"
curl -i -X POST "http://localhost:8080/rest/alumnos?id=99&nombre=Isa&curso=Grado"
curl -i -X POST "http://localhost:8080/rest/verduras?id=3&nombre=alcachofa&precio=1.59"
# DELETE
curl -i -X DELETE "http://localhost:8080/rest/libros?id=3"
```
<a name="paso3"></a>
## Paso 3: Incluye persistencia con MongoDB

