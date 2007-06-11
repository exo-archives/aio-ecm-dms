/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

import java.util.List ;
import java.util.ArrayList ;

import org.exoplatform.services.cms.metadata.MetadataService;
import org.exoplatform.services.cms.scripts.CmsScript;

import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.bean.SelectItemOption;
/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * May 29, 2007 10:01:09 AM
 */
public class FillSelectBoxWithMetadatas implements CmsScript {
  
  private MetadataService metadataService_ ;
  
  public FillSelectBoxWithMetadatas(MetadataService metadataService) {
    metadataService_ = metadataService ;
  }
  
  public void execute(Object context) {
    UIFormSelectBox selectBox = (UIFormSelectBox) context;
    List options = new ArrayList();
    for(String metadataName : metadataService_.getMetadataList()) {
      options.add(new SelectItemOption(metadataName, metadataName));
    }            
    selectBox.setOptions(options);
  }

  public void setParams(String[] params) {}

}