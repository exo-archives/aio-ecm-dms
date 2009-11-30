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
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.ecm.jcr.model.Preference;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.sidebar.UISideBar;
import org.exoplatform.ecm.webui.component.explorer.sidebar.UITagExplorer;
import org.exoplatform.services.cms.folksonomy.NewFolksonomyService;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Anh Vu
 *          anhvurz90@gmail.com
 * Nov 27, 2009  
 * 5:03:28 PM
 */
@ComponentConfig( 
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UITagForm.UpdateTagActionListener.class),
      @EventConfig(listeners = UITagForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UITagForm extends UIForm {

  final static public String TAG_NAME = "tagName" ;
  final static public String RELATED_DOCUMENTS = "relatedDocuments" ;
  final static public String PUBLIC_TAG_NODE_PATH = "exoPublicTagNode";
  
  private Node selectedTag_ ;
  private String oldTagPath_;
  private String oldName_;
  
  public UITagForm() throws Exception {
    addUIFormInput(new UIFormStringInput(TAG_NAME, TAG_NAME, null).addValidator(MandatoryValidator.class)) ;
    addUIFormInput(new UIFormStringInput(RELATED_DOCUMENTS , RELATED_DOCUMENTS , null)) ;
  }
  
  public Node getTag() { return selectedTag_; }
  
  public void setTag(Node selectedTag) throws Exception { 
    selectedTag_ = selectedTag;
    getUIStringInput(RELATED_DOCUMENTS).setEditable(false);
    if (selectedTag != null) {
    	oldTagPath_ = selectedTag_.getPath();
    	oldName_ = selectedTag_.getName();
	    getUIStringInput(TAG_NAME).setValue(oldName_);
//	    getUIStringInput(TAG_NAME).setEditable(false) ;
	    long relatedDocuments = selectedTag_.getProperty(UIEditingTagList.EXO_TOTAL).getValue().getLong();    
	    getUIStringInput(RELATED_DOCUMENTS).setValue(relatedDocuments + "") ;
    } else 
    	getUIStringInput(RELATED_DOCUMENTS).setRendered(false);    
  }
  
  static public class UpdateTagActionListener extends EventListener<UITagForm> {
    public void execute(Event<UITagForm> event) throws Exception {
      UITagForm uiForm = event.getSource() ;
      UIEditingTagsForm uiEdit = uiForm.getAncestorOfType(UIEditingTagsForm.class) ;
      UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class);
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      
      String repository = uiForm.getAncestorOfType(UIJCRExplorer.class).getRepositoryName();
      String workspace = uiForm.getAncestorOfType(UIJCRExplorer.class).getRepository().getConfiguration().getDefaultWorkspaceName();
      String userName = uiExplorer.getSession().getUserID();
      int scope = uiExplorer.getTagScope();
      
      String newTagName = uiForm.getUIStringInput(TAG_NAME).getValue() ;
//		TODO : check tag name      
//      if(!uiForm.validateRange(documentRange)) {
//        uiApp.addMessage(new ApplicationMessage("UITagStyleForm.msg.range-validator", null)) ;
//        return ;
//      }
      try {
      	// add new tag
      	if (uiForm.getTag() == null) {
      		String tagName = uiForm.getUIStringInput(TAG_NAME).getValue();
          NewFolksonomyService newFolksonomyService = uiForm.getApplicationComponent(NewFolksonomyService.class) ;
          if (scope == UITagExplorer.PRIVATE) { 
          	newFolksonomyService.addPrivateTag(new String[] { tagName }, 
          																		 null, 
          																		 repository, 
          																		 workspace, 
          																		 userName);	
          }
          if (scope == UITagExplorer.PUBLIC) {
          	NodeHierarchyCreator nodeHierarchyCreator = uiForm.getApplicationComponent(NodeHierarchyCreator.class);
          	String publicTagNodePath = nodeHierarchyCreator.getJcrPath(PUBLIC_TAG_NODE_PATH);
          	newFolksonomyService.addPublicTag(publicTagNodePath, 
          																		new String[] { tagName },
          																		null, 
          																		repository, 
          																		workspace); 
          }
      	}
      	// rename tag
      	else {
      		String tagName = uiForm.getUIStringInput(TAG_NAME).getValue();      		
          NewFolksonomyService newFolksonomyService = uiForm.getApplicationComponent(NewFolksonomyService.class) ;
          if (!existTag(tagName, repository, workspace, scope, uiForm, userName)) {
          	newFolksonomyService.modifyTagName(uiForm.oldTagPath_, tagName, repository, workspace);
          } else {
        	 uiApp.addMessage(new ApplicationMessage("UITagForm.msg.NameAlreadyExist", null, 
          	              ApplicationMessage.ERROR));
//           event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          }
      	}
        
        UIEditingTagList uiTagList = uiEdit.getChild(UIEditingTagList.class) ;
        uiTagList.updateGrid() ;
      } catch(Exception e) {
        String key = "UITagStyleForm.msg.error-update" ;
        uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING));
        return ;
      }
      UIPopupWindow uiPopup = uiEdit.getChild(UIPopupWindow.class) ;
      uiPopup.setShow(false) ;

      Preference preferences = uiExplorer.getPreference();
      if (preferences.isShowSideBar()) {
        UISideBar uiSideBar = uiExplorer.findFirstComponentOfType(UISideBar.class);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiSideBar);
      }      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiEdit) ;
    }
    
    private boolean existTag(String tagName, String repo, String workspace, int scope, 
    												 UITagForm uiForm, String userName) throws Exception {
    	NewFolksonomyService newFolksonomyService = uiForm.getApplicationComponent(NewFolksonomyService.class) ;
    	NodeHierarchyCreator nodeHierarchyCreator = uiForm.getApplicationComponent(NodeHierarchyCreator.class);
    	String publicTagNodePath = nodeHierarchyCreator.getJcrPath(PUBLIC_TAG_NODE_PATH);
    	List<Node> tagList = (scope == UITagExplorer.PUBLIC) ?	
    												newFolksonomyService.getAllPublicTags(publicTagNodePath, repo, workspace) :
    												newFolksonomyService.getAllPrivateTags(userName, repo, workspace);
			for (Node tag : tagList)
				if (tag.getName().equals(tagName))
					return true;
			return false;
    }
  }
  

  static public class CancelActionListener extends EventListener<UITagForm> {
    public void execute(Event<UITagForm> event) throws Exception {
      UITagForm uiForm = event.getSource() ;
      UIEditingTagsForm uiEdit = uiForm.getAncestorOfType(UIEditingTagsForm.class) ;
      UIPopupWindow uiPopup = uiEdit.getChild(UIPopupWindow.class) ;
      uiPopup.setShow(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiEdit);
    }
  }
  
}
