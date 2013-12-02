package server;

import java.io.*;
import com.sun.net.httpserver.*;
import java.lang.String;
import java.util.Scanner;

class SimpleHttpHandler implements HttpHandler {

    public void handle(HttpExchange httpExchange) throws IOException {
	String      method    = httpExchange . getRequestMethod();
	String      request   = httpExchange . getRequestURI().toString();
	String      fileName  = "." +  request;
	System.out.println(method + "'ing '" + fileName + "' now...");
	
	StringBuilder text    = new StringBuilder();
	String        NL      = System.getProperty("line.separator");
	String        sepStr  = "";
	boolean       qSetSep = true; 
	Scanner scanner = new Scanner(new FileInputStream(fileName));
	try {
	    while (scanner.hasNextLine()){
		text.append(sepStr + scanner.nextLine());
		if(qSetSep) {
		    qSetSep = false; 
		    sepStr  = NL;
		}
	    }
	}
	finally{
	    scanner.close();
	}
	String response = text . toString();
	System.out.println("SERVER DEBUG File content: " + response);
	httpExchange.sendResponseHeaders(200, 0);//response.length());
	OutputStream os = httpExchange.getResponseBody();
	os.write(response.getBytes());
	try {
	    Thread.sleep(1000);
	}
	catch(Throwable t) {
	    // nothing;
	}
	os.close();
    }
}
