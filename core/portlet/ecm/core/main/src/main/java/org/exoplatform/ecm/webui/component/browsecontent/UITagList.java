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
package org.exoplatform.ecm.webui.component.browsecontent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cms.folksonomy.NewFolksonomyService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * 18-07-2007  
 */
@ComponentConfig(
    template = "app:/groovy/webui/component/browse/UITagList.gtmpl",
    events = {
        @EventConfig(listeners = UITagList.ViewByTagActionListener.class)
    }
)
public class UITagList extends UIComponent {

  final static public String PUBLIC_TAG_NODE_PATH = "exoPublicTagNode";
  
  private String tagPath_ ;   
  public UITagList() throws Exception {
  }

  public List<Node> getPrivateTagLink() throws Exception {
    String repository = getRepository();
  	ExoContainer container = ExoContainerContext.getCurrentContainer();
		RepositoryService repositoryService 
		= (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
		ManageableRepository	manageableRepo = repositoryService.getRepository(repository);
		
		String workspace = manageableRepo.getConfiguration().getDefaultWorkspaceName();
    String userName = getUserName();
    NewFolksonomyService newFolksonomyService = getApplicationComponent(NewFolksonomyService.class) ;
    return newFolksonomyService.getAllPrivateTags(userName, repository, workspace);
  }
  
  public List<Node> getPublicTagLink() throws Exception {
    String repository = getRepository();
  	ExoContainer container = ExoContainerContext.getCurrentContainer();
		RepositoryService repositoryService = 
		(RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
		ManageableRepository	manageableRepo = repositoryService.getRepository(repository);
		NodeHierarchyCreator nodeHierarchyCreator =
		(NodeHierarchyCreator) container.getComponentInstanceOfType(NodeHierarchyCreator.class);
		
		String workspace = manageableRepo.getConfiguration().getDefaultWorkspaceName();
		
		String publicTagNodePath = nodeHierarchyCreator.getJcrPath(PUBLIC_TAG_NODE_PATH);
    NewFolksonomyService newFolksonomyService = getApplicationComponent(NewFolksonomyService.class) ;
    
    return newFolksonomyService.getAllPublicTags(publicTagNodePath, repository, workspace);
  }
  
  public Map<String ,String> getTagStyle() throws Exception {
    String repository = getRepository() ;
    String workspace = getAncestorOfType(UIBrowseContainer.class).getDMSSystemWorkspace(repository);
    NewFolksonomyService newFolksonomyService = getApplicationComponent(NewFolksonomyService.class) ;
    Map<String , String> tagStyle = new HashMap<String ,String>() ;
    for(Node tag : newFolksonomyService.getAllTagStyle(repository, workspace)) {
      tagStyle.put(tag.getName(), tag.getProperty("exo:htmlStyle").getValue().getString()) ;
    }
    return tagStyle ;
  }
  public String getRepository() { return getAncestorOfType(UIBrowseContainer.class).getRepository();}
  public String getWorkspace() { return getAncestorOfType(UIBrowseContainer.class).getWorkSpace(); }
  public String getUserName() { return getAncestorOfType(UIBrowseContainer.class).getUserName(); }
  
  public String getTagPath() { return this.tagPath_ ; }  
  public void setTagPath(String tagName) { this.tagPath_ = tagName ; }

  static public class ViewByTagActionListener extends EventListener<UITagList> {
    public void execute(Event<UITagList> event) throws Exception {
      UITagList uiTaglist = event.getSource() ;
      String tagPath = event.getRequestContext().getRequestParameter(OBJECTID);
      UIBrowseContainer uiContainer = uiTaglist.getAncestorOfType(UIBrowseContainer.class) ;
      uiContainer.setShowDocumentByTag(true) ;
      uiContainer.setTagPath(tagPath) ;
      uiContainer.setPageIterator(uiContainer.getDocumentByTag()) ;
      uiContainer.storeHistory() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }
}
