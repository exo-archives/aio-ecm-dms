/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.folksonomy.impl;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.cms.folksonomy.FolksonomyService;
import org.exoplatform.services.cms.folksonomy.TagStyle;
import org.exoplatform.services.jcr.RepositoryService;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * Author : Xuan Hoa Pham
 *          hoapham@exoplatform.com
 * 					phamvuxuanhoa@gmail.com
 * Dec 5, 2006  
 */
public class FolksonomyServiceImpl implements FolksonomyService, Startable {

  final private static String EXO_FOLKSONOMIZED_MIXIN = "exo:folksonomized".intern() ;
  final private static String EXO_FOLKSONOMY_PROP = "exo:folksonomy".intern() ;
  final private static String MIX_REFERENCEABLE_MIXIN = "mix:referenceable".intern() ;
  final private static String EXO_TAG = "exo:tag".intern() ;
  final private static String TAG_CREATED_DATE_PROP = "exo:tagCreatedDate".intern() ;
  final private static String TAG_STATUS_PROP = "exo:tagStatus".intern() ;
  final private static String TAG_LAST_UPDATED_DATE_PROP = "exo:lastUpdatedDate".intern() ;
  
  final private static String EXO_TAG_STYLE = "exo:tagStyle".intern() ;
  final private static String TAG_RATE_PROP = "exo:styleRange".intern() ;
  final private static String HTML_STYLE_PROP = "exo:htmlStyle".intern() ;

  final private static String NORMAL_STYLE = "nomal".intern() ;
  final private static String INTERSTING_STYLE = "interesting".intern() ;
  final private static String ATTRACTIVE_STYLE = "attractive".intern() ;
  final private static String HOT_STYLE = "hot".intern() ;
  final private static String HOSTES_STYLE = "hotest".intern() ;    

  private RepositoryService repoService_ ;
  private CmsConfigurationService cmsConfigService_ ;   
  private String baseTagsPath_ ;  
  private String exoTagStylePath_ ;
  private List<TagStylePlugin> plugin_ = new ArrayList<TagStylePlugin>() ;
  private ExoCache cache_ ;  
  
  public FolksonomyServiceImpl(RepositoryService repoService,
      CmsConfigurationService cmsConfigService, CacheService cacheService ) throws Exception{
    repoService_ = repoService ;
    cmsConfigService_ = cmsConfigService ;    
    baseTagsPath_ = cmsConfigService_.getJcrPath(BasePath.EXO_TAGS_PATH) ;
    exoTagStylePath_ = cmsConfigService_.getJcrPath(BasePath.EXO_TAG_STYLE_PATH) ;
    cache_ = cacheService.getCacheInstance(FolksonomyServiceImpl.class.getName()) ;       
  }

  public void start() {
    try {
      init() ;
    }catch (Exception e) {
      System.out.println("===>>>>Exception when init FolksonomySerice"+e.getMessage());      
    }
  }

  public void stop() { }

  public void addTagStylePlugin(ComponentPlugin plugin) {      
    if(plugin instanceof TagStylePlugin) {
      plugin_.add((TagStylePlugin)plugin) ;
    }    
  }
  
  private void init() throws Exception {    
    for(TagStylePlugin plugin : plugin_) {
      try{
        plugin.init() ;
      }catch(Exception e) {
        //System.out.println("[WARNING]===>Can not init "+e.getMessage());
      }
    }
  }
  
  public void init(String repository) throws Exception {
    for(TagStylePlugin plugin : plugin_) {
      try{
        plugin.init(repository) ;
      }catch(Exception e) {
        //System.out.println("[WARNING]===>Can not init "+e.getMessage());
      }
    }
  }
  
