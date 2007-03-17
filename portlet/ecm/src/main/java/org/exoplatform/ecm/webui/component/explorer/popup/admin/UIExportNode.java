/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.ecm.webui.component.explorer.popup.admin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.services.compress.CompressData;
import org.exoplatform.services.download.DownloadResource;
import org.exoplatform.services.download.DownloadService;
import org.exoplatform.services.download.InputStreamDownloadResource;
import org.exoplatform.webui.application.RequestContext;
import org.exoplatform.webui.component.UIForm;
import org.exoplatform.webui.component.UIFormCheckBoxInput;
import org.exoplatform.webui.component.UIFormInputInfo;
import org.exoplatform.webui.component.UIFormRadioBoxInput;
import org.exoplatform.webui.component.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.component.model.SelectItemOption;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 5, 2006  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/webui/component/explorer/popup/admin/UIFormWithMultiRadioBox.gtmpl",
    events = {
      @EventConfig(listeners = UIExportNode.ExportActionListener.class),
      @EventConfig(listeners = UIExportNode.CancelActionListener.class)
    }
)
public class UIExportNode extends UIForm implements UIPopupComponent {

  public static final String NODE_PATH = "nodePath" ;
  public static final String FORMAT = "format" ;
  public static final String ZIP = "zip" ;
  public static final String DOCUMENT_VIEW = "Document View" ;
  public static final String SYSTEM_VIEW = "System View" ;
  public static final String DOC_VIEW = "docview" ;
  public static final String SYS_VIEW = "sysview" ;

  public UIExportNode() throws Exception {
    List<SelectItemOption<String>> formatItem = new ArrayList<SelectItemOption<String>>() ;
    formatItem.add(new SelectItemOption<String>(DOCUMENT_VIEW, DOC_VIEW));
    formatItem.add(new SelectItemOption<String>(SYSTEM_VIEW, SYS_VIEW));
    addUIFormInput(new UIFormInputInfo(NODE_PATH, NODE_PATH, null)) ;
    addUIFormInput(new UIFormRadioBoxInput(FORMAT, DOC_VIEW, formatItem).
                   setAlign(UIFormRadioBoxInput.VERTICAL_ALIGN)) ;
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(ZIP, ZIP, null)) ;
  }

  public void update(Node node) throws Exception {
    getUIFormInputInfo(NODE_PATH).setValue(node.getPath()) ;
  }

  static public class ExportActionListener extends EventListener<UIExportNode> {
    public void execute(Event<UIExportNode> event) throws Exception {
      UIExportNode uiExport = event.getSource() ;
      UIJCRExplorer uiExplorer = uiExport.getAncestorOfType(UIJCRExplorer.class) ;
      Session session = uiExplorer.getSession() ;
      CompressData zipService = new CompressData();
      DownloadService dservice = uiExport.getApplicationComponent(DownloadService.class) ;
      DownloadResource dresource ;
      String format = uiExport.<UIFormRadioBoxInput>getUIInput(FORMAT).getValue() ;
      boolean isZip = uiExport.getUIFormCheckBoxInput(ZIP).isChecked() ;
      ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
      String nodePath = uiExplorer.getCurrentNode().getPath() ;
      if(isZip) {
        if(format.equals(DOC_VIEW)) session.exportDocumentView(nodePath, bos, false, false ) ;
        else session.exportSystemView(nodePath, bos, false, false ) ;
        ByteArrayInputStream input = new ByteArrayInputStream(bos.toByteArray()) ;
        zipService.addInputStream("docview.xml",input);
        zipService.createZip(bos);
        ByteArrayInputStream zipInput = new ByteArrayInputStream(bos.toByteArray());
        dresource = new InputStreamDownloadResource(zipInput, "application/zip") ;
        dresource.setDownloadName( format + ".zip");
      } else {
        if(format.equals(DOC_VIEW)) session.exportDocumentView(nodePath, bos, false, false ) ;
        else session.exportSystemView(nodePath, bos, false, false ) ;
        ByteArrayInputStream is = new ByteArrayInputStream(bos.toByteArray()) ;
        dresource = new InputStreamDownloadResource(is, "text/xml") ;
        
        dresource.setDownloadName(format + ".xml");
      }
      String downloadLink = dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
      RequestContext rcontext = event.getRequestContext().getParentAppRequestContext();
      HttpServletResponse response = rcontext.getResponse();
      System.out.println("\n\nDownload link====>" + downloadLink + "\n\n");
      response.sendRedirect(downloadLink);
      uiExplorer.updateAjax(event) ;
    }
  }

  static public class CancelActionListener extends EventListener<UIExportNode> {
    public void execute(Event<UIExportNode> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction() ;
    }
  }

  public void activate() throws Exception {
    update(getAncestorOfType(UIJCRExplorer.class).getCurrentNode()) ;
  }

  public void deActivate() throws Exception { }
}
