/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;

import org.exoplatform.ecm.jcr.UISelector;
import org.exoplatform.ecm.jcr.model.VersionNode;
import org.exoplatform.ecm.webui.component.UIFormInputSetWithAction;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.services.cms.views.impl.ViewDataImpl.Tab;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.component.UIFormCheckBoxInput;
import org.exoplatform.webui.component.UIFormInputInfo;
import org.exoplatform.webui.component.UIFormSelectBox;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.model.SelectItemOption;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@yahoo.com
 * Jun 28, 2006
 */
@ComponentConfig(template = "app:/groovy/webui/component/admin/UIFormInputSetWithAction.gtmpl")
public class UIViewForm extends UIFormInputSetWithAction implements UISelector {
  
  final static public String MIXVERSION = "mix:versionable" ;
  final static public String FIELD_VERSION = "version" ;
  final static public String FIELD_NAME = "viewName" ;
  final static public String FIELD_PERMISSION = "permission" ;
  final static public String FIELD_TABS = "tabs" ;
  final static public String FIELD_TEMPLATE = "template" ;
  final static public String FIELD_ENABLEVERSION = "enableVersion" ;

  private boolean isView_ = true ;
  private Node views_;
  private HashMap<String, Tab> tabMap_ = new HashMap<String, Tab>() ;  
  private ManageViewService vservice_ = null ;
  private List<String> listVersion = new ArrayList<String>() ;
  private Version baseVersion_;
  private VersionNode selectedVersion_;

