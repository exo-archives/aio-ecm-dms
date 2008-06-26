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

import javax.jcr.Node;
import javax.jcr.Value;
import javax.jcr.version.VersionHistory;

import org.exoplatform.ecm.jcr.model.VersionNode;
import org.exoplatform.services.ecm.publication.plugins.staticdirect.StaticAndDirectPublicationPlugin;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jun 26, 2008 9:22:51 AM
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/explorer/publication/UIVersionTreeList.gtmpl",
    events = {
        @EventConfig(listeners = UIVersionTreeList.SelectActionListener.class)
    }
)
public class UIVersionTreeList extends UIContainer {
  
  protected VersionNode rootVersion_ ;
  protected VersionNode curentVersion_;
  protected Node node_ ;
  
  public UIVersionTreeList() throws Exception {
    
  }
  
  public VersionNode getRootVersionNode() throws Exception {  return rootVersion_ ; }
  
  public VersionNode getCurrentVersionNode() { return curentVersion_ ; }
  
  public Node getCurrentNode() { return node_ ; }
  
  public void initVersion(Node currentNode) throws Exception {
    node_ = currentNode;   
    rootVersion_ = new VersionNode(node_.getVersionHistory().getRootVersion());
    curentVersion_ = rootVersion_;
  }
  
  public String[] getVersionLabels(VersionNode version) throws Exception {
    VersionHistory vH = node_.getVersionHistory();
    return vH.getVersionLabels(version.getVersion());   
  }
  
  public boolean isBaseVersion(VersionNode versionNode) throws Exception {
    if (node_.getBaseVersion().getName().equals(versionNode.getVersion().getName())) return true ;
    return false ;
  }
  
  public boolean isSelectedVersion(VersionNode versionNode) throws Exception {
    if(curentVersion_.equals(versionNode)) return true ;
    return false ;
  }
  
  public boolean isPublised(VersionNode versionNode) throws Exception {
    Value[] publicationStates =  node_.getProperty(StaticAndDirectPublicationPlugin.VERSIONS_PUBLICATION_STATES).getValues() ;
    for(Value value : publicationStates) {
      String[] arrPublicationState = value.getString().split(",") ;
      for(int i=0; i < arrPublicationState.length; i++) {
        if(arrPublicationState[0].equals(versionNode.getVersion().getUUID())) {
          if(arrPublicationState[1].equals(StaticAndDirectPublicationPlugin.PUBLISHED)) return true ;
          return true ;
        }
      }
    }
    return false ;
  }
  
  static public class SelectActionListener extends EventListener<UIVersionTreeList> {
    public void execute(Event<UIVersionTreeList> event) throws Exception {
      UIVersionTreeList uiVersionTreeList = event.getSource() ;
      String versionPath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiVersionTreeList.curentVersion_  = uiVersionTreeList.rootVersion_.findVersionNode(versionPath) ;
      UIPublicationContainer uiPublicationContainer = uiVersionTreeList.getParent() ;
      UIPublicationForm uiForm = uiPublicationContainer.getChild(UIPublicationForm.class) ;
      uiForm.updateForm(uiVersionTreeList.curentVersion_) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPublicationContainer.getParent()) ;
    }
  }
}
