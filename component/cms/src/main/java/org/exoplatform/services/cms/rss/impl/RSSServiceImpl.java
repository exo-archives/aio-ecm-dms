package org.exoplatform.services.cms.rss.impl;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.PortalContainerInfo;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.cms.rss.RSSService;
import org.exoplatform.services.jcr.RepositoryService;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEnclosureImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedOutput;
import com.totsp.xml.syndication.itunes.EntryInformation;
import com.totsp.xml.syndication.itunes.EntryInformationImpl;
import com.totsp.xml.syndication.itunes.FeedInformation;
import com.totsp.xml.syndication.itunes.FeedInformationImpl;
import com.totsp.xml.syndication.itunes.types.Category;
import com.totsp.xml.syndication.itunes.types.Duration;
import com.totsp.xml.syndication.itunes.types.Subcategory;

/**
 * @author Nguyen Quang Hung
 * @mail   nguyenkequanghung@yahoo.com 
 */

public class RSSServiceImpl implements RSSService{

  static private String SRC_WORKSPACE = "srcWorkspace".intern() ;
  static private String RSS_VERSION = "exo:rssVersion".intern() ;
  static private String FEED_TITLE = "exo:feedTitle".intern() ;
  static private String FEED_TYPE = "exo:feedType".intern() ;
  static private String DESCRIPTION = "exo:description".intern() ;
  static private String STORE_PATH = "exo:storePath".intern() ;
  static private String KEYWORDS = "exo:keywords".intern() ;
  static private String TITLE = "exo:title".intern() ;
  static private String LINK = "exo:link".intern() ;
  static private String LANGUAGE = "exo:language".intern() ;
  static private String COPYRIGHT = "exo:copyright".intern() ;
  static private String PUBDATE = "exo:pubDate".intern() ;
  static private String OWNER_NAME = "exo:ownerName".intern() ;
  static private String OWNER_MAIL = "exo:ownerEmail".intern() ;
  static private String IMAGE_URL = "exo:imageURL".intern() ;
  static private String CATEGORY = "exo:podcastCategory".intern() ;
  static private String PUBLISHED_DATE = "exo:publishedDate".intern() ;
  static private String AUTHOR = "exo:author".intern() ;
  static private String EXPLICIT = "exo:explicit".intern() ;
  static private String FEED_NAME = "exo:feedName".intern() ;
  static private String QUERY_PATH = "exo:queryPath".intern() ;
  static private String URL = "exo:url".intern() ;
  static private String SUMMARY = "exo:summary".intern() ;
  static private String LENGTH = "exo:length".intern() ;
  static private String JCR_CONTENT = "jcr:content".intern() ;
  static private String JCR_DATA = "jcr:data".intern() ;
  static private String JCR_MIMETYPE = "jcr:mimeType".intern() ;
  static private String JCR_LASTMODIFIED = "jcr:lastModified".intern() ;
  static private String NT_UNSTRUCTURED = "nt:unstructured".intern() ;
  static private String NT_FILE = "nt:file".intern() ;
  static private String NT_RESOURCE = "nt:resource".intern() ;  
  static private String RSS = "/rss".intern() ;
  static private String MIX_VERSIONABLE = "mix:versionable".intern() ;

  private RepositoryService repositoryService_;
  private CmsConfigurationService cmsConfigService_ ;
  public RSSServiceImpl(RepositoryService repositoryService, 
      CmsConfigurationService cmsConfigService) {
    repositoryService_ = repositoryService;
    cmsConfigService_ = cmsConfigService ;
  }

  public void generateFeed(Map context) {
    String feedType = (String) context.get(FEED_TYPE) ;
    if(feedType.equals("rss")) {
      generateRSS(context) ;
    }else if(feedType.equals("podcast") || feedType.equals("video podcast")) {
      generatePodcast(context) ;
    }
  }

