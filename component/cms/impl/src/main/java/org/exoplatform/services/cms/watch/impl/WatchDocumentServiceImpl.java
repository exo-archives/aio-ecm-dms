/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.watch.impl;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.cms.watch.WatchDocumentService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.picocontainer.Startable;


/**
 * Created by The eXo Platform SARL
 * Author : Pham Xuan Hoa
 *          hoapham@exoplatform.com
 * Nov 30, 2006  
 */
public class WatchDocumentServiceImpl implements WatchDocumentService, Startable {      

  final public static String EXO_WATCHABLE_MIXIN = "exo:watchable".intern() ;  
  final public static String EMAIL_WATCHERS_PROP = "exo:emailWatcher".intern() ;
  final public static String RSS_WATCHERS_PROP = "exo:rssWatcher".intern() ;
  final private String initParamName = "messageConfig".intern();
  final private static String WATCHABLE_MIXIN_QUERY = "//element(*,exo:watchable)" ;
  
  private RepositoryService repoService_ ;
  private MessageConfig messageConfig_ ;
  private TemplateService templateService_ ;

  public WatchDocumentServiceImpl(InitParams params, 
                 RepositoryService repoService, TemplateService templateService) {        
    repoService_ = repoService ;
    templateService_ = templateService ;
    messageConfig_ = 
      (MessageConfig)params.getObjectParam(initParamName).getObject() ;    
  }

  public int getNotificationType(Node documentNode, String userName) throws Exception {
    NodeType[] mixinTypes = documentNode.getMixinNodeTypes() ;
    NodeType watchableMixin = null ;
    if(mixinTypes.length>0) {
      for(NodeType nodeType: mixinTypes) {
        if(nodeType.getName().equalsIgnoreCase(EXO_WATCHABLE_MIXIN)) {
          watchableMixin = nodeType ;
          break ;
        }
      }      
    }
    if(watchableMixin == null)  return -1 ;
    boolean notifyByEmail = checkNotifyTypeOfWatcher(documentNode,userName,EMAIL_WATCHERS_PROP) ;
    boolean notifyByRss = checkNotifyTypeOfWatcher(documentNode,userName,RSS_WATCHERS_PROP) ;
    if( notifyByEmail && notifyByRss) return FULL_NOTIFICATION ;
    if(notifyByEmail) return NOTIFICATION_BY_EMAIL ;
    if(notifyByRss) return NOTIFICATION_BY_RSS ;
    return -1 ;
  }

  public void watchDocument(Node documentNode, String userName, int notifyType) throws Exception {
    Session session = documentNode.getSession() ;
    Value newWatcher = session.getValueFactory().createValue(userName) ;
    if(!documentNode.isNodeType(EXO_WATCHABLE_MIXIN)) {
      documentNode.addMixin(EXO_WATCHABLE_MIXIN) ;
      if(notifyType == NOTIFICATION_BY_EMAIL) {
        documentNode.setProperty(EMAIL_WATCHERS_PROP,new Value[] {newWatcher}) ;
        documentNode.save() ;
        session.save() ;                
        EmailNotifyListener listener = new EmailNotifyListener(documentNode) ;                
        observeNode(documentNode,listener) ;        
      }        
      if(notifyType == NOTIFICATION_BY_RSS ) {        
        documentNode.setProperty(RSS_WATCHERS_PROP,new Value[] {newWatcher}) ;
        documentNode.save() ;        
        RssNotifyListener listener = new RssNotifyListener(documentNode) ;
        observeNode(documentNode,listener) ;        
        //TODO Send an RSS feed to user when document is modify        
      }
      session.save() ;
    }else {      
      List<Value>  watcherList = new ArrayList<Value>() ;
      if(notifyType == NOTIFICATION_BY_EMAIL) {
        if(documentNode.hasProperty(RSS_WATCHERS_PROP)) {
          for(Value watcher : documentNode.getProperty(EMAIL_WATCHERS_PROP).getValues()) {
            watcherList.add(watcher) ;
          }
          watcherList.add(newWatcher) ;
        }
        documentNode.setProperty(EMAIL_WATCHERS_PROP,watcherList.toArray(new Value[watcherList.size()])) ;
        documentNode.save() ;
      }
      if(notifyType == NOTIFICATION_BY_RSS) {
        if(documentNode.hasProperty(RSS_WATCHERS_PROP)) {
          for(Value watcher : documentNode.getProperty(RSS_WATCHERS_PROP).getValues()) {
            watcherList.add(watcher) ;
          }
          watcherList.add(newWatcher) ;
        }
        documentNode.setProperty(RSS_WATCHERS_PROP,watcherList.toArray(new Value[watcherList.size()])) ;
        documentNode.save() ;
      }
      session.save() ;
    }
  }

