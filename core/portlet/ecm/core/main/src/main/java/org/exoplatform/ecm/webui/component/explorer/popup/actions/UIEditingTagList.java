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
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.sidebar.UITagExplorer;
import org.exoplatform.services.cms.folksonomy.NewFolksonomyService;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIGrid;

/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Anh Vu
 *          anhvurz90@gmail.com
 * Nov 27, 2009  
 * 4:18:12 PM
 */
@ComponentConfig(
    template = "system:/groovy/webui/core/UIGrid.gtmpl"
)
public class UIEditingTagList extends UIGrid {
	
	public UIEditingTagList() throws Exception {
		super();
		getUIPageIterator().setId("TagIterator");
		configure("name", BEAN_FIELD, ACTIONS);
	}

	private static String[] BEAN_FIELD = {"name", "relatedDocuments" };
	private static String[] ACTIONS = {"EditTag", "RemoveTag"};
	
  final static public String PUBLIC_TAG_NODE_PATH = "exoPublicTagNode";
  final static public String EXO_TOTAL = "exo:total";
  
	public void updateGrid() throws Exception {
		NewFolksonomyService newFolksonomyService = getApplicationComponent(NewFolksonomyService.class);
		UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
//		UITagExplorer uiTagExplorer = getAncestorOfType(UITagExplorer.class);
    NodeHierarchyCreator nodeHierarchyCreator = uiExplorer.getApplicationComponent(NodeHierarchyCreator.class);		
		String repository = uiExplorer.getRepositoryName();
		String workspace = uiExplorer.getRepository().getConfiguration().getDefaultWorkspaceName();
		int scope = uiExplorer.getTagScope();
		String publicTagNodePath = nodeHierarchyCreator.getJcrPath(PUBLIC_TAG_NODE_PATH);
		
		List<Node> tags = (scope == UITagExplorer.PRIVATE) ?
											newFolksonomyService.getAllPrivateTags(uiExplorer.getSession().getUserID(), repository, workspace) :
											newFolksonomyService.getAllPublicTags(publicTagNodePath, repository, workspace);
		List<TagData> tagDataList = new ArrayList<TagData>();		
		for (Node tag : tags) {
			tagDataList.add(new TagData(tag.getName(), (int)tag.getProperty(EXO_TOTAL).getLong()));
		}

    ObjectPageList objPageList = new ObjectPageList(tagDataList, 10) ;
    getUIPageIterator().setPageList(objPageList) ;
	}
  
	
	static public class TagData {
		private String tagName;
		private int relatedDocuments;
		
		public TagData(String tagName, int relatedDocuments) {
			this.tagName = tagName;
			this.relatedDocuments = relatedDocuments;
		}
		
		public String getName() { return tagName; }
		public String getRelatedDocuments() { return relatedDocuments + ""; }
	}


}
