/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.browsecontent.UICBSearchResults.ResultData;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.component.UIApplication;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormCheckBoxInput;
import org.exoplatform.webui.component.UIFormSelectBox;
import org.exoplatform.webui.component.UIFormStringInput;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.component.model.SelectItemOption;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Dec 22, 2006 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/webui/component/browse/UICBSearchForm.gtmpl",
    events = {
      @EventConfig(listeners = UICBSearchForm.SearchActionListener.class),
      @EventConfig(listeners = UICBSearchForm.ChangeTypeActionListener.class)
    }
)
public class UICBSearchForm extends UIForm {
  final static  private String FIELD_SEARCHVALUE = "inputValue" ;
  final static  private String FIELD_OPTION = "option" ;
  final static  private String FIELD_CB_REF = "referencesDoc" ;
  final static  private String FIELD_CB_CHILD = "childDoc" ;
  
  public static final String CATEGORY_SEARCH = "Category" ;
  public static final String DOCUMENT_SEARCH = "Content" ;  
  public static final String CATEGORY_QUERY = "select * from $0 where jcr:path like '%/$1[%]' "  ;
  public static final String DOCUMENT_QUERY = "select * from $0 where contains(*, '$1') AND jcr:path like '$2[%]/%' ";  
  public boolean isDocumentType = true ;
  protected long duration_ = 0 ; 
  public UICBSearchForm() {
    UIFormSelectBox selectType = new UIFormSelectBox(FIELD_OPTION, FIELD_OPTION, getOptions()) ;
    selectType.setOnChange("ChangeType") ;
    selectType.setValue(DOCUMENT_SEARCH) ;
    addChild(new UIFormStringInput(FIELD_SEARCHVALUE, FIELD_SEARCHVALUE, null)) ;
    addChild(selectType) ;
    UIFormCheckBoxInput cbRef = new UIFormCheckBoxInput<Boolean>(FIELD_CB_REF, FIELD_CB_REF, null) ; 
    UIFormCheckBoxInput cbRel = new UIFormCheckBoxInput<Boolean>(FIELD_CB_CHILD, FIELD_CB_CHILD, null) ;
    addChild(cbRef.setRendered(isDocumentType)) ;
    addChild(cbRel.setRendered(isDocumentType)) ;
  }
  
