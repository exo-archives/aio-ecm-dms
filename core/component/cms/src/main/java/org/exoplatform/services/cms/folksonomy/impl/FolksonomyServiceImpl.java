/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
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
import org.exoplatform.services.cms.folksonomy.FolksonomyService;
import org.exoplatform.services.cms.folksonomy.TagStyle;
import org.exoplatform.services.cms.impl.DMSConfiguration;
import org.exoplatform.services.cms.impl.DMSRepositoryConfiguration;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * Author : Xuan Hoa Pham
 *          hoapham@exoplatform.com
 * 					phamvuxuanhoa@gmail.com
 * Dec 5, 2006  
 */
public class FolksonomyServiceImpl implements FolksonomyService, Startable {
  
  /**
   * NodeType EXO_FOLKSONOMIZED_MIXIN
   */
  final private static String EXO_FOLKSONOMIZED_MIXIN = "exo:folksonomized".intern() ;
  
  /**
   * Property name EXO_FOLKSONOMY_PROP
   */
  final private static String EXO_FOLKSONOMY_PROP = "exo:folksonomy".intern() ;
  
  /**
   * Mixin Type MIX_REFERENCEABLE_MIXIN
   */
  final private static String MIX_REFERENCEABLE_MIXIN = "mix:referenceable".intern() ;
  
  /**
   * Node type name EXO_TAG
   */
  final private static String EXO_TAG = "exo:tag".intern() ;
  
  /**
   * Property name TAG_CREATED_DATE_PROP
   */
  final private static String TAG_CREATED_DATE_PROP = "exo:tagCreatedDate".intern() ;
  
  /**
   * Property name TAG_STATUS_PROP
   */
  final private static String TAG_STATUS_PROP = "exo:tagStatus".intern() ;
  
  /**
   * Property name TAG_LAST_UPDATED_DATE_PROP
   */
  final private static String TAG_LAST_UPDATED_DATE_PROP = "exo:lastUpdatedDate".intern() ;
  
//  final private static String EXO_TAG_STYLE = "exo:tagStyle".intern() ;
  
  /**
   * Property name TAG_RATE_PROP
   */
  final private static String TAG_RATE_PROP = "exo:styleRange".intern() ;
  
  /**
   * Property name HTML_STYLE_PROP
   */
  final private static String HTML_STYLE_PROP = "exo:htmlStyle".intern() ;

  /**
   * Name of node NORMAL_STYLE
   */
  final private static String NORMAL_STYLE = "nomal".intern() ;
  
  /**
   * Name of node INTERSTING_STYLE
   */
  final private static String INTERSTING_STYLE = "interesting".intern() ;
  
  /**
   * Name of nodes ATTRACTIVE_STYLE
   */
  final private static String ATTRACTIVE_STYLE = "attractive".intern() ;
  
  /**
   * Name of nodes HOT_STYLE
   */
  final private static String HOT_STYLE = "hot".intern() ;
  
  /**
   * Name of nodes HOSTES_STYLE
   */
  final private static String HOSTES_STYLE = "hotest".intern() ;    

  /**
   * RepositoryService object
   */
  private RepositoryService repoService_ ;
  
  /**
   * NodeHierarchyCreator object
   */
  private NodeHierarchyCreator nodeHierarchyCreator_ ;   
  
  /**
   * Base path to tag 
   */
  private String baseTagsPath_ ;  
  
  /**
   * 
   */
  private String exoTagStylePath_ ;
  
  /**
   * List of TagStylePlugin
   */
  private List<TagStylePlugin> plugin_ = new ArrayList<TagStylePlugin>() ;
  
  /**
   * ExoCache object
   */
  private ExoCache cache_ ;  
  
  /**
   * DMS configuration which used to store informations
   */   
  private DMSConfiguration dmsConfiguration_;  
  
  /**
   * Constructor method
   * Construct repoService_, nodeHierarchyCreator_, baseTagsPath_, exoTagStylePath_, cache_
   * @param repoService             RepositoryService object
   * @param nodeHierarchyCreator    NodeHierarchyCreator object
   * @param cacheService            CacheService object     
   * @throws Exception
   */
  public FolksonomyServiceImpl(RepositoryService repoService,
      NodeHierarchyCreator nodeHierarchyCreator, CacheService cacheService, 
      DMSConfiguration dmsConfiguration) throws Exception{
    repoService_ = repoService ;
    nodeHierarchyCreator_ = nodeHierarchyCreator ;    
    baseTagsPath_ = nodeHierarchyCreator_.getJcrPath(BasePath.EXO_TAGS_PATH) ;
    exoTagStylePath_ = nodeHierarchyCreator_.getJcrPath(BasePath.EXO_TAG_STYLE_PATH) ;
    cache_ = cacheService.getCacheInstance(FolksonomyServiceImpl.class.getName()) ;
    dmsConfiguration_ = dmsConfiguration;
  }

