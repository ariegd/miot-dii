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

@Path("/libros")
public class LibrosResource {
    @Inject
    private MongoDatabase database;

    private MongoCollection<Document> getCollection() {
        return database.getCollection("libros");
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Libro> obtenerLibros() {
        List<Libro> resultado = new ArrayList<>();
        // Usamos la conexión inyectada
        for (Document doc : getCollection().find()) {
            Libro libro = new Libro(doc.getInteger("id"), doc.getString("titulo"));
            resultado.add(libro);
        }
        return resultado;
    }

    @PUT
    public Response crearLibro(@QueryParam("id") int id, 
                                                          @QueryParam("titulo") String titulo) {
        MongoCollection<Document> col = getCollection();
        if (col.countDocuments(Filters.eq("id", id)) > 0) {
            return Response.status(Response.Status.CONFLICT).entity("Ya existe").build();
        }
        col.insertOne(new Document("id", id).append("titulo", titulo));
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    public Response actualizarLibro(@QueryParam("id") int id, 
                                                                  @QueryParam("titulo") String titulo) {
        var result = getCollection().updateOne(Filters.eq("id", id), Updates.set("titulo", titulo));           
        if (result.getMatchedCount() == 0) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok().build();
    }

    @DELETE
    public Response eliminarLibro(@QueryParam("id") int id) {
        var result = getCollection().deleteOne(Filters.eq("id", id));
            
        if (result.getDeletedCount() == 0) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok().build();
    }

    public static class Libro {
        public int id;
        public String titulo;
        public Libro() {} // Requerido por JSON-B
        public Libro(int id, String titulo) {
            this.id = id;
            this.titulo = titulo;
        }
    }
}
