/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ecm.webui.component.explorer.publication;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.services.ecm.publication.NotInPublicationLifecycleException;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponentDecorator;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jun 26, 2008 1:16:08 AM
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/explorer/publication/UIPublicationLogList.gtmpl",
    events = {
        @EventConfig(listeners = UIPublicationLogList.CloseActionListener.class)
    }
)
public class UIPublicationLogList extends UIComponentDecorator {
  
  private UIPageIterator uiPageIterator_ ;
  
  public UIPublicationLogList() throws Exception {
    uiPageIterator_ = createUIComponent(UIPageIterator.class, null, "PublicationLogListIterator");
    setUIComponent(uiPageIterator_) ;
  }
  
  public List<HistoryBean> getLog() throws NotInPublicationLifecycleException, Exception {
    Node currentNode = getAncestorOfType(UIJCRExplorer.class).getCurrentNode();
    PublicationService publicationService = getApplicationComponent(PublicationService.class);
    String[][] array = publicationService.getLog(currentNode);
    List<HistoryBean> list = new ArrayList<HistoryBean>();    
    for (int i = 0; i < array.length; i++) {
      HistoryBean bean = new HistoryBean();
      String[] currentLog=array[i];
      bean.setDate(currentLog[0]);
      bean.setNewState(currentLog[1]);
      bean.setUser(currentLog[2]);
      String[] values=new String[currentLog.length-4];
      for (int j=4;j<currentLog.length;j++) {
        values[j-4]=currentLog[j];
      }
      String description=publicationService.getLocalizedAndSubstituteLog(Util.getUIPortal().getAncestorOfType(UIPortalApplication.class).getLocale(), currentLog[3], values);
      bean.setDescription(description);
      list.add(bean); 
    }
    return list;
  }
  
  @SuppressWarnings("unchecked")
  public void updateGrid() throws Exception {   
    ObjectPageList objPageList = new ObjectPageList(getLog(), 10) ;
    uiPageIterator_.setPageList(objPageList) ;
  }
  
  public UIPageIterator getUIPageIterator() { return uiPageIterator_ ; }
  
  public List getLogList() throws Exception { return uiPageIterator_.getCurrentPageData() ; }

  public String[] getActions() {return new String[]{"Close"} ;}
  
  static public class CloseActionListener extends EventListener<UIPublicationLogList> {
    public void execute(Event<UIPublicationLogList> event) throws Exception {      
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction();
    }
  }
  
  public class HistoryBean {
    private String date;
    private String newState;
    private String user;
    private String description;
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getNewState() { return newState; }
    public void setNewState(String newState) { this.newState = newState; }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
  }
}
