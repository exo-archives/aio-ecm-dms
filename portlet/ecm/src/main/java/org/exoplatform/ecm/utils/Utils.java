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
package org.exoplatform.ecm.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import javax.portlet.PortletPreferences;

import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
import org.exoplatform.webui.form.UIFormUploadInput;

public class Utils {
  final public static String WORKSPACE_NAME = "workspace".intern() ;   
  final public static String JCR_PATH = "path".intern() ; 
  final public static String DRIVE_FOLDER = "allowCreateFolder".intern() ; 
  final public static String MIN_WIDTH = "minwidth".intern() ;
  final public static String CB_DOCUMENT_NAME = "documentName".intern() ;
  final public static String CB_SCRIPT_NAME = "scriptName".intern() ;
  final public static String CB_REF_DOCUMENT = "reference".intern() ;  
  final public static String CB_CHILD_DOCUMENT = "child".intern();
  final public static String CB_NB_PER_PAGE = "nbPerPage".intern() ;
  final public static String CB_QUERY_STATEMENT = "queryStatement".intern() ;
  final public static String CB_QUERY_ISNEW = "isAddNew".intern() ;
  final public static String CB_QUERY_TYPE = "queryType".intern() ;
  final public static String CB_QUERY_STORE = "queryStore".intern() ;
  final public static String CB_QUERY_LANGUAGE = "queryLanguage".intern() ;
  final public static String CB_VIEW_TOOLBAR = "viewToolbar".intern();
  final public static String CB_VIEW_TAGMAP = "viewTagMap".intern();
  final public static String CB_VIEW_COMMENT = "viewComment".intern();
  final public static String CB_VIEW_VOTE= "viewVote".intern();

  final public static String CB_BOX_TEMPLATE = "boxTemplate".intern();   
  final public static String CB_TEMPLATE = "template" ;
  final public static String CB_USECASE = "usecase".intern() ;

  final public static String FROM_PATH = "From Path".intern() ;
  final public static String USE_DOCUMENT = "Document".intern() ;
  final public static String USE_JCR_QUERY = "Using a JCR query".intern() ;
  final public static String USE_SCRIPT = "Using a script".intern() ;

  final public static String CB_USE_FROM_PATH = "path".intern() ;
  final public static String CB_USE_DOCUMENT = "detail-document".intern() ;
  final public static String CB_USE_JCR_QUERY = "query".intern() ;
  final public static String CB_USE_SCRIPT = "script".intern() ;  

  final public static String SEMI_COLON = ";".intern() ;
  final public static String COLON = ":".intern() ;
  final public static String SLASH = "/".intern() ;
  final public static String BACKSLASH = "\\".intern() ;
  final public static String EXO_CREATED_DATE = "exo:dateCreated" ;
  final public static String EXO_MODIFIED_DATE = "exo:dateModified" ;


  final public static String SPECIALCHARACTER[] = {SEMI_COLON,COLON,SLASH,BACKSLASH,"'","|",">","<","\"", "?", "!", "@", "#", "$", "%","^","&","*"} ;
  final public static String REPOSITORY = "repository".intern() ;
  final public static String VIEWS = "views".intern() ;
  final public static String DRIVE = "drive".intern() ;
  final public static String JCR_INFO = "jcrInfo";
  final static public String NT_UNSTRUCTURED = "nt:unstructured" ;
  final static public String NT_FILE = "nt:file" ;
  final static public String NT_FOLDER = "nt:folder" ;
  final static public String NT_FROZEN = "nt:frozenNode".intern() ;
  final static public String EXO_TITLE = "exo:title" ;
  final static public String EXO_SUMMARY = "exo:summary" ;
  final static public String EXO_RELATION = "exo:relation" ;
  final static public String EXO_TAXANOMY = "exo:taxonomy" ;
  final static public String EXO_IMAGE = "exo:image" ;
  final static public String EXO_ARTICLE = "exo:article" ;
  final static public String EXO_LANGUAGE = "exo:language" ;
  final static public String LANGUAGES = "languages" ;
  final static public String EXO_METADATA = "exo:metadata" ;
  final static public String MIX_REFERENCEABLE = "mix:referenceable" ;
  final static public String MIX_VERSIONABLE = "mix:versionable" ;
  final static public String NT_RESOURCE = "nt:resource" ;
  final static public String DEFAULT = "default" ;
  final static public String JCR_CONTENT = "jcr:content" ;
  final static public String JCR_MIMETYPE = "jcr:mimeType" ;
  final static public String JCR_FROZEN = "jcr:frozenNode" ;
  final public static String JCR_LASTMODIFIED = "jcr:lastModified" ;
  final static public String JCR_DATA = "jcr:data" ;
  final static public String EXO_ROLES = "exo:roles" ;
  final static public String EXO_TEMPLATEFILE = "exo:templateFile" ;
  final static public String EXO_TEMPLATE = "exo:template" ;
  final static public String EXO_ACTION = "exo:action" ;
  final static public String MIX_LOCKABLE = "mix:lockable" ;
  final static public String EXO_CATEGORIZED = "exo:categorized" ;
  final static public String EXO_CATEGORY = "exo:category" ;

