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
package org.exoplatform.ecm.webui.tree.selectmany;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector;
import org.exoplatform.ecm.webui.tree.UINodeTreeBuilder;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.categories.CategoriesService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com"
 * Aug 11, 2008  
 */

@ComponentConfig(lifecycle = UIContainerLifecycle.class)

public class UICategoriesSelector extends UIBaseNodeTreeSelector {

  private List<String> existedCategoryList = new ArrayList<String>();

  public UICategoriesSelector() throws Exception {
    addChild(UINodeTreeBuilder.class,null,null);
    addChild(UICategoriesContainer.class,null,null);
  }

  public void init() throws Exception{
    CategoriesService categoriesService = getApplicationComponent(CategoriesService.class);
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    String repositoryName = repositoryService.getCurrentRepository().getConfiguration().getName();
    Node rootCategories = categoriesService.getTaxonomyHomeNode(repositoryName, SessionProviderFactory.createSystemProvider());
    Node rootCategoryTree = rootCategories;
    UINodeTreeBuilder builder = getChild(UINodeTreeBuilder.class);
    builder.setRootTreeNode(rootCategoryTree);

    UICategoriesContainer uiCategoriesContainer = getChild(UICategoriesContainer.class);
    UISelectedCategoriesGrid categoriesGrid = uiCategoriesContainer.getChild(UISelectedCategoriesGrid.class);
    categoriesGrid.setSelectedCategories(existedCategoryList);
    categoriesGrid.updateGrid();
  }

  public void onChange(Node currentNode, Object context) throws Exception {
    UICategoriesContainer uiCategoriesContainer = getChild(UICategoriesContainer.class);
    UICategoriesSelectPanel uiCategoriesSelectPanel = uiCategoriesContainer.getChild(UICategoriesSelectPanel.class);
    uiCategoriesSelectPanel.setParentNode(currentNode);
  }

  public List<String> getExistedCategoryList() {
    return existedCategoryList;
  }

  public void setExistedCategoryList(List<String> existedCategoryList) {
    this.existedCategoryList = existedCategoryList; 
  }

}
