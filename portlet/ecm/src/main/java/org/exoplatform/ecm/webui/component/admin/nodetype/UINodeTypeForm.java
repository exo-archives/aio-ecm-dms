/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.nodetype;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.core.nodetype.NodeDefinitionValue;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeValue;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionValue;
import org.exoplatform.webui.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIComponent;
import org.exoplatform.webui.component.UIFormInputInfo;
import org.exoplatform.webui.component.UIFormSelectBox;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.UIFormTabPane;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.component.model.SelectItemOption;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Sep 20, 2006
 * 5:33:13 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/component/UIFormTabPane.gtmpl",
    events = {
      @EventConfig(listeners = UINodeTypeForm.CancelActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UINodeTypeOptionList.CancelTabActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UINodeTypeForm.SaveDraftActionListener.class),
      @EventConfig(listeners = UINodeTypeForm.SaveActionListener.class),
      @EventConfig(listeners = UIPropertyDefinitionForm.AddPropertyActionListener.class),
      @EventConfig(listeners = UIChildNodeDefinitionForm.AddChildActionListener.class),
      @EventConfig(listeners = UINodeTypeForm.ViewChildNodeActionListener.class),
      @EventConfig(listeners = UINodeTypeForm.ViewPropertyActionListener.class),
      @EventConfig(listeners = UINodeTypeForm.AddSuperTypeActionListener.class),
      @EventConfig(listeners = UIChildNodeDefinitionForm.AddDefaultTypeActionListener.class),
      @EventConfig(listeners = UIChildNodeDefinitionForm.AddRequiredTypeActionListener.class),
      @EventConfig(listeners = UINodeTypeOptionList.AddTypeActionListener.class),
      @EventConfig(listeners = UIChildNodeDefinitionForm.RemoveChildNodeActionListener.class),
      @EventConfig(listeners = UIChildNodeDefinitionForm.EditChildNodeActionListener.class),
      @EventConfig(listeners = UIPropertyDefinitionForm.EditPropertyActionListener.class),
      @EventConfig(listeners = UIPropertyDefinitionForm.RemovePropertyActionListener.class),
      @EventConfig(listeners = UIChildNodeDefinitionForm.UpdateChildActionListener.class),
      @EventConfig(listeners = UIChildNodeDefinitionForm.CancelChildActionListener.class),
      @EventConfig(listeners = UIPropertyDefinitionForm.UpdatePropertyActionListener.class),
      @EventConfig(listeners = UIPropertyDefinitionForm.CancelPropertyActionListener.class),
      @EventConfig(listeners = UIPropertyDefinitionForm.ChangeRequiredTypeActionListener.class),
      @EventConfig(listeners = UIPropertyDefinitionForm.AddConstraintsActionListener.class),
      @EventConfig(listeners = UIPropertyDefinitionForm.AddValueActionListener.class),
      @EventConfig(listeners = UIPropertyDefinitionForm.CancelConstraintsActionListener.class),
      @EventConfig(listeners = UIPropertyDefinitionForm.AddActionListener.class)
    }
)
public class UINodeTypeForm extends UIFormTabPane {

  final static public String NAMESPACE = "namespace" ;
  final static public String NODETYPE_NAME = "nodeTypeName" ;
  final static public String MIXIN_TYPE = "mixinType" ;
  final static public String HAS_ORDERABLE_CHILDNODES = "hasOrderableChildNodes" ;
  final static public String PRIMARY_ITEMNAME = "primaryItemName" ;
  final static public String PROPERTY_DEFINITIONS = "propertiesDefinitions" ;
  final static public String CHILDNODE_DEFINITIONS = "childDefinitions" ;
  final static public String SUPER_TYPE = "superTypes" ;
  final static public String SUPER_TYPE_TAB = "SuperTypeTab" ;
  final static public String DEFAULT_PRIMARY_TYPE_TAB = "DefaultTypeTab" ;
  final static public String REQUIRED_PRIMARY_TYPE_TAB = "RequiredTypeTab" ;
  final static public String NODETYPE_DEFINITION = "nodeTypeDefinition" ;
  final static public String PROPERTY_DEFINITION = "propertyDefinition" ;
  final static public String CHILDNODE_DEFINITION = "childNodeDefinition" ;
  final static public String NT_UNSTRUCTURED = "nt:unstructured" ;
  final static public String PROPERTY_DEFINITIONS_NODE = "propertyDefinitions" ;
  final static public String CHILD_NODE_DEFINITIONS = "childNodeDefinitions" ;
  final static public String IS_MIX_IN = "isMixin" ;
  final static public String PROPERTY_NAME = "name" ;
  final static public String ON_PARENT_VERSION = "onParentVersion" ;
  final static public String JCR_NODETYPE_DRAFT = "jcr:nodetypesDraft" ;
  final static public String TRUE = "true" ;
  final static public String FALSE = "false" ;
  final static public String ADD_CHILD = "AddChild" ;
  final static public String ADD_PROPERTY = "AddProperty" ;
  final static public String ACTION_SAVE = "Save" ;
  final static public String ACTION_SAVEDRAFT = "SaveDraft" ;
  final static public String ACTION_CANCEL = "Cancel" ;
  final static public String ACTION_CANCEL_TAB = "CancelTab" ;
  final static public String[] ACTION_BACK = {"Back"} ;
  final static public String ADD_TYPE = "AddType" ;

