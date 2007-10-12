/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.workflow.utils;

import java.security.AccessControlException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
import org.exoplatform.webui.form.UIFormUploadInput;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jun 13, 2007 9:00:45 AM
 */
public class Utils {
  
  public static String SYSTEM_SUFFIX = ":/" + SystemIdentity.SYSTEM ;
  public static String ANONIM_SUFFIX = ":/" + SystemIdentity.ANONIM ;
  
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
  final static public String MIX_REFERENCEABLE = "mix:referenceable" ;
  final static public String MIX_VERSIONABLE = "mix:versionable" ;
  final static public String NT_RESOURCE = "nt:resource" ;
  final static public String DEFAULT = "default" ;
  final static public String JCR_CONTENT = "jcr:content" ;
  final static public String JCR_MIMETY = "jcr:mimeType" ;
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
  final static public String JCR_MIMETYPE = "jcr:mimeType" ;

  @SuppressWarnings({"unchecked", "unused"})
  public static Map prepareMap(List inputs, Map properties, Session session) throws Exception {
    Map<String, JcrInputProperty> rawinputs = new HashMap<String, JcrInputProperty>();
    for (int i = 0; i < inputs.size(); i++) {
      JcrInputProperty property ;
      if(inputs.get(i) instanceof UIFormMultiValueInputSet) {
        String inputName = ((UIFormMultiValueInputSet)inputs.get(i)).getName() ;
        List<String> values = (List<String>) ((UIFormMultiValueInputSet)inputs.get(i)).getValue() ;
        property = (JcrInputProperty) properties.get(inputName);        
        if(property != null){          
          property.setValue(values.toArray(new String[values.size()])) ;
        } 
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
  
  public static SessionProvider getSystemProvider() {   
    String key = Util.getPortalRequestContext().getSessionId() + SYSTEM_SUFFIX;
    return getJcrSessionProvider(key) ;
  }    

  public static SessionProvider getSessionProvider() {    
    String key = Util.getPortalRequestContext().getSessionId();
    return getJcrSessionProvider(key) ;
  }
  
  public static SessionProvider getAnonimProvider() {
    String key = Util.getPortalRequestContext().getSessionId() + ANONIM_SUFFIX ;
    return getJcrSessionProvider(key) ;
  } 

  private static SessionProvider getJcrSessionProvider(String key) {    
    SessionProviderService service = 
      (SessionProviderService)PortalContainer.getComponent(SessionProviderService.class) ;    
    SessionProvider sessionProvider = null ;    
    try{
      sessionProvider = service.getSessionProvider(key) ;
      return sessionProvider ;
    }catch (NullPointerException e) {
      if(key.indexOf(SYSTEM_SUFFIX)>0) {
        sessionProvider = SessionProvider.createSystemProvider() ;
        service.setSessionProvider(key,sessionProvider) ;
        return sessionProvider ;
      }else if(key.indexOf(ANONIM_SUFFIX)>0) {
        sessionProvider = SessionProvider.createAnonimProvider() ;
        service.setSessionProvider(key,sessionProvider) ;
        return sessionProvider ;
      }else {
        sessionProvider = new SessionProvider(null) ;
        service.setSessionProvider(key,sessionProvider) ;
        return sessionProvider ;
      }
    }   
  }
  
  public static boolean isReadAuthorized(Node node) throws RepositoryException {
    try {
      ((ExtendedNode)node).checkPermission(PermissionType.READ);
      return true;
    } catch(AccessControlException e) {
      return false;
    }    
  }
  
  public static String getNodeTypeIcon(Node node, String appended, String mode) throws RepositoryException {
    StringBuilder str = new StringBuilder() ;
    if(isReadAuthorized(node)) {
      String nodeType = node.getPrimaryNodeType().getName().replaceAll(":", "_") + appended ;
      str.append(nodeType) ;
      if(mode != null && mode.equalsIgnoreCase("Collapse")) str.append(" ").append(mode).append(nodeType) ;
      if(node.isNodeType(NT_FILE)) {
        Node jcrContentNode = node.getNode(JCR_CONTENT) ;
        str.append(" ").append(jcrContentNode.getProperty(JCR_MIMETYPE).getString().replaceAll("/|\\.","_")).append(appended);
      }
    }
    return str.toString() ;
  }

  public static String getNodeTypeIcon(Node node, String appended) throws RepositoryException {
    return getNodeTypeIcon(node, appended, null) ;
  }
}
