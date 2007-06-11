/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.versions;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.version.Version;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.services.document.diff.AddDelta;
import org.exoplatform.services.document.diff.ChangeDelta;
import org.exoplatform.services.document.diff.DeleteDelta;
import org.exoplatform.services.document.diff.Delta;
import org.exoplatform.services.document.diff.DiffService;
import org.exoplatform.services.document.diff.Revision;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * May 3, 2007  
 */

@ComponentConfig(template = "app:/groovy/webui/component/explorer/versions/UIDiff.gtmpl")
  
public class UIDiff extends UIComponent {

  private Version baseVersion_ ;
  private Version version_ ;

  public void setVersions(Version baseVersion, Version version)
      throws Exception {
    baseVersion_ = baseVersion ;
    version_ = version ;
  }
  
  public String getText(Node node) throws Exception {
    if(node.hasNode("jcr:content")) {
      Node content = node.getNode("jcr:content");
      if(content.hasProperty("jcr:mimeType")){
        Property mime = content.getProperty("jcr:mimeType");
        //DocumentReaderService readerService = getApplicationComponent(DocumentReaderService.class) ;
        if(content.hasProperty("jcr:data")) {
          if(mime.getString().startsWith("text")) return content.getProperty("jcr:data").getString();          
        }
      }
    }
    return null ;
  }
  public String getBaseVersionNum() throws Exception {return  baseVersion_.getName() ;}
  public String getCurrentVersionNum() throws Exception {return version_.getName() ;}
  
  public List<Delta> getDeltas() throws Exception {
    List<Delta> deltas = new ArrayList<Delta>();
    String previousText = getText(version_.getNode("jcr:frozenNode"));
    String currentText = getText(baseVersion_.getNode("jcr:frozenNode"));
    if((previousText != null)&&(currentText != null)) {
      String lineSeparator = DiffService.NL;
      Object[] orig = StringUtils.split(previousText, lineSeparator);
      Object[] rev = StringUtils.split(currentText, lineSeparator);
      DiffService diffService = getApplicationComponent(DiffService.class) ;
      Revision revision = diffService.diff(orig, rev);
      for (int i = 0; i < revision.size(); i++) {
        deltas.add(revision.getDelta(i));        
      }
    }
    return deltas;
  }

  public boolean isDeleteDelta(Delta delta) {    
    if (delta instanceof DeleteDelta) return true;
    return false;
  }

  public boolean isAddDelta(Delta delta) {
    if (delta instanceof AddDelta) return true;
    return false;
  }

  public boolean isChangeDelta(Delta delta) {
    if (delta instanceof ChangeDelta) return true;
    return false;
  } 
}
