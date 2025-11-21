package rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;

// Recurso REST que gestiona libros
@Path("/libros")
public class LibrosResource {

    private static final Map<Integer, String> libros = new HashMap<>();
    static {
        libros.put(1, "El Quijote");
        libros.put(2, "Cien años de soledad");
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Libro> obtenerLibros() {
        List<Libro> resultado = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : libros.entrySet()) {
            resultado.add(new Libro(entry.getKey(), entry.getValue()));
        }
        return resultado;
    }

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

    @POST
    public Response actualizarLibro(@QueryParam("id") int id, 
                                    @QueryParam("titulo") String titulo) {
        if (!libros.containsKey(id)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        libros.put(id, titulo);
        return Response.ok().build();
    }

    @DELETE
    public Response eliminarLibro(@QueryParam("id") int id) {
        if (!libros.containsKey(id)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        libros.remove(id);

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
