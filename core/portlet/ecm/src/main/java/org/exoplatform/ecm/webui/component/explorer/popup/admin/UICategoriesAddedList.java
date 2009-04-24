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
package org.exoplatform.ecm.webui.component.explorer.popup.admin;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.tree.selectone.UIOneNodePathSelector;
import org.exoplatform.ecm.webui.tree.selectone.UIOneTaxonomySelector;
import org.exoplatform.ecm.webui.utils.JCRExceptionManager;
import org.exoplatform.services.cms.taxonomy.TaxonomyService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 18, 2006
 * 2:28:18 PM 
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/explorer/popup/admin/UICategoriesAddedList.gtmpl",
    events = {
      @EventConfig(listeners = UICategoriesAddedList.DeleteActionListener.class, confirm="UICategoriesAddedList.msg.confirm-delete")
    }
)
public class UICategoriesAddedList extends UIContainer implements UISelectable {
  private UIPageIterator uiPageIterator_;

  public UICategoriesAddedList() throws Exception {
    uiPageIterator_ = addChild(UIPageIterator.class, null, "CategoriesAddedList");
  }
  
  public UIPageIterator getUIPageIterator() { return uiPageIterator_; }
  
  public List getListCategories() throws Exception { return uiPageIterator_.getCurrentPageData(); }
  
  public void updateGrid(int currentPage) throws Exception {
    ObjectPageList objPageList = new ObjectPageList(getCategories(), 10);
    uiPageIterator_.setPageList(objPageList);
    if(currentPage > getUIPageIterator().getAvailablePage())
      getUIPageIterator().setCurrentPage(currentPage-1);
    else
      getUIPageIterator().setCurrentPage(currentPage);
  }
  
  public List<Node> getCategories() throws Exception {
    List<Node> listCategories = new ArrayList<Node>();
    UIJCRExplorer uiJCRExplorer = getAncestorOfType(UIJCRExplorer.class);
    TaxonomyService taxonomyService = getApplicationComponent(TaxonomyService.class);
    String repository = uiJCRExplorer.getRepositoryName();
    List<Node> listNode = new ArrayList<Node>();
    listNode = taxonomyService.getAllTaxonomyTrees(repository);
    String taxonomyName;
    String pathItemNode;
    for(Node itemNode : listNode) {
      pathItemNode = itemNode.getPath();
      taxonomyName = pathItemNode.substring(pathItemNode.lastIndexOf("/") + 1);
      listCategories.addAll(taxonomyService.getCategories(uiJCRExplorer.getCurrentNode(), taxonomyName));
    }
    
    return listCategories;
  }
  
  @SuppressWarnings("unused")
  public void doSelect(String selectField, Object value) throws Exception {
    UIJCRExplorer uiJCRExplorer = getAncestorOfType(UIJCRExplorer.class);    
    UICategoryManager uiCategoryManager = getAncestorOfType(UICategoryManager.class);
    String rootTaxonomyName;
    if (uiCategoryManager == null) {
      UISimpleCategoryManager uiSimpleCategoryManager = getAncestorOfType(UISimpleCategoryManager.class);
      UIOneNodePathSelector uiNodePathSelector = uiSimpleCategoryManager.getChild(UIOneNodePathSelector.class);
      rootTaxonomyName = uiNodePathSelector.getRootTaxonomyName();
    } else {
      UIOneTaxonomySelector uiOneTaxonomySelector = uiCategoryManager.getChild(UIOneTaxonomySelector.class);
      rootTaxonomyName = uiOneTaxonomySelector.getRootTaxonomyName();
    }
    TaxonomyService taxonomyService = getApplicationComponent(TaxonomyService.class);
    try {
      Node currentNode = uiJCRExplorer.getCurrentNode();
      uiJCRExplorer.addLockToken(currentNode);
      String[] arrayCategoryPath = value.toString().split(rootTaxonomyName + "/");
      String categoryPath;
      if (arrayCategoryPath.length > 1) {
        if (arrayCategoryPath[1].startsWith("/"))
          categoryPath = arrayCategoryPath[1];
        else
          categoryPath = "/" + arrayCategoryPath[1];
      } else {
        categoryPath = value.toString();
      } 
      taxonomyService.addCategory(currentNode, rootTaxonomyName, categoryPath);
      uiJCRExplorer.getCurrentNode().save() ;
      uiJCRExplorer.getSession().save() ;
      updateGrid(1) ;
      setRenderSibbling(UICategoriesAddedList.class) ;
    } catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  static public class DeleteActionListener extends EventListener<UICategoriesAddedList> {
    public void execute(Event<UICategoriesAddedList> event) throws Exception {
      UICategoriesAddedList uiAddedList = event.getSource() ;
      UIContainer uiManager = uiAddedList.getParent();      
      UIApplication uiApp = uiAddedList.getAncestorOfType(UIApplication.class) ;
      String nodePath = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIJCRExplorer uiExplorer = uiAddedList.getAncestorOfType(UIJCRExplorer.class) ;
      TaxonomyService taxonomyService = 
        uiAddedList.getApplicationComponent(TaxonomyService.class);
      try {
        String repository = uiExplorer.getRepositoryName();
        String taxonomyName;
        String pathItemNode;
        List<Node> listNode = new ArrayList<Node>();
        listNode = taxonomyService.getAllTaxonomyTrees(repository);
        for(Node itemNode : listNode) {
          pathItemNode = itemNode.getPath();
          taxonomyName = pathItemNode.substring(pathItemNode.lastIndexOf("/") + 1);
          taxonomyService.removeCategory(uiExplorer.getCurrentNode(), taxonomyName, nodePath);
        }
        uiAddedList.updateGrid(uiAddedList.getUIPageIterator().getCurrentPage());
      } catch(AccessDeniedException ace) {
        throw new MessageException(new ApplicationMessage("UICategoriesAddedList.msg.access-denied",
                                   null, ApplicationMessage.WARNING)) ;
      } catch(Exception e) {
        JCRExceptionManager.process(uiApp, e) ;
      }
      uiManager.setRenderedChild("UICategoriesAddedList");
    }
  }
}
