/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.query.Query;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.queries.QueryService;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Dec 19, 2006 9:05:58 AM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/form/UIFormWithTitle.gtmpl",
    events = {
      @EventConfig(listeners = UIQueryConfig.SaveActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIQueryConfig.ChangeLangActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIQueryConfig.ChangeStatusActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIQueryConfig.ChangeTypeActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIQueryConfig.EditActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIQueryConfig.AddActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIQueryConfig.CancelActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIQueryConfig.BackActionListener.class)
    }
)
public class UIQueryConfig extends UIForm {

  final private String xpathDefault_ = "/jcr:root/cms/publications//element(*, exo:article)" ;
  final private String sqlDefault_ = "select * from exo:article where jcr:path like '/cms/publications/%'" ;
  final private static String NEW_QUERY = "New Query".intern() ;
  final private static String EXITING_QUERY = "Exiting Query".intern() ;
  final private static String PERSONAL_QUERY = "Personal Query".intern() ;
  final private static String SHARED_QUERY = "Shared Query".intern() ;
  private List<String> roles_ = new ArrayList<String>();
  protected boolean isEdit_ = false ;

  public UIQueryConfig() throws Exception {
    List<SelectItemOption<String>> Options = new ArrayList<SelectItemOption<String>>() ;
    addChild(new UIFormStringInput(UINewConfigForm.FIELD_REPOSITORY, UINewConfigForm.FIELD_REPOSITORY, null)) ;
    addChild(new UIFormStringInput(UINewConfigForm.FIELD_WORKSPACE, UINewConfigForm.FIELD_WORKSPACE, null)) ;
    addChild(new UIFormSelectBox(UINewConfigForm.FIELD_QUERYSTATUS, null, Options)) ;
    addChild(new UIFormSelectBox(UINewConfigForm.FIELD_QUERYLANG, null, Options)) ;
    addChild(new UIFormSelectBox(UINewConfigForm.FIELD_QUERYTYPE, null, Options).setRendered(false)) ;
    addChild(new UIFormSelectBox(UINewConfigForm.FIELD_QUERYSTORE, null, Options).setRendered(false)) ;
    addChild(new UIFormTextAreaInput(UINewConfigForm.FIELD_QUERY, null, null)) ;    
    addChild(new UIFormSelectBox(UINewConfigForm.FIELD_TEMPLATE, null, Options)) ;
    addChild(new UIFormStringInput(UINewConfigForm.FIELD_ITEMPERPAGE, null, null)) ;
    addChild(new UIFormSelectBox(UINewConfigForm.FIELD_DETAILBOXTEMP, null, Options)) ;
    addChild(new UIFormCheckBoxInput<Boolean>(UINewConfigForm.FIELD_ENABLETAGMAP, null, null)) ;
    addChild(new UIFormCheckBoxInput<Boolean>(UINewConfigForm.FIELD_ENABLECOMMENT, null, null)) ;
    addChild(new UIFormCheckBoxInput<Boolean>(UINewConfigForm.FIELD_ENABLEVOTE, null, null)) ;
    setActions(UINewConfigForm.DEFAULT_ACTION) ;
    OrganizationService oservice = getApplicationComponent(OrganizationService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    Collection memberships = oservice.getMembershipHandler().findMembershipsByUser(username) ;
    if(memberships != null && memberships.size() > 0){
      Object[] objects = memberships.toArray() ;      
      for(int i = 0 ; i < objects.length ; i ++ ){
        Membership membership = (Membership)objects[i] ;
        String roles = membership.getMembershipType()+ ":" + membership.getGroupId() ;
        roles_.add(roles) ;      
      } 
    }
  }

  public PortletPreferences getPortletPreferences() {    
    return getAncestorOfType(UIBrowseContentPortlet.class).getPortletPreferences() ;
  }

  public void initForm(PortletPreferences preference, String repository, String workSpace, 
      boolean isAddNew) throws Exception {
    String queryLang = "sql" ;
    String queryType = PERSONAL_QUERY ;
    String queryStoreName = null ;
    String queryStatement = sqlDefault_ ;
    String queryNew = "true" ;
    String hasComment = "false" ;
    String hasVote = "false" ;
    String hasTagMap = "false" ;
    String itemPerPage = "20" ;
    UIFormStringInput workSpaceField = getChildById(UINewConfigForm.FIELD_WORKSPACE) ;
    workSpaceField.setValue(workSpace) ;
    workSpaceField.setEditable(false) ;
    UIFormStringInput repositoryField = getChildById(UINewConfigForm.FIELD_REPOSITORY) ;
    repositoryField.setValue(repository) ;
    repositoryField.setEditable(false) ;
    UIFormSelectBox queryStatusField = getChildById(UINewConfigForm.FIELD_QUERYSTATUS) ;
    UIFormSelectBox queryLangField = getChildById(UINewConfigForm.FIELD_QUERYLANG) ;
    UIFormSelectBox queryTypeField = getChildById(UINewConfigForm.FIELD_QUERYTYPE) ;
    UIFormSelectBox queryStoreField = getChildById(UINewConfigForm.FIELD_QUERYSTORE) ;
    //UIFormTextAreaInput queryField = getChildById(UINewConfigForm.FIELD_QUERY) ;
    UIFormSelectBox templateField = getChildById(UINewConfigForm.FIELD_TEMPLATE) ;
    UIFormStringInput numbPerPageField = getChildById(UINewConfigForm.FIELD_ITEMPERPAGE) ;
    UIFormSelectBox detailtemField = getChildById(UINewConfigForm.FIELD_DETAILBOXTEMP) ;
    UIFormCheckBoxInput enableTagMapField = getChildById(UINewConfigForm.FIELD_ENABLETAGMAP) ;
    UIFormCheckBoxInput enableCommentField = getChildById(UINewConfigForm.FIELD_ENABLECOMMENT) ;
    UIFormCheckBoxInput enableVoteField = getChildById(UINewConfigForm.FIELD_ENABLEVOTE) ;
    if(isEdit_) {
      if(isAddNew) {
        setActions(UINewConfigForm.ADD_NEW_ACTION) ;
        templateField.setOptions(getQueryTemplate()) ;
        UIConfigTabPane uiConfigTabPane = getAncestorOfType(UIConfigTabPane.class) ;
        detailtemField.setOptions(uiConfigTabPane.getBoxTemplateOption(repository)) ;
        queryStatusField.setOptions(getQueryStatus()) ;
        queryStatusField.setValue(NEW_QUERY) ;
        queryLangField.setOptions(getQueryLang()) ;
        queryLangField.setValue(queryLang) ;
        queryTypeField.setOptions(getQueryType()) ;
        queryTypeField.setValue(queryType) ;
        onchangeAction(queryStatusField.getValue(), queryTypeField.getValue(), queryLangField.getValue(), null, queryStatement) ;
        numbPerPageField.setValue(itemPerPage) ;
        enableTagMapField.setChecked(Boolean.parseBoolean(hasTagMap)) ;
        enableCommentField.setChecked(Boolean.parseBoolean(hasComment)) ;
        enableVoteField.setChecked(Boolean.parseBoolean(hasVote)) ;
        queryStatusField.setOptions(getQueryStatus()) ;
      }else {
        setActions(UINewConfigForm.NORMAL_ACTION) ;
      }
    } else {
      setActions(UINewConfigForm.DEFAULT_ACTION) ;
      repository = preference.getValue(Utils.REPOSITORY, "") ;
      queryNew = preference.getValue(Utils.CB_QUERY_ISNEW, "") ;
      queryType = preference.getValue(Utils.CB_QUERY_TYPE, "") ;
      queryStoreName = preference.getValue(Utils.CB_QUERY_STORE, "") ;
      queryStatement = preference.getValue(Utils.CB_QUERY_STATEMENT, "") ;
      queryLang = preference.getValue(Utils.CB_QUERY_LANGUAGE, "") ;
      itemPerPage = preference.getValue(Utils.CB_NB_PER_PAGE, "") ;
      hasTagMap  = preference.getValue(Utils.CB_VIEW_TAGMAP, "") ;
      hasComment = preference.getValue(Utils.CB_VIEW_COMMENT, "") ;
      hasVote = preference.getValue(Utils.CB_VIEW_VOTE, "") ;
      templateField.setOptions(getQueryTemplate()) ;
      numbPerPageField.setValue(itemPerPage) ;
      UIConfigTabPane uiConfigTabPane = getAncestorOfType(UIConfigTabPane.class) ;
      detailtemField.setOptions(uiConfigTabPane.getBoxTemplateOption(repository)) ;
      enableTagMapField.setChecked(Boolean.parseBoolean(hasTagMap)) ;
      enableCommentField.setChecked(Boolean.parseBoolean(hasComment)) ;
      enableVoteField.setChecked(Boolean.parseBoolean(hasVote)) ;
      queryStatusField.setOptions(getQueryStatus()) ;
      queryStatusField.setValue(EXITING_QUERY) ;
      if(Boolean.parseBoolean(queryNew)) queryStatusField.setValue(NEW_QUERY) ;
      queryLangField.setOptions(getQueryLang()) ;
      queryLangField.setValue(queryLang) ;
      queryTypeField.setOptions(getQueryType()) ;
      onchangeAction(queryStatusField.getValue(), queryTypeField.getValue(), queryLangField.getValue(), queryStoreName, queryStatement) ;
    }
    queryStatusField.setOnChange("ChangeStatus") ;
    queryLangField.setOnChange("ChangeLang") ;
    queryTypeField.setOnChange("ChangeType") ;
    queryStatusField.setEnable(isEdit_) ;
    queryLangField.setEnable(isEdit_) ;
    queryTypeField.setEnable(isEdit_) ;
    queryStoreField.setEnable(isEdit_) ;
    //queryField.setEditable(isEdit_) ;
    //queryField.setEnable(isEdit_) ;
    numbPerPageField.setEditable(isEdit_) ;
    templateField.setEnable(isEdit_) ;
    detailtemField.setEnable(isEdit_) ;
    enableTagMapField.setEnable(isEdit_) ;
    enableCommentField.setEnable(isEdit_) ;  
    enableVoteField.setEnable(isEdit_) ; 
  }

  protected void onchangeAction(String queryStatus, String queryType, String queryLanguage, String queryStoreName, 
      String queryStatement) throws Exception {
    boolean isNewquery = queryStatus.equals(NEW_QUERY) ;
    UIFormSelectBox queryStore = getChildById(UINewConfigForm.FIELD_QUERYSTORE) ;
    UIFormSelectBox queryTypeField = getChildById(UINewConfigForm.FIELD_QUERYTYPE) ;
    UIFormTextAreaInput query = getChildById(UINewConfigForm.FIELD_QUERY) ;
    if(isNewquery) {
      if(queryLanguage.equals(Query.XPATH)) {
        if(queryStatement == null) queryStatement = xpathDefault_ ;
        query.setValue(queryStatement) ;
      }
      if(queryLanguage.equals(Query.SQL)&& queryStatement != null) {
        if(queryStatement == null) queryStatement = sqlDefault_ ;
        query.setValue(queryStatement) ;
      }
    } else {
      queryStore.setOptions(getQueryStore(queryType, queryLanguage)) ;
      if(queryStoreName != null) queryStore.setValue(queryStoreName) ;
    }
    queryStore.setRendered(!isNewquery);
    queryTypeField.setRendered(!isNewquery);
    query.setRendered(isNewquery) ;
  }

  private List<SelectItemOption<String>> getQueryTemplate() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    String repository = getAncestorOfType(UIBrowseContentPortlet.class).getPreferenceRepository() ;
    List<Node> querylTemplates = getApplicationComponent(ManageViewService.class)
    .getAllTemplates(BasePath.CB_QUERY_TEMPLATES, repository) ;
    for(Node node: querylTemplates){
      options.add(new SelectItemOption<String>(node.getName(),node.getName())) ;
    }
    return options ;
  }