  final static public String EXO_MUSICFOLDER = "exo:musicFolder";
  final static public String EXO_VIDEOFOLDER = "exo:videoFolder";
  final static public String EXO_PICTUREFOLDER = "exo:pictureFolder";
  final static public String EXO_DOCUMENTFOLDER = "exo:documentFolder";
  final static public String EXO_SEARCHFOLDER = "exo:searchFolder";
  final static public String[] SPECIFIC_FOLDERS = {EXO_MUSICFOLDER,EXO_VIDEOFOLDER,EXO_PICTUREFOLDER,EXO_DOCUMENTFOLDER,EXO_SEARCHFOLDER };

  final static public String[] FOLDERS = {NT_UNSTRUCTURED, NT_FOLDER};
  final static public String[] NON_EDITABLE_NODETYPES = {NT_UNSTRUCTURED, NT_FOLDER, NT_RESOURCE};
  final public static String[] CATEGORY_NODE_TYPES = {NT_FOLDER, NT_UNSTRUCTURED, EXO_TAXANOMY} ;  
  public Map<String, Object> maps_ = new HashMap<String, Object>() ;

  public static String encodeHTML(String text) {
    return text.replaceAll("&", "&amp;").replaceAll("\"", "&quot;")
    .replaceAll("<", "&lt;").replaceAll(">", "&gt;") ;
  }

  public static String formatNodeName(String text) {
    return text.replaceAll("'", "\\\\'") ;
  }

  public static boolean isVersionable(Node node) throws RepositoryException {
    return node.isNodeType(MIX_VERSIONABLE) && !node.isNodeType(JCR_FROZEN);
  }

  public static boolean isReadAuthorized(Node node) throws RepositoryException {
    try {
      ((ExtendedNode)node).checkPermission(PermissionType.READ);
      return true;
    } catch(AccessControlException e) {
      return false;
    }    
  }

  public static boolean isAddNodeAuthorized(Node node) throws RepositoryException {
    try {
      ((ExtendedNode)node).checkPermission(PermissionType.ADD_NODE);
      return true;
    } catch(AccessControlException e) {
      return false;
    }    
  }

  public static boolean isChangePermissionAuthorized(Node node) throws RepositoryException {
    try {
      ((ExtendedNode)node).checkPermission(PermissionType.CHANGE_PERMISSION);
      return true;
    } catch(AccessControlException e) {
      return false;
    }    
  }

  public static boolean hasChangePermissionRight(Node node) throws RepositoryException {
    return isAddNodeAuthorized(node) && isSetPropertyNodeAuthorized(node) && isRemoveNodeAuthorized(node) ;
  }
  public static boolean isAnyPermissionAuthorized(Node node)throws RepositoryException {
    try {
      ((ExtendedNode)node).checkPermission(SystemIdentity.ANY);
      return true;
    } catch(AccessControlException e) {
      return false;
    }    
  }

  public static boolean isSetPropertyNodeAuthorized(Node node) throws RepositoryException {
    try {
      ((ExtendedNode)node).checkPermission(PermissionType.SET_PROPERTY);
    } catch(AccessControlException e) {
      return false;
    }
    return true ;
  }

  public static boolean isRemoveNodeAuthorized(Node node) throws RepositoryException {
    try {
      ((ExtendedNode)node).checkPermission(PermissionType.REMOVE);
      return true;
    } catch(AccessControlException e) {
      return false;
    }    
  }

  public static boolean isNodeAuthorized(Node node, String owner) throws Exception {
    return ((ExtendedNode)node).getACL().getOwner().equals(owner) ;
  }

  static public class NodeTypeNameComparator implements Comparator {
    public int compare(Object o1, Object o2) throws ClassCastException {
      String name1 = ((NodeType) o1).getName() ;
      String name2 = ((NodeType) o2).getName() ;
      return name1.compareToIgnoreCase(name2) ;
    }
  }

