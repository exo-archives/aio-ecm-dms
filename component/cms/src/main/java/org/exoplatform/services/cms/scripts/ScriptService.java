package org.exoplatform.services.cms.scripts;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

public interface ScriptService {
  
  public Node getECMScriptHome(String repository,SessionProvider provider) throws Exception ;  
  
  public Node getCBScriptHome(String repository,SessionProvider provider) throws Exception ;    
  
  public List<Node> getECMActionScripts(String repository,SessionProvider provider) throws Exception ;
  
  public List<Node> getECMInterceptorScripts(String repository,SessionProvider provider) throws Exception;
  
  public List<Node> getECMWidgetScripts(String repository,SessionProvider provider) throws Exception ;
  
  public CmsScript getScript(String scriptPath, String repository) throws Exception;
  
  public String getBaseScriptPath() throws Exception ;  
  
  public String getScriptAsText(String scriptPath, String repository) throws Exception;        
  
  public void addScript(String name, String text, String repository,SessionProvider provider) throws Exception;
  
  public void removeScript(String scriptPath, String repository,SessionProvider provider) throws Exception;
  
  public Node getScriptNode(String scriptName, String repository,SessionProvider provider) throws Exception; 
  
  public void initRepo(String repository) throws Exception ;
}
