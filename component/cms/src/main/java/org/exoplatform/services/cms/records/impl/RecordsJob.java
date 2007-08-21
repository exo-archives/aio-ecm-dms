package org.exoplatform.services.cms.records.impl;


import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cms.records.RecordsService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.scheduler.BaseJob;
import org.exoplatform.services.scheduler.JobContext;

public class RecordsJob extends BaseJob {

  private static final String QUERY = "SELECT * FROM rma:filePlan";

  private static Log log_ = ExoLogger.getLogger("job.RecordsJob");

  private RepositoryService repositoryService_ ;
  private RecordsService recordsService_ ;  

  public void execute(JobContext context) throws Exception {
    Session session = null ;
    try {
      if(log_.isDebugEnabled())
        log_.debug("File plan job started");
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      repositoryService_ = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
      recordsService_ = (RecordsService) container.getComponentInstanceOfType(RecordsService.class);
      ManageableRepository repository = repositoryService_.getDefaultRepository();
      String[] workspaces = repository.getWorkspaceNames();      
      for (int i = 0; i < workspaces.length; i++) {
        String workspaceName = workspaces[i];
        if(log_.isDebugEnabled())
          log_.debug("Search File plans in workspace : " + workspaceName);
        session = repository.getSystemSession(workspaceName);
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        Query query = queryManager.createQuery(QUERY, Query.SQL);
        QueryResult results = query.execute();
        NodeIterator iter = results.getNodes();
        if(log_.isDebugEnabled())
          log_.debug("File plan nodes : " + iter.getSize());
        while (iter.hasNext()) {
          Node filePlan = iter.nextNode();
          try {
            recordsService_.computeCutoffs(filePlan);
            recordsService_.computeHolds(filePlan);
            recordsService_.computeTransfers(filePlan);
            recordsService_.computeAccessions(filePlan);
            recordsService_.computeDestructions(filePlan);
          } catch (RepositoryException ex) {
            log_.error(ex.getMessage(), ex);
          }
        }
        session.logout();
      }
    } catch (Exception e) {
      if(session != null) {
        session.logout(); 
      }      
    } 
    if(log_.isDebugEnabled())
      log_.debug("File plan job done");
  }
}
