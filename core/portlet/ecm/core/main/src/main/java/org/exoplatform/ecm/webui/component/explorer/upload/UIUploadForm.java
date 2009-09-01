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
import javax.jcr.ItemExistsException;
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
import org.exoplatform.services.cms.i18n.MultiLanguageService;
import org.exoplatform.services.cms.impl.DMSConfiguration;
import org.exoplatform.services.cms.impl.DMSRepositoryConfiguration;
import org.exoplatform.services.cms.taxonomy.TaxonomyService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.upload.UploadResource;
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
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormUploadInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

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
          template = "app:/groovy/webui/component/explorer/upload/UIUploadForm.gtmpl",
          events = {
            @EventConfig(listeners = UIUploadForm.SaveActionListener.class), 
            @EventConfig(listeners = UIUploadForm.CancelActionListener.class, phase = Phase.DECODE),
            @EventConfig(listeners = UIUploadForm.AddUploadActionListener.class, phase = Phase.DECODE),
            @EventConfig(listeners = UIUploadForm.RemoveUploadActionListener.class, phase = Phase.DECODE)
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
  
  private boolean isMultiLanguage_;
  private String language_;
  private boolean isDefault_;
  private List<String> listTaxonomy = new ArrayList<String>();
  private List<String> listTaxonomyName = new ArrayList<String>();
  
  private int numberUploadFile = 1;
  private HashMap<String, List<String>> mapTaxonomies = new HashMap<String, List<String>>();
  private List<Node> listUploadedNodes = new ArrayList<Node>();
  
  public UIUploadForm() throws Exception {
    setMultiPart(true) ;
    addUIFormInput(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null)) ;
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPref = pcontext.getRequest().getPreferences();
    String limitPref = portletPref.getValue(Utils.UPLOAD_SIZE_LIMIT_MB, "");
    UIFormUploadInput uiInput = null;
    if (limitPref != null) {
      try {
        uiInput = new UIFormUploadInput(FIELD_UPLOAD, FIELD_UPLOAD, Integer.parseInt(limitPref.trim()));
      } catch (NumberFormatException e) {
        uiInput = new UIFormUploadInput(FIELD_UPLOAD, FIELD_UPLOAD);
      }
    } else {
      uiInput = new UIFormUploadInput(FIELD_UPLOAD, FIELD_UPLOAD);
    }
    addUIFormInput(uiInput);
  }
  
  public int getNumberUploadFile() {
    return numberUploadFile;
  }
  
  public void setNumberUploadFile(int numberUpload) {
    numberUploadFile = numberUpload;
  }
  
  public HashMap<String, List<String>> getMapTaxonomies() {
    return mapTaxonomies;
  }
  
  public void setMapTaxonomies(HashMap<String, List<String>> mapTaxonomiesAvaiable) {
    mapTaxonomies = mapTaxonomiesAvaiable;
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
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    String repository = uiExplorer.getRepositoryName();
    DMSConfiguration dmsConfig = getApplicationComponent(DMSConfiguration.class);
    DMSRepositoryConfiguration dmsRepoConfig = dmsConfig.getConfig(repository);
    String workspaceName = dmsRepoConfig.getSystemWorkspace();    
    NodeHierarchyCreator nodeHierarchyCreator = getApplicationComponent(NodeHierarchyCreator.class);
    Session session = uiExplorer.getSessionByWorkspace(workspaceName);
    return ((Node)session.getItem(nodeHierarchyCreator.getJcrPath(BasePath.TAXONOMIES_TREE_STORAGE_PATH))).getPath();
  }
  
  public void initFieldInput() throws Exception {
    TaxonomyService taxonomyService = getApplicationComponent(TaxonomyService.class);
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    Node currentNode = uiExplorer.getCurrentNode();
    List<Node> listCategories = taxonomyService.getAllCategories(currentNode);
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPref = pcontext.getRequest().getPreferences();
    String categoryMandatoryWhenFileUpload =  portletPref.getValue(Utils.CATEGORY_MANDATORY, "").trim();
    for (Node itemNode : listCategories) {
      String categoryPath = itemNode.getPath().replaceAll(getPathTaxonomy() + "/", "");
      if (!listTaxonomy.contains(categoryPath)) {
        listTaxonomy.add(categoryPath);
        listTaxonomyName.add(categoryPath);
      }
    }
    UIFormMultiValueInputSet uiFormMultiValue = createUIComponent(UIFormMultiValueInputSet.class, "UploadMultipleInputset", null);
    uiFormMultiValue.setId(FIELD_LISTTAXONOMY);
    uiFormMultiValue.setName(FIELD_LISTTAXONOMY);
    uiFormMultiValue.setType(UIFormStringInput.class);
    uiFormMultiValue.setEditable(false);
    if (categoryMandatoryWhenFileUpload.equalsIgnoreCase("true")) {
      uiFormMultiValue.addValidator(MandatoryValidator.class);
    }
    uiFormMultiValue.setValue(listTaxonomyName);
    addUIFormInput(uiFormMultiValue);
  }
  
  public String[] getActions() {
    return new String[] {"Save", "Cancel"};
  }

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
    List<String> indexMapTaxonomy = new ArrayList<String>();
    if (mapTaxonomies.containsKey(selectField)){
      indexMapTaxonomy = mapTaxonomies.get(selectField);
      mapTaxonomies.remove(selectField);
    }
    if (!indexMapTaxonomy.contains(valueTaxonomy)) indexMapTaxonomy.add(valueTaxonomy);
    mapTaxonomies.put(selectField, indexMapTaxonomy);
    
    updateAdvanceTaxonomy(selectField);
    UIUploadManager uiUploadManager = getParent();
    uiUploadManager.removeChildById(POPUP_TAXONOMY);
  }
  
  private void updateAdvanceTaxonomy(String selectField) throws Exception {    
    List<UIComponent> listChildren = getChildren();
    for (UIComponent uiComp : listChildren) {
      if (uiComp.getId().equals(selectField)) {
        UIFormMultiValueInputSet uiFormMultiValueInputSet = getChildById(selectField);
        if (mapTaxonomies.containsKey(selectField)) uiFormMultiValueInputSet.setValue(mapTaxonomies.get(selectField));
      }
    }
  }
  
  static  public class SaveActionListener extends EventListener<UIUploadForm> {
    public void execute(Event<UIUploadForm> event) throws Exception {
      UIUploadForm uiForm = event.getSource();
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      UIJCRExplorer uiExplorer = uiForm.getAncestorOfType(UIJCRExplorer.class) ;
      String repository = uiExplorer.getRepositoryName();
      
      UIUploadManager uiManager = uiForm.getParent();
      UIUploadContainer uiUploadContainer = uiManager.getChild(UIUploadContainer.class);
      UploadService uploadService = uiForm.getApplicationComponent(UploadService.class);
      UIUploadContent uiUploadContent = uiManager.findFirstComponentOfType(UIUploadContent.class);
      List<String[]> listArrValues = new ArrayList<String[]>();
      
      // Proccess with save multiple upload form   
      List<UIComponent> listFormChildren = uiForm.getChildren();
      int index = 0;
      for (UIComponent uiComp : listFormChildren) {
        if(uiComp instanceof UIFormUploadInput) {
        String[] arrayId = uiComp.getId().split(FIELD_UPLOAD);
        if ((arrayId.length > 0) && (arrayId[0].length() > 0)) index = new Integer(arrayId[0]).intValue();
        UIFormUploadInput uiFormUploadInput;  
        if (index == 0){
          uiFormUploadInput = (UIFormUploadInput)uiForm.getUIInput(FIELD_UPLOAD);
        } else {
          uiFormUploadInput = (UIFormUploadInput)uiForm.getUIInput(index + FIELD_UPLOAD);
        }
      CmsService cmsService = uiForm.getApplicationComponent(CmsService.class) ;
      TaxonomyService taxonomyService = uiForm.getApplicationComponent(TaxonomyService.class);
      if(uiFormUploadInput.getUploadResource() == null) {
        if ((uiForm.listUploadedNodes != null) && (uiForm.listUploadedNodes.size() > 0)) {
          for (Node uploadedNode : uiForm.listUploadedNodes) {
            uploadedNode.remove();
          }
          uiExplorer.getCurrentNode().save();
          uiForm.listUploadedNodes.clear();
        }
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
      String fileName = uiFormUploadInput.getUploadResource().getFileName();
      MultiLanguageService multiLangService = uiForm.getApplicationComponent(MultiLanguageService.class) ;
      if(fileName == null || fileName.length() == 0) {
        if ((uiForm.listUploadedNodes != null) && (uiForm.listUploadedNodes.size() > 0)) {
          for (Node uploadedNode : uiForm.listUploadedNodes) {
            uploadedNode.remove();
          }
          uiExplorer.getCurrentNode().save();
          uiForm.listUploadedNodes.clear();
        }
        uiApp.addMessage(new ApplicationMessage("UIUploadForm.msg.fileName-error", null, 
                                                ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      }      
      String[] arrFilterChar = {"&", "$", "@", ":", "]", "[", "*", "%", "!", "+", "(", ")", "'", "#", ";", "}", "{"} ;
      InputStream inputStream = uiFormUploadInput.getUploadDataAsStream();
      String name;
      if (index == 0){
        name = uiForm.getUIStringInput(FIELD_NAME).getValue();
      } else {
        name = uiForm.getUIStringInput(index + FIELD_NAME).getValue();
      }
      
      if (name == null) {
        for(String filterChar : arrFilterChar) {
          if (fileName.indexOf(filterChar) > -1) {
            name = fileName.replace(filterChar, "");
            fileName = name;
          }
        }
        name = fileName;
      } else name = name.trim();
      
      if (!Utils.isNameValid(name, arrFilterChar)) {
          uiApp.addMessage(new ApplicationMessage("UIUploadForm.msg.fileName-invalid", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
      }
      
      List<String> listTaxonomyNameNew = new ArrayList<String>();
      if (index == 0) listTaxonomyNameNew = uiForm.mapTaxonomies.get(FIELD_LISTTAXONOMY);
      else listTaxonomyNameNew = uiForm.mapTaxonomies.get(index + FIELD_LISTTAXONOMY);
      
      String taxonomyTree = null;
      String taxonomyPath = null;
      if (listTaxonomyNameNew != null) {
        for(String categoryPath : listTaxonomyNameNew) {
          try {
            if (categoryPath.startsWith("/")) categoryPath = categoryPath.substring(1);
            taxonomyTree = categoryPath.substring(0, categoryPath.indexOf("/"));
            taxonomyPath = categoryPath.substring(categoryPath.indexOf("/") + 1);
            taxonomyService.getTaxonomyTree(repository, taxonomyTree).hasNode(taxonomyPath);
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
      String newNodeUUID = null;
      try {
        String pers = PermissionType.ADD_NODE + "," + PermissionType.SET_PROPERTY ;
        selectedNode.getSession().checkPermission(selectedNode.getPath(), pers);
        
        if(uiForm.isMultiLanguage()) {
          ValueFactoryImpl valueFactory = (ValueFactoryImpl) uiExplorer.getSession().getValueFactory() ;
          Value contentValue = valueFactory.createValue(inputStream) ;
          multiLangService.addFileLanguage(selectedNode, name, contentValue, mimeType, uiForm.getLanguageSelected(), uiExplorer.getRepositoryName(), uiForm.isDefault_) ;
          uiExplorer.setIsHidePopup(true) ;
          UIMultiLanguageManager uiLanguageManager = uiForm.getAncestorOfType(UIMultiLanguageManager.class) ;
          UIMultiLanguageForm uiMultiForm = uiLanguageManager.getChild(UIMultiLanguageForm.class) ;
          uiMultiForm.doSelect(uiExplorer.getCurrentNode()) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiLanguageManager);
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
            
            selectedNode.save();
            selectedNode.getSession().save();
            if ((listTaxonomyNameNew != null) && (listTaxonomyNameNew.size() > 0)) {
              Node newNode = null;
              try {
                newNode = selectedNode.getSession().getNodeByUUID(newNodeUUID);
              } catch(ItemNotFoundException e) {
                newNode = Utils.findNodeByUUID(repository, newNodeUUID);
              }
              if (newNode != null) {
                for (String categoryPath : listTaxonomyNameNew) {
                  try {
                    if (categoryPath.startsWith("/")) categoryPath = categoryPath.substring(1);
                    taxonomyTree = categoryPath.substring(0, categoryPath.indexOf("/"));
                    taxonomyPath = categoryPath.substring(categoryPath.indexOf("/") + 1);
                    taxonomyService.addCategory(newNode, taxonomyTree, taxonomyPath);
                  } catch (ItemExistsException e) {
                    uiApp.addMessage(new ApplicationMessage("UIUploadForm.msg.ItemExistsException",
                        null, ApplicationMessage.WARNING));
                    event.getRequestContext().addUIComponentToUpdateByAjax(
                        uiApp.getUIPopupMessages());
                    return;
                  } catch (RepositoryException e) {
                    LOG.error("Unexpected error", e);
                    JCRExceptionManager.process(uiApp, e);
                    return;
                  }
                }
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
            } else {
              contentNode.setProperty(Utils.JCR_DATA, inputStream);              
            }
            if (node.isNodeType("exo:datetime")) {
              node.setProperty("exo:dateModified",new GregorianCalendar()) ;
            }
            node.save();
            if (listTaxonomyNameNew != null) {
              for (String categoryPath : listTaxonomyNameNew) {
                try {              
                  if (categoryPath.startsWith("/")) categoryPath = categoryPath.substring(1);
                  taxonomyTree = categoryPath.substring(0, categoryPath.indexOf("/"));
                  taxonomyPath = categoryPath.substring(categoryPath.indexOf("/") + 1);
                  taxonomyService.addCategory(node, taxonomyTree, taxonomyPath);
                } catch (ItemExistsException e) {
                  uiApp.addMessage(new ApplicationMessage("UIUploadForm.msg.ItemExistsException",
                      null, ApplicationMessage.WARNING));
                  event.getRequestContext().addUIComponentToUpdateByAjax(
                      uiApp.getUIPopupMessages());
                  return;
                } catch (RepositoryException e) {
                  LOG.error("Unexpected error", e);
                  JCRExceptionManager.process(uiApp, e);
                  return;
                }
              }
            }
          }
        }
        uiExplorer.getSession().save() ;
        
        Node uploadedNode = null;
        if(uiForm.isMultiLanguage_) {
          uiUploadContainer.setUploadedNode(selectedNode);
          uploadedNode = selectedNode;
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
          if(newNode != null) {
            uiUploadContainer.setUploadedNode(newNode);
            uploadedNode = newNode;
          }
        }
        double size = uploadService.getUploadResource(uiFormUploadInput.getUploadId()).getEstimatedSize()/1024;
        String fileSize = Double.toString(size);
        String iconUpload = Utils.getNodeTypeIcon(uploadedNode, "16x16Icon").replaceAll("nt_file16x16Icon ", "");
        String[] arrValues = {iconUpload, fileName, name, fileSize +" Kb", mimeType, uploadedNode.getPath()};
        uiForm.listUploadedNodes.add(uploadedNode);
        listArrValues.add(arrValues);
        inputStream.close();
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
      
        // Begin proccess with save multiple upload form
        }
      }
      uiUploadContent.setListUploadValues(listArrValues);
      uiManager.setRenderedChild(UIUploadContainer.class);
      uiExplorer.setIsHidePopup(true);
      
      uiExplorer.updateAjax(event);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager);
      //End proccess with save multiple upload form      
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
        String[] arrayId = id.split(FIELD_LISTTAXONOMY);
        int index = 0;
        int indexRemove = 0;
        if ((arrayId.length > 0) && (arrayId[0].length() > 0)) index = new Integer(arrayId[0]).intValue();
        if ((arrayId.length > 0) && (arrayId[1].length() > 0)) indexRemove = new Integer(arrayId[1]).intValue();
        String idFieldListTaxonomy;
        if (index == 0) idFieldListTaxonomy = FIELD_LISTTAXONOMY; else idFieldListTaxonomy = index + FIELD_LISTTAXONOMY; 
        if (uiUploadForm.mapTaxonomies.containsKey(idFieldListTaxonomy)) {
          List<String> indexMapTaxonomy = new ArrayList<String>();
          indexMapTaxonomy = uiUploadForm.mapTaxonomies.get(idFieldListTaxonomy);
          uiUploadForm.mapTaxonomies.remove(idFieldListTaxonomy);
          if (indexMapTaxonomy.size() > indexRemove) indexMapTaxonomy.remove(indexRemove);
          uiUploadForm.mapTaxonomies.put(idFieldListTaxonomy, indexMapTaxonomy);
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
      String fieldTaxonomyId = event.getRequestContext().getRequestParameter(OBJECTID);
      String[] arrayId = fieldTaxonomyId.split(FIELD_LISTTAXONOMY);
      int index = 0;
      if ((arrayId.length > 0) && (arrayId[0].length() > 0)) index = new Integer(arrayId[0]).intValue();
      String idFieldUpload;
      if (index == 0) idFieldUpload = FIELD_UPLOAD; else idFieldUpload = index + FIELD_UPLOAD;
      UIFormUploadInput uiFormUploadInput = uiUploadForm.getChildById(idFieldUpload);
      UploadResource uploadResource = uiFormUploadInput.getUploadResource();
      if (uploadResource == null) {
        UIApplication uiApp = uiUploadForm.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UIUploadForm.msg.upload-not-null", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      UIUploadManager uiUploadManager = uiUploadForm.getParent();
      UIJCRExplorer uiExplorer = uiUploadForm.getAncestorOfType(UIJCRExplorer.class);
      NodeHierarchyCreator nodeHierarchyCreator = 
        uiUploadForm.getApplicationComponent(NodeHierarchyCreator.class);
      String repository = uiExplorer.getRepositoryName();
      DMSConfiguration dmsConfig = uiUploadForm.getApplicationComponent(DMSConfiguration.class);
      DMSRepositoryConfiguration dmsRepoConfig = dmsConfig.getConfig(repository);
      String workspaceName = dmsRepoConfig.getSystemWorkspace();
      
      UIPopupWindow uiPopupWindow = uiUploadManager.initPopupTaxonomy(POPUP_TAXONOMY);
      UIOneTaxonomySelector uiOneTaxonomySelector = 
        uiUploadManager.createUIComponent(UIOneTaxonomySelector.class, null, null);
      uiPopupWindow.setUIComponent(uiOneTaxonomySelector);
      uiOneTaxonomySelector.setIsDisable(workspaceName, false);
      String rootTreePath = nodeHierarchyCreator.getJcrPath(BasePath.TAXONOMIES_TREE_STORAGE_PATH);      
      Session session = 
        uiUploadForm.getAncestorOfType(UIJCRExplorer.class).getSessionByWorkspace(workspaceName);
      Node rootTree = (Node) session.getItem(rootTreePath);      
      NodeIterator childrenIterator = rootTree.getNodes();
      while (childrenIterator.hasNext()) {
        Node childNode = childrenIterator.nextNode();
        rootTreePath = childNode.getPath();
        break;
      }      
      uiOneTaxonomySelector.setRootNodeLocation(repository, workspaceName, rootTreePath);
      uiOneTaxonomySelector.setExceptedNodeTypesInPathPanel(new String[] {Utils.EXO_SYMLINK});
      uiOneTaxonomySelector.init(uiExplorer.getSystemProvider());
      String param = "returnField=" + fieldTaxonomyId;
      uiOneTaxonomySelector.setSourceComponent(uiUploadForm, new String[]{param});
      uiPopupWindow.setRendered(true);
      uiPopupWindow.setShow(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiUploadManager);
    }
  }
  
  static  public class AddUploadActionListener extends EventListener<UIUploadForm> {
    public void execute(Event<UIUploadForm> event) throws Exception {      
      UIUploadForm uiUploadForm = event.getSource();
      List<UIComponent> listChildren = uiUploadForm.getChildren();
      int index = 0;
      int numberUploadFile = 0;
      String fieldFieldUpload = null;
      for (UIComponent uiComp : listChildren) {
        if(uiComp instanceof UIFormUploadInput) {
          fieldFieldUpload = uiComp.getId();
          numberUploadFile++;
        }
      }
      if (fieldFieldUpload != null) {
        String[] arrayId = fieldFieldUpload.split(FIELD_UPLOAD);
        if ((arrayId.length > 0) && (arrayId[0].length() > 0)) index = new Integer(arrayId[0]).intValue();
      }
      index++; 
      uiUploadForm.addUIFormInput(new UIFormStringInput(index + FIELD_NAME, index + FIELD_NAME, null));
      PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
      PortletPreferences portletPref = pcontext.getRequest().getPreferences();
      String limitPref = portletPref.getValue(Utils.UPLOAD_SIZE_LIMIT_MB, "");
      UIFormUploadInput uiInput = null;      
      if (limitPref != null) {
        try {
          uiInput = new UIFormUploadInput(index + FIELD_UPLOAD, index + FIELD_UPLOAD, Integer.parseInt(limitPref.trim()));
        } catch (NumberFormatException e) {
          uiInput = new UIFormUploadInput(index + FIELD_UPLOAD, index + FIELD_UPLOAD);
        }
      } else {
        uiInput = new UIFormUploadInput(index + FIELD_UPLOAD, index + FIELD_UPLOAD);
      }
      uiUploadForm.addUIFormInput(uiInput);      
      UIFormMultiValueInputSet uiFormMultiValue = uiUploadForm.createUIComponent(UIFormMultiValueInputSet.class, "UploadMultipleInputset", null);
      uiFormMultiValue.setId(index + FIELD_LISTTAXONOMY);
      uiFormMultiValue.setName(index + FIELD_LISTTAXONOMY);
      uiFormMultiValue.setType(UIFormStringInput.class);
      uiFormMultiValue.setEditable(false);
      uiUploadForm.addUIFormInput(uiFormMultiValue);
      uiUploadForm.setNumberUploadFile(numberUploadFile + 1);
      uiUploadForm.setRendered(true);      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiUploadForm.getParent());
    }
  }
  
  static  public class RemoveUploadActionListener extends EventListener<UIUploadForm> {
    public void execute(Event<UIUploadForm> event) throws Exception {
      String id = event.getRequestContext().getRequestParameter(OBJECTID);
      UIUploadForm uiUploadForm = event.getSource();
      List<UIComponent> listChildren = uiUploadForm.getChildren();
      int index = 0;
      for (UIComponent uiComp : listChildren) {
        if(uiComp instanceof UIFormUploadInput) index++;
      }
      String[] arrayId = id.split(FIELD_NAME);
      int indexRemove = 0;
      if ((arrayId.length > 0) && (arrayId[0].length() > 0)) indexRemove = new Integer(arrayId[0]).intValue();
      if (indexRemove == 0) {
        uiUploadForm.removeChildById(FIELD_NAME);
        uiUploadForm.removeChildById(FIELD_UPLOAD);
        uiUploadForm.removeChildById(FIELD_LISTTAXONOMY);
        if (uiUploadForm.mapTaxonomies.containsKey(FIELD_LISTTAXONOMY)) 
          uiUploadForm.mapTaxonomies.remove(FIELD_LISTTAXONOMY);
      } else {
        uiUploadForm.removeChildById(indexRemove + FIELD_NAME);
        uiUploadForm.removeChildById(indexRemove + FIELD_UPLOAD);
        uiUploadForm.removeChildById(indexRemove + FIELD_LISTTAXONOMY);
        if (uiUploadForm.mapTaxonomies.containsKey(indexRemove + FIELD_LISTTAXONOMY)) 
          uiUploadForm.mapTaxonomies.remove(indexRemove + FIELD_LISTTAXONOMY);
      }
      uiUploadForm.setNumberUploadFile(index - 1);
      uiUploadForm.setRendered(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiUploadForm.getParent());
    }
  }
}