  public UIViewForm(String name) throws Exception {
    super(name) ;
    setComponentConfig(getClass(), null) ;
    UIFormSelectBox versions = new UIFormSelectBox(FIELD_VERSION , FIELD_VERSION, null) ;
    versions.setOnChange("ChangeVersion") ;
    versions.setRendered(false) ;
    addUIFormInput(versions) ;
    addUIFormInput(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null)) ;
    addUIFormInput(new UIFormStringInput(FIELD_PERMISSION, FIELD_PERMISSION, null)) ;
    addUIFormInput(new UIFormInputInfo(FIELD_TABS, FIELD_TABS, null)) ;
    setActionInfo(FIELD_PERMISSION, new String[] {"AddPermission"}) ;
    vservice_ = getApplicationComponent(ManageViewService.class) ;
    Node ecmTemplateHome = vservice_.getTemplateHome(BasePath.ECM_EXPLORER_TEMPLATES) ;
    List<SelectItemOption<String>> temp = new ArrayList<SelectItemOption<String>>() ; 
    NodeIterator iter = ecmTemplateHome.getNodes() ;
    while(iter.hasNext()) {
      Node tempNode = iter.nextNode() ;
      temp.add(new SelectItemOption<String>(tempNode.getName(),tempNode.getPath())) ;
    }
    addUIFormInput(new UIFormSelectBox(FIELD_TEMPLATE,FIELD_TEMPLATE, temp)) ;
    UIFormCheckBoxInput enableVersion = 
      new UIFormCheckBoxInput<Boolean>(FIELD_ENABLEVERSION, FIELD_ENABLEVERSION, null) ;
    enableVersion.setRendered(false) ;
    addUIFormInput(enableVersion) ;
    setActions(new String[]{"Save", "Reset", "Cancel"}, null) ;
  }
  
  public void processRender(WebuiRequestContext context) throws Exception {
    super.processRender(context) ;
  }
  
  @SuppressWarnings("unused")
  public void updateSelect(String selectField, String value) {
    getUIStringInput(UIViewForm.FIELD_PERMISSION).setValue(value) ;
    UIViewContainer uiContainer = getAncestorOfType(UIViewContainer.class) ;
    uiContainer.removeChildById(UIViewFormTabPane.POPUP_PERMISSION) ;
  }
  
  public boolean isView() { return isView_ ; }
  public Node getViews() { return views_; }
  
  public boolean canEnableVersionning(Node node) throws Exception {
    return node.canAddMixin("mix:versionable");
  }

  private boolean isVersioned(Node node) throws RepositoryException {          
    return node.isNodeType("mix:versionable");    
  }

  private VersionNode getRootVersion(Node node) throws Exception{       
    VersionHistory vH = node.getVersionHistory() ;
    if(vH != null) return new VersionNode(vH.getRootVersion()) ;
    return null ;
  }

  private List<String> getNodeVersions(List<VersionNode> children) throws Exception {         
    List<VersionNode> child = new ArrayList<VersionNode>() ;
    for(VersionNode vNode : children){
      listVersion.add(vNode.getName());
      child = vNode.getChildren() ;
      if (!child.isEmpty()) getNodeVersions(child) ; 
    }           
    return listVersion ;
  }

  private List<SelectItemOption<String>> getVersionValues(Node node) throws Exception { 
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    List<VersionNode> children = getRootVersion(node).getChildren() ;
    listVersion.clear() ;
    List<String> versionList = getNodeVersions(children) ;
    for(int i = 0; i < versionList.size(); i++) {
      for(int j = i + 1; j < versionList.size(); j ++) {
        if( Integer.parseInt(versionList.get(j)) < Integer.parseInt(versionList.get(i))) {
          String temp = versionList.get(i) ;
          versionList.set(i, versionList.get(j))  ;
          versionList.set(j, temp) ;
        }
      }
      options.add(new SelectItemOption<String>(versionList.get(i), versionList.get(i))) ;
    }
    return options ;
  }
  
  public void addTab(String tabName, String buttons){
    Tab tab = new Tab() ;
    tab.setTabName(tabName) ;
    tab.setButtons(buttons) ;
    tabMap_.put(tabName, tab) ;
  }

  private String getTabList() throws Exception {
    StringBuilder result = new StringBuilder() ;
    List<Tab> tabList = new ArrayList<Tab>(tabMap_.values());
    if(result != null) {
      for(Tab tab : tabList) {
        if(result.length() > 0) result.append(",") ;
        result.append(tab.getTabName()) ;
      }
    }
    return result.toString() ;
  }
  
  public void refresh(boolean isAddNew) throws Exception {
    getUIFormSelectBox(FIELD_VERSION).setRendered(!isAddNew) ;
    getUIFormSelectBox(FIELD_VERSION).setDisabled(!isAddNew) ;
    getUIStringInput(FIELD_NAME).setEditable(isAddNew).setValue(null) ;
    getUIStringInput(FIELD_PERMISSION).setEditable(isAddNew).setValue(null) ;
    getUIFormInputInfo(FIELD_TABS).setEditable(isAddNew).setValue(null) ;
    getUIFormSelectBox(FIELD_TEMPLATE).setValue(null) ;
    getUIFormSelectBox(FIELD_TEMPLATE).setDisabled(!isAddNew) ;
    getUIFormCheckBoxInput(FIELD_ENABLEVERSION).setRendered(!isAddNew) ;
    if(isAddNew) {
      setActions(new String[]{"Save", "Reset", "Cancel"}, null) ;
      setActionInfo(FIELD_PERMISSION, new String[] {"AddPermission"}) ;
      tabMap_.clear() ;
      views_ = null ;
      setActionInfo(FIELD_TABS, null) ;
    }
    selectedVersion_ = null ;
    baseVersion_ = null ;
  }
  
  public void update(Node viewNode, boolean isView, VersionNode selectedVersion) throws Exception {
    isView_ = isView ;
    if(viewNode != null) {
      views_ = viewNode ;
      if(isVersioned(views_)) baseVersion_ = views_.getBaseVersion();
      tabMap_.clear() ;
      for(NodeIterator iter = views_.getNodes(); iter.hasNext(); ) {
        Node tab = iter.nextNode() ;
        String buttons = tab.getProperty("exo:buttons").getString() ;
        Tab tabObj = new Tab() ;
        tabObj.setTabName(tab.getName()) ;
        tabObj.setButtons(buttons) ;
        tabMap_.put(tab.getName(), tabObj) ;
      }

      getUIFormCheckBoxInput(FIELD_ENABLEVERSION).setRendered(true) ;
      if (isVersioned(views_)) {              
        getUIFormSelectBox(FIELD_VERSION).setOptions(getVersionValues(views_)).setRendered(true) ;
        getUIFormSelectBox(FIELD_VERSION).setValue(baseVersion_.getName()) ;
        getUIFormCheckBoxInput(FIELD_ENABLEVERSION).setChecked(true) ;
        getUIFormCheckBoxInput(FIELD_ENABLEVERSION).setEnable(false) ;
      } else if (!isVersioned(views_)) {
        getUIFormSelectBox(FIELD_VERSION).setRendered(false) ;
        getUIFormCheckBoxInput(FIELD_ENABLEVERSION).setChecked(false) ;
        getUIFormCheckBoxInput(FIELD_ENABLEVERSION).setEnable(!isView_) ;   
      } 
    }
    if (selectedVersion != null) {      
      views_.restore(selectedVersion.getVersion(), false) ;
      views_.checkout() ;
      tabMap_.clear() ;
      for(NodeIterator iter = views_.getNodes(); iter.hasNext(); ) {
        Node tab = iter.nextNode() ;
        String buttons = tab.getProperty("exo:buttons").getString() ;
        Tab tabObj = new Tab() ;
        tabObj.setTabName(tab.getName()) ;
        tabObj.setButtons(buttons) ;
        tabMap_.put(tab.getName(), tabObj) ;
      }
      selectedVersion_ = selectedVersion;         
    }
    if(views_ != null) {
      getUIStringInput(FIELD_NAME).setEditable(false).setValue(views_.getName()) ;
      getUIStringInput(FIELD_PERMISSION).setValue(views_.getProperty("exo:permissions").getString()) ;
      getUIFormSelectBox(FIELD_TEMPLATE).setValue(views_.getProperty("exo:template").getString()) ;
    }
    setInfoField(FIELD_TABS, getTabList()) ;
    String[] actionInfor ;
    if(isView_) {
      actionInfor = new String[] {"EditTab"} ;
      setIsView(true) ;
    } else {
      actionInfor = new String[] {"EditTab", "DeleteTab"} ;
      setIsView(false) ;
    }
    setActionInfo(FIELD_TABS, actionInfor) ;
  }

  public void save() throws Exception {
    String viewName = getUIStringInput(FIELD_NAME).getValue() ;
    ApplicationMessage message ;
    if(viewName == null || viewName.length() < 1){
      throw new MessageException(new ApplicationMessage("UIViewForm.msg.view-name-invalid", null)) ;
    }
    String permissions = getUIStringInput(FIELD_PERMISSION).getValue() ;
    if(permissions == null || permissions.length() < 1){
      message = new ApplicationMessage("UIViewForm.msg.permission-not-empty", null) ;
      throw new MessageException(message) ;
    }
    if(tabMap_.size() < 1 ){
      message = new ApplicationMessage("UIViewForm.msg.mustbe-add-tab", null) ;
      throw new MessageException(message) ;
    }
    String template = getUIFormSelectBox(FIELD_TEMPLATE).getValue() ;

    List<Tab> tabList = new ArrayList<Tab>(tabMap_.values());
    boolean isEnableVersioning = getUIFormCheckBoxInput(FIELD_ENABLEVERSION).isChecked() ;
    if(views_ == null || !isEnableVersioning) {
      vservice_.addView(viewName, permissions, template, tabList) ;
    } else {
      if (!isVersioned(views_)) views_.addMixin(MIXVERSION);                            
      else views_.checkout() ;
      for(NodeIterator iter = views_.getNodes(); iter.hasNext(); ) {
        Node tab = iter.nextNode() ;
        if(!tabMap_.containsKey(tab.getName())) tab.remove() ;
      }
      vservice_.addView(viewName, permissions, template, tabList) ;
      views_.checkin();
      views_.save();
    }
    getAncestorOfType(UIViewContainer.class).getChild(UIViewList.class).updateViewListGrid() ;
    refresh(true) ;
  }

  public void editTab(String tabName) throws Exception {
    UIViewFormTabPane viewTabPane = getParent() ;
    UITabForm tabForm = viewTabPane.getChild(UITabForm.class) ;
    tabForm.update(tabMap_.get(tabName), isView_) ;
    setRenderSibbling(UITabForm.class) ;
  }

  public void deleteTab(String tabName) throws Exception {
    tabMap_.remove(tabName) ;
    update(null, false, null) ;
    UIViewContainer uiViewContainer = getAncestorOfType(UIViewContainer.class) ;
    UIViewList uiViewList = uiViewContainer.getChild(UIViewList.class) ;
    uiViewList.updateViewListGrid() ;
    setRenderSibbling(UIViewForm.class) ;
  }

  public void changeVersion() throws Exception {
    String path = 
      views_.getVersionHistory().getVersion(getUIFormSelectBox(FIELD_VERSION).getValue()).getPath() ;
    VersionNode selectedVesion = getRootVersion(views_).findVersionNode(path);
    update(null, false, selectedVesion) ;
  }
  
  public void revertVersion() throws Exception {
    if (selectedVersion_ != null && !selectedVersion_.equals(baseVersion_)) { 
      views_.restore(baseVersion_, true);
    }
  }
  
  static public class AddPermissionActionListener extends EventListener<UIViewFormTabPane> {
    public void execute(Event<UIViewFormTabPane> event) throws Exception {
      UIViewFormTabPane uiViewTabPane = event.getSource() ;
      UIViewContainer uiContainer = uiViewTabPane.getAncestorOfType(UIViewContainer.class) ;
      uiContainer.initPopupPermission() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }
}