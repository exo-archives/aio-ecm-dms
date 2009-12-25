/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
 *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.nodetype;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.jcr.nodetype.NodeType;

import org.exoplatform.ecm.webui.nodetype.selector.UINodeTypeSearch;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hoang Van Hung
 *          hunghvit@gmail.com
 * Dec 23, 2009  
 */

@ComponentConfig (
  lifecycle = UIFormLifecycle.class, 
  events = {
            @EventConfig(listeners = UINodeTypeSearchForm.SearchNodeTypeActionListener.class)
           }
)
public class UINodeTypeSearchForm extends UIForm {

  public UINodeTypeSearchForm() throws Exception {
    addChild(UINodeTypeSearch.class, null, "NodeTypeSearch").init();
  }
  
  public static class SearchNodeTypeActionListener extends EventListener<UINodeTypeSearchForm> {
    public void execute(Event<UINodeTypeSearchForm> event) throws Exception {
      UINodeTypeSearchForm uiForm = event.getSource();
      UIFormStringInput uiInputNodeType = (UIFormStringInput)uiForm.findComponentById("NodeTypeText");
      String nodeTypeName = uiInputNodeType.getValue();
      if (nodeTypeName == null || nodeTypeName.length() == 0) return;
      nodeTypeName = (nodeTypeName.contains("*") && !nodeTypeName.contains(".*")) ? nodeTypeName.replace("*", ".*") : nodeTypeName;
      Pattern p = Pattern.compile(".*".concat(nodeTypeName.trim()).concat(".*"), Pattern.CASE_INSENSITIVE);
      UINodeTypeManager uiNodeTypeManager = uiForm.getAncestorOfType(UINodeTypeManager.class);
      UINodeTypeList uiNodeTypeList = uiNodeTypeManager.getChild(UINodeTypeList.class);
      List<NodeType> lstAllNodetype = uiNodeTypeList.getAllNodeTypes();
      List<NodeType> lstNodetype = new ArrayList<NodeType>();
      for (NodeType nodeType : lstAllNodetype) {
        if (p.matcher(nodeType.getName()).find()) {
          lstNodetype.add(nodeType);
        }
      }
      uiNodeTypeList.refresh(null, 1, lstNodetype);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiNodeTypeManager) ;
    }
  }
}
