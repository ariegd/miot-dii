import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider; 

// Imports para Inyección de Dependencias (HK2)
import org.glassfish.hk2.utilities.binding.AbstractBinder; 
// Imports de Mongo
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.io.IOException;
import java.net.URI;
import rest.LibrosResource;

public class ServidorJersey {
    public static final String BASE_URI = "http://0.0.0.0:8080/rest/";
    
    // Variable para mantener el cliente vivo
    private static MongoClient mongoClient;

    public static HttpServer startServer() {
        String connectionString = "mongodb://mongo-db:27017";
        mongoClient = MongoClients.create(connectionString);
        MongoDatabase db = mongoClient.getDatabase("biblioteca");
    
        final ResourceConfig rc = new ResourceConfig().packages("rest"); 
        rc.register(JacksonJsonProvider.class); 

        rc.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(db).to(MongoDatabase.class);
            }
        });

        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        System.out.println(String.format("Servidor REST Jersey-Grizzly iniciado en %s%s", BASE_URI, "alumnos"));
         System.out.println(String.format("Servidor REST Jersey-Grizzly iniciado en %s%s", BASE_URI, "verduras"));
        System.out.println(String.format("Servidor REST Jersey-Grizzly iniciado en %s%s%nHit enter para detenerlo...",
                BASE_URI, "libros"));

        System.in.read();
        server.shutdownNow();
        if (mongoClient != null) {
                    mongoClient.close();
                    System.out.println("Conexión MongoDB cerrada.");
        }
    }
}


/*
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import java.util.Collections;

public class ServidorCXF {
    public static void main(String[] args) {
        JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
        factory.setResourceClasses(LibrosResource.class);
        factory.setResourceProvider(LibrosResource.class,
            new SingletonResourceProvider(new LibrosResource()));
        factory.setProviders(Collections.singletonList(
            new JacksonJsonProvider()));
        factory.setAddress("http://localhost:8080/rest");
        factory.create();
        System.out.println("Servidor REST CXF iniciado en "
            + "http://localhost:8080/rest/libros");
    }
}
*/
