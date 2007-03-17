/*
 * Created on Mar 24, 2006
 */
package org.exoplatform.services.cms.impl;

import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.RepositoryException;

/**
 * @author benjaminmestrallet
 */
public class TestDialogs extends BaseTest{
  
  public void testInit() throws LoginException, NoSuchWorkspaceException, RepositoryException{
    assertTrue(root.hasNode("cms/templates"));  
  }
  
  public void testGetDialog() throws Exception {
    String template = dialogService.getDefaultTemplatePath(true, "nt:resource");    
    assertEquals("/cms/templates/nt:resource/dialogs/dialog1", template);
  }
    
}
