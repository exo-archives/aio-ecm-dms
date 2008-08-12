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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
 * Aug 11, 2008  
 */

@ComponentConfig(
    template = "classpath:groovy/ecm/webui/UIGridWithButton.gtmpl",
    events = {
        @EventConfig(listeners = UISelectedCategoriesGrid.DeleteActionListener.class),
        @EventConfig(listeners = UISelectedCategoriesGrid.SaveCategoriesActionListener.class)
    }
)
public class UISelectedCategoriesGrid extends UIGrid {

  public final static String[] BEAN_FIELD = {"categoryName"} ;
  public final static String[] ACTIONS = {"Delete"} ;

  private List<String> selectedCategories = new ArrayList<String>();

  public UISelectedCategoriesGrid() throws Exception {
    configure("categoryPath", BEAN_FIELD, ACTIONS);
    getUIPageIterator().setId("UISelectedCategoriesGrid");

  }

  public void updateGrid () throws Exception {
    List<CategoryData> actions = new ArrayList<CategoryData>();
    for(String categoryPath: getSelectedCategories()) {
      CategoryData bean = new CategoryData();
      bean.setCategoryName(categoryPath.substring(categoryPath.lastIndexOf("/")+1));
      bean.setCategoryPath(categoryPath);
      actions.add(bean);
    }
    Collections.sort(actions,new CategoryComparator());
    ObjectPageList objPageList = new ObjectPageList(actions,10) ;
    getUIPageIterator().setPageList(objPageList) ;
  }

  public String[] getActions() {return new String[] {"Add"} ;}

  public void addCategory(String categoryPath) {
    selectedCategories.add(categoryPath);
  }

  public void removeCategory(String categoryPath) {
    selectedCategories.remove(categoryPath);
  }

  public List<String> getSelectedCategories() {
    return selectedCategories;
  }

  public void setSelectedCategories(List<String> list) {
    this.selectedCategories = list;
  }

  static public class DeleteActionListener extends EventListener<UISelectedCategoriesGrid> {
    public void execute(Event<UISelectedCategoriesGrid> event) throws Exception {
//    UISelectedCategoriesGrid uiDefault = event.getSource() ;
//    String value = event.getRequestContext().getRequestParameter(OBJECTID) ;
//    uiDefault.removeCategory(value);
//    re render selected categories list
    }
  }

  static public class SaveCategoriesActionListener extends EventListener<UISelectedCategoriesGrid> {
    public void execute(Event<UISelectedCategoriesGrid> event) throws Exception {
//    UISelectedCategoriesGrid uiDefault = event.getSource() ;
//    UIBaseNodeTreeSelector uiTreeSelector = uiDefault.getAncestorOfType(UIBaseNodeTreeSelector.class);
//    String returnField = uiTreeSelector.getReturnFieldName();
//    ((UISelectable)uiTreeSelector.getSourceComponent()).doSelect(returnField,uiDefault.getSelectedCategories());
    }
  }

  public class CategoryData {
    private String categoryName ;
    private String categoryPath ;

    public String getCategoryName() {
      return categoryName;
    }
    public void setCategoryName(String categoryName) {
      this.categoryName = categoryName;
    }
    public String getCategoryPath() {
      return categoryPath;
    }
    public void setCategoryPath(String categoryPath) {
      this.categoryPath = categoryPath;
    }
  }

  static public class CategoryComparator implements Comparator<CategoryData> {
    public int compare(CategoryData cData1, CategoryData cData2) {
      return cData1.getCategoryName().compareTo(cData2.getCategoryName());
    }
  }
}
