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

import org.exoplatform.ecm.webui.popup.UIPopupComponent;
import org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector;
import org.exoplatform.ecm.webui.tree.UINodeTreeBuilder;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.categories.CategoriesService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com"
 * Aug 11, 2008  
 */

@ComponentConfig(
    template = "classpath:groovy/ecm/webui/UIContainerWithAction.gtmpl", 
    events = @EventConfig(listeners = UICategoriesSelector.CloseActionListener.class)
)

public class UICategoriesSelector extends UIBaseNodeTreeSelector implements UIPopupComponent {
  
  final static public String[] ACTIONS = {"Close"};
  private List<String> existedCategoryList = new ArrayList<String>();

  public UICategoriesSelector() throws Exception {
    addChild(UINodeTreeBuilder.class,null,null);
    addChild(UICategoriesContainer.class,null,null);
  }
  
  public String[] getActions() { return ACTIONS ; }

  static public class CloseActionListener extends EventListener<UICategoriesSelector> {
    public void execute(Event<UICategoriesSelector> event) throws Exception {      
      UICategoriesSelector uiCategoriesSelector = event.getSource();
      UIPopupWindow uiPopup = uiCategoriesSelector.getParent();
      if(uiPopup != null) {
        uiPopup.setShow(false);
        uiPopup.setRendered(false);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup.getParent());
        return;
      }
      uiCategoriesSelector.deActivate();
    }
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
  
  public void activate() throws Exception {    
  }
  
  public void deActivate() throws Exception {
  }
}
