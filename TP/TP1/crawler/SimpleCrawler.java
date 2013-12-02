package crawler;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.*;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

public class SimpleCrawler {

    public static void main(String[] args) throws Throwable {
	final String usage = "usage: <port> <fileName> <path>";
	if (args . length < 3) {
	   throw new Exception(usage);
	}
	// on se prend la liste des URLs a parcourir
	Queue<CrawlerUrl> urlQueue = new LinkedList<CrawlerUrl>();
	// l'url de demarrage du crawler
	String url = "http://localhost:" + args[0] + "/data/"+args[1];
	// on le met dans la liste
	urlQueue.add(new CrawlerUrl(url, 0));
	System.out.println("Demarrage du crawler a partir de "+url+ " ...");
	SimpleCrawler crawler=new SimpleCrawler(urlQueue,100,3,100L,args[2]);
	System.out.println("Crawling crawling crawling ...");
	crawler.crawl();
	System.out.println("Crawl high ! C'est bon, on a fini, au revoir.");
    }
    
j
    private final        String outputPath;
    private final        String NL = System.getProperty("line.separator");

    private static final String USER_AGENT = "User-agent:";
    private static final String DISALLOW   = "Disallow:";

    private int maxNumberUrls;
    private long delayBetweenUrls;
    private int maxDepth;

    private Map<String, CrawlerUrl> visitedUrls             = null;
    private Map<String, Collection<String>> sitePermissions = null;

    private Queue<CrawlerUrl> urlQueue                      = null;
    private BufferedWriter crawlOutput                      = null;
    private BufferedWriter crawlStatistics                  = null;
    private int numberItemsSaved                            = 0;

    public SimpleCrawler(Queue<CrawlerUrl> urlQueue, int maxNumberUrls,
			 int maxDepth, long delayBetweenUrls, String outP)
            throws Exception {
	System.out.println("SimpleCrawler::SimpleCrawler()");
	this.outputPath       = outP;
        this.urlQueue         = urlQueue;
        this.maxNumberUrls    = maxNumberUrls;
        this.delayBetweenUrls = delayBetweenUrls;
        this.maxDepth         = maxDepth;
        this.visitedUrls      = new HashMap<String, CrawlerUrl>();
        this.sitePermissions  = new HashMap<String, Collection<String>>();
        crawlOutput           = new BufferedWriter(// pour ecrire dans fichier
	    new FileWriter(outputPath + "/crawl.txt"));  // on y mettra 
       // les URLs et les docIds, un par un 
        crawlStatistics       = new BufferedWriter(// pour ecrire dans fichier
	    new FileWriter(outputPath + "/crawlStatistics.txt"));//on y mettra
       // le nombre de liens visites, etc. -- c'est moins important
    }

    public void crawl() throws Exception { // methode principale 
        while (continueCrawling()) { // tant qu'on a des URLs a parcourir
            // et qu'on remplit les conditions (profondeur, n max, etc.) 
            CrawlerUrl url = getNextUrl(); // au suivant 
            if (url != null) {
                printCrawlInfo(); // affichage+sauvgrd dans crawlStatistics
		getContent(url);  // recuperation du contenu depuis le web 
		saveContent(url); // sauvgrd texte dans download_<docId>.txt
		                  // du titre dans title_<docId>.txt, etc. 
		addUrlsToUrlQueue(url); // liens sortants dans la queue
		saveLinks(url);   //liens sortants -> outlinkurls_<docId>.txt
		numberItemsSaved++; // numberItemsSaved donne le docId
                Thread.sleep(this.delayBetweenUrls); // pour ne pas assommer 
		                                     // les serveurs 
            }
        }
        closeOutputStream();   // flush() et close() pour crawl.txt et stats 
	generateOutlinksIds(); // lecture de crawl.txt avec construction de 
                               // la table de hachage et puis pour chaque 
                               // outlinkurls_<docId>.txt, ecriture de 
	                       // outlinks_<docId>.txt qui contient les 
	                       // docIds atteignables 
    }
    
    private boolean continueCrawling() { // savoir si on peut continuer
	boolean qContinue = false;
	// TRAVAIL A FAIRE
	return(qContinue);
    }

    private CrawlerUrl getNextUrl() {  // obtenir l'URL suivant a explorer 
        CrawlerUrl nextUrl = null;
        while ((nextUrl == null) && (!urlQueue.isEmpty())) {
            CrawlerUrl crawlerUrl = this.urlQueue.remove();
            if (doWeHavePermissionToVisit(crawlerUrl)
                    && (!isUrlAlreadyVisited(crawlerUrl))
                    && isDepthAcceptable(crawlerUrl)) {
                nextUrl = crawlerUrl;
                System.out.println("Le prochain url a visiter est "+nextUrl);
            }
        }
        return nextUrl;
    }

