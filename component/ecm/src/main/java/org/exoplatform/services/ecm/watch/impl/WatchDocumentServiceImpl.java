/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.ecm.watch.impl;

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
import org.exoplatform.services.ecm.template.impl.NodeTemplateServiceImpl;
import org.exoplatform.services.ecm.watch.WatchDocumentService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * Author : Ly Dinh Quang
 *          quang.ly@exoplatform.com
 *          xxx5669@yahoo.com
 * Jun 2, 2008  
 */
public class WatchDocumentServiceImpl implements WatchDocumentService, Startable {
  final public static String      EXO_WATCHABLE_MIXIN   = "exo:watchable".intern();
  final public static String      EMAIL_WATCHERS_PROP   = "exo:emailWatcher".intern();
  final public static String      RSS_WATCHERS_PROP     = "exo:rssWatcher".intern();
  final private String            initParamName         = "messageConfig".intern();
  final private static String     WATCHABLE_MIXIN_QUERY = "//element(*, exo:watchable)";
  
  private RepositoryService       repositoryService_;
  private MessageConfig           messageConfig_;
  private NodeTemplateServiceImpl templateService_;

  public WatchDocumentServiceImpl(InitParams initParams, RepositoryService repositoryService,
      NodeTemplateServiceImpl templateService) {
    repositoryService_ = repositoryService;
    templateService_ = templateService;
    messageConfig_ = (MessageConfig)initParams.getObjectParam(initParamName).getObject();
  }

  public void start() {
    try {
      reInitObserver();
    } catch (Exception e) {
      System.out.println("==>>> Exeption when started WatchDocumentSerice!!!!");
    }
  }

  public void stop() {
   
  }

  public int getNotificationType(Node documentNode, String userName, SessionProvider sessionProvider) throws Exception {
    if (!documentNode.isNodeType(EXO_WATCHABLE_MIXIN)) { return -1; }        
    boolean notifyByEmail = checkNotifyTypeOfWatcher(documentNode, userName, EMAIL_WATCHERS_PROP);        
    boolean notifyByRss = checkNotifyTypeOfWatcher(documentNode,userName,RSS_WATCHERS_PROP) ;    
    if (notifyByEmail && notifyByRss) return FULL_NOTIFICATION ;
    if (notifyByEmail) return NOTIFICATION_BY_EMAIL ;
    if (notifyByRss) return NOTIFICATION_BY_RSS ;
    return -1;
  }

  private boolean checkNotifyTypeOfWatcher(Node documentNode, String userName, String notificationType) throws Exception {
    if (documentNode.hasProperty(notificationType)) {
      Value [] watchers = documentNode.getProperty(notificationType).getValues() ;
      for (Value value: watchers) {
        if (value.getString().equalsIgnoreCase(userName)) return true ;
      }
    }
    return false ;
  }  
  
  public void unwatchDocument(Node documentNode, String userName, int notificationType, SessionProvider sessionProvider) throws Exception {
    if (!documentNode.isNodeType(EXO_WATCHABLE_MIXIN)) return;    
    if (notificationType == NOTIFICATION_BY_EMAIL) {
      ManageableRepository manageRepository = (ManageableRepository)documentNode.getSession().getRepository();
      String workspace = manageRepository.getConfiguration().getDefaultWorkspaceName();
      Session session = sessionProvider.getSession(workspace, manageRepository);
      Value[] values = documentNode.getProperty(EMAIL_WATCHERS_PROP).getValues();
      List<Value> listValue = new ArrayList<Value>();
      for (Value value : values) {
        if (!value.getString().equals(userName)) {
          listValue.add(value);
        }
      }
      documentNode.setProperty(EMAIL_WATCHERS_PROP, listValue.toArray(new Value[listValue.size()]));
      session.save();
    }    
  }