  public List<SelectItemOption<String>> getWorkSpaceOption() throws Exception {
    UIConfigTabPane uiTabPane = getAncestorOfType(UIConfigTabPane.class) ;
    return uiTabPane.getWorkSpaceOption() ;
  }

  private List<SelectItemOption<String>> getQueryLang() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>(Query.SQL, Query.SQL)) ;
    options.add(new SelectItemOption<String>(Query.XPATH, Query.XPATH)) ;
    return options ;
  }

  private List<SelectItemOption<String>> getQueryStatus() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>(NEW_QUERY, NEW_QUERY)) ;
    options.add(new SelectItemOption<String>(EXITING_QUERY, EXITING_QUERY)) ;
    return options ;
  }

  private List<SelectItemOption<String>> getQueryType() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>(SHARED_QUERY, SHARED_QUERY)) ;
    options.add(new SelectItemOption<String>(PERSONAL_QUERY, PERSONAL_QUERY)) ;
    return options ;
  }

  private List<SelectItemOption<String>> getQueryStore(String queryType, String queryLanguage) throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    String repository = getAncestorOfType(UIBrowseContentPortlet.class).getPreferenceRepository() ;
    QueryService qservice = getApplicationComponent(QueryService.class) ;  
    if(UIQueryConfig.PERSONAL_QUERY.equals(queryType)) {
      String username = Util.getPortalRequestContext().getRemoteUser() ;
      List<Query> queries = qservice.getQueries(username, repository);
      for(Query queryNode : queries) {
        String path = queryNode.getStoredQueryPath() ;
        if(queryNode.getLanguage().equals(queryLanguage))
          options.add(new SelectItemOption<String>(path.substring(path.lastIndexOf("/")+ 1), path)) ;
      }
    } else {
      List<Node> queries = qservice.getSharedQueries(queryLanguage, roles_, repository);
      for(Node queryNode : queries) {
        options.add(new SelectItemOption<String>(queryNode.getName(), queryNode.getPath())) ;
      }
    } 
    return options ;
  }
  public static class SaveActionListener extends EventListener<UIQueryConfig>{
    public void execute(Event<UIQueryConfig> event) throws Exception {
      UIQueryConfig uiForm = event.getSource() ;
      UIBrowseContentPortlet uiBrowseContentPortlet = uiForm.getAncestorOfType(UIBrowseContentPortlet.class) ;
      PortletPreferences prefs = uiBrowseContentPortlet.getPortletPreferences();
      String repository = uiForm.getUIStringInput(UINewConfigForm.FIELD_REPOSITORY).getValue() ;
      String workSpace = uiForm.getUIStringInput(UINewConfigForm.FIELD_WORKSPACE).getValue() ;
      String queryLang = uiForm.getUIFormSelectBox(UINewConfigForm.FIELD_QUERYLANG).getValue() ;
      String query = uiForm.getUIStringInput(UINewConfigForm.FIELD_QUERY).getValue() ;
      String template = uiForm.getUIFormSelectBox(UINewConfigForm.FIELD_TEMPLATE).getValue() ;
      String itemPerPage = uiForm.getUIStringInput(UINewConfigForm.FIELD_ITEMPERPAGE).getValue() ;
      String boxTemplate = uiForm.getUIStringInput(UINewConfigForm.FIELD_DETAILBOXTEMP).getValue() ;
      UIFormSelectBox queryValueField = uiForm.getUIFormSelectBox(UINewConfigForm.FIELD_QUERYSTORE) ;      
      String  queryPath = "" ;
      String queryStatu = uiForm.getUIFormSelectBox(UINewConfigForm.FIELD_QUERYSTATUS).getValue() ;
      boolean isNewquery = queryStatu.equals(UIQueryConfig.NEW_QUERY) ;
      if((!queryStatu.equals(UIQueryConfig.NEW_QUERY))&&(queryValueField.isRendered())) {
        queryPath = queryValueField.getValue() ;
        if((queryPath == null )||(queryPath.trim().length() == 0)){
          UIApplication app = uiForm.getAncestorOfType(UIApplication.class) ;
          app.addMessage(new ApplicationMessage("UIQueryConfig.msg.invalid-name", null)) ;
          return ;
        }
      }
      try{
        Integer.parseInt(itemPerPage) ;
      } catch(Exception e){
        UIApplication app = uiForm.getAncestorOfType(UIApplication.class) ;
        app.addMessage(new ApplicationMessage("UIQueryConfig.msg.invalid-value", null)) ;
        return ;
      }
      String  queryType = uiForm.getUIFormSelectBox(UINewConfigForm.FIELD_QUERYTYPE).getValue() ;
      boolean hasTagMap = uiForm.getUIFormCheckBoxInput(UINewConfigForm.FIELD_ENABLETAGMAP).isChecked() ;
      boolean hasComment = uiForm.getUIFormCheckBoxInput(UINewConfigForm.FIELD_ENABLECOMMENT).isChecked() ;
      boolean hasVote = uiForm.getUIFormCheckBoxInput(UINewConfigForm.FIELD_ENABLEVOTE).isChecked() ;    
      prefs.setValue(Utils.CB_USECASE, Utils.CB_USE_JCR_QUERY) ;
      prefs.setValue(Utils.REPOSITORY, repository) ;
      prefs.setValue(Utils.WORKSPACE_NAME, workSpace) ;
      prefs.setValue(Utils.CB_QUERY_LANGUAGE, queryLang) ;
      prefs.setValue(Utils.CB_NB_PER_PAGE, itemPerPage) ;
      prefs.setValue(Utils.CB_TEMPLATE, template) ;
      prefs.setValue(Utils.CB_BOX_TEMPLATE, boxTemplate) ; 
      prefs.setValue(Utils.CB_VIEW_TAGMAP, String.valueOf(hasTagMap)) ; 
      prefs.setValue(Utils.CB_VIEW_COMMENT,String.valueOf(hasComment)) ;    
      prefs.setValue(Utils.CB_VIEW_VOTE,String.valueOf(hasVote)) ;  
      prefs.setValue(Utils.CB_QUERY_ISNEW, String.valueOf(isNewquery)) ; 
      prefs.setValue(Utils.CB_QUERY_TYPE, queryType) ;
      prefs.setValue(Utils.CB_QUERY_STORE, queryPath) ;
      prefs.setValue(Utils.CB_QUERY_STATEMENT, query) ;
      prefs.store() ; 
      uiBrowseContentPortlet.getChild(UIBrowseContainer.class).setShowDocumentDetail(false) ;
      uiForm.isEdit_ = false ;
      uiForm.getAncestorOfType(UIConfigTabPane.class).isNewConfig_ = false ;
    }
  }  


  public static class ChangeStatusActionListener extends EventListener<UIQueryConfig>{
    public void execute(Event<UIQueryConfig> event) throws Exception {
      UIQueryConfig uiForm = event.getSource();
      UIFormSelectBox queryStatuField = uiForm.getChildById(UINewConfigForm.FIELD_QUERYSTATUS) ;
      UIFormSelectBox queryTypeField = uiForm.getChildById(UINewConfigForm.FIELD_QUERYTYPE) ;
      UIFormSelectBox queryLangField = uiForm.getUIFormSelectBox(UINewConfigForm.FIELD_QUERYLANG);
      uiForm.onchangeAction(queryStatuField.getValue(), queryTypeField.getValue(), queryLangField.getValue(), null, null) ;
      uiForm.isEdit_ = true ;
      uiForm.getAncestorOfType(UIConfigTabPane.class).isNewConfig_ = true ;
    }
  }

  public static class ChangeTypeActionListener extends EventListener<UIQueryConfig>{
    public void execute(Event<UIQueryConfig> event) throws Exception {
      UIQueryConfig uiForm = event.getSource() ;
      UIFormSelectBox queryStatuField = uiForm.getChildById(UINewConfigForm.FIELD_QUERYSTATUS) ;
      UIFormSelectBox queryLangField = uiForm.getUIFormSelectBox(UINewConfigForm.FIELD_QUERYLANG) ;
      UIFormSelectBox queryTypeField = uiForm.getUIFormSelectBox(UINewConfigForm.FIELD_QUERYTYPE) ;
      uiForm.onchangeAction(queryStatuField.getValue(), queryTypeField.getValue(), queryLangField.getValue(), null, null) ;
      uiForm.isEdit_ = true ;
      uiForm.getAncestorOfType(UIConfigTabPane.class).isNewConfig_ = true ;
    }
  }

  public static class ChangeLangActionListener extends EventListener<UIQueryConfig>{
    public void execute(Event<UIQueryConfig> event) throws Exception {
      UIQueryConfig uiForm = event.getSource() ;
      UIFormSelectBox queryStatusField = uiForm.getChildById(UINewConfigForm.FIELD_QUERYSTATUS) ;
      UIFormSelectBox queryTypeField = uiForm.getChildById(UINewConfigForm.FIELD_QUERYTYPE) ;
      UIFormSelectBox queryLangField = uiForm.getUIFormSelectBox(UINewConfigForm.FIELD_QUERYLANG);
      uiForm.onchangeAction(queryStatusField.getValue(), queryTypeField.getValue(), queryLangField.getValue(), null, null) ;
      uiForm.isEdit_ = true ;
      uiForm.getAncestorOfType(UIConfigTabPane.class).isNewConfig_ = true ;
    }
  }

  public static class AddActionListener extends EventListener<UIQueryConfig>{
    public void execute(Event<UIQueryConfig> event) throws Exception {
      UIQueryConfig uiForm = event.getSource() ;
      UIConfigTabPane uiConfigTabPane = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      uiConfigTabPane.isNewConfig_ = true ;
      uiConfigTabPane.showNewConfigForm(true);
    }
  }

  public static class CancelActionListener extends EventListener<UIQueryConfig>{
    public void execute(Event<UIQueryConfig> event) throws Exception {
      UIQueryConfig uiForm = event.getSource() ;
      uiForm.isEdit_ = false ;
      UIConfigTabPane uiConfigTabPane = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      uiConfigTabPane.isNewConfig_ = false ;
    }
  }
  public static class BackActionListener extends EventListener<UIQueryConfig>{
    public void execute(Event<UIQueryConfig> event) throws Exception {
      UIQueryConfig uiForm = event.getSource() ;
      UIConfigTabPane uiConfigTabPane = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      uiForm.isEdit_ =  false ;
      uiConfigTabPane.isNewConfig_ = true;
      uiConfigTabPane.showNewConfigForm(false) ;
    }
  }
  public static class EditActionListener extends EventListener<UIQueryConfig>{
    public void execute(Event<UIQueryConfig> event) throws Exception {
      UIQueryConfig uiForm = event.getSource() ;
      uiForm.isEdit_ = true ;
      UIConfigTabPane uiConfigTabPane = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      uiConfigTabPane.isNewConfig_ = false ;
    }
  }
}

