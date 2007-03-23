/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.nodetype;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.download.DownloadResource;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormCheckBoxInput;
import org.exoplatform.webui.component.UIFormInputInfo;
import org.exoplatform.webui.component.UIFormInputSet;
import org.exoplatform.webui.component.UIFormTableInputSet;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Sep 29, 2006
 * 12:01:58 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/webui/component/UIFormWithOutTitle.gtmpl",
    events = {
      @EventConfig(listeners = UINodeTypeExport.ExportActionListener.class),
      @EventConfig(phase=Phase.DECODE, listeners = UINodeTypeExport.CancelActionListener.class)
    }
)
public class UINodeTypeExport extends UIForm {

  final static String TABLE_NAME =  "UINodeTypeExport" ; 
  final static String LABEL = "label" ;
  final static String INPUT = "input" ;
  final static String[] TABLE_COLUMNS = {LABEL, INPUT} ;

  private List<NodeType> nodeTypeList_ = new ArrayList<NodeType>() ;

  public UINodeTypeExport() throws Exception {
  }

  public void update() throws Exception {
    UIFormTableInputSet uiTableInputSet = createUIComponent(UIFormTableInputSet.class, null, null) ; 
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    NodeTypeManager ntManager = repositoryService.getRepository().getNodeTypeManager() ;
    NodeTypeIterator nodeTypeIter = ntManager.getAllNodeTypes() ;
    UIFormInputSet uiInputSet ;
    uiTableInputSet.setName(TABLE_NAME);
    uiTableInputSet.setColumns(TABLE_COLUMNS);
    while(nodeTypeIter.hasNext()){
      NodeType nt = nodeTypeIter.nextNodeType() ;
      nodeTypeList_.add(nt) ;
      String ntName = nt.getName() ;
      uiInputSet = new UIFormInputSet(ntName) ;
      UIFormInputInfo uiInfo = new UIFormInputInfo(LABEL, null, ntName);
      uiInputSet.addChild(uiInfo);
      UIFormCheckBoxInput<String> uiCheckbox = new UIFormCheckBoxInput<String>(ntName, ntName, null);
      uiCheckbox.setChecked(true); 
      uiCheckbox.setValue(ntName);
      uiInputSet.addChild(uiCheckbox);
      uiTableInputSet.addChild(uiInputSet);      
    }
    addUIFormInput(uiTableInputSet) ;
  }

  public String getLabel(String id) { return id ; }

  public NodeType getNodeTypeByName(String nodeTypeName) throws Exception {
    NodeType nodeTypeSelected = null ;
    for(NodeType node : nodeTypeList_) {
      if(node.getName().equals(nodeTypeName)) {
        nodeTypeSelected = node ;
        break ;
      }
    }
    return nodeTypeSelected ;
  }

