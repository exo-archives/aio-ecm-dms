/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;

import org.exoplatform.ecm.jcr.ECMNameValidator;
import org.exoplatform.ecm.jcr.JCRExceptionManager;
import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.services.cms.relations.RelationsService;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : pham tuan 
 *          phamtuanchip@yahoo.de
 * September 07, 2006
 * 08:57:15 AM
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/component/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIRenameForm.SaveActionListener.class),
      @EventConfig(listeners = UIRenameForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)

public class UIRenameForm extends UIForm implements UIPopupComponent {
  final static public String FIELD_OLDNAME =  "oldName" ;
  final static public String FIELD_NEWNAME = "newName" ;  

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
    getUIStringInput(FIELD_OLDNAME).setValue(renameNode.getName()) ;
    getUIStringInput(FIELD_OLDNAME).setEditable(false) ;
    getUIStringInput(FIELD_NEWNAME).setValue("") ;    
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
              relationsService.removeRelation(refNode, uiRenameForm.renameNode_.getPath()) ;
              refNode.save()  ;
            }
          }
        }
        String newName = uiRenameForm.getUIStringInput(FIELD_NEWNAME).getValue();
        String srcPath = uiRenameForm.renameNode_.getPath() ;
        String destPath ;
        if(uiRenameForm.isReferencedNode_) {        
          Node parent = uiRenameForm.renameNode_.getParent() ;
          if(parent.getPath().equals("/")) destPath = "/" + newName ; 
          else destPath = parent.getPath() + "/" + newName ;
        } else {
          if(uiJCRExplorer.getCurrentNode().getPath().equals("/")) destPath = "/" + newName ;
          else destPath = uiJCRExplorer.getCurrentNode().getPath() + "/" + newName ;
        }
        Session nodeSession = uiRenameForm.renameNode_.getSession() ;
        nodeSession.move(srcPath,destPath) ;
        nodeSession.save() ;
        nodeSession.refresh(false) ;
        for(int i = 0; i < refList.size(); i ++) {
          Node addRef = refList.get(i) ;
          relationsService.addRelation(addRef, destPath) ;
          addRef.save() ;
        }  
        if(!uiJCRExplorer.getPreference().isJcrEnable()) uiJCRExplorer.getSession().save() ;
//        Object[] args = {uiRenameForm.renameNode_.getName(), newName} ;
//        uiApp.addMessage(new ApplicationMessage("UIRenameForm.msg.rename-successfull", args)) ;
        uiJCRExplorer.updateAjax(event) ;
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp,e) ;
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

