all: server.jar crawler.jar 
	touch all

server.jar : server/SimpleHttpServer.java server/SimpleHttpHandler.class makefile
	javac -source 1.6 -target 1.6 -cp . server/SimpleHttpServer.java && jar -cfe server.jar server/SimpleHttpServer server/SimpleHttpServer.class server/SimpleHttpHandler.class

server/SimpleHttpHandler.class: server/SimpleHttpHandler.java makefile
	javac -source 1.6 -target 1.6 -cp . server/SimpleHttpHandler.java

crawler.jar : crawler/SimpleCrawler.java crawler/CrawlerUrl.class makefile 
	javac -source 1.6 -target 1.6 -cp . crawler/SimpleCrawler.java && jar -cfe crawler.jar crawler/SimpleCrawler crawler org 

crawler/CrawlerUrl.class : crawler/CrawlerUrl.java makefile
	javac -source 1.6 -target 1.6 -cp . crawler/CrawlerUrl.java

clean :
	find . -name '*.class' -exec \rm -f {} \; ; \rm server.jar crawler.jar 

