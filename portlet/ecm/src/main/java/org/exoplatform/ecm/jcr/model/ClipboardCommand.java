package org.exoplatform.ecm.jcr.model;

public class ClipboardCommand {

  public static final String COPY = "copy";
  public static final String CUT = "cut";
  
  private String type ;
  private String srcPath ;
  private String wsName  = null ;

  public String getSrcPath() { return srcPath ;}
  public void setSrcPath(String srcPath) { this.srcPath = srcPath ;}
  public String getType() { return type ;}
  public void setType(String type) { this.type = type ;}
  public void setWorkspace(String ws) { wsName = ws ; }
  public String getWorkspace() { return wsName ; }
}
