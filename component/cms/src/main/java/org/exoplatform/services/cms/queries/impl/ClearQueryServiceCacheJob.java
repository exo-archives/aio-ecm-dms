/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.queries.impl;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.scheduler.BaseJob;
import org.exoplatform.services.scheduler.JobContext;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan08@users.sourceforge.net
 * 06-Oct-2005
 */
public class ClearQueryServiceCacheJob extends BaseJob {  
  public  void  execute(JobContext context) throws Exception {
    PortalContainer pcontainer = PortalContainer.getInstance() ;
    CacheService cacheService = 
      (CacheService)pcontainer.getComponentInstanceOfType(CacheService.class) ;
    ExoCache queryCache = cacheService.getCacheInstance(QueryServiceImpl.class.getName()) ;
    queryCache.clearCache() ;    
  }
}
