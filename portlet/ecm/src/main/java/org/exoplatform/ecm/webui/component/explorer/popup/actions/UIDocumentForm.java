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
package org.exoplatform.ecm.webui.component.explorer.popup.actions;

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.VersionException;

import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.form.DialogFormActionListeners;
import org.exoplatform.ecm.webui.form.UIDialogForm;
import org.exoplatform.ecm.webui.popup.UIPopupComponent;
import org.exoplatform.ecm.webui.selector.ComponentSelector;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.tree.selectmany.UICategoriesSelector;
import org.exoplatform.ecm.webui.tree.selectone.UIOneNodePathSelector;
import org.exoplatform.ecm.webui.utils.DialogFormUtil;
import org.exoplatform.ecm.webui.utils.LockUtil;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.categories.CategoriesService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
import org.exoplatform.webui.form.UIFormUploadInput;

/**
 * Created by The eXo Platform SARL
 * Author : nqhungvn
 *          nguyenkequanghung@yahoo.com
 * July 3, 2006
 * 10:07:15 AM
 * Editor : Pham Tuan
 *        phamtuanchip@yahoo.de
 * Nov 08, 2006  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig(listeners = UIDocumentForm.SaveActionListener.class),
      @EventConfig(listeners = UIDocumentForm.CancelActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIDocumentForm.AddActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIDocumentForm.RemoveActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIDocumentForm.ShowComponentActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIDocumentForm.RemoveReferenceActionListener.class, confirm = "DialogFormField.msg.confirm-delete", phase = Phase.DECODE),
      @EventConfig(listeners = DialogFormActionListeners.RemoveDataActionListener.class, confirm = "DialogFormField.msg.confirm-delete", phase = Phase.DECODE)
    }
)

public class UIDocumentForm extends UIDialogForm implements UIPopupComponent, UISelectable {   
  
  public UIDocumentForm() throws Exception {
    setActions(new String[]{"Save", "Cancel"}) ;  
  }     
  
  @SuppressWarnings("unchecked")
  public void doSelect(String selectField, Object value) {
    isUpdateSelect = true ;    
    UIFormInput formInput = getUIInput(selectField) ;
    if(formInput instanceof UIFormInputBase) {
      ((UIFormInputBase)formInput).setValue(value.toString()) ;
    }else if(formInput instanceof UIFormMultiValueInputSet) {
      UIFormMultiValueInputSet  inputSet = (UIFormMultiValueInputSet) formInput ;
      UIFormInputBase input = inputSet.getChild(inputSet.getChildren().size()-1);
      input.setValue(value.toString());
    }
    UIDocumentFormController uiContainer = getParent() ;
    uiContainer.removeChildById("PopupComponent") ;
  }

  public String getTemplate() {
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    String userName = Util.getPortalRequestContext().getRemoteUser() ;
    try {      
      System.out.println("AAA" + templateService.getTemplatePathByUser(true, contentType, userName, repositoryName));
      return templateService.getTemplatePathByUser(true, contentType, userName, repositoryName) ;
    } catch (Exception e) {
      e.printStackTrace();
      UIApplication uiApp = getAncestorOfType(UIApplication.class) ;
      Object[] arg = { contentType } ;
      uiApp.addMessage(new ApplicationMessage("UIDocumentForm.msg.not-support", arg, 
          ApplicationMessage.ERROR)) ;
      return null ;
    } 
  }

  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    return getAncestorOfType(UIJCRExplorer.class).getJCRTemplateResourceResolver() ;
  }

  public void activate() throws Exception {}
  public void deActivate() throws Exception {}

  public Node getCurrentNode() throws Exception { 
    return getAncestorOfType(UIJCRExplorer.class).getCurrentNode() ; 
  }
  
  static  public class SaveActionListener extends EventListener<UIDocumentForm> {
    public void execute(Event<UIDocumentForm> event) throws Exception {
      UIDocumentForm documentForm = event.getSource();
      UIJCRExplorer uiExplorer = documentForm.getAncestorOfType(UIJCRExplorer.class) ;
      List inputs = documentForm.getChildren();
      UIApplication uiApp = documentForm.getAncestorOfType(UIApplication.class);
      boolean hasCategories = false;
      String categoriesPath = null;
      String[] categoriesPathList = null;
      String repository = uiExplorer.getRepositoryName();
      CategoriesService categoriesService = documentForm.getApplicationComponent(CategoriesService.class);
      if(documentForm.isAddNew()) {
        for (int i = 0; i < inputs.size(); i++) {
          UIFormInputBase input = (UIFormInputBase) inputs.get(i);          
          if((input.getName() != null) && input.getName().equals("name")) {
            String[] arrFilterChar = {".", "/", ":", "[", "]", "*", "'", "|", "\""};          
            String valueName = input.getValue().toString();          
            for(String filterChar : arrFilterChar) {
              if(valueName.indexOf(filterChar) > -1) {
                uiApp.addMessage(new ApplicationMessage("UIFolderForm.msg.name-not-allowed", null, 
                    ApplicationMessage.WARNING));
                event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
                return;
              }
            }
          }          
        }
      }
      for (int i = 0; i < inputs.size(); i++) {        
        UIFormInputBase input = (UIFormInputBase) inputs.get(i);
        if((input.getName() != null) && input.getName().equals("categories")) {
          hasCategories = true;
          categoriesPath = input.getValue().toString();
          if (categoriesPath.startsWith("[")) categoriesPath = categoriesPath.substring(1, categoriesPath.length()).trim();
          if (categoriesPath.endsWith("]")) categoriesPath = categoriesPath.substring(0, categoriesPath.length()-1).trim();
          if ((categoriesPath == null) || (categoriesPath.length() == 0)) {
            uiApp.addMessage(new ApplicationMessage("UISelectedCategoriesGrid.msg.non-categories", null, 
                ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return;
          }
          categoriesPathList = categoriesPath.split(",");                    
          Session systemSession = categoriesService.getSession(repository);          
          for(String categoryPath : categoriesPathList) {              
            if((categoryPath != null) && (categoryPath.trim().length() > 0)){
              try {
                systemSession.getItem(categoryPath.trim());
              } catch (ItemNotFoundException e) {
                uiApp.addMessage(new ApplicationMessage("UISelectedCategoriesGrid.msg.non-categories", null, 
                    ApplicationMessage.WARNING)) ;
                event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
                return;
              } catch (RepositoryException re) {
                uiApp.addMessage(new ApplicationMessage("UISelectedCategoriesGrid.msg.non-categories", null, 
                    ApplicationMessage.WARNING)) ;
                event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
                return;
              } catch(Exception e) {
                e.printStackTrace();
                uiApp.addMessage(new ApplicationMessage("UISelectedCategoriesGrid.msg.non-categories", null, 
                    ApplicationMessage.WARNING)) ;
                event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
                return;
              }
            }
          }                      
        }
      }      
      Map inputProperties = DialogFormUtil.prepareMap(inputs, documentForm.getInputProperties());
      Node newNode = null ;
      String nodeType ;
      Node homeNode ;
      Node currentNode = uiExplorer.getCurrentNode();      
      if(documentForm.isAddNew()) {
        UIDocumentFormController uiDFController = documentForm.getParent() ;
        homeNode = currentNode ;
        UISelectDocumentForm uiSelectDocumentForm = uiDFController.getChild(UISelectDocumentForm.class);
        if (uiSelectDocumentForm != null) {
          nodeType = uiSelectDocumentForm.getSelectValue();                           // Exist select box, get selected value
        } else {
          nodeType = uiDFController.getChild(UIDocumentForm.class).getContentType();  // Not exist select box, get default value
        }
        if(homeNode.isLocked()) {
          homeNode.getSession().addLockToken(LockUtil.getLockToken(homeNode));
        }
      } else { 
        Node documentNode = documentForm.getNode();
        homeNode = documentNode.getParent();
        nodeType = documentNode.getPrimaryNodeType().getName() ;
        if(documentNode.isLocked()) {
          documentNode.getSession().addLockToken(LockUtil.getLockToken(documentNode)) ;
        }
      }       
      try {
        CmsService cmsService = documentForm.getApplicationComponent(CmsService.class) ;
        String addedPath = cmsService.storeNode(nodeType, homeNode, inputProperties, documentForm.isAddNew(),documentForm.repositoryName);
        try {
          homeNode.save() ;
          newNode = (Node)homeNode.getSession().getItem(addedPath);          
          if (hasCategories && (newNode != null) && ((categoriesPath != null) && (categoriesPath.length() > 0))){
            categoriesService.addMultiCategory(newNode, categoriesPathList, repository);            
          }
        } catch(Exception e) {
          if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save() ;
          uiExplorer.updateAjax(event);          
        }
        if(!uiExplorer.getPreference().isJcrEnable()) uiExplorer.getSession().save() ;
        uiExplorer.updateAjax(event);        
      } catch (AccessControlException ace) {
        throw new AccessDeniedException(ace.getMessage());
      } catch(VersionException ve) {
        uiApp.addMessage(new ApplicationMessage("UIDocumentForm.msg.in-versioning", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      } catch(ItemNotFoundException item) {
        uiApp.addMessage(new ApplicationMessage("UIDocumentForm.msg.item-not-found", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      } catch(RepositoryException repo) {
        repo.printStackTrace();
        String key = "UIDocumentForm.msg.repository-exception" ;
        if(ItemExistsException.class.isInstance(repo)) key = "UIDocumentForm.msg.not-allowed-same-name-sibling" ;
        uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      } catch(NumberFormatException nume) {
        String key = "UIDocumentForm.msg.numberformat-exception" ;
        uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      } catch(Exception e) {
        e.printStackTrace();
        String key = "UIDocumentForm.msg.cannot-save" ;
        uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      }
      event.getRequestContext().setAttribute("nodePath",newNode.getPath());      
    }
  }
  
  @SuppressWarnings("unchecked")
  static public class ShowComponentActionListener extends EventListener<UIDocumentForm> {
    public void execute(Event<UIDocumentForm> event) throws Exception {
      UIDocumentForm uiForm = event.getSource() ;
      UIDocumentFormController uiContainer = uiForm.getParent() ;
      uiForm.isShowingComponent = true;
      String fieldName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      Map fieldPropertiesMap = uiForm.componentSelectors.get(fieldName) ;
      String classPath = (String)fieldPropertiesMap.get("selectorClass") ;
      ClassLoader cl = Thread.currentThread().getContextClassLoader() ;
      Class clazz = Class.forName(classPath, true, cl) ;
      String rootPath = (String)fieldPropertiesMap.get("rootPath") ;
      UIComponent uiComp = uiContainer.createUIComponent(clazz, null, null);
      
      String value = uiForm.getUIStringInput(fieldName).getValue();
      String[] arrayTaxonomy = new String[1];
      if (value != null && !value.equals("")) {
        arrayTaxonomy = value.split(",");
        if (arrayTaxonomy.length > 0) {
          if (arrayTaxonomy[0].startsWith("[")) arrayTaxonomy[0] = arrayTaxonomy[0].substring(1, arrayTaxonomy[0].length());
          if (arrayTaxonomy[arrayTaxonomy.length - 1].endsWith("]")) {
            arrayTaxonomy[arrayTaxonomy.length - 1] = arrayTaxonomy[arrayTaxonomy.length - 1].substring(0, arrayTaxonomy[arrayTaxonomy.length - 1].length() - 1);  
          }
        }
      }
      if(uiComp instanceof UIOneNodePathSelector) {
        UIJCRExplorer explorer = uiForm.getAncestorOfType(UIJCRExplorer.class) ;
        String repositoryName = explorer.getRepositoryName() ;
        SessionProvider provider = explorer.getSessionProvider() ;                
        String wsFieldName = (String)fieldPropertiesMap.get("workspaceField") ;
        String wsName = "";
        if(wsFieldName != null && wsFieldName.length() > 0) {
          wsName = (String)uiForm.<UIFormInputBase>getUIInput(wsFieldName).getValue() ;
          ((UIOneNodePathSelector)uiComp).setIsDisable(wsName, true) ;      
        }
        String selectorParams = (String)fieldPropertiesMap.get("selectorParams") ;
        if(selectorParams != null) {
          String[] arrParams = selectorParams.split(",") ;
          if(arrParams.length == 4) {
            ((UIOneNodePathSelector)uiComp).setAcceptedNodeTypesInPathPanel(new String[] {Utils.NT_FILE}) ;
            wsName = arrParams[1];
            rootPath = arrParams[2];
            ((UIOneNodePathSelector)uiComp).setIsDisable(wsName, true) ;
            if(arrParams[3].indexOf(";") > -1) {
              ((UIOneNodePathSelector)uiComp).setAcceptedMimeTypes(arrParams[3].split(";")) ;
            } else {
              ((UIOneNodePathSelector)uiComp).setAcceptedMimeTypes(new String[] {arrParams[3]}) ;
            }
          }
        }
        if(rootPath == null) rootPath = "/";
        ((UIOneNodePathSelector)uiComp).setRootNodeLocation(repositoryName, wsName, rootPath) ;
        ((UIOneNodePathSelector)uiComp).setShowRootPathSelect(true);
        ((UIOneNodePathSelector)uiComp).init(provider);
      } else if (uiComp instanceof UICategoriesSelector){
        CategoriesService categoriesService = uiForm.getApplicationComponent(CategoriesService.class);
        UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class);
        Node currentNode = uiExplorer.getCurrentNode();
        String repository = uiExplorer.getRepositoryName();
        List<Node> cats = categoriesService.getCategories(currentNode, repository);
        List<String> arrCategoriesList = new ArrayList<String>();        
        for(int i=0; i<cats.size(); i++) {
          arrCategoriesList.add(cats.get(i).getPath());          
        }        
        ((UICategoriesSelector)uiComp).setExistedCategoryList(arrCategoriesList);       
        if (value != null && !value.equals("")) {
          List<String> listTaxonomy = new ArrayList<String>();
          if (arrayTaxonomy.length > 0) {
            for (int i = 0; i < arrayTaxonomy.length; i++) {
              if ((arrayTaxonomy[i] != null) && (arrayTaxonomy[i].length() > 0) && !listTaxonomy.contains(arrayTaxonomy[i])) 
                listTaxonomy.add(arrayTaxonomy[i]);
            }
          }
          ((UICategoriesSelector)uiComp).setExistedCategoryList(listTaxonomy);
        }
        ((UICategoriesSelector)uiComp).init();
      }
      uiContainer.initPopup(uiComp) ;
      String param = "returnField=" + fieldName ;
      ((ComponentSelector)uiComp).setSourceComponent(uiForm, new String[]{param}) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }  

  static public class RemoveReferenceActionListener extends EventListener<UIDocumentForm> {
    public void execute(Event<UIDocumentForm> event) throws Exception {
      UIDocumentForm uiForm = event.getSource() ;
      uiForm.isRemovePreference = true;
      String fieldName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiForm.getUIStringInput(fieldName).setValue(null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }

  static  public class CancelActionListener extends EventListener<UIDocumentForm> {
    public void execute(Event<UIDocumentForm> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction() ;
    }
  }

  static public class AddActionListener extends EventListener<UIDocumentForm> {
    public void execute(Event<UIDocumentForm> event) throws Exception {
      UIDocumentForm uiForm = event.getSource();
      List<String> inputNames = new ArrayList<String>();
      for(UIComponent uiComp : uiForm.getChildren()) {
        if(uiComp instanceof UIFormMultiValueInputSet) {
          for(UIComponent uiInput : ((UIFormMultiValueInputSet)uiComp).getChildren()) {
            if(uiInput instanceof UIFormUploadInput) {
              if(inputNames.contains(((UIFormUploadInput)uiInput).getName())) {
                ((UIFormMultiValueInputSet)uiComp).removeChild(UIFormUploadInput.class);
                break;
              }
              inputNames.add(((UIFormUploadInput)uiInput).getName());
            }
          }
        }
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent());
    }
  }

  static public class RemoveActionListener extends EventListener<UIDocumentForm> {
    public void execute(Event<UIDocumentForm> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource().getParent()) ;
    }
  }  
}