  public void addTag(Node node, String[] tagNames, String repository) throws Exception {        
    Session systemSession = getSystemSession(repository) ;     
    Session currentSession = node.getSession() ;    
    Node exoTagsHomeNode_ = (Node)systemSession.getItem(baseTagsPath_) ;
    Node taggingNode = null ;
    for(String tagName: tagNames ) {      
      if(!exoTagsHomeNode_.hasNode(tagName)) {
        taggingNode = exoTagsHomeNode_.addNode(tagName,EXO_TAG) ;
        taggingNode.addMixin(MIX_REFERENCEABLE_MIXIN) ;
        taggingNode.setProperty(TAG_CREATED_DATE_PROP,new GregorianCalendar()) ;        
        taggingNode.setProperty(TAG_STATUS_PROP,TagStyle.NOMAL) ;        
      }else {
        taggingNode = exoTagsHomeNode_.getNode(tagName) ; 
      }            
      Value value2add = currentSession.getValueFactory().createValue(taggingNode);
      if(!node.isNodeType(EXO_FOLKSONOMIZED_MIXIN)) {
        node.addMixin(EXO_FOLKSONOMIZED_MIXIN) ;
        node.setProperty(EXO_FOLKSONOMY_PROP,new Value[] { value2add }) ;        
      }else {
        Value[] folksonomyValues = node.getProperty(EXO_FOLKSONOMY_PROP).getValues() ;
        String currenUUID = taggingNode.getUUID() ;
        List<Value> vals = new ArrayList<Value>();         
        for(Value value: folksonomyValues) {
          String uuid = value.getString() ;
          if(uuid.equals(currenUUID)) return ; 
          vals.add(value) ;
        }
        vals.add(value2add);
        node.setProperty(EXO_FOLKSONOMY_PROP,vals.toArray(new Value[vals.size()])) ;        
      }      
      exoTagsHomeNode_.save() ;
      node.save() ;
      cache_.remove(taggingNode.getPath()) ;
      cache_.remove(baseTagsPath_) ;
      cache_.remove(node.getPath()) ;
      updateTagStatus(taggingNode.getPath(), repository) ;      
    }        
    currentSession.save() ;    
    systemSession.save() ;    
    systemSession.logout();
  }
  
  public Node getTag(String path, String repository) throws Exception {    
    Session systemSession = getSystemSession(repository) ;
    return (Node)systemSession.getItem(path) ;
  }

  public List<Node> getDocumentsOnTag(String tagPath, String repository) throws Exception {
    if(cache_.get(tagPath)!=null) {
      return (List<Node>)cache_.get(tagPath) ;
    }
    List<Node> documentList = new ArrayList<Node>() ;        
    Session systemSession = getSystemSession(repository) ;
    Node tagNode = (Node)systemSession.getItem(tagPath) ;
    String uuid = tagNode.getUUID() ;    
    String[] workspaces = repoService_.getRepository(repository).getWorkspaceNames() ;
    Session  sessionOnWS = null ;
    for(String workspaceName: workspaces) {
      sessionOnWS = repoService_.getRepository(repository).getSystemSession(workspaceName) ;
      Node tagNodeOnWS = sessionOnWS.getNodeByUUID(uuid) ;
      PropertyIterator iter = tagNodeOnWS.getReferences() ;
      for(;iter.hasNext();) {
        Property folksonomy = iter.nextProperty() ;
        Node document = folksonomy.getParent() ;
        documentList.add(document) ;
      }
      sessionOnWS.logout();
    }
    systemSession.logout();
    cache_.put(tagPath,documentList) ;
    return documentList;
  } 

  public List<Node> getAllTags(String repository) throws Exception {
    Object cachedList = cache_.get(baseTagsPath_) ;
    if(cachedList != null ) {
      return (List<Node>)cachedList ;
    }    
    List<Node> tagList = new ArrayList<Node>() ;       
    Session systemSession = getSystemSession(repository) ;
    Node exoTagsHomeNode_ = (Node)systemSession.getItem(baseTagsPath_) ;
    for(NodeIterator iter = exoTagsHomeNode_.getNodes(); iter.hasNext();) {
      tagList.add(iter.nextNode()) ;
    }
    cache_.put(baseTagsPath_,tagList) ;
    systemSession.logout();
    return tagList ;
  }
  
  public List<Node> getAllTagStyle(String repository) throws Exception {
    Object cachedList = cache_.get(exoTagStylePath_) ;
    if(cachedList != null ) {
      return (List<Node>)cachedList ;
    }
    List<Node> tagStyleList = new ArrayList<Node>() ;   
    Session systemSession = getSystemSession(repository) ;
    Node tagStyleHomeNode = (Node)systemSession.getItem(exoTagStylePath_) ;
    for(NodeIterator iter = tagStyleHomeNode.getNodes(); iter.hasNext() ;) {      
      tagStyleList.add(iter.nextNode()) ;
    }
    systemSession.logout();
    cache_.put(exoTagStylePath_,tagStyleList) ;
    return tagStyleList ;
  }    
  
  protected Session getSystemSession(String repository) throws Exception {
    return repoService_.getRepository(repository).getSystemSession(cmsConfigService_.getWorkspace(repository)) ;    
  }  

