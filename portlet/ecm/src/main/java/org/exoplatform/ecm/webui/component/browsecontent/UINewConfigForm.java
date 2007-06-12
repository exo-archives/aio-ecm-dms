/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Oct 25, 2006 3:23:00 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/form/UIFormWithTitle.gtmpl",
    events = {
      @EventConfig(phase = Phase.DECODE, listeners = UINewConfigForm.BackActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UINewConfigForm.NextActionListener.class)
    }
)

public class UINewConfigForm extends UIForm {
  final static public String FIELD_REPOSITORY = "repository" ;
  final static public String FIELD_WORKSPACE = "workspace" ;
  final static public String FIELD_BROWSETYPE = "browseType" ;
  final static public String FIELD_TEMPLATE = "template" ;
  final static public String FIELD_DETAILBOXTEMP = "detailBoxTemp" ;
  final static public String FIELD_QUERYLANG = "queryLanguage" ;
  final static public String FIELD_QUERYSTATUS = "queryStatus" ;
  final static public String FIELD_QUERYSTORE = "queryStore" ;
  final static public String FIELD_QUERYTYPE = "queryType" ;
  final static public String FIELD_CATEGORYPATH = "categoryPath" ;
  final static public String FIELD_SCRIPTNAME = "scriptName" ;
  final static public String FIELD_DOCNAME = "docName" ;
  final static public String FIELD_ITEMPERPAGE = "itemPerPage" ;
  final static public String FIELD_ENABLETOOLBAR = "enableToolBar" ;
  final static public String FIELD_ENABLEREFDOC = "enableRefDoc" ;
  final static public String FIELD_ENABLECHILDDOC = "enableChildDoc" ;
  final static public String FIELD_ENABLETAGMAP = "enableTagMap" ;
  final static public String FIELD_ENABLECOMMENT = "enableComment" ;
  final static public String FIELD_ENABLEVOTE = "enableVote" ;
  final static public String FIELD_QUERY = "query" ;
  final static public String[] DEFAULT_ACTION = new String[]{"Edit", "Add"} ;
  final static public String[] NORMAL_ACTION = new String[]{"Save", "Cancel"} ;
  final static public String[] ADD_NEW_ACTION = new String[]{"Back", "Save"} ;

  private String repoName_ = "repository" ;

  public UINewConfigForm() throws Exception {
    UIFormSelectBox repoSelectBox = new UIFormSelectBox(FIELD_REPOSITORY, FIELD_REPOSITORY, getRepoOption()) ;
    repoSelectBox.setValue(repoName_) ;
    repoSelectBox.setOnChange("OnChange") ;
    addChild(repoSelectBox) ;
    addChild(new UIFormSelectBox(FIELD_WORKSPACE, FIELD_WORKSPACE, getWorkSpaceOption())) ;
    addChild( new UIFormSelectBox(FIELD_BROWSETYPE, FIELD_BROWSETYPE, getBrowseTypeOption())) ;
  }

  public void resetForm() throws Exception{
    getUIFormSelectBox(FIELD_WORKSPACE).reset() ;
    getUIFormSelectBox(FIELD_BROWSETYPE).reset() ;
  }

  public List<SelectItemOption<String>>  getRepoOption() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    ManageableRepository repo = repositoryService.getRepository() ;
    options.add(new SelectItemOption<String>(repo.getConfiguration().getName(), repo.getConfiguration().getName())) ;
    return options ;
  }

  public List<SelectItemOption<String>> getWorkSpaceOption() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    String[] workspaceNames = repositoryService.getRepository(repoName_).getWorkspaceNames() ;
    for(String workspace:workspaceNames) {
      options.add(new SelectItemOption<String>(workspace,workspace)) ;
    }   
    return options ;
  }
  public List<SelectItemOption<String>> getBrowseTypeOption() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>(Utils.FROM_PATH, Utils.CB_USE_FROM_PATH)) ;
    options.add(new SelectItemOption<String>(Utils.USE_JCR_QUERY, Utils.CB_USE_JCR_QUERY)) ;
    options.add(new SelectItemOption<String>(Utils.USE_SCRIPT, Utils.CB_USE_SCRIPT)) ;
    options.add(new SelectItemOption<String>(Utils.USE_DOCUMENT, Utils.CB_USE_DOCUMENT)) ;
    return options ;
  }

  public static class OnChangeActionListener extends EventListener<UINewConfigForm>{
    public void execute(Event<UINewConfigForm> event) throws Exception {
      UINewConfigForm uiForm = event.getSource() ;
      UIFormSelectBox repoSelect = uiForm.getUIFormSelectBox(UINewConfigForm.FIELD_REPOSITORY) ;
      uiForm.repoName_ = repoSelect.getValue() ;
      UIFormSelectBox workspaceSelect = uiForm.getUIFormSelectBox(UINewConfigForm.FIELD_WORKSPACE) ;
      workspaceSelect.setOptions(uiForm.getWorkSpaceOption()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }

  }

  public static class BackActionListener extends EventListener<UINewConfigForm>{
    public void execute(Event<UINewConfigForm> event) throws Exception {
      UINewConfigForm uiForm = event.getSource() ;
      UIConfigTabPane uiConfigTabPane = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      uiConfigTabPane.getCurrentConfig() ;
    }
  }  

  public static class NextActionListener extends EventListener<UINewConfigForm>{
    public void execute(Event<UINewConfigForm> event) throws Exception {
      UINewConfigForm uiForm = event.getSource() ;
      UIConfigTabPane uiConfigTabPane = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      String browseType = uiForm.getUIFormSelectBox(FIELD_BROWSETYPE).getValue() ;
      String workSpace = uiForm.getUIFormSelectBox(FIELD_WORKSPACE).getValue() ; 
      uiConfigTabPane.initNewConfig(browseType,workSpace) ;
    }
  }  
}
