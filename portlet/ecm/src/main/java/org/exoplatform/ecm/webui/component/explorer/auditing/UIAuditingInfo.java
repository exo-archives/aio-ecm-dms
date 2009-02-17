/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.ecm.webui.component.explorer.auditing;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.utils.Utils;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.services.jcr.ext.audit.AuditHistory;
import org.exoplatform.services.jcr.ext.audit.AuditRecord;
import org.exoplatform.services.jcr.ext.audit.AuditService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Ly Dinh Quang
 *          quang.ly@exoplatform.com
 *			    xxx5669@yahoo.com
 * Sep 29, 2008  
 */
@ComponentConfig(
  template = "app:/groovy/webui/component/explorer/auditing/UIAuditingInfo.gtmpl",
  events = {
    @EventConfig(listeners = UIAuditingInfo.CloseActionListener.class)        
  }
)
public class UIAuditingInfo extends UIContainer implements UIPopupComponent {
  private UIPageIterator uiPageIterator_ ;
  
  public UIAuditingInfo() throws Exception {
    uiPageIterator_ = addChild(UIPageIterator.class, null, "AuditingInfoIterator");
  }

  public void activate() throws Exception { }
  public void deActivate() throws Exception { }

  public Node getCurrentNode() throws Exception { 
    return getAncestorOfType(UIJCRExplorer.class).getCurrentNode(); 
  }
  
  public UIPageIterator getUIPageIterator() { return uiPageIterator_; }
  
  public List getListRecords() throws Exception { return uiPageIterator_.getCurrentPageData(); }
  
  @SuppressWarnings("unchecked")
  public void updateGrid() throws Exception {   
    ObjectPageList objPageList = new ObjectPageList(getRecords(), 10);
    uiPageIterator_.setPageList(objPageList);
  }
  
  public String getVersionName(AuditRecord ar) {
    String versionName;
    try {      
      versionName = ar.getVersionName();
    } catch (Exception e) {
      versionName = null;
    }
    return versionName;
  }
  public List<AuditRecord> getRecords() throws Exception {
     List<AuditRecord> listRec = new ArrayList<AuditRecord>();
     Node currentNode = getCurrentNode(); 
     try {
      AuditService auditService = getApplicationComponent(AuditService.class);
      if (auditService.hasHistory(currentNode)){
        if (Utils.NT_FILE.equals(currentNode.getProperty(Utils.JCR_PRIMARYTYPE).getString())) { 
          currentNode = currentNode.getNode(Utils.JCR_CONTENT);
        } 
        AuditHistory auHistory = auditService.getHistory(currentNode);
        listRec = auHistory.getAuditRecords();     
      }
    } catch(Exception e){
      e.printStackTrace();
    }
    return listRec;
  }
  
  static public class CloseActionListener extends EventListener<UIAuditingInfo> {
    public void execute(Event<UIAuditingInfo> event) throws Exception {
      UIAuditingInfo uiAuditingInfo = event.getSource();
      UIJCRExplorer uiExplorer = uiAuditingInfo.getAncestorOfType(UIJCRExplorer.class);
      uiExplorer.cancelAction();
    }
  }
}