  public void unwatchDocument(Node documentNode, String userName, int notificationType) throws Exception {
    if(!documentNode.isNodeType(EXO_WATCHABLE_MIXIN)) return  ;
    Session session = documentNode.getSession() ;
    if(notificationType == NOTIFICATION_BY_EMAIL) {
      Value[] watchers = documentNode.getProperty(EMAIL_WATCHERS_PROP).getValues() ;
      List<Value> watcherList = new ArrayList<Value>() ;
      for(Value watcher: watchers) {
        if(!watcher.getString().equals(userName)) {
          watcherList.add(watcher) ;
        }
      }
      documentNode.setProperty(EMAIL_WATCHERS_PROP,watcherList.toArray(new Value[watcherList.size()])) ;
    }

    if(notificationType == NOTIFICATION_BY_RSS) {
      Value[] watchers = documentNode.getProperty(RSS_WATCHERS_PROP).getValues() ;
      List<Value> watcherList = new ArrayList<Value>() ;
      for(Value watcher: watchers) {
        if(!watcher.getString().equals(userName)) {
          watcherList.add(watcher) ;
        }
      }
      documentNode.setProperty(RSS_WATCHERS_PROP,watcherList.toArray(new Value[watcherList.size()])) ;
    }
    documentNode.removeMixin(EXO_WATCHABLE_MIXIN) ;
    documentNode.save() ;  
    session.save() ;
  }  

  private void observeNode(Node node,EventListener listener) throws Exception {
    String workspace = node.getSession().getWorkspace().getName() ;
    Session systemSession = repoService_.getRepository().getSystemSession(workspace) ;
    List<String> list = getDocumentNodeTypes(node) ;          
    String[] observedNodeTypeNames = list.toArray(new String[list.size()]) ;
    ObservationManager observationManager = systemSession.getWorkspace().getObservationManager() ;
    observationManager.addEventListener(listener,Event.PROPERTY_CHANGED,
        node.getPath(),true,null,observedNodeTypeNames,false) ;    
  }

  private boolean checkNotifyTypeOfWatcher(Node documentNode, String userName,String notificationType) throws Exception {
    if(documentNode.hasProperty(notificationType)) {
      Value [] watchers = documentNode.getProperty(notificationType).getValues() ;
      for(Value value: watchers) {
        if(userName.equalsIgnoreCase(value.getString())) return true ;
      }
    }
    return false ;
  }  
  
  private List<String> getDocumentNodeTypes(Node node) throws Exception {
    List<String> nodeTypeNameList = new ArrayList<String>() ;
    NodeType  primaryType = node.getPrimaryNodeType() ;
    if(templateService_.isManagedNodeType(primaryType.getName())) {
      nodeTypeNameList.add(primaryType.getName()) ;
    }    
    for(NodeType nodeType: node.getMixinNodeTypes()) {
      if(templateService_.isManagedNodeType(nodeType.getName())) 
        nodeTypeNameList.add(nodeType.getName()) ;
    }
    return nodeTypeNameList ;
  }
  private void reInitObserver() throws Exception {
    ManageableRepository repository = repoService_.getRepository() ;
    String[] workspaceNames = repository.getWorkspaceNames() ;
    for(String workspace: workspaceNames) {
      Session session = repository.getSystemSession(workspace) ;
      QueryManager queryManager = null ;
      try{
        queryManager = session.getWorkspace().getQueryManager() ;
      }catch (Exception e) { }
      if(queryManager == null) continue ;
      try {        
        Query query = queryManager.createQuery(WATCHABLE_MIXIN_QUERY,Query.XPATH) ;
        QueryResult queryResult = query.execute() ;
        for(NodeIterator iter = queryResult.getNodes(); iter.hasNext(); ) {          
          Node observedNode = iter.nextNode() ;
          EmailNotifyListener emailNotifyListener = new EmailNotifyListener(observedNode) ;
          ObservationManager manager = session.getWorkspace().getObservationManager() ;
          List<String> list = getDocumentNodeTypes(observedNode) ;          
          String[] observedNodeTypeNames = list.toArray(new String[list.size()]) ;          
          manager.addEventListener(emailNotifyListener,Event.PROPERTY_CHANGED,
             observedNode.getPath(),true,null,observedNodeTypeNames,false) ;          
          // TODO Add Listener for notify type by RSS
          RssNotifyListener rssNotifyListener = new RssNotifyListener(observedNode) ;
          manager.addEventListener(rssNotifyListener,Event.PROPERTY_CHANGED,
              observedNode.getPath(),true,null,observedNodeTypeNames,false) ;
        }
      }catch (Exception e) {
        System.out.println("==>>> Cannot init observer for node: " +e.getLocalizedMessage());
        //e.printStackTrace() ;
      }
    }
  }    
  
  protected MessageConfig getMessageConfig() { return messageConfig_ ; }
  
  public void start() {
    try {
      reInitObserver() ;
    }catch (Exception e) {
      System.out.println("==>>> Exeption when startd WatchDocumentSerice!!!!");
      //e.printStackTrace() ;
    }
  }

  public void stop() { }
}
