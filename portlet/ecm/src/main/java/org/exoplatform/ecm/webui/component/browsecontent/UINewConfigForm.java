/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.browsecontent;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.nodetype.NodeType;

import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormSelectBox;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.component.model.SelectItemOption;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Oct 25, 2006 3:23:00 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/component/UIFormWithTitle.gtmpl",
    events = {
      @EventConfig(phase = Phase.DECODE, listeners = UINewConfigForm.NextActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UINewConfigForm.CloseActionListener.class)
    }
)

public class UINewConfigForm extends UIForm {
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
  final static public String[] DEFAULT_ACTION = new String[]{"Edit", "Add", "Close"} ;
  final static public String[] NORMAL_ACTION = new String[]{"Save", "Cancel"} ;
  final static public String[] ADD_NEW_ACTION = new String[]{"Save", "Reset", "Cancel"} ;
  
  public UINewConfigForm() throws Exception {
    addChild(new UIFormSelectBox(FIELD_WORKSPACE, FIELD_WORKSPACE, getWorkSpaceOption())) ;
    addChild( new UIFormSelectBox(FIELD_BROWSETYPE, FIELD_BROWSETYPE, getBrowseTypeOption())) ;
    setActions(new String[] {"Next", "Close"}) ;
  }

  public List<SelectItemOption<String>> getWorkSpaceOption() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    String[] workspaceNames = repositoryService.getRepository().getWorkspaceNames() ;
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

  @SuppressWarnings("unchecked")
  public boolean isDocument(NodeType nodeType) throws Exception{
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;    
    List<String> listDocumentNodeType = templateService.getDocumentTemplates() ;
    String name = nodeType.getName() ;
    for(String documentType:listDocumentNodeType) {
      if(documentType.equals(name)) return true ;
    }
    return false ;
  }
 
  public static class CloseActionListener extends EventListener<UINewConfigForm>{
    public void execute(Event<UINewConfigForm> event) throws Exception {
      UINewConfigForm uiForm = event.getSource() ;
      UIBrowseContentPortlet uiBrowseContentPortlet = 
        uiForm.getAncestorOfType(UIBrowseContentPortlet.class) ;
      uiBrowseContentPortlet.removeChild(UIConfigTabPane.class) ;
      UIBrowseContainer uiContainer  = uiBrowseContentPortlet.getChild(UIBrowseContainer.class) ;
      uiContainer.setRendered(true) ; 
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
