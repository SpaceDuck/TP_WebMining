package crawler;

import java.net.MalformedURLException;
import java.net.URL;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

public class CrawlerUrl {
    // donnees et methodes pour l'URL elle-meme (en tant qu'"addresse web")

    private int depth = 0;
    private String urlString = null;
    private URL url = null;
    private boolean isAllowedToVisit;
    private boolean isCheckedForPermission = false;
    private boolean isVisited = false;

    public CrawlerUrl(String urlString, int depth) {
        this.depth = depth;
        this.urlString = urlString;
        computeURL();
    }

    private void computeURL() {
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
	    // petit probleme
        }
    }

    public URL getURL() {
        return this.url;
    }

    public int getDepth() {
        return this.depth;
    }

    public boolean isAllowedToVisit() {
        return isAllowedToVisit;
    }

    public void setAllowedToVisit(boolean isAllowedToVisit) {
        this.isAllowedToVisit = isAllowedToVisit;
        this.isCheckedForPermission = true;
    }

    public boolean isCheckedForPermission() {
        return isCheckedForPermission;
    }

    public boolean isVisited() {
        return isVisited;
    }

    public void setIsVisited() {
        this.isVisited = true;
    }

    public String getUrlString() {
        return this.urlString;
    }

    @Override
    public String toString() {
        return this.urlString + " [depth=" + depth + " visit="
                + this.isAllowedToVisit + " check="
                + this.isCheckedForPermission + "]";
    }

    @Override
    public int hashCode() {
        return this.urlString.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == this.hashCode();
    }

    // donnees et methodes concernant le contenu telecharge depuis l'URL

    private String       htmlText;
    private Document     htmlJsoupDoc;
    private String       niceText;
    private String       title;
    private List<String> linkList;

    public String getNiceText() {
	return(niceText);
    }

    public String getTitle() {
	return(title);
    }

    public List<String> getLinks() {
	return(linkList);
    }

    public void setRawContent(String htmlText) {
	String baseURL  = getURL() . toExternalForm();
	this . htmlText = htmlText;
	htmlJsoupDoc    = Jsoup . parse(htmlText,baseURL);
	title           = htmlJsoupDoc . title();
	niceText        = htmlJsoupDoc . body() . text();
	linkList        = new ArrayList<String>();
	Elements hrefJsoupLinks = htmlJsoupDoc . select("a[href]");
        for (Element link : hrefJsoupLinks) {
	    String thisLink = link.attr("abs:href");
	    if(thisLink . startsWith("http://")) {
		System.out.println("JSOUP Found: " + thisLink);
		linkList . add(thisLink);
	    }
	}		
    }

}
