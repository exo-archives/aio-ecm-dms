/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.admin.folksonomy;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.webui.component.admin.UIECMAdminPortlet;
import org.exoplatform.services.cms.folksonomy.FolksonomyService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIGrid;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jan 11, 2007  
 * 2:55:47 PM
 */
@ComponentConfig(
    template = "system:/groovy/webui/core/UIGrid.gtmpl"
)
public class UITagStyleList extends UIGrid {

  final static String RANGE_PROP = "exo:styleRange" ;
  final static String HTML_STYLE_PROP = "exo:htmlStyle" ;
  
  private static String[] BEAN_FIELD = {"name", "documentRange", "tagHTML"} ;
  private static String[] ACTIONS = {"EditStyle"} ;
  
  public UITagStyleList() throws Exception {
    getUIPageIterator().setId("TagStyleIterator") ;
    configure("name", BEAN_FIELD, ACTIONS) ;
  }
  
  public void updateGrid() throws Exception {
    List<TagStyleData> tagStyleList = new ArrayList<TagStyleData>() ;
    FolksonomyService folksonomyService = getApplicationComponent(FolksonomyService.class) ;
    String repository = getAncestorOfType(UIECMAdminPortlet.class).getPreferenceRepository() ;
    TagStyleData tagStyleData = null ;
    for(Node node : folksonomyService.getAllTagStyle(repository)) {
      tagStyleData = new TagStyleData(node.getName(), getRangeOfStyle(node), getHtmlStyleOfStyle(node)) ;
      tagStyleList.add(tagStyleData) ;
    }
    ObjectPageList objPageList = new ObjectPageList(tagStyleList, 10) ;
    getUIPageIterator().setPageList(objPageList) ;
  }
  
  public String getRangeOfStyle(Node tagStyle) throws Exception {
    return tagStyle.getProperty(RANGE_PROP).getValue().getString() ;
  }
  
  public String getHtmlStyleOfStyle(Node tagStyle) throws Exception {
    return tagStyle.getProperty(HTML_STYLE_PROP).getValue().getString() ;
  }
  
  static public class TagStyleData {
    private String tagName_ ;
    private String documentRange_ ;
    private String tagHTML_ ;
    
    public TagStyleData(String tagName, String documentRange, String tagHTML) {
      tagName_ = tagName ;
      documentRange_ = documentRange ;
      tagHTML_ = tagHTML ;
    }
    
    public String getName() { return tagName_ ; } 
    public String getDocumentRange() { return documentRange_ ; }  
    public String getTagHTML() { return tagHTML_ ; }
  }
}
