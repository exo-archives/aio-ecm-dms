package org.exoplatform.ecm.webui.component.explorer.auditing;

import javax.jcr.Node;

import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * The dialog window to activate the auditing. 
 * 
 * @author CPop Bull CS
 */

@ComponentConfig(
  type = UIActivateAuditing.class,
      template = "app:/groovy/webui/component/explorer/auditing/UIActivateAuditing.gtmpl",
      events = {                
        @EventConfig(listeners = UIActivateAuditing.EnableAuditingActionListener.class),
        @EventConfig(listeners = UIActivateAuditing.CancelActionListener.class)
      }
  )

public class UIActivateAuditing extends UIContainer implements UIPopupComponent {

  public UIActivateAuditing() throws Exception {}
  
  public void activate() throws Exception {}

  public void deActivate() throws Exception {}
  
  /**
   * Event generated by "Activate" button
   * 
   * @author CPop
   */
  static public class EnableAuditingActionListener extends EventListener<UIActivateAuditing> {
    public void execute(Event<UIActivateAuditing> event) throws Exception {
      try {
        UIActivateAuditing uiActivateAuditing = event.getSource();
        UIJCRExplorer uiExplorer = uiActivateAuditing.getAncestorOfType(UIJCRExplorer.class) ;
        Node currentNode = uiExplorer.getRealCurrentNode() ;
      
        currentNode.addMixin(Utils.EXO_AUDITABLE);
        currentNode.save() ;

        uiExplorer.getSession().save();   
        uiExplorer.getSession().refresh(true) ;      
        uiExplorer.updateAjax(event) ;  
      }catch(Exception e){
        e.printStackTrace();
        UIActivateAuditing uiActivateAuditing = event.getSource();
        UIJCRExplorer uiExplorer = uiActivateAuditing.getAncestorOfType(UIJCRExplorer.class) ;
        WebuiRequestContext contx = event.getRequestContext();     
        UIApplication uiApp = uiExplorer.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.does-not-support-auditing",null,ApplicationMessage.WARNING)) ;
        contx.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      }
    }
  }
  
  static public class CancelActionListener extends EventListener<UIActivateAuditing> {
    public void execute(Event<UIActivateAuditing> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction() ;        
    }
  }
}
