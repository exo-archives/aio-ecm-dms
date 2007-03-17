/*
 * Created on Mar 24, 2006
 */
package org.exoplatform.services.cms.impl;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.Property;

import org.exoplatform.services.organization.User;

/**
 * @author Benjamin Mestrallet
 * benjamin.mestrallet@exoplatform.com
 */
public class TestNewUserListener extends BaseTest{
      
  public void testCreation() throws Exception{            
    User newuser = orgService.getUserHandler().createUserInstance();                    
    newuser.setUserName("newuser");
    newuser.setPassword("exo");
    newuser.setFirstName("exo");
    newuser.setLastName("platform");
    newuser.setEmail("exo@exoportal.org");    
    orgService.getUserHandler().createUser(newuser,true);
    assertTrue(root.hasNode("cms/home/users/newuser"));
    assertTrue(root.hasNode("cms/home/users/demo"));
    /*
    Node node = root.getNode("/cms/home/users/portal/products/product");    
    assertTrue(node.hasNode("product_en.html"));
    Property prop = node.getNode("product_en.html").getNode("jcr:content").getProperty("jcr:data");
    assertNotNull(prop.getString());    
    orgService.getUserHandler().removeUser("exo", true);
    */
  }

}
