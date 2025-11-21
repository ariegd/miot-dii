package rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;

@Path("/verduras")
public class VerdurasResource {
    private static final Map<Integer, Verdura> stock = new HashMap<>();
    
    static {
        stock.put(100, new Verdura(100, "Lechuga Romana", 0.99));
        stock.put(101, new Verdura(101, "Tomate Raf", 2.50));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Verdura> listarVerduras() {
        return stock.values();
    }

    @PUT
    public Response agregarVerdura(@QueryParam("id") int id, 
                                   @QueryParam("nombre") String nombre,
                                   @QueryParam("precio") double precio) {
        if (stock.containsKey(id)) {
            return Response.status(Response.Status.CONFLICT).build();
        }
        stock.put(id, new Verdura(id, nombre, precio));
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    public Response venderVerdura(@QueryParam("id") int id) {
        if (!stock.containsKey(id)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        stock.remove(id);
        return Response.ok("Verdura vendida/eliminada").build();
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