  private boolean isDraft_ = false ;

  public List<PropertyDefinitionValue> addedPropertiesDef_ = new ArrayList<PropertyDefinitionValue>();
  public List<NodeDefinitionValue> addedChildDef_ = new ArrayList<NodeDefinitionValue>();

  private List<SelectItemOption<String>> namespacesOptions_ = new ArrayList<SelectItemOption<String>>();
  private PropertyDefinition[] propertyDefinitions_ ;
  private NodeDefinition[] childNodeDefinitions_ ;
  private NodeType nodeType_ ;

  public UINodeTypeForm() throws Exception {
    super("UINodeTypeForm", false);
    List<SelectItemOption<String>> booleanItem = new ArrayList<SelectItemOption<String>>() ;
    booleanItem.add(new SelectItemOption<String>(FALSE, FALSE)) ;
    booleanItem.add(new SelectItemOption<String>(TRUE, TRUE)) ;

    UIFormInputSetWithAction nodeTypeTab = new UIFormInputSetWithAction(NODETYPE_DEFINITION) ;
    nodeTypeTab.addUIFormInput(new UIFormSelectBox(NAMESPACE,NAMESPACE, null)).
                addUIFormInput(new UIFormStringInput(NODETYPE_NAME,NODETYPE_NAME, null)).                                 
                addUIFormInput(new UIFormSelectBox(MIXIN_TYPE,MIXIN_TYPE, booleanItem)).
                addUIFormInput(new UIFormSelectBox(HAS_ORDERABLE_CHILDNODES,
                                                   HAS_ORDERABLE_CHILDNODES, booleanItem)).
                addUIFormInput(new UIFormStringInput(PRIMARY_ITEMNAME,PRIMARY_ITEMNAME, null)).
                addUIFormInput(new UIFormStringInput(SUPER_TYPE,SUPER_TYPE, null)).
                addUIFormInput(new UIFormInputInfo(PROPERTY_DEFINITIONS,PROPERTY_DEFINITIONS, null)).
                addUIFormInput(new UIFormInputInfo(CHILDNODE_DEFINITIONS,CHILDNODE_DEFINITIONS, null)) ; 
    setActionInTab(nodeTypeTab) ;     
    addUIComponentInput(nodeTypeTab) ;
    
    UIFormInputSetWithAction propertiesTab = new UIPropertyDefinitionForm(PROPERTY_DEFINITION) ;
    setActionInTab(propertiesTab) ;
    addUIComponentInput(propertiesTab) ;
    
    UIFormInputSetWithAction childTab = new UIChildNodeDefinitionForm(CHILDNODE_DEFINITION) ;
    setActionInTab(childTab) ;
    addUIComponentInput(childTab) ;   
  }
  
  public void setActionInTab(UIFormInputSetWithAction tab) {
    if(tab.getId().equals(NODETYPE_DEFINITION)) {
      tab.setActionInfo(SUPER_TYPE, new String[] {"AddSuperType"}) ;
      tab.setActions(new String[] {ACTION_SAVE, ACTION_SAVEDRAFT, ACTION_CANCEL}, null) ;
      tab.setRendered(true) ;
    } else if(tab.getId().equals(CHILDNODE_DEFINITION)) {
      tab.setRendered(false) ;
      tab.setActionInfo(UIChildNodeDefinitionForm.DEFAULT_PRIMARY_TYPE, 
                        new String[] {"AddDefaultType"}) ;
      tab.setActionInfo(UIChildNodeDefinitionForm.REQUIRED_PRIMARY_TYPE, 
                        new String[] {"AddRequiredType"}) ;
      tab.setActions(new String[] {ADD_CHILD}, null) ;
    } else if(tab.getId().equals(PROPERTY_DEFINITION)) {
      tab.setRendered(false) ;
      tab.setActionInfo(UIPropertyDefinitionForm.VALUE_CONSTRAINTS, new String[] {"AddConstraints"}) ;
      tab.setActions(new String[] {ADD_PROPERTY}, null) ;
    }
  }
  
  public String[] getActions() { return null ; }
  
  public String getLabel(ResourceBundle res, String id) {
    String label = getId() + ".label." + id ;
    try {
      return res.getString(label) ;
    } catch(Exception e) {
      return id ;
    }
  }  

  public List<SelectItemOption<String>> getNamespaces() throws Exception {
    if (namespacesOptions_.size() == 0) {
      RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
      String[] namespaces = repositoryService.getRepository().getNamespaceRegistry().getPrefixes() ;
      for(int i = 0; i < namespaces.length; i ++){
        namespacesOptions_.add(new SelectItemOption<String>(namespaces[i], namespaces[i])) ;
      }
    }  
    return namespacesOptions_ ;    
  }

  public void setChildValue(List<NodeDefinitionValue> listChildNode) throws Exception {
    StringBuilder childNodes = new StringBuilder() ;
    if(listChildNode == null) return ;
    for(NodeDefinitionValue node : listChildNode) {
      if(childNodes.length() > 0) childNodes.append(",") ;
      childNodes.append(node.getName()) ;
    }
    UIFormInputSetWithAction nodeTypeTab = getChildById(NODETYPE_DEFINITION) ;
    nodeTypeTab.setInfoField(CHILDNODE_DEFINITIONS, childNodes.toString()) ;
    String[] actionInfor = {"EditChildNode", "RemoveChildNode"} ;
    nodeTypeTab.setActionInfo(CHILDNODE_DEFINITIONS, actionInfor) ;
  }

