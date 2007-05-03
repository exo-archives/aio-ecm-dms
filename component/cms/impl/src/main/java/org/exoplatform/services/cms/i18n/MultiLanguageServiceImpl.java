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
            newLanguageNode.setProperty(propertyName, node.getProperty(propertyName).getValue()) ;
            node.setProperty(propertyName, property.getValue().toString()) ;
          } else {           
            newLanguageNode.setProperty(propertyName, property.getValue().toString()) ;
          }
        }               
      }
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
      if(!languageNode.getName().equals(defaultLang)) {
        voteTotal = voteTotal + languageNode.getProperty(VOTE_TOTAL_LANG_PROP).getLong() ;
      }
    }
    return voteTotal ;
  }
  
  public void setDefault(Node node, String language) throws Exception {
    String defaultLanguage = getDefault(node) ;
    if(!defaultLanguage.equals(language)){
      Node languagesNode = null ;
      if(node.hasNode(LANGUAGES)) languagesNode = node.getNode(LANGUAGES) ;
      else languagesNode = node.addNode(LANGUAGES, NTUNSTRUCTURED) ;
      Node selectedLangNode = languagesNode.getNode(language) ;
      if(node.getPrimaryNodeType().getName().equals("nt:file")) {
        node.getSession().move(languagesNode.getPath() + "/" + language, languagesNode.getPath() + "/" + defaultLanguage) ;
        node.setProperty(EXO_LANGUAGE, language) ;
      } else {
        Node newLang = languagesNode.addNode(defaultLanguage) ;
        PropertyDefinition[] properties = node.getPrimaryNodeType().getPropertyDefinitions() ;
        for(PropertyDefinition pro : properties){
          if(!pro.isProtected()){
            String propertyName = pro.getName() ;
            newLang.setProperty(propertyName, node.getProperty(propertyName).getValue()) ;
            node.setProperty(propertyName, selectedLangNode.getProperty(propertyName).getValue()) ;
          }
        }
        newLang.setProperty(VOTE_TOTAL_PROP, getVoteTotal(node)) ; 
        newLang.setProperty(VOTE_TOTAL_LANG_PROP, node.getProperty(VOTE_TOTAL_LANG_PROP).getLong()) ;
        newLang.setProperty(VOTING_RATE_PROP, node.getProperty(VOTING_RATE_PROP).getLong()) ;
        newLang.setProperty(VOTER_PROP, node.getProperty(VOTER_PROP).getValues()) ;
        
        node.setProperty(VOTE_TOTAL_PROP, getVoteTotal(node)) ; 
        node.setProperty(VOTE_TOTAL_LANG_PROP, selectedLangNode.getProperty(VOTE_TOTAL_LANG_PROP).getLong()) ;
        node.setProperty(VOTING_RATE_PROP, selectedLangNode.getProperty(VOTING_RATE_PROP).getLong()) ;
        node.setProperty(VOTER_PROP, selectedLangNode.getProperty(VOTER_PROP).getValues()) ;
        node.setProperty(EXO_LANGUAGE, language) ;
        if(node.hasNode(COMMENTS)) {
          node.getSession().move(node.getPath() + "/" + COMMENTS, newLang.getPath() + "/" + COMMENTS) ;
        }
        if(selectedLangNode.hasNode(COMMENTS)) {
          node.getSession().move(selectedLangNode.getPath() + "/" + COMMENTS, node.getPath() + "/" + COMMENTS) ;
        }
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
