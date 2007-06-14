/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.UIJCRBrowser;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Dec 19, 2006 9:24:36 AM 
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)

public class UIConfigTabPane extends UIContainer {

  public static String PATH_SELECTOR = "pathSelector" ;
  public static String DOCUMENT_SELECTOR = "documentSelector" ;
  public String configType_ = null ;
  protected boolean isNewConfig_ = false ;
  public UIConfigTabPane() throws Exception {
    addChild(UINewConfigForm.class, null, null).setRendered(false) ;
    addChild(UIConfigContainer.class, null, null) ;
  }
  
  public List<SelectItemOption<String>> getWorkSpaceOption() throws Exception {
    List<SelectItemOption<String>> Options = new ArrayList<SelectItemOption<String>>() ;
    String repository = getAncestorOfType(UIBrowseContentPortlet.class).getPreferenceRepository() ;
    String[] workspaceNames = getApplicationComponent(RepositoryService.class)
                             .getRepository(repository).getWorkspaceNames() ;
    for(String workspace:workspaceNames) {
      Options.add(new SelectItemOption<String>(workspace,workspace)) ;
    }   
    return Options ;
  }

  public List<SelectItemOption<String>> getBoxTemplateOption() throws Exception {
    List<SelectItemOption<String>> Options = new ArrayList<SelectItemOption<String>>() ;
    String repository = getAncestorOfType(UIBrowseContentPortlet.class).getPreferenceRepository() ;
    List<Node> docTemplates = getApplicationComponent(ManageViewService.class)
                             .getAllTemplates(BasePath.CB_DETAIL_VIEW_TEMPLATES, repository) ;
    for(Node template: docTemplates) {
      Options.add(new SelectItemOption<String>(template.getName(), template.getName())) ;
    }
    return Options ;
  }

  public void getCurrentConfig() throws Exception {
    PortletPreferences preference = getAncestorOfType(UIBrowseContentPortlet.class).getPortletPreferences() ;
    UINewConfigForm uiConfigForm = getChild(UINewConfigForm.class) ;
    uiConfigForm.setRendered(false) ;
    UIConfigContainer uiConfigContainer = getChild(UIConfigContainer.class) ;
    String repository = preference.getValue(Utils.REPOSITORY, "") ;
    String usecase = preference.getValue(Utils.CB_USECASE, "") ;
    String workspace = preference.getValue(Utils.WORKSPACE_NAME, "") ;
    if(usecase.equals(Utils.CB_USE_FROM_PATH)) {
      UIPathConfig uiPathConfig = uiConfigContainer.getChild(UIPathConfig.class) ;
      if(uiPathConfig == null) {
        uiPathConfig = uiConfigContainer.addChild(UIPathConfig.class, null, null) ;
      }      
      uiPathConfig.initForm(preference, repository, workspace, false) ;
      uiConfigContainer.setRenderedChild(UIPathConfig.class) ;
    } else if(usecase.equals(Utils.CB_USE_JCR_QUERY)) {
      UIQueryConfig uiQueryConfig = uiConfigContainer.getChild(UIQueryConfig.class) ;
      if(uiQueryConfig == null) {
        uiQueryConfig = uiConfigContainer.addChild(UIQueryConfig.class, null, null) ;
      }      
      uiQueryConfig.initForm(preference, repository, workspace, false) ;
      uiConfigContainer.setRenderedChild(UIQueryConfig.class) ;
    } else if(usecase.equals(Utils.CB_USE_SCRIPT)) { 
       UIScriptConfig uiScriptConfig = uiConfigContainer.getChild(UIScriptConfig.class);
       if(uiScriptConfig == null) { 
         uiScriptConfig = uiConfigContainer.addChild(UIScriptConfig.class, null, null) ;
         
       }
      uiScriptConfig.initForm(preference, repository, workspace, false);
      uiConfigContainer.setRenderedChild(UIScriptConfig.class) ;
    } else if(usecase.equals(Utils.CB_USE_DOCUMENT)) {
      UIDocumentConfig uiDocumentConfig = uiConfigContainer.getChild(UIDocumentConfig.class) ;
        if(uiDocumentConfig == null) {
          uiDocumentConfig = uiConfigContainer.addChild(UIDocumentConfig.class, null, null) ;
        }
      uiDocumentConfig.initForm(preference, repository, workspace, false);
      uiConfigContainer.setRenderedChild(UIDocumentConfig.class) ;
    }
    uiConfigContainer.setRendered(true) ;
  }
  
  
  public void showNewConfigForm(boolean isAddNew) throws Exception {
    UINewConfigForm uiConfigForm = getChild(UINewConfigForm.class) ;
    UIConfigContainer uiConfigContainer = getChild(UIConfigContainer.class) ;
    if(isAddNew) uiConfigForm.resetForm() ;
    uiConfigForm.setRendered(true) ;
    uiConfigContainer.setRendered(false) ;
  }

  public void initNewConfig(String usercase, String repository, String workSpace) throws Exception {
    UINewConfigForm uiConfigForm = getChild(UINewConfigForm.class) ;
    uiConfigForm.setRendered(false) ;
    UIConfigContainer uiConfigContainer = getChild(UIConfigContainer.class) ;
    uiConfigContainer.initNewConfig(usercase, repository, workSpace) ;
    uiConfigContainer.setRendered(true) ;
  }

  public void initPopupPathSelect(UIForm uiForm, String repo, String workSpace) throws Exception {
    removeChildById(PATH_SELECTOR) ;
    removeChildById(DOCUMENT_SELECTOR) ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, PATH_SELECTOR);
    uiPopup.setWindowSize(610, 300);
    UIJCRBrowser uiJCRBrowser = createUIComponent(UIJCRBrowser.class, null, null) ;
    uiJCRBrowser.setWorkspace(workSpace) ;
    uiJCRBrowser.setRepository(repo) ;
    String[] filterType = {Utils.NT_FOLDER, Utils.NT_UNSTRUCTURED} ;
    uiJCRBrowser.setFilterType(filterType) ;
    uiPopup.setUIComponent(uiJCRBrowser) ;
    uiJCRBrowser.setComponent(uiForm, new String[] {UINewConfigForm.FIELD_CATEGORYPATH}) ;
    uiPopup.setShow(true) ;
  }

  @SuppressWarnings("unchecked")
  public void initPopupDocumentSelect(UIForm uiForm, String repo, String workspace, String path) throws Exception {
    removeChildById(PATH_SELECTOR) ;
    removeChildById(DOCUMENT_SELECTOR) ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, DOCUMENT_SELECTOR);
    uiPopup.setWindowSize(610, 300);
    UIJCRBrowser uiJCRBrowser = createUIComponent(UIJCRBrowser.class, null, null) ;
    uiJCRBrowser.setRepository(repo) ;
    uiJCRBrowser.setWorkspace(workspace) ;
    uiJCRBrowser.setRootPath(path) ;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    List<String> documents = templateService.getDocumentTemplates(repo) ;
    String [] filterType = new String[documents.size()];
    documents.toArray(filterType) ;
    uiJCRBrowser.setFilterType(filterType) ;
    uiPopup.setUIComponent(uiJCRBrowser) ;
    uiJCRBrowser.setComponent(uiForm, new String[] {UINewConfigForm.FIELD_DOCNAME}) ;
    uiPopup.setShow(true) ;
  }
}
