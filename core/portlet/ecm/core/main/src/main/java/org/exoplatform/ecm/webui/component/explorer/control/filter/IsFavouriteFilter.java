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
package org.exoplatform.ecm.webui.component.explorer.control.filter;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Value;

import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.webui.ext.filter.UIExtensionAbstractFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilterType;

/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Anh Vu
 *          anhvurz90@gmail.com
 * Oct 16, 2009  
 * 10:20:29 AM
 */
public class IsFavouriteFilter extends UIExtensionAbstractFilter {
	
	public IsFavouriteFilter() {
		this(null);
	}
	
	public IsFavouriteFilter(String messageKey) {
		super(messageKey, UIExtensionFilterType.MANDATORY);
	}
	
	public static boolean isFavourite(Node node) throws Exception {
		try {
			Property favouriter = node.getProperty(Utils.EXO_FAVOURITER);
			Value[] values = favouriter.getValues();
			String userName = node.getSession().getUserID();
			for (Value v : values) {
				if (userName.equals(v.getString()))
					return true;
			}
			return false;
		} catch (Exception ex) {
			return false;
		}
	}
	public boolean accept(Map<String, Object> context) throws Exception {
	    if (context == null) return true;
	    Node currentNode = (Node) context.get(Node.class.getName());
	    return isFavourite(currentNode);
	}
	
	public void onDeny(Map<String, Object> context) throws Exception {  }    
}
