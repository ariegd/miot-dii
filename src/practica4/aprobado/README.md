# Aprobado. Servicio REST funcional con Jersey + Grizzly
```
objetivos
├── 1) Crear un servidor HTTP embebido ejecutable mediante un método main() que configure y lance el servicio REST
├── 2) Exponer al menos un recurso REST (/libros, /alumnos, etc.) bajo la ruta base http://localhost:8080/rest/
├── 3) Ser completamente funcional sin depender de un contenedor externo (como Tomcat o GlassFish)
├── 4) Permitir realizar operaciones GET, POST, PUT y DELETE, aceptando parámetros en la URL y devolviendo datos en formato JSON
├── 5) Implementar el recurso REST con las anotaciones de JAX-RS ( @Path, @GET, @POST, etc.)
├── 6) Tener verificado su funcionamiento con curl, o herramientas equivalentes
└── 7) Crear un Dockerfile que construya la aplicación y ejecute el servicio REST al lanzar el contenedor.
```

## Paso 1: Crear un servidor HTTP embebido ejecutable mediante un método main() que configure y lance el servicio REST
1. Crear el archivo `ServidorJerser.java`, con el método `main()`
```
aprobado/
|-- ServidorJerser.java
```
2. Implementación con Jersey y Grizzly
```
// Framework JAX-RS: 
Jersey (Implementación de Referencia)

// Clase de Servidor: 
GrizzlyHttpServerFactory.createHttpServer()

// Servidor HTTP Embebido: 
Grizzly 2 (Explícitamente configurado)

// Configuración de Recursos: 
ResourceConfig rc = new ResourceConfig().register(LibrosResource.class)
```
## Paso 2: Exponer al menos un recurso REST (/libros, /alumnos, etc.) bajo la ruta base http://localhost:8080/rest/
1.  URI base donde se publicará el servicio (Base URI)
```
 public static final String BASE_URI = "http://localhost:8080/rest/";
```
2. El ResourceConfig debe registrar las clases de recursos JAX-RS
```
rc.register(LibrosResource.class);
```
## Paso 3: Ser completamente funcional sin depender de un contenedor externo (como Tomcat o GlassFish)
1.  Crear y arrancar la instancia de Grizzly HttpServer
```
return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
```
## Paso 4: Permitir realizar operaciones GET, POST, PUT y DELETE, aceptando parámetros en la URL y devolviendo datos en formato JSON
1. Crear el archivo `LibrosResource.java`
```
aprobado/
|-- ServidorJerser.java
|-- rest/
|       |-- LibrosResource.java
```
2. Soporte para JSON (como Jackson) --> `ServidorJerser.java`
```
rc.register(new JacksonJsonProvider());
```
3. Código del GET, que devuelve un JSON --> `LibrosResource.java`
```
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Libro> obtenerLibros() {
        List<Libro> resultado = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : libros.entrySet()) {
            resultado.add(new Libro(entry.getKey(), entry.getValue()));
        }
        return resultado;
    }
```
4. Código del PUT que aceptando parámetros en la URL, mediante `@QueryParam` --> `LibrosResource.java`
```
    @PUT
    public Response crearLibro(@QueryParam("id") int id, 
                               @QueryParam("titulo") String titulo) {
        if (libros.containsKey(id)) {
            return Response.status(
                Response.Status.CONFLICT).entity("Ya existe").build();
        }
        libros.put(id, titulo);
        return Response.status(Response.Status.CREATED).build();
    }
```
## Paso 5: Implementar el recurso REST con las anotaciones de JAX-RS ( @Path, @GET, @POST, etc.)
1. La Configuración Central (ResourceConfig) --> `ServidorJerser.java`
```
// El método .packages("rest") le dice a Jersey: "Escanea el paquete llamado 'rest', busca clases con anotaciones JAX-RS (como @Path) y regístralas automáticamente".
final ResourceConfig rc = new ResourceConfig().packages("rest");
```
## Paso 6: Tener verificado su funcionamiento con curl, o herramientas equivalentes
1. Crear directorio `jax-rs-libs` con dependencias necesarias
```
aprobado/
|-- ServidorJerser.java
|-- rest/
|       |-- LibrosResource.java
|-- jax-rs-libs/
|       |-- jakarta.inject-api-2.0.1.jar
|       |-- ...
```
2. Compilar. En nuestro caso a mediada que compilabamos, nos lanzaba excepciones con las *.jar faltantes.
```
# Sin falta algún *.jar hay descargarlo en: https://mvnrepository.com/
javac -cp "jax-rs-libs/*" ServidorJersey.java rest/LibrosResource.java

# Tenemos que utilizar -Xlint:deprecation, para ver que está obsoleto
javac -cp "jax-rs-libs/*" -d classes ServidorJersey.java rest/LibrosResource.java -Xlint:deprecation
```
3. Ejecutar
```
# ASUMIENTO que estamos en el directorio raíz, en nuestro práctica 'aprobado'
java -cp "jax-rs-libs/*:classes" ServidorJersey
```
4. Consultar con Curls
```
# GET
curl http://localhost:8080/rest/libros
# PUT
curl -i -X PUT "http://localhost:8080/rest/libros?id=3&titulo=Who%20Is%20Who?"
# POST
curl -i -X POST "http://localhost:8080/rest/libros?id=3&titulo=Un%20lío%20de%20millones"
# DELETE
curl -i -X DELETE "http://localhost:8080/rest/libros?id=3"

# Nota: Nuestro servidor (a través de Jackson) envía la respuesta JSON minificada (compactada) para ahorrar ancho de banda.
# No envía un carácter de "salto de línea" (\n) al final. Para que se vea el resultado formateado podemos utilizar al final del curl
-w "\n"
# o 
| python3 -m json.tool
```
## Paso 7: Crear un Dockerfile que construya la aplicación y ejecute el servicio REST al lanzar el contenedor.
1. ETAPA BASE: Utilizamos la imagen oficial de OpenJDK 17
```
FROM eclipse-temurin:21
```
 2. DIRECTORIO DE TRABAJO: Todas las operaciones siguientes se harán dentro de /app