  /**
   * Implement method in Startable
   * Call init() method
   * @see {@link #init()}
   */
  public void start() {
    try {
      init() ;
    }catch (Exception e) {
      System.out.println("===>>>>Exception when init FolksonomySerice"+e.getMessage());      
    }
  }

  /**
   * Implement method in Startable
   */
  public void stop() { }

  /**
   * Add new TagStylePlugin in plugin_
   * @param plugin
   */
  public void addTagStylePlugin(ComponentPlugin plugin) {      
    if(plugin instanceof TagStylePlugin) {
      plugin_.add((TagStylePlugin)plugin) ;
    }    
  }
  
  /**
   * init all avaiable TagStylePlugin
   * @throws Exception
   */
  private void init() throws Exception {    
    for(TagStylePlugin plugin : plugin_) {
      try{
        plugin.init() ;
      }catch(Exception e) {
        //System.out.println("[WARNING]===>Can not init "+e.getMessage());
      }
    }
  }
  
  /**
   * Init all TagStylePlugin with session in repository name
   * @param repository     repository name
   */
  public void init(String repository) throws Exception {
    for(TagStylePlugin plugin : plugin_) {
      try{
        plugin.init(repository) ;
      }catch(Exception e) {
        //System.out.println("[WARNING]===>Can not init "+e.getMessage());
      }
    }
  }
  
