/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.form.validator.ECMNameValidator;
import org.exoplatform.ecm.webui.popup.UIPopupComponent;
import org.exoplatform.ecm.webui.utils.JCRExceptionManager;
import org.exoplatform.ecm.webui.utils.LockUtil;
import org.exoplatform.services.cms.relations.RelationsService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : pham tuan 
 *          phamtuanchip@yahoo.de
 * September 07, 2006
 * 08:57:15 AM
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIRenameForm.SaveActionListener.class),
      @EventConfig(listeners = UIRenameForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)

public class UIRenameForm extends UIForm implements UIPopupComponent {
  final static public String FIELD_OLDNAME =  "oldName" ;
  final static public String FIELD_NEWNAME = "newName" ;  
  final static private String RELATION_PROP = "exo:relation";

  private Node renameNode_ ;
  private boolean isReferencedNode_ = false ;

  public UIRenameForm() throws Exception {    
    addUIFormInput(new UIFormStringInput(FIELD_OLDNAME, FIELD_OLDNAME, null)) ;
    addUIFormInput(new UIFormStringInput(FIELD_NEWNAME, FIELD_NEWNAME, null).
                   addValidator(ECMNameValidator.class)) ;
  }

  public void update(Node renameNode, boolean isReferencedNode) throws Exception {
    isReferencedNode_ = isReferencedNode ;
    renameNode_ = renameNode ;
    String renamePath = renameNode.getPath() ;
    String oldName = renamePath.substring(renamePath.lastIndexOf("/") + 1, renamePath.length()) ;   
    getUIStringInput(FIELD_OLDNAME).setValue(oldName) ;
    getUIStringInput(FIELD_OLDNAME).setEditable(false) ;
    getUIStringInput(FIELD_NEWNAME).setValue("") ;    
  }
  
  private void changeLockForChild(String srcPath, Node parentNewNode) throws Exception {
    if(parentNewNode.hasNodes()) {
      NodeIterator newNodeIter = parentNewNode.getNodes();
      String newSRCPath = null;
      while(newNodeIter.hasNext()) {
        Node newChildNode = newNodeIter.nextNode();
        newSRCPath = newChildNode.getPath().replace(parentNewNode.getPath(), srcPath);
        if(newChildNode.isLocked()) LockUtil.changeLockToken(newSRCPath, newChildNode);
        if(newChildNode.hasNodes()) changeLockForChild(newSRCPath, newChildNode);
      }
    }
  }

