/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ecm.webui.component.explorer.popup.admin;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

import javax.jcr.AccessDeniedException;
import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.nodetype.ConstraintViolationException;

import org.apache.commons.logging.Log;
import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormUploadInput;

/**
 * Created by The eXo Platform SARL Author : Dang Van Minh minh.dang@exoplatform.com Oct 5, 2006
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "app:/groovy/webui/component/explorer/popup/admin/UIFormWithMultiRadioBox.gtmpl", events = {
    @EventConfig(listeners = UIImportNode.ImportActionListener.class),
    @EventConfig(listeners = UIImportNode.CancelActionListener.class, phase = Phase.DECODE) })
public class UIImportNode extends UIForm implements UIPopupComponent {

  private final static Log          log           = ExoLogger.getLogger("ecm.UIImportNode");

  public static final String FORMAT        = "format";

  public static final String DOCUMENT_VIEW = "Document View";

  public static final String SYSTEM_VIEW   = "System View";

  public static final String DOC_VIEW      = "docview";

  public static final String SYS_VIEW      = "sysview";

  public static final String FILE_UPLOAD   = "upload";

  public UIImportNode() throws Exception {
    this.setMultiPart(true);
    addUIFormInput(new UIFormUploadInput(FILE_UPLOAD, FILE_UPLOAD));
    List<SelectItemOption<String>> formatItem = new ArrayList<SelectItemOption<String>>();
    formatItem.add(new SelectItemOption<String>(DOCUMENT_VIEW, DOC_VIEW));
    formatItem.add(new SelectItemOption<String>(SYSTEM_VIEW, SYS_VIEW));
    addUIFormInput(new UIFormRadioBoxInput(FORMAT, DOC_VIEW, formatItem).setAlign(UIFormRadioBoxInput.VERTICAL_ALIGN));
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  static public class ImportActionListener extends EventListener<UIImportNode> {
    public void execute(Event<UIImportNode> event) throws Exception {
      UIImportNode uiImport = event.getSource();
      UIJCRExplorer uiExplorer = uiImport.getAncestorOfType(UIJCRExplorer.class);
      UIApplication uiApp = uiImport.getAncestorOfType(UIApplication.class);
      UIFormUploadInput input = uiImport.getUIInput(FILE_UPLOAD);
      Node currentNode = uiExplorer.getCurrentNode();
      Session session = currentNode.getSession() ;
      String nodePath = currentNode.getPath();
      uiExplorer.addLockToken(currentNode);
      if (input.getUploadResource() == null) {
        uiApp.addMessage(new ApplicationMessage("UIImportNode.msg.filename-invalid", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      String fileName = input.getUploadResource().getFileName();
      MimeTypeResolver resolver = new MimeTypeResolver();
      String mimeType = resolver.getMimeType(fileName);
      ByteArrayInputStream xmlInputStream = null;
      if ("text/xml".equals(mimeType)) {
        xmlInputStream = new ByteArrayInputStream(input.getUploadData());
      } else if ("application/zip".equals(mimeType)) {
        ZipInputStream zipInputStream = new ZipInputStream(input.getUploadDataAsStream());
        xmlInputStream = Utils.extractFromZipFile(zipInputStream);
      } else {
        uiApp.addMessage(new ApplicationMessage("UIImportNode.msg.mimetype-invalid", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      try {
        session.importXML(nodePath, xmlInputStream, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);

        if (!uiExplorer.getPreference().isJcrEnable())
          // TODO
          // if an import fails, it's possible when source xml contains errors,
          // user may fix the fail caused items and save session (JSR-170, 7.3.7 Session Import Methods).
          // Or user may decide to make a rollback - make Session.refresh(false)  
          // So, we should make rollback in case of error...
          // see Session.importXML() throws IOException, PathNotFoundException, ItemExistsException, 
          // ConstraintViolationException, VersionException, InvalidSerializedDataException, LockException, RepositoryException
          // otherwise ECM FileExplolrer crashes as it assume all items were imported correct.
          session.save();

        uiApp.addMessage(new ApplicationMessage("UIImportNode.msg.import-successful", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());

      } catch (AccessDeniedException ace) {
        log.error("XML Import error " + ace, ace);
        // TODO does rollback will be performed?
        uiApp.addMessage(new ApplicationMessage("UIImportNode.msg.access-denied", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch (ConstraintViolationException con) {
        log.error("XML Import error " + con, con);
        // TODO does rollback will be performed?
        Object[] args = { uiExplorer.getCurrentNode().getPrimaryNodeType().getName() };
        uiApp.addMessage(new ApplicationMessage("UIImportNode.msg.constraint-violation-exception",
                                                args,
                                                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch (Exception ise) {
        log.error("XML Import error " + ise, ise);
        // TODO does rollback will be performed?
        uiApp.addMessage(new ApplicationMessage("UIImportNode.msg.filetype-error", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }

      uiExplorer.updateAjax(event);
    }
  }

  static public class CancelActionListener extends EventListener<UIImportNode> {
    public void execute(Event<UIImportNode> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      uiExplorer.cancelAction();
    }
  }

}
