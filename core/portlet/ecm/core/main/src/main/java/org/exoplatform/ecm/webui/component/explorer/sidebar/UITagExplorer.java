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
package org.exoplatform.ecm.webui.component.explorer.sidebar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;

import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIEditingTagsForm;
import org.exoplatform.services.cms.folksonomy.NewFolksonomyService;
import org.exoplatform.services.cms.impl.DMSConfiguration;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 26, 2007 4:59:40 PM
 */
@ComponentConfig(
    template =  "app:/groovy/webui/component/explorer/sidebar/UITagExplorer.gtmpl",
    events = {	@EventConfig(listeners = UITagExplorer.ViewTagActionListener.class),
    						@EventConfig(listeners = UITagExplorer.EditTagsActionListener.class)}
)
public class UITagExplorer extends UIContainer {
	
  public static final String PUBLIC_TAG_NODE_PATH = "exoPublicTagNode";
  public static final int PUBLIC = 0;
  public static final int PRIVATE = 1;
  private int tagScope;
  
  public UITagExplorer() throws Exception {
  }
  
  public int getTagScope() { return tagScope; }
  public void setTagScope(int scope) { tagScope = scope; }
  public static int getPublic() { return PUBLIC; }
  public static int getPrivate() { return PRIVATE; }
  
  public List<Node> getPrivateTagLink() throws Exception {
    NewFolksonomyService folksonomyService = getApplicationComponent(NewFolksonomyService.class) ;
    return folksonomyService.getAllPrivateTags(getUserName(), getRepository(), getWorkspace()) ;
  }
  
  public List<Node> getPublicTagLink() throws Exception {
    NewFolksonomyService folksonomyService = getApplicationComponent(NewFolksonomyService.class) ;
    NodeHierarchyCreator nodeHierarchyCreator = getApplicationComponent(NodeHierarchyCreator.class);
    String publicTagNodePath = nodeHierarchyCreator.getJcrPath(PUBLIC_TAG_NODE_PATH);  
    return folksonomyService.getAllPublicTags(publicTagNodePath, getRepository(), getWorkspace()) ;
  }
  
  public Map<String ,String> getTagStyle() throws Exception {
    NewFolksonomyService folksonomyService = getApplicationComponent(NewFolksonomyService.class) ;
    String workspace = getApplicationComponent(DMSConfiguration.class).getConfig(getRepository()).getSystemWorkspace();
    Map<String , String> tagStyle = new HashMap<String ,String>() ;
    for(Node tag : folksonomyService.getAllTagStyle(getRepository(), workspace)) {
      tagStyle.put(tag.getName(), tag.getProperty("exo:htmlStyle").getValue().getString()) ;
    }
    return tagStyle ;
  }
  
  public String getRepository() { return getAncestorOfType(UIJCRExplorer.class).getRepositoryName();}
  public String getWorkspace() { return getAncestorOfType(UIJCRExplorer.class).getCurrentWorkspace();}
  public String getUserName() {
  	try {
  		return getAncestorOfType(UIJCRExplorer.class).getSession().getUserID();
		} catch (Exception ex) {
			return "";
		}
  }
  
  static public class ViewTagActionListener extends EventListener<UITagExplorer> {
    public void execute(Event<UITagExplorer> event) throws Exception {
      UITagExplorer uiTagExplorer = event.getSource() ;
      String tagPath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIJCRExplorer uiExplorer = uiTagExplorer.getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.setSelectRootNode();
      uiExplorer.setTagPath(tagPath);
      
      // Reset status of document flag updated by lampt 
      uiExplorer.setViewDocument(false);
      uiExplorer.setIsViewTag(true);
      uiExplorer.updateAjax(event);
    }
  }
  
  static public class EditTagsActionListener extends EventListener<UITagExplorer> {
  	public void execute(Event<UITagExplorer> event) throws Exception {
  		UITagExplorer uiTagExplorer = event.getSource();
  		String scope = event.getRequestContext().getRequestParameter(OBJECTID);
  		uiTagExplorer.getAncestorOfType(UIJCRExplorer.class).setTagScope("Public".equals(scope) ? PUBLIC : PRIVATE);   		
  		UIJCRExplorer uiExplorer = uiTagExplorer.getAncestorOfType(UIJCRExplorer.class);
  		UIPopupContainer uiPopupContainer = uiExplorer.getChild(UIPopupContainer.class);
  		uiPopupContainer.activate(UIEditingTagsForm.class, 600);
  		event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer);
  	}
  }
}