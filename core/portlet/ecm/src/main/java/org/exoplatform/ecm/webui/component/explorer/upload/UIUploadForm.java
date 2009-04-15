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
package org.exoplatform.ecm.webui.component.explorer.upload;

import java.io.InputStream;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.portlet.PortletPreferences;

import org.apache.commons.logging.Log;
import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIMultiLanguageForm;
import org.exoplatform.ecm.webui.component.explorer.popup.actions.UIMultiLanguageManager;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.tree.selectone.UIOneTaxonomySelector;
import org.exoplatform.ecm.webui.utils.JCRExceptionManager;
import org.exoplatform.ecm.webui.utils.LockUtil;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.categories.CategoriesService;
import org.exoplatform.services.cms.i18n.MultiLanguageService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIBreadcumbs.LocalPath;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormUploadInput;

/**
 * Created by The eXo Platform SARL
 * Author : nqhungvn
 *          nguyenkequanghung@yahoo.com
 * July 3, 2006
 * 10:07:15 AM
 */

@ComponentConfigs(
    {
      @ComponentConfig(
          lifecycle = UIFormLifecycle.class,
          template =  "system:/groovy/webui/form/UIForm.gtmpl",
          events = {
            @EventConfig(listeners = UIUploadForm.SaveActionListener.class), 
            @EventConfig(listeners = UIUploadForm.CancelActionListener.class, phase = Phase.DECODE)
          }
      ),
      @ComponentConfig(
          type = UIFormMultiValueInputSet.class,
          id="UploadMultipleInputset",
          events = {
            @EventConfig(listeners = UIUploadForm.RemoveActionListener.class, phase = Phase.DECODE),
            @EventConfig(listeners = UIUploadForm.AddActionListener.class, phase = Phase.DECODE) 
          }
      )
    }
)

public class UIUploadForm extends UIForm implements UIPopupComponent, UISelectable {

  /**
   * Logger.
   */
  private static final Log LOG  = ExoLogger.getLogger("explorer.upload.UIUploadForm");

  final static public String FIELD_NAME =  "name" ;
  final static public String FIELD_UPLOAD = "upload" ;  
  final static public String JCRCONTENT = "jcr:content";
  final static public String FIELD_TAXONOMY = "fieldTaxonomy";
  final static public String FIELD_LISTTAXONOMY = "fieldListTaxonomy";
  final static public String POPUP_TAXONOMY = "UIPopupTaxonomy";
  final static private String TAXONOMIES_ALIAS = "exoTaxonomiesPath" ;
  
  private boolean isMultiLanguage_;
  private String language_;
  private boolean isDefault_;
  private List<String> listTaxonomy = new ArrayList<String>();
  private List<String> listTaxonomyName = new ArrayList<String>();
  
