/*
 * Created on Mar 24, 2006
 */
package org.exoplatform.services.cms.impl;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;

import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.jcr.impl.core.SessionImpl;

/**
 * @author benjaminmestrallet
 */
public class TestStoreNode extends BaseTest{
  
  public void testStoreNode() throws Exception{
    	
    Map mappings = new HashMap();     
    JcrInputProperty jcrInputProperty1 = new JcrInputProperty(); 
    JcrInputProperty jcrInputProperty2 = new JcrInputProperty();
    JcrInputProperty jcrInputProperty3 = new JcrInputProperty();
    jcrInputProperty1.setValue("MyTemplateNode");
    jcrInputProperty2.setValue("PropertyOfExotemplate");
    jcrInputProperty3.setValue("PropertyOfExoroles");
    mappings.put("/node", jcrInputProperty1);
    mappings.put("/node/exo:templateFile", jcrInputProperty2);
    mappings.put("/node/exo:roles", jcrInputProperty3);
    Node storeHomeNode = (Node) session.getItem("/cms");
    cmsService.storeNode("exo:template", storeHomeNode, mappings, true, "", "repository");    
    Property property = (Property) session.getItem("/cms/MyTemplateNode/exo:templateFile");   
    assertEquals(property.getString(), "PropertyOfExotemplate");
    
  }  

}
