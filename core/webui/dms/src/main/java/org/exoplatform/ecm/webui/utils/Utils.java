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
package org.exoplatform.ecm.webui.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;

import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.cms.thumbnail.ThumbnailService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * May 8, 2008 3:13:32 PM
 */
public class Utils {
  final public static String WORKSPACE_NAME = "workspace".intern();   
  final public static String JCR_PATH = "path".intern(); 
  final public static String DRIVE_FOLDER = "allowCreateFolder".intern(); 
  final public static String MIN_WIDTH = "minwidth".intern();
  final public static String CB_DOCUMENT_NAME = "documentName".intern();
  final public static String CB_SCRIPT_NAME = "scriptName".intern();
  final public static String CB_REF_DOCUMENT = "reference".intern();  
  final public static String CB_CHILD_DOCUMENT = "child".intern();
  final public static String CB_NB_PER_PAGE = "nbPerPage".intern();
  final public static String CB_QUERY_STATEMENT = "queryStatement".intern();
  final public static String CB_QUERY_ISNEW = "isAddNew".intern();
  final public static String CB_QUERY_TYPE = "queryType".intern();
  final public static String CB_QUERY_STORE = "queryStore".intern();
  final public static String CB_QUERY_LANGUAGE = "queryLanguage".intern();
  final public static String CB_VIEW_TOOLBAR = "viewToolbar".intern();
  final public static String CB_VIEW_TAGMAP = "viewTagMap".intern();
  final public static String CB_VIEW_COMMENT = "viewComment".intern();
  final public static String CB_VIEW_VOTE= "viewVote".intern();
  final public static String CB_SEARCH_LOCATION = "searchLocation".intern();
  final public static String CB_ENABLE_SEARCH_LOCATION = "enableSearch".intern();
  final public static String CB_FILTER_CATEGORY = "filterCategory".intern();
  final static public String EXO_AUDITABLE = "exo:auditable";
  final public static String CB_BOX_TEMPLATE = "boxTemplate".intern();   
  final public static String CB_TEMPLATE = "template";
  final public static String CB_USECASE = "usecase".intern();
  final public static String CB_ALLOW_PUBLISH = "isAllowPublish".intern();
  
  final public static String FROM_PATH = "From Path".intern();
  final public static String USE_DOCUMENT = "Document".intern();
  final public static String USE_JCR_QUERY = "Using a JCR query".intern();
  final public static String USE_SCRIPT = "Using a script".intern();

  final public static String CB_USE_FROM_PATH = "path".intern();
  final public static String CB_USE_DOCUMENT = "detail-document".intern();
  final public static String CB_USE_JCR_QUERY = "query".intern();
  final public static String CB_USE_SCRIPT = "script".intern();  

  final public static String SEMI_COLON = ";".intern();
  final public static String COLON = ":".intern();
  final public static String SLASH = "/".intern();
  final public static String BACKSLASH = "\\".intern();
  final public static String EXO_CREATED_DATE = "exo:dateCreated";
  final public static String EXO_MODIFIED_DATE = "exo:dateModified";
  final public static String EXO_OWNER = "exo:owner";

  final public static String SPECIALCHARACTER[] = {SEMI_COLON,COLON,SLASH,BACKSLASH,"'","|",">","<",
                                                   "\"", "?", "!", "@", "#", "$", "%","^","&","*",
                                                   "(", ")", "[", "]", "{", "}"};
  final public static String REPOSITORY = "repository".intern();
  final public static String VIEWS = "views".intern();
  final public static String DRIVE = "drive".intern();
  final public static String JCR_INFO = "jcrInfo";
  final static public String NT_UNSTRUCTURED = "nt:unstructured";
  final static public String NT_FILE = "nt:file";
  final static public String NT_FOLDER = "nt:folder";
  final static public String NT_FROZEN = "nt:frozenNode".intern();
  final static public String EXO_TITLE = "exo:title";
  final static public String EXO_SUMMARY = "exo:summary";
  final static public String EXO_RELATION = "exo:relation";
  final static public String EXO_TAXANOMY = "exo:taxonomy";
  final static public String EXO_IMAGE = "exo:image";
  final static public String EXO_ARTICLE = "exo:article";
  final static public String EXO_LANGUAGE = "exo:language";
  final static public String LANGUAGES = "languages";
  final static public String EXO_METADATA = "exo:metadata";
  final static public String MIX_REFERENCEABLE = "mix:referenceable";
  final static public String MIX_VERSIONABLE = "mix:versionable";
  final static public String NT_RESOURCE = "nt:resource";
  final static public String DEFAULT = "default";
  final static public String JCR_CONTENT = "jcr:content";
  final static public String JCR_MIMETYPE = "jcr:mimeType";
  final static public String JCR_FROZEN = "jcr:frozenNode";
  final public static String JCR_LASTMODIFIED = "jcr:lastModified";
  final public static String JCR_PRIMARYTYPE = "jcr:primaryType";
  final static public String JCR_DATA = "jcr:data";
  final static public String EXO_ROLES = "exo:roles";
  final static public String EXO_TEMPLATEFILE = "exo:templateFile";
  final static public String EXO_TEMPLATE = "exo:template";
  final static public String EXO_ACTION = "exo:action";
  final static public String MIX_LOCKABLE = "mix:lockable";
  final static public String EXO_CATEGORIZED = "exo:categorized";
  final static public String EXO_CATEGORY = "exo:category";
  final static public String EXO_HIDDENABLE = "exo:hiddenable";
  final static public String EXO_ACCESSPERMISSION = "exo:accessPermissions";

