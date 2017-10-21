import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CancerOnDeviantArt extends WebSpider
	{
		
		// visted webs and the image link. 
		public static TreeMap<String, URL> G_visitedWebs = new TreeMap<String, URL>();
		
		//This is the content we want to get from this page. 
		public URL G_desirecontent;
		
		public CancerOnDeviantArt(String link, String referedlink, int tag) throws IOException {
			super(link, referedlink, tag);
			
			// prepare for growing. 
			this.grabContent();
			this.getSimiliarWebs();
			G_visitedWebs.put( this.G_ThisPageURL , this.G_desirecontent);
		}
		
		public CancerOnDeviantArt(String link, int tag) throws IOException
		{
			this(link,null,tag);
		}
		
		private ArrayList<String> Sub_getMoreDA()
		{
			Document arg = this.G_ThispageContent;
			ArrayList<String > slist = new ArrayList<String>();
			Elements eles = arg.select("div.tinythumb");
			int c = 0;
			for(Element e : eles)
			{
				if(c++==9)break;
				slist.add(e.parent().attr("href"));
			}
			return slist;
		}
		
		private ArrayList<String> getLinksinDescription()
		{
			ArrayList<String> astrlist = new ArrayList<String>();
			
			Elements ele = this.G_ThispageContent.select(".dev-description")
					.select(".text-ctrl")
					.select(".text.block");
			
				//System.out.println(e.childNodeSize());
				System.out.println(ele.get(0).attr("abs:href"));
				
				for(Element e :ele.select("a[href]"))
				{
					astrlist.add(e.attr("abs:href"));
				}
			// Filter using the artist name. 
				String artistname = this.getArtistName();
				Iterator<String> itr =astrlist.iterator();
				
			while(itr.hasNext())
			{
				String temp = itr.next();
				if(temp.contains("fav.me"))
				{
					// don't remove
				}
				else if(!temp.contains("deviantart")||!temp.contains(artistname))
				{
					
					itr.remove();
					
				}
				
			}
			
			return astrlist;
		}
		
		private String getArtistName()
		{
			Elements eles = this.G_ThispageContent.select(".dev-title-container h1 small")
					.select("span.username-with-symbol");
				
			String result = eles.text();
			
			if(result.length()>0)
				return result;
			else
			{return null;}
		}
		
		private Set<String> getMoreDA()
		{
			
			ArrayList<String> morelinks = this.Sub_getMoreDA();
			
			//add some new things to the collection from the super class! 
			
			ArrayList<String> slist =this.getLinksinDescription();
			
			for(String s : morelinks)
			{
				slist.add(s);
			}
			
			Set<String> linkset = new TreeSet<String>(slist);
			
			return linkset;
			
		}
		
		
		
		/**
		 * Compare to the new webs with all the links in the static field. 
		 * only if thesimilar wens are established. 
		 */
		private void trimOutVisitedWebs()
		{
			Iterator<String> itr = this.G_Similiarwebs.iterator();
			
			while(itr.hasNext())
			{
				String temp = itr.next();
				if(G_visitedWebs.keySet().contains(temp))
				{
					itr.remove();
				}
			}
			
		}
		
		
		@Override
		public WebSpider getSimiliarWebs() 
		{
			this.G_Similiarwebs=this.getMoreDA();
			this.trimOutVisitedWebs();
			return this;
		}

		@Override
		public WebSpider growTheWeb() throws IOException 
		{
			if(this.TAG<1)
			{
				System.out.println("recursion limit reached, refuse to grow. "+"Limit: "+this.TAG);
			}
			else
			{
				
				for(String s : this.G_Similiarwebs)
				{
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				this.createSynapse(
						new CancerOnDeviantArt(s, this.G_ThisPageURL, this.TAG-1).growTheWeb()
								   );
				}
			}
			return this;
		}

		@Override
		public WebSpider createSynapse( WebSpider ws) {
			this.G_Sypnapse.add(ws);
			return this;
		}

		@Override
		public WebSpider grabContent() throws IOException {
			if(this.G_desirecontent==null)this.G_desirecontent=getDAMainImag();
			return this;
		}
		
		private URL getDAMainImag() throws IOException
		{
			String highquality = this.getHighQualityDownloadImage();
			 if(highquality!=null)
			 {
				 return new URL(highquality);
			 }
			 else
			 {
				Elements eles = this.G_ThispageContent.getAllElements();
				eles = eles.select(".dev-content-full");
				for(Element e : eles)
				{
			
					System.out.println("DA link: "+e.baseUri());
					System.out.println("main Img: "+e.attr("abs:src"));
					return new URL(e.attr("abs:src"));
				}
			 }
			return null;
		}
		
		private String getHighQualityDownloadImage()throws MalformedURLException, IOException
		{
			String downloadlink = getDownloadlink(this.G_ThispageContent );
			if(downloadlink == null)
			{
				return null;
			}
			return connectUsingCookie(downloadlink);
		}
		
		
		private String connectUsingCookie(String link) throws IOException
		{
			Response res = Jsoup.connect(link)
					.userAgent(this.ANGENTINUSE)
					.referrer(this.G_ThisPageURL)
					.cookies(this.G_cookies)
					.followRedirects(true)
					.ignoreContentType(true)
					.execute();
			
			int responsecode = res.statusCode();
			
			System.out.println("This is the response code from the server: ");
			System.out.println(responsecode);
			if(responsecode==404||responsecode == 403)
			{
				System.out.println("Service denied, sleep...");
				
				try {
					Thread.sleep(1000*2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				return null;
			}
		
			System.out.println("Put the respnse into url we get: ");
			System.out.println(res.url());
			
			return res.url().toString();
		}
		
		/**
		 * Get the download link on the page. 
		 * @param doc
		 * @return
		 */
		private static String getDownloadlink(Document doc)
		{
			String downloadlink =null; 
			Elements eles = doc.select("a.dev-page-download");
			if(eles.size()>0)
			{
				System.out.println("There is a download botton on this deviant art page. ");
				 downloadlink = eles.get(0).attr("href");
			}
			return downloadlink;
		}
		
		
		
		//try to get favorite images in an account's web page. 
		public static class GetFavorite extends CancerOnDeviantArt
		{

			public GetFavorite(String link, String referedlink, int tag) throws IOException {
				super(link, referedlink, tag);
			
			}
			
		}
		
	}

