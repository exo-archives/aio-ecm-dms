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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.form.UIDialogForm;
import org.exoplatform.ecm.webui.popup.UIPopupComponent;
import org.exoplatform.ecm.webui.selector.ComponentSelector;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.tree.selectmany.UICategoriesSelector;
import org.exoplatform.ecm.webui.utils.DialogFormUtil;
import org.exoplatform.ecm.webui.utils.LockUtil;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.categories.CategoriesService;
import org.exoplatform.services.cms.i18n.MultiLanguageService;
import org.exoplatform.services.cms.templates.TemplateService;
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

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Apr 24, 2007 9:05:25 AM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig(listeners = UILanguageDialogForm.SaveActionListener.class),
      @EventConfig(listeners = UILanguageDialogForm.CancelActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UILanguageDialogForm.AddActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UILanguageDialogForm.ShowComponentActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UILanguageDialogForm.RemoveReferenceActionListener.class, confirm = "DialogFormField.msg.confirm-delete", phase = Phase.DECODE),
      @EventConfig(listeners = UILanguageDialogForm.RemoveActionListener.class, phase = Phase.DECODE)
    }
)
public class UILanguageDialogForm extends UIDialogForm implements UIPopupComponent, UISelectable {

  private boolean isAddNew_ = false; 
  private String selectedLanguage_ = null;
  private boolean isDefault_ = false;
  private String documentType_;
  
  public UILanguageDialogForm() throws Exception {
    setActions(new String[]{"Save", "Cancel"});
  }
    
  public void doSelect(String selectField, Object value) {
    isUpdateSelect = true;    
    UIFormInput formInput = getUIInput(selectField);
    if(formInput instanceof UIFormInputBase) {
      ((UIFormInputBase)formInput).setValue(value.toString());
    }else if(formInput instanceof UIFormMultiValueInputSet) {
      UIFormMultiValueInputSet  inputSet = (UIFormMultiValueInputSet) formInput;
      UIFormInputBase input = inputSet.getChild(inputSet.getChildren().size()-1);
      input.setValue(value.toString());
    }
    UIAddLanguageContainer uiContainer = getParent();
    uiContainer.removeChildById("PopupComponent");
  }
  
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  
  public void setTemplateNode(String type) { documentType_ = type;}
  