  public void removeChildTabs(String[] tabNames) {
    for(int i = 0; i < tabNames.length ; i++) {
      removeChildById(tabNames[i]) ;
    }
  }

  public void setPropertyValue(List<PropertyDefinitionValue> listProperty) throws Exception {
    StringBuilder propertyValues = new StringBuilder() ;
    if(listProperty == null) return ;
    for(PropertyDefinitionValue property : listProperty) {
      if(propertyValues.length() > 0) propertyValues.append(",") ;
      propertyValues.append(property.getName()) ; 
    }
    UIFormInputSetWithAction nodeTypeTab = getChildById(NODETYPE_DEFINITION) ;
    nodeTypeTab.setInfoField(PROPERTY_DEFINITIONS, propertyValues.toString()) ;
    String[] actionInfor = {"EditProperty", "RemoveProperty"} ;
    nodeTypeTab.setActionInfo(PROPERTY_DEFINITIONS, actionInfor) ;
  }

  private void setChildDefinitions(NodeDefinition[] childDef) throws Exception {
    StringBuilder childDefinitions = new StringBuilder() ;
    if(childDef != null) {
      for(int i = 0 ; i < childDef.length ; i++) {
        if(childDefinitions.length() > 0) childDefinitions.append(",") ;
        childDefinitions.append(childDef[i].getName()) ;  
      }
    }
    UIFormInputSetWithAction nodeTypeTab = getChildById(NODETYPE_DEFINITION) ;
    nodeTypeTab.setInfoField(CHILDNODE_DEFINITIONS, childDefinitions.toString()) ;
    String[] actionInfor = {"ViewChildNode"} ;
    nodeTypeTab.setIsView(true) ;
    nodeTypeTab.setActionInfo(CHILDNODE_DEFINITIONS, actionInfor) ;
  }

  private void setPropertyDefinitions(PropertyDefinition[] propertyDef) throws Exception {
    StringBuilder propertyDefinitions = new StringBuilder() ;
    if(propertyDef != null) {
      for(int i = 0; i < propertyDef.length; i++) {
        if(propertyDefinitions.length() > 0) propertyDefinitions.append(",") ;
        propertyDefinitions.append(propertyDef[i].getName()) ;  
      }
    }
    UIFormInputSetWithAction nodeTypeTab = getChildById(NODETYPE_DEFINITION) ;
    nodeTypeTab.setInfoField(PROPERTY_DEFINITIONS, propertyDefinitions.toString()) ;
    String[] actionInfor = {"ViewProperty"} ;
    nodeTypeTab.setIsView(true) ;
    nodeTypeTab.setActionInfo(PROPERTY_DEFINITIONS, actionInfor) ;
  }

  public void update(NodeType nodeType,  boolean isView) throws Exception{
    namespacesOptions_.clear() ;
    if(nodeType == null) {
      refresh() ;
      setActionInTab((UIFormInputSetWithAction)getChildById(NODETYPE_DEFINITION)) ;
      setActionInTab((UIFormInputSetWithAction)getChildById(CHILDNODE_DEFINITION)) ;
      setActionInTab((UIFormInputSetWithAction)getChildById(PROPERTY_DEFINITION)) ;
      return ;
    }
    String fullName = nodeType.getName() ;
    String nodeTypeName = fullName ;
    String namespacesPrefix = "";
    if(fullName.indexOf(":") > -1) {
      nodeTypeName = fullName.substring(fullName.indexOf(":") + 1) ;
      namespacesPrefix = fullName.substring(0, fullName.indexOf(":")) ;
    }
    getUIStringInput(NODETYPE_NAME).setValue(nodeTypeName) ;
    getUIFormSelectBox(NAMESPACE).setOptions(getNamespaces()) ;
    getUIFormSelectBox(NAMESPACE).setValue(namespacesPrefix) ;

    getUIStringInput(PRIMARY_ITEMNAME).setValue(nodeType.getPrimaryItemName()) ;
    getUIFormSelectBox(MIXIN_TYPE).setValue(String.valueOf(nodeType.isMixin())) ;
    String orderableChildNodes = String.valueOf(nodeType.hasOrderableChildNodes()) ;
    getUIFormSelectBox(HAS_ORDERABLE_CHILDNODES).setValue(orderableChildNodes) ;      
    NodeType[] superType = nodeType.getSupertypes() ;
    StringBuilder types = new StringBuilder(); 
    for(int i = 0; i < superType.length; i++){
      if(types.length() > 0) types.append(", ") ;
      types.append(superType[i].getName()) ;
    }
    getUIStringInput(SUPER_TYPE).setValue(types.toString()) ;      
    if(isView) {
      getUIFormSelectBox(NAMESPACE).setDisabled(true) ;
      getUIStringInput(NODETYPE_NAME).setEditable(false) ;
      getUIStringInput(PRIMARY_ITEMNAME).setEditable(false) ;
      getUIFormSelectBox(MIXIN_TYPE).setDisabled(true) ;
      getUIFormSelectBox(HAS_ORDERABLE_CHILDNODES).setDisabled(true);
      getUIStringInput(SUPER_TYPE).setEditable(false) ;
      UIPropertyDefinitionForm uiPropertyForm = getChild(UIPropertyDefinitionForm.class) ;
      uiPropertyForm.update(nodeType, null) ;
      UIChildNodeDefinitionForm uiChildNodeForm = getChild(UIChildNodeDefinitionForm.class) ;
      uiChildNodeForm.update(nodeType, null) ;
      UIFormInputSetWithAction uiNodeTypeTab = getChildById(CHILDNODE_DEFINITION) ;
      uiNodeTypeTab.setIsView(true) ;
    }
    propertyDefinitions_ = nodeType.getPropertyDefinitions() ;
    childNodeDefinitions_ = nodeType.getChildNodeDefinitions() ;
    setChildDefinitions(childNodeDefinitions_) ;
    setPropertyDefinitions(propertyDefinitions_) ;
    nodeType_ = nodeType ;
  }
  