  final static public String EXO_MUSICFOLDER = "exo:musicFolder";
  final static public String EXO_VIDEOFOLDER = "exo:videoFolder";
  final static public String EXO_PICTUREFOLDER = "exo:pictureFolder";
  final static public String EXO_DOCUMENTFOLDER = "exo:documentFolder";
  final static public String EXO_SEARCHFOLDER = "exo:searchFolder";
  final static public String MIX_COMMENTABLE = "mix:commentable";
  final static public String MIX_VOTABLE = "mix:votable";
  final static public String RMA_RECORD = "rma:record";
  final static public String EXO_SYMLINK = "exo:symlink";
  final static public String EXO_PRIMARYTYPE = "exo:primaryType";
  final static public String[] SPECIFIC_FOLDERS = {EXO_MUSICFOLDER,EXO_VIDEOFOLDER,EXO_PICTUREFOLDER,EXO_DOCUMENTFOLDER,EXO_SEARCHFOLDER };

  final static public String[] FOLDERS = {NT_UNSTRUCTURED, NT_FOLDER};
  final static public String[] NON_EDITABLE_NODETYPES = {NT_UNSTRUCTURED, NT_FOLDER, NT_RESOURCE};
  final public static String[] CATEGORY_NODE_TYPES = {NT_FOLDER, NT_UNSTRUCTURED, EXO_TAXANOMY};
  
  final static public String   CATEGORY_MANDATORY        = "categoryMandatoryWhenFileUpload";

  final static public String   UPLOAD_SIZE_LIMIT_MB      = "uploadFileSizeLimitMB";
  
  public Map<String, Object> maps_ = new HashMap<String, Object>();

  public static String encodeHTML(String text) {
    return text.replaceAll("&", "&amp;").replaceAll("\"", "&quot;")
    .replaceAll("<", "&lt;").replaceAll(">", "&gt;");
  }

  public static String formatNodeName(String text) {
    return text.replaceAll("'", "\\\\'");
  }

  public static boolean isVersionable(Node node) throws RepositoryException {
    return node.isNodeType(MIX_VERSIONABLE);
  }  
  
  static public class NodeTypeNameComparator implements Comparator<NodeType> {
    public int compare(NodeType n1, NodeType n2) throws ClassCastException {
      String name1 = n1.getName();
      String name2 = n2.getName();
      return name1.compareToIgnoreCase(name2);
    }
  }

  public static boolean isNameValid(String name, String[] regexpression) {
    for(String c : regexpression){ if(name.contains(c)) return false;}
    return true;
  }

  public static boolean isNameEmpty(String name) {
    return (name == null || name.trim().length() == 0);
  }
  
  public static boolean isAuditable(Node node) throws RepositoryException {
    return node.isNodeType(EXO_AUDITABLE);
  }

