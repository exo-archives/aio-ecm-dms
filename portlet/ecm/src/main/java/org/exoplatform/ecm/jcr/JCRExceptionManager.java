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

import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.core.UIApplication;

public class JCRExceptionManager {

  public static void process(UIApplication uiApp,Exception e,String messageKey) throws Exception{
    if(e instanceof LoginException) {
      if(messageKey == null) messageKey = "LoginException.msg";      
    } else if(e instanceof AccessDeniedException) {
      if(messageKey == null) messageKey = "AccessDeniedException.msg";      
    } else if(e instanceof NoSuchWorkspaceException) {
      if(messageKey == null) messageKey = "NoSuchWorkspaceException.msg";      
    } else if(e instanceof ItemNotFoundException) { 
      if(messageKey == null) messageKey = "ItemNotFoundException.msg";      
    } else if(e instanceof ItemExistsException) {
      if(messageKey == null) messageKey = "ItemExistsException.msg";      
    } else if(e instanceof ConstraintViolationException) { 
      if(messageKey == null) messageKey = "ConstraintViolationException.msg";      
    } else if(e instanceof InvalidItemStateException) { 
      if(messageKey == null) messageKey = "InvalidItemStateException.msg";      
    } else if(e instanceof ReferentialIntegrityException) { 
      if(messageKey == null) messageKey = "ReferentialIntegrityException.msg";      
    } else if(e instanceof LockException) { 
      if(messageKey == null) messageKey = "LockException.msg";      
    } else if(e instanceof NoSuchNodeTypeException) { 
      if(messageKey == null) messageKey = "NoSuchNodeTypeException.msg";      
    } else if(e instanceof VersionException) {
      if(messageKey == null) messageKey = "VersionException.msg";      
    } else if(e instanceof PathNotFoundException) {
      if(messageKey == null) messageKey = "PathNotFoundException.msg";      
    } else if(e instanceof ValueFormatException) {
      if(messageKey == null) messageKey = "ValueFormatException.msg";      
    } else if(e instanceof InvalidSerializedDataException) {
      if(messageKey == null) messageKey = "InvalidSerializedDataException.msg";      
    } else if(e instanceof RepositoryException) {
      if(messageKey == null) messageKey = "RepositoryException.msg";      
    } else if(e instanceof AccessControlException) {
      if(messageKey == null) messageKey = "AccessControlException.msg";      
    } else if(e instanceof UnsupportedRepositoryOperationException) {
      if(messageKey == null) messageKey = "UnsupportedRepositoryOperationException.msg";      
    } else {
      throw e;
    }
    uiApp.addMessage(new ApplicationMessage(messageKey,null,ApplicationMessage.ERROR)) ;
  }

  public static void process(UIApplication uiApp, Exception e) throws Exception {
    process(uiApp,e,null) ;
  }
}
