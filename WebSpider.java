

/**
 * This is a generization of the comic download class. 
 * @author victo
 *
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


public abstract class WebSpider {

	public Document G_ThispageContent;
	
	public Response G_response;
	
	public String G_ThisPageURL;
	
	public Map<String,String> G_cookies;
	
	public int TAG; // level of recursion.
	
	private static final String[] ALLUSERAGENT={"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36",
			//something weird
			"Mozilla",
			//Windows edge. 
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.246",
			// Ie 11				
			"Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; AS; rv:11.0) like Gecko"					};
	
	public final String ANGENTINUSE= ALLUSERAGENT[new Random().nextInt(ALLUSERAGENT.length)];	
	
	public Set<String> G_Similiarwebs;
	
	public HashSet<WebSpider> G_Sypnapse;// create a map to all the other spanning webs. 
	

	/**
	 * This is the most genral constructor for this class.
	 * @param link
	 * @param referedlink
	 * @param tag
	 * @throws IOException
	 */
	public WebSpider(String link, String referedlink, int tag) throws IOException
	{
		if(referedlink!=null)
		this.loadURL(link,referedlink);
		else
		{
			this.loadURL(link);
		}
		
		//set up fields to prevent null pointer.
		
		this.G_Similiarwebs= new TreeSet<String>();
		this.G_Sypnapse = new HashSet<WebSpider>();
		this.TAG=tag;
		
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
				this.G_ThisPageURL = response.url().toString();
				this.G_ThispageContent= response.parse();
				
				return this;
		}
	
	catch (SocketTimeoutException e) 
	{
		System.out.println("Time out; reloading. ");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			
		}
		return loadURL(arg);
	}
	
	}
	
	/**
	 * methods that setup the field. 
	 * @param url
	 * @param referedlink
	 * @return
	 * @throws IOException
	 */
	private WebSpider loadURL(String url,String referedlink) throws IOException
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
				this.G_ThisPageURL=response.url().toString();
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
	
	public abstract WebSpider growTheWeb() throws IOException;
	
	public abstract WebSpider grabContent() throws IOException; 
	
	public abstract WebSpider createSynapse(WebSpider ws);
	
	
	public String toString()
	{
		return "This is the web link:"+this.G_ThisPageURL;
	}
	
	private void printSturcture(String indent)
	{
		System.out.println(this);
		if(this.G_Sypnapse!=null&&this.G_Sypnapse.size()>0)
		{
			System.out.println("Subwebs: ");
			for(WebSpider w :this.G_Sypnapse )
			{
				System.out.println(indent+" " + w);
			}
		}
	}
	
	public void printSturcture()
	{
		this.printSturcture(" ");
	}
	
	/**
	 * gives a string of url and this this will travel through the structure to 
	 * see if url has been visited. it doesn't check the similiar webs in each obj.
	 * @return
	 */
	public boolean haveVisited(String url)
	{
		if(this.G_ThisPageURL.equals(url))
		{return true;}
		else
		{
				Iterator<WebSpider> itr = this.G_Sypnapse.iterator();
				while(itr.next()!=null)
				{
					if(itr.next().haveVisited(url))return true;
				}
				return false;
		}
		
	}
	
	
	/**
	 * 
	 * This is a class that will handle the dowonloading features of the pages. 
	 * Gien the web link,and the directory, this class will do all the things 
	 * automaticaly, sequencially. 
	 * @author victo
	 *
	 */
	public static class SmartDownload
	{
		
		public final String G_direct;
		// the system directory£¡
		
		
		public SmartDownload(String Dire)
		{
			this.G_direct = Dire;
		}

		public SmartDownload BulkDownload(Set<URL> pool) throws MalformedURLException, IOException
		{
			for(URL s : pool)
			{
				if(s!= null)this.downLoad(s);
			}
			return this;
		}
		
		
		
		public SmartDownload downLoad(String url) throws MalformedURLException, IOException
		{
			downloadFromURL(new URL(url), this.G_direct);
			return this;
		}
		
		
		
		public SmartDownload downLoad(URL url) throws MalformedURLException, IOException
		{
			
			downloadFromURL(url, this.G_direct);
			
			return this;
			
		}
		
		
		
		
		public static File downloadFromURL(URL arg, String directory) throws IOException 
		{
			
			File f = new File(directory+URLTrimToString(arg));
			
			if(!f.getParentFile().exists())
			{
				f.getParentFile().mkdirs();
			}
			if(!f.exists())
			{
			
			BufferedInputStream bis = new BufferedInputStream(arg.openStream());
			FileOutputStream fos = new FileOutputStream(f);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			
			for ( int i; (i = bis.read()) != -1; ) {
			    bos.write(i);
			}
			bis.close();
			bos.close();
			
			System.out.println(f.toString()+">>>>> Is created from the url.");
			}
			else
			{
				System.out.println("The file aready exists. ");
			}
			
			return f; 
		}

		/**
		 * gives a web link it will trim to the last 
		 * string section that can be used as a proper name.
		 * @param arg
		 * @return
		 */
		public static String URLTrimToString(URL arg)
		{
			String str = arg.toString();
			int i = str.lastIndexOf('/');
			str = str.substring(i, str.length());
			return str;
		}
		
		
		
	}
	
	
	
}
	
	