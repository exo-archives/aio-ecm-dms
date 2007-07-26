/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorerPortlet;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
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

  public UIDocumentFormController() throws Exception {
    addChild(UISelectDocumentForm.class, null, null) ;
    UIDocumentForm uiDocumentForm = createUIComponent(UIDocumentForm.class, null, null) ;
    uiDocumentForm.setTemplateNode(DEFAULT_VALUE) ;
    uiDocumentForm.addNew(true) ;
    addChild(uiDocumentForm) ;    
  }

  public void activate() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    UISelectDocumentForm uiSelectForm = getChild(UISelectDocumentForm.class) ;
    UIFormSelectBox uiSelectBox = uiSelectForm.getUIFormSelectBox(UISelectDocumentForm.FIELD_SELECT) ;
    boolean hasDefaultDoc = false ;
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    String repository = getAncestorOfType(UIJCRExplorerPortlet.class).getPreferenceRepository() ;
    Node currentNode = uiExplorer.getCurrentNode() ;
    NodeTypeManager ntManager = currentNode.getSession().getWorkspace().getNodeTypeManager() ; 
    NodeType currentNodeType = currentNode.getPrimaryNodeType() ; 
    NodeDefinition[] childDefs = currentNodeType.getChildNodeDefinitions() ;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    List templates = templateService.getDocumentTemplates(repository) ;
    try {
      for(int i = 0; i < templates.size(); i ++){
        String nodeTypeName = templates.get(i).toString() ; 
        NodeType nodeType = ntManager.getNodeType(nodeTypeName) ;
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
            String label = templateService.getTemplateLabel(nodeTypeName, repository) ;
            options.add(new SelectItemOption<String>(label, nodeTypeName));          
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
                  String label = templateService.getTemplateLabel(nodeTypeName, repository) ;
                  options.add(new SelectItemOption<String>(label, nodeTypeName));                
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
      e.printStackTrace() ;
    }
    getChild(UIDocumentForm.class).setTemplateNode(defaultDocument_) ;
    getChild(UIDocumentForm.class).setWorkspace(uiExplorer.getCurrentWorkspace()) ;
    getChild(UIDocumentForm.class).setStoredPath(currentNode.getPath()) ;
    getChild(UIDocumentForm.class).resetProperties();
  }

  public void deActivate() throws Exception {}

}
