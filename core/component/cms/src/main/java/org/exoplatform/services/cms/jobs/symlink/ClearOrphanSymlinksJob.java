package org.exoplatform.services.cms.jobs.symlink;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.ItemNotFoundException;
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
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.scheduler.BaseJob;
import org.exoplatform.services.scheduler.JobContext;

public class ClearOrphanSymlinksJob extends BaseJob {

	private static final Log    log                 = ExoLogger.getLogger(ClearOrphanSymlinksJob.class);

	public void execute(JobContext arg0)  {

	    String queryString = "SELECT * FROM exo:symlink";

	    ExoContainer exoContainer = ExoContainerContext.getCurrentContainer();
	    RepositoryService repositoryService = (RepositoryService)exoContainer.getComponentInstanceOfType(RepositoryService.class);
	    LinkManager linkManager = (LinkManager)exoContainer.getComponentInstanceOfType(LinkManager.class);

	    Session session = null;
	    try {
	      ManageableRepository manageableRepository = repositoryService.getCurrentRepository();

	      SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider();
	      String[] workspaces = manageableRepository.getWorkspaceNames();

	      for (String workspace : workspaces) {
	        try {
	          session = sessionProvider.getSession(workspace, manageableRepository);
	          QueryManager queryManager = session.getWorkspace().getQueryManager();
	          Query query = queryManager.createQuery(queryString, Query.SQL);
	          QueryResult queryResult = query.execute();
	          NodeIterator nodeIterator = queryResult.getNodes();
	          List<Node> deleteNodeList = new ArrayList<Node>();
	          while (nodeIterator.hasNext()) {
	            Node symlinkNode = nodeIterator.nextNode();
	            //get list of node to delete
	            try {
	              linkManager.getTarget(symlinkNode, true);
	            } catch (ItemNotFoundException e) {
	              deleteNodeList.add(symlinkNode);
	            } catch (RepositoryException e) {}
	          }
	          for (Node node : deleteNodeList) {
	            try {
	              Node parentNode = node.getParent();
	              node.remove();	              
	              parentNode.save();	              
	            } catch (Exception e) {
	              log.error("ClearOrphanSymlinksJob: Can not remove node :" + node.getPath(), e);
	            }
	          }
	          session.save();
	        } catch (RepositoryException e) {
	          log.error("ClearOrphanSymlinksJob: Error when deleting orphan symlinks in workspace: " + workspace, e);
	        } finally {
	          if (session != null && session.isLive())
	            session.logout();
	        }
	      }
	    } catch (Exception e) { }
	}

}