  public static boolean isNameValid(String name, String[] regexpression) {
    for(String c : regexpression){ if(name.contains(c)) return false ;}
    return true ;
  }

  public static boolean isNameEmpty(String name) {
    return (name == null || name.trim().length() == 0) ;
  }

  public static List<String> getListAllowedFileType(Node currentNode, String repository, TemplateService templateService) throws Exception {
    List<String> nodeTypes = new ArrayList<String>() ;
    NodeTypeManager ntManager = currentNode.getSession().getWorkspace().getNodeTypeManager() ; 
    NodeType currentNodeType = currentNode.getPrimaryNodeType() ; 
    NodeDefinition[] childDefs = currentNodeType.getChildNodeDefinitions() ;
    List templates = templateService.getDocumentTemplates(repository) ;
    try {
      for(int i = 0; i < templates.size(); i ++){
        String nodeTypeName = templates.get(i).toString() ; 
        NodeType nodeType = ntManager.getNodeType(nodeTypeName) ;
        NodeType[] superTypes = nodeType.getSupertypes() ;
        boolean isCanCreateDocument = false ;
        for(NodeDefinition childDef : childDefs){
          NodeType[] requiredChilds = childDef.getRequiredPrimaryTypes() ;
          for(NodeType requiredChild : requiredChilds) {          
            if(nodeTypeName.equals(requiredChild.getName())){            
              isCanCreateDocument = true ;
              break ;
            }            
          }
          if(nodeTypeName.equals(childDef.getName()) || isCanCreateDocument) {
            if(!nodeTypes.contains(nodeTypeName)) nodeTypes.add(nodeTypeName) ;
            isCanCreateDocument = true ;          
          }
        }      
        if(!isCanCreateDocument){
          for(NodeType superType:superTypes) {
            for(NodeDefinition childDef : childDefs){          
              for(NodeType requiredType : childDef.getRequiredPrimaryTypes()) {              
                if (superType.getName().equals(requiredType.getName())) {
                  if(!nodeTypes.contains(nodeTypeName)) nodeTypes.add(nodeTypeName) ;
                  isCanCreateDocument = true ;
                  break;
                }
              }
              if(isCanCreateDocument) break ;
            }
            if(isCanCreateDocument) break ;
          }
        }            
      }
    } catch(Exception e) {
      e.printStackTrace() ;
    }
    return nodeTypes ;
  }

  public static String getNodeTypeIcon(Node node, String appended, String mode) throws RepositoryException {
    StringBuilder str = new StringBuilder() ;
    String nodeType = node.getPrimaryNodeType().getName() ;
    //String nodeType = node.getPrimaryNodeType().getName().replaceAll(":", "_") + appended ;
    if(nodeType.equals(NT_UNSTRUCTURED) || nodeType.equals(NT_FOLDER)) {
      for(String specificFolder:SPECIFIC_FOLDERS) {
        if(node.isNodeType(specificFolder)) {
          nodeType = specificFolder;
          break;
        }
      }
    }
    nodeType = nodeType.replaceAll(":","_") + appended ;    
    str.append(nodeType) ;
    if(mode != null && mode.equalsIgnoreCase("Collapse")) str.append(" ").append(mode).append(nodeType) ;
    if(node.isNodeType(NT_FILE)) {
      Node jcrContentNode = node.getNode(JCR_CONTENT) ;
      str.append(" ").append(jcrContentNode.getProperty(JCR_MIMETYPE).getString().replaceAll("/|\\.","_")).append(appended);      
    }
    return str.toString() ;
  }

  public static String getNodeTypeIcon(Node node, String appended) throws RepositoryException {
    return getNodeTypeIcon(node, appended, null) ;
  }

  public String getPropertyName(String jcrPath) { 
    return jcrPath.substring(jcrPath.lastIndexOf("/") + 1) ; 
  }

  public static NodeIterator getAuthorizedChildNodes(Node node) throws Exception {
    NodeIterator iter = node.getNodes() ;
    while(iter.hasNext()) {
      if(!isReadAuthorized(iter.nextNode())) iter.remove() ; 
    }  
    return iter ;
  }

  public static List<Node> getAuthorizedChildList(Node node) throws Exception {
    List<Node> children = new ArrayList<Node>() ;
    NodeIterator iter = node.getNodes() ;
    while(iter.hasNext()) {
      Node child = iter.nextNode() ;
      if(isReadAuthorized(child)) children.add(child) ;
    }  
    return children ;
  }

  public static boolean isLockTokenHolder(Node node) throws Exception {
    if(node.getLock().getLockToken() != null) { 
      return true ; 
    } 
    return false ;    
  }

