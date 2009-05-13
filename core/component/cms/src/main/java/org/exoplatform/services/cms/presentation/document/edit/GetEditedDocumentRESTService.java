/***************************************************************************
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
 *
 **************************************************************************/
package org.exoplatform.services.cms.presentation.document.edit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.ecm.utils.comparator.PropertyValueComparator;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.folksonomy.FolksonomyService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.audit.AuditHistory;
import org.exoplatform.services.jcr.ext.audit.AuditRecord;
import org.exoplatform.services.jcr.ext.audit.AuditService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.ws.frameworks.json.transformer.Bean2JsonOutputTransformer;

/**
 * Created by The eXo Platform SARL
 * Author : Hoang Van Hung
 *          hunghvit@gmail.com
 * May 14, 2009  
 */
@URITemplate("/presentation/document/edit/")
public class GetEditedDocumentRESTService implements ResourceContainer {
  
  private RepositoryService  repositoryService;

  private TemplateService  templateService;
  
  private FolksonomyService folksonomyService;
  
  private AuditService auditService;
  
  private static final String DATE_MODIFIED = "exo:dateModified";

  private static final String JCR_PRIMARYTYPE = "jcr:primaryType";

  private static final String NT_FILE = "nt:file";

  private static final String NT_BASE = "nt:base";

  private static final String JCR_CONTENT = "jcr:content";
  
  private static final String EXO_AUDITABLE = "exo:auditable";

  private static final String EXO_OWNER = "exo:owner";
  
  private static final int NO_PER_PAGE = 3;
  
  private static final String QUERY_STATEMENT = "SELECT * FROM $0 WHERE $1 ORDER BY $2 DESC";

  private Log LOG = ExoLogger.getLogger("cms.GetEditedDocumentRESTService");
  
  public GetEditedDocumentRESTService(RepositoryService repositoryService,
      TemplateService templateService, FolksonomyService folksonomyService,
      AuditService auditService) {
    this.repositoryService = repositoryService;
    this.templateService = templateService;
    this.folksonomyService = folksonomyService;
    this.auditService = auditService;
  }
  
  @URITemplate("/{repository}/")
  @HTTPMethod("GET")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getLastEditedDoc(@URIParam("repository") String repository,
      @QueryParam("showItems") String showItems) throws Exception {
    List<Node> lstLastEditedNode = getLastEditedNode(repository, showItems);
    List<DocumentNode> lstDocNode = getDocumentData(repository, lstLastEditedNode);
    ListEditDocumentNode listEditDocumentNode = new ListEditDocumentNode();
    listEditDocumentNode.setLstDocNode(lstDocNode);
    return Response.Builder.ok(listEditDocumentNode).mediaType("application/json").build();
  }

  private List<Node> getLastEditedNode(String repository, String noOfItem) throws Exception{
    ArrayList<Node>  lstNode = new ArrayList<Node>();
    StringBuffer bf = new StringBuffer(1024);
    List<String> lstNodeType = templateService.getDocumentTemplates(repository);
    if (lstNodeType != null) {
      for (String nodeType : lstNodeType) {
        bf.append("(").append(JCR_PRIMARYTYPE).append("=").append("'").append(nodeType).append("'")
            .append(")").append(" OR ");
      }
    }
    if (bf.length() == 1) return null;
    bf.delete(bf.lastIndexOf("OR") - 1, bf.length());
    if (noOfItem == null || noOfItem.trim().length() == 0) noOfItem = String.valueOf(NO_PER_PAGE);
    String queryStatement = StringUtils.replace(QUERY_STATEMENT, "$0", NT_BASE);
    queryStatement = StringUtils.replace(queryStatement, "$1", bf.toString());
    queryStatement = StringUtils.replace(queryStatement, "$2", DATE_MODIFIED);
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    try {
      String[] workspaces = manageableRepository.getWorkspaceNames();
      SessionProvider provider = SessionProviderFactory.createAnonimProvider();
      Query query = null;
      Session session = null;
      QueryResult queryResult = null;
      QueryManager queryManager = null;
      for (String workspace : workspaces) {
        session = provider.getSession(workspace, manageableRepository);
        queryManager = session.getWorkspace().getQueryManager();
        query = queryManager.createQuery(queryStatement, Query.SQL);
        queryResult = query.execute();
        puttoList(lstNode, queryResult.getNodes());
        session.logout();
      }
    } catch (RepositoryException e) {
      LOG.error("Exception when execute SQL " + queryStatement, e);
    }
    return lstNode;
  }
  
