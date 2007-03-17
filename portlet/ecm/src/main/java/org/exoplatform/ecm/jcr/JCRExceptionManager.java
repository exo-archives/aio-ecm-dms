package org.exoplatform.ecm.jcr;

import java.security.AccessControlException;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.InvalidSerializedDataException;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.PathNotFoundException;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

import org.exoplatform.webui.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;

public class JCRExceptionManager {

  public static void process(UIApplication uiApp, Exception e) throws Exception {    
    if(e instanceof LoginException) {
      uiApp.addMessage(new ApplicationMessage("LoginException.msg", null, 
                                              ApplicationMessage.WARNING)) ;
    } else if(e instanceof AccessDeniedException) {
      uiApp.addMessage(new ApplicationMessage("AccessDeniedException.msg", null, 
                                              ApplicationMessage.WARNING)) ;
    } else if(e instanceof NoSuchWorkspaceException) {
      uiApp.addMessage(new ApplicationMessage("NoSuchWorkspaceException.msg", null, 
                                              ApplicationMessage.WARNING)) ;
    } else if(e instanceof ItemNotFoundException) { 
      uiApp.addMessage(new ApplicationMessage("ItemNotFoundException.msg", null, 
                                              ApplicationMessage.WARNING)) ;
    } else if(e instanceof ItemExistsException) { 
      uiApp.addMessage(new ApplicationMessage("ItemExistsException.msg", null, 
                                              ApplicationMessage.WARNING)) ;
    } else if(e instanceof ConstraintViolationException) { 
      uiApp.addMessage(new ApplicationMessage("ConstraintViolationException.msg", null, 
                                              ApplicationMessage.WARNING)) ;
    } else if(e instanceof InvalidItemStateException) { 
      uiApp.addMessage(new ApplicationMessage("InvalidItemStateException.msg", null, 
                                              ApplicationMessage.WARNING)) ;
    } else if(e instanceof ReferentialIntegrityException) { 
      uiApp.addMessage(new ApplicationMessage("ReferentialIntegrityException.msg", null, 
                                              ApplicationMessage.WARNING)) ;
    } else if(e instanceof LockException) { 
      uiApp.addMessage(new ApplicationMessage("LockException.msg", null, 
                                              ApplicationMessage.WARNING)) ;
    } else if(e instanceof NoSuchNodeTypeException) { 
      uiApp.addMessage(new ApplicationMessage("NoSuchNodeTypeException.msg", null, 
                                              ApplicationMessage.WARNING)) ;
    } else if(e instanceof VersionException) { 
      uiApp.addMessage(new ApplicationMessage("VersionException.msg", null, 
                                              ApplicationMessage.WARNING)) ;
    } else if(e instanceof PathNotFoundException) { 
      uiApp.addMessage(new ApplicationMessage("PathNotFoundException.msg", null, 
                                              ApplicationMessage.WARNING)) ;
    } else if(e instanceof ValueFormatException) { 
      uiApp.addMessage(new ApplicationMessage("ValueFormatException.msg", null, 
                                              ApplicationMessage.WARNING)) ;
    } else if(e instanceof InvalidSerializedDataException) { 
      uiApp.addMessage(new ApplicationMessage("InvalidSerializedDataException.msg", null, 
                                              ApplicationMessage.WARNING)) ;
    } else if(e instanceof RepositoryException) {
      uiApp.addMessage(new ApplicationMessage("RepositoryException.msg", null, 
                                              ApplicationMessage.WARNING)) ;
    } else if(e instanceof AccessControlException) {
      uiApp.addMessage(new ApplicationMessage("AccessControlException.msg", null, 
                                              ApplicationMessage.WARNING)) ;
    } else if(e instanceof UnsupportedRepositoryOperationException) {
    	uiApp.addMessage(new ApplicationMessage("UnsupportedRepositoryOperationException.msg", null, 
                                              ApplicationMessage.WARNING)) ;
		} else {
      throw e ;
    }
  }
}