  public static String getRepository() {
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;   
    PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
    return portletPref.getValue(Utils.REPOSITORY, "") ;
  }

  @SuppressWarnings({"unchecked", "unused"})
  public static Map prepareMap(List inputs, Map properties, Session session) throws Exception {
    Map<String, JcrInputProperty> rawinputs = new HashMap<String, JcrInputProperty>();
    HashMap<String, JcrInputProperty> hasMap = new HashMap<String, JcrInputProperty>() ;
    for (int i = 0; i < inputs.size(); i++) {
      JcrInputProperty property = null;
      if(inputs.get(i) instanceof UIFormMultiValueInputSet) {
        String inputName = ((UIFormMultiValueInputSet)inputs.get(i)).getName() ;
        if(!hasMap.containsKey(inputName)) {
          List<String> values = (List<String>) ((UIFormMultiValueInputSet)inputs.get(i)).getValue() ;
          property = (JcrInputProperty) properties.get(inputName);        
          if(property != null){          
            property.setValue(values.toArray(new String[values.size()])) ;
          }
        }
        hasMap.put(inputName, property) ;
      } else {
        UIFormInputBase input = (UIFormInputBase) inputs.get(i);
        property = (JcrInputProperty) properties.get(input.getName());
        if(property != null) {
          if (input instanceof UIFormUploadInput) {
            byte[] content = ((UIFormUploadInput) input).getUploadData() ; 
            property.setValue(content);
          } else if(input instanceof UIFormDateTimeInput) {
            property.setValue(((UIFormDateTimeInput)input).getCalendar()) ;
          } else {
            property.setValue(input.getValue()) ;
          }
        }
      }
    }
    Iterator iter = properties.values().iterator() ;
    JcrInputProperty property ;
    while (iter.hasNext()) {
      property = (JcrInputProperty) iter.next() ;
      rawinputs.put(property.getJcrPath(), property) ;
    }
    return rawinputs;
  }

  public static List<String> getMemberships() throws Exception {
    String userId = Util.getPortalRequestContext().getRemoteUser() ;
    OrganizationService oservice = Util.getUIPortal().getApplicationComponent(OrganizationService.class) ;
    List<String> userMemberships = new ArrayList<String> () ;
    userMemberships.add(userId) ;
    Collection memberships = oservice.getMembershipHandler().findMembershipsByUser(userId) ;
    if(memberships == null || memberships.size() < 0) return userMemberships ;
    Object[] objects = memberships.toArray() ;
    for(int i = 0 ; i < objects.length ; i ++ ){
      Membership membership = (Membership)objects[i] ;
      String role = membership.getMembershipType() + ":" + membership.getGroupId() ;
      userMemberships.add(role) ;     
    }
    return userMemberships ;
  }

  public static List<String> getGroups() throws Exception {
    String userId = Util.getPortalRequestContext().getRemoteUser() ;
    OrganizationService oservice = Util.getUIPortal().getApplicationComponent(OrganizationService.class) ;
    List<String> groupList = new ArrayList<String> () ;
    Collection groups = oservice.getGroupHandler().findGroupsOfUser(userId) ;
    Object[] objects = groups.toArray() ;
    for(int i = 0 ; i < objects.length ; i ++ ){
      Group group = (Group)objects[i] ;
      String groupPath = null ;
      if(group.getParentId() == null || group.getParentId().length() == 0) groupPath = "/" + group.getGroupName() ; 
      else groupPath = group.getParentId() + "/" + group.getGroupName() ; 
      groupList.add(groupPath) ;
    }
    return groupList ;
  }

  public static String getNodeOwner(Node node) throws Exception {
    try {
      if(node.hasProperty("exo:owner")) {
        return node.getProperty("exo:owner").getString();
      }
    } catch (Exception e) {
      //e.printStackTrace() ;
    } 
    return null ;
  }

  public static ByteArrayInputStream extractFromZipFile(ZipInputStream zipStream) throws Exception {
    ByteArrayOutputStream out= new ByteArrayOutputStream();
    byte[] data  = new byte[1024];   
    ZipEntry entry = zipStream.getNextEntry() ;
    while(entry != null) {
      int available = -1 ;
      while ((available = zipStream.read(data, 0, 1024)) > -1) {
        out.write(data, 0, available); 
      }                         
      zipStream.closeEntry();
      entry = zipStream.getNextEntry();
    }
    out.close();
    zipStream.close();
    ByteArrayInputStream inputStream = new ByteArrayInputStream(out.toByteArray()) ;
    return inputStream ;
  }

}