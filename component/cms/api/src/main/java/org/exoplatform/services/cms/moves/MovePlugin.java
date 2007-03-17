package org.exoplatform.services.cms.moves;

import java.util.Collection;
import java.util.Map;

public interface MovePlugin {
  
  public boolean isMoveTypeSupported(String moveType);

  public String getExecutableDefinitionName();
  
  public Collection<String> getMoveExecutables() throws Exception;

  public String getMoveExecutableLabel();
  
  public String getMoveExecutable(String moveTypeName) throws Exception;

  public boolean isVariable(String variable) throws Exception;
  
  public Collection<String> getVariableNames(String moveTypeName) throws Exception;
  
  public void removeMove(String moveName) throws Exception;

  public void addMove(String moveType, String srcWorkspace, String srcPath, Map mappings) throws Exception;  

}
