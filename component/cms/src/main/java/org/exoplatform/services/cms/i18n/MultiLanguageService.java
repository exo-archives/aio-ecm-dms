package org.exoplatform.services.cms.i18n;

import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Value;


/**
 * Author : Hung Nguyen Quang
 *          nguyenkequanghung@yahoo.com
 */

public interface MultiLanguageService {
  
  final static public String LANGUAGES = "languages" ;
  final static public String EXO_LANGUAGE = "exo:language" ;
  final static public String COMMENTS = "comments".intern() ;
  
  
  public List<String> getSupportedLanguages(Node node) throws Exception ;
  public void setDefault(Node node, String language) throws Exception ;
  public void addLanguage(Node node, Map inputs, String language, boolean isDefault) throws Exception ;
  public void addFileLanguage(Node node, Value value, String language, boolean isDefault) throws Exception ;
  public void addFileLanguage(Node node, String language, Map mappings, boolean isDefault) throws Exception ;
  public String getDefault(Node node) throws Exception ;
  public Node getLanguage(Node node, String language) throws Exception ;
  
}
