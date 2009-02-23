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
package org.exoplatform.services.ecm.publication.presentation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;

/**
 * Created by The eXo Platform SARL Author : Ly Dinh Quang
 * quang.ly@exoplatform.com xxx5669@gmail.com Feb 19, 2009
 */
@URITemplate("/publication/presentation/")
public class PublicationGetDocumentRESTService implements ResourceContainer {

  private RepositoryService  repositoryService_;

  private PublicationService publicationService_;

  public PublicationGetDocumentRESTService(RepositoryService repositoryService,
      PublicationService publicationService) {
    repositoryService_ = repositoryService;
    publicationService_ = publicationService;
  }

  /**
   * <p>Get the document which is published</p>
   * ex:
   * /portal/rest/publication/presentation/{repository}/{workspace}/{state}?showItems={numberOfItem}
   * 
   * @param repoName      Repository name
   * @param wsName        Workspace name
   * @param state         The state is specified to classify the process
   * @return
   * @throws Exception
   */
  @URITemplate("/{repository}/{workspace}/{state}/")
  @HTTPMethod("GET")
  public Response getPublishDocument(@URIParam("repository") String repoName, @URIParam("workspace")
  String wsName, @URIParam("state") String state, @QueryParam("showItems")
  String showItems) throws Exception {
    return getPublishDocument(repoName, wsName, state, null, showItems);
  }

  /**
   * <p>Get the document which is published</p>
   * ex: /portal/rest/publication/presentation/{repository}/{workspace}/{publicationPluginName}/
   * {state}?showItems={numberOfItem}
   * 
   * @param repoName                Repository name               
   * @param wsName                  Workspace name
   * @param publicationPluginName   Plugin name
   * @param state                   The state is specified to classify the process
   * @return
   * @throws Exception
   */
  @URITemplate("/{repository}/{workspace}/{publicationPluginName}/{state}/")
  @HTTPMethod("GET")
  // @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getPublishDocument1(@URIParam("repository") String repoName, @URIParam("workspace")
  String wsName, @URIParam("publicationPluginName") String pluginName, @URIParam("state")
  String state, @QueryParam("showItems")
  String showItems) throws Exception {
    return getPublishDocument(repoName, wsName, state, pluginName, showItems);
  }

  private Response getPublishDocument(String repoName, String wsName, String state,
      String pluginName, String itemPage) throws Exception {
    int item = Integer.parseInt(itemPage);
    String queryStatement = "select * from publication:publication";
    SessionProvider provider = SessionProviderFactory.createSessionProvider();
    Session session = provider.getSession(wsName, repositoryService_.getRepository(repoName));
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    Query query = queryManager.createQuery(queryStatement, Query.SQL);
    QueryResult queryResult = query.execute();
    NodeIterator iter = queryResult.getNodes();
    List<Node> listNode = getNodePublish(iter, pluginName);
    Collections.sort(listNode, new DateComparator());
    List<Node> listDocumentPublish = new ArrayList<Node>();
    if (item < listNode.size()) {
      for (int i = 0; i < item; i++) {
        listDocumentPublish.add(listNode.get(i));
      }
    } else {
      listDocumentPublish = listNode;
    }
    for (Node node : listDocumentPublish) {
      System.out.println("\n" + node.getPath() + "\n");
    }

    return Response.Builder.ok().build();
  }

  private List<Node> getNodePublish(NodeIterator iter, String pluginName) throws Exception {
    List<Node> listNode = new ArrayList<Node>();
    while (iter.hasNext()) {
      Node node = iter.nextNode();
      Node nodecheck = publicationService_.getNodePublish(node, pluginName);
      if (nodecheck != null) {
        listNode.add(nodecheck);
      }
    }
    return listNode;
  }

  private static class DateComparator implements Comparator<Node> {
    public int compare(Node node1, Node node2) {
      try {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd.HHmmss.SSS");
        Date date1 = formatter.parse(getDateTime(node1));
        Date date2 = formatter.parse(getDateTime(node2));
        return date2.compareTo(date1);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return 0;
    }

    private String getDateTime(Node currentNode) throws Exception {
      Value[] history = currentNode.getProperty("publication:history").getValues();
      for (Value value : history) {
        String[] arrHistory = value.getString().split(",");
        return arrHistory[0];
      }
      return "";
    }
  }
}