  private void generateRSS(Map context) {  
    String actionName = (String)context.get("actionName") ;
    String srcWorkspace = (String)context.get(SRC_WORKSPACE);                   
    String rssVersion = (String) context.get(RSS_VERSION) ;
    String feedTitle = (String) context.get(FEED_TITLE) ;
    String feedType = (String) context.get(FEED_TYPE) ;
    String feedDescription = (String) context.get(DESCRIPTION) ;
    String storePath = (String) context.get(STORE_PATH) ;
    String feedName = (String) context.get(FEED_NAME) ;      
    String queryPath = (String) context.get(QUERY_PATH) ;
    String rssUrl = (String) context.get(URL) ;
    String repository = (String) context.get("repository") ;
    storePath = storePath + "/" + feedType ;
    if(feedName == null || feedName.length() == 0 || feedName.equals("")) 
      feedName = actionName ;
    if(feedTitle == null || feedTitle.length() == 0 || feedTitle.equals("")) 
      feedTitle = actionName ;        
    try {      
      Session session = repositoryService_.getRepository(repository).getSystemSession(srcWorkspace);
      session.refresh(true) ;
      QueryManager queryManager = session.getWorkspace().getQueryManager();
      Query query = queryManager.createQuery(queryPath, Query.XPATH);
      QueryResult queryResult = query.execute();            
      SyndFeed feed = new SyndFeedImpl();      
      feed.setFeedType(rssVersion);      
      feed.setTitle(feedTitle);
      feed.setLink("");
      feed.setDescription(feedDescription);     
      List<SyndEntry> entries = new ArrayList<SyndEntry>();
      SyndEntry entry;
      SyndContent description;
      ExoContainer container = ExoContainerContext.getCurrentContainer() ;
      PortalContainerInfo containerInfo = 
        (PortalContainerInfo)container.getComponentInstanceOfType(PortalContainerInfo.class) ;      
      String portalName = containerInfo.getContainerName() ; 
      String wsName = session.getWorkspace().getName() ;
      NodeIterator iter = queryResult.getNodes() ;
      System.out.println("---  \nThere are "+ iter.getSize() + " items in RSS  ---.\n") ;
      while (iter.hasNext()) {        
        Node child = iter.nextNode(); 
        if(child.isNodeType("exo:rss-enable")) {
          String url = getEntryUrl(portalName, wsName, child.getPath(), rssUrl) ;
          entry = new SyndEntryImpl();
          entry.setTitle(child.getName());                
          entry.setLink(url);        
          description = new SyndContentImpl();
          description.setType("text/plain");
          description.setValue(child.getProperty(SUMMARY).getString());
          entry.setDescription(description);        
          entries.add(entry);
          entry.getEnclosures() ;
        }        
      }      
      feed.setEntries(entries);      
      feed.setEncoding("UTF-8") ;     
      SyndFeedOutput output = new SyndFeedOutput();      
      String feedXML = output.outputString(feed);      
      feedXML = StringUtils.replace(feedXML,"&amp;","&");      
      storeXML(feedXML, storePath, feedName, repository);
      session.logout();
    } catch (Exception e) {
      e.printStackTrace();
    }     
  }

