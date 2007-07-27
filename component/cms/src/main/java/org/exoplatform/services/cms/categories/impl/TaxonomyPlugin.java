package org.exoplatform.services.cms.categories.impl;

import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.cms.categories.impl.TaxonomyConfig.Taxonomy;
import org.exoplatform.services.cms.impl.Utils;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;

public class TaxonomyPlugin extends BaseComponentPlugin{	

  private RepositoryService repositoryService_ ;  
  private String baseTaxonomiesPath_ ;
  private String baseCalendarTaxonomiesPath_ ; 
  private InitParams params_ ;
  private boolean autoCreateInNewRepository_ = true;  


  public TaxonomyPlugin(InitParams params, RepositoryService repositoryService, CmsConfigurationService cmsConfig) throws Exception {
    repositoryService_ = repositoryService ;
    baseTaxonomiesPath_ = cmsConfig.getJcrPath(BasePath.EXO_TAXONOMIES_PATH) ;
    baseCalendarTaxonomiesPath_ = cmsConfig.getJcrPath(BasePath.CALENDAR_CATEGORIES_PATH) ;
    params_ = params ;
    ValueParam valueParam = params_.getValueParam("autoCreateInNewRepository") ;
    if(valueParam !=null) {
      autoCreateInNewRepository_ = Boolean.parseBoolean(valueParam.getValue()) ;
    }
  }

  public void init() throws Exception{    
    if(autoCreateInNewRepository_) {
      for(RepositoryEntry repositoryEntry:repositoryService_.getConfig().getRepositoryConfigurations()) {
        importPredefineTaxonomies(repositoryEntry.getName()) ;        
      }
      return ;
    }
    ValueParam param = params_.getValueParam("repository") ;
    String repository = null ;
    if(param == null) {
      repository = repositoryService_.getDefaultRepository().getConfiguration().getName();
    }else {
      repository = param.getValue() ;
    }    
    importPredefineTaxonomies(repository) ;
  }

  public void init(String repository) throws Exception {
    if(!autoCreateInNewRepository_) return ;
    importPredefineTaxonomies(repository) ;
  }

  private void importPredefineTaxonomies(String repository) throws Exception {    
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository) ;
    String workspace = manageableRepository.getConfiguration().getDefaultWorkspaceName() ;    
    Session session = manageableRepository.getSystemSession(workspace) ;
    Node baseCalendarNode = (Node)session.getItem(baseCalendarTaxonomiesPath_) ;
    //TODO because base calendar node is share for this service & ICalendarService.
    if(baseCalendarNode.hasNodes()) return ;
    Node taxonomyHomeNode = (Node)session.getItem(baseTaxonomiesPath_) ;
    Iterator<ObjectParameter> it = params_.getObjectParamIterator() ;
    while(it.hasNext()) {
      TaxonomyConfig config = (TaxonomyConfig)it.next().getObject() ;
      for(Taxonomy taxonomy : config.getTaxonomies()) {
        Node taxonomyNode = Utils.makePath(taxonomyHomeNode, taxonomy.getPath(), "exo:taxonomy") ;
        if(taxonomyNode.canAddMixin("mix:referenceable")) {
          taxonomyNode.addMixin("mix:referenceable") ;
        }
      }
    }
    taxonomyHomeNode.save();
    session.save();
    session.logout();
  } 
}
