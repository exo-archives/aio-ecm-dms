package org.exoplatform.services.cms.categories.impl;

import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.cms.categories.impl.TaxonomyConfig.Taxonomy;
import org.exoplatform.services.cms.impl.Utils;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;

public class TaxonomyPlugin extends BaseComponentPlugin{	
	
	private RepositoryService repositoryService_ ;  
  private String baseTaxonomiesPath_ ;
  private CmsConfigurationService cmsConfigService_ ;
  private InitParams params_ ;
	
	public TaxonomyPlugin(InitParams params, RepositoryService repositoryService, CmsConfigurationService cmsConfig) throws Exception {
    repositoryService_ = repositoryService ;
    baseTaxonomiesPath_ = cmsConfig.getJcrPath(BasePath.EXO_TAXONOMIES_PATH) ;
    cmsConfigService_= cmsConfig ;
    params_ = params ;
	}
	
  public void init() throws Exception{
    Iterator<ObjectParameter> it = params_.getObjectParamIterator() ;    
    Session session = null ;
    Node taxonomyHomeNode = null ;
    while(it.hasNext()){
      TaxonomyConfig config = (TaxonomyConfig)it.next().getObject() ;      
      if(config.getAutoCreatedInNewRepository()) {
        List<RepositoryEntry> repositories = repositoryService_.getConfig().getRepositoryConfigurations() ;
        for(RepositoryEntry repo : repositories) {
          session = getSession(repo.getName()) ;
          taxonomyHomeNode = (Node)session.getItem(baseTaxonomiesPath_) ;
          List<Taxonomy> taxonomies = config.getTaxonomies() ;
          for(Taxonomy taxonomy : taxonomies) {
            Node taxonomyNode = Utils.makePath(taxonomyHomeNode, taxonomy.getPath(), "exo:taxonomy") ;
            if(taxonomyNode.canAddMixin("mix:referenceable")) {
              taxonomyNode.addMixin("mix:referenceable") ;
            }       
            session.save() ; 
            session.logout();
          }
        }
      }else {
        session = getSession(config.getRepository()) ;
        taxonomyHomeNode = (Node)session.getItem(baseTaxonomiesPath_) ;
        List<Taxonomy> taxonomies = config.getTaxonomies() ;
        for(Taxonomy taxonomy : taxonomies) {
          Node taxonomyNode = Utils.makePath(taxonomyHomeNode, taxonomy.getPath(), "exo:taxonomy") ;
          if(taxonomyNode.canAddMixin("mix:referenceable")) {
            taxonomyNode.addMixin("mix:referenceable") ;
          }       
          session.save() ; 
          session.logout();
        }
      }
    }
  }
  
  public void init(String repository) throws Exception {
    Iterator<ObjectParameter> it = params_.getObjectParamIterator() ;
    Session session = null ;
    Node taxonomyHomeNode = null ;
    while(it.hasNext()){
      TaxonomyConfig config = (TaxonomyConfig)it.next().getObject() ;
      if(config.getAutoCreatedInNewRepository() || repository.equals(config.getRepository())) {
        List<Taxonomy> taxonomies = config.getTaxonomies() ;
        session = getSession(repository) ;
        taxonomyHomeNode = (Node)session.getItem(baseTaxonomiesPath_) ;
        for(Taxonomy taxonomy : taxonomies) {
          Node taxonomyNode = Utils.makePath(taxonomyHomeNode, taxonomy.getPath(), "exo:taxonomy") ;
          if(taxonomyNode.canAddMixin("mix:referenceable")) {
            taxonomyNode.addMixin("mix:referenceable") ;
          }       
          session.save() ;  
          session.logout();
        }
      }
    }
  }
  
  private Session getSession(String repository)throws Exception {
    return repositoryService_.getRepository(repository)
    .getSystemSession(cmsConfigService_.getWorkspace(repository)) ;
  }
  
}