  public String getTemplate() {
    repositoryName = getAncestorOfType(UIJCRExplorer.class).getRepositoryName();
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    String userName = Util.getPortalRequestContext().getRemoteUser();
    try {      
      return templateService.getTemplatePathByUser(true, documentType_, userName, repositoryName);
    } catch (Exception e) {
      UIApplication uiApp = getAncestorOfType(UIApplication.class);
      Object[] arg = { documentType_ };
      uiApp.addMessage(new ApplicationMessage("UIDocumentForm.msg.not-support", arg, 
                                              ApplicationMessage.ERROR));
      return null;
    } 
  }

  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    return getAncestorOfType(UIJCRExplorer.class).getJCRTemplateResourceResolver();
  }
  
  public boolean isAddNew() {return isAddNew_;}
  public void addNew(boolean b) {isAddNew_ = b;}
  
  public void setIsAddNew(boolean isAddNew) { isAddNew_ = isAddNew; }
  
  public boolean isEditing() { return !isAddNew_; }
  
  public Node getCurrentNode() throws Exception { 
    return getAncestorOfType(UIJCRExplorer.class).getCurrentNode(); 
  }
  
  public void setSelectedLanguage(String selectedLanguage) { selectedLanguage_ = selectedLanguage; }
  public String getSelectedLanguage() { return selectedLanguage_; }
  
  public void setIsDefaultLanguage(boolean isDefault) { isDefault_ = isDefault; }
  public boolean isDefaultLanguage() { return isDefault_; } 
  
  private boolean hasNodeTypeNTResource(Node node) throws Exception {
    if(node.hasNodes()) {
      NodeIterator nodeIter = node.getNodes();
      while(nodeIter.hasNext()) {
        Node childNode = nodeIter.nextNode();
        if(childNode.getPrimaryNodeType().getName().equals("nt:resource")) return true;
      }
    }
    return false;
  }
  
  static  public class SaveActionListener extends EventListener<UILanguageDialogForm> {
    public void execute(Event<UILanguageDialogForm> event) throws Exception {
      UILanguageDialogForm languageDialogForm = event.getSource();
      UIJCRExplorer uiExplorer = languageDialogForm.getAncestorOfType(UIJCRExplorer.class);
      Node node = uiExplorer.getCurrentNode();
      if(node.isLocked()) {
        String lockToken = LockUtil.getLockToken(node);
        if(lockToken != null) uiExplorer.getSession().addLockToken(lockToken);
      }
      MultiLanguageService multiLanguageService = languageDialogForm.getApplicationComponent(MultiLanguageService.class);
      UIApplication uiApp = languageDialogForm.getAncestorOfType(UIApplication.class);
      if (languageDialogForm.selectedLanguage_ == null) {
        uiApp.addMessage(new ApplicationMessage("UILanguageDialogForm.msg.select-lang", null, 
                                                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(!uiExplorer.hasAddPermission()) {
        uiApp.addMessage(new ApplicationMessage("UILanguageDialogForm.msg.access-denied", null, 
                                                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(node.hasNode(Utils.EXO_IMAGE)) {
        Map inputProperties = DialogFormUtil.prepareMap(languageDialogForm.getChildren(), languageDialogForm.getInputProperties());
        try {
          multiLanguageService.addLanguage(node, inputProperties, languageDialogForm.getSelectedLanguage(), languageDialogForm.isDefaultLanguage(), Utils.EXO_IMAGE);
        } catch(AccessDeniedException ace) {
          uiApp.addMessage(new ApplicationMessage("UILanguageDialogForm.msg.access-denied", null, 
                                                  ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
      } else if(languageDialogForm.hasNodeTypeNTResource(node)) {
        Map inputProperties = DialogFormUtil.prepareMap(languageDialogForm.getChildren(), languageDialogForm.getInputProperties());
        try {
          multiLanguageService.addFileLanguage(node, languageDialogForm.getSelectedLanguage(), inputProperties, languageDialogForm.isDefaultLanguage());
        } catch(AccessDeniedException ace) {
          uiApp.addMessage(new ApplicationMessage("UILanguageDialogForm.msg.access-denied", null, 
                                                  ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
      } else {
        Map map = DialogFormUtil.prepareMap(languageDialogForm.getChildren(), languageDialogForm.properties);
        try {
          multiLanguageService.addLanguage(node, map, languageDialogForm.getSelectedLanguage(), languageDialogForm.isDefaultLanguage());
        } catch(AccessDeniedException ace) {
          uiApp.addMessage(new ApplicationMessage("UILanguageDialogForm.msg.access-denied", null, 
                                                  ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;        
        }
      }
      node.save();
      UIMultiLanguageManager uiManager = languageDialogForm.getAncestorOfType(UIMultiLanguageManager.class);
      UIMultiLanguageForm uiMultiForm = uiManager.getChild(UIMultiLanguageForm.class);
      uiMultiForm.doSelect(node);
      if(languageDialogForm.isDefaultLanguage()) uiExplorer.setLanguage(languageDialogForm.getSelectedLanguage());
      uiManager.setRenderedChild(UIMultiLanguageForm.class);
      UIAddLanguageContainer uiAddContainer = uiManager.getChild(UIAddLanguageContainer.class);
      UILanguageTypeForm uiLanguageTypeForm = uiAddContainer.getChild(UILanguageTypeForm.class);
      uiLanguageTypeForm.resetLanguage();
      uiAddContainer.removeChild(UILanguageDialogForm.class);
      uiAddContainer.setComponentDisplay(languageDialogForm.documentType_);
      if(!uiExplorer.getPreference().isJcrEnable()) node.getSession().save();
      uiExplorer.setIsHidePopup(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager);
      uiExplorer.updateAjax(event);      
    }
  }
  
  @SuppressWarnings("unchecked")
  static public class ShowComponentActionListener extends EventListener<UILanguageDialogForm> {
    public void execute(Event<UILanguageDialogForm> event) throws Exception {
      UILanguageDialogForm uiForm = event.getSource();
      UIAddLanguageContainer uiContainer = uiForm.getAncestorOfType(UIAddLanguageContainer.class); 
      uiForm.isShowingComponent = true;
      String fieldName = event.getRequestContext().getRequestParameter(OBJECTID);
      Map fieldPropertiesMap = uiForm.componentSelectors.get(fieldName);
      String classPath = (String)fieldPropertiesMap.get("selectorClass");
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Class clazz = Class.forName(classPath, true, cl);
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
      if (uiComp instanceof UICategoriesSelector){
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
      uiContainer.initPopup(uiComp);
      String param = "returnField=" + fieldName;
      ((ComponentSelector)uiComp).setSourceComponent(uiForm, new String[]{param});
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer);
    }
  }
  
  static public class RemoveReferenceActionListener extends EventListener<UILanguageDialogForm> {
    public void execute(Event<UILanguageDialogForm> event) throws Exception {
      UILanguageDialogForm uiForm = event.getSource() ;
      uiForm.isRemovePreference = true;
      String fieldName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiForm.getUIStringInput(fieldName).setValue(null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UILanguageDialogForm> {
    public void execute(Event<UILanguageDialogForm> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class);
      uiExplorer.cancelAction();
    }
  }
  
  static public class AddActionListener extends EventListener<UILanguageDialogForm> {
    public void execute(Event<UILanguageDialogForm> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource().getParent());
    }
  }

  static public class RemoveActionListener extends EventListener<UILanguageDialogForm> {
    public void execute(Event<UILanguageDialogForm> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource().getParent());
    }
  }
}