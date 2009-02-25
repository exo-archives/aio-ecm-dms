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
package org.exoplatform.services.ecm.watch;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.services.ecm.BaseECMTestCase;
import org.exoplatform.services.ecm.core.JcrItemInput;
import org.exoplatform.services.ecm.core.NodeService;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS
 * Author : Ly Dinh Quang
 *          quang.ly@exoplatform.com
 *			    xxx5669@yahoo.com
 * Jun 3, 2008  
 */
public class TestWatchDocumentService extends BaseECMTestCase {
  final public int NOTIFICATION_BY_EMAIL = 1 ;
  
  public void testDocument() throws Exception {
    NodeService nodeService = (NodeService)container.getComponentInstanceOfType(NodeService.class);
    WatchDocumentService watchService = (WatchDocumentService)container.getComponentInstanceOfType(WatchDocumentService.class);
    SessionProviderService sessionProviderService_ = (SessionProviderService)container.getComponentInstanceOfType(SessionProviderService.class);
    SessionProvider sessionProvider = sessionProviderService_.getSystemSessionProvider(null);
    
    Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS) ;
    Node root = session.getRootNode();    
    
    Map<String , JcrItemInput> mapArticle = prepareArticleProperties();
    Node testArticle = nodeService.addNode(root, "exo:article", mapArticle, true);
    session.save();   
       
    //test WatchDocument
    System.out.println("\n\n-------------------TestWatchDocument");
    watchService.watchDocument(testArticle, "root", 1, sessionProvider);
    
    watchService.watchDocument(testArticle, "john", 1, sessionProvider);
    
    testArticle.setProperty("exo:title", "111");
    session.save();        
    
    //test UnwatchDocument  
    System.out.println("\n\n-------------------TestUnWatchDocument");
    watchService.unwatchDocument(testArticle, "root", 1, sessionProvider);
    testArticle.setProperty("exo:title", "222");
    session.save();
    
    //test getNotificationType
    System.out.println("\n\n-------------------TestGetNotificationType");
    int type1 = watchService.getNotificationType(testArticle, "root", sessionProvider);
    assertEquals(type1, -1);
    
    int type2 = watchService.getNotificationType(testArticle, "john", sessionProvider);
    assertEquals(type2, 1);
  }
  
  private Map<String, JcrItemInput> prepareArticleProperties() {
    Map<String, JcrItemInput> map = new HashMap<String, JcrItemInput>();
    JcrItemInput itemInput = getNodeInput("exo:article", "mix:votable", "MyArticle2", "/node");
    map.put("/node", itemInput);
    
    JcrItemInput itemInputPropText = getPropertyInput("/node/exo:text", "MyText2");
    map.put("/node/exo:text", itemInputPropText);
    
    JcrItemInput itemInputPropTitle = getPropertyInput("/node/exo:title", "MyTitle2");
    map.put("/node/exo:title", itemInputPropTitle);
    
    JcrItemInput itemInputPropSummary = getPropertyInput("/node/exo:summary", "MySummary2");
    map.put("/node/exo:summary", itemInputPropSummary);
    
    return map ;
  }

  private JcrItemInput getNodeInput(String primaryType, String mixinTypes, String nodeName, String path) {
    JcrItemInput jcrItemInput = new JcrItemInput();
    jcrItemInput.setType(JcrItemInput.NODE) ;    
    jcrItemInput.setPath(path);
    jcrItemInput.setPrimaryNodeType(primaryType);
    if (mixinTypes!= null && mixinTypes.length() > 0  ) {
      jcrItemInput.setMixinNodeType(mixinTypes);       
    }   
    jcrItemInput.setValue(nodeName);
    return jcrItemInput ;
  }  

  private JcrItemInput getPropertyInput(String propertyPath,Object propertyValue) {
    JcrItemInput jcrItemInput = new JcrItemInput();
    jcrItemInput.setPath(propertyPath);    
    jcrItemInput.setValue(propertyValue);
    return jcrItemInput ;
  }    
}
