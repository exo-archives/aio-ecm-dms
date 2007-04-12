/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.portlet.PortletPreferences;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIJCRBrowser;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.webui.component.UIContainer;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIPopupWindow;
import org.exoplatform.webui.component.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.component.model.SelectItemOption;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Dec 19, 2006 9:24:36 AM 
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)

public class UIConfigTabPane extends UIContainer {

  public static String BROWSETYPE = null ;
  public static String WORKSPACE = null ;
  public static String PATH_SELECTOR = "pathSelector" ;
  public static String DOCUMENT_SELECTOR = "documentSelector" ;
  
  public UIConfigTabPane() throws Exception {
    addChild(UINewConfigForm.class, null, null).setRendered(false) ;
    addChild(UIConfigContainer.class, null, null) ;
  }
  
  public List<SelectItemOption<String>> getWorkSpaceOption() throws Exception {
    List<SelectItemOption<String>> Options = new ArrayList<SelectItemOption<String>>() ;
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    String[] workspaceNames = repositoryService.getRepository().getWorkspaceNames() ;
    for(String workspace:workspaceNames) {
      Options.add(new SelectItemOption<String>(workspace,workspace)) ;
    }   
    return Options ;
  }

  public List<SelectItemOption<String>> getBoxTemplateOption() throws Exception {
    List<SelectItemOption<String>> Options = new ArrayList<SelectItemOption<String>>() ;
    ManageViewService viewService = 
      (ManageViewService)PortalContainer.getComponent(ManageViewService.class) ;
    List<Node> docTemplates = viewService.getAllTemplates(BasePath.CB_DETAIL_VIEW_TEMPLATES) ;
    for(Node template: docTemplates) {
      Options.add(new SelectItemOption<String>(template.getName(), template.getName())) ;
    }
    return Options ;
  }

  public void getCurrentConfig() throws Exception {
    PortletPreferences preference = getAncestorOfType(UIBrowseContentPortlet.class).getPortletPreferences() ;
    UINewConfigForm uiConfigForm = getChild(UINewConfigForm.class) ;
    UIConfigContainer uiConfigContainer = getChild(UIConfigContainer.class) ;
    uiConfigForm.setRendered(false) ;
    uiConfigContainer.getChildren().clear() ;
    BROWSETYPE = preference.getValue(Utils.CB_USECASE, "") ;
    WORKSPACE = preference.getValue(Utils.WORKSPACE_NAME, "") ;
    if(BROWSETYPE.equals(Utils.CB_USE_FROM_PATH)) {
      UIPathConfig uiPathConfig = uiConfigContainer.addChild(UIPathConfig.class, null, null) ;
      uiPathConfig.initForm(preference, WORKSPACE, false, false) ;
    } else if(BROWSETYPE.equals(Utils.CB_USE_JCR_QUERY)) {
      UIQueryConfig  uiQueryConfig = uiConfigContainer.addChild(UIQueryConfig.class, null, null) ;
      uiQueryConfig.initForm(preference, WORKSPACE, false, false) ;
    } else if(BROWSETYPE.equals(Utils.CB_USE_SCRIPT)) {
      UIScriptConfig uiScriptConfig = uiConfigContainer.addChild(UIScriptConfig.class, null, null) ;
      uiScriptConfig.initForm(preference, WORKSPACE, false, false);
    } else if(BROWSETYPE.equals(Utils.CB_USE_DOCUMENT)) {
      UIDocumentConfig uiDocumentConfig = uiConfigContainer.addChild(UIDocumentConfig.class, null, null) ;
      uiDocumentConfig.initForm(preference, WORKSPACE, false, false);
    }
    uiConfigContainer.setRendered(true) ;
  }

  public void loadNewConfig(boolean isAddNew) throws Exception {
    UINewConfigForm uiConfigForm = getChild(UINewConfigForm.class) ;
    UIConfigContainer uiConfigContainer = getChild(UIConfigContainer.class) ;
    if(isAddNew) uiConfigForm.resetForm() ;
    uiConfigForm.setRendered(true) ;
    uiConfigContainer.setRendered(false) ;
    uiConfigContainer.getChildren().clear() ;
  }

  public void initNewConfig(String browseType, String workSpace) throws Exception {
    UINewConfigForm uiConfigForm = getChild(UINewConfigForm.class) ;
    uiConfigForm.setRendered(false) ;
    UIConfigContainer uiConfigContainer = getChild(UIConfigContainer.class) ;
    uiConfigContainer.getChildren().clear() ;
    uiConfigContainer.initNewConfig(browseType, workSpace) ;
    uiConfigContainer.setRendered(true) ;
  }

  public void initPopupPathSelect(UIForm uiForm, String workSpace) throws Exception {
    removeChildById(PATH_SELECTOR) ;
    removeChildById(DOCUMENT_SELECTOR) ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, PATH_SELECTOR);
    uiPopup.setWindowSize(610, 300);
    UIJCRBrowser uiJCRBrowser = createUIComponent(UIJCRBrowser.class, null, null) ;
    uiJCRBrowser.setWorkspace(workSpace) ;
    String[] filterType = {Utils.NT_FOLDER, Utils.NT_UNSTRUCTURED} ;
    uiJCRBrowser.setFilterType(filterType) ;
    uiPopup.setUIComponent(uiJCRBrowser) ;
    uiJCRBrowser.setComponent(uiForm, new String[] {UINewConfigForm.FIELD_CATEGORYPATH}) ;
    uiPopup.setShow(true) ;
  }

  @SuppressWarnings("unchecked")
  public void initPopupDocumentSelect(UIForm uiForm, String path) throws Exception {
    CmsConfigurationService cmsService = getApplicationComponent(CmsConfigurationService.class) ;
    removeChildById(PATH_SELECTOR) ;
    removeChildById(DOCUMENT_SELECTOR) ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, DOCUMENT_SELECTOR);
    uiPopup.setWindowSize(610, 300);
    UIJCRBrowser uiJCRBrowser = createUIComponent(UIJCRBrowser.class, null, null) ;
    uiJCRBrowser.setWorkspace(cmsService.getWorkspace()) ;
    uiJCRBrowser.setRootPath(path) ;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    List<String> documents = templateService.getDocumentTemplates() ;
    String [] filterType = new String[documents.size()];
    documents.toArray(filterType) ;
    uiJCRBrowser.setFilterType(filterType) ;
    uiPopup.setUIComponent(uiJCRBrowser) ;
    uiJCRBrowser.setComponent(uiForm, new String[] {UINewConfigForm.FIELD_DOCNAME}) ;
    uiPopup.setShow(true) ;
  }
}
