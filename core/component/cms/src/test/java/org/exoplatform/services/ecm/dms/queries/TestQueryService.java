/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.ecm.dms.queries;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.queries.QueryService;
import org.exoplatform.services.ecm.dms.BaseDMSTestCase;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

/**
 * Created by The eXo Platform SARL Author : Ly Dinh Quang
 * quang.ly@exoplatform.com xxx5669@gmail.com Jun 12, 2009
 */
public class TestQueryService extends BaseDMSTestCase {
  private QueryService         queryService;

  private Session              session;

  private NodeHierarchyCreator nodeHierarchyCreator;

  private String               baseUserPath;

  private String               baseQueriesPath;

  private String               relativePath = "Private/Searches";

  public void setUp() throws Exception {
    super.setUp();
    queryService = (QueryService) container.getComponentInstanceOfType(QueryService.class);
    nodeHierarchyCreator = (NodeHierarchyCreator) container
        .getComponentInstanceOfType(NodeHierarchyCreator.class);
    baseUserPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_USERS_PATH);
    baseQueriesPath = nodeHierarchyCreator.getJcrPath(BasePath.QUERIES_PATH);
    session = repository.login(credentials, COLLABORATION_WS);
  }

  public void testInit() throws Exception {
    Session mySession = repository.login(credentials, DMSSYSTEM_WS);
    Node queriesHome = (Node) mySession.getItem(baseQueriesPath);
    assertEquals(queriesHome.getNodes().getSize(), 3);
  }

  public void testAddQuery() throws Exception {
    queryService.addQuery("QueryAll", "Select * from nt:base", "sql", "root", REPO_NAME);
    String userPath = baseUserPath + "/root/" + relativePath;
    Node nodeSearch = (Node) session.getItem(userPath);
    Node queryAll = nodeSearch.getNode("QueryAll");
    assertNotNull(queryAll);
  }

  public void testGetQueries() throws Exception {
    SessionProviderService sessionProviderService_ = (SessionProviderService) container
        .getComponentInstanceOfType(SessionProviderService.class);
    SessionProvider sessionProvider = sessionProviderService_.getSystemSessionProvider(null);
    queryService.addQuery("QueryAll1", "Select * from nt:base", "sql", "root", REPO_NAME);
    queryService.addQuery("QueryAll2", "//element(*, exo:article)", "xpath", "root", REPO_NAME);
    List<Query> listQueryRoot = queryService.getQueries("root", REPO_NAME, sessionProvider);
    assertEquals(listQueryRoot.size(), 2);
    List<Query> listQueryMarry = queryService.getQueries("marry", REPO_NAME, sessionProvider);
    assertEquals(listQueryMarry.size(), 0);
    List<Query> listQueryNull = queryService.getQueries(null, REPO_NAME, sessionProvider);
    assertEquals(listQueryNull.size(), 0);
  }

  public void testRemoveQuery() throws Exception {
    SessionProviderService sessionProviderService_ = (SessionProviderService) container
        .getComponentInstanceOfType(SessionProviderService.class);
    SessionProvider sessionProvider = sessionProviderService_.getSystemSessionProvider(null);
    queryService.addQuery("QueryAll1", "Select * from nt:base", "sql", "root", REPO_NAME);
    queryService.addQuery("QueryAll2", "//element(*, exo:article)", "xpath", "root", REPO_NAME);

    String queryPathRoot = baseUserPath + "/root/" + relativePath + "/QueryAll1";
    queryService.removeQuery(queryPathRoot, "root", REPO_NAME);
    List<Query> listQuery = queryService.getQueries("root", REPO_NAME, sessionProvider);
    assertEquals(listQuery.size(), 1);

    try {
      String queryPathMarry = baseUserPath + "/marry/" + relativePath + "/QueryAll2";
      queryService.removeQuery(queryPathMarry, "marry", REPO_NAME);
      listQuery = queryService.getQueries("marry", REPO_NAME, sessionProvider);
      assertEquals(listQuery.size(), 0);
      fail("Query Path not found!");
    } catch (PathNotFoundException e) {
    }
  }

  public void testGetQueryByPath() throws Exception {
    SessionProviderService sessionProviderService_ = (SessionProviderService) container
        .getComponentInstanceOfType(SessionProviderService.class);
    SessionProvider sessionProvider = sessionProviderService_.getSystemSessionProvider(null);
    queryService.addQuery("QueryAll1", "Select * from nt:base", "sql", "root", REPO_NAME);
    queryService.addQuery("QueryAll2", "//element(*, exo:article)", "xpath", "root", REPO_NAME);
    String queryPath1 = baseUserPath + "/root/" + relativePath + "/QueryAll1";
    Query query = queryService.getQueryByPath(queryPath1, "root", REPO_NAME, sessionProvider);
    assertNotNull(query);
    assertEquals(query.getStatement(), "Select * from nt:base");

    String queryPath2 = baseUserPath + "/root/" + relativePath + "/QueryAll3";
    query = queryService.getQueryByPath(queryPath2, "root", REPO_NAME, sessionProvider);
    assertNull(query);
  }

  public void testAddSharedQuery() throws Exception {
    queryService.addSharedQuery("QueryAll1", "Select * from nt:base", "sql",
        new String[] { "*:/platform/administrators" }, false, REPO_NAME);
    session = repository.login(credentials, DMSSYSTEM_WS);
    Node queriesHome = (Node) session.getItem(baseQueriesPath);
    Node queryAll1 = queriesHome.getNode("QueryAll1");
    assertNotNull(queryAll1);
    assertEquals(queryAll1.getProperty("jcr:language").getString(), "sql");
    assertEquals(queryAll1.getProperty("jcr:statement").getString(), "Select * from nt:base");
    assertEquals(queryAll1.getProperty("exo:cachedResult").getBoolean(), false);
    assertEquals(queryAll1.getProperty("exo:accessPermissions").getValues()[0].getString(),
        "*:/platform/administrators");
  }

  public void testGetSharedQuery() throws Exception {
    SessionProviderService sessionProviderService_ = (SessionProviderService) container
        .getComponentInstanceOfType(SessionProviderService.class);
    SessionProvider sessionProvider = sessionProviderService_.getSessionProvider(null);
    queryService.addSharedQuery("QueryAll1", "Select * from nt:base", "sql",
        new String[] { "*:/platform/administrators" }, false, REPO_NAME);
    Node nodeQuery = queryService.getSharedQuery("QueryAll1", REPO_NAME, sessionProvider);
    assertNotNull(nodeQuery);
    assertEquals(nodeQuery.getProperty("jcr:language").getString(), "sql");
    assertEquals(nodeQuery.getProperty("jcr:statement").getString(), "Select * from nt:base");
    assertEquals(nodeQuery.getProperty("exo:cachedResult").getBoolean(), false);
    assertEquals(nodeQuery.getProperty("exo:accessPermissions").getValues()[0].getString(),
        "*:/platform/administrators");
  }

  public void testRemoveSharedQuery() throws Exception {
    SessionProviderService sessionProviderService_ = (SessionProviderService) container
        .getComponentInstanceOfType(SessionProviderService.class);
    SessionProvider sessionProvider = sessionProviderService_.getSessionProvider(null);
    queryService.addSharedQuery("QueryAll1", "Select * from nt:base", "sql",
        new String[] { "*:/platform/administrators" }, false, REPO_NAME);
    try {
      queryService.removeSharedQuery("QueryAll2", REPO_NAME);
      fail("Query Path not found!");
      queryService.removeSharedQuery("QueryAll1", REPO_NAME);
      Node nodeQuery = queryService.getSharedQuery("QueryAll1", REPO_NAME, sessionProvider);
      assertNull(nodeQuery);
    } catch (PathNotFoundException e) {
    }
  }

  public void testgetSharedQueries() throws Exception {
    SessionProviderService sessionProviderService_ = (SessionProviderService) container
        .getComponentInstanceOfType(SessionProviderService.class);
    SessionProvider sessionProvider = sessionProviderService_.getSessionProvider(null);
    queryService.addSharedQuery("QueryAll1", "Select * from nt:base", "sql",
        new String[] { "*:/platform/administrators" }, false, REPO_NAME);
    queryService.addSharedQuery("QueryAll2", "//element(*, exo:article)", "xpath",
        new String[] { "*:/platform/users" }, true, REPO_NAME);
    // Test getSharedQueries(String repository, SessionProvider provider)
    List<Node> listQuery = queryService.getSharedQueries(REPO_NAME, sessionProvider);
    assertEquals(listQuery.size(), 2);
    Node queryNode1 = listQuery.get(0);
    assertEquals(queryNode1.getName(), "QueryAll1");
    assertEquals(queryNode1.getProperty("jcr:language").getString(), "sql");
    assertEquals(queryNode1.getProperty("jcr:statement").getString(), "Select * from nt:base");
    assertEquals(queryNode1.getProperty("exo:cachedResult").getBoolean(), false);
    assertEquals(queryNode1.getProperty("exo:accessPermissions").getValues()[0].getString(),
        "*:/platform/administrators");

    Node queryNode2 = listQuery.get(1);
    assertEquals(queryNode2.getName(), "QueryAll2");
    assertEquals(queryNode2.getProperty("jcr:language").getString(), "xpath");
    assertEquals(queryNode2.getProperty("jcr:statement").getString(), "//element(*, exo:article)");
    assertEquals(queryNode2.getProperty("exo:cachedResult").getBoolean(), true);
    assertEquals(queryNode2.getProperty("exo:accessPermissions").getValues()[0].getString(),
        "*:/platform/users");

    // Test getSharedQueries(String userId, String repository, SessionProvider
    // provider)
    listQuery = queryService.getSharedQueries("root", REPO_NAME, sessionProvider);
    assertEquals(listQuery.size(), 2);

    listQuery = queryService.getSharedQueries("marry", REPO_NAME, sessionProvider);
    assertEquals(listQuery.size(), 1);
    assertEquals(listQuery.get(0).getName(), "QueryAll2");
    assertEquals(listQuery.get(0).getProperty("jcr:language").getString(), "xpath");
    assertEquals(listQuery.get(0).getProperty("jcr:statement").getString(),
        "//element(*, exo:article)");
    assertEquals(listQuery.get(0).getProperty("exo:cachedResult").getBoolean(), true);
    assertEquals(listQuery.get(0).getProperty("exo:accessPermissions").getValues()[0].getString(),
        "*:/platform/users");

    // Test getSharedQueries(String queryType, String userId, String repository,
    // SessionProvider provider)
    listQuery = queryService.getSharedQueries("sql", "root", REPO_NAME, sessionProvider);
    assertEquals(listQuery.size(), 1);
    assertEquals(listQuery.get(0).getName(), "QueryAll1");
    assertEquals(listQuery.get(0).getProperty("jcr:language").getString(), "sql");
    assertEquals(listQuery.get(0).getProperty("jcr:statement").getString(), "Select * from nt:base");
    assertEquals(listQuery.get(0).getProperty("exo:cachedResult").getBoolean(), false);
    assertEquals(listQuery.get(0).getProperty("exo:accessPermissions").getValues()[0].getString(),
        "*:/platform/administrators");

    listQuery = queryService.getSharedQueries("sql", "marry", REPO_NAME, sessionProvider);
    assertEquals(listQuery.size(), 0);

    listQuery = queryService.getSharedQueries("xpath", "marry", REPO_NAME, sessionProvider);
    assertEquals(listQuery.size(), 1);
    assertEquals(listQuery.get(0).getName(), "QueryAll2");
    assertEquals(listQuery.get(0).getProperty("jcr:language").getString(), "xpath");
    assertEquals(listQuery.get(0).getProperty("jcr:statement").getString(),
        "//element(*, exo:article)");
    assertEquals(listQuery.get(0).getProperty("exo:cachedResult").getBoolean(), true);
    assertEquals(listQuery.get(0).getProperty("exo:accessPermissions").getValues()[0].getString(),
        "*:/platform/users");
  }

  public void testExecute() throws Exception {
    SessionProviderService sessionProviderService_ = (SessionProviderService) container
        .getComponentInstanceOfType(SessionProviderService.class);
    SessionProvider sessionProvider = sessionProviderService_.getSessionProvider(null);
    queryService.addSharedQuery("QueryAll1",
        "Select * from nt:base where jcr:path like '/exo:ecm/queries/%'", "sql",
        new String[] { "*:/platform/administrators" }, false, REPO_NAME);
    queryService.addSharedQuery("QueryAll2", "//element(*, exo:article)", "xpath",
        new String[] { "*:/platform/users" }, true, REPO_NAME);
    String queryPath = baseQueriesPath + "/QueryAll1";
    QueryResult queryResult = queryService.execute(queryPath, DMSSYSTEM_WS, REPO_NAME,
        sessionProvider, "root");
    assertEquals(queryResult.getNodes().getSize(), 2);

    try {
      queryPath = baseQueriesPath + "/QueryAll3";
      queryResult = queryService.execute(queryPath, DMSSYSTEM_WS, REPO_NAME, sessionProvider,
          "root");
      assertEquals(queryResult.getNodes().getSize(), 0);
      fail("Query Path not found!");
    } catch (PathNotFoundException e) {
    }
  }

  public void tearDown() throws Exception {
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(REPO_NAME);
      Session mySession = manageableRepository.getSystemSession(manageableRepository
          .getConfiguration().getDefaultWorkspaceName());
      Node nodeUser = (Node) mySession.getItem(baseUserPath);
      NodeIterator iter = nodeUser.getNodes();
      while (iter.hasNext()) {
        Node node = iter.nextNode();
        node.remove();
      }
      mySession.save();

      session = repository.login(credentials, DMSSYSTEM_WS);
      Node nodeQueryHome = (Node) session.getItem(baseQueriesPath);
      iter = nodeQueryHome.getNodes();
      while (iter.hasNext()) {
        iter.nextNode().remove();
      }
      session.save();
    } catch (PathNotFoundException e) {
    }
    super.tearDown();
  }
}
