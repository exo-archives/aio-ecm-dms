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
package org.exoplatform.ecm.webui.component.explorer;

import java.awt.Image;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;

import javax.imageio.ImageIO;
import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.commons.logging.Log;
import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.PortalContainerInfo;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.ecm.jcr.model.Preference;
import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.ecm.webui.component.explorer.control.UIActionBar;
import org.exoplatform.ecm.webui.component.explorer.sidebar.UITreeExplorer;
import org.exoplatform.ecm.webui.component.explorer.sidebar.UITreeNodePageIterator;
import org.exoplatform.ecm.webui.presentation.AbstractActionComponent;
import org.exoplatform.ecm.webui.presentation.NodePresentation;
import org.exoplatform.ecm.webui.presentation.removeattach.RemoveAttachmentComponent;
import org.exoplatform.ecm.webui.presentation.removecomment.RemoveCommentComponent;
import org.exoplatform.ecm.webui.utils.JCRExceptionManager;
import org.exoplatform.ecm.webui.utils.PermissionUtil;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.comments.CommentsService;
import org.exoplatform.services.cms.documents.FavouriteService;
import org.exoplatform.services.cms.i18n.MultiLanguageService;
import org.exoplatform.services.cms.impl.DMSConfiguration;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.cms.link.LinkUtils;
import org.exoplatform.services.cms.link.NodeFinder;
import org.exoplatform.services.cms.link.NodeLinkAware;
import org.exoplatform.services.cms.mimetype.DMSMimeTypeResolver;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.cms.thumbnail.ThumbnailPlugin;
import org.exoplatform.services.cms.thumbnail.ThumbnailService;
import org.exoplatform.services.cms.timeline.TimelineService;
import org.exoplatform.services.cms.voting.VotingService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.audit.AuditHistory;
import org.exoplatform.services.jcr.ext.audit.AuditService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIRightClickPopupMenu;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.ext.UIExtension;
import org.exoplatform.webui.ext.UIExtensionManager;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Sep 3, 2006
 * 10:07:15 AM
 * Editor : Pham Tuan
 *          phamtuanchip@gmail.com
 * Nov 10, 2006         
 */
@ComponentConfig(
    events = {
        @EventConfig(listeners = UIDocumentInfo.ChangeNodeActionListener.class),
        @EventConfig(listeners = UIDocumentInfo.ViewNodeActionListener.class),
        @EventConfig(listeners = UIDocumentInfo.SortActionListener.class),
        @EventConfig(listeners = UIDocumentInfo.VoteActionListener.class),
        @EventConfig(listeners = UIDocumentInfo.ChangeLanguageActionListener.class),
        @EventConfig(listeners = UIDocumentInfo.DownloadActionListener.class),
        @EventConfig(listeners = UIDocumentInfo.StarClickActionListener.class),
        @EventConfig(listeners = UIDocumentInfo.ShowPageActionListener.class),
        @EventConfig(listeners = UIDocumentInfo.SortTimelineASCActionListener.class),
        @EventConfig(listeners = UIDocumentInfo.SortTimelineDESCActionListener.class)
    }
)
public class UIDocumentInfo extends UIContainer implements NodePresentation {

  final private static String CONTENT_PAGE_ITERATOR_ID = "ContentPageIterator".intern();

  final private static String COMMENT_COMPONENT = "Comment".intern();

	private static final Log LOG  = ExoLogger.getLogger("explorer.search.UIDocumentInfo");
  
  private String typeSort_ = Preference.SORT_BY_NODETYPE;
  private String sortOrder_ = Preference.BLUE_DOWN_ARROW;
  private Node currentNode_ ;
  private boolean isDocumentTemplate_ = false;
  private String currentRepository_ = null;
  private String currentWorkspaceName_ = null;
  private String selectedLang_ = null;

  private UIPageIterator pageIterator_ ;

	private List<Node> todayNodes;
  private List<Node> yesterdayNodes;
  private List<Node> earlierThisWeekNodes;
  private List<Node> earlierThisMonthNodes;
  private List<Node> earlierThisYearNodes;
  
	private static String timeLineSortByFavourite = Preference.BLUE_DOWN_ARROW;
  private static String timeLineSortByName = "";
  private static String timeLineSortByDate = "";
	private static FavouriteService favouriteService_ = null;
	
	static {
		ExoContainer myContainer = ExoContainerContext.getCurrentContainer();
		favouriteService_ = 
			(FavouriteService) myContainer
			.getComponentInstanceOfType(FavouriteService.class);
	}

	public FavouriteService getFavouriteService() {
		return favouriteService_;
	}
	
	public String getTimeLineSortByFavourite() { return timeLineSortByFavourite; }
	public void setTimeLineSortByFavourite(String timeLineSortByFavourite) {
		this.timeLineSortByFavourite = timeLineSortByFavourite;
	}

	public String getTimeLineSortByName() {	return timeLineSortByName; }
	public void setTimeLineSortByName(String timeLineSortByName) {
		this.timeLineSortByName = timeLineSortByName;
	}

	public String getTimeLineSortByDate() {	return timeLineSortByDate; }
	public void setTimeLineSortByDate(String timeLineSortByDate) {
		this.timeLineSortByDate = timeLineSortByDate;
	}
  
	public UIDocumentInfo() throws Exception {
    pageIterator_ = addChild(UIPageIterator.class, null,CONTENT_PAGE_ITERATOR_ID);    
  }

