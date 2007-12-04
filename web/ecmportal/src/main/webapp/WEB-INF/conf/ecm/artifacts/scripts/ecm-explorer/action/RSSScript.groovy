/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

import java.util.Map;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.cms.scripts.CmsScript;
import org.exoplatform.services.cms.rss.RSSService;
import org.exoplatform.services.jcr.impl.core.NodeImpl;

public class RSSScript implements CmsScript {
  
  private RepositoryService repositoryService_;
  private RSSService rssService_;
  
  public RSSScript(RepositoryService repositoryService, RSSService rssService) {
		repositoryService_ = repositoryService;
		rssService_ = rssService;
  }
  
  public void execute(Object context) {
    Map variables = (Map) context;
    
    String feedType = (String) context.get("exo:feedType") ;    
    
    if(feedType.equals("rss")) {
    	
    	println("***  RSS FEED BUILDING...   ***");    
			
			rssService_.generateFeed(context);
			
		  println("***  BUILD SUCCESSFULL  ***");  
    
    } else if(feedType.equals("podcast")) {
    
			println("***  PODCAST FEED BUILDING... ***");    

			rssService_.generateFeed(context);

			println("***  BUILD SUCCESSFULL  ***");  
    
    }else if(feedType.equals("video podcast")){
    
    	println("***  VIDEO PODCAST FEED BUILDING... ***");    
			
			rssService_.generateFeed(context);
			
			println("***  BUILD SUCCESSFULL  ***");  
			
    } else {
    	
    	println("***  NO BUILD FEED ACTION DONE ***");    
    	
    }
          
  }

  public void setParams(String[] params) {}

}
