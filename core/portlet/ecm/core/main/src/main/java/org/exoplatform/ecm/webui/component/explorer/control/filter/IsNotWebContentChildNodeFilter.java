/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.ecm.webui.component.explorer.control.filter;

import java.util.Map;

import javax.jcr.Node;

import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilterType;

/**
 * Created by The eXo Platform SAS
 * Author : Dang Viet Ha
 *          ha.dangviet@exoplatform.com
 * Dec 28, 2010  
 */
public class IsNotWebContentChildNodeFilter implements UIExtensionFilter {

  public boolean accept(Map<String, Object> context) throws Exception {
    Node currentNode = (Node) context.get(Node.class.getName());
    Node parrentNode = currentNode.getParent();
    while (!((NodeImpl) parrentNode).isRoot()) {
      if (parrentNode.isNodeType(Utils.EXO_WEBCONTENT)) {
        return false;
      }
      parrentNode = parrentNode.getParent();
    }
    return true;
  }

  public UIExtensionFilterType getType() {
    return UIExtensionFilterType.MANDATORY;
  }
  
  public void onDeny(Map<String, Object> context) throws Exception {
  }

}
