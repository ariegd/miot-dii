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

@Path("/verduras")
public class VerdurasResource {
/*
    private static final Map<Integer, Verdura> stock = new HashMap<>();
    
    static {
        stock.put(100, new Verdura(100, "Lechuga Romana", 0.99));
        stock.put(101, new Verdura(101, "Tomate Raf", 2.50));
    }
*/
    @Inject
    private MongoDatabase database;

    private MongoCollection<Document> getCollection() {
        return database.getCollection("verduras");
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Verdura> listarVerduras() {
        List<Verdura> resultado = new ArrayList<>();
        // Usamos la conexión inyectada
        for (Document doc : getCollection().find()) {
            Verdura  verdura = new Verdura(doc.getInteger("id"), doc.getString("nombre"), doc.getDouble("precio"));
            resultado.add(verdura);
        }
        return resultado;
    }

    @PUT
    public Response agregarVerdura(@QueryParam("id") int id, 
                                   @QueryParam("nombre") String nombre,
                                   @QueryParam("precio") double precio) {
        MongoCollection<Document> col = getCollection();
        if (col.countDocuments(Filters.eq("id", id)) > 0) {
            return Response.status(Response.Status.CONFLICT).entity("Ya existe").build();
        }
        col.insertOne(new Document("id", id).append("nombre", nombre).append("precio", precio));
        return Response.status(Response.Status.CREATED).build();
    }
    
    @POST
    public Response actualizarVerdura(@QueryParam("id") int id, 
                                   @QueryParam("precio") double nuevoPrecio) {
        var result = getCollection().updateOne(Filters.eq("id", id), Updates.set("precio", nuevoPrecio));           
        if (result.getMatchedCount() == 0) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok().build();
    }


    @DELETE
    public Response venderVerdura(@QueryParam("id") int id) {
        var result = getCollection().deleteOne(Filters.eq("id", id));
            
        if (result.getDeletedCount() == 0) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok().build();
    }

    // Clase interna POJO
    public static class Verdura {
        public int id;
        public String nombre;
        public double precio;

        public Verdura() {}

        public Verdura(int id, String nombre, double precio) {
            this.id = id;
            this.nombre = nombre;
            this.precio = precio;
        }
    }
}
