import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider; 
import java.io.IOException;
import java.net.URI;
import rest.LibrosResource;

public class ServidorJersey {
    public static final String BASE_URI = "http://0.0.0.0:8080/rest/";

    public static HttpServer startServer() {
        final ResourceConfig rc = new ResourceConfig().packages("rest"); 
        rc.register(JacksonJsonProvider.class); 

        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        System.out.println(String.format("Servidor REST Jersey-Grizzly iniciado en %s%s%nHit enter para detenerlo...",
                BASE_URI, "libros"));

        System.in.read();
        server.shutdownNow();
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