    // maintenant il y a quelques methodes auxiliaires 

    private void printCrawlInfo() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("Queue length = ").append(this.urlQueue.size()).append(
                " visited urls=").append(getNumberOfUrlsVisited()).append(
                " site permissions=").append(this.sitePermissions.size());
        crawlStatistics.append("" + getNumberOfUrlsVisited()).append(
                "," + numberItemsSaved).append("," + this.urlQueue.size())
                .append("," + this.sitePermissions.size() + "\n");
        crawlStatistics.flush();
        System.out.println(sb.toString());
    }

    private int getNumberOfUrlsVisited() {
        return this.visitedUrls.size();
    }

    private void closeOutputStream() throws Exception {
        crawlOutput.flush();
        crawlOutput.close();
        crawlStatistics.flush();
        crawlStatistics.close();
    }
    
    private boolean isDepthAcceptable(CrawlerUrl crawlerUrl) {
        return crawlerUrl.getDepth() <= this.maxDepth;
    }

    private boolean isUrlAlreadyVisited(CrawlerUrl crawlerUrl) {
        if ((crawlerUrl.isVisited())
	    || (this.visitedUrls.containsKey(crawlerUrl.getUrlString()))) {
            return true;
        }
        return false;
    }

    public boolean doWeHavePermissionToVisit(CrawlerUrl crawlerUrl) {
        if (crawlerUrl == null) {
            return false;
        }
        if (!crawlerUrl.isCheckedForPermission()) {
            crawlerUrl.setAllowedToVisit(
		computePermissionForVisiting(crawlerUrl));
        }
        // We need to check
        return crawlerUrl.isAllowedToVisit();
    }


    private boolean computePermissionForVisiting(CrawlerUrl crawlerUrl) {
        URL url = crawlerUrl.getURL();
        boolean retValue = (url != null);
        if (retValue) {
            String host  = url.getHost();
	    String port  = "";
	    int    portN = url.getPort();
	    if(portN > -1) {
		port = ":" + String.valueOf(portN);
	    }
            Collection<String> disallowedPaths=this.sitePermissions.get(host);
            if (disallowedPaths == null) {
                disallowedPaths = 
		    parseRobotsTxtFileToGetDisallowedPaths(host+port);
            }
            // On itere pour tous les chemins interdits (disallowed paths)
            String path = url.getPath();
            for (String disallowedPath : disallowedPaths) {
                if (path.contains(disallowedPath)) {
                    retValue = false;
                }
            }
        }
        return retValue;
    }

    private Collection<String> parseRobotsTxtFileToGetDisallowedPaths(
            String host) {
        String robotFilePath = getContent("http://" + host + "/robots.txt");
        Collection<String> disallowedPaths = new ArrayList<String>();
        if (robotFilePath != null) {
            Pattern p = Pattern.compile(USER_AGENT);
            String[] permissionSets = p.split(robotFilePath);
            String permissionString = "";
            for (String permission : permissionSets) {
                if (permission.trim().startsWith("*")) {
                    permissionString = permission.substring(1);
                }
            }
            p = Pattern.compile(DISALLOW);
            String[] items = p.split(permissionString);
            for (String s : items) {
                disallowedPaths.add(s.trim());
                // System.out.println(s.trim());
            }
        }
        this.sitePermissions.put(host, disallowedPaths);
        return disallowedPaths;
    }
    
    private String getContent(String urlString) {
        return getContent(new CrawlerUrl(urlString, 0));
    }

    private String getContent(CrawlerUrl url) { // methode essentielle -- 
        // recuperation du fichier .html depuis le serveur
	HttpClient httpclient = new DefaultHttpClient();
	String text = new String();
        try {
            HttpGet httpget = new HttpGet(url.getUrlString()); //construction
	    //  de l'objet qui fera la connexion

            System.out.println("executing request " + httpget.getURI());
            // construction de l'objet qui gerera le dialogue avec le serveur 
            ResponseHandler<String> responseHandler = 
		new BasicResponseHandler();
            text = httpclient.execute(httpget, responseHandler); //et on y va
            System.out.println("----------------------------------------");
            System.out.println(text);
            System.out.println("----------------------------------------");
	}
	catch(Throwable t) {
	    System.out.println("OOPS YIKES "+ t . toString());
	    t . printStackTrace();
	}
	finally {
            // Lorsque on n'a plus besoin de l'objet de type HttpClient
            // on ferme la connexion pour eliberer rapidement les resources
            // systeme qu'on avait monopolisees
            httpclient.getConnectionManager().shutdown();
        }
	// appeler la methode de SimpleCrawler qui marque l'URL comme visite
	// et qui l'insere dans la liste des URLs visites

	// appeler la methode de CrawlerUrl qui recoit le texte HTML brut
	//  (et le donne au parseur jsoup, pour en extraire le texte, le titre,
	//   les liens, etc,); l'objet CrawlerUrl a utiliser est 'url'
        return text;
    }
    
    private void markUrlAsVisited(CrawlerUrl url) {
        this.visitedUrls.put(url.getUrlString(), url);
        url.setIsVisited();
    }

    private void saveContent(CrawlerUrl url)  throws Exception {
	String fileId = String.valueOf(numberItemsSaved);
	BufferedWriter rankOutput     =
	    new BufferedWriter(
		new FileWriter(outputPath + "/rankscore_"+ fileId +".txt"));
	rankOutput     . write("0.0");
        rankOutput     . flush();
        rankOutput     . close();		

	String docContent;
	String docTitle;  

	// TRAVAIL A FAIRE

	// recuperez dans docContent le texte extrait avec jsoup de la 
	// variable url, en utilisant une methode de CrawlerUrl 

	// recuperez dans docTitle le titre extrait avec jsoup de la 
	// variable url, en utilisant une methode de CrawlerUrl 

	// tout comme pour 'rankscore', creez le fichier de nom
        //      outputPath + "/download_"+ fileId +".txt"
        // sauvegarder dedans docContent
        // faire flush() et close() 

	// sauvegardez le titre dans son fichier, similairement

	// sauvegardez l'url.getUrlString() dans son fichier, similairement
    }
        
    private void saveLinks(CrawlerUrl url) throws Exception {
	Collection<String> urlStrings = url . getLinks();
	// TRAVAIL A FAIRE 
	
	// mettez l'indice du document courant (variable numberItemsSaved)
        // comme String dans fileId 

	// creez le fichier nomme
	//
	//    outputPath + "/outlinkurls_"+ fileId +".txt"
	//

        // sauvegardez dedans iterativement chaque URL de la collection
        // urlStrings, suivie de NL

	// flusher et fermer l'ecriture
    }
    
    private void addUrlsToUrlQueue(CrawlerUrl url) {
	// TRAVAIL A FAIRE

	// recuperer la collection d'URL a l'aide d'une methode
	// de la classe CrawlerUrl

        int depth = url.getDepth() + 1;

	// pour chaque chaine de caracteres URL de la collection
        //
	///// si la liste (table de hachage) des URL visites (visitedUrls) 
	/////    ne contient pas cette URL
	/////    
        ////////// rajouter a la queue des URL a visiter (methode Queue.add())
	////////// un nouvel objet de type CrawlerUrl. Le constructeur prend
	////////// comme arguments cette chaine URL et la variable depth
	/////    
	///// finsi
        //
    }

    private void generateOutlinksIds() throws Exception{
	BufferedWriter nTotDocOutput = 
	    new BufferedWriter(
		new FileWriter(outputPath + "/nTotDocs.txt"));
	nTotDocOutput . write(String . valueOf(numberItemsSaved));
	nTotDocOutput . flush();
	nTotDocOutput . close();
	System.out.println("Enfin: mapping des outlinkurls dans des outlink"+ 
			   " ids, pour ecrire les fichiers outlink_");
	// TRAVAIL A FAIRE
	
        // declarez un objet nomme urlToIdMap de la classe Map, et
        // instanciez-le avec une nouvelle HashMap<String,String,>

        // creez un nouveau Scanner d'un nouveau FileInputStream pour
        // lire le fichier nomme
        //        outputPath + "/crawl.txt"
        //
        // (un exemple de code de lecture de fichier se trouve dans
        // server/SimpleHttpHandler.java)

        // pour chaque ligne lue depuis ce fichier
        //////
        ////// separer la ligne a l'aide de String.split("\\s+")
        //////
        ////// mettez les deux morceaux en tant que nouvelle entree dans 
        ////// urlToIdMap (methode .put())
        //
	// finboucle

	System.out.println("J'ai lu tout le crawl.txt, et je "+
			   "vais ecrire les outlink_ files");

	// boucle pour chaque entree dans la table de hachage urlToIdMap
	////
	//// se prendre un objet de type StringBuilder
	////
        //// ouvrir en lecture le fichier de liens nomme 
	////      outputPath + "/outlinkurls_" + docId + ".txt" 
	//// (avec un nouveau Scanner, etc. comme plus haut)
	////
	////
	//// pour chacune de ses lignes (dans une boucle), 
	////    obtenir l'id du document depuis urlToIdMap (s'il est dedans)
	////    rajouter cet id a notre objet StringBuilder a l'aide
	////    de la methode append()
	//// 
	//// finboucle
	//// 
	//// ecrire le .toString() de notre objet StringBuilder dans 
	//// le fichier de liens par id nomme
	////
	////     outputPath + "/outlinks_"+ docId +".txt"
	////
	// finBoucle
    }
}

