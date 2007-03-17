/******************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL. All rights reserved.            *
 * Please look at license.txt in info directory for more license detail.      *
 ******************************************************************************/
package org.exoplatform.services.workflow.impl.bonita;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.download.DownloadService;
import org.exoplatform.services.download.InputStreamDownloadResource;
import org.exoplatform.services.workflow.FileDefinition;
import org.exoplatform.services.workflow.Form;

/**
 * This class represents a Form that is defined in a configuration file.
 * Compared with the automatic one, this type of Form allows to customize the
 * shown panel, by filtering displayed fields, setting attributes of components
 * or choosing their renderer.
 * 
 * Created by Bull R&D
 * @author Brice Revenant
 * Feb 27, 2006
 */
public class SpecifiedFormImpl implements Form {
  /** Customized view corresponding to this Form or empty String if unset */
  private String customizedView = null;
  
  /** URL of the icon corresponding to this Form */
  private byte[] icon = null;
  
  /** Indicates if this Form corresponds to a delegated view */
  private boolean isDelegatedView = false;
  
  /** Localized Resource Bundle corresponding to this Form */
  private ResourceBundle resourceBundle = null;
  
  /** Name of the State corresponding to this Form */
  private String stateName = null;
  
  /** Submit buttons corresponding to this Form */
  private List<Map<String, Object>> submitButtons;
  
  /** Variables corresponding to this Form */
  private List<Map<String, Object>> variables;
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.Form#getCustomizedView()
   */
  public String getCustomizedView() {
    return this.customizedView;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.Form#getIconURL()
   */
  public String getIconURL() {
    String url;
    
    if(icon != null) {
      url = this.publishImage(icon);
    }
    else {
      url = "";
    }
    
    return url;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.Form#getResourceBundle()
   */
  public ResourceBundle getResourceBundle() {
    return this.resourceBundle;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.Form#getStateImageURL()
   */
  public String getStateImageURL() {
    // TODO Retrieve an URL corresponding to an SVG image
    return "";
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.Form#getStateName()
   */
  public String getStateName() {
    return this.stateName;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.Form#getSubmitButtons()
   */
  public List getSubmitButtons() {
    return this.submitButtons;
  }

  /**
   * Make an image available from the download service
   * 
   * @param  image bytes describing the image
   * @return String giving the download URL of the published image
   */
  private String publishImage(byte[] image) {
    DownloadService dS = (DownloadService) PortalContainer.getInstance().
      getComponentInstanceOfType(DownloadService.class);
    InputStream iS = new ByteArrayInputStream(image);
    String id = dS.addDownloadResource(
      new InputStreamDownloadResource(iS, "image/gif"));
    return dS.getDownloadLink(id);
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.Form#getVariables()
   */
  public List getVariables() {
    return this.variables;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.Form#isCustomizedView()
   */
  public boolean isCustomizedView() {
    return (this.customizedView != null) && (! "".equals(this.customizedView));
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.Form#isDelegatedView()
   */
  public boolean isDelegatedView() {
    return this.isDelegatedView;
  }
  
  /**
   * This constructor instantiates a Form specified in a File Definition, based
   * on a State name and a Locale. 
   * 
   * @param fileDefinition contains the definition of Process in which
   *                       information of Forms should be found
   * @param stateName      identifies the State for which to create the Form
   * @param locale         specifies the Locale for which to create the Form
   */
  public SpecifiedFormImpl(FileDefinition fileDefinition,
                           String         stateName,
                           Locale         locale) {
    // Retrieve information from the File Definition
    this.customizedView  = fileDefinition.getCustomizedView(stateName);
    this.isDelegatedView = fileDefinition.isDelegatedView(stateName);
    this.resourceBundle  = fileDefinition.getResourceBundle(stateName, locale);
    this.stateName       = stateName;
    this.variables       = fileDefinition.getVariables(stateName);

    try {
      // Retrieve the icon URL
      this.icon          = fileDefinition.getEntry(this.stateName +
                                                   "-icon.gif");
    }
    catch(Exception e) {
      // No provided icon
      this.icon          = null;
    }

    /*
     * Initialize the buttons. The list is left empty when the Form corresponds
     * to a start Process one. In Bonita, we consider this is the Workflow duty
     * to determine which activity comes next hence a single default button for
     * all activities.
     */
    this.submitButtons = new ArrayList<Map<String, Object>>();
    if(! ProcessData.START_STATE_NAME.equals(stateName)) {
      Map<String, Object> attributes = new HashMap<String, Object>();
      // TODO Translate me
      attributes.put("name", "submit");
      attributes.put("transition", "");
      this.submitButtons.add(attributes);
    }
  }
}