  public void setChildDraftNode(Node draftNodeType) throws Exception {
    addedChildDef_ = new ArrayList<NodeDefinitionValue>() ;
    if(!draftNodeType.hasNode(CHILD_NODE_DEFINITIONS)) return ;
    Node childNodeHome = draftNodeType.getNode(CHILD_NODE_DEFINITIONS) ; 
    NodeIterator childIter = childNodeHome.getNodes() ;
    while(childIter.hasNext()) {
      Node childDef = childIter.nextNode() ;
      NodeDefinitionValue nodeTypeValue = new NodeDefinitionValue();
      nodeTypeValue.setName(childDef.getName()) ;
      String defaultPriType = 
        childDef.getProperty(UIChildNodeDefinitionForm.DEFAULT_PRIMARY_TYPE).getString() ;
      nodeTypeValue.setDefaultNodeTypeName(defaultPriType) ;
      String sameName = childDef.getProperty(UIChildNodeDefinitionForm.SAME_NAME).getString() ;
      nodeTypeValue.setSameNameSiblings(Boolean.parseBoolean(sameName)) ;      
      String mandatory = childDef.getProperty(UIChildNodeDefinitionForm.MANDATORY).getString() ;
      nodeTypeValue.setMandatory(Boolean.parseBoolean(mandatory)) ;
      String autoCreate = childDef.getProperty(UIChildNodeDefinitionForm.AUTOCREATED).getString() ;
      nodeTypeValue.setAutoCreate(Boolean.parseBoolean(autoCreate)) ;
      String isProtected = childDef.getProperty(UIChildNodeDefinitionForm.PROTECTED).getString() ;
      nodeTypeValue.setReadOnly(Boolean.parseBoolean(isProtected)) ;
      String parentVer = childDef.getProperty(ON_PARENT_VERSION).getString() ;
      nodeTypeValue.setOnVersion(Integer.parseInt(parentVer)) ;
      String requiredTypes = 
        childDef.getProperty(UIChildNodeDefinitionForm.REQUIRED_PRIMARY_TYPE).getString() ;
      List<String> requiredList = new ArrayList<String>() ;
      if(requiredTypes != null && requiredTypes.length() > 0) {
        if(requiredTypes.indexOf(",") > -1){
          String[] array = requiredTypes.split(",") ;
          for(int i = 0; i < array.length; i ++) {
            requiredList.add(array[i].trim()) ;
          }
        } else {
          requiredList.add(requiredTypes) ;
        }
      }
      nodeTypeValue.setRequiredNodeTypeNames(requiredList) ;
      addedChildDef_.add(nodeTypeValue) ;
      setChildValue(addedChildDef_) ;
    }
  }
  
  public void setPropertyDraftNode(Node draftNodeType) throws Exception {
    addedPropertiesDef_ = new ArrayList<PropertyDefinitionValue>() ;
    if(!draftNodeType.hasNode(PROPERTY_DEFINITIONS_NODE)) return ;
    Node propertiesHome = draftNodeType.getNode(PROPERTY_DEFINITIONS_NODE) ;
    NodeIterator proIter = propertiesHome.getNodes() ;
    while (proIter.hasNext()) {
      Node proDef = proIter.nextNode() ;
      PropertyDefinitionValue propertyInfo = new PropertyDefinitionValue() ;
      propertyInfo.setName(proDef.getName()) ;          
      String requiredType = proDef.getProperty(UIPropertyDefinitionForm.REQUIRED_TYPE).getString() ;
      propertyInfo.setRequiredType(Integer.parseInt(requiredType)) ;
      String multiple = proDef.getProperty(UIPropertyDefinitionForm.MULTIPLE).getString() ;
      propertyInfo.setMultiple(Boolean.parseBoolean(multiple)) ;    
      String mandatory = proDef.getProperty(UIPropertyDefinitionForm.MANDATORY).getString() ;
      propertyInfo.setMandatory(Boolean.parseBoolean(mandatory)) ;          
      String autoCreate = proDef.getProperty(UIPropertyDefinitionForm.AUTOCREATED).getString() ;
      propertyInfo.setAutoCreate(Boolean.parseBoolean(autoCreate)) ;
      String isProtected = proDef.getProperty(UIPropertyDefinitionForm.PROTECTED).getString() ;
      propertyInfo.setReadOnly(Boolean.parseBoolean(isProtected)) ;
      String onParent = proDef.getProperty(ON_PARENT_VERSION).getString() ;
      propertyInfo.setOnVersion(Integer.parseInt(onParent)) ; 
      String constraints = proDef.getProperty(UIPropertyDefinitionForm.CONSTRAINTS).getString() ;
      List<String> valueConst = new ArrayList<String>() ;
      if(constraints != null && constraints.length() > 0) {
        if(constraints.indexOf(",") > -1) {
          String[] array = constraints.split(",") ;
          for(int i = 0; i < array.length; i ++) {
            valueConst.add(array[i].trim()) ;
          }
        } else {
          valueConst.add(constraints) ;
        }
      }
      propertyInfo.setValueConstraints(valueConst) ;
      addedPropertiesDef_.add(propertyInfo) ;
      setPropertyValue(addedPropertiesDef_) ;
    }
  }