```
WORKDIR /app
```
3. COPIAR RECURSOS:  Copia la carpeta de librerías (JARs) y clases compiladas al contenedor.
```
COPY jax-rs-libs/ /app/jax-rs-libs/
COPY classes/ /app/classes/
```
 4. Definimos la variable CLASSPATH, incluye la carpeta de clases compiladas y todos los JARs en jax-rs-libs/
```
ENV CLASSPATH /app/classes/:/app/jax-rs-libs/*
```
5. EXPONER PUERTO: Informa a Docker que el contenedor escucha en el 8080
```
EXPOSE 8080
```
6. COMANDO DE INICIO: Ejecuta la clase principal del servidor
```
CMD ["java", "ServidorJersey"]
```
7. Construir la Imagen.
```
docker build -t mi-servicio-jersey .
```
8. Ejecutar el Contenedor
```
# Ahora lanza el contenedor añadiendo -it. Esto evitará que System.in.read() se salte y cierre el servidor.
docker run -it --rm -p 8080:8080 --name servidor-libros mi-servicio-jersey
```
9.  El problema de la conexión (localhost)
* El log dice claramente: INFO: Started listener bound to [localhost:8080]

* ¿Por qué es un problema? Si el servidor escucha en localhost dentro del contenedor, no se podrá acceder desde fuera (desde nuestro navegador Chrome/Firefox). Docker aísla la red. Para que Docker pueda redirigir el tráfico, Java debe escuchar en 0.0.0.0.
```
// CAMBIAR ESTO:
// public static final String BASE_URI = "http://localhost:8080/rest/";

// POR ESTO:
public static final String BASE_URI = "http://0.0.0.0:8080/rest/";
```