  public UIUploadForm() throws Exception {
    setMultiPart(true) ;
    addUIFormInput(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null)) ;
    UIFormUploadInput uiInput = new UIFormUploadInput(FIELD_UPLOAD, FIELD_UPLOAD) ;
    addUIFormInput(uiInput);
  }
  
  public List<String> getListTaxonomy() {
    return listTaxonomy;
  }
  
  public List<String> getlistTaxonomyName() {
    return listTaxonomyName;
  }
  
  public void setListTaxonomy(List<String> listTaxonomyNew) {
    listTaxonomy = listTaxonomyNew;
  }
  
  public void setListTaxonomyName(List<String> listTaxonomyNameNew) {
    listTaxonomyName = listTaxonomyNameNew;
  }
  
  public String getPathTaxonomy() throws Exception {
    NodeHierarchyCreator nodeHierarchyCreator = getApplicationComponent(NodeHierarchyCreator.class);
    Session session = getAncestorOfType(UIJCRExplorer.class).getSession();
    return ((Node)session.getItem(nodeHierarchyCreator.getJcrPath(BasePath.TAXONOMIES_TREE_DEFINITION_PATH))).getPath();
  }
  
  public void initFieldInput() throws Exception {
    CategoriesService categoriesService = getApplicationComponent(CategoriesService.class);
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    Node currentNode = uiExplorer.getCurrentNode();
    if (categoriesService.hasCategories(currentNode)) {
      Value[] values = currentNode.getProperty("exo:category").getValues();
      for (int i = 0; i < values.length; i++) {
        String path  = uiExplorer.getSession().getNodeByUUID(values[i].getString()).getPath();
        if (!listTaxonomy.contains(path)) {
          listTaxonomy.add(path);
          listTaxonomyName.add(cutPath(path));
        }
      }
    }
    UIFormMultiValueInputSet uiFormMultiValue = createUIComponent(UIFormMultiValueInputSet.class, "UploadMultipleInputset", null);
    uiFormMultiValue.setId(FIELD_LISTTAXONOMY);
    uiFormMultiValue.setName(FIELD_LISTTAXONOMY);
    uiFormMultiValue.setType(UIFormStringInput.class);
    uiFormMultiValue.setValue(listTaxonomyName);
    addUIFormInput(uiFormMultiValue);
  }
  
  private String cutPath(String path) {
    String[] array = path.split("/");
    String value = "";
    if (array.length > 4) {
      for (int i = 4; i < array.length; i++) {
        value += array[i].trim(); 
        if (i < array.length - 1) value += "/";
      }
    } else value = path;
    return value;
  }
  
  public String[] getActions() { return new String[] {"Save", "Cancel"}; }

  public void setIsMultiLanguage(boolean isMultiLanguage, String language) { 
    isMultiLanguage_ = isMultiLanguage ;
    language_ = language ;
  }
  
  public void resetComponent() {
    removeChildById(FIELD_UPLOAD);
    addUIFormInput(new UIFormUploadInput(FIELD_UPLOAD, FIELD_UPLOAD));
  }  

  public boolean isMultiLanguage() { return isMultiLanguage_ ; }

  public void setIsDefaultLanguage(boolean isDefault) { isDefault_ = isDefault ; }

  private String getLanguageSelected() { return language_ ; }

  public void activate() throws Exception {}
  public void deActivate() throws Exception {}

  public void doSelect(String selectField, Object value) throws Exception {
    String valueTaxonomy = String.valueOf(value).trim();
    if (!listTaxonomy.contains(valueTaxonomy)) {
      listTaxonomy.add(valueTaxonomy);
      listTaxonomyName.add(cutPath(valueTaxonomy));
    }
    updateAdvanceTaxonomy();
    UIUploadManager uiUploadManager = getParent();
    uiUploadManager.removeChildById(POPUP_TAXONOMY);
  }
  
  private void updateAdvanceTaxonomy() throws Exception { 
    UIFormMultiValueInputSet uiFormMultiValueInputSet = getChild(UIFormMultiValueInputSet.class);
    uiFormMultiValueInputSet.setValue(listTaxonomyName);
  }
  
  static  public class SaveActionListener extends EventListener<UIUploadForm> {
    public void execute(Event<UIUploadForm> event) throws Exception {
      UIUploadForm uiForm = event.getSource();
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class) ;
      UIFormUploadInput input = (UIFormUploadInput)uiForm.getUIInput(FIELD_UPLOAD);
      CmsService cmsService = uiForm.getApplicationComponent(CmsService.class) ;
      CategoriesService categoriesService = uiForm.getApplicationComponent(CategoriesService.class);
      if(input.getUploadResource() == null) {
        uiApp.addMessage(new ApplicationMessage("UIUploadForm.msg.fileName-error", null, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(uiExplorer.getCurrentNode().isLocked()) {
        String lockToken = LockUtil.getLockToken(uiExplorer.getCurrentNode());
        if(lockToken != null) uiExplorer.getSession().addLockToken(lockToken);
      }
      PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
      PortletPreferences portletPref = pcontext.getRequest().getPreferences();
      String categoryMandatoryWhenFileUpload =  portletPref.getValue(Utils.CATEGORY_MANDATORY, "").trim();    
      if (categoryMandatoryWhenFileUpload.equalsIgnoreCase("true") && uiForm.getListTaxonomy().size() == 0 && !uiExplorer.getCurrentNode().hasNode(JCRCONTENT)) {
        uiApp.addMessage(new ApplicationMessage("UIUploadForm.msg.taxonomyPath-error", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String fileName = input.getUploadResource().getFileName();
      MultiLanguageService multiLangService = uiForm.getApplicationComponent(MultiLanguageService.class) ;
      if(fileName == null || fileName.length() == 0) {
          uiApp.addMessage(new ApplicationMessage("UIUploadForm.msg.fileName-error", null, 
                                                  ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
      }      
      String[] arrFilterChar = {"&", "$", "@", ":", "]", "[", "*", "%", "!", "+", "(", ")", "'", "#", ";", "}", "{"} ;
      for(String filterChar : arrFilterChar) {
        if (fileName.indexOf(filterChar) > -1) {
          uiApp.addMessage(new ApplicationMessage("UIUploadForm.msg.fileName-invalid", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }

      InputStream inputStream = input.getUploadDataAsStream();
      String name = uiForm.getUIStringInput(FIELD_NAME).getValue();
      if (name == null) name = fileName;      
      else name = name.trim();
      for(String filterChar : arrFilterChar) {
        if(name.indexOf(filterChar) > -1) {
          uiApp.addMessage(new ApplicationMessage("UIUploadForm.msg.fileName-invalid", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }
      
      UIFormMultiValueInputSet uiSet = uiForm.getChild(UIFormMultiValueInputSet.class);
      if (uiSet != null) {
        List<UIComponent> listChildren = uiSet.getChildren();
        List<String> listTaxonomyNew = new ArrayList<String>();
        List<String> listTaxonomyNameNew = new ArrayList<String>();
        for (UIComponent component : listChildren) {
          UIFormStringInput uiStringInput = (UIFormStringInput)component;
          if(uiStringInput.getValue() != null) {
            String value = uiStringInput.getValue().trim();
            listTaxonomyNameNew.add(value);
            if (value.startsWith("/"))
              listTaxonomyNew.add(uiForm.getPathTaxonomy() + value);
            else
              listTaxonomyNew.add(uiForm.getPathTaxonomy() + "/" + value);
          }
        }
        uiForm.setListTaxonomy(listTaxonomyNew);
        uiForm.setListTaxonomyName(listTaxonomyNameNew);
        
        uiSet.setValue(uiForm.getListTaxonomy());
      }
      
      String[] arrayTaxonomy = new String[uiForm.getListTaxonomy().size()];
      for (int i = 0; i < arrayTaxonomy.length; i++) {
        arrayTaxonomy[i] = uiForm.getListTaxonomy().get(i).trim();
      }
           
      Session session = uiExplorer.getSession();          
      for(String categoryPath : arrayTaxonomy) {              
        if((categoryPath != null) && (categoryPath.trim().length() > 0)){
          try {
            session.getItem(categoryPath.trim());
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
            LOG.error("An unexpected error occurs", e);
            uiApp.addMessage(new ApplicationMessage("UISelectedCategoriesGrid.msg.non-categories", null, 
                ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          }
        }
      }  
      
      MimeTypeResolver mimeTypeSolver = new MimeTypeResolver() ;
      String mimeType = mimeTypeSolver.getMimeType(fileName) ;
      Node selectedNode = uiExplorer.getCurrentNode();      
      boolean isExist = selectedNode.hasNode(name) ;
      String repository = uiExplorer.getRepositoryName() ;
      String newNodeUUID = null;
      try {
        String pers = PermissionType.ADD_NODE + "," + PermissionType.SET_PROPERTY ;
        selectedNode.getSession().checkPermission(selectedNode.getPath(), pers);
        
        if(uiForm.isMultiLanguage()) {
          ValueFactoryImpl valueFactory = (ValueFactoryImpl) uiExplorer.getSession().getValueFactory() ;
          Value contentValue = valueFactory.createValue(inputStream) ;
          multiLangService.addFileLanguage(selectedNode, name, contentValue, mimeType, uiForm.getLanguageSelected(), uiExplorer.getRepositoryName(), uiForm.isDefault_) ;
          uiExplorer.setIsHidePopup(true) ;
          UIMultiLanguageManager uiManager = uiForm.getAncestorOfType(UIMultiLanguageManager.class) ;
          UIMultiLanguageForm uiMultiForm = uiManager.getChild(UIMultiLanguageForm.class) ;
          uiMultiForm.doSelect(uiExplorer.getCurrentNode()) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
        } else {
          if(!isExist) {            
            Map<String,JcrInputProperty> inputProperties = new HashMap<String,JcrInputProperty>() ;            
            JcrInputProperty nodeInput = new JcrInputProperty() ;
            nodeInput.setJcrPath("/node") ;
            nodeInput.setValue(name) ;
            nodeInput.setMixintype("mix:i18n,mix:votable,mix:commentable") ;
            nodeInput.setType(JcrInputProperty.NODE) ;
            inputProperties.put("/node",nodeInput) ;

            JcrInputProperty jcrContent = new JcrInputProperty() ;
            jcrContent.setJcrPath("/node/jcr:content") ;
            jcrContent.setValue("") ;
            jcrContent.setMixintype("dc:elementSet") ;
            jcrContent.setNodetype(Utils.NT_RESOURCE) ;
            jcrContent.setType(JcrInputProperty.NODE) ;
            inputProperties.put("/node/jcr:content",jcrContent) ;

            JcrInputProperty jcrData = new JcrInputProperty() ;
            jcrData.setJcrPath("/node/jcr:content/jcr:data") ;            
            jcrData.setValue(inputStream) ;          
            inputProperties.put("/node/jcr:content/jcr:data",jcrData) ; 

            JcrInputProperty jcrMimeType = new JcrInputProperty() ;
            jcrMimeType.setJcrPath("/node/jcr:content/jcr:mimeType") ;
            jcrMimeType.setValue(mimeType) ;          
            inputProperties.put("/node/jcr:content/jcr:mimeType",jcrMimeType) ;

            JcrInputProperty jcrLastModified = new JcrInputProperty() ;
            jcrLastModified.setJcrPath("/node/jcr:content/jcr:lastModified") ;
            jcrLastModified.setValue(new GregorianCalendar()) ;
            inputProperties.put("/node/jcr:content/jcr:lastModified",jcrLastModified) ;

            JcrInputProperty jcrEncoding = new JcrInputProperty() ;
            jcrEncoding.setJcrPath("/node/jcr:content/jcr:encoding") ;
            jcrEncoding.setValue("UTF-8") ;
            inputProperties.put("/node/jcr:content/jcr:encoding",jcrEncoding) ;          
            newNodeUUID = cmsService.storeNodeByUUID(Utils.NT_FILE, selectedNode, inputProperties, true,repository) ;
            
            selectedNode.save() ;
            if(arrayTaxonomy.length > 0) {
              Node newNode = null;
              try {
                newNode = selectedNode.getSession().getNodeByUUID(newNodeUUID);
              } catch(ItemNotFoundException e) {
                newNode = Utils.findNodeByUUID(repository, newNodeUUID);
              }
              if(newNode != null) {
              categoriesService.addMultiCategory(newNode, arrayTaxonomy, uiExplorer.getRepositoryName());
              selectedNode.getSession().save() ;                        
            }
            }
          } else {
            Node node = selectedNode.getNode(name) ;
            if(!node.getPrimaryNodeType().isNodeType(Utils.NT_FILE)) {
              Object[] args = { name } ;
              uiApp.addMessage(new ApplicationMessage("UIUploadForm.msg.name-is-exist", args, 
                                                      ApplicationMessage.WARNING)) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
              return ;
            }
            Node contentNode = node.getNode(Utils.JCR_CONTENT);
            if(node.isNodeType(Utils.MIX_VERSIONABLE)) {              
              if(!node.isCheckedOut()) node.checkout() ; 
              contentNode.setProperty(Utils.JCR_DATA, inputStream);
              contentNode.setProperty(Utils.JCR_MIMETYPE, mimeType);
              contentNode.setProperty(Utils.JCR_LASTMODIFIED, new GregorianCalendar());
              node.save() ;       
              node.checkin() ;
              node.checkout() ;
            }else {
              contentNode.setProperty(Utils.JCR_DATA, inputStream);              
            }
            if(node.isNodeType("exo:datetime")) {
              node.setProperty("exo:dateModified",new GregorianCalendar()) ;
            }
            if(arrayTaxonomy.length > 0) {
              categoriesService.addMultiCategory(node, arrayTaxonomy, uiExplorer.getRepositoryName());
            }
            node.save();
          }
        }
        uiExplorer.getSession().save() ;
        UIUploadManager uiManager = uiForm.getParent() ;
        UIUploadContainer uiUploadContainer = uiManager.getChild(UIUploadContainer.class) ;
        UploadService uploadService = uiForm.getApplicationComponent(UploadService.class) ;
        UIFormUploadInput uiChild = uiForm.getChild(UIFormUploadInput.class) ;
        if(uiForm.isMultiLanguage_) {
          uiUploadContainer.setUploadedNode(selectedNode) ; 
        } else {
          Node newNode = null ;
          if(!isExist) {
            try {
              newNode = selectedNode.getSession().getNodeByUUID(newNodeUUID);
            } catch(ItemNotFoundException e) {
              newNode = Utils.findNodeByUUID(repository, newNodeUUID);
            }
          } else {
            newNode = selectedNode.getNode(name) ;
          }
          if(newNode != null) uiUploadContainer.setUploadedNode(newNode) ;
        }
        UIUploadContent uiUploadContent = uiManager.findFirstComponentOfType(UIUploadContent.class) ;
        double size = uploadService.getUploadResource(uiChild.getUploadId()).getEstimatedSize()/1024;
        String fileSize = Double.toString(size);     
        String[] arrValues = {fileName, name, fileSize +" Kb", mimeType} ;
        uiUploadContent.setUploadValues(arrValues) ;
        inputStream.close();
        
        uploadService.removeUpload(uiChild.getUploadId()) ;
        uiManager.setRenderedChild(UIUploadContainer.class) ;
        uiExplorer.setIsHidePopup(true) ;
        uiExplorer.updateAjax(event) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
      } catch(ConstraintViolationException con) {
        Object[] args = {name, } ;
        throw new MessageException(new ApplicationMessage("UIUploadForm.msg.contraint-violation", 
                                                           args, ApplicationMessage.WARNING)) ;
      } catch(LockException lock) {
        throw new MessageException(new ApplicationMessage("UIUploadForm.msg.lock-exception", 
            null, ApplicationMessage.WARNING)) ;        
      } catch(AccessDeniedException ade) {
        throw new MessageException(new ApplicationMessage("UIActionBar.msg.access-add-denied", 
            null, ApplicationMessage.WARNING)); 
      } catch(AccessControlException ace) {
        throw new MessageException(new ApplicationMessage("UIActionBar.msg.access-add-denied", 
            null, ApplicationMessage.WARNING)); 
      } catch(Exception e) {
        LOG.error("An unexpected error occurs", e);
        JCRExceptionManager.process(uiApp, e);
        return ;
      }
    }
  }

  static  public class CancelActionListener extends EventListener<UIUploadForm> {
    public void execute(Event<UIUploadForm> event) throws Exception {
      UIJCRExplorer uiExplorer = event.getSource().getAncestorOfType(UIJCRExplorer.class) ;
      uiExplorer.cancelAction() ;
    }
  }
      
  static  public class RemoveActionListener extends EventListener<UIFormMultiValueInputSet> {
    public void execute(Event<UIFormMultiValueInputSet> event) throws Exception {
      UIFormMultiValueInputSet uiSet = event.getSource();
      UIComponent uiComponent = uiSet.getParent();
      if (uiComponent instanceof UIUploadForm) {
        UIUploadForm uiUploadForm = (UIUploadForm)uiComponent;
        String id = event.getRequestContext().getRequestParameter(OBJECTID);
        UIFormStringInput uiFormStringInput = uiSet.getChildById(id);
        String value = uiFormStringInput.getValue().trim();
        if (uiUploadForm.getlistTaxonomyName().contains(value)) {
          int indexRemove = uiUploadForm.getlistTaxonomyName().indexOf(value);
          uiUploadForm.getlistTaxonomyName().remove(indexRemove);
          uiUploadForm.getListTaxonomy().remove(indexRemove);
        }
        uiSet.removeChildById(id);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiUploadForm);
      }
    } 
  }
  
  static  public class AddActionListener extends EventListener<UIFormMultiValueInputSet> {
    public void execute(Event<UIFormMultiValueInputSet> event) throws Exception {
      UIFormMultiValueInputSet uiSet = event.getSource();
      UIUploadForm uiUploadForm =  (UIUploadForm) uiSet.getParent();
      UIUploadManager uiUploadManager = uiUploadForm.getParent();
      UIJCRExplorer uiExplorer = uiUploadForm.getAncestorOfType(UIJCRExplorer.class);
      NodeHierarchyCreator nodeHierarchyCreator = uiUploadForm.getApplicationComponent(NodeHierarchyCreator.class);
      String repository = uiExplorer.getRepositoryName();
      ManageableRepository manaRepository = 
        uiUploadForm.getApplicationComponent(RepositoryService.class).getRepository(repository);
      String workspaceName = manaRepository.getConfiguration().getSystemWorkspaceName();
      
      UIPopupWindow uiPopupWindow = uiUploadManager.initPopupTaxonomy(POPUP_TAXONOMY);
      UIOneTaxonomySelector uiOneTaxonomySelector = uiUploadManager.createUIComponent(UIOneTaxonomySelector.class, null, null);
      uiPopupWindow.setUIComponent(uiOneTaxonomySelector);
      uiOneTaxonomySelector.setIsDisable(workspaceName, false);
      String rootTreePath = nodeHierarchyCreator.getJcrPath(BasePath.TAXONOMIES_TREE_DEFINITION_PATH);      
      Session session = uiUploadForm.getAncestorOfType(UIJCRExplorer.class).getSession();
      Node rootTree = (Node) session.getItem(rootTreePath);      
      NodeIterator childrenIterator = rootTree.getNodes();
      while (childrenIterator.hasNext()) {
        Node childNode = childrenIterator.nextNode();
        rootTreePath = childNode.getPath();
        break;
      }      
      uiOneTaxonomySelector.setRootNodeLocation(repository, workspaceName, rootTreePath);
      uiOneTaxonomySelector.init(uiExplorer.getSystemProvider());
      String param = "returnField=" + FIELD_TAXONOMY ;
      uiOneTaxonomySelector.setSourceComponent(uiUploadForm, new String[]{param});
      uiPopupWindow.setRendered(true);
      uiPopupWindow.setShow(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiUploadManager);
    }
  }
}
