

/**
 * This is a generization of the comic download class. 
 * @author victo
 *
 */

import java.net.*;import java.io.*; import java.util.*;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public abstract class WebSpider {

	public Document G_ThispageContent;
	
	public Response G_response;
	
	public URL G_ThisPageURL;
	
	public Map<String,String> G_cookies;
	
	public int TAG; // level of recursion.
	
	public static final String[] ALLUSERAGENT={"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36",
			//something weird
			"Mozilla",
			//Windows edge. 
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.246",
			// Ie 11				
			"Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; AS; rv:11.0) like Gecko"					};
	
	public final String ANGENTINUSE= ALLUSERAGENT[new Random().nextInt(ALLUSERAGENT.length)];	
	
	public Set<String> G_Similiarwebs;
	
	public HashSet<WebSpider> G_Sypnapse;// create a map to all the other spanning webs. 
	
	public WebSpider(String link, String referedlink, int tag) throws IOException
	{
		if(referedlink!=null)
		this.loadURL(link,referedlink);
		else
		{
			this.loadURL(link);
		}
		
	}
	
	public WebSpider(String link, int tag) throws IOException
	{
		this(link,null,tag);
	}
	
	
	/*
	 * This is a method that fake an agent and make a document out of 
	 * a url link. 
	 */
	private WebSpider loadURL(String arg) throws IOException
	{
	
		try
		{
		Response response = Jsoup.connect(arg)
				 
				  .userAgent(ANGENTINUSE)
				  
				  .timeout(10000).execute();
		
				this.G_cookies =response.cookies();
				this.G_response= response;
		
				this.G_ThispageContent= response.parse();
				
				return this;
		}
	
	catch (SocketTimeoutException e) 
	{
		System.out.println("Time out; reloading. ");
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e1) {
			
		}
		return loadURL(arg);
	}
	
	}
	
	public WebSpider loadURL(String url,String referedlink) throws IOException
	{
		try
		{
		Response response = Jsoup.connect(url)
				  //.data("query", "Java")
				  .userAgent(ANGENTINUSE)
				  .cookie("auth", "token").referrer(referedlink)
				  .timeout(10000).execute();
		
				this.G_cookies =response.cookies();
				
				this.G_response=response;
				
				this.G_ThispageContent = response.parse();
		
		return this;
		}
	
		catch (SocketTimeoutException e) 
		{
			System.out.println("Time out; reloading. ");
			try {
				Thread.sleep(15000);
			} catch (InterruptedException e1) {
				
			}
			return loadURL(url,referedlink);
		}
	
	}
	
	
	
	public abstract WebSpider getSimiliarWebs();
	
	public abstract WebSpider growTheWeb();
	
	public abstract WebSpider grabContent();
	
	public abstract WebSpider createSynapse();
	
	
	public String toString()
	{
		return "This is the web link\n\t"+this.G_ThisPageURL;
	}
	
}
	
	