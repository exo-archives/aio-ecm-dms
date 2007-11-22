package org.exoplatform.services.cms.folksonomy.impl;

import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.folksonomy.impl.TagStyleConfig.HtmlTagStyle;
import org.exoplatform.services.cms.impl.Utils;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

public class TagStylePlugin extends BaseComponentPlugin{		  
  
  final private static String EXO_TAG_STYLE = "exo:tagStyle".intern() ;
  final private static String TAG_RATE_PROP = "exo:styleRange".intern() ;
  final private static String HTML_STYLE_PROP = "exo:htmlStyle".intern() ;
	private InitParams params_ ;
  private RepositoryService repoService_ ;
  private NodeHierarchyCreator nodeHierarchyCreator_ ;
  
	public TagStylePlugin(InitParams params, RepositoryService repoService,
      NodeHierarchyCreator nodeHierarchyCreator) throws Exception {
    params_ = params ;
    repoService_ = repoService ;
    nodeHierarchyCreator_ = nodeHierarchyCreator ;
	}	 
  
  
  @SuppressWarnings("unchecked")
  public void init() throws Exception {   
    Iterator<ObjectParameter> it = params_.getObjectParamIterator() ;
    TagStyleConfig tagConfig ;
    Session session = null;
    while(it.hasNext()) {
      tagConfig = (TagStyleConfig)it.next().getObject() ;
      if(tagConfig.getAutoCreatedInNewRepository()) {
        List<RepositoryEntry> repositories = repoService_.getConfig().getRepositoryConfigurations() ;
        for(RepositoryEntry repo : repositories) {
          ManageableRepository manageableRepository = repoService_.getRepository(repo.getName()) ;
          session = manageableRepository.getSystemSession(manageableRepository.getConfiguration().getSystemWorkspaceName()) ; ;
          addTag(session, tagConfig) ;
          session.logout();
        }
      } else {
        session = repoService_.getRepository(tagConfig.getRepository()).getSystemSession(repoService_.getRepository(tagConfig.getRepository()).getConfiguration().getSystemWorkspaceName()) ;
        addTag(session, tagConfig) ;
        session.logout();
      }      
    }
  }
  
  @SuppressWarnings("unchecked")
  public void init(String repository) throws Exception {
    Iterator<ObjectParameter> it = params_.getObjectParamIterator() ;
    TagStyleConfig tagConfig ;
    Session session ;
    ManageableRepository manageableRepository = repoService_.getRepository(repository) ;
    while(it.hasNext()) {
      tagConfig = (TagStyleConfig)it.next().getObject() ;
      if(tagConfig.getAutoCreatedInNewRepository() || repository.equals(tagConfig.getRepository())) {
        session = manageableRepository.getSystemSession(manageableRepository.getConfiguration().getSystemWorkspaceName()) ; 
        addTag(session, tagConfig) ;
        session.logout();
      }
    }
  }
  
  private void addTag(Session session, TagStyleConfig tagConfig) throws Exception {
    String exoTagStylePath = nodeHierarchyCreator_.getJcrPath(BasePath.EXO_TAG_STYLE_PATH) ;
    Node exoTagStyleHomeNode = (Node)session.getItem(exoTagStylePath) ;
    List<HtmlTagStyle> htmlStyle4Tag = tagConfig.getTagStyleList() ;
    for(HtmlTagStyle style: htmlStyle4Tag) {
      Node tagStyleNode = Utils.makePath(exoTagStyleHomeNode,"/"+style.getName(),EXO_TAG_STYLE) ;
      tagStyleNode.setProperty(TAG_RATE_PROP,style.getTagRate()) ;
      tagStyleNode.setProperty(HTML_STYLE_PROP,style.getHtmlStyle()) ;
    }
    exoTagStyleHomeNode.save() ;
    session.save() ;    
  }
}