  public List<SelectItemOption<String>> getOptions() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>(DOCUMENT_SEARCH,DOCUMENT_SEARCH)) ;
    options.add(new SelectItemOption<String>(CATEGORY_SEARCH,CATEGORY_SEARCH)) ;
    return options ;
  } 
  
  public long searchTime() { return duration_; }
  
  public List<ResultData> searchByCategory(String keyword, Node currentNode) throws Exception{
    List<ResultData> resultList = new ArrayList<ResultData>() ;
    ResultData result ;
    UIBrowseContainer uiContainer = getAncestorOfType(UIBrowseContainer.class) ;
    Session session = uiContainer.getSession() ;
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    duration_ = 0 ;
    String statement = StringUtils.replace(CATEGORY_QUERY, "$1", keyword);
    for(String type : Utils.CATEGORY_NODE_TYPES) {            
      String queryStatement = StringUtils.replace(statement, "$0", type);
      Query query = queryManager.createQuery(queryStatement, Query.SQL);
      long beforeTime = System.currentTimeMillis();
      try{
        QueryResult queryResult = query.execute();
        long searchTime = System.currentTimeMillis() - beforeTime;
        duration_ += searchTime ;
        NodeIterator iter = queryResult.getNodes() ;
        while(iter.hasNext()) {
          Node node = iter.nextNode() ;
          if(node.getPath().startsWith(currentNode.getPath())) {                    
            result = new ResultData(node.getName(), node.getPath()) ;
            resultList.add(result) ; 
          }
        }
      }catch(Exception e){
        return null ;
      }
    }
    UISearchController uiController = uiContainer.getChild(UISearchController.class) ;
    uiController.setSearchTime(duration_) ;
    uiController.setResultRecord(resultList.size()) ;
    return resultList ;
  } 

  @SuppressWarnings("unused")
  public List<ResultData> searchDocument(String keyword, boolean reference,
      boolean relation, Node currentNode) throws Exception { 
    String nodePath = currentNode.getPath() ;
    if(nodePath.equals("/")) nodePath = "" ;
    List<ResultData> resultList = new ArrayList<ResultData>() ;
    ResultData result ;
    String statement = StringUtils.replace(DOCUMENT_QUERY, "$1", keyword) ;
    statement = StringUtils.replace(statement, "$2", nodePath) ;    
    UIBrowseContainer uiContainer = getAncestorOfType(UIBrowseContainer.class) ;
    Session session = uiContainer.getSession() ;
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    duration_ = 0 ;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    List<String> documentNodeTypes = templateService.getDocumentTemplates() ;
    documentNodeTypes.add("nt:resource") ;
    for(String ntDocument : documentNodeTypes) {            
      String queryStatement = StringUtils.replace(statement, "$0", ntDocument) ;      
      Query query = queryManager.createQuery(queryStatement, Query.SQL);
      long beforeTime = System.currentTimeMillis();
      try{
        QueryResult queryResult = query.execute() ;
        long searchTime = System.currentTimeMillis() - beforeTime;
        duration_ += searchTime ;
        NodeIterator iter = queryResult.getNodes() ;
        while (iter.hasNext()) {
          Node node = iter.nextNode() ;
          if(ntDocument.equals("nt:resource")) {
            Node paNode = node.getParent() ;
            if(documentNodeTypes.contains(paNode.getPrimaryNodeType().getName())) {
              result = new ResultData(paNode.getName(), paNode.getPath()) ;
              resultList.add(result) ;
            }
          } else {
            result = new ResultData(node.getName(), node.getPath()) ;
            resultList.add(result) ;
          }
        }
      } catch (Exception e) {
        return null ;
      }
    }
    
    UISearchController uiController = uiContainer.getChild(UISearchController.class) ;
    uiController.setSearchTime(duration_) ;
    uiController.setResultRecord(resultList.size()) ;
    return resultList ;
  }
  
  public void reset() {
    getUIStringInput(FIELD_SEARCHVALUE).setValue("") ;
    getUIFormSelectBox(FIELD_OPTION).setOptions(getOptions()) ;
    getUIFormCheckBoxInput(FIELD_CB_REF).setRendered(isDocumentType) ;
    getUIFormCheckBoxInput(FIELD_CB_CHILD).setRendered(isDocumentType) ;
  }
  
  static public class ChangeTypeActionListener extends EventListener <UICBSearchForm> {
    public void execute(Event<UICBSearchForm>  event) throws Exception {
      UICBSearchForm uiForm = event.getSource() ;
      String searchType = uiForm.getUIFormSelectBox(FIELD_OPTION).getValue() ;
      uiForm.isDocumentType = searchType.equals(DOCUMENT_SEARCH) ; 
      uiForm.getUIFormCheckBoxInput(FIELD_CB_REF).setRendered(uiForm.isDocumentType) ;
      uiForm.getUIFormCheckBoxInput(FIELD_CB_CHILD).setRendered(uiForm.isDocumentType) ;
    }   
  }

  static  public class SearchActionListener extends EventListener<UICBSearchForm> {
    public void execute(Event<UICBSearchForm> event) throws Exception {
      UICBSearchForm uiForm = event.getSource() ;
      UIBrowseContainer container = uiForm.getAncestorOfType(UIBrowseContainer.class) ;
      Node currentNode = container.getRootNode() ;
      String keyword = uiForm.getUIStringInput(FIELD_SEARCHVALUE).getValue();
      String type = uiForm.getUIFormSelectBox(FIELD_OPTION).getValue() ;            
      List<ResultData> queryResult ;
      UICBSearchResults searchResults = container.findFirstComponentOfType(UICBSearchResults.class) ;
      UIApplication app = uiForm.getAncestorOfType(UIApplication.class) ;
      if(keyword == null || keyword.trim().length() == 0) {          
        app.addMessage(new ApplicationMessage("UICBSearchForm.msg.not-empty", null)) ;
        return ;
      }
      if(type.equals(CATEGORY_SEARCH)) {
        queryResult = uiForm.searchByCategory(keyword, currentNode) ;
      } else {
        boolean reference = uiForm.getUIFormCheckBoxInput(FIELD_CB_REF).isChecked() ;
        boolean relation = uiForm.getUIFormCheckBoxInput(FIELD_CB_CHILD).isChecked() ;   
        queryResult = uiForm.searchDocument(keyword, reference, relation, currentNode) ;
      }
      if(queryResult == null){
        app.addMessage(new ApplicationMessage("UICBSearchForm.msg.invalid-keyword", null)) ;
        return ;
      }
      searchResults.updateGrid(queryResult) ;
    }
  }
}