  static  public class SaveActionListener extends EventListener<UIRenameForm> {
    public void execute(Event<UIRenameForm> event) throws Exception {      
      UIRenameForm uiRenameForm = event.getSource() ; 
      RelationsService relationsService = 
        uiRenameForm.getApplicationComponent(RelationsService.class);
      UIJCRExplorer uiJCRExplorer = uiRenameForm.getAncestorOfType(UIJCRExplorer.class) ;
      List<Node> refList =  new ArrayList<Node>() ;
      boolean isReference = false ;
      PropertyIterator references = null ;    
      UIApplication uiApp = uiRenameForm.getAncestorOfType(UIApplication.class) ;
      try {  
        try {
          references = uiRenameForm.renameNode_.getReferences() ;
          isReference = true ;
        } catch(Exception e) {
          isReference = false ;
        }        
        if(isReference && references != null) {
          if(references.getSize() > 0 ) {
            while (references.hasNext()) {
              Property pro = references.nextProperty() ;
              Node refNode = pro.getParent() ;
              if(refNode.hasProperty(RELATION_PROP)) {
                relationsService.removeRelation(refNode, uiRenameForm.renameNode_.getPath(), 
                    uiJCRExplorer.getRepositoryName()) ;
                refNode.save()  ;
              }
            }
          }
        }
        String newName = uiRenameForm.getUIStringInput(FIELD_NEWNAME).getValue();        
        String[] arrFilterChar = {"&", "$", "@", ":", "]", "[", "*", "%", "!", "+", "(", ")", "'", "#", ";", "}", "{", "/", "|", "\""}; 
        for(String filterChar : arrFilterChar) {
          if(newName.indexOf(filterChar) > -1) {
            uiApp.addMessage(new ApplicationMessage("UIFolderForm.msg.name-not-allowed", null, 
                ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          }
        }        
        String srcPath = uiRenameForm.renameNode_.getPath() ;
        String destPath ;
        if(uiJCRExplorer.nodeIsLocked(uiRenameForm.renameNode_)) {  
          
          uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", null, 
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          uiJCRExplorer.updateAjax(event) ;        
          return ;
        }      
        if(uiRenameForm.isReferencedNode_) {        
          Node parent = uiRenameForm.renameNode_.getParent() ;
          if(parent.getPath().equals("/")) destPath = "/" + newName ; 
          else destPath = parent.getPath() + "/" + newName ;
        } else {
          if(uiJCRExplorer.getCurrentPath().equals("/")) destPath = "/" + newName ;
          else destPath = uiJCRExplorer.getCurrentPath()+ "/" + newName ;
        }
        Session nodeSession = uiRenameForm.renameNode_.getSession() ;
        Node parentNode = uiRenameForm.renameNode_.getParent() ;
        if(parentNode.isLocked()) {
          String lockToken1 = LockUtil.getLockToken(parentNode);
          nodeSession.addLockToken(lockToken1) ;
        }
        nodeSession.refresh(true) ;
        nodeSession.getWorkspace().move(srcPath,destPath) ;
        String currentPath = uiJCRExplorer.getCurrentPath() ;
        if(srcPath.equals(uiJCRExplorer.getCurrentPath())) {
          uiJCRExplorer.setCurrentPath(destPath) ;
        } else if(currentPath.startsWith(srcPath)) {
          uiJCRExplorer.setCurrentPath(destPath + currentPath.substring(currentPath.lastIndexOf("/"))) ;
        }
        nodeSession.save() ;
        nodeSession.refresh(false) ;
        for(int i = 0; i < refList.size(); i ++) {
          Node addRef = refList.get(i) ;
          relationsService.addRelation(addRef, destPath, nodeSession.getWorkspace().getName(),uiJCRExplorer.getRepositoryName()) ;
          addRef.save() ;
        }
        Node destNode = (Node) nodeSession.getItem(destPath) ;
        if(destNode.isLocked()) LockUtil.changeLockToken(uiRenameForm.renameNode_, destNode) ;
        uiRenameForm.changeLockForChild(srcPath, destNode);
        nodeSession.logout() ;
        if(!uiJCRExplorer.getPreference().isJcrEnable()) uiJCRExplorer.getSession().save() ;
        uiJCRExplorer.updateAjax(event) ;
      } catch(AccessDeniedException ace) {
        uiJCRExplorer.getSession().refresh(false) ;
        uiJCRExplorer.refreshExplorer() ;
        uiJCRExplorer.cancelAction() ;
        Object[] args = {uiRenameForm.renameNode_.getName()} ;
        uiApp.addMessage(new ApplicationMessage("UIRenameForm.msg.rename-denied", args, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
       
        return ;
      } catch(VersionException ve) {
        uiJCRExplorer.getSession().refresh(false) ;
        uiJCRExplorer.refreshExplorer() ;
        uiJCRExplorer.cancelAction() ;
        uiApp.addMessage(new ApplicationMessage("UIRenameForm.msg.version-exception", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        
        return ;
      } catch(ConstraintViolationException cons) {
        uiJCRExplorer.getSession().refresh(false) ;
        uiJCRExplorer.refreshExplorer() ;
        uiJCRExplorer.cancelAction() ;
        Object[] args = {uiRenameForm.renameNode_.getPrimaryNodeType().getName()} ;
        uiApp.addMessage(new ApplicationMessage("UIRenameForm.msg.constraintViolation-exception", args,
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
       
        return ;     
      } catch(LockException lockex){
        lockex.printStackTrace() ;
        Object[] agrs = {uiRenameForm.renameNode_.getPrimaryNodeType().getName()};
        uiApp.addMessage(new ApplicationMessage("UIRenameForm.msg.lock-exception", agrs, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp,e) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }     
    }
  }

  static  public class CancelActionListener extends EventListener<UIRenameForm> {
    public void execute(Event<UIRenameForm> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction() ;
    }
  }

  public void activate() throws Exception { 
  }

  public void deActivate() throws Exception { }
  
}
