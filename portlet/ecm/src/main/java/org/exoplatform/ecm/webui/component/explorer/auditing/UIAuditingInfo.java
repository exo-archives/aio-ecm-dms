package org.exoplatform.ecm.webui.component.explorer.auditing;


import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.ecm.jcr.UIPopupComponent;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
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

  protected Node node_;
  
  public UIAuditingInfo() throws Exception{
  }

  public void activate() throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;    
    node_ = uiExplorer.getCurrentNode() ;         
  }

  
  public void deActivate() throws Exception {
    node_=null;
  }

  public Node getCurrentNode() { return node_ ; }

  public List<AuditRecord> getListRecords() {
     List<AuditRecord> listRec = new ArrayList<AuditRecord>();
     try{
      PortalContainer cont = PortalContainer.getInstance();
      AuditService auServ= (AuditService)cont.getComponentInstanceOfType(AuditService.class);
      if(auServ.hasHistory(node_)){
        AuditHistory auHistory = auServ.getHistory(node_);
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
