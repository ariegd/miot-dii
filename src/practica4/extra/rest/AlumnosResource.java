package rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;

import jakarta.inject.Inject; 
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

@Path("/alumnos")
public class AlumnosResource {
/*
    private static final Map<Integer, Alumno> alumnosDB = new HashMap<>();
    
    static {
        alumnosDB.put(1, new Alumno(1, "Ana García", "Ingeniería IoT"));
        alumnosDB.put(2, new Alumno(2, "Luis Rodriguez", "Inteligencia Artificial"));
    }
*/
    @Inject
    private MongoDatabase database;

    private MongoCollection<Document> getCollection() {
        return database.getCollection("alumnos");
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Alumno> obtenerAlumnos() {
        List<Alumno> resultado = new ArrayList<>();
        for (Document doc : getCollection().find()) {
            Alumno alumno = new Alumno(doc.getInteger("id"), doc.getString("nombre"),  doc.getString("curso"));
            resultado.add(alumno);
        }
        return resultado;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response crearAlumno(@QueryParam("id") int id, 
                                                              @QueryParam("nombre") String nombre,
                                                              @QueryParam("curso") String curso) {
        MongoCollection<Document> col = getCollection();
        if (col.countDocuments(Filters.eq("id", id)) > 0) {
            return Response.status(Response.Status.CONFLICT).entity("Ya existe").build();
        }
        col.insertOne(new Document("id", id).append("nombre", nombre).append("curso", curso));
        return Response.status(Response.Status.CREATED).build();
    }

    @POST // Actualizar
    public Response actualizarAlumno(@QueryParam("id") int id, 
                                     @QueryParam("curso") String nuevoCurso) {
        var result = getCollection().updateOne(Filters.eq("id", id), Updates.set("curso", nuevoCurso));           
        if (result.getMatchedCount() == 0) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok().build();
    }

    @DELETE
    public Response eliminarAlumno(@QueryParam("id") int id) {
        var result = getCollection().deleteOne(Filters.eq("id", id));
            
        if (result.getDeletedCount() == 0) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok().build();
    }

    // Clase interna (POJO) para representar el Alumno
    public static class Alumno {
        public int id;
        public String nombre;
        public String curso;

        public Alumno() {} // Constructor vacío necesario para JSON

        public Alumno(int id, String nombre, String curso) {
            this.id = id;
            this.nombre = nombre;
            this.curso = curso;
        }
    }
}
