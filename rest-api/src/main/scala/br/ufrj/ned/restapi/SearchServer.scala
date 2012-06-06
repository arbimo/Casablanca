package br.ufrj.ned.restapi

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;

import javax.ws.rs.core.UriBuilder;

import br.ufrj.ned.backendmanager._



object SearchServer {

    private def getPort(defaultPort : Int) = {
        val port = System.getProperty("jersey.test.port");;

        if (null != port)
            try  {
              Integer.parseInt(port);
            } catch {
                case ex: NumberFormatException => defaultPort;
            }
        else
            defaultPort;
    }

    private def getBaseURI() = {
        UriBuilder.fromUri("http://localhost/").port(getPort(9998)).
            path("ned").build();
    }

    val BASE_URI = getBaseURI();

    def startServer() = {
        val rc = new PackagesResourceConfig("br.ufrj.ned.restapi");

        println("Starting grizzly...");
        GrizzlyServerFactory.createHttpServer(BASE_URI, rc);
    }

    def main(args: Array[String]) {

        /* Starting Backend manager */
        BackendManager.start()
        if(System.getenv("UFRJ_NED_CONF") != null)
          BackendManager.loadFromDir(System.getenv("UFRJ_NED_CONF"))

        val httpServer = startServer();

        println("Server running");
        println("Hit return to stop...");
        System.in.read();
        println("Stopping server");
        httpServer.stop();
        BackendManager ! 'quit
        println("Server stopped");
        System.exit(0);
    }
}
