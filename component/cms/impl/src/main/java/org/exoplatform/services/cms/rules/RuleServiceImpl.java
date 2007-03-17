package org.exoplatform.services.cms.rules;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.drools.RuleBase;
import org.drools.WorkingMemory;
import org.drools.io.RuleBaseLoader;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.cms.impl.BaseResourceLoaderService;
import org.exoplatform.services.cms.impl.ResourceConfig;
import org.exoplatform.services.jcr.RepositoryService;

public class RuleServiceImpl extends BaseResourceLoaderService implements RuleService {

  public RuleServiceImpl(RepositoryService repositoryService, 
                         CmsConfigurationService cmsConfigService, 
                         ConfigurationManager cservice,CacheService cacheService, 
                         InitParams params) throws Exception {
    super((ResourceConfig)params.getObjectParamValues(ResourceConfig.class).get(0), cservice,cmsConfigService, repositoryService,cacheService);    
  }

  public WorkingMemory getRule(String ruleName) throws Exception {
    Object obj = resourceCache_.get(ruleName) ;
    if(obj !=null ) {
      return (WorkingMemory)obj ;
    }
    InputStream iS = new ByteArrayInputStream(getRuleAsText(ruleName).getBytes());
    RuleBase ruleBase = RuleBaseLoader.loadFromInputStream(iS);
    WorkingMemory workingMemory = ruleBase.newWorkingMemory() ;
    resourceCache_.put(ruleName,workingMemory) ;
    return workingMemory ;    
  }

  public String getRuleAsText(String ruleName) throws Exception {
    return getResourceAsText(ruleName);
  }

  public NodeIterator getRules() throws Exception {
    return getResources();
  }

  public boolean hasRules() throws Exception {
    return hasResources();
  }

  public void addRule(String name, String text) throws Exception {
    addResource(name, text);
  }

  public void removeRule(String ruleName) throws Exception {
    removeResource(ruleName);
    removeFromCache(ruleName) ;
  }

  protected String getBasePath() {
    return cmsConfigService_.getJcrPath(BasePath.CMS_RULES_PATH);
  }
  
  protected void removeFromCache(String resourceName) {
    try{
      resourceCache_.remove(resourceName) ;
    }catch (Exception e) {      
    }
    //Object cachedobject = localCache_.get(resourceName);
  }
  
  public Node getRuleNode(String ruleName) throws Exception {
    try {
      Node ruleHome = getResourcesHome() ;
      return ruleHome.getNode(ruleName) ;
    }catch (Exception e) {
      e.printStackTrace() ;
      return null ;
    }
  }
}
