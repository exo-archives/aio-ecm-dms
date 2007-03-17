package org.exoplatform.services.cms.scripts;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.container.component.ComponentPlugin;

public interface ScriptService {
  
  //public void addScriptPlugin(ComponentPlugin plugin) ;
  
  public Node getECMScriptHome() throws Exception ;  
  
  public Node getCBScriptHome() throws Exception ;
  public boolean hasCBScript() throws Exception ;
  public List<Node> getCBScripts() throws Exception ;
  
  public Node getECMActionScriptHome() throws Exception ;  
  public List<Node> getECMActionScripts() throws Exception ;
  
  public Node getECMInterceptorScriptHome() throws Exception ;  
  public List<Node> getECMInterceptorScripts() throws Exception;
  
  public Node getECMWidgetScriptHome() throws Exception ;  
  public List<Node> getECMWidgetScripts() throws Exception ;
  
  public CmsScript getScript(String scriptPath) throws Exception;
  
  public String getBaseScriptPath() throws Exception ;  
  
  public String getScriptAsText(String scriptPath) throws Exception;        
  
  public void addScript(String name, String text) throws Exception;
  
  public void removeScript(String scriptPath) throws Exception;
  
  public Node getScriptNode(String scriptName) throws Exception; 
}
