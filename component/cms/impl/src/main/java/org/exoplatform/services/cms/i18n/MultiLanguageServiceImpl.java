package org.exoplatform.services.cms.i18n;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.services.cms.JcrInputProperty;

/**
 * @author Hung Nguyen Quang
 * @mail   nguyenkequanghung@yahoo.com
 */

public class MultiLanguageServiceImpl implements MultiLanguageService{
  final static public String  JCRCONTENT = "jcr:content";
  final static public String  JCRDATA = "jcr:data";
  final static public String  NTUNSTRUCTURED = "nt:unstructured";
  final static String VOTER_PROP = "exo:voter".intern() ;  
  final static String VOTING_RATE_PROP = "exo:votingRate".intern() ;
  final static String VOTE_TOTAL_PROP = "exo:voteTotal".intern() ; 
  final static String VOTE_TOTAL_LANG_PROP = "exo:voteTotalOfLang".intern() ;
  final static String NODE = "/node/" ;
  final static String CONTENT_PATH = "/node/jcr:content/" ;
  final static String TEMP_NODE = "temp" ;
  
  public MultiLanguageServiceImpl() throws Exception {
  }

  private void setPropertyValue(String propertyName, Node node, int requiredtype, Object value, boolean isMultiple) throws Exception {
    switch (requiredtype) {
    case PropertyType.STRING:
      if (value == null) {
        node.setProperty(propertyName, "");
      } else {
        if(isMultiple) {
          if (value instanceof String) node.setProperty(propertyName, new String[] { value.toString()});
          else if(value instanceof String[]) node.setProperty(propertyName, (String[]) value);
        } else {
          node.setProperty(propertyName, value.toString());
        }
      }
      break;
    case PropertyType.BINARY:
      if (value == null) node.setProperty(propertyName, "");
      else if (value instanceof byte[]) node.setProperty(propertyName, new ByteArrayInputStream((byte[]) value));
      else if (value instanceof String) node.setProperty(propertyName, new ByteArrayInputStream((value.toString()).getBytes()));
      else if (value instanceof String[]) node.setProperty(propertyName, new ByteArrayInputStream((((String[]) value)).toString().getBytes()));      
      break;
    case PropertyType.BOOLEAN:
      if (value == null) node.setProperty(propertyName, false);
      else if (value instanceof String) node.setProperty(propertyName, new Boolean(value.toString()).booleanValue());
      else if (value instanceof String[]) node.setProperty(propertyName, (String[]) value);         
      break;
    case PropertyType.LONG:
      if (value == null || "".equals(value)) node.setProperty(propertyName, 0);
      else if (value instanceof String) node.setProperty(propertyName, new Long(value.toString()).longValue());
      else if (value instanceof String[]) node.setProperty(propertyName, (String[]) value);  
      break;
    case PropertyType.DOUBLE:
      if (value == null || "".equals(value)) node.setProperty(propertyName, 0);
      else if (value instanceof String) node.setProperty(propertyName, new Double(value.toString()).doubleValue());
      else if (value instanceof String[]) node.setProperty(propertyName, (String[]) value);        
      break;
    case PropertyType.DATE:      
      if (value == null) {        
        node.setProperty(propertyName, new GregorianCalendar());
      } else {
        if(isMultiple) {
          Session session = node.getSession() ;
          if (value instanceof String) {
            Value value2add = session.getValueFactory().createValue(ISO8601.parse((String) value));
            node.setProperty(propertyName, new Value[] {value2add});
          } else if (value instanceof String[]) {
            String[] values = (String[]) value;
            Value[] convertedCalendarValues = new Value[values.length];
            int i = 0;
            for (String stringValue : values) {
              Value value2add = session.getValueFactory().createValue(ISO8601.parse(stringValue));
              convertedCalendarValues[i] = value2add;
              i++;
            }
            node.setProperty(propertyName, convertedCalendarValues);       
          }
        } else {
          if (value instanceof String) {
            node.setProperty(propertyName, ISO8601.parse(value.toString()));
          } else if (value instanceof GregorianCalendar) {
            node.setProperty(propertyName, (GregorianCalendar) value);
          } 
        }
      }
      break ;
    }
  }
  
