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
package org.exoplatform.services.cms.taxonomy.impl;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Workspace;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.impl.DMSConfiguration;
import org.exoplatform.services.cms.impl.DMSRepositoryConfiguration;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.cms.taxonomy.TaxonomyService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SARL Author : Ly Dinh Quang
 * quang.ly@exoplatform.com xxx5669@gmail.com Mar 31, 2009
 */
public class TaxonomyServiceImpl implements TaxonomyService, Startable {
  private SessionProviderService providerService_;

  private NodeHierarchyCreator   nodeHierarchyCreator_;

  private RepositoryService      repositoryService_;

  private final String           TAXONOMY_LINK   = "exo:taxonomyLink";

  private final String           EXOSYMLINK_LINK = "exo:symlink";

  private LinkManager            linkManager_;

  private final String           SQL_QUERY       = "Select * from exo:taxonomyLink where jcr:path like '$0/%' and exo:uuid = '$1' order by exo:dateCreated DESC";
  
  List<TaxonomyPlugin>           plugins_        = new ArrayList<TaxonomyPlugin>();
  
  private DMSConfiguration dmsConfiguration_;

  /**
   * Constructor method
   * @param providerService         create session
   * @param nodeHierarchyCreator    get path by alias name
   * @param repoService             manage repository
   * @param linkManager             create and reach link
   * @param dmsConfiguration        get dms-system workspace
   * @throws Exception
   */
  public TaxonomyServiceImpl(SessionProviderService providerService,
      NodeHierarchyCreator nodeHierarchyCreator, RepositoryService repoService,
      LinkManager linkManager, DMSConfiguration dmsConfiguration) throws Exception {
    providerService_ = providerService;
    nodeHierarchyCreator_ = nodeHierarchyCreator;
    repositoryService_ = repoService;
    linkManager_ = linkManager;
    dmsConfiguration_ = dmsConfiguration;
  }

