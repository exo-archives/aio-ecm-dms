/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.search;

import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.pham@exoplatform.com
 * Oct 2, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIContentNameSearch.SearchActionListener.class),
      @EventConfig(listeners = UIContentNameSearch.CancelActionListener.class, phase=Phase.DECODE)
    }
)
public class UIContentNameSearch extends UIForm {

  private static String KEYWORD = "keyword".intern();  
  private static String SEARCH_LOCATION = "location".intern();
  private static final String ROOT_PATH_SQL_QUERY = "select * from nt:base where jcr:path like '%/$1' order by exo:dateCreated DESC,jcr:primaryType DESC";
  private static final String PATH_SQL_QUERY = "select * from nt:base where jcr:path like '$0/%/$1' or jcr:path like '$0/$1' order by exo:dateCreated DESC,jcr:primaryType DESC";
  
  public UIContentNameSearch() throws Exception {
    addChild(new UIFormInputInfo(SEARCH_LOCATION,null,null)) ;
    addChild(new UIFormStringInput(KEYWORD,null));
  }
  
  public void setLocation(String location) {
    getUIFormInputInfo(SEARCH_LOCATION).setValue(location);
  }

  static public class SearchActionListener extends EventListener<UIContentNameSearch> {
    public void execute(Event<UIContentNameSearch> event) throws Exception {
      UIContentNameSearch contentNameSearch = event.getSource();
      String keyword = contentNameSearch.getUIStringInput(KEYWORD).getValue() ;      
      //TODO need review this code. should use validator for text field
      String[] arrFilterChar = {"&", "$", "@", ":","]", "[", "*", "%", "!"} ;
      UIApplication application = contentNameSearch.getAncestorOfType(UIApplication.class);
      if(keyword == null || keyword.length() ==0) {
        application.addMessage(new ApplicationMessage("UIContentNameSearch.msg.keyword-not-allowed", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(application.getUIPopupMessages()) ;
        return ;
      }
      for(String filterChar : arrFilterChar) {
        if(keyword.indexOf(filterChar) > -1) {
          application.addMessage(new ApplicationMessage("UIContentNameSearch.msg.keyword-not-allowed", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(application.getUIPopupMessages()) ;
          return ;
        }
      }
      UIJCRExplorer explorer = contentNameSearch.getAncestorOfType(UIJCRExplorer.class);
      String currentNodePath = explorer.getCurrentNode().getPath();
      String statement = null ;
      if("/".equalsIgnoreCase(currentNodePath)) {
        statement = StringUtils.replace(ROOT_PATH_SQL_QUERY,"$1",keyword);
      }else {
        statement = StringUtils.replace(PATH_SQL_QUERY,"$0",currentNodePath);
        statement = StringUtils.replace(statement,"$1",keyword);
      }
      QueryManager queryManager = explorer.getCurrentNode().getSession().getWorkspace().getQueryManager();
      UIECMSearch uiECMSearch = contentNameSearch.getAncestorOfType(UIECMSearch.class) ; 
      UISearchResult uiSearchResult = uiECMSearch.getChild(UISearchResult.class) ;      
      Query query = queryManager.createQuery(statement,Query.SQL);
      long startTime = System.currentTimeMillis();
      QueryResult queryResult = query.execute();
      uiSearchResult.setQueryResults(queryResult);
      uiSearchResult.updateGrid();
      long time = System.currentTimeMillis() - startTime;
      uiSearchResult.setSearchTime(time);
      uiECMSearch.setRenderedChild(UISearchResult.class);
      contentNameSearch.getUIFormInputInfo(SEARCH_LOCATION).setValue(currentNodePath);
    }  
  }

  static public class CancelActionListener extends EventListener<UIContentNameSearch> {   
    public void execute(Event<UIContentNameSearch> event) throws Exception {    
      event.getSource().getAncestorOfType(UIJCRExplorer.class).cancelAction() ;
    }    
  }

}