  /**
   * Add new child node in node baseTagsPath_
   * If there are not node with name in tagNames then create new one
   * Add mixin type MIX_REFERENCEABLE_MIXIN for new node
   * Add new property TAG_CREATED_DATE_PROP, TAG_STATUS_PROP
   * Add new mixin type EXO_FOLKSONOMIZED_MIXIN to current node if not exist
   * Set property EXO_FOLKSONOMY_PROP for current node
   * @param node        current node
   * @param tagNames    Array of node name as child node of exoTagsHomeNode_
   * @param repository
   * @throws Exception
   */
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
//      cache_.remove(taggingNode.getPath()) ;
//      cache_.remove(baseTagsPath_) ;
//      cache_.remove(node.getPath()) ;
      updateTagStatus(taggingNode.getPath(), repository) ;      
    }        
    currentSession.save() ;    
    systemSession.save() ;    
    systemSession.logout();
  }
  
  /**
   * Get node following path in repository
   * @param path          path to node
   * @param repository    repository name
   * @return  node following path
   * @throws Exception
   */
  public Node getTag(String path, String repository) throws Exception {    
    Session systemSession = getSystemSession(repository) ;
    return (Node)systemSession.getItem(path) ;
  }

  /**
   * Get document list      from repository
   * @param tagPath         path to node in all workspace
   * @param repository      repository name
   * @return ArrayList of node
   * @throws Exception
   */
  public List<Node> getDocumentsOnTag(String tagPath, String repository) throws Exception {
//    if(cache_.get(tagPath)!=null) {
//      return (List<Node>)cache_.get(tagPath) ;
//    }
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
//    cache_.put(tagPath,documentList) ;
    return documentList;
  } 

  /**
   * Get all node base on path = baseTagsPath_ in repository
   * @param repository      repository name
   * @return ArrayList of Node
   */
  public List<Node> getAllTags(String repository) throws Exception {
//    Object cachedList = cache_.get(baseTagsPath_) ;
//    if(cachedList != null ) {
//      return (List<Node>)cachedList ;
//    }    
    List<Node> tagList = new ArrayList<Node>() ;       
    Session systemSession = getSystemSession(repository) ;
    Node exoTagsHomeNode_ = (Node)systemSession.getItem(baseTagsPath_) ;
    for(NodeIterator iter = exoTagsHomeNode_.getNodes(); iter.hasNext();) {
      tagList.add(iter.nextNode()) ;
    }
//    cache_.put(baseTagsPath_,tagList) ;
    systemSession.logout();
    return tagList ;
  }
  
  /**
   *  Get all node base on path = exoTagStylePath_ in repository
   * @param repository
   * @return ArrayList of Node
   * @throws Exception
   */
  public List<Node> getAllTagStyle(String repository) throws Exception {
//    Object cachedList = cache_.get(exoTagStylePath_) ;
//    if(cachedList != null ) {
//      return (List<Node>)cachedList ;
//    }
    List<Node> tagStyleList = new ArrayList<Node>() ;   
    Session systemSession = getSystemSession(repository) ;
    Node tagStyleHomeNode = (Node)systemSession.getItem(exoTagStylePath_) ;
    for(NodeIterator iter = tagStyleHomeNode.getNodes(); iter.hasNext() ;) {      
      tagStyleList.add(iter.nextNode()) ;
    }
    systemSession.logout();
//    cache_.put(exoTagStylePath_,tagStyleList) ;
    return tagStyleList ;
  }    
  
  /**
   * Get session from repository and system workspace
   * @param repository    repository name
   * @return session
   * @throws Exception
   */
  protected Session getSystemSession(String repository) throws Exception {
    ManageableRepository manageableRepository = repoService_.getRepository(repository) ;
    DMSRepositoryConfiguration dmsRepoConfig = dmsConfiguration_.getConfig(repository);
    return manageableRepository.getSystemSession(dmsRepoConfig.getSystemWorkspace()) ;    
  }  

  /**
   * Get HTML_STYLE_PROP property in styleName node in repository
   * @param styleName       name of node
   * @param repository      repository name
   * @return  value of HTML_STYLE_PROP property of styleName node
   * @throws Exception
   */
  public String getTagStyle(String styleName, String repository) throws Exception {
//    Object cachedObj = cache_.get(styleName) ;
//    if(cachedObj != null) {
//      return (String)cachedObj ;
//    }    
    Session systemSession = getSystemSession(repository) ;    
    Node tagStyleHomeNode = (Node)systemSession.getItem(exoTagStylePath_) ;
    Node tagStyle = tagStyleHomeNode.getNode(styleName) ;
    String htmlStyle = tagStyle.getProperty(HTML_STYLE_PROP).getValue().getString() ;
//    cache_.put(styleName,htmlStyle) ;
    systemSession.logout();
    return htmlStyle ;
  }

  /**
   * Update property TAG_RATE_PROP, HTML_STYLE_PROP following value tagRate, htmlStyle
   * for node in tagPath in repository
   * @param tagPath     path to node
   * @param tagRate
   * @param htmlStyle
   * @param repository
   * @throws Exception
   */
  public void updateStype(String tagPath, String tagRate, String htmlStyle, String repository) throws Exception {
    Session session = getSystemSession(repository) ;
    Node tagStyle = (Node)session.getItem(tagPath) ;
    tagStyle.setProperty(TAG_RATE_PROP,tagRate) ;
    tagStyle.setProperty(HTML_STYLE_PROP,htmlStyle) ;
    tagStyle.save() ;
    session.save() ;
    session.logout();
//    cache_.remove(tagStyle.getName()) ;
//    cache_.remove(baseTagsPath_) ;
//    cache_.remove(exoTagStylePath_) ;
  }

  /**
   * Update TAG_STATUS_PROP property for node in path = tagPath
   * @param tagPath         path to node
   * @param repository      repository name
   * @throws Exception
   */
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

  /**
   * Check numOfDocument is in range of value of TAG_RATE_PROP property in tagStyle node 
   * @param numOfDocument number of document      
   * @param tagStyle      tagStyle Node
   * @return  true if numOfDocument is in range
   *          false if not
   * @throws Exception
   */
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
  
  /**
   * Base on uuid in values in EXO_FOLKSONOMY_PROP property in document node,
   * get all node linked to this document node
   * @param document          document node
   * @param repository        repository name
   * @return                  ArrayList of Node
   * @throws Exception
   */
  public List<Node> getLinkedTagsOfDocument(Node document, String repository) throws Exception {
    if(document == null || !document.hasProperty(EXO_FOLKSONOMY_PROP)) 
      return new ArrayList<Node>() ;
    
//    if(cache_.get(document.getPath())!=null ) {
//      return (List<Node>) cache_.get(document.getPath()) ;
//    }
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
//    cache_.put(document.getPath(),tagList) ;
    return tagList;
  }   
}