  /**
   * {@inheritDoc}
   */
  public void init(String repository) throws Exception {
    for (TaxonomyPlugin plugin : plugins_) {
      plugin.init(repository);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void addTaxonomyPlugin(ComponentPlugin plugin) {
    if (plugin instanceof TaxonomyPlugin) {
      plugins_.add((TaxonomyPlugin) plugin);
    }
  }

  /**
   * {@inheritDoc}
   */
  public List<Node> getAllTaxonomyTrees(String repository) throws RepositoryException {
    return getAllTaxonomyTrees(repository, false);
  }

  /**
   * {@inheritDoc}
   */
  public List<Node> getAllTaxonomyTrees(String repository, boolean system)
      throws RepositoryException {
    List<Node> listNode = new ArrayList<Node>();
    try {
      Node taxonomyDef = getRootTaxonomyDef(repository);
      NodeIterator nodeIter = taxonomyDef.getNodes();
      while (nodeIter.hasNext()) {
        Node node = (Node) nodeIter.next();
        if (node.isNodeType(EXOSYMLINK_LINK)) {
          Node target = linkManager_.getTarget(node, system);
          if (target != null)
            listNode.add(target);
        }
      }
    } catch (RepositoryConfigurationException e) {
      throw new RepositoryException(e);
    }
    return listNode;
  }

  /**
   * {@inheritDoc}
   */
  public Node getTaxonomyTree(String repository, String taxonomyName) throws RepositoryException {
    return getTaxonomyTree(repository, taxonomyName, false);
  }

  /**
   * {@inheritDoc}
   */
  public Node getTaxonomyTree(String repository, String taxonomyName, boolean system)
      throws RepositoryException {
    try {
      Node taxonomyDef = getRootTaxonomyDef(repository);
      Node taxonomyTree = taxonomyDef.getNode(taxonomyName);
      if (taxonomyTree.isNodeType(EXOSYMLINK_LINK))
        return linkManager_.getTarget(taxonomyTree, system);
    } catch (RepositoryConfigurationException e1) {
      throw new RepositoryException(e1);
    } catch (PathNotFoundException e2) {
      throw new RepositoryException(e2);
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasTaxonomyTree(String repository, String taxonomyName) throws RepositoryException {
    try {
      Node taxonomyTree = getRootTaxonomyDef(repository).getNode(taxonomyName);
      return taxonomyTree.isNodeType(EXOSYMLINK_LINK);
    } catch (RepositoryConfigurationException e1) {
      throw new RepositoryException(e1);
    } catch (PathNotFoundException e2) {
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public void addTaxonomyTree(Node taxonomyTree) throws RepositoryException,
      TaxonomyAlreadyExistsException {
    if (hasTaxonomyTree(((ManageableRepository) taxonomyTree.getSession().getRepository())
        .getConfiguration().getName(), taxonomyTree.getName())) {
      throw new TaxonomyAlreadyExistsException();
    }
    try {
      Node taxonomyDef = getRootTaxonomyDef(((ManageableRepository) taxonomyTree.getSession()
          .getRepository()).getConfiguration().getName());
      linkManager_.createLink(taxonomyDef, EXOSYMLINK_LINK, taxonomyTree, taxonomyTree.getName());
    } catch (RepositoryConfigurationException e) {
      throw new RepositoryException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void updateTaxonomyTree(String taxonomyName, Node taxonomyTree) throws RepositoryException {
    String repository = ((ManageableRepository) taxonomyTree.getSession().getRepository())
        .getConfiguration().getName();
    try {
      if (hasTaxonomyTree(repository, taxonomyName)) {
        Node taxonomyTreeLink = getRootTaxonomyDef(repository).getNode(taxonomyName);
        linkManager_.updateLink(taxonomyTreeLink, taxonomyTree);
      }
    } catch (RepositoryConfigurationException e) {
      throw new RepositoryException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void removeTaxonomyTree(String taxonomyName) throws RepositoryException {
    try {
      String repository = repositoryService_.getDefaultRepository().getConfiguration().getName();
      if (hasTaxonomyTree(repository, taxonomyName)) {
        Node targetNode = getTaxonomyTree(repository, taxonomyName, true);
        Session session = targetNode.getSession();
        targetNode.remove();
        session.save();
        Node taxonomyDef = getRootTaxonomyDef(repository);
        Node taxonomyTree = taxonomyDef.getNode(taxonomyName);
        taxonomyTree.remove();
        taxonomyDef.getSession().save();
      }
    } catch (RepositoryConfigurationException e) {
      throw new RepositoryException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void addTaxonomyNode(String repository, String workspace, String parentPath,
      String taxoNodeName) throws RepositoryException, TaxonomyNodeAlreadyExistsException {
    try {
      ManageableRepository manaRepo = repositoryService_.getRepository(repository);
      Session systemSession = getSession(manaRepo, workspace, true);
      Node parentNode = (Node) systemSession.getItem(parentPath);
      if (parentNode.hasNode(taxoNodeName))
        throw new TaxonomyNodeAlreadyExistsException();
      parentNode.addNode(taxoNodeName, "exo:taxonomy");
      systemSession.save();
    } catch (RepositoryConfigurationException e1) {
      throw new RepositoryException(e1);
    } catch (PathNotFoundException e2) {
      throw new RepositoryException(e2);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void removeTaxonomyNode(String repository, String workspace, String absPath)
      throws RepositoryException {
    try {
      ManageableRepository manaRepo = repositoryService_.getRepository(repository);
      Session systemSession = getSession(manaRepo, workspace, true);
      Node taxonomyNode = (Node) systemSession.getItem(absPath);
      taxonomyNode.remove();
      systemSession.save();
    } catch (RepositoryConfigurationException e1) {
      throw new RepositoryException(e1);
    } catch (PathNotFoundException e2) {
      throw new RepositoryException(e2);
    }
  }

  /**
   * {@inheritDoc}
   */
  public List<Node> getCategories(Node node, String taxonomyName) throws RepositoryException {
    List<Node> listCate = new ArrayList<Node>();
    try {
      if (node.isNodeType("mix:referenceable")) {
        String repository = ((ManageableRepository) node.getSession().getRepository())
            .getConfiguration().getName();
        Node rootNodeTaxonomy = getTaxonomyTree(repository, taxonomyName);
        if (rootNodeTaxonomy != null) {
          String sql = null; 
          sql = StringUtils.replace(SQL_QUERY, "$0", rootNodeTaxonomy.getPath());        
          sql = StringUtils.replace(sql, "$1", node.getUUID());
          Session session = 
            repositoryService_.getRepository(repository).login(rootNodeTaxonomy.getSession().getWorkspace().getName());
          QueryManager queryManager = session.getWorkspace().getQueryManager();
          Query query = queryManager.createQuery(sql, Query.SQL);
          QueryResult result = query.execute();
          NodeIterator iterate = result.getNodes();
          while (iterate.hasNext()) {
            Node parentCate = iterate.nextNode().getParent();
            listCate.add(parentCate);
          }
        }
      }
    } catch (Exception e) {
      throw new RepositoryException(e);
    }
    return listCate;
  }
  
  /**
   * {@inheritDoc}
   */
  public List<Node> getAllCategories(Node node) throws RepositoryException {
    List<Node> listCategories = new ArrayList<Node>();
    String repository = ((ManageableRepository) node.getSession().getRepository())
    .getConfiguration().getName();
    List<Node> allTrees = getAllTaxonomyTrees(repository);
    for (Node tree : allTrees) {
      List<Node> categories = getCategories(node, tree.getName());
      for (Node category : categories) listCategories.add(category);
    }
    return listCategories;
  }

  /**
   * {@inheritDoc}
   */
  public void addCategory(Node node, String taxonomyName, String categoryPath)
      throws RepositoryException {
    addCategories(node, taxonomyName, new String[] { categoryPath });
  }

  /**
   * {@inheritDoc}
   */
  public void addCategories(Node node, String taxonomyName, String[] categoryPaths)
      throws RepositoryException {
    String category = "";
    try {
      String repository = ((ManageableRepository) node.getSession().getRepository())
          .getConfiguration().getName();
      Node rootNodeTaxonomy = getTaxonomyTree(repository, taxonomyName);
      for (String categoryPath : categoryPaths) {        
        if (rootNodeTaxonomy.getPath().equals("/")) {
          category = categoryPath;
        } else if (!categoryPath.startsWith("/")) {
          category = rootNodeTaxonomy.getPath() + "/" + categoryPath;
        } else {
          category = rootNodeTaxonomy.getPath() + categoryPath;
        }
        Node categoryNode;
        if (categoryPath.startsWith(rootNodeTaxonomy.getPath())) {
          categoryNode = (Node) rootNodeTaxonomy.getSession().getItem(categoryPath);
        } else {
          categoryNode = (Node) rootNodeTaxonomy.getSession().getItem(category);
        }
        linkManager_.createLink(categoryNode, TAXONOMY_LINK, node, node.getName());
      }
    } catch (PathNotFoundException e) {
      throw new RepositoryException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasCategories(Node node, String taxonomyName) throws RepositoryException {
    List<Node> listCate = getCategories(node, taxonomyName);
    if (listCate != null && listCate.size() > 0)
      return true;
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public void moveTaxonomyNode(String repository, String workspace, String srcPath,
      String destPath, String type) throws RepositoryException {
    try {
      ManageableRepository manaRepo = repositoryService_.getRepository(repository);
      Session systemSession = getSession(manaRepo, workspace, true);
      if ("cut".equals(type)) {
        systemSession.move(srcPath, destPath);
        systemSession.save();
      } else if ("copy".equals(type)) {
        Workspace wspace = systemSession.getWorkspace();
        wspace.copy(srcPath, destPath);
        systemSession.save();
      } else
        throw new UnsupportedRepositoryOperationException();
    } catch (RepositoryConfigurationException e1) {
      throw new RepositoryException(e1);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void removeCategory(Node node, String taxonomyName, String categoryPath)
      throws RepositoryException {
    try {
      String category = "";
      String repository = ((ManageableRepository) node.getSession().getRepository())
          .getConfiguration().getName();
      Node rootNodeTaxonomy = getTaxonomyTree(repository, taxonomyName);
      if (rootNodeTaxonomy.getPath().equals("/")) {
        category = categoryPath;
      } else if (!categoryPath.startsWith("/")) {
        category = rootNodeTaxonomy.getPath() + "/" + categoryPath;
      } else {
        category = rootNodeTaxonomy.getPath() + categoryPath;
      }
      Node categoryNode = ((Node) rootNodeTaxonomy.getSession().getItem(category));
      Node nodeTaxonomyLink = categoryNode.getNode(node.getName());
      nodeTaxonomyLink.remove();
      categoryNode.save();
      node.getSession().save();
    } catch (PathNotFoundException e) {
      throw new RepositoryException(e);
    }
  }

  /**
   * Get node as root of all taxonomy in the repository that is in TAXONOMIES_TREE_DEFINITION_PATH
   * @param repository
   * @return 
   * @throws RepositoryException
   * @throws RepositoryConfigurationException
   */
  private Node getRootTaxonomyDef(String repository) throws RepositoryException,
      RepositoryConfigurationException {
    ManageableRepository manaRepository = repositoryService_.getRepository(repository);
    DMSRepositoryConfiguration dmsRepoConfig = dmsConfiguration_.getConfig(repository);
    Session sysmtemSession = getSession(manaRepository, dmsRepoConfig.getSystemWorkspace(), true);
    String taxonomiesTreeDef = nodeHierarchyCreator_
        .getJcrPath(BasePath.TAXONOMIES_TREE_DEFINITION_PATH);
    return (Node) sysmtemSession.getItem(taxonomiesTreeDef);
  }

  /**
   * Get session by workspace and ManageableRepository
   * @param manageRepository
   * @param workspaceName
   * @param system    
   * @return          System session if system = true, else return session of current user 
   * @throws RepositoryException
   */
  private Session getSession(ManageableRepository manageRepository, String workspaceName,
      boolean system) throws RepositoryException {
    if (system)
      return providerService_.getSystemSessionProvider(null).getSession(workspaceName,
          manageRepository);
    return providerService_.getSessionProvider(null).getSession(workspaceName, manageRepository);
  }

  /**
   * {@inheritDoc}
   */
  public void start() {
    try {
      for (TaxonomyPlugin plugin : plugins_) {
        plugin.init() ;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void stop() {
    // TODO Auto-generated method stub
  }
}
