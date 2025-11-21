package rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;

@Path("/alumnos")
public class AlumnosResource {
    private static final Map<Integer, Alumno> alumnosDB = new HashMap<>();
    
    static {
        alumnosDB.put(1, new Alumno(1, "Ana García", "Ingeniería IoT"));
        alumnosDB.put(2, new Alumno(2, "Luis Rodriguez", "Inteligencia Artificial"));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Alumno> obtenerAlumnos() {
        return alumnosDB.values();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response crearAlumno(@QueryParam("id") int id, 
                                @QueryParam("nombre") String nombre,
                                @QueryParam("curso") String curso) {
        if (alumnosDB.containsKey(id)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("El alumno con ese ID ya existe").build();
        }
        alumnosDB.put(id, new Alumno(id, nombre, curso));
        return Response.status(Response.Status.CREATED).entity(alumnosDB.get(id)).build();
    }

    @POST
    public Response actualizarAlumno(@QueryParam("id") int id, 
                                     @QueryParam("curso") String nuevoCurso) {
        if (!alumnosDB.containsKey(id)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        Alumno alumno = alumnosDB.get(id);
        alumno.curso = nuevoCurso;
        return Response.ok().entity(alumno).build();
    }

    @DELETE
    public Response eliminarAlumno(@QueryParam("id") int id) {
        if (alumnosDB.remove(id) == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok("Alumno eliminado").build();
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
