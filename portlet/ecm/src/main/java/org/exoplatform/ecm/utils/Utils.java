package org.exoplatform.ecm.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.webui.component.UIFormInputBase;
import org.exoplatform.webui.component.UIFormMultiValueInputSet;
import org.exoplatform.webui.component.UIFormUploadInput;

public class Utils {
	final public static String WORKSPACE_NAME = "workspace".intern() ;   
  final public static String JCR_PATH = "path".intern() ; 
  
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
  
  final public static String VIEWS = "views".intern() ;
  final public static String DRIVE = "drive".intern() ;
  final public static String JCR_INFO = "jcrInfo";
  final static public String NT_UNSTRUCTURED = "nt:unstructured" ;
  final static public String NT_FILE = "nt:file" ;
  final static public String NT_FOLDER = "nt:folder" ;
  final static public String NT_FROZEN = "nt:frozenNode" ;
  final static public String EXO_TITLE = "exo:title" ;
  final static public String EXO_SUMMARY = "exo:summary" ;
  final static public String EXO_RELATION = "exo:relation" ;
  final static public String EXO_TAXANOMY = "exo:taxonomy" ;
  final static public String MIX_REFERENCEABLE = "mix:referenceable" ;
  final static public String MIX_VERSIONABLE = "mix:versionable" ;
  final static public String NT_RESOURCE = "nt:resource" ;
  final static public String DEFAULT = "default" ;
  final static public String JCR_CONTENT = "jcr:content" ;
  final static public String JCR_MIMETY = "jcr:mimeType" ;
  final static public String JCR_FROZEN = "jcr:frozenNode" ;
  final static public String JCR_DATA = "jcr:data" ;
  final static public String EXO_ROLES = "exo:roles" ;
  final static public String EXO_TEMPLATE = "exo:templateFile" ;
  final static public String MIX_LOCKABLE = "mix:lockable" ;
  final static public String EXO_CATEGORIZED = "exo:categorized" ;
  final static public String EXO_CATEGORY = "exo:category" ;
  final static public String[] NON_EDITABLE_NODETYPES = {NT_UNSTRUCTURED, NT_FOLDER, NT_RESOURCE};
  final public static String[] CATEGORY_NODE_TYPES = {NT_FOLDER, NT_UNSTRUCTURED, EXO_TAXANOMY} ; 
  
  public static String getIconClass(Node node) throws RepositoryException {
    return node.getPrimaryNodeType().getName().replaceAll(":", "_") ;
  }
  
  public static String getNodeTypeIcon(Node node, String appended, String mode) throws RepositoryException {
    String nodeType = node.getPrimaryNodeType().getName().replaceAll(":", "_") + appended ;
    StringBuilder str = new StringBuilder(nodeType) ;
    if(mode != null && mode.equalsIgnoreCase("Collapse")) str.append(" ").append(mode).append(nodeType) ;
    if(node.isNodeType(NT_FILE)) {
      Node jcrContentNode = node.getNode(JCR_CONTENT) ;
      str.append(" ").append(jcrContentNode.getProperty(JCR_MIMETY).getString().replaceFirst("/", "_")).append(appended);
    }
    return str.toString() ;
  }
  
  public static String getNodeTypeIcon(Node node, String appended) throws RepositoryException {
    return getNodeTypeIcon(node, appended, null) ;
  }
  
  public static Value[] getMultiValue(List<String> values, int valueType, Session session) throws Exception {
    List<Value> valueList = new ArrayList<Value>();
    for (int i = 0; i < values.size(); i++) {
      String value = values.get(i);
      if (value != null && value.length() > 0) {
        if (valueType == 6) { // boolean
          valueList.add(session.getValueFactory().createValue(Boolean.parseBoolean(value)));
        } else {
          valueList.add(session.getValueFactory().createValue(value));
        }
      }
    }
    return valueList.toArray(new Value[] {});
  }
  
  @SuppressWarnings("unchecked")
  public static Map prepareMap(List inputs, Map properties, Session session) throws Exception {
    Map<String, JcrInputProperty> rawinputs = new HashMap<String, JcrInputProperty>();
    for (int i = 0; i < inputs.size(); i++) {
      JcrInputProperty property ;
      if(inputs.get(i) instanceof UIFormMultiValueInputSet) {
        String inputName = ((UIFormMultiValueInputSet)inputs.get(i)).getName() ;
        List<String> values = (List<String>) ((UIFormMultiValueInputSet)inputs.get(i)).getValue() ;
        property = (JcrInputProperty) properties.get(inputName);
        Value[] multiValue = getMultiValue(values, property.getType(), session);
        if(property != null) property.setValue(multiValue) ;
      } else {
        UIFormInputBase input = (UIFormInputBase) inputs.get(i);
        property = (JcrInputProperty) properties.get(input.getName());
        if(property != null) {
          if (input instanceof UIFormUploadInput) {
            byte[] content = ((UIFormUploadInput) input).getUploadData() ; 
            property.setValue(content);            
          } else {
            property.setValue(input.getValue());            
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
}
