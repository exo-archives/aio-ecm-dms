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
public class TestNodeProperties extends BaseTest{
      
  public void testCreation() throws Exception{
    assertTrue(root.hasNode("cms/home/users"));
    assertTrue(root.hasNode("cms/home/groups"));
    Node homenode = root.getNode("cms/home");      
    Node usersnode = root.getNode("cms/home/users");
    Node groupsnode = root.getNode("cms/home/groups");
    assertEquals(4, usersnode.getProperties().getSize());
    assertEquals(4, groupsnode.getProperties().getSize());
    //***************************************************************
    Node newnode = homenode.addNode("newnode");
    newnode.remove();
    session.save();
    assertFalse(root.hasNode("cms/home/newnode"));
    
    assertEquals(4, usersnode.getProperties().getSize());
    assertEquals(4, groupsnode.getProperties().getSize());
    //***************************************************************
    Node newnode1 = homenode.addNode("newnode");
    newnode1.remove();
    session.save();
    assertFalse(root.hasNode("cms/home/newnode"));
    
    assertEquals(4, usersnode.getProperties().getSize());
    assertEquals(4, groupsnode.getProperties().getSize());
    //***************************************************************
    /*
    Node node = root.getNode("/cms/home/users/portal/products/product");    
    assertTrue(node.hasNode("product_en.html"));
    Property prop = node.getNode("product_en.html").getNode("jcr:content").getProperty("jcr:data");
    assertNotNull(prop.getString());    
    orgService.getUserHandler().removeUser("exo", true);
    */
  }

}