  public void addLanguage(Node node, Map inputs, String language, boolean isDefault) throws Exception {
    Node newLanguageNode = null ;
    Node languagesNode = null ;
    String defaultLanguage = getDefault(node) ;
    if(node.hasNode(LANGUAGES)) languagesNode = node.getNode(LANGUAGES) ;
    else languagesNode = node.addNode(LANGUAGES, NTUNSTRUCTURED) ;
    if(!defaultLanguage.equals(language)){
      if(isDefault) {
        if(languagesNode.hasNode(defaultLanguage)) {
          newLanguageNode = languagesNode.getNode(defaultLanguage) ;
        } else {
          newLanguageNode = languagesNode.addNode(defaultLanguage) ;
          NodeType[] mixins = node.getMixinNodeTypes() ;
          for(NodeType mixin:mixins) {
            newLanguageNode.addMixin(mixin.getName()) ;            
          }
        }
      } else {
        if(languagesNode.hasNode(language)) {
          newLanguageNode = languagesNode.getNode(language) ;
        } else {
          newLanguageNode = languagesNode.addNode(language) ;
          NodeType[] mixins = node.getMixinNodeTypes() ;
          for(NodeType mixin:mixins) {
            newLanguageNode.addMixin(mixin.getName()) ;
            for(PropertyDefinition def: mixin.getPropertyDefinitions()) {
              String propName = def.getName() ;
              if(def.isMandatory() && !def.isAutoCreated()) {
                if(def.isMultiple()) {
                  newLanguageNode.setProperty(propName,node.getProperty(propName).getValues()) ;
                } else {
                  newLanguageNode.setProperty(propName,node.getProperty(propName).getValue()) ; 
                }
              }        
            }
          }
        }
      }
    }   
    PropertyDefinition[] properties = node.getPrimaryNodeType().getPropertyDefinitions() ;
    for(PropertyDefinition pro : properties){
      if(!pro.isProtected()) {
        String propertyName = pro.getName() ;
        JcrInputProperty property = (JcrInputProperty)inputs.get(NODE + propertyName) ;
        if(defaultLanguage.equals(language)) {
          node.setProperty(propertyName, property.getValue().toString()) ;
        } else {          
          if(isDefault) {            
            if(node.hasProperty(propertyName)) {
              newLanguageNode.setProperty(propertyName, node.getProperty(propertyName).getValue()) ;
            }
            if(node.hasProperty(propertyName) && property != null) {
              boolean isMultiple = node.getProperty(propertyName).getDefinition().isMultiple() ;
              setPropertyValue(propertyName, node, node.getProperty(propertyName).getType(), property.getValue(), isMultiple) ;
            }
          } else {
            if(node.hasProperty(propertyName) && property != null) {
              boolean isMultiple = node.getProperty(propertyName).getDefinition().isMultiple() ;
              setPropertyValue(propertyName, newLanguageNode, node.getProperty(propertyName).getType(), property.getValue(), isMultiple) ;
            }
          }
        }               
      }
    }
    if(isDefault) node.setProperty(EXO_LANGUAGE, language) ;
    if(isDefault && languagesNode.hasNode(language)) languagesNode.getNode(language).remove() ;
    node.save() ;
    node.getSession().save() ;    
  }
  
  public void addFileLanguage(Node node, Value value, String language, boolean isDefault) throws Exception {
    Node newLanguageNode = null ;
    Node languagesNode = null ;
    Workspace ws = node.getSession().getWorkspace() ;
    String defaultLanguage = getDefault(node) ;
    if(node.hasNode(LANGUAGES)) languagesNode = node.getNode(LANGUAGES) ;
    else languagesNode = node.addNode(LANGUAGES, NTUNSTRUCTURED) ;
    if(!defaultLanguage.equals(language)){
      if(isDefault) {
        if(languagesNode.hasNode(defaultLanguage)) newLanguageNode = languagesNode.getNode(defaultLanguage) ;
        else newLanguageNode = languagesNode.addNode(defaultLanguage) ;
        Node jcrContent = node.getNode(JCRCONTENT) ;
        node.save() ;
        ws.copy(jcrContent.getPath(), newLanguageNode.getPath() + "/" + jcrContent.getName()) ;
        jcrContent.setProperty(JCRDATA, value) ;
      } else {
        if(languagesNode.hasNode(language)) newLanguageNode = languagesNode.getNode(language) ;
        else newLanguageNode = languagesNode.addNode(language) ;
        Node jcrContent = node.getNode(JCRCONTENT) ;
        node.save() ;
        ws.copy(jcrContent.getPath(), newLanguageNode.getPath() + "/" + jcrContent.getName()) ;
        newLanguageNode.getNode(JCRCONTENT).setProperty(JCRDATA, value) ;        
      }
    }    
    // add mixin type for node
    NodeType[] mixins = node.getMixinNodeTypes() ;
    for(NodeType mixin:mixins) {
      newLanguageNode.addMixin(mixin.getName()) ;
      for(PropertyDefinition def: mixin.getPropertyDefinitions()) {
        String propName = def.getName() ;
        if(def.isMandatory() && !def.isAutoCreated()) {
          if(def.isMultiple()) {
            newLanguageNode.setProperty(propName,node.getProperty(propName).getValues()) ;
          } else {
            newLanguageNode.setProperty(propName,node.getProperty(propName).getValue()) ; 
          }
        }        
      }
    }
    
    if(isDefault) node.setProperty(EXO_LANGUAGE, language) ;
    node.save() ;
    node.getSession().save() ;    
  }
  
