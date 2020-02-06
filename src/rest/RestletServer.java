package rest;

import org.apache.spark.network.sasl.SparkSaslServer;
import org.restlet.Server;
import org.restlet.data.Protocol;

public class RestletServer {

	public static void main(String[] args) throws Exception {

		Server server = new Server(Protocol.HTTP, 8080, RestService.class);
		server.start();
		RestService.init();

	}
}
