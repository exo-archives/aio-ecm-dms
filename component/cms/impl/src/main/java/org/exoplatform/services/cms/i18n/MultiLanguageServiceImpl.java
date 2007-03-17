package org.exoplatform.services.cms.i18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Value;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

/**
 * @author Hung Nguyen Quang
 * @mail   nguyenkequanghung@yahoo.com
 */

public class MultiLanguageServiceImpl implements MultiLanguageService{
  final static public String  JCRCONTENT = "jcr:content";
  final static public String  JCRDATA = "jcr:data";
  final static public String  NTUNSTRUCTURED = "nt:unstructured";
  
  public MultiLanguageServiceImpl() throws Exception {
  }

  public void addLanguage(Node node, Map inputs, String language, boolean isDefault) throws Exception {
    Node newLanguageNode = null ;
    Node languagesNode = null ;
    String defaultLanguage = getDefault(node) ;
    if(node.hasNode(LANGUAGES)) languagesNode = node.getNode(LANGUAGES) ;
    else languagesNode = node.addNode(LANGUAGES, NTUNSTRUCTURED) ;
    if(!defaultLanguage.equals(language)){
      if(isDefault) {
        if(languagesNode.hasNode(defaultLanguage)) newLanguageNode = languagesNode.getNode(defaultLanguage) ;
        else newLanguageNode = languagesNode.addNode(defaultLanguage) ;
      }else {
        if(languagesNode.hasNode(language)) newLanguageNode = languagesNode.getNode(language) ;
        else newLanguageNode = languagesNode.addNode(language) ;
      }
    }    
    PropertyDefinition[] properties = node.getPrimaryNodeType().getPropertyDefinitions() ;
    for(PropertyDefinition pro : properties){
      if(!pro.isProtected()){
        String propertyName = pro.getName() ;        
        if(defaultLanguage.equals(language)){
          node.setProperty(propertyName, (String)inputs.get(propertyName)) ;
        }else {          
          if(isDefault){            
            newLanguageNode.setProperty(propertyName, node.getProperty(propertyName).getValue()) ;
            node.setProperty(propertyName, (String)inputs.get(propertyName)) ;
          }else {            
            newLanguageNode.setProperty(propertyName, (String)inputs.get(propertyName)) ;
          }
        }               
      }
    }
    
    // add mixin type for node
    NodeType[] mixins = node.getMixinNodeTypes() ;
    for(NodeType mixin:mixins) {
      newLanguageNode.addMixin(mixin.getName()) ;            
    }
    
    if(isDefault) node.setProperty(EXO_LANGUAGE, language) ;
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
      }else {
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
    }
    
    if(isDefault) node.setProperty(EXO_LANGUAGE, language) ;
    node.save() ;
    node.getSession().save() ;    
  }

  public String getDefault(Node node) throws Exception {
    return node.getProperty(EXO_LANGUAGE).getString() ;
  }

  public List<String> getSupportedLanguages(Node node) throws Exception {
    List<String> languages = new ArrayList<String>();
    String defaultLang = getDefault(node) ;
    languages.add(defaultLang) ;
    if(node.hasNode(LANGUAGES)){
      Node languageNode = node.getNode(LANGUAGES) ;
      NodeIterator iter  = languageNode.getNodes() ;      
      while(iter.hasNext()) {
        languages.add(iter.nextNode().getName());
      }
    } 
    return languages;
  }

  public void setDefault(Node node, String language) throws Exception {
    String defaultLanguage = getDefault(node) ;
    if(!defaultLanguage.equals(language)){
      Node languagesNode = null ;
      if(node.hasNode(LANGUAGES)) languagesNode = node.getNode(LANGUAGES) ;
      else languagesNode = node.addNode(LANGUAGES, NTUNSTRUCTURED) ;
      Node selectedLangNode = languagesNode.getNode(language) ;
      if(node.getPrimaryNodeType().getName().equals("nt:file")) {
        Value value = node.getNode(JCRCONTENT).getProperty(JCRDATA).getValue() ;
        Value selectedValue = selectedLangNode.getNode(JCRCONTENT).getProperty(JCRDATA).getValue() ;
        node.getNode(JCRCONTENT).setProperty(JCRDATA, selectedValue) ;
        selectedLangNode.getNode(JCRCONTENT).setProperty(JCRDATA, value) ;
        node.getSession().move(languagesNode.getPath() + "/" + language, languagesNode.getPath() + "/" + defaultLanguage) ;
        node.setProperty(EXO_LANGUAGE, language) ;
      }else {
        Node newLang = languagesNode.addNode(defaultLanguage) ;
        PropertyDefinition[] properties = node.getPrimaryNodeType().getPropertyDefinitions() ;
        for(PropertyDefinition pro : properties){
          if(!pro.isProtected()){
            String propertyName = pro.getName() ;
            newLang.setProperty(propertyName, node.getProperty(propertyName).getValue()) ;
            node.setProperty(propertyName, selectedLangNode.getProperty(propertyName).getValue()) ;
          }
        }
        node.setProperty(EXO_LANGUAGE, language) ;
        selectedLangNode.remove() ;
      }
      node.save() ;
      node.getSession().save() ;
    }
  }

  public Node getLanguage(Node node, String language) throws Exception {
    if(node.hasNode(LANGUAGES + "/"+ language)) return node.getNode(LANGUAGES + "/"+ language) ;
    return null;
  }
  
}