  public void addFileLanguage(Node node, String language, Map mappings, boolean isDefault) throws Exception {
    Node newLanguageNode = null ;
    Node languagesNode = null ;
    Workspace ws = node.getSession().getWorkspace() ;
    String defaultLanguage = getDefault(node) ;
    if(node.hasNode(LANGUAGES)) languagesNode = node.getNode(LANGUAGES) ;
    else languagesNode = node.addNode(LANGUAGES, NTUNSTRUCTURED) ;
    if(!defaultLanguage.equals(language)){
      if(isDefault) {
        if(languagesNode.hasNode(defaultLanguage)) newLanguageNode = languagesNode.getNode(defaultLanguage) ;
        else newLanguageNode = languagesNode.addNode(defaultLanguage) ;
      } else {
        if(languagesNode.hasNode(language)) newLanguageNode = languagesNode.getNode(language) ;
        else newLanguageNode = languagesNode.addNode(language) ;
      }
      Node jcrContent = node.getNode(JCRCONTENT) ;
      node.save() ;
      ws.copy(jcrContent.getPath(), newLanguageNode.getPath() + "/" + jcrContent.getName()) ;
      Node newContentNode = newLanguageNode.getNode(JCRCONTENT) ;
      PropertyIterator props = newContentNode.getProperties() ;
      while(props.hasNext()) {
        Property prop = props.nextProperty() ;
        if(mappings.containsKey(CONTENT_PATH + prop.getName())) {
          JcrInputProperty inputVariable = (JcrInputProperty) mappings.get(CONTENT_PATH + prop.getName()) ;
          boolean isMultiple = prop.getDefinition().isMultiple() ;
          setPropertyValue(prop.getName(), newContentNode, prop.getType(), inputVariable.getValue(), isMultiple) ;
        }
      }
      if(isDefault) {
        Node tempNode = node.addNode(TEMP_NODE, "nt:unstructured") ;
        node.getSession().move(node.getNode(JCRCONTENT).getPath(), tempNode.getPath() + "/" + JCRCONTENT) ;
        node.getSession().move(newLanguageNode.getNode(JCRCONTENT).getPath(), node.getPath() + "/" + JCRCONTENT) ;
        node.getSession().move(tempNode.getNode(JCRCONTENT).getPath(), languagesNode.getPath() + "/" + defaultLanguage + "/" + JCRCONTENT) ;
        tempNode.remove() ;
      }
    }    
    // add mixin type for node
    NodeType[] mixins = node.getMixinNodeTypes() ;
    for(NodeType mixin:mixins) {
      newLanguageNode.addMixin(mixin.getName()) ;
      for(PropertyDefinition def: mixin.getPropertyDefinitions()) {
        String propName = def.getName() ;
        if(def.isMandatory() && !def.isAutoCreated()) {
          if(def.isMultiple()) {
            newLanguageNode.setProperty(propName,node.getProperty(propName).getValues()) ;
          } else {
            newLanguageNode.setProperty(propName,node.getProperty(propName).getValue()) ; 
          }
        }        
      }
    }
    
    if(isDefault) node.setProperty(EXO_LANGUAGE, language) ;
    node.save() ;
    node.getSession().save() ;    
  }

  public String getDefault(Node node) throws Exception {
    if(node.hasProperty(EXO_LANGUAGE)) return node.getProperty(EXO_LANGUAGE).getString() ;
    return null ;
  }

  public List<String> getSupportedLanguages(Node node) throws Exception {
    List<String> languages = new ArrayList<String>();
    String defaultLang = getDefault(node) ;
    if(defaultLang != null) languages.add(defaultLang) ;
    if(node.hasNode(LANGUAGES)){
      Node languageNode = node.getNode(LANGUAGES) ;
      NodeIterator iter  = languageNode.getNodes() ;      
      while(iter.hasNext()) {
        languages.add(iter.nextNode().getName());
      }
    } 
    return languages;
  }