  public void updateEdit(Node draftNodeType, boolean isDraft) throws Exception{  
    isDraft_ = isDraft ; 
    getUIFormSelectBox(NAMESPACE).setOptions(getNamespaces()) ;
    String name = draftNodeType.getName() ;
    String noteTypeName = name ;
    String namespacesPrefix = "";
    if(name.indexOf(":") > -1) {
      noteTypeName = name.substring(name.indexOf(":") + 1) ;
      namespacesPrefix = name.substring(0, name.indexOf(":")) ;
    }
    getUIStringInput(NODETYPE_NAME).setValue(noteTypeName) ;
    getUIFormSelectBox(NAMESPACE).setValue(namespacesPrefix) ;
    getUIStringInput(NODETYPE_NAME).setEditable(false) ;    
    if (draftNodeType.hasProperty(PRIMARY_ITEMNAME)) {
      String primaryItem = draftNodeType.getProperty(PRIMARY_ITEMNAME).getString() ;
      getUIStringInput(PRIMARY_ITEMNAME).setValue(primaryItem) ;
    }
    getUIFormSelectBox(MIXIN_TYPE).setValue(draftNodeType.getProperty(IS_MIX_IN).getString()) ;
    String hasOrderable = draftNodeType.getProperty(HAS_ORDERABLE_CHILDNODES).getString() ;
    getUIFormSelectBox(HAS_ORDERABLE_CHILDNODES).setValue(hasOrderable) ;      
    if (draftNodeType.hasProperty(SUPER_TYPE)) {
      getUIStringInput(SUPER_TYPE).setValue(draftNodeType.getProperty(SUPER_TYPE).getString()) ;
    }
    getUIFormSelectBox(NAMESPACE).setDisabled(true) ;
    getUIStringInput(NODETYPE_NAME).setEditable(false) ;
    getUIStringInput(PRIMARY_ITEMNAME).setEditable(true) ;
    getUIFormSelectBox(MIXIN_TYPE).setDisabled(false) ;
    getUIFormSelectBox(HAS_ORDERABLE_CHILDNODES).setDisabled(false) ;
    getUIStringInput(SUPER_TYPE).setEditable(true) ;
    setChildDraftNode(draftNodeType) ;
    setPropertyDraftNode(draftNodeType) ;

    setActionInTab((UIFormInputSetWithAction)getChildById(NODETYPE_DEFINITION)) ;
    setActionInTab((UIFormInputSetWithAction)getChildById(CHILDNODE_DEFINITION)) ;
    setActionInTab((UIFormInputSetWithAction)getChildById(PROPERTY_DEFINITION)) ;
  }

  public void refresh() throws Exception{    
    List<SelectItemOption<String>> booleanItem = new ArrayList<SelectItemOption<String>>() ;
    booleanItem.add(new SelectItemOption<String>(FALSE, FALSE)) ;
    booleanItem.add(new SelectItemOption<String>(TRUE, TRUE)) ;
    UIChildNodeDefinitionForm uiChildNodeTab = getChild(UIChildNodeDefinitionForm.class) ;
    UIPropertyDefinitionForm uiPropertyTab = getChild(UIPropertyDefinitionForm.class) ;
    getUIFormSelectBox(NAMESPACE).setOptions(getNamespaces()).setDisabled(false) ;
    getUIStringInput(NODETYPE_NAME).setEditable(true).setValue(null) ;
    getUIStringInput(PRIMARY_ITEMNAME).setEditable(true).setValue(null) ;
    getUIStringInput(SUPER_TYPE).setEditable(true).setValue(null) ;
    getUIFormSelectBox(MIXIN_TYPE).setOptions(booleanItem).setDisabled(false) ;
    getUIFormSelectBox(HAS_ORDERABLE_CHILDNODES).setOptions(booleanItem).setDisabled(false) ;
    getUIFormInputInfo(PROPERTY_DEFINITIONS).setValue(null) ;
    getUIFormInputInfo(CHILDNODE_DEFINITIONS).setValue(null) ;
    addedPropertiesDef_ = new ArrayList<PropertyDefinitionValue>();
    addedChildDef_ = new ArrayList<NodeDefinitionValue>();
    UIFormInputSetWithAction uiNodeTypeTab = getChildById(NODETYPE_DEFINITION) ;
    uiNodeTypeTab.setRendered(true) ;
    uiNodeTypeTab.setIsView(false) ;
    setActionInTab(uiNodeTypeTab) ;
    setChildDefinitions(null) ;
    setPropertyDefinitions(null) ;
    setChildValue(null) ;
    setPropertyValue(null) ;
    uiChildNodeTab.refresh() ;
    uiPropertyTab.refresh() ;
  }