  private void generatePodcast(Map context){
    try{
      String actionName = (String)context.get("actionName") ;
      String srcWorkspace = (String)context.get(SRC_WORKSPACE);                   
      String feedTitle = (String) context.get(FEED_TITLE) ;      
      String feedLink = (String) context.get(LINK) ;
      String feedType = (String) context.get(FEED_TYPE) ;
      String feedDescription = (String) context.get(DESCRIPTION) ;
      String language = (String) context.get(LANGUAGE) ;
      String copyright = (String) context.get(COPYRIGHT) ;
      Date pubDate ; 
      try{
        pubDate = ((GregorianCalendar)context.get(PUBDATE)).getTime() ;
      }catch (Exception e) {
        pubDate= new Date() ;
      }
      String ownerName = (String) context.get(OWNER_NAME) ;
      String ownerEmail = (String) context.get(OWNER_MAIL) ;
      String imageURL = (String) context.get(IMAGE_URL) ;
      String categories = (String) context.get(CATEGORY) ;
      String keywords = (String) context.get(KEYWORDS) ;
      String storePath = (String) context.get(STORE_PATH) ;
      String feedName = (String) context.get(FEED_NAME) ;
      String rssVersion = (String) context.get(RSS_VERSION) ;
      String queryPath = (String) context.get(QUERY_PATH) ;
      String rssUrl = (String) context.get(URL) ;
      String repository = (String) context.get("repository") ;
      storePath = storePath + "/" + feedType ;
      if(feedName == null || feedName.length() == 0 || feedName.equals("")) 
        feedName = actionName ;
      if(feedTitle == null || feedTitle.length() == 0 || feedTitle.equals("")) 
        feedTitle = actionName ;

      Session session = repositoryService_.getRepository(repository).getSystemSession(srcWorkspace);
      session.refresh(true) ;
      QueryManager queryManager = session.getWorkspace().getQueryManager();
      Query query = queryManager.createQuery(queryPath, Query.XPATH);
      QueryResult queryResult = query.execute();            
      SyndFeed feed = new SyndFeedImpl() ;
      FeedInformation infor = new FeedInformationImpl() ;
      infor.setExplicit(false) ;

      Category cat = new Category() ;
      if(categories != null && categories.length() > 0) {
        if(categories.indexOf(",") > -1) {
          String[] arrCategories = categories.split(",") ;
          cat.setName(arrCategories[0].trim()) ;
          for(int i = 1; i < arrCategories.length; i ++) {
            Subcategory subCat = new Subcategory() ;
            subCat.setName(arrCategories[i].trim()) ;
            cat.setSubcategory(subCat) ;
          }
        }else{
          cat.setName(categories) ;          
        }
        infor.setCategory(cat) ;
      }      
      if(imageURL != null){
        try{
          URL url = new URL(imageURL) ;
          infor.setImage(url) ; 
        }catch(Exception e) {}
      }
      if(keywords != null) {
        String[] arrKeywords = keywords.split(" ");
        infor.setKeywords(arrKeywords) ;
      }

      infor.setOwnerEmailAddress(ownerEmail) ;
      infor.setOwnerName(ownerName) ;
      infor.setSummary(feedDescription) ;
      infor.setSubtitle(feedDescription) ;
      List<FeedInformation> modules = new ArrayList<FeedInformation>() ;
      modules.add(infor) ;
      feed.setModules(modules) ;
      feed.setCopyright(copyright) ;
      feed.setDescription(feedDescription) ;
      feed.setFeedType(rssVersion);
      feed.setLanguage(language) ;
      feed.setLink(feedLink) ;
      feed.setPublishedDate(pubDate) ;
      feed.setTitle(feedTitle) ;
      List<SyndEntry> entries = new ArrayList<SyndEntry>();
      SyndEntry entry;
      SyndContent description;
      ExoContainer container = ExoContainerContext.getCurrentContainer() ;
      PortalContainerInfo containerInfo = 
        (PortalContainerInfo)container.getComponentInstanceOfType(PortalContainerInfo.class) ;      
      String portalName = containerInfo.getContainerName() ;  
      MimeTypeResolver resolver = new MimeTypeResolver();
      NodeIterator iter = queryResult.getNodes() ;
      System.out.println("---  \nThere are "+ iter.getSize() + " items in Podcast.  ---\n") ;
      while (iter.hasNext()) {        
        Node child = iter.nextNode();        
        entry = new SyndEntryImpl();
        entry.setTitle(child.getProperty(TITLE).getString());
        List enclosureList = new ArrayList() ;
        SyndEnclosure enc = new SyndEnclosureImpl() ;
        Node content = child.getNode(JCR_CONTENT) ;
        String mimeType = content.getProperty(JCR_MIMETYPE).getString() ;
        String ext = resolver.getExtension(mimeType);
        enc.setType(mimeType) ;
        String path = child.getPath().trim() + "." + ext.trim() ; 
        enc.setLength(child.getProperty(LENGTH).getLong()) ;
        String encUrl = getEntryUrl(portalName, cmsConfigService_.getWorkspace(), path, rssUrl) ;
        enc.setUrl(encUrl) ;
        enclosureList.add(enc) ;
        entry.setEnclosures(enclosureList) ;
        entry.setLink(encUrl) ;
        EntryInformation entryInfo = new EntryInformationImpl() ;
        description = new SyndContentImpl();
        description.setType("text/plain");
        if(child.hasProperty(DESCRIPTION)){
          description.setValue(child.getProperty(DESCRIPTION).getString());
          entryInfo.setSubtitle(child.getProperty(DESCRIPTION).getString()) ;
          entryInfo.setSummary(child.getProperty(DESCRIPTION).getString()) ;
        }else{
          description.setValue("");
          entryInfo.setSubtitle("") ;
          entryInfo.setSummary("") ;
        }        
        entry.setDescription(description);
        try{
          Date pdate = child.getProperty(PUBLISHED_DATE).getDate().getTime() ;
          entry.setPublishedDate(pdate) ;
        }catch (Exception e) {
          entry.setPublishedDate(new Date()) ;
        }

        if(child.hasProperty(AUTHOR)) entryInfo.setAuthor(child.getProperty(AUTHOR).getString()) ;
        else entryInfo.setAuthor("") ;
        if(child.hasProperty(CATEGORY)) {
          Category itemCat = new Category() ;
          String itemCategories = child.getProperty(CATEGORY).getString() ;
          if(itemCategories != null && itemCategories.length() > 0) {
            if(itemCategories.indexOf(",") > -1) {
              String[] arrCategories = itemCategories.split(",") ;
              itemCat.setName(arrCategories[0].trim()) ;
              for(int i = 1; i < arrCategories.length; i ++) {
                Subcategory subCat = new Subcategory() ;
                subCat.setName(arrCategories[i].trim()) ;
                itemCat.setSubcategory(subCat) ;
              }
            }else{
              itemCat.setName(itemCategories) ;          
            }
            entryInfo.setCategory(itemCat) ;
          }
        }
        Duration dura = new Duration() ;
        dura.setMilliseconds(enc.getLength()) ;
        entryInfo.setDuration(dura) ;
        if(child.getProperty(EXPLICIT).getString().equals("no")) entryInfo.setExplicit(false) ;
        else entryInfo.setExplicit(true) ;
        if(child.hasProperty(KEYWORDS)) {
          String keys = child.getProperty(KEYWORDS).getString() ;
          if(keys != null) {
            String[] arrKeywords = keys.split(" ") ;
            entryInfo.setKeywords(arrKeywords) ;
          }
        }else {
          entryInfo.setKeywords(new String[] {}) ;
        }
        List<EntryInformation> entryList = new ArrayList<EntryInformation>() ;
        entryList.add(entryInfo) ;
        entry.setModules(entryList) ;
        entries.add(entry);        
      }      
      feed.setEntries(entries);      
      feed.setEncoding("UTF-8") ;      
      SyndFeedOutput output = new SyndFeedOutput();      
      String feedXML = output.outputString(feed);      
      storeXML(feedXML, storePath, feedName, repository);
      session.logout();
    }catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  private void storeXML(String feedXML, String rssStoredPath, String rssNodeName, String repository){   
    try {      
      Session session = 
        repositoryService_.getRepository(repository)
        .getSystemSession(cmsConfigService_.getWorkspace(repository));
      Node rootNode = session.getRootNode();
      String[] array = rssStoredPath.split("/") ;
      for(int i = 0; i < array.length; i ++) {
        if(array[i] != null && array[i].trim().length() > 0) {
          if(rootNode.hasNode(array[i].trim())) {
            rootNode = rootNode.getNode(array[i].trim()) ;
          }else {
            rootNode.addNode(array[i].trim(),NT_UNSTRUCTURED) ;
            rootNode.save() ;
            rootNode = rootNode.getNode(array[i].trim()) ;
          }
        }
      }
      session.save() ;
      String mimeType = "text/xml" ;
      Node rss = null;
      if(!rootNode.hasNode(rssNodeName)){
        rss = rootNode.addNode(rssNodeName, NT_FILE);
        Node contentNode = rss.addNode(JCR_CONTENT, NT_RESOURCE);
        contentNode.setProperty(JCR_DATA, new ByteArrayInputStream(feedXML.getBytes()));
        contentNode.setProperty(JCR_MIMETYPE, mimeType);
        contentNode.setProperty(JCR_LASTMODIFIED, new GregorianCalendar());
        session.save(); 
      } else {
        rss = rootNode.getNode(rssNodeName);
        boolean isEnabledVersion = false ;
        NodeType[] mixinTypes = rss.getMixinNodeTypes() ;
        for(int i = 0; i < mixinTypes.length; i ++) {
          if(mixinTypes[i].getName().equals(MIX_VERSIONABLE)) {
            isEnabledVersion = true ;
            break ;
          }
        }
        if(isEnabledVersion)  rss.checkout();           
        else  rss.addMixin(MIX_VERSIONABLE) ;                
        Node contentNode = rss.getNode(JCR_CONTENT);
        contentNode.setProperty(JCR_DATA, new ByteArrayInputStream(feedXML.getBytes()));
        contentNode.setProperty(JCR_MIMETYPE, mimeType);
        contentNode.setProperty(JCR_LASTMODIFIED, new GregorianCalendar());
        rss.save() ;
        rss.checkin() ;        
      }
      session.logout();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String getEntryUrl(String portalName, String wsName, String path, String rssUrl) 
  throws Exception{
    StringBuilder url ;
    if(rssUrl.indexOf(RSS) > -1 ) {
      String baseUrl = rssUrl.substring(0, rssUrl.indexOf(RSS) + 4) ;
      url = new StringBuilder(baseUrl) ;
    } else url = new StringBuilder("") ;
    url.append("/").append(portalName)
    .append("/").append(wsName)
    .append(path) ;           
    return url.toString();
  }
}