  public List<Node> getTodayNodes() {	return todayNodes; }
	public void setTodayNodes(List<Node> todayNodes) {
		this.todayNodes = todayNodes;
	}

	public List<Node> getYesterdayNodes() { return yesterdayNodes; }
	public void setYesterdayNodes(List<Node> yesterdayNodes) {
		this.yesterdayNodes = yesterdayNodes;
	}

	public List<Node> getEarlierThisWeekNodes() {	return earlierThisWeekNodes; }
	public void setEarlierThisWeekNodes(List<Node> earlierThisWeekNodes) {
		this.earlierThisWeekNodes = earlierThisWeekNodes;
	}

	public List<Node> getEarlierThisMonthNodes() { return earlierThisMonthNodes; }
	public void setEarlierThisMonthNodes(List<Node> earlierThisMonthNodes) {
		this.earlierThisMonthNodes = earlierThisMonthNodes;
	}

	public List<Node> getEarlierThisYearNodes() {	return earlierThisYearNodes; }
	public void setEarlierThisYearNodes(List<Node> earlierThisYearNodes) {
		this.earlierThisYearNodes = earlierThisYearNodes;
	}
  
  public void updateNodeLists() throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    TimelineService timelineService = (TimelineService)container.getComponentInstanceOfType(TimelineService.class);
    
    UIJCRExplorer uiExplorer = this.getAncestorOfType(UIJCRExplorer.class);
    SessionProvider sessionProvider = uiExplorer.getSessionProvider();
    Session session = uiExplorer.getSession();
    String repository = this.getRepository();
    String workspace = this.getWorkspaceName();
    String userName = session.getUserID();
    
    boolean byUser = uiExplorer.getPreference().isShowItemsByUserInTimelineView();
  	todayNodes = timelineService.
  				getDocumentsOfToday(repository,	workspace, sessionProvider,	userName, byUser);
  	yesterdayNodes = timelineService.
  				getDocumentsOfYesterday(repository, workspace, sessionProvider, userName, byUser);
  	earlierThisWeekNodes = timelineService.
  				getDocumentsOfEarlierThisWeek(repository, workspace, sessionProvider, userName, byUser);
  	earlierThisMonthNodes = timelineService.
  				getDocumentsOfEarlierThisMonth(repository, workspace, sessionProvider, userName, byUser);
  	earlierThisYearNodes = timelineService.
  				getDocumentsOfEarlierThisYear(repository, workspace, sessionProvider, userName, byUser);
  	
