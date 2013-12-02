package server;

import java.io.*;
import java.net.*;
import com.sun.net.httpserver.*;
import java.lang.String;

import server.SimpleHttpHandler;

class SimpleHttpServer {

    public static void main(String[] args) throws Throwable {
	final String usage = "usage: <port>";
	if (args . length < 1) {
	    throw new Exception(usage);
	}	
	final int   port   = Integer . valueOf(args[0]);
	System.out.println("HttpServer(): listening on port " + String.valueOf(port));
	HttpServer server = HttpServer.create(new InetSocketAddress(port), 5);
	server.createContext("/", new SimpleHttpHandler());
	server.setExecutor(null); // creates a default executor
	server.start();
    }
}


