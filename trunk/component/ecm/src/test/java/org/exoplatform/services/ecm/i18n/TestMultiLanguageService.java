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
package org.exoplatform.services.ecm.i18n;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.services.ecm.BaseECMTestCase;
import org.exoplatform.services.ecm.core.JcrItemInput;

/**
 * Created by The eXo Platform SAS
 * Author : Ly Dinh Quang
 *          quang.ly@exoplatform.com
 *          xxx5669@yahoo.com
 * May 13, 2008  
 */
public class TestMultiLanguageService extends BaseECMTestCase{
  private MultiLanguageService multiLanguageService_;
  
  public void testAddLanguage() throws Exception {
    System.out.println("\n\n----------------------------------------TestAddLanguage");    
    //Prepare data
    Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS);
    Node root = session.getRootNode();
    Node test = root.addNode("Test", "nt:unstructured");    
        
    session.save();
            
    Map<String, JcrItemInput> map = new HashMap<String, JcrItemInput>();
    JcrItemInput jcrItemInput = new JcrItemInput();
    jcrItemInput.setType(jcrItemInput.NODE);
    jcrItemInput.setPath("/node") ;
    jcrItemInput.setMixinNodeType("mix:votable");
    jcrItemInput.setPrimaryNodeType("nt:unstructured") ;
    jcrItemInput.setValue("Test") ;
    map.put("/node", jcrItemInput) ;
       
    //properties case
    JcrItemInput jcrItemInputProperties = new JcrItemInput() ;
    jcrItemInputProperties.setType(JcrItemInput.PROPERTY) ;
    jcrItemInputProperties.setPath("/node/exo:title");
    jcrItemInputProperties.setValue("PropertiesCaseTest") ;
    map.put("/node/exo:title", jcrItemInputProperties) ;    
    
    //test
    multiLanguageService_ = (MultiLanguageService)container.getComponentInstanceOfType(MultiLanguageService.class);
    multiLanguageService_.addLanguage(test, map, "English", false);
    multiLanguageService_.addLanguage(test, map, "Vietnamese", false);
    multiLanguageService_.addLanguage(test, map, "French", false);
    
    Node nodeEnglish = multiLanguageService_.getLanguage(test, "English");
    assertNotNull(nodeEnglish);
    
    Node nodeVietName = multiLanguageService_.getLanguage(test, "Vietnamese");
    assertNotNull(nodeVietName);
    
    Node nodeFrench = multiLanguageService_.getLanguage(test, "French");
    assertNotNull(nodeFrench);
    
    Node nodeSpain = multiLanguageService_.getLanguage(test, "Spanish");
    assertNull(nodeSpain);
  }
}
