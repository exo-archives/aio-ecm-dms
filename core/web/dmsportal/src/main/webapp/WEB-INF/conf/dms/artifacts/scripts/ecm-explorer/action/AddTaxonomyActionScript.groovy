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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.cms.scripts.CmsScript;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.idgenerator.IDGeneratorService;
import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * April 08, 2009  
 */
/**
* This script will be used to move document to target which specified by users and create
* a symlink corresponding with target node
*/
public class AddTaxonomyActionScript implements CmsScript {
  
  private RepositoryService repositoryService_;
  private SessionProviderService seProviderService_;
  private LinkManager linkManager_;
  private IDGeneratorService idGenerator_;
  private static final Log LOG  = ExoLogger.getLogger("AddTaxonomyActionScript");
  
  public AddTaxonomyActionScript(RepositoryService repositoryService, 
      SessionProviderService sessionProviderService, LinkManager linkManager, IDGeneratorService idGenerator) {
    repositoryService_ = repositoryService;
    seProviderService_ = sessionProviderService;
    linkManager_ = linkManager;
    idGenerator_ = idGenerator;
  }
  
  public void execute(Object context) throws Exception {
    Map variables = (Map) context;       
    String nodePath = (String)variables.get("nodePath") ;
    String storeFullPath = (String)variables.get("exo:storeHomePath") ;
    String storeHomePath = null;
    String storeWorkspace = null;
    String repository = (String)variables.get("repository");
    String targetWorkspace = (String)variables.get("exo:targetWorkspace");
    String targetPath = (String)variables.get("exo:targetPath");
    if(storeFullPath.indexOf(":/") > -1) {
      storeWorkspace = storeFullPath.split(":/")[0];
      storeHomePath = storeFullPath.split(":/")[1];
      if(!storeHomePath.startsWith("/")) storeHomePath = "/" + storeHomePath;
    } else {
      storeWorkspace = targetWorkspace;
      storeHomePath = targetPath;
    }
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository);
    Session sessionHomeNode = null;
    Session sessionTargetNode = null;
    Node targetNode = null;
    Node storeNode = null;
    try{
      sessionHomeNode = seProviderService_.getSystemSessionProvider(null).getSession(storeWorkspace, manageableRepository);
      storeNode = (Node)sessionHomeNode.getItem(storeHomePath);
    } catch(Exception e) {
      LOG.error("Exception when try to get node of root taxonomy tree", e);
      throw e;
    }
    try{
      sessionTargetNode = seProviderService_.getSystemSessionProvider(null).getSession(targetWorkspace, manageableRepository);
      targetNode = (Node)sessionTargetNode.getItem(targetPath);
    } catch(Exception e) {
      LOG.error("Exception when try to get node of target", e);
      throw e;
    }
	String[] subPaths = getDateLocation().split("/");
	Node cNode = targetNode;
	for (String subPath : subPaths) {
		if (!cNode.hasNode(subPath)) {
			cNode.addNode(subPath);
			cNode.save();
		}
		cNode = cNode.getNode(subPath);
	}
  	String nodeName = nodePath.substring(nodePath.lastIndexOf("/") + 1);
  	// defend node with same name is overwrited
  	String generatedNodeName = idGenerator_.generateStringID(nodeName);
  	String targetParentPath = cNode.getPath(); 
  	targetPath = cNode.getPath().concat("/").concat(generatedNodeName).replaceAll("/+", "/");
    if(!storeWorkspace.equals(targetWorkspace)) {
      Node currentNode = (Node)storeNode.getSession().getItem(nodePath);
      sessionTargetNode.getWorkspace().clone(storeWorkspace, nodePath, targetPath, true);
      currentNode.remove();
      storeNode.save();
    } else {
      sessionTargetNode.move(nodePath, targetPath);
    }
    sessionTargetNode.save();
    targetNode = (Node)sessionTargetNode.getItem(targetPath);
    Node nodeLink = linkManager_.createLink((Node)storeNode.getSession().getItem(nodePath.substring(0, nodePath.lastIndexOf("/"))), "exo:taxonomyLink", targetNode, nodeName);
    // rename added node to recover official name
    sessionTargetNode.move(targetPath, targetParentPath.concat("/").concat(nodeName));
    if (targetNode.canAddMixin("exo:privilegeable"))
      targetNode.addMixin("exo:privilegeable");
    sessionTargetNode.save();
  }
  
  private String getDateLocation() {
    Calendar calendar = new GregorianCalendar();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd") ;
    return dateFormat.format(calendar.getTime());
  }
  
  public void setParams(String[] params) {}

}