  public void setTabRender(String tabName) {
    for(UIComponent uiComp : getChildren()) {
      UIFormInputSetWithAction tab = getChildById(uiComp.getId()) ;
      if(tab.getId().equals(tabName)) tab.setRendered(true) ;
      else tab.setRendered(false) ;
    }
  }

  static public class CancelActionListener extends EventListener<UINodeTypeForm> {
    public void execute(Event<UINodeTypeForm> event) throws Exception {
      UINodeTypeForm uiForm = event.getSource() ;
      uiForm.update(null, false) ;
      uiForm.refresh() ;
      UIPopupWindow uiPopup = uiForm.getParent() ;
      uiPopup.setRendered(false) ;
      uiPopup.setShow(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }

  static public class ViewPropertyActionListener extends EventListener<UINodeTypeForm> {
    public void execute(Event<UINodeTypeForm> event) throws Exception {
      UINodeTypeForm uiForm = event.getSource() ;
      String propertyName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIPropertyDefinitionForm uiPropertyForm = uiForm.getChild(UIPropertyDefinitionForm.class) ;
      uiPropertyForm.update(uiForm.nodeType_, propertyName) ;
      uiForm.setTabRender(PROPERTY_DEFINITION) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }

  static public class ViewChildNodeActionListener extends EventListener<UINodeTypeForm> {
    public void execute(Event<UINodeTypeForm> event) throws Exception {
      UINodeTypeForm uiForm = event.getSource() ;
      String childNodeName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIChildNodeDefinitionForm uiChildNodeForm = uiForm.getChild(UIChildNodeDefinitionForm.class) ;
      uiChildNodeForm.update(uiForm.nodeType_, childNodeName) ;
      uiForm.setTabRender(CHILDNODE_DEFINITION) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }

  static public class SaveActionListener extends EventListener<UINodeTypeForm> {
    public void execute(Event<UINodeTypeForm> event) throws Exception {
      UINodeTypeForm uiForm = event.getSource() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      RepositoryService repositoryService = uiForm.getApplicationComponent(RepositoryService.class) ;
      NodeTypeManager ntManager = repositoryService.getRepository().getNodeTypeManager() ;
      String prefix = uiForm.getUIFormSelectBox(NAMESPACE).getValue() ;
      String nodeTypeName = uiForm.getUIStringInput(NODETYPE_NAME).getValue() ;
      if(nodeTypeName == null || nodeTypeName.length() == 0) {
        uiApp.addMessage(new ApplicationMessage("UINodeTypeForm.msg.nodeType-name", null)) ;
        uiForm.setTabRender(NODETYPE_DEFINITION) ;
        return ;
      }
      if(prefix != null && prefix.length() > 0 ) nodeTypeName = prefix + ":" + nodeTypeName ;
      String superTypes = uiForm.getUIStringInput(SUPER_TYPE).getValue() ;
      if(uiForm.getUIFormSelectBox(MIXIN_TYPE).getValue().equals("false")) {
        if(superTypes == null || superTypes.length() == 0) {
          uiApp.addMessage(new ApplicationMessage("UINodeTypeForm.msg.supertype-is-madatory", null)) ;
          uiForm.setTabRender(NODETYPE_DEFINITION) ;
          return ;
        }
      }
      List<String> listTypes = new ArrayList<String>() ;
      if(superTypes != null) {
        if(superTypes.indexOf(",") > -1) {
          String[] arrayTypes = superTypes.split(",") ;
          for(String type :arrayTypes ) {
            listTypes.add(type.trim()) ;
          }  
        }
      }
                       
      String primaryItemName = uiForm.getUIStringInput(PRIMARY_ITEMNAME).getValue() ;
      NodeTypeValue newNodeType = new NodeTypeValue() ;                             
      newNodeType.setName(nodeTypeName) ;            
      newNodeType.setPrimaryItemName(primaryItemName) ;      
      newNodeType.setMixin(Boolean.parseBoolean(uiForm.getUIFormSelectBox(MIXIN_TYPE).getValue())) ;      
      String orderableChild = uiForm.getUIFormSelectBox(HAS_ORDERABLE_CHILDNODES).getValue() ;
      newNodeType.setOrderableChild(Boolean.parseBoolean(orderableChild)) ;      
      newNodeType.setDeclaredSupertypeNames(listTypes) ;      
      newNodeType.setDeclaredPropertyDefinitionValues(uiForm.addedPropertiesDef_) ;
      newNodeType.setDeclaredChildNodeDefinitionValues(uiForm.addedChildDef_) ;
      ExtendedNodeTypeManager extNTManager = (ExtendedNodeTypeManager)ntManager ;
      try {        
        extNTManager.registerNodeType(newNodeType, ExtendedNodeTypeManager.FAIL_IF_EXISTS) ;
      } catch(Exception e) {
        uiApp.addMessage(new ApplicationMessage("UINodeTypeForm.msg.register-failed", null)) ;
        uiForm.setTabRender(NODETYPE_DEFINITION) ;
        return ;
      }      
      UINodeTypeManager uiManager = uiForm.getAncestorOfType(UINodeTypeManager.class) ;
      UINodeTypeList nodeTypeList = uiManager.getChild(UINodeTypeList.class) ;
      if(uiForm.isDraft_){
        uiForm.isDraft_ = false ;
        nodeTypeList.refresh(nodeTypeName) ;
      } else {
        nodeTypeList.refresh(null) ; 
      }
      uiForm.refresh() ;
      uiForm.setTabRender(NODETYPE_DEFINITION) ;
      UIPopupWindow uiPopup = uiForm.getParent() ;
      uiPopup.setRendered(false) ;
      Object[] args = { newNodeType.getName() } ;
      uiApp.addMessage(new ApplicationMessage("UINodeTypeForm.msg.register-successfully", args)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }

  static public class SaveDraftActionListener extends EventListener<UINodeTypeForm> {
    public void execute(Event<UINodeTypeForm> event) throws Exception {
      UINodeTypeForm uiForm = event.getSource() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      RepositoryService repositoryService = uiForm.getApplicationComponent(RepositoryService.class) ;
      CmsConfigurationService cmsConfigService = 
        uiForm.getApplicationComponent(CmsConfigurationService.class) ;
      Session session = 
        repositoryService.getRepository().getSystemSession(cmsConfigService.getWorkspace()) ;
      String prefix = uiForm.getUIFormSelectBox(NAMESPACE).getValue() ;
      String nodeTypeName = uiForm.getUIStringInput(NODETYPE_NAME).getValue() ;
      if(nodeTypeName == null || nodeTypeName.length() == 0) {
        uiApp.addMessage(new ApplicationMessage("UINodeTypeForm.msg.nodeType-name", null)) ;
        uiForm.setTabRender(NODETYPE_DEFINITION) ;
        return ;
      }
      if(prefix != null && prefix.length() > 0 ) nodeTypeName = prefix + ":" + nodeTypeName ;
      Node systemNode = (Node)session.getItem("/jcr:system") ;
      Node nodeTypeDraft ;
      if(systemNode.hasNode(JCR_NODETYPE_DRAFT)) {
        nodeTypeDraft = systemNode.getNode(JCR_NODETYPE_DRAFT) ;
      } else {
        nodeTypeDraft = systemNode.addNode(JCR_NODETYPE_DRAFT, NT_UNSTRUCTURED) ;
      }
      systemNode.save() ;        
      Node newNode ;
      if(nodeTypeDraft.hasNode(nodeTypeName)) newNode = nodeTypeDraft.getNode(nodeTypeName) ;
      else newNode = nodeTypeDraft.addNode(nodeTypeName, NT_UNSTRUCTURED) ;
      nodeTypeDraft.save() ;
      newNode.setProperty(PRIMARY_ITEMNAME, uiForm.getUIStringInput(PRIMARY_ITEMNAME).getValue()) ;
      newNode.setProperty(IS_MIX_IN, uiForm.getUIFormSelectBox(MIXIN_TYPE).getValue()) ;
      String hasOrderable = uiForm.getUIFormSelectBox(HAS_ORDERABLE_CHILDNODES).getValue() ;
      newNode.setProperty(HAS_ORDERABLE_CHILDNODES, hasOrderable) ;      
      String superTypes = uiForm.getUIStringInput(SUPER_TYPE).getValue() ;
      newNode.setProperty(SUPER_TYPE, superTypes) ;
      newNode.save() ;
      nodeTypeDraft.save() ;
      if(uiForm.addedPropertiesDef_ .size() > 0) {
        Node propertyDef ;
        if(newNode.hasNode(PROPERTY_DEFINITIONS_NODE)) { 
          propertyDef = newNode.getNode(PROPERTY_DEFINITIONS_NODE) ;          
        } else {
          propertyDef = newNode.addNode(PROPERTY_DEFINITIONS_NODE, NT_UNSTRUCTURED) ;
        }
        newNode.save() ;
        propertyDef.save() ;
        for(int i = 0; i < uiForm.addedPropertiesDef_.size(); i ++) {
          PropertyDefinitionValue property = uiForm.addedPropertiesDef_.get(i) ;
          Node propertyNode ;
          if(propertyDef.hasNode(property.getName())) {
            propertyNode = propertyDef.getNode(property.getName()) ;            
          } else {
            propertyNode = propertyDef.addNode(property.getName(), NT_UNSTRUCTURED) ;
            propertyNode.setProperty(PROPERTY_NAME, property.getName()) ;
          }
          String requiredType = String.valueOf(property.getRequiredType()) ;
          propertyNode.setProperty(UIPropertyDefinitionForm.REQUIRED_TYPE, requiredType) ;
          String autoCreate = String.valueOf(property.isAutoCreate()) ;
          propertyNode.setProperty(UIPropertyDefinitionForm.AUTOCREATED, autoCreate) ;
          String isProtected = String.valueOf(property.isReadOnly()) ;
          propertyNode.setProperty(UIPropertyDefinitionForm.PROTECTED, isProtected) ;
          String mandatory = String.valueOf(property.isMandatory()) ;
          propertyNode.setProperty(UIPropertyDefinitionForm.MANDATORY, mandatory) ;
          propertyNode.setProperty(ON_PARENT_VERSION, String.valueOf(property.getOnVersion())) ;
          String multiple = String.valueOf(property.isMultiple()) ;
          propertyNode.setProperty(UIPropertyDefinitionForm.MULTIPLE, multiple) ;
          List valueConstraint = property.getValueConstraints() ; 
          StringBuilder values = new StringBuilder() ;
          for(int j = 0; j < valueConstraint.size(); j ++) {
            if(values.length() > 0) values.append(", ") ;
            values.append(valueConstraint.get(j).toString()) ;
          }
          propertyNode.setProperty(UIPropertyDefinitionForm.CONSTRAINTS, values.toString()) ;          
          propertyDef.save() ;
          propertyNode.save() ;
        }
      }
      if(uiForm.addedChildDef_.size() > 0) {
        Node childDef ; 
        if(newNode.hasNode(CHILD_NODE_DEFINITIONS)) { 
          childDef = newNode.getNode(CHILD_NODE_DEFINITIONS) ;          
        } else {
          childDef = newNode.addNode(CHILD_NODE_DEFINITIONS, NT_UNSTRUCTURED) ;
        }
        newNode.save() ;
        childDef.save() ;
        for(int i = 0; i < uiForm.addedChildDef_.size(); i ++ ){
          NodeDefinitionValue nodeDef =  uiForm.addedChildDef_.get(i) ;
          Node childNode ;
          if(childDef.hasNode(nodeDef.getName())) {
            childNode = childDef.getNode(nodeDef.getName()) ;
          } else {
            childNode = childDef.addNode(nodeDef.getName(), NT_UNSTRUCTURED) ;
            childNode.setProperty(PROPERTY_NAME, nodeDef.getName()) ;
          }
          String defaultPrimaryType = nodeDef.getDefaultNodeTypeName() ;
          childNode.setProperty(UIChildNodeDefinitionForm.DEFAULT_PRIMARY_TYPE, defaultPrimaryType) ;
          String autoCreate = String.valueOf(nodeDef.isAutoCreate()) ;
          childNode.setProperty(UIChildNodeDefinitionForm.AUTOCREATED, autoCreate) ; 
          String isProtected = String.valueOf(nodeDef.isReadOnly()) ;
          childNode.setProperty(UIChildNodeDefinitionForm.PROTECTED, isProtected) ; 
          String mandatory = String.valueOf(nodeDef.isMandatory()) ;
          childNode.setProperty(UIChildNodeDefinitionForm.MANDATORY, mandatory) ;
          childNode.setProperty(ON_PARENT_VERSION, String.valueOf(nodeDef.getOnVersion())) ;
          String sameName = String.valueOf(nodeDef.isSameNameSiblings()) ;
          childNode.setProperty(UIChildNodeDefinitionForm.SAME_NAME, sameName) ;
          if(nodeDef.getRequiredNodeTypeNames().size() > 0 ) {
            StringBuilder requiredTypes = new StringBuilder("") ;
            for(int j = 0; j < nodeDef.getRequiredNodeTypeNames().size(); j ++) {
              if( requiredTypes.length() > 0) requiredTypes.append(",") ;
              requiredTypes.append(nodeDef.getRequiredNodeTypeNames().get(j).toString()) ;
            }
            childNode.setProperty(UIChildNodeDefinitionForm.REQUIRED_PRIMARY_TYPE, 
                requiredTypes.toString()) ;
          } else {
            childNode.setProperty(UIChildNodeDefinitionForm.REQUIRED_PRIMARY_TYPE, "") ;
          }
          childDef.save() ;
          childNode.save() ;
        }
      }
      session.save() ;
      UINodeTypeManager uiManager = uiForm.getAncestorOfType(UINodeTypeManager.class) ;
      UINodeTypeList nodeTypeList = uiManager.getChild(UINodeTypeList.class) ;
      nodeTypeList.refresh(null);
      uiForm.refresh() ;
      uiForm.setTabRender(NODETYPE_DEFINITION) ;
      UIPopupWindow uiPopup = uiForm.getParent() ;
      uiPopup.setRendered(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }

  static  public class AddSuperTypeActionListener extends EventListener<UINodeTypeForm> {
    public void execute(Event<UINodeTypeForm> event) throws Exception {
      UINodeTypeForm uiForm = event.getSource() ;
      UIFormInputSetWithAction superTypeTab = new UINodeTypeOptionList(SUPER_TYPE_TAB) ;
      uiForm.removeChildTabs(new String[] {DEFAULT_PRIMARY_TYPE_TAB, REQUIRED_PRIMARY_TYPE_TAB}) ;
      if(superTypeTab.isRendered()) uiForm.removeChildById(SUPER_TYPE_TAB) ;
      String[] actionNames = new String[] {ADD_TYPE, ACTION_CANCEL_TAB} ;
      String[] fieldNames = new String[] {SUPER_TYPE, SUPER_TYPE_TAB} ;
      superTypeTab.setActions(actionNames, fieldNames) ;
      uiForm.addUIComponentInput(superTypeTab) ;
      uiForm.setTabRender(SUPER_TYPE_TAB) ;
      UINodeTypeOptionList uiOptionList = uiForm.getChild(UINodeTypeOptionList.class) ;
      String superTypeValue = uiForm.getUIStringInput(SUPER_TYPE).getValue() ;
      uiOptionList.update(superTypeValue) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }
}