  public long getVoteTotal(Node node) throws Exception {
    long voteTotal = 0;
    if(!node.hasNode(LANGUAGES) && node.hasProperty(VOTE_TOTAL_PROP)) {
      return node.getProperty(VOTE_TOTAL_LANG_PROP).getLong() ;
    }
    Node multiLanguages = node.getNode(LANGUAGES) ;
    voteTotal = node.getProperty(VOTE_TOTAL_LANG_PROP).getLong() ;
    NodeIterator nodeIter = multiLanguages.getNodes() ;
    String defaultLang = getDefault(node) ;
    while(nodeIter.hasNext()) {
      Node languageNode = nodeIter.nextNode() ;
      if(!languageNode.getName().equals(defaultLang) && languageNode.hasProperty(VOTE_TOTAL_LANG_PROP)) {
        voteTotal = voteTotal + languageNode.getProperty(VOTE_TOTAL_LANG_PROP).getLong() ;
      }
    }
    return voteTotal ;
  }
  
  private boolean hasMixin(Node node, String nodeTypeName) throws Exception {
    NodeType[] mixinTypes = node.getMixinNodeTypes() ; 
    for(NodeType nodeType : mixinTypes) {
      if(nodeType.getName().equals(nodeTypeName)) return true ;
    }
    return false ;
  }
  
  public void setDefault(Node node, String language) throws Exception {
    String defaultLanguage = getDefault(node) ;
    if(!defaultLanguage.equals(language)){
      Node languagesNode = null ;
      if(node.hasNode(LANGUAGES)) languagesNode = node.getNode(LANGUAGES) ;
      else languagesNode = node.addNode(LANGUAGES, NTUNSTRUCTURED) ;
      Node selectedLangNode = languagesNode.getNode(language) ;
      Node newLang = languagesNode.addNode(defaultLanguage) ;
      PropertyDefinition[] properties = node.getPrimaryNodeType().getPropertyDefinitions() ;
      for(PropertyDefinition pro : properties){
        if(!pro.isProtected()){
          String propertyName = pro.getName() ;
          if(node.hasProperty(propertyName)) {
            newLang.setProperty(propertyName, node.getProperty(propertyName).getValue()) ;
          }
          if(selectedLangNode.hasProperty(propertyName)) {
            node.setProperty(propertyName, selectedLangNode.getProperty(propertyName).getValue()) ;
          }
        }
      }
      if(node.getPrimaryNodeType().getName().equals("nt:file")) {
        Node tempNode = node.addNode(TEMP_NODE, "nt:unstructured") ;
        node.getSession().move(node.getNode(JCRCONTENT).getPath(), tempNode.getPath() + "/" + JCRCONTENT) ;
        node.getSession().move(selectedLangNode.getNode(JCRCONTENT).getPath(), node.getPath() + "/" + JCRCONTENT) ;
        node.getSession().move(tempNode.getNode(JCRCONTENT).getPath(), languagesNode.getPath() + "/" + defaultLanguage + "/" + JCRCONTENT) ;
        tempNode.remove() ;
        node.setProperty(EXO_LANGUAGE, language) ;
      } 
      if(hasMixin(newLang, "mix:votable")) {
        newLang.setProperty(VOTE_TOTAL_PROP, getVoteTotal(node)) ; 
        newLang.setProperty(VOTE_TOTAL_LANG_PROP, node.getProperty(VOTE_TOTAL_LANG_PROP).getLong()) ;
        newLang.setProperty(VOTING_RATE_PROP, node.getProperty(VOTING_RATE_PROP).getLong()) ;
        newLang.setProperty(VOTER_PROP, node.getProperty(VOTER_PROP).getValues()) ;
        
        node.setProperty(VOTE_TOTAL_PROP, getVoteTotal(node)) ; 
        node.setProperty(VOTE_TOTAL_LANG_PROP, selectedLangNode.getProperty(VOTE_TOTAL_LANG_PROP).getLong()) ;
        node.setProperty(VOTING_RATE_PROP, selectedLangNode.getProperty(VOTING_RATE_PROP).getLong()) ;
        node.setProperty(VOTER_PROP, selectedLangNode.getProperty(VOTER_PROP).getValues()) ;
      }
      node.setProperty(EXO_LANGUAGE, language) ;
      if(node.hasNode(COMMENTS)) {
        node.getSession().move(node.getPath() + "/" + COMMENTS, newLang.getPath() + "/" + COMMENTS) ;
      }
      if(selectedLangNode.hasNode(COMMENTS)) {
        node.getSession().move(selectedLangNode.getPath() + "/" + COMMENTS, node.getPath() + "/" + COMMENTS) ;
      }
      selectedLangNode.remove() ;
      node.save() ;
      node.getSession().save() ;
    }
  }

  public Node getLanguage(Node node, String language) throws Exception {
    if(node.hasNode(LANGUAGES + "/"+ language)) return node.getNode(LANGUAGES + "/"+ language) ;
    return null;
  }
}