  static public class ExportActionListener extends EventListener<UINodeTypeExport> {
    public void execute(Event<UINodeTypeExport> event) throws Exception {
      UINodeTypeExport uiExport = event.getSource() ;
      UINodeTypeManager uiManager = uiExport.getAncestorOfType(UINodeTypeManager.class) ;
      List<NodeType> selectedNodes = new ArrayList<NodeType>() ;
      List<UIFormCheckBoxInput> listCheckbox =  new ArrayList<UIFormCheckBoxInput>();
      uiExport.findComponentOfType(listCheckbox, UIFormCheckBoxInput.class);
      for(int i = 0; i < listCheckbox.size(); i ++) {
        boolean checked = listCheckbox.get(i).isChecked() ;
        if(checked) selectedNodes.add(uiExport.getNodeTypeByName(listCheckbox.get(i).getName())) ;
      }
      String nodeTypeXML = getNodeTypeXML(selectedNodes) ;
      ByteArrayInputStream is = new ByteArrayInputStream(nodeTypeXML.getBytes()) ;
      DownloadResource dresource = new InputStreamDownloadResource(is, "text/xml") ;
      DownloadService dservice = uiExport.getApplicationComponent(DownloadService.class) ;
      dresource.setDownloadName("nodetype_export.xml");
      String downloadLink = dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
      event.getRequestContext().addJavascript("window.location=\"" + downloadLink + "\"") ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
    private String getNodeTypeXML(List selectedNodes) {
      StringBuilder nodeTypeXML = new StringBuilder() ;
      nodeTypeXML.append("<nodeTypes xmlns:nt=").append("\"") ;
      nodeTypeXML.append("http://www.jcp.org/jcr/nt/1.0").append("\" ") ;
      nodeTypeXML.append("xmlns:mix=").append("\"") ;
      nodeTypeXML.append("http://www.jcp.org/jcr/mix/1.0").append("\" ") ;
      nodeTypeXML.append("xmlns:jcr=").append("\"").append("http://www.jcp.org/jcr/1.0") ;
      nodeTypeXML.append("\" >").append("\n") ;
      for(int i = 0; i < selectedNodes.size(); i++) {
        NodeType nodeType = (NodeType)selectedNodes.get(i) ;
        nodeTypeXML.append("<nodeType ") ;
        nodeTypeXML.append("name=").append("\"").append(nodeType.getName()).append("\" ") ;
        String isMixIn = String.valueOf(nodeType.isMixin()) ;
        nodeTypeXML.append("isMixin=").append("\"").append(String.valueOf(isMixIn)).append("\" ") ;
        String hasOrderable = String.valueOf(nodeType.hasOrderableChildNodes()) ;
        nodeTypeXML.append("hasOrderableChildNodes=\"").append(hasOrderable).append("\" ") ;
        String primaryItemName = "";
        if(nodeType.getPrimaryItemName() != null) primaryItemName = nodeType.getPrimaryItemName() ;
        nodeTypeXML.append("primaryItemName=").append("\"").append(primaryItemName).append("\" >") ;
        nodeTypeXML.append("\n") ;
        // represent supertypes
        String representSuperType = representSuperTypes(nodeType) ;
        nodeTypeXML.append(representSuperType) ;
        // represent PropertiesDefinition
        String representPropertiesXML = representPropertyDefinition(nodeType) ; 
        nodeTypeXML.append(representPropertiesXML) ;
        // represent ChildNodeDefinition
        String representChildXML = representChildNodeDefinition(nodeType) ;
        nodeTypeXML.append(representChildXML) ;
        nodeTypeXML.append("</nodeType>").append("\n") ;
      }
      nodeTypeXML.append("</nodeTypes>") ;
      return nodeTypeXML.toString() ;
    }

    private String representSuperTypes(NodeType nodeType){
      StringBuilder superTypeXML = new StringBuilder() ;
      NodeType[] superType = nodeType.getDeclaredSupertypes() ;
      if(superType != null && superType.length > 0){
        superTypeXML.append("<supertypes>").append("\n") ;
        for(int i = 0 ; i < superType.length ; i ++){
          String typeName = superType[i].getName() ;
          superTypeXML.append("<supertype>").append(typeName).append("</supertype>").append("\n") ;
        }
        superTypeXML.append("</supertypes>").append("\n") ;
      }
      return superTypeXML.toString() ;
    }
    
    private String representPropertyDefinition(NodeType nodeType) {
      String[] requireType = {"undefined", "String", "Binary", "Long", "Double", "Date", "Boolean", 
                              "Name", "Path", "References"} ;
      String[] onparentVersion = {"", "COPY", "VERSION", "INITIALIZE", "COMPUTE", "IGNORE", "ABORT"} ;
      StringBuilder propertyXML = new StringBuilder() ;
      propertyXML.append("<propertyDefinitions>").append("\n") ;        
      PropertyDefinition[] proDef = nodeType.getPropertyDefinitions() ;
      for(int j = 0 ; j < proDef.length; j ++) {
        propertyXML.append("<propertyDefinition ") ;
        propertyXML.append("name=").append("\"").append(proDef[j].getName()).append("\" ") ;         
        String requiredValue = null ;
        if(proDef[j].getRequiredType() == 100) requiredValue = "Permission" ;
        else requiredValue = requireType[proDef[j].getRequiredType()] ;
        propertyXML.append("requiredType=").append("\"").append(requiredValue).append("\" ") ;         
        String autoCreate = String.valueOf(proDef[j].isAutoCreated()) ; 
        propertyXML.append("autoCreated=").append("\"").append(autoCreate).append("\" ") ;         
        String mandatory = String.valueOf(proDef[j].isMandatory()) ; 
        propertyXML.append("mandatory=").append("\"").append(mandatory).append("\" ") ;         
        String onVersion = onparentVersion[proDef[j].getOnParentVersion()] ; 
        propertyXML.append("onParentVersion=").append("\"").append(onVersion).append("\" ") ;         
        String protect = String.valueOf(proDef[j].isProtected()) ; 
        propertyXML.append("protected=").append("\"").append(protect).append("\" ") ;
        String multiple = String.valueOf(proDef[j].isMultiple()) ; 
        propertyXML.append("multiple=").append("\"").append(multiple).append("\" >").append("\n") ;         
        String[] constraints = proDef[j].getValueConstraints() ;
        if(constraints != null && constraints.length > 0) {
          propertyXML.append("<valueConstraints>").append("\n") ;
          for(int k = 0; k < constraints.length ; k ++) {
            String cons = constraints[k].toString() ;
            propertyXML.append("<valueConstraint>").append(cons).append("</valueConstraint>") ;
            propertyXML.append("\n") ;
          }
          propertyXML.append("</valueConstraints>").append("\n") ;
        } else {
          propertyXML.append("<valueConstraints/>").append("\n") ;
        }
        propertyXML.append("</propertyDefinition>").append("\n") ;
      }
      propertyXML.append("</propertyDefinitions>").append("\n") ;
      return propertyXML.toString() ;
    }

    private String representChildNodeDefinition(NodeType nodeType) {
      String[] onparentVersion = {"","COPY","VERSION","INITIALIZE","COMPUTE","IGNORE","ABORT"} ;
      StringBuilder childNodeXML = new StringBuilder() ;
      NodeDefinition[] childDef = nodeType.getChildNodeDefinitions() ;
      if(childDef != null && childDef.length > 0) {
        childNodeXML.append("<childNodeDefinitions>").append("\n") ;
        for(int j = 0 ; j < childDef.length ; j ++){
          childNodeXML.append("<childNodeDefinition ") ;
          childNodeXML.append("name=").append("\"").append(childDef[j].getName()).append("\" ") ;
          NodeType defaultType = childDef[j].getDefaultPrimaryType() ;
          if(defaultType != null) {
            String defaultName = defaultType.getName() ;
            childNodeXML.append("defaultPrimaryType=").append("\"").append(defaultName).append("\" ") ;
          } else {
            childNodeXML.append("defaultPrimaryType=").append("\"").append("\" ") ;
          }               
          String autoCreate = String.valueOf(childDef[j].isAutoCreated()) ; 
          childNodeXML.append("autoCreated=").append("\"").append(autoCreate).append("\" ") ;
          String mandatory = String.valueOf(childDef[j].isMandatory()) ; 
          childNodeXML.append("mandatory=").append("\"").append(mandatory).append("\" ") ;
          String onVersion = onparentVersion[childDef[j].getOnParentVersion()] ; 
          childNodeXML.append("onParentVersion=").append("\"").append(onVersion).append("\" ") ;
          String protect = String.valueOf(childDef[j].isProtected()) ; 
          childNodeXML.append("protected=").append("\"").append(protect).append("\" ") ;
          String sameName = String.valueOf(childDef[j].allowsSameNameSiblings()) ; 
          childNodeXML.append("sameNameSiblings=").append("\"").append(sameName).append("\" >") ;
          childNodeXML.append("\n") ;
          NodeType[] requiredType = childDef[j].getRequiredPrimaryTypes() ;
          if(requiredType != null && requiredType.length > 0 ) {
            childNodeXML.append("<requiredPrimaryTypes>").append("\n") ;
            for(int k = 0 ; k < requiredType.length ; k ++) {
              String requiredName = requiredType[k].getName() ;
              childNodeXML.append("<requiredPrimaryType>").append(requiredName) ;
              childNodeXML.append("</requiredPrimaryType>").append("\n") ;
            }
            childNodeXML.append("</requiredPrimaryTypes>").append("\n") ;
          }
          childNodeXML.append("</childNodeDefinition>").append("\n") ; 
        }          
        childNodeXML.append("</childNodeDefinitions>").append("\n") ;
      }
      return childNodeXML.toString() ;
    }
  }
  
  static public class CancelActionListener extends EventListener<UINodeTypeExport> {
    public void execute(Event<UINodeTypeExport> event) throws Exception {
      UINodeTypeExport uiExport = event.getSource() ;
      UINodeTypeManager uiManager = uiExport.getAncestorOfType(UINodeTypeManager.class) ;
      UIPopupWindow uiPopup = uiManager.findComponentById(UINodeTypeManager.EXPORT_POPUP) ;
      uiPopup.setRendered(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
}