  public void watchDocument(Node documentNode, String userName, int notifyType, SessionProvider sessionProvider) throws Exception {    
    ManageableRepository manageRepository = (ManageableRepository)documentNode.getSession().getRepository();
    String workspace = manageRepository.getConfiguration().getDefaultWorkspaceName();
    Session session = sessionProvider.getSession(workspace, manageRepository);
    Value newWatcher = session.getValueFactory().createValue(userName);
    if (!documentNode.isNodeType(EXO_WATCHABLE_MIXIN)) {      
      documentNode.addMixin(EXO_WATCHABLE_MIXIN);
      if (notifyType == NOTIFICATION_BY_EMAIL) {        
        documentNode.setProperty(EMAIL_WATCHERS_PROP, new Value[] {newWatcher});        
        EmailNotifyListener emailListener = new EmailNotifyListener(documentNode);
        observeNode(documentNode, emailListener, sessionProvider);        
      }
      session.save();
    } else {      
      if (notifyType == NOTIFICATION_BY_EMAIL) {
        List<Value> watcherList = new ArrayList<Value>();
        if (documentNode.hasProperty(EMAIL_WATCHERS_PROP)) {
          Value[] values = documentNode.getProperty(EMAIL_WATCHERS_PROP).getValues();
          for (Value value : values) {
            watcherList.add(value);
          }
        } 
        watcherList.add(newWatcher);
        documentNode.setProperty(EMAIL_WATCHERS_PROP, watcherList.toArray(new Value[watcherList.size()]));
        session.save();
      }      
    }
  }
  
  private void observeNode(Node node, EventListener listener, SessionProvider sessionProvider) throws Exception {
    ManageableRepository manageRepo = (ManageableRepository)node.getSession().getRepository();    
    String workspace = manageRepo.getConfiguration().getDefaultWorkspaceName();    
    Session session = sessionProvider.getSession(workspace, manageRepo);
    List<String> listNodeType = getDocumentNodeTypes(node, sessionProvider);
    String[] observedNodeTypeNames = listNodeType.toArray(new String[listNodeType.size()]);    
    ObservationManager observationManager = session.getWorkspace().getObservationManager();
    observationManager.addEventListener(listener, Event.PROPERTY_CHANGED, node.getPath(), true, null, observedNodeTypeNames, false);   
  }
  
  private List<String> getDocumentNodeTypes(Node node, SessionProvider sessionProvider) throws Exception {    
    List<String> listNodeType = new ArrayList<String>();
    NodeType primayNode = node.getPrimaryNodeType();
    String repository = ((ManageableRepository)node.getSession().getRepository()).getConfiguration().getName();
    
//   use these code to test case
//    NodeType[] nodeTypes = node.getMixinNodeTypes();
//    listNodeType.add(primayNode.getName());
//    for (NodeType nodeType : nodeTypes) {
//      listNodeType.add(nodeType.getName());
//    }
    
    if (templateService_.isManagedNodeType(primayNode.getName(), repository, sessionProvider)) {      
      listNodeType.add(primayNode.getName());
    }
    NodeType[] nodeTypes = node.getMixinNodeTypes();   
    for (NodeType nodeType : nodeTypes) {
      if (templateService_.isManagedNodeType(nodeType.getName(), repository, sessionProvider)) {        
        listNodeType.add(nodeType.getName());
      }
    }
    return listNodeType;
  }
  
  protected MessageConfig getMessageConfig() { return messageConfig_ ; }
  
  private void reInitObserver() throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    for (RepositoryEntry entry : repositoryService_.getConfig().getRepositoryConfigurations()) {
      ManageableRepository manageRepository = repositoryService_.getRepository(entry.getName());
      String workspaces[] = manageRepository.getWorkspaceNames();
      for (String workspace : workspaces) {
        Session session = sessionProvider.getSession(workspace, manageRepository);
        QueryManager queryManage = null;
        try {
          queryManage = session.getWorkspace().getQueryManager();
        } catch (Exception e) {
          e.printStackTrace();
        }
        if (queryManage == null) { continue; }
        try {
          Query query = queryManage.createQuery(WATCHABLE_MIXIN_QUERY, Query.XPATH);
          QueryResult queryResult = query.execute();
          NodeIterator iterate = queryResult.getNodes();
          while (iterate.hasNext()) {
            Node node = iterate.nextNode();
            EmailNotifyListener listener = new EmailNotifyListener(node);   
            observeNode(node, listener, sessionProvider);                           
          }
        } catch (Exception e) {
          System.out.println("==>Can not init observer for node in repository: " + entry.getName());          
        }
      }
    }
    sessionProvider.close();
  }
}
