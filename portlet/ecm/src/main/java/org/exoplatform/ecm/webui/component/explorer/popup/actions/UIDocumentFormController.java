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

import javax.jcr.Node;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Nov 8, 2006 10:16:18 AM 
 */

@ComponentConfig (lifecycle = UIContainerLifecycle.class)

public class UIDocumentFormController extends UIContainer implements UIPopupComponent {

  private String defaultDocument_ ;
  private static String DEFAULT_VALUE = "exo:article" ;
  private Node currentNode_ ;
  private String repository_ ;
  private String workspace_ ;

  public UIDocumentFormController() throws Exception {
    addChild(UISelectDocumentForm.class, null, null) ;
    UIDocumentForm uiDocumentForm = createUIComponent(UIDocumentForm.class, null, null) ;
    uiDocumentForm.setTemplateNode(DEFAULT_VALUE) ;
    uiDocumentForm.addNew(true) ;
    addChild(uiDocumentForm) ;
  }

  public void setCurrentNode(Node node) { currentNode_ = node ; }

  public void setRepository(String repository) { repository_ = repository ; }

  public void setWorkspace(String workspace) { workspace_ = workspace ; }

  public void initPopup(UIComponent uiComp) throws Exception {
    removeChildById("PopupComponent") ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, "PopupComponent") ;
    uiPopup.setUIComponent(uiComp) ;
    uiPopup.setWindowSize(640, 300) ;
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;
  }

  public List<SelectItemOption<String>> getListFileType() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    List<String> nodeTypes = new ArrayList<String>() ;
    UISelectDocumentForm uiSelectForm = getChild(UISelectDocumentForm.class) ;
    UIFormSelectBox uiSelectBox = uiSelectForm.getUIFormSelectBox(UISelectDocumentForm.FIELD_SELECT) ;
    boolean hasDefaultDoc = false ;
    NodeTypeManager ntManager = currentNode_.getSession().getWorkspace().getNodeTypeManager() ; 
    NodeType currentNodeType = currentNode_.getPrimaryNodeType() ; 
    NodeDefinition[] childDefs = currentNodeType.getChildNodeDefinitions() ;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    List templates = templateService.getDocumentTemplates(repository_) ;
    try {
      for(int i = 0; i < templates.size(); i ++){
        String nodeTypeName = templates.get(i).toString() ;        
        String label = templateService.getTemplateLabel(nodeTypeName, repository_) ;
        NodeType nodeType = ntManager.getNodeType(nodeTypeName) ;
        if(nodeType.isMixin()) {
          if(!nodeTypes.contains(nodeTypeName)) {
            options.add(new SelectItemOption<String>(label, nodeTypeName));
            nodeTypes.add(nodeTypeName) ;
            continue;
          }
        }
        NodeType[] superTypes = nodeType.getSupertypes() ;
        boolean isCanCreateDocument = false ;
        for(NodeDefinition childDef : childDefs){
          NodeType[] requiredChilds = childDef.getRequiredPrimaryTypes() ;
          for(NodeType requiredChild : requiredChilds) {          
            if(nodeTypeName.equals(requiredChild.getName())){            
              isCanCreateDocument = true ;
              break ;
            }            
          }
          if(nodeTypeName.equals(childDef.getName()) || isCanCreateDocument) {
            if(!hasDefaultDoc && nodeTypeName.equals(DEFAULT_VALUE)) {
              defaultDocument_ = DEFAULT_VALUE ;
              hasDefaultDoc = true ;
            }            
            if(!nodeTypes.contains(nodeTypeName)) {
              options.add(new SelectItemOption<String>(label, nodeTypeName));
              nodeTypes.add(nodeTypeName) ;
            }
            isCanCreateDocument = true ;          
          }
        }      
        if(!isCanCreateDocument){
          for(NodeType superType:superTypes) {
            for(NodeDefinition childDef : childDefs){          
              for(NodeType requiredType : childDef.getRequiredPrimaryTypes()) {              
                if (superType.getName().equals(requiredType.getName())) {
                  if(!hasDefaultDoc && nodeTypeName.equals(DEFAULT_VALUE)) {
                    defaultDocument_ = DEFAULT_VALUE ;
                    hasDefaultDoc = true ;
                  }                  
                  if(!nodeTypes.contains(nodeTypeName)) {
                    options.add(new SelectItemOption<String>(label, nodeTypeName));
                    nodeTypes.add(nodeTypeName) ;
                  }
                  isCanCreateDocument = true ;
                  break;
                }
              }
              if(isCanCreateDocument) break ;
            }
            if(isCanCreateDocument) break ;
          }
        }            
      }
      uiSelectBox.setOptions(options) ;
      if(hasDefaultDoc) {
        uiSelectBox.setValue(defaultDocument_);
      } else if(options.size() > 0) {
        defaultDocument_ = options.get(0).getValue() ;
        uiSelectBox.setValue(defaultDocument_);
      } 
    } catch(Exception e) {
//    e.printStackTrace() ;
    }
    return options ;
  }

  public void init() throws Exception {
    getChild(UIDocumentForm.class).setRepositoryName(repository_) ;
    getChild(UIDocumentForm.class).setTemplateNode(defaultDocument_) ;
    getChild(UIDocumentForm.class).setWorkspace(workspace_) ;
    getChild(UIDocumentForm.class).setStoredPath(currentNode_.getPath()) ;
    getChild(UIDocumentForm.class).resetProperties();
  }

  public void activate() throws Exception {}

  public void deActivate() throws Exception {}

}
