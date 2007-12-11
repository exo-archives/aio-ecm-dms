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
package org.exoplatform.services.workflow.impl.bonita;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.cms.impl.Utils;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.workflow.FileDefinition;
import org.exoplatform.services.workflow.WorkflowFileDefinitionService;

/**
 * Created by Bull R&D
 * @author Silani Patrick 
 * E-mail: patrick.silani@gmail.com
 * May 29, 2006
 */
public class JCRFileDefinitionServiceImpl
  implements WorkflowFileDefinitionService {

  /** Name of the Portal Container to use if no current instance is set */
  private static final String EXO_PORTALNAME = "ecm";
  
  /** Name of the Node Type corresponding to a Business Process Model */
  private static final String NODE_TYPE = "exo:businessProcessModel";
  
  /** Property that identifies the Business Process in the Model node */
  private static final String BPID_PROPERTY ="exo:businessProcessId";

  /** Business processes node path */
  private String bpNodePath;
  
  /** Reference to the Cms configuration Service */
  private NodeHierarchyCreator nodeHierarchyCreator_;
 
  /**
   * Cache to store File Definition objets, so we
   * don't need to get them from the JCR every time
   */
  private Hashtable<String, FileDefinition> fileDefinitions =
    new Hashtable<String, FileDefinition>();

  /**
   * Adds files to the business process model node.
   * Files in META-INF directory and all class files are ignored.
   *  
   * @param modelNode parent node
   * @param filePath complete file path
   * @param value file content
   * @throws Exception if a problem occurs
   */
  private void addFilesToModelNode(Node modelNode,
                                   String filePath,
                                   byte[] value)
    throws Exception {
  
    // We are ignoring all classes and all files in META-INF directory
    if (!(filePath.endsWith("class") || filePath.startsWith("META"))) {

      String fileName = null;
      String path     = null;
      Node fileNode   = null;

      if (filePath.contains("/")){
        fileName = new File(filePath).getName();
        path = filePath.substring(0,filePath.length() - fileName.length());
        
        if (!modelNode.hasNode(path)) {
          Node pathNode = Utils.makePath(modelNode, path, "nt:unstructured");
          fileNode = pathNode.addNode(fileName, "nt:file");
        } else {
          fileNode = modelNode.getNode(path).addNode(fileName, "nt:file");
        }
      } else {
        fileName = filePath;
        fileNode = modelNode.addNode(fileName, "nt:file");
      }
      
      Node contentNode = fileNode.addNode("jcr:content", "nt:resource");
      String mimeType = new MimeTypeResolver().getMimeType(fileName); 
      contentNode.setProperty("jcr:mimeType", mimeType);
      if (mimeType.startsWith("text")) {
        contentNode.setProperty("jcr:encoding", "UTF-8");
      }
      contentNode.setProperty("jcr:lastModified", new GregorianCalendar());
      contentNode.setProperty("jcr:data", new ByteArrayInputStream(value));
    }
  }

  /**
   * Gets a Node in the JCR from a Process id 
   * @param processId identifies the Process
   * @param Session   reference to the JCR session
   * @return the Node corresponding to the specified Process id
   */
  private Node getNodeByProcessId(String processId, Session session) 
    throws RepositoryException {

    QueryManager qm = session.getWorkspace().getQueryManager();
    Query q= qm.createQuery(
      "select * from "
      + NODE_TYPE
      + " where jcr:path like '"
      + bpNodePath
      + "/%' and "
      + BPID_PROPERTY
      + " = '"
      + processId
      + "'",
      Query.SQL);
    QueryResult result = q.execute();
    NodeIterator it = result.getNodes();
    if (it.hasNext()) {
      return it.nextNode();
    }
    return null;
  }
  
  /** 
   * Retrieves a system session to the production Workspace.
   * A reference to the default Portal Container is retrieved. If not found,
   * then a Portal container is looked up with a hardcoded name.
   * 
   * @return Session object to the production Workspace
   */
  private Session getSession() {
    
    boolean checkpoint = false;

    try {
      PortalContainer container = PortalContainer.getInstance();

      if (container == null) {
        container = RootContainer.getInstance().getPortalContainer(
          EXO_PORTALNAME);
        PortalContainer.setInstance(container);
        checkpoint = true;
      }
      
      RepositoryService repositoryService = (RepositoryService) container
        .getComponentInstanceOfType(RepositoryService.class);
      String wsName = 
        repositoryService.getDefaultRepository().getConfiguration().getSystemWorkspaceName() ;
      return repositoryService.getDefaultRepository().getSystemSession(wsName);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      if (checkpoint) {
        PortalContainer.setInstance(null);
        checkpoint = false;
      }
    }
    return null;
  }

  /**
   * Instantiates a new JCRFileDefinitionServiceImpl.
   * Caches a reference to the cmsConfiguration service.
   * 
   * @param CmsConfiguration reference to the Cms Configuration Manager
   */
  public JCRFileDefinitionServiceImpl(
    NodeHierarchyCreator nodeHierarchyCreator) {
    
    // Store references to dependent services
    this.nodeHierarchyCreator_ = nodeHierarchyCreator;
    this.bpNodePath = nodeHierarchyCreator_.
      getJcrPath(BasePath.ECM_BUSINESS_PROCESSES_PATH);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.impl.bonita.WorkflowFileDefinitionService#remove(java.lang.String)
   */
  public void remove(String processId) {

    // Remove the Business Process from cache
    removeFromCache(processId);
    
    try {
      Session session = getSession();
      Node node = getNodeByProcessId(processId, session);
      if (node !=null) {
        node.remove();
      }
      session.save();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.impl.bonita.WorkflowFileDefinitionService#removeFromCache(java.lang.String)
   */
  public void removeFromCache(String processId) {
    
    if (fileDefinitions.containsKey(processId)) {
      fileDefinitions.remove(processId);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.impl.bonita.WorkflowFileDefinitionService#retrieve(java.lang.String)
   */
  public FileDefinition retrieve(String processId) {

    // We get the filedefinition object from cache if we can
    if (fileDefinitions.containsKey(processId)) {
      return fileDefinitions.get(processId);      
    } 
    
    // Filedefinition is not cached we get it from the JCR
    try {
     Session session = getSession();
     Node modelNode = getNodeByProcessId(processId, session);
     FileDefinition fd = new XPDLFileDefinition(modelNode);
     fileDefinitions.put(processId,fd);
     return fd;
    }
    catch (RepositoryException e) {
      e.printStackTrace();
    } 
    
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.impl.bonita.WorkflowFileDefinitionService#store(org.exoplatform.services.workflow.impl.bonita.FileDefinition, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  public void store(FileDefinition fileDefinition, String processId) {
 
    try {
      // We put filedefinition in the cache to increase performances
      fileDefinitions.put(processId, fileDefinition);
       
      Hashtable<String, byte[]> entries = fileDefinition.getEntries(); 
      Session session = getSession();
       
      // We have to delete the first '/' character from bpNodePath string
      Node bpNode = session.getRootNode().getNode(
        bpNodePath.startsWith("/") ? bpNodePath.substring(1) : bpNodePath);

      if (bpNode.hasNode(fileDefinition.getProcessModelName())) {  
        Node n = bpNode.getNode(fileDefinition.getProcessModelName());
        n.remove();
      }
       
      Node modelNode = bpNode.addNode(fileDefinition.getProcessModelName(), 
                                      NODE_TYPE);
      modelNode.setProperty(BPID_PROPERTY, processId);
       
      for (Enumeration e = entries.keys(); e.hasMoreElements();) {
        Object element = e.nextElement();
        byte[] entry = entries.get(element);
        entries.get(element);
        String filename = element.toString();
        addFilesToModelNode(modelNode, filename, entry);
      }
     
      // Commit changes
      session.save();

      // Add reload action
      HashMap mappings = new HashMap();
      
      JcrInputProperty nameInputProperty = new JcrInputProperty();
      nameInputProperty.setJcrPath("/node/exo:name");
      nameInputProperty.setValue("reload");
      mappings.put("/node/exo:name", nameInputProperty);
       
      JcrInputProperty nodeTypeInputProperty = new JcrInputProperty();
      nodeTypeInputProperty.setJcrPath("/node");
      nodeTypeInputProperty.setValue("reload");
      mappings.put("/node", nodeTypeInputProperty);
     
      JcrInputProperty lifeCycleInputProperty = new JcrInputProperty();
      lifeCycleInputProperty.setJcrPath("/node/exo:lifecyclePhase");
      lifeCycleInputProperty.setValue("read"); 
      mappings.put("/node/exo:lifecyclePhase", lifeCycleInputProperty);
      
      JcrInputProperty descriptionInputProperty = new JcrInputProperty();
      descriptionInputProperty.setJcrPath("/node/exo:description");
      descriptionInputProperty.setValue("make change effective"); 
      mappings.put("/node/exo:description", descriptionInputProperty);

      /*
       * The Action Service Container is obtained only here and not in the
       * constructor. Indeed, there was a loop in the service reference graph.
       * WorkflowServiceContainer (Bonita Impl) -> FileDefinitionService (JCR
       * Impl) -> ActionServiceContainer (default Impl) ->
       * WorkflowServiceContainer.
       * When WorkflowServiceContainer was started by the container because it
       * implements Startable, its reference graph in the constructor was
       * instantiated. One of dependent service required the Workflow Service
       * Container to be initialized meanwhile. The WorkflowServiceContainer was
       * initialized again at the end hence two instances and some references
       * were wrong. By getting this reference late, we break the loop and avoid
       * this issue.
       */
      ActionServiceContainer actionServiceContainer = (ActionServiceContainer)
        PortalContainer.getComponent(ActionServiceContainer.class);
       
      if(actionServiceContainer != null) {
        String repository = ((ManageableRepository)session.getRepository()).getConfiguration().getName() ;
        actionServiceContainer.addAction(modelNode, repository, 
                                         "exo:ReloadBPAction",
                                         mappings);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
