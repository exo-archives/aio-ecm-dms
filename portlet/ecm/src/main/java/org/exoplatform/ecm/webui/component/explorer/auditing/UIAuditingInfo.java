package org.exoplatform.ecm.webui.component.explorer.auditing;


import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.popup.UIPopupComponent;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.services.jcr.ext.audit.AuditHistory;
import org.exoplatform.services.jcr.ext.audit.AuditRecord;
import org.exoplatform.services.jcr.ext.audit.AuditService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Listing of the log of auditing
 * 
 * @author CPop
 */
@ComponentConfig(
  template = "app:/groovy/webui/component/explorer/auditing/UIAuditingInfo.gtmpl",
  events = {
    @EventConfig(listeners = UIAuditingInfo.CloseActionListener.class)        
  }
)
public class UIAuditingInfo extends UIContainer implements UIPopupComponent {

  public UIAuditingInfo() throws Exception{
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public Node getCurrentNode() throws Exception { 
    return getAncestorOfType(UIJCRExplorer.class).getCurrentNode(); 
  }

  public List<AuditRecord> getListRecords() throws Exception {
     List<AuditRecord> listRec = new ArrayList<AuditRecord>();
     Node currentNode = getAncestorOfType(UIJCRExplorer.class).getCurrentNode(); 
     try{
      AuditService auServ= getApplicationComponent(AuditService.class);
      if(auServ.hasHistory(currentNode)){
        if (Utils.NT_FILE.equals(currentNode.getProperty(Utils.JCR_PRIMARYTYPE).getString())) { 
          currentNode = currentNode.getNode(Utils.JCR_CONTENT);
        } 
        AuditHistory auHistory = auServ.getHistory(currentNode);
        listRec=auHistory.getAuditRecords();     
        return listRec;
      }
    }catch(Exception e){
      e.printStackTrace() ;
      return listRec;
    }
    return listRec;
  }
  
  static public class CloseActionListener extends EventListener<UIAuditingInfo> {
    public void execute(Event<UIAuditingInfo> event) throws Exception {
      UIAuditingInfo uiAuditingInfo = event.getSource();
      UIJCRExplorer uiExplorer = uiAuditingInfo.getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction() ;
    }
  }
}
