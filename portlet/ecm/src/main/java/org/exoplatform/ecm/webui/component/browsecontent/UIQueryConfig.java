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
import org.exoplatform.portal.component.view.Util;
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
  private boolean isAddNewQuery_ = true ;

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
        String role = membership.getMembershipType() + ":" + membership.getGroupId() ;
        roles_.add(role) ;      
      } 
    }
  }

  public PortletPreferences getPortletPreferences() {    
    return getAncestorOfType(UIBrowseContentPortlet.class).getPortletPreferences() ;
  }

  public void initForm(PortletPreferences preference, String repository, String workSpace, boolean isAddNew, 
      boolean isEditable) throws Exception {
    String queryLang = "" ;
    String queryType = "" ;
    String queryStore = null ;
    String query = "" ;
    String queryNew = "true" ;
    String hasComment = "false" ;
    String hasVote = "false" ;
    String hasTagMap = "false" ;
    String itemPerPage = "20" ;
    try {
      Integer.parseInt(preference.getValue(Utils.CB_NB_PER_PAGE, "")) ;
      itemPerPage = (preference.getValue(Utils.CB_NB_PER_PAGE, "")) ;
    }
    catch (Exception  e) {
    }
    if(isAddNew) { 
      setActions(UINewConfigForm.ADD_NEW_ACTION) ;
      query = sqlDefault_ ;
    } else {
      isEditable = false ;
      queryNew = preference.getValue(Utils.CB_QUERY_ISNEW, "") ;
      queryType = preference.getValue(Utils.CB_QUERY_TYPE, "") ;
      queryStore = preference.getValue(Utils.CB_QUERY_STORE, "") ;
      query = preference.getValue(Utils.CB_QUERY_STATEMENT, "") ;
      queryLang = preference.getValue(Utils.CB_QUERY_LANGUAGE, "") ;
      itemPerPage = preference.getValue(Utils.CB_NB_PER_PAGE, "") ;
      hasTagMap  = preference.getValue(Utils.CB_VIEW_TAGMAP, "") ;
      hasComment = preference.getValue(Utils.CB_VIEW_COMMENT, "") ;
      hasVote = preference.getValue(Utils.CB_VIEW_VOTE, "") ;
    }
    UIFormStringInput workSpaceField = getChildById(UINewConfigForm.FIELD_WORKSPACE) ;
    workSpaceField.setValue(workSpace) ;
    workSpaceField.setEditable(false) ;
    UIFormStringInput repositoryField = getChildById(UINewConfigForm.FIELD_REPOSITORY) ;
    repositoryField.setValue(repository) ;
    repositoryField.setEditable(false) ;
    UIFormSelectBox queryStatusField = getChildById(UINewConfigForm.FIELD_QUERYSTATUS) ;
    queryStatusField.setOptions(getQueryStatus()) ;
    queryStatusField.setOnChange("ChangeStatus") ;
    queryStatusField.setEnable(isEditable) ;
    UIFormSelectBox queryLangField = getChildById(UINewConfigForm.FIELD_QUERYLANG) ;
    queryLangField.setOptions(getQueryLang()) ;
    queryLangField.setValue(queryLang) ;
    queryLangField.setOnChange("ChangeLang") ;
    queryLangField.setEnable(isEditable) ;
    UIFormTextAreaInput queryFied = getChildById(UINewConfigForm.FIELD_QUERY) ;
    UIFormSelectBox queryTypeField = getChildById(UINewConfigForm.FIELD_QUERYTYPE) ;
    queryTypeField.setOptions(getQueryType()) ;
    queryTypeField.setOnChange("ChangeType") ;
    queryTypeField.setEnable(isEditable) ;
    UIFormSelectBox queryStoreField = getChildById(UINewConfigForm.FIELD_QUERYSTORE) ;
    setQueryValue(queryStoreField, queryLang, queryType, queryStore) ;  
    queryStoreField.setEnable(isEditable) ;
    if(Boolean.parseBoolean(queryNew)) {    
      queryTypeField.setRendered(false) ;
      queryStoreField.setRendered(false) ;
      queryFied.setRendered(true) ;
    } else {
      queryTypeField.setRendered(true) ;
      queryStoreField.setRendered(true) ;
      queryFied.setRendered(false) ;
      queryStatusField.setValue(EXITING_QUERY) ;
      isAddNewQuery_ = Boolean.parseBoolean(queryNew);
      queryTypeField.setValue(queryType) ;
      setQueryValue(queryStoreField, queryLang, queryType, queryStore) ;
    }   
    UIFormTextAreaInput queryField = getChildById(UINewConfigForm.FIELD_QUERY) ;
    queryField.setValue(query) ;
    queryField.setEditable(isEditable) ;
    UIFormSelectBox templateField = getChildById(UINewConfigForm.FIELD_TEMPLATE) ;
    templateField.setOptions(getQueryTemplate()) ;
    templateField.setEnable(isEditable) ;
    UIFormStringInput numbPerPageField = getChildById(UINewConfigForm.FIELD_ITEMPERPAGE) ;
    numbPerPageField.setValue(itemPerPage) ;
    numbPerPageField.setEditable(isEditable) ;
    UIFormSelectBox detailtemField = getChildById(UINewConfigForm.FIELD_DETAILBOXTEMP) ;
    UIConfigTabPane uiConfigTabPane = getAncestorOfType(UIConfigTabPane.class) ;
    detailtemField.setOptions(uiConfigTabPane.getBoxTemplateOption()) ;
    detailtemField.setEnable(isEditable) ;
    UIFormCheckBoxInput enableTagMapField = getChildById(UINewConfigForm.FIELD_ENABLETAGMAP) ;
    enableTagMapField.setChecked(Boolean.parseBoolean(hasTagMap)) ;
    enableTagMapField.setEnable(isEditable) ;
    UIFormCheckBoxInput enableCommentField = getChildById(UINewConfigForm.FIELD_ENABLECOMMENT) ;
    enableCommentField.setChecked(Boolean.parseBoolean(hasComment)) ;
    enableCommentField.setEnable(isEditable) ;  
    UIFormCheckBoxInput enableVoteField = getChildById(UINewConfigForm.FIELD_ENABLEVOTE) ;
    enableVoteField.setEnable(isEditable) ; 
    enableVoteField.setChecked(Boolean.parseBoolean(hasVote)) ;
  }

  public void editForm(boolean isEditable) throws Exception {
    UIFormSelectBox templateField = getChildById(UINewConfigForm.FIELD_TEMPLATE) ;
    templateField.setEnable(isEditable) ;
    PortletPreferences preference = getPortletPreferences() ;
    String queryStore = preference.getValue(Utils.CB_QUERY_STORE, "") ;
    String queryNew = preference.getValue(Utils.CB_QUERY_ISNEW, "") ;
    String queryType = preference.getValue(Utils.CB_QUERY_TYPE, "") ;
    String query = preference.getValue(Utils.CB_QUERY_STATEMENT, "") ;
    UIFormSelectBox queryStatusField = getUIFormSelectBox(UINewConfigForm.FIELD_QUERYSTATUS) ;
    if(Boolean.parseBoolean(queryNew)) queryStatusField.setValue(NEW_QUERY) ;
    else queryStatusField.setValue(EXITING_QUERY) ;
    isAddNewQuery_ = Boolean.parseBoolean(queryNew) ;
    queryStatusField.setEnable(isEditable) ;
    UIFormSelectBox queryTypeField = getChildById(UINewConfigForm.FIELD_QUERYTYPE) ;
    if(Boolean.parseBoolean(queryType)) queryTypeField.setValue(PERSONAL_QUERY) ;
    else queryTypeField.setValue(SHARED_QUERY) ;
    queryTypeField.setEnable(isEditable) ;
    UIFormSelectBox queryLangField = getChildById(UINewConfigForm.FIELD_QUERYLANG) ;
    queryLangField.setValue(preference.getValue(Utils.CB_QUERY_LANGUAGE, "")) ;
    queryLangField.setEnable(isEditable) ;
    UIFormSelectBox queryValueField = getChildById(UINewConfigForm.FIELD_QUERYSTORE) ;
    queryValueField.setValue(queryStore) ;
    queryValueField.setEnable(isEditable) ;
    UIFormTextAreaInput queryField = getChildById(UINewConfigForm.FIELD_QUERY) ;
    queryField.setValue(query) ;
    queryField.setEnable(isEditable) ;
    UIFormStringInput numbPerPageField = getChildById(UINewConfigForm.FIELD_ITEMPERPAGE) ;
    numbPerPageField.setEditable(isEditable) ;
    UIFormSelectBox detailtemField = getChildById(UINewConfigForm.FIELD_DETAILBOXTEMP) ;
    detailtemField.setEnable(isEditable) ;
    UIFormCheckBoxInput enableTagMapField = getChildById(UINewConfigForm.FIELD_ENABLETAGMAP)  ;
    enableTagMapField.setEnable(isEditable) ;
    UIFormCheckBoxInput enableCommentField = getChildById(UINewConfigForm.FIELD_ENABLECOMMENT) ;
    UIFormCheckBoxInput enableVoteField = getChildById(UINewConfigForm.FIELD_ENABLEVOTE) ;
    enableCommentField.setEnable(isEditable) ;
    enableVoteField.setEnable(isEditable) ;
  }


  private void setQueryValue(UIFormSelectBox select, String queryLanguage, String queryType, String queryStore) throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    String repository = getAncestorOfType(UIBrowseContentPortlet.class).getPreferenceRepository() ;
    if(!isAddNewQuery_) {  
      QueryService qservice = getApplicationComponent(QueryService.class) ;   
      if(UIQueryConfig.PERSONAL_QUERY.equals(queryType)) {
        String username = Util.getPortalRequestContext().getRemoteUser() ;
        List<Query> queries = qservice.getQueries(username, repository);
        for(Query query : queries) {
          String path = query.getStoredQueryPath() ;
          if(query.getLanguage().equals(queryLanguage))
            options.add(new SelectItemOption<String>(path.substring(path.lastIndexOf("/")+ 1), path)) ;
        }
      }else {
        List<Node> queries = qservice.getSharedQueries(queryLanguage, roles_, repository);
        for(Node query : queries) {
          options.add(new SelectItemOption<String>(query.getName(), query.getPath())) ;
        }
      } 
    }
    select.setOptions(options) ;
    if(queryStore != null) select.setValue(queryStore) ;
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
  public void chageStatus () throws Exception {
    UIFormSelectBox queryStatus = getChildById(UINewConfigForm.FIELD_QUERYSTATUS) ;
    isAddNewQuery_ = UIQueryConfig.NEW_QUERY.equals(queryStatus.getValue()) ;    
    UIFormSelectBox queryStore = getChildById(UINewConfigForm.FIELD_QUERYSTORE) ;
    queryStore.setRendered(!isAddNewQuery_);
    UIFormSelectBox queryTypeField = getChildById(UINewConfigForm.FIELD_QUERYTYPE) ;
    queryTypeField.setRendered(!isAddNewQuery_);
    String queryLang = getUIFormSelectBox(UINewConfigForm.FIELD_QUERYLANG).getValue() ;
    UIFormTextAreaInput query = getChildById(UINewConfigForm.FIELD_QUERY) ;
    query.setRendered(isAddNewQuery_) ;
    if(!isAddNewQuery_) {
      UIFormSelectBox queryValue = getUIFormSelectBox(UINewConfigForm.FIELD_QUERYSTORE) ;
      setQueryValue(queryValue ,queryLang, queryTypeField.getValue(), queryStore.getValue()) ;
    } else {
      if(queryLang.equals(Query.XPATH)) {
        getUIFormTextAreaInput(UINewConfigForm.FIELD_QUERY).setValue(xpathDefault_) ;
      }
      if(queryLang.equals(Query.SQL)) {
        getUIFormTextAreaInput(UINewConfigForm.FIELD_QUERY).setValue(sqlDefault_) ;
      }
    }
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
      if((!uiForm.isAddNewQuery_)&&(queryValueField.isRendered())) {
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
      prefs.setValue(Utils.CB_QUERY_ISNEW, String.valueOf(uiForm.isAddNewQuery_)) ; 
      prefs.setValue(Utils.CB_QUERY_TYPE, queryType) ;
      prefs.setValue(Utils.CB_QUERY_STORE, queryPath) ;
      prefs.setValue(Utils.CB_QUERY_STATEMENT, query) ;
      prefs.store() ; 
      UIBrowseContainer container = 
        uiBrowseContentPortlet.findFirstComponentOfType(UIBrowseContainer.class) ;
      try{
        container.loadPortletConfig(prefs) ;
      } catch(Exception e) {
        UIApplication app = uiForm.getAncestorOfType(UIApplication.class) ;
        app.addMessage(new ApplicationMessage("UIQueryConfig.msg.save-error", null)) ;
        return ;
      }      
      uiForm.editForm(false) ;
      uiForm.setActions(UINewConfigForm.DEFAULT_ACTION) ;
    }
  }  

  public static class ChangeLangActionListener extends EventListener<UIQueryConfig>{
    public void execute(Event<UIQueryConfig> event) throws Exception {
      System.out.println("\n\nChange Type 1\n\n");
      UIQueryConfig uiForm = event.getSource() ;
      String queryLang = uiForm.getUIFormSelectBox(UINewConfigForm.FIELD_QUERYLANG).getValue() ;
      String queryType = uiForm.getUIFormSelectBox(UINewConfigForm.FIELD_QUERYTYPE).getValue() ;
      if(uiForm.isAddNewQuery_) {
        if(queryLang.equals(Query.XPATH)) {
          uiForm.getUIFormTextAreaInput(UINewConfigForm.FIELD_QUERY).setValue(uiForm.xpathDefault_) ;
        }
        if(queryLang.equals(Query.SQL)) {
          uiForm.getUIFormTextAreaInput(UINewConfigForm.FIELD_QUERY).setValue(uiForm.sqlDefault_) ;
        }
      } else {
        UIFormSelectBox queryValue = uiForm.getUIFormSelectBox(UINewConfigForm.FIELD_QUERYSTORE) ;
        uiForm.setQueryValue(queryValue, queryLang, queryType, null) ;
      }
    }
  }

  public static class ChangeStatusActionListener extends EventListener<UIQueryConfig>{
    public void execute(Event<UIQueryConfig> event) throws Exception {
      System.out.println("\n\nChange Type 2\n\n");
      UIQueryConfig uiForm = event.getSource();
      uiForm.chageStatus () ;
    }
  }

  public static class ChangeTypeActionListener extends EventListener<UIQueryConfig>{
    public void execute(Event<UIQueryConfig> event) throws Exception {
      System.out.println("\n\nChange Type 3\n\n");
      UIQueryConfig uiForm = event.getSource() ;
      String queryLang = uiForm.getUIFormSelectBox(UINewConfigForm.FIELD_QUERYLANG).getValue() ;
      String queryType = uiForm.getUIFormSelectBox(UINewConfigForm.FIELD_QUERYTYPE).getValue() ;
      UIFormSelectBox queryValue = uiForm.getUIFormSelectBox(UINewConfigForm.FIELD_QUERYSTORE) ;
      uiForm.setQueryValue(queryValue, queryLang, queryType, null) ;
    }
  }

  public static class AddActionListener extends EventListener<UIQueryConfig>{
    public void execute(Event<UIQueryConfig> event) throws Exception {
      UIQueryConfig uiForm = event.getSource() ;
      UIConfigTabPane uiConfigTabPane = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      uiConfigTabPane.loadNewConfig(true);
    }
  }

  public static class CancelActionListener extends EventListener<UIQueryConfig>{
    public void execute(Event<UIQueryConfig> event) throws Exception {
      UIQueryConfig uiForm = event.getSource() ;
      UIConfigTabPane uiConfigTabPane = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      uiConfigTabPane.getCurrentConfig() ;
    }
  }
  public static class BackActionListener extends EventListener<UIQueryConfig>{
    public void execute(Event<UIQueryConfig> event) throws Exception {
      UIQueryConfig uiForm = event.getSource() ;
      UIConfigTabPane uiConfigTabPane = uiForm.getAncestorOfType(UIConfigTabPane.class) ;
      uiConfigTabPane.loadNewConfig(false) ;
    }
  }
  public static class EditActionListener extends EventListener<UIQueryConfig>{
    public void execute(Event<UIQueryConfig> event) throws Exception {
      UIQueryConfig uiForm = event.getSource() ;
      uiForm.editForm(true) ; 
      uiForm.setActions(UINewConfigForm.NORMAL_ACTION) ;
    }
  }
}

