package org.exoplatform.services.workflow;

import java.util.List;
import java.util.ResourceBundle;

/**
 * Created y the eXo platform team
 * User: Benjamin Mestrallet
 * Date: 17 mai 2004
 */
public interface Form {

  public List getVariables();

  public List getSubmitButtons();

  public String getStateName();

  public ResourceBundle getResourceBundle();
  
  public boolean isCustomizedView();
  
  public String getCustomizedView();
  
  public boolean isDelegatedView();
  
  public String getIconURL();
  
  public String getStateImageURL();

}
