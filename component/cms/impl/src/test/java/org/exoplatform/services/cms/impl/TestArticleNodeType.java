/*
 * Created on Mar 24, 2006
 */
package org.exoplatform.services.cms.impl;

import javax.jcr.Node;
import javax.jcr.PropertyType;

import org.exoplatform.container.PortalContainer;

/**
 * @author benjaminmestrallet
 */
public class TestArticleNodeType extends BaseTest{

  public void testArticleNodeType() throws Exception{       
    Node node = root.getNode("cms");    
    node = node.addNode("myArticle", "exo:article");
    node.setProperty("exo:title", "MyTitle");
    node.setProperty("exo:summary", "MySummary");
    node.setProperty("exo:text", "MyText");    
    node = node.addNode("exo:image", "nt:resource");    
    node.setProperty("jcr:mimeType", "image/gif");
    node.setProperty("jcr:data", session.getValueFactory().
    		createValue("345678J9UJ9UHJ87TFG78", PropertyType.BINARY));
    assertTrue(root.hasNode("cms/myArticle"));
    //session.save();    
  }
  
}