  public String getTagStyle(String styleName, String repository) throws Exception {
    Object cachedObj = cache_.get(styleName) ;
    if(cachedObj != null) {
      return (String)cachedObj ;
    }    
    Session systemSession = getSystemSession(repository) ;    
    Node tagStyleHomeNode = (Node)systemSession.getItem(exoTagStylePath_) ;
    Node tagStyle = tagStyleHomeNode.getNode(styleName) ;
    String htmlStyle = tagStyle.getProperty(HTML_STYLE_PROP).getValue().getString() ;
    cache_.put(styleName,htmlStyle) ;
    systemSession.logout();
    return htmlStyle ;
  }

  public void updateStype(String tagPath, String tagRate, String htmlStyle, String repository) throws Exception {
    Session session = getSystemSession(repository) ;
    Node tagStyle = (Node)session.getItem(tagPath) ;
    tagStyle.setProperty(TAG_RATE_PROP,tagRate) ;
    tagStyle.setProperty(HTML_STYLE_PROP,htmlStyle) ;
    tagStyle.save() ;
    session.save() ;
    session.logout();
    cache_.remove(tagStyle.getName()) ;
    cache_.remove(baseTagsPath_) ;
    cache_.remove(exoTagStylePath_) ;
  }

  private void updateTagStatus(String tagPath, String repository) throws Exception {    
    int numberOfDocumentOnTag = getDocumentsOnTag(tagPath, repository).size() ;
    Session systemSession = getSystemSession(repository) ;
    Node selectedTagNode = (Node)systemSession.getItem(tagPath) ; 
    Node tagStyleHomeNode = (Node)systemSession.getItem(exoTagStylePath_) ;
    Node tagStyle = null ;
    for(NodeIterator iter = tagStyleHomeNode.getNodes(); iter.hasNext();) {
      Node node = iter.nextNode() ;
      if(checkTagRateOnTagStyle(numberOfDocumentOnTag,node)) {
        tagStyle = node ;
        break ;
      }
    }
    if(tagStyle!=null) {
      String styleName = tagStyle.getName() ;    
      if(NORMAL_STYLE.equals(styleName)) {
        selectedTagNode.setProperty(TAG_STATUS_PROP,TagStyle.NOMAL) ;
      }else if(INTERSTING_STYLE.equals(styleName)) {
        selectedTagNode.setProperty(TAG_STATUS_PROP,TagStyle.INTERESTING) ;
      }else if(ATTRACTIVE_STYLE.equals(styleName)) {
        selectedTagNode.setProperty(TAG_STATUS_PROP,TagStyle.ATTRACTIVE) ;
      }else if(HOT_STYLE.equals(styleName)) {
        selectedTagNode.setProperty(TAG_STATUS_PROP,TagStyle.HOT) ;
      }else if(HOSTES_STYLE.equals(styleName)) {
        selectedTagNode.setProperty(TAG_STATUS_PROP,TagStyle.HOTEST) ;
      }
      selectedTagNode.setProperty(TAG_LAST_UPDATED_DATE_PROP,new GregorianCalendar()) ;
      selectedTagNode.save() ;      
      systemSession.save();
      systemSession.refresh(true) ;      
    }    
    systemSession.logout();    
  }

  private boolean checkTagRateOnTagStyle(int numOfDocument, Node tagStyle) throws Exception {
    String tagRate = tagStyle.getProperty(TAG_RATE_PROP).getValue().getString() ;
    String[] vals = StringUtils.split(tagRate,"..") ;    
    int minValue = Integer.parseInt(vals[0]) ;
    int maxValue ;
    if(vals[1].equals("*")) {
      maxValue = Integer.MAX_VALUE ;
    }else {
      maxValue = Integer.parseInt(vals[1]) ;
    }
    if(minValue <=numOfDocument && numOfDocument <maxValue ) return true ;    
    return false ;
  }
  
  public List<Node> getLinkedTagsOfDocument(Node document, String repository) throws Exception {
    if(document == null || !document.hasProperty(EXO_FOLKSONOMY_PROP)) 
      return new ArrayList<Node>() ;
    
    if(cache_.get(document.getPath())!=null ) {
      return (List<Node>) cache_.get(document.getPath()) ;
    }
    List<Node> tagList = new ArrayList<Node>() ;    
    Session systemSession = getSystemSession(repository) ;
    try{      
      Value[] values = document.getProperty(EXO_FOLKSONOMY_PROP).getValues() ;
      for(Value v:values) {
        String uuid = v.getString() ;
        tagList.add(systemSession.getNodeByUUID(uuid)) ;
      }
    }catch (Exception e) {      
    }
    systemSession.logout();
    cache_.put(document.getPath(),tagList) ;
    return tagList;
  }   
}
