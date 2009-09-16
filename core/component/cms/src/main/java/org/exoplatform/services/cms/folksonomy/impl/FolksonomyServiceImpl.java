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

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.folksonomy.FolksonomyService;
import org.exoplatform.services.cms.folksonomy.TagStyle;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
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
   * Logger.
   */
  private static final Log LOG  = ExoLogger.getLogger("cms.FolksonomyServiceImpl");
  
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
   * Owner of Node
   */
  final public static String EXO_OWNER = "exo:owner";
  
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
   * Constructor method
   * Construct repoService_, nodeHierarchyCreator_, baseTagsPath_, exoTagStylePath_, cache_
   * @param repoService             RepositoryService object
   * @param nodeHierarchyCreator    NodeHierarchyCreator object
   * @param cacheService            CacheService object     
   * @throws Exception
   */
  public FolksonomyServiceImpl(RepositoryService repoService,
      NodeHierarchyCreator nodeHierarchyCreator, CacheService cacheService) throws Exception{
    repoService_ = repoService ;
    nodeHierarchyCreator_ = nodeHierarchyCreator ;    
    baseTagsPath_ = nodeHierarchyCreator_.getJcrPath(BasePath.EXO_TAGS_PATH) ;
    exoTagStylePath_ = nodeHierarchyCreator_.getJcrPath(BasePath.EXO_TAG_STYLE_PATH) ;
    cache_ = cacheService.getCacheInstance(FolksonomyServiceImpl.class.getName()) ;
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
      LOG.error("===>>>>Exception when init FolksonomySerice", e);      
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
   * {@inheritDoc}
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
   * {@inheritDoc}
   */
  public void addTag(Node node, String[] tagNames, String repository) throws Exception {        
    Session systemSession = getSystemSession(repository);     
    Session currentSession = node.getSession();    
    Node exoTagsHomeNode_ = (Node)systemSession.getItem(baseTagsPath_);
    String userId = currentSession.getUserID();
    if (!exoTagsHomeNode_.hasNode(userId)) {
      exoTagsHomeNode_.addNode(userId);
    }
    Node exoTagsUserNode = exoTagsHomeNode_.getNode(userId);
    Node taggingNode = null;
    for(String tagName: tagNames ) {      
      if(!exoTagsUserNode.hasNode(tagName)) {
        taggingNode = exoTagsUserNode.addNode(tagName,EXO_TAG) ;
        taggingNode.addMixin(MIX_REFERENCEABLE_MIXIN) ;
        taggingNode.setProperty(TAG_CREATED_DATE_PROP,new GregorianCalendar()) ;        
        taggingNode.setProperty(TAG_STATUS_PROP,TagStyle.NOMAL) ;        
      }else {
        taggingNode = exoTagsUserNode.getNode(tagName) ; 
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
   * {@inheritDoc}
   */
  public Node getTag(String path, String repository) throws Exception {    
    Session systemSession = getSystemSession(repository) ;
    return (Node)systemSession.getItem(path) ;
  }

  /**
   * {@inheritDoc}
   */
  public List<Node> getDocumentsOnTag(String tagPath, String repository) throws Exception {
    Session systemSession = null;
    try {
      List<Node> documentList = new ArrayList<Node>() ;        
      systemSession = getSystemSession(repository) ;
      Node tagNode = (Node)systemSession.getItem(tagPath) ;
      String uuid = tagNode.getUUID() ;    
      String[] workspaces = repoService_.getRepository(repository).getWorkspaceNames() ;
      Session  sessionOnWS = null ;
      for(String workspaceName: workspaces) {
        sessionOnWS = repoService_.getRepository(repository).getSystemSession(workspaceName) ;
        Node tagNodeOnWS = null;
        try {
          tagNodeOnWS = sessionOnWS.getNodeByUUID(uuid);
        } catch (ItemNotFoundException e) {
          LOG.warn("Item not found by uuid = " + uuid);
        }
        if (tagNodeOnWS != null) {
          PropertyIterator iter = tagNodeOnWS.getReferences();
          for (; iter.hasNext();) {
            Property folksonomy = iter.nextProperty();
            Node document = folksonomy.getParent();
            documentList.add(document);
          }
        }
        sessionOnWS.logout();
      }
      return documentList;
    } finally {
      if (systemSession != null) systemSession.logout();
    }    
  } 

  private String getNodeOwner(Node node) throws Exception {
    if (node.hasProperty(EXO_OWNER)) {
      return node.getProperty(EXO_OWNER).getString();
    }
    return null;
  }
  
  private String getUserTagPath(String repository) throws Exception {
    StringBuffer bf = new StringBuffer();
    String userid = getSessionByUser(repository).getUserID();
    return bf.append(baseTagsPath_).append("/").append(userid).toString();
  }
  
  private List<Node> getTagNodes(Session session, String repository) throws Exception {
    List<Node> tagList = new ArrayList<Node>();
    String userPath = getUserTagPath(repository);
    if (!session.itemExists(userPath)) return tagList;
    Node exoTagsHomeNode_ = (Node) session.getItem(getUserTagPath(repository));
    for (NodeIterator iter = exoTagsHomeNode_.getNodes(); iter.hasNext();) {
      tagList.add(iter.nextNode());
    }
    session.logout();
    return tagList;
  }
  /**
   * {@inheritDoc}
   */
  public List<Node> getAllTags(String repository) throws Exception {
    return getTagNodes(getSystemSession(repository), repository);
  }
  
  /**
   *  {@inheritDoc}
   */
  public List<Node> getAllTagStyle(String repository) throws Exception {
    Session systemSession = null;
    try {
      List<Node> tagStyleList = new ArrayList<Node>() ;   
      systemSession = getSystemSession(repository) ;
      Node tagStyleHomeNode = (Node)systemSession.getItem(exoTagStylePath_) ;
      for(NodeIterator iter = tagStyleHomeNode.getNodes(); iter.hasNext() ;) {      
        tagStyleList.add(iter.nextNode()) ;
      }
      return tagStyleList ;
    } finally {
      if (systemSession != null) systemSession.logout();
    }
  }    
  
  private Session getSessionByUser(String repository) throws Exception {
    ManageableRepository manageableRepository = repoService_.getRepository(repository) ;
    String systemWorkspacere = manageableRepository.getConfiguration().getSystemWorkspaceName();
    return SessionProviderFactory.createSessionProvider().getSession(systemWorkspacere, manageableRepository);
  }
  /**
   * Get session from repository and system workspace
   * @param repository    repository name
   * @return session
   * @throws Exception
   */
  protected Session getSystemSession(String repository) throws Exception {
    ManageableRepository manageableRepository = repoService_.getRepository(repository) ;
    return manageableRepository.getSystemSession(manageableRepository.getConfiguration().getSystemWorkspaceName()) ;    
  }  

  /**
   * {@inheritDoc}
   */
  public String getTagStyle(String styleName, String repository) throws Exception {
    Session systemSession = null;
    try {
      systemSession = getSystemSession(repository) ;    
      Node tagStyleHomeNode = (Node)systemSession.getItem(exoTagStylePath_) ;
      Node tagStyle = tagStyleHomeNode.getNode(styleName) ;
      String htmlStyle = tagStyle.getProperty(HTML_STYLE_PROP).getValue().getString() ;
      systemSession.logout();
      return htmlStyle ;
    } finally {
      if (systemSession != null) systemSession.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void updateStype(String tagPath, String tagRate, String htmlStyle, String repository) throws Exception {
    Session systemSession = null;
    try {
      systemSession = getSystemSession(repository) ;
      Node tagStyle = (Node)systemSession.getItem(tagPath) ;
      tagStyle.setProperty(TAG_RATE_PROP,tagRate) ;
      tagStyle.setProperty(HTML_STYLE_PROP,htmlStyle) ;
      tagStyle.save() ;      
    } finally {
      if (systemSession != null) {
        systemSession.save() ;
        systemSession.logout();
      }
    }
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
   * {@inheritDoc}
   */
  public boolean removeTagOfDocument(Node document, String tagName, String repository) throws Exception {
    Value[] folksonomyValues = document.getProperty(EXO_FOLKSONOMY_PROP).getValues() ;
    Node taggingNode = getTag(getUserTagPath(repository) + "/" + tagName, repository);
    String currenUUID = taggingNode.getUUID() ;
    List<Value> vals = new ArrayList<Value>();         
    for(Value value: folksonomyValues) {
      String uuid = value.getString() ;
      if(!uuid.equals(currenUUID)) vals.add(value);
    }
    if (vals.size() > 0) {
      document.setProperty(EXO_FOLKSONOMY_PROP,vals.toArray(new Value[vals.size()])) ; 
    } else {
      document.removeMixin(EXO_FOLKSONOMIZED_MIXIN);
    }
    document.save();
    document.getSession().save();
    //refresh node
    getSystemSession(repository).refresh(true);
    String uuid = taggingNode.getUUID();
    String[] workspaces = repoService_.getRepository(repository).getWorkspaceNames() ;
    Session  sessionOnWS = null ;
    boolean removetag = true;
    for(String workspaceName: workspaces) {
      sessionOnWS = repoService_.getRepository(repository).getSystemSession(workspaceName) ;
      Node tagNodeOnWS = null;
      try {
        tagNodeOnWS = sessionOnWS.getNodeByUUID(uuid);
      } catch (ItemNotFoundException e) {
        LOG.warn("Item not found by uuid = " + uuid);
      }
      if (tagNodeOnWS != null) {
        PropertyIterator iter = tagNodeOnWS.getReferences();
        if (iter.hasNext()) {
          removetag = false;
        }
      }
      sessionOnWS.logout();
    }
    
    if (removetag) {
      Node parent = taggingNode.getParent();
      taggingNode.remove();
      parent.save();
      parent.getSession().save();
    } else {
      updateTagStatus(taggingNode.getPath(), repository);
    }
    return true;
  }
  
  /**
   * {@inheritDoc}
   */
  public List<Node> getLinkedTagsOfDocument(Node document, String repository) throws Exception {
    Session systemSession = null;
    try {
      if(document == null || !document.hasProperty(EXO_FOLKSONOMY_PROP)) 
        return new ArrayList<Node>() ;
      List<Node> tagList = new ArrayList<Node>() ;    
      systemSession = getSystemSession(repository) ;
      try{      
        Value[] values = document.getProperty(EXO_FOLKSONOMY_PROP).getValues() ;
        Node node;
        for(Value v:values) {
          String uuid = v.getString() ;
          node = systemSession.getNodeByUUID(uuid);
          if (node.getParent().getName().equals(getSessionByUser(repository).getUserID())) {
            tagList.add(node) ;
          }
        }
      }catch (Exception e) {      
      }
      return tagList;
    } finally {
      if (systemSession != null) {
        systemSession.save() ;
        systemSession.logout();
      }
    }    
  }   
}
