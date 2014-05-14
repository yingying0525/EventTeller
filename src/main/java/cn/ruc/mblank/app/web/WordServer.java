package cn.ruc.mblank.app.web;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;

import java.net.InetSocketAddress;

/**
 * Created by mblank on 14-4-21.
 */
public class WordServer {

    private int ServerPort = 8088;

    public void startServer() throws Exception {
        HttpServerProvider provider = HttpServerProvider.provider();
        HttpServer httpserver = provider.createHttpServer(new InetSocketAddress(ServerPort), 100);
        httpserver.createContext("/", new WordHttpHandler());
        httpserver.setExecutor(null);
        httpserver.start();
        System.out.println("server started");
    }

    public static void main(String[] args) throws Exception {
        WordServer ps = new WordServer();
        ps.startServer();
    }

}