  public static List<String> getListAllowedFileType(Node currentNode, String repository, TemplateService templateService) throws Exception {
    List<String> nodeTypes = new ArrayList<String>();
    NodeTypeManager ntManager = currentNode.getSession().getWorkspace().getNodeTypeManager(); 
    NodeType currentNodeType = currentNode.getPrimaryNodeType(); 
    NodeDefinition[] childDefs = currentNodeType.getChildNodeDefinitions();
    List<String> templates = templateService.getDocumentTemplates(repository);
    try {
      for(int i = 0; i < templates.size(); i ++){
        String nodeTypeName = templates.get(i).toString(); 
        NodeType nodeType = ntManager.getNodeType(nodeTypeName);
        NodeType[] superTypes = nodeType.getSupertypes();
        boolean isCanCreateDocument = false;
        for(NodeDefinition childDef : childDefs){
          NodeType[] requiredChilds = childDef.getRequiredPrimaryTypes();
          for(NodeType requiredChild : requiredChilds) {          
            if(nodeTypeName.equals(requiredChild.getName())){            
              isCanCreateDocument = true;
              break;
            }            
          }
          if(nodeTypeName.equals(childDef.getName()) || isCanCreateDocument) {
            if(!nodeTypes.contains(nodeTypeName)) nodeTypes.add(nodeTypeName);
            isCanCreateDocument = true;          
          }
        }      
        if(!isCanCreateDocument){
          for(NodeType superType:superTypes) {
            for(NodeDefinition childDef : childDefs){          
              for(NodeType requiredType : childDef.getRequiredPrimaryTypes()) {              
                if (superType.getName().equals(requiredType.getName())) {
                  if(!nodeTypes.contains(nodeTypeName)) nodeTypes.add(nodeTypeName);
                  isCanCreateDocument = true;
                  break;
                }
              }
              if(isCanCreateDocument) break;
            }
            if(isCanCreateDocument) break;
          }
        }            
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
    return nodeTypes;
  }

  public static String getNodeTypeIcon(Node node, String appended, String mode) throws RepositoryException {
    StringBuilder str = new StringBuilder();
    if (node == null) return "";
    String nodeType = node.getPrimaryNodeType().getName();
    if(node.isNodeType(EXO_SYMLINK)) {
      LinkManager linkManager = Util.getUIPortal().getApplicationComponent(LinkManager.class);
      try {
        nodeType = node.getProperty(EXO_PRIMARYTYPE).getString();
        node = linkManager.getTarget(node);
        if (node == null) return "";
      } catch(Exception e) {
        return "";
      }
    }
    if(nodeType.equals(NT_UNSTRUCTURED) || nodeType.equals(NT_FOLDER)) {
      for(String specificFolder:SPECIFIC_FOLDERS) {
        if(node.isNodeType(specificFolder)) {
          nodeType = specificFolder;
          break;
        }
      }
    }
    nodeType = nodeType.replace(':','_') + appended;    
    str.append(nodeType);
    if(mode != null && mode.equalsIgnoreCase("Collapse")) str.append(' ').append(mode).append(nodeType);
    if (node.isNodeType(NT_FILE)) {
      if (node.hasNode(JCR_CONTENT)) {
        Node jcrContentNode = node.getNode(JCR_CONTENT);
        str.append(' ').append(jcrContentNode.getProperty(JCR_MIMETYPE).getString().replaceAll("/|\\.","_")).append(appended);
      }
    }
    return str.toString();
  }

  public static String getNodeTypeIcon(Node node, String appended) throws RepositoryException {
    return getNodeTypeIcon(node, appended, null);
  }  
  
  public static NodeIterator getAuthorizedChildNodes(Node node) throws Exception {
    NodeIterator iter = node.getNodes();
    while(iter.hasNext()) {
      if(!PermissionUtil.canRead(iter.nextNode())) iter.remove(); 
    }  
    return iter;
  }

  public static List<Node> getAuthorizedChildList(Node node) throws Exception {
    List<Node> children = new ArrayList<Node>();
    NodeIterator iter = node.getNodes();
    while(iter.hasNext()) {
      Node child = iter.nextNode();
      if(PermissionUtil.canRead(child)) children.add(child);
    }  
    return children;
  }

  public static boolean isLockTokenHolder(Node node) throws Exception {
    if(node.getLock().getLockToken() != null) { 
      return true; 
    } 
    return false;    
  }     
  
  public static List<String> getMemberships() throws Exception {
    String userId = Util.getPortalRequestContext().getRemoteUser();
    OrganizationService oservice = Util.getUIPortal().getApplicationComponent(OrganizationService.class);
    List<String> userMemberships = new ArrayList<String> ();
    userMemberships.add(userId);
    Collection<?> memberships = oservice.getMembershipHandler().findMembershipsByUser(userId);
    if(memberships == null || memberships.size() < 0) return userMemberships;
    Object[] objects = memberships.toArray();
    for(int i = 0; i < objects.length; i ++ ){
      Membership membership = (Membership)objects[i];
      String role = membership.getMembershipType() + ":" + membership.getGroupId();
      userMemberships.add(role);     
    }
    return userMemberships;
  }

  public static List<String> getGroups() throws Exception {
    String userId = Util.getPortalRequestContext().getRemoteUser();
    OrganizationService oservice = Util.getUIPortal().getApplicationComponent(OrganizationService.class);
    List<String> groupList = new ArrayList<String> ();
    Collection<?> groups = oservice.getGroupHandler().findGroupsOfUser(userId);
    Object[] objects = groups.toArray();
    for(int i = 0; i < objects.length; i ++ ){
      Group group = (Group)objects[i];
      String groupPath = null;
      if(group.getParentId() == null || group.getParentId().length() == 0) groupPath = "/" + group.getGroupName(); 
      else groupPath = group.getParentId() + "/" + group.getGroupName(); 
      groupList.add(groupPath);
    }
    return groupList;
  }

  public static String getNodeOwner(Node node) throws Exception {
    try {
      if(node.hasProperty(EXO_OWNER)) {
        return node.getProperty(EXO_OWNER).getString();
      }
    } catch (Exception e) {
      //e.printStackTrace();
    } 
    return null;
  }
  
  public static Node findNodeByUUID(String repository, String uuid) throws Exception {
    RepositoryService repositoryService = 
      Util.getUIPortal().getApplicationComponent(RepositoryService.class);
    SessionProviderService sessionProviderService = 
      Util.getUIPortal().getApplicationComponent(SessionProviderService.class);
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    Node node = null;
    for(String wsName : manageableRepository.getWorkspaceNames()) {
      try {
        node = sessionProvider.getSession(wsName, manageableRepository).getNodeByUUID(uuid);
      } catch(ItemNotFoundException e) {
        continue;
      }
    }
    return node;
  }
  
  public static boolean isSymLink(Node node) throws RepositoryException {
    LinkManager linkManager = Util.getUIPortal().getApplicationComponent(LinkManager.class);
    return linkManager.isLink(node);
  }

  public static Node getNodeSymLink(Node node) throws Exception {
    LinkManager linkManager = Util.getUIPortal().getApplicationComponent(LinkManager.class);
    Node realNode = null;
    if (linkManager.isLink(node)) {
      if (linkManager.isTargetReachable(node)) {
        realNode = linkManager.getTarget(node);
      }
    } else {
      realNode = node;
    }
    return realNode;
  }
  
  
  
  public static InputStream extractFromZipFile(ZipInputStream zipStream, String extractedFile) throws Exception {
    File file = new File(extractedFile);
    if (!file.exists()) file.createNewFile();
    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
    byte[] data  = new byte[1024];   
    ZipEntry entry = zipStream.getNextEntry();
    while(entry != null) {
      int available = -1;
      while ((available = zipStream.read(data, 0, 1024)) > -1) {
        bos.write(data, 0, available);
      }                         
      zipStream.closeEntry();
      entry = zipStream.getNextEntry();
    }
    bos.flush();
    bos.close();
    zipStream.close();
    return new BufferedInputStream(new FileInputStream(file));
  }
  
  public static String getThumbnailImage(Node node, String propertyName) throws Exception {
    ThumbnailService thumbnailService = 
      Util.getUIPortal().getApplicationComponent(ThumbnailService.class);
    if(node.getPrimaryNodeType().getName().equals(NT_FILE)) {
      String mimeType = node.getNode(JCR_CONTENT).getProperty(JCR_MIMETYPE).getString();
      if(mimeType.startsWith("image")) {
        Node thumbnailNode = thumbnailService.addThumbnailNode(node);
        InputStream inputStream = node.getNode(JCR_CONTENT).getProperty(JCR_DATA).getStream();
        thumbnailService.createSpecifiedThumbnail(thumbnailNode, ImageIO.read(inputStream), propertyName);
      }
    }
    Node thumbnailNode = thumbnailService.getThumbnailNode(node);
    if(thumbnailNode != null && thumbnailNode.hasProperty(propertyName)) {
      DownloadService dservice = Util.getUIPortal().getApplicationComponent(DownloadService.class);
      InputStream input = thumbnailNode.getProperty(propertyName).getStream();
      InputStreamDownloadResource dresource = new InputStreamDownloadResource(input, "image");
      dresource.setDownloadName(node.getName());
      return dservice.getDownloadLink(dservice.addDownloadResource(dresource));
    }
    return null;
  }
}