  private void puttoList(List<Node> lstNode, NodeIterator nodeIter) {
    if (nodeIter != null) {
      while (nodeIter.hasNext()) {
        lstNode.add(nodeIter.nextNode());
      }
    }
  }
  
  private List<DocumentNode> getDocumentData(String repository, List<Node> lstNode) throws Exception {
    return getDocumentData(repository, lstNode, String.valueOf(NO_PER_PAGE));
  }
  
  private String getDateFormat(String date) {
    return ISO8601.parse(date).getTime().toString();
  }
  
  private List<DocumentNode> getDocumentData(String repository, List<Node> lstNode, String noOfItem) throws Exception {
    if (lstNode == null || lstNode.size() == 0) return null;
    List<DocumentNode> lstDocNode = new ArrayList<DocumentNode>();
    Collections.sort(lstNode, new PropertyValueComparator(DATE_MODIFIED, PropertyValueComparator.DESCENDING_ORDER));
    DocumentNode docNode = null;
    StringBuilder linkedTags = null;
    StringBuilder author = null;
    List<AuditRecord> listRec = null;
    
    for (Node node : lstNode) {
      docNode = new DocumentNode();
      docNode.setName(node.getName());
      docNode.setPath(node.getPath());
      docNode.setLastAuthor(node.getProperty(EXO_OWNER).getString());
      docNode.setLstAuthor(node.getProperty(EXO_OWNER).getString());
      docNode.setDateEdited(getDateFormat(node.getProperty(DATE_MODIFIED).getString()));
      linkedTags = new StringBuilder(1024);
      author = new StringBuilder(1024);
      for(Node tag : folksonomyService.getLinkedTagsOfDocument(node, repository)) {
        if (linkedTags.length() > 0) linkedTags = linkedTags.append(",");
        linkedTags.append(tag.getName());
      }
      docNode.setTags(linkedTags.toString());
      String user = "";
      if (auditService.hasHistory(node)){
        if (NT_FILE.equals(node.getProperty(JCR_PRIMARYTYPE).getString())) { 
          node = node.getNode(JCR_CONTENT);
        }
        if(node.isNodeType(EXO_AUDITABLE)){
          AuditHistory auHistory = auditService.getHistory(node);
          listRec = auHistory.getAuditRecords();
        }
        if (listRec != null && listRec.size() > 0) {
          for (AuditRecord ar : listRec) {
            if (!user.equals(ar.getUserId()))
              author.append(ar.getUserId()).append(", ");
            user = ar.getUserId();
          }
        }
      }
      
      if (author.indexOf(",") > 0) {
        String lstAuthor = author.substring(0, author.lastIndexOf(","));
        docNode.setLastAuthor(lstAuthor.substring(lstAuthor.lastIndexOf(",")));
        docNode.setLstAuthor(lstAuthor);
      }
      docNode.setDriveName("");
      if (lstDocNode.size() < Integer.parseInt(noOfItem))  lstDocNode.add(docNode);
    }
    return lstDocNode;
  }
  
  public class DocumentNode {
    
    private String nodeName_;

    private String nodePath_;

    private String driveName_;

    private String   dateEdited_;

    private String tags;

    private String   lastAuthor;

    private String lstAuthor;
    
    public String getTags() {
      return tags;
    }
    public void setTags(String tags) {
      this.tags = tags;
    }
    public String getLastAuthor() {
      return lastAuthor;
    }
    public void setLastAuthor(String lastAuthor) {
      this.lastAuthor = lastAuthor;
    }
    public String getLstAuthor() {
      return lstAuthor;
    }
    public void setLstAuthor(String lstAuthor) {
      this.lstAuthor = lstAuthor;
    }

    public void setName(String nodeName) {
      nodeName_ = nodeName;
    }

    public String getName() {
      return nodeName_;
    }

    public void setPath(String nodePath) {
      nodePath_ = nodePath;
    }

    public String getPath() {
      return nodePath_;
    }

    public void setDriveName(String driveName) {
      driveName_ = driveName;
    }

    public String getDriveName() {
      return driveName_;
    }
    
    public String getDateEdited() {
      return dateEdited_;
    }
    public void setDateEdited(String dateEdited_) {
      this.dateEdited_ = dateEdited_;
    }

  }

  public class ListEditDocumentNode {

    private List<DocumentNode> lstDocNode;
    
    public List<DocumentNode> getLstDocNode() {
      return lstDocNode;
    }

    public void setLstDocNode(List<DocumentNode> lstDocNode) {
      this.lstDocNode = lstDocNode;
    }
  }
  
}