		Collections.sort(todayNodes, new SearchComparator());
		Collections.sort(yesterdayNodes, new SearchComparator());
		Collections.sort(earlierThisWeekNodes, new SearchComparator());
		Collections.sort(earlierThisMonthNodes, new SearchComparator());
		Collections.sort(earlierThisYearNodes, new SearchComparator());  	
  }

  public UIPageIterator getContentPageIterator() {return pageIterator_ ; }

  public UIComponent getUIComponent(String mimeType) throws Exception {
    UIExtensionManager manager = getApplicationComponent(UIExtensionManager.class);
    List<UIExtension> extensions = manager.getUIExtensions(Utils.FILE_VIEWER_EXTENSION_TYPE);
    Map<String, Object> context = new HashMap<String, Object>();
    context.put(Utils.MIME_TYPE, mimeType);
    for (UIExtension extension : extensions) {
      UIComponent uiComponent = manager.addUIExtension(extension, context, this);
      if(uiComponent != null) return uiComponent;
    }
    return null;
  }
  
  public String getTemplate() {    
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    if(uiExplorer.getPreference().isJcrEnable()) 
      return uiExplorer.getDocumentInfoTemplate();
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    try {
      Node node = uiExplorer.getCurrentNode();
      String nodeTypeName = node.getPrimaryNodeType().getName();
      currentRepository_ = uiExplorer.getRepositoryName();
      currentWorkspaceName_ = node.getSession().getWorkspace().getName();;
      selectedLang_ = uiExplorer.getLanguage();
      isDocumentTemplate_ = templateService.getDocumentTemplates(currentRepository_).contains(nodeTypeName);
      String template = templateService.getTemplatePath(node,false) ;
      templateService.removeCacheTemplate(uiExplorer.getJCRTemplateResourceResolver().createResourceId(template));
      if(template != null) return template ;
    } catch(AccessDeniedException ace) {
      try {
        uiExplorer.setSelectRootNode() ;
        Object[] args = { uiExplorer.getCurrentNode().getName() } ;
        throw new MessageException(new ApplicationMessage("UIDocumentInfo.msg.access-denied", args, 
            ApplicationMessage.WARNING)) ;
      } catch(Exception exc) {
      }
    } catch(Exception e) {    
    }
    return uiExplorer.getDocumentInfoTemplate(); 
  }

  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    if (isDocumentTemplate_) {
      DMSConfiguration dmsConfiguration = getApplicationComponent(DMSConfiguration.class);
      String workspace = dmsConfiguration.getConfig(currentRepository_).getSystemWorkspace();
      JCRResourceResolver resourceResolver = new JCRResourceResolver(currentRepository_, workspace, Utils.EXO_TEMPLATEFILE, selectedLang_);
      return resourceResolver;
    }
    return getAncestorOfType(UIJCRExplorer.class).getJCRTemplateResourceResolver();
  }

  public UIRightClickPopupMenu getContextMenu() {
    return getAncestorOfType(UIWorkingArea.class).getChild(UIRightClickPopupMenu.class) ;
  }

  public Node getNodeByUUID(String uuid) throws Exception{
    ManageableRepository manageRepo = getApplicationComponent(RepositoryService.class).getRepository(getRepository()) ;
    String[] workspaces = manageRepo.getWorkspaceNames() ;
    for(String ws : workspaces) {
      try{
        return SessionProviderFactory.createSystemProvider().getSession(ws, manageRepo).getNodeByUUID(uuid) ;
      } catch(Exception e) {
        continue;
      }      
    }
    return null;
  }

  public String getCapacityOfFile(Node file) throws Exception {
    Node contentNode = file.getNode(Utils.JCR_CONTENT);
    long size = contentNode.getProperty(Utils.JCR_DATA).getLength() ;    
    long capacity = size/1024 ;
    String strCapacity = Long.toString(capacity) ;
    if(strCapacity.indexOf(".") > -1) return strCapacity.substring(0, strCapacity.lastIndexOf(".")) ;
    return strCapacity ;
  }
  
  public List<String> getMultiValues(Node node, String name) throws Exception {
    return getAncestorOfType(UIJCRExplorer.class).getMultiValues(node, name) ;
  }

  public boolean isSystemWorkspace() throws Exception {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    ManageableRepository manaRepoService = 
      getApplicationComponent(RepositoryService.class).getRepository(uiExplorer.getRepositoryName()) ;
    String systemWsName = manaRepoService.getConfiguration().getSystemWorkspaceName() ;
    if(systemWsName.equals(uiExplorer.getCurrentWorkspace())) return true ;
    return false ;
  }
  
  public boolean isSupportedThumbnailImage(Node node) throws Exception {
    if(node.getPrimaryNodeType().getName().equals(Utils.NT_FILE)) {
      Node contentNode = node.getNode(Utils.JCR_CONTENT);
      ThumbnailService thumbnailService = getApplicationComponent(ThumbnailService.class);
      for(ComponentPlugin plugin : thumbnailService.getComponentPlugins()) {
        if(plugin instanceof ThumbnailPlugin) {
          ThumbnailPlugin thumbnailPlugin = (ThumbnailPlugin) plugin;
          if(thumbnailPlugin.getMimeTypes().contains(
              contentNode.getProperty(Utils.JCR_MIMETYPE).getString())) {
            return true;
          }
        }
      }
      return false;
    }
    return false;
  }
  
  public boolean isImageType(Node node) throws Exception {
    if(node.getPrimaryNodeType().getName().equals(Utils.NT_FILE)) {
      Node contentNode = node.getNode(Utils.JCR_CONTENT);
      if(contentNode.getProperty(Utils.JCR_MIMETYPE).getString().startsWith("image")) return true;
    }
    return false;
  }
  
  public String getThumbnailImage(Node node) throws Exception {
    node = node instanceof NodeLinkAware ? ((NodeLinkAware) node).getTargetNode().getRealNode() : node;
    return Utils.getThumbnailImage(node, ThumbnailService.MEDIUM_SIZE);
  }
  
  public Node getThumbnailNode(Node node) throws Exception {
    ThumbnailService thumbnailService = getApplicationComponent(ThumbnailService.class);
    node = node instanceof NodeLinkAware ? ((NodeLinkAware) node).getTargetNode().getRealNode() : node;
    return thumbnailService.getThumbnailNode(node);
  }

  public String getDownloadLink(Node node) throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;    
    Node jcrContentNode = node.getNode(Utils.JCR_CONTENT) ;
    InputStream input = jcrContentNode.getProperty(Utils.JCR_DATA).getStream() ;
    String mimeType = jcrContentNode.getProperty(Utils.JCR_MIMETYPE).getString() ;
    InputStreamDownloadResource dresource = new InputStreamDownloadResource(input, mimeType) ;
    DMSMimeTypeResolver mimeTypeSolver = DMSMimeTypeResolver.getInstance();
    String ext = mimeTypeSolver.getExtension(mimeType) ;
    String fileName = node.getName() ;    
    if(fileName.lastIndexOf("."+ext)<0){
      fileName = fileName + "." +ext ;
    } 
    dresource.setDownloadName(fileName) ;
    return dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
  }

  public String getImage(Node node) throws Exception {
    return getImage(node, Utils.EXO_IMAGE);
  }

  public String getImage(Node node, String nodeTypeName) throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    InputStreamDownloadResource dresource ;
    Node imageNode = node.getNode(nodeTypeName) ;    
    InputStream input = imageNode.getProperty(Utils.JCR_DATA).getStream() ;
    dresource = new InputStreamDownloadResource(input, "image") ;
    dresource.setDownloadName(node.getName()) ;
    return dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
  }

  public String getWebDAVServerPrefix() throws Exception {    
    PortletRequestContext portletRequestContext = PortletRequestContext.getCurrentInstance() ;
    String prefixWebDAV = portletRequestContext.getRequest().getScheme() + "://" + 
    portletRequestContext.getRequest().getServerName() + ":" +
    String.format("%s",portletRequestContext.getRequest().getServerPort()) ;
    return prefixWebDAV ;
  }

  public Node getViewNode(String nodeType) throws Exception {
    return getAncestorOfType(UIJCRExplorer.class).getCurrentNode().getNode(nodeType) ;
  }

  public Node getNodeByPath(String nodePath, String workspace) throws Exception {
    ManageableRepository manageRepo = getApplicationComponent(RepositoryService.class).getRepository(getRepository()) ;
    Session session = SessionProviderFactory.createSystemProvider().getSession(workspace, manageRepo) ;
    return getAncestorOfType(UIJCRExplorer.class).getNodeByPath(nodePath, session) ;
  }

  public String getActionsList(Node node) throws Exception {
    return getAncestorOfType(UIWorkingArea.class).getActionsExtensionList(node) ;
  }

  public List<Node> getCustomActions(Node node) throws Exception {
    return getAncestorOfType(UIWorkingArea.class).getCustomActions(node) ;
  }

  public boolean isPreferenceNode(Node node) throws Exception {
    return getAncestorOfType(UIWorkingArea.class).isPreferenceNode(node) ;
  }

  public boolean isReadAuthorized(ExtendedNode node) throws Exception {
    return getAncestorOfType(UIJCRExplorer.class).isReadAuthorized(node) ;
  }

  @SuppressWarnings("unchecked")
  public Object getComponentInstanceOfType(String className) {
    Object service = null;
    try {
      ClassLoader loader =  Thread.currentThread().getContextClassLoader();
      Class clazz = loader.loadClass(className);
      service = getApplicationComponent(clazz);
    } catch (ClassNotFoundException ex) {
      ex.printStackTrace();
    } 
    return service;
  }

  public String getNodeOwner(Node node) throws RepositoryException {
    if(node.hasProperty(Utils.EXO_OWNER)) {
      return node.getProperty(Utils.EXO_OWNER).getString();
    }
    return SystemIdentity.ANONIM ;
  }

  public Date getDateCreated(Node node) throws Exception{
    if(node.hasProperty(Utils.EXO_CREATED_DATE)) {
      return node.getProperty(Utils.EXO_CREATED_DATE).getDate().getTime();
    }
    return new GregorianCalendar().getTime();
  }

  public Date getDateModified(Node node) throws Exception {
    if(node.hasProperty(Utils.EXO_MODIFIED_DATE)) {
      return node.getProperty(Utils.EXO_MODIFIED_DATE).getDate().getTime();
    }
    return new GregorianCalendar().getTime();
  }

  public List<Node> getRelations() throws Exception {
    List<Node> relations = new ArrayList<Node>() ;
    if (currentNode_.hasProperty(Utils.EXO_RELATION)) {
      Value[] vals = currentNode_.getProperty(Utils.EXO_RELATION).getValues();
      for (int i = 0; i < vals.length; i++) {
        String uuid = vals[i].getString();
        Node node = getNodeByUUID(uuid);
        if (node != null)
          relations.add(node);
      }
    }
    return relations;
  }

  public List<Node> getAttachments() throws Exception {
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    List<Node> attachments = new ArrayList<Node>() ;
    NodeIterator childrenIterator = currentNode_.getNodes();;
    while (childrenIterator.hasNext()) {
      Node childNode = childrenIterator.nextNode();
      String nodeType = childNode.getPrimaryNodeType().getName();
      List<String> listCanCreateNodeType = 
        Utils.getListAllowedFileType(currentNode_, getRepository(), templateService) ; 
      if(listCanCreateNodeType.contains(nodeType)) attachments.add(childNode);
    }
    return attachments;
  }

  public boolean isNodeTypeSupported(String nodeTypeName) {
    try {      
      TemplateService templateService = getApplicationComponent(TemplateService.class);
      return templateService.isManagedNodeType(nodeTypeName, getRepository());
    } catch (Exception e) {
      return false;
    }
  }

  public String getNodeType() throws Exception { return null; }

  public List<String> getSupportedLocalise() throws Exception {
    MultiLanguageService multiLanguageService = getApplicationComponent(MultiLanguageService.class) ;
    return multiLanguageService.getSupportedLanguages(currentNode_) ;
  }

  public String getTemplatePath() throws Exception { return null; }

  public boolean isNodeTypeSupported() { return false; }

  public String getVersionName(Node node) throws Exception {
    return node.getBaseVersion().getName() ;
  }

  /**
   * Method which returns true if the node has a history.
   * @author CPop
   */  
  public boolean hasAuditHistory(Node node) throws Exception{
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    AuditService auServ = (AuditService)container.getComponentInstanceOfType(AuditService.class);
    node = node instanceof NodeLinkAware ? ((NodeLinkAware) node).getTargetNode().getRealNode() : node;
    return auServ.hasHistory(node);
  }

  /**
   * Method which returns the number of histories.
   * @author CPop
   */ 
  public int getNumAuditHistory(Node node) throws Exception{
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    AuditService auServ = (AuditService)container.getComponentInstanceOfType(AuditService.class);
    node = node instanceof NodeLinkAware ? ((NodeLinkAware) node).getTargetNode().getRealNode() : node;
    if (auServ.hasHistory(node)) {
      AuditHistory auHistory = auServ.getHistory(node);
      return (auHistory.getAuditRecords()).size();
    }
    return 0;
  }

  public void setNode(Node node) { currentNode_ = node ; }

  public boolean isRssLink() { return false ; }
  public String getRssLink() { return null ; }

  public String getPortalName() {
    ExoContainer container = ExoContainerContext.getCurrentContainer() ;
    PortalContainerInfo containerInfo = 
      (PortalContainerInfo)container.getComponentInstanceOfType(PortalContainerInfo.class) ;      
    return containerInfo.getContainerName() ; 
  }

  public String getRepository() throws Exception {
    return getAncestorOfType(UIJCRExplorer.class).getRepositoryName();
  }

  public String getWorkspaceName() throws Exception {
    if(currentNode_ == null) {
      return getOriginalNode().getSession().getWorkspace().getName();
    }
    return currentNode_.getSession().getWorkspace().getName();
  }

  public Node getNode() throws Exception { 
    currentNode_ = getAncestorOfType(UIJCRExplorer.class).getCurrentNode() ;
    if(currentNode_.hasProperty(Utils.EXO_LANGUAGE)) {
      String defaultLang = currentNode_.getProperty(Utils.EXO_LANGUAGE).getString() ;
      if(getLanguage() == null) setLanguage(defaultLang) ;
      if(!getLanguage().equals(defaultLang)) {
        Node curNode = currentNode_.getNode(Utils.LANGUAGES + Utils.SLASH + getLanguage()) ;
        return curNode ;
      }
    }    
    return currentNode_; 
  }

  public Node getOriginalNode() throws Exception {return getAncestorOfType(UIJCRExplorer.class).getCurrentNode() ;}

  public String getIcons(Node node, String size) throws Exception {
    return Utils.getNodeTypeIcon(node, size) ;
  }

  public List<Node> getComments() throws Exception {
    return getApplicationComponent(CommentsService.class).getComments(currentNode_, getLanguage()) ;
  }
 
  public String getViewTemplate(String nodeTypeName, String templateName) throws Exception {
    TemplateService tempServ = getApplicationComponent(TemplateService.class) ;
    return tempServ.getTemplatePath(false, nodeTypeName, templateName, getRepository()) ;
  }

  public String getLanguage() {
    return getAncestorOfType(UIJCRExplorer.class).getLanguage() ;
  }

  public void setLanguage(String language) { 
    getAncestorOfType(UIJCRExplorer.class).setLanguage(language) ;
  }
  
  public boolean isCanPaste() {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    if(uiExplorer.getAllClipBoard().size() > 0) return true;
    return false;
  }

  public void updatePageListData() throws Exception {    
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    Preference pref = uiExplorer.getPreference();
    String currentPath = uiExplorer.getCurrentPath();
    List<Node> childrenList = new ArrayList<Node>();
    
    // Following code trunk is original codes.
    /*if(!uiExplorer.isViewTag()) {
      childrenList = uiExplorer.getChildrenList(currentPath, pref.isShowPreferenceDocuments());    
    } else {
      childrenList = uiExplorer.getDocumentByTag() ;
    }*/
    
    // This pieces of code to show document types are updated by lampt.
    // In case of file explorer display.
    if(!uiExplorer.isViewTag() && !uiExplorer.isViewDocument()) {      
      childrenList = uiExplorer.getChildrenList(currentPath, pref.isShowPreferenceDocuments());
    } else if (uiExplorer.isViewTag()) {               
      childrenList = uiExplorer.getDocumentByTag();       
    } else if (uiExplorer.isViewDocument()) {              
      childrenList = uiExplorer.getDocumentBySupportedType();
    }
    // End updated codes.
    
    int nodesPerPage = pref.getNodesPerPage();    
    PageList pageList = new ObjectPageList(childrenList,nodesPerPage) ;
    pageIterator_.setPageList(pageList);        
  }

  @SuppressWarnings("unchecked")
  public List<Node> getChildrenList() throws Exception {
    return pageIterator_.getCurrentPageData();    
  }

  public String getTypeSort() { return typeSort_; }
  
  public void setTypeSort(String typeSort) {
    typeSort_ = typeSort;
  }
  
  public String getSortOrder() { return sortOrder_; }
  
  public void setSortOrder(String sortOrder) {
    sortOrder_ = sortOrder;
  }
  
  public String encodeHTML(String text) { return Utils.encodeHTML(text) ; }

  
  public UIComponent getCommentComponent() {
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
    UIActionBar uiActionBar = uiExplorer.findFirstComponentOfType(UIActionBar.class);
    UIComponent uicomponent = uiActionBar.getUIAction(COMMENT_COMPONENT);
    return (uicomponent != null ? uicomponent : this);
  }
  
  private Node getFileLangNode(Node currentNode) throws Exception {
    if(currentNode.getNodes().getSize() > 0) {
      NodeIterator nodeIter = currentNode.getNodes() ;
      while(nodeIter.hasNext()) {
        Node ntFile = nodeIter.nextNode() ;
        if(ntFile.getPrimaryNodeType().getName().equals(Utils.NT_FILE)) {
          return ntFile ;
        }
      }
      return currentNode ;
    }
    return currentNode ;
  }
  
  public boolean isEnableThumbnail() {
    ThumbnailService thumbnailService = getApplicationComponent(ThumbnailService.class);
    return thumbnailService.isEnableThumbnail();
  }
  
  public String getFlowImage(Node node) throws Exception {
    node = node instanceof NodeLinkAware ? ((NodeLinkAware) node).getTargetNode().getRealNode() : node;
    return Utils.getThumbnailImage(node, ThumbnailService.BIG_SIZE);
  }
  
  public String getThumbnailSize(Node node) throws Exception {
    node = node instanceof NodeLinkAware ? ((NodeLinkAware) node).getTargetNode().getRealNode() : node;
    String imageSize = null;
    if(node.hasProperty(ThumbnailService.BIG_SIZE)) {
      Image image = ImageIO.read(node.getProperty(ThumbnailService.BIG_SIZE).getStream());
      imageSize = 
        Integer.toString(image.getWidth(null)) + "x" + Integer.toString(image.getHeight(null));
    }
    return imageSize;
  }
  
  public DateFormat getSimpleDateFormat() {
    Locale locale = Util.getUIPortal().getAncestorOfType(UIPortalApplication.class).getLocale();
    return SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT, locale);
  }
  
  public boolean isSymLink(Node node) throws RepositoryException {
    LinkManager linkManager = getApplicationComponent(LinkManager.class);
    return linkManager.isLink(node);
  }
  
  public UIComponent getRemoveAttach() throws Exception {
    removeChild(RemoveAttachmentComponent.class);
    UIComponent uicomponent = addChild(RemoveAttachmentComponent.class, null, "DocumentInfoRemoveAttach");
    ((AbstractActionComponent)uicomponent).setLstComponentupdate(Arrays.asList(new Class[] {UIDocumentContainer.class}));
    return uicomponent;
  }

  public UIComponent getRemoveComment() throws Exception {
    removeChild(RemoveCommentComponent.class);
    UIComponent uicomponent = addChild(RemoveCommentComponent.class, null, "DocumentInfoRemoveComment");
    ((AbstractActionComponent)uicomponent).setLstComponentupdate(Arrays.asList(new Class[] {UIDocumentContainer.class, UIWorkingArea.class}));
    return uicomponent;
  }
  
	public boolean isFavouriter(Node data) throws Exception {
		if (!data.isNodeType(Utils.EXO_FAVOURITE))
			return false;
    Value[] favouriters = data.getProperty("exo:favouriter").getValues();
	  String userID = data.getSession().getUserID();
	  for(Value value : favouriters) {
	  	if (userID.equals(value.getString())) {
	  		return true;
	  	}
	  }		
	  return false;
	}
  
  static public class ViewNodeActionListener extends EventListener<UIDocumentInfo> {
    public void execute(Event<UIDocumentInfo> event) throws Exception {      
      UIDocumentInfo uicomp = event.getSource() ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class);      
      String uri = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String workspaceName = event.getRequestContext().getRequestParameter("workspaceName") ;      
      uiExplorer.setSelectNode(workspaceName, uri) ;
      uiExplorer.updateAjax(event) ;           
      event.broadcast();      
    }
  }

  static public class ChangeNodeActionListener extends EventListener<UIDocumentInfo> {
    public void execute(Event<UIDocumentInfo> event) throws Exception {     
      UIDocumentInfo uicomp =  event.getSource();
      NodeFinder nodeFinder = uicomp.getApplicationComponent(NodeFinder.class);
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class); 
      String uri = event.getRequestContext().getRequestParameter(OBJECTID);
      String workspaceName = event.getRequestContext().getRequestParameter("workspaceName");
      UIApplication uiApp = uicomp.getAncestorOfType(UIApplication.class);
      try {
        // Manage ../ and ./
        uri = LinkUtils.evaluatePath(uri);
        // Just in order to check if the node exists
        nodeFinder.getItem(uiExplorer.getRepositoryName(), workspaceName, uri);
        uiExplorer.setSelectNode(workspaceName, uri); 
        uiExplorer.updateAjax(event) ;
      } catch(ItemNotFoundException nu) {
        uiApp.addMessage(new ApplicationMessage("UIDocumentInfo.msg.null-exception", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } catch(PathNotFoundException pa) {
        uiApp.addMessage(new ApplicationMessage("UIDocumentInfo.msg.null-exception", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;          
      } catch(AccessDeniedException ace) {
        uiApp.addMessage(new ApplicationMessage("UIDocumentInfo.msg.access-denied", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;          
      } catch(Exception e) {    
        JCRExceptionManager.process(uiApp, e);
        return ;
      }
    }
  }
  
  static public class SortActionListener extends EventListener<UIDocumentInfo> {
    public void execute(Event<UIDocumentInfo> event) throws Exception {
      UIDocumentInfo uicomp = event.getSource() ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ;
      String sortParam = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String[] array = sortParam.split(";");
      String order = "";
      if (array[0].trim().equals(Preference.ASCENDING_ORDER)) order = Preference.BLUE_DOWN_ARROW;
      else order = Preference.BLUE_UP_ARROW;
      uicomp.setSortOrder(order);
      uicomp.setTypeSort(array[1]);
      
      Preference pref = uiExplorer.getPreference();
      if (array.length == 2) {
        pref.setSortType(array[1].trim());
        pref.setOrder(array[0].trim()); 
      } else {
        return ;
      }       
      uiExplorer.updateAjax(event) ;
    }
  }

  static public class ChangeLanguageActionListener extends EventListener<UIDocumentInfo> {
    public void execute(Event<UIDocumentInfo> event) throws Exception {
      UIDocumentInfo uiDocumentInfo = event.getSource() ;
      UIJCRExplorer uiExplorer = uiDocumentInfo.getAncestorOfType(UIJCRExplorer.class) ;
      String selectedLanguage = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiExplorer.setLanguage(selectedLanguage) ;
      uiExplorer.updateAjax(event) ;
    }   
  }

  static public class VoteActionListener extends EventListener<UIDocumentInfo> {
    public void execute(Event<UIDocumentInfo> event) throws Exception {
      UIDocumentInfo uiComp = event.getSource() ;
      String userName = Util.getPortalRequestContext().getRemoteUser() ;
      double objId = Double.parseDouble(event.getRequestContext().getRequestParameter(OBJECTID)) ;
      VotingService votingService = uiComp.getApplicationComponent(VotingService.class) ;
      votingService.vote(uiComp.currentNode_, objId, userName, uiComp.getLanguage()) ;
    }
  }

  static public class DownloadActionListener extends EventListener<UIDocumentInfo> {
    public void execute(Event<UIDocumentInfo> event) throws Exception {
      UIDocumentInfo uiComp = event.getSource() ;
      String downloadLink = uiComp.getDownloadLink(uiComp.getFileLangNode(uiComp.getNode()));
      event.getRequestContext().getJavascriptManager().addCustomizedOnLoadScript("ajaxRedirect('" + downloadLink + "');");
    }
  }
  
  static public class StarClickActionListener extends EventListener<UIDocumentInfo> {
  	public void execute(Event<UIDocumentInfo> event) throws Exception {
      //final String virtualNodePath = nodePath;
      String srcPath = event.getRequestContext().getRequestParameter(OBJECTID);
      UIDocumentInfo uiDocumentInfo = event.getSource();
      UIJCRExplorer uiExplorer = uiDocumentInfo.getAncestorOfType(UIJCRExplorer.class);
	    UIApplication uiApp = uiDocumentInfo.getAncestorOfType(UIApplication.class);
	    
	    Matcher matcher = UIWorkingArea.FILE_EXPLORER_URL_SYNTAX.matcher(srcPath);
	    String wsName = null;
	    Node node = null;
	    if (matcher.find()) {
	      wsName = matcher.group(1);
	      srcPath = matcher.group(2);
	    } else {
	      throw new IllegalArgumentException("The ObjectId is invalid '"+ srcPath + "'");
	    }
	
	    Session session = uiExplorer.getSessionByWorkspace(wsName);
	    try {
	      // Use the method getNodeByPath because it is link aware
	      node = uiExplorer.getNodeByPath(srcPath, session, false);
	      // Reset the path to manage the links that potentially create virtual path
	      srcPath = node.getPath();
	      // Reset the session to manage the links that potentially change of workspace
	      session = node.getSession();
	      // Reset the workspace name to manage the links that potentially change of workspace 
	      wsName = session.getWorkspace().getName();
	    } catch(PathNotFoundException path) {
	      uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.path-not-found-exception", 
	          null,ApplicationMessage.WARNING));
	      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
	      return;
	    }
	
	    try {
	        uiExplorer.addLockToken(node);
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp, e);
        return;
      }
      try {
	    	if (favouriteService_.isFavouriter(node.getSession().getUserID(), node)) {
	    		if (PermissionUtil.canRemoveNode(node)) {
	    			favouriteService_.removeFavourite(node, node.getSession().getUserID());
	    		}
	    		else {
	    			throw new AccessDeniedException();
	    		}
	    	} else {
	    		if (PermissionUtil.canSetProperty(node)) {		    			
	    			favouriteService_.addFavourite(node, node.getSession().getUserID());
	    		}
	    		else {
	    			throw new AccessDeniedException();
	    		}
	    	}
		    //uiStar.changeFavourite();
	        event.getRequestContext().addUIComponentToUpdateByAjax(uiDocumentInfo);			    
	    } catch (AccessDeniedException e) {
	    	LOG.error("Access denied! No permission for modifying property " +
			  Utils.EXO_FAVOURITER + " of node: " + node.getPath());
    		uiApp.addMessage(new ApplicationMessage("UIShowAllFavouriteResult.msg.accessDenied", null, ApplicationMessage.WARNING));
    		event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      } catch (VersionException ve) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.remove-verion-exception", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        uiExplorer.updateAjax(event);
        return;
      } catch (ReferentialIntegrityException ref) {
        session.refresh(false);
        uiExplorer.refreshExplorer();
        uiApp
            .addMessage(new ApplicationMessage(
                "UIPopupMenu.msg.remove-referentialIntegrityException", null,
                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        uiExplorer.updateAjax(event);
        return;
      } catch (ConstraintViolationException cons) {
        session.refresh(false);
        uiExplorer.refreshExplorer();
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.constraintviolation-exception",
            null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        uiExplorer.updateAjax(event);
        return;
      } catch (LockException lockException) {
        uiApp.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked-other-person", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        uiExplorer.updateAjax(event);
        return;
      } catch (Exception e) {
        LOG.error("an unexpected error occurs while removing the node", e);
        JCRExceptionManager.process(uiApp, e);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
	  }
  }

  static public class SortTimelineASCActionListener extends EventListener<UIDocumentInfo> {
		public void execute(Event<UIDocumentInfo> event) throws Exception {
			UIDocumentInfo uiDocumentInfo = event.getSource();
			String objectID = event.getRequestContext().getRequestParameter(OBJECTID);
			if (objectID.equals("favourite")) {
				uiDocumentInfo.timeLineSortByFavourite = Preference.BLUE_DOWN_ARROW;
				uiDocumentInfo.timeLineSortByName = "";
				uiDocumentInfo.timeLineSortByDate = "";
			} else if (objectID.equals("name")) {
				uiDocumentInfo.timeLineSortByFavourite = "";
				uiDocumentInfo.timeLineSortByName = Preference.BLUE_DOWN_ARROW;
				uiDocumentInfo.timeLineSortByDate = "";
			} else if (objectID.equals("dateTime")) {
				uiDocumentInfo.timeLineSortByFavourite = "";
				uiDocumentInfo.timeLineSortByName = "";
				uiDocumentInfo.timeLineSortByDate = Preference.BLUE_DOWN_ARROW;
			}
//			Collections.sort(uiDocumentInfo.todayNodes, new SearchComparator());
//			Collections.sort(uiDocumentInfo.yesterdayNodes, new SearchComparator());
//			Collections.sort(uiDocumentInfo.earlierThisWeekNodes, new SearchComparator());
//			Collections.sort(uiDocumentInfo.earlierThisMonthNodes, new SearchComparator());
//			Collections.sort(uiDocumentInfo.earlierThisYearNodes, new SearchComparator());
			event.getRequestContext().addUIComponentToUpdateByAjax(uiDocumentInfo);
		}
  }
  
  static public class SortTimelineDESCActionListener extends EventListener<UIDocumentInfo> {
		public void execute(Event<UIDocumentInfo> event) throws Exception {
			UIDocumentInfo uiDocumentInfo = event.getSource();
			String objectID = event.getRequestContext().getRequestParameter(OBJECTID);
			if (objectID.equals("favourite")) {
				uiDocumentInfo.timeLineSortByFavourite = Preference.BLUE_UP_ARROW;
				uiDocumentInfo.timeLineSortByName = "";
				uiDocumentInfo.timeLineSortByDate = "";
			} else if (objectID.equals("name")) {
				uiDocumentInfo.timeLineSortByFavourite = "";
				uiDocumentInfo.timeLineSortByName = Preference.BLUE_UP_ARROW;
				uiDocumentInfo.timeLineSortByDate = "";
			} else if (objectID.equals("dateTime")) {
				uiDocumentInfo.timeLineSortByFavourite = "";
				uiDocumentInfo.timeLineSortByName = "";
				uiDocumentInfo.timeLineSortByDate = Preference.BLUE_UP_ARROW;
			}
//			Collections.sort(uiDocumentInfo.todayNodes, new SearchComparator());
//			Collections.sort(uiDocumentInfo.yesterdayNodes, new SearchComparator());
//			Collections.sort(uiDocumentInfo.earlierThisWeekNodes, new SearchComparator());
//			Collections.sort(uiDocumentInfo.earlierThisMonthNodes, new SearchComparator());
//			Collections.sort(uiDocumentInfo.earlierThisYearNodes, new SearchComparator());
			event.getRequestContext().addUIComponentToUpdateByAjax(uiDocumentInfo);
		}
  }  

  static public class ShowPageActionListener extends EventListener<UIPageIterator> {
    public void execute(Event<UIPageIterator> event) throws Exception {      
      UIPageIterator uiPageIterator = event.getSource() ;
      UIJCRExplorer explorer = uiPageIterator.getAncestorOfType(UIJCRExplorer.class);      
      UITreeExplorer treeExplorer = explorer.findFirstComponentOfType(UITreeExplorer.class);
      if(treeExplorer == null || !treeExplorer.isRendered()) return;
      String componentId = explorer.getCurrentNode().getPath();
      UITreeNodePageIterator extendedPageIterator = treeExplorer.getUIPageIterator(componentId);
      if(extendedPageIterator == null) return;      
      int page = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID)) ;      
      extendedPageIterator.setCurrentPage(page);
      event.getRequestContext().addUIComponentToUpdateByAjax(treeExplorer);      
    }
  } 
  
  static private class SearchComparator implements Comparator<Node> {
  	public int compare(Node node1, Node node2) {
  		try {
  			if (timeLineSortByFavourite.length() != 0) {
  				int factor = (timeLineSortByFavourite.equals(Preference.BLUE_DOWN_ARROW)) ? 1 : -1;
  				if (node1.isNodeType(Utils.EXO_FAVOURITE)) return -1 * factor;
  				else if (node2.isNodeType(Utils.EXO_FAVOURITE)) return 1 * factor;
  				else return 0;
  				
  			} else if (timeLineSortByDate.length() != 0) {
					int factor = timeLineSortByDate.equals(Preference.BLUE_DOWN_ARROW) ? 1 : -1;
					Calendar c1 = node1.getProperty(Utils.EXO_MODIFIED_DATE).getValue().getDate();
					Calendar c2 = node2.getProperty(Utils.EXO_MODIFIED_DATE).getValue().getDate();
					return factor * c1.compareTo(c2);
					
  			} else if (timeLineSortByName.length() != 0) {
  				int factor = timeLineSortByName.equals(Preference.BLUE_DOWN_ARROW) ? 1 : -1;
  				String s1 = node1.getName();
  				String s2 = node2.getName();
  				return factor * s1.compareTo(s2);
  			}
  		} catch (Exception e) {
  			LOG.error("Cannot compare nodes", e);
  		}
  		return 0;
  	}
  }
}
