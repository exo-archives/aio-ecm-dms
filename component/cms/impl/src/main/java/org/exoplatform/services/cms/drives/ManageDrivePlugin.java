package org.exoplatform.services.cms.drives;

import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsConfigurationService;
import org.exoplatform.services.jcr.RepositoryService;

public class ManageDrivePlugin extends BaseComponentPlugin {

  private static String WORKSPACE = "exo:workspace".intern() ;
  private static String PERMISSIONS = "exo:permissions".intern() ;
  private static String VIEWS = "exo:views".intern() ;
  private static String ICON = "exo:icon".intern() ;
  private static String PATH = "exo:path".intern() ;
  private static String VIEW_REFERENCES = "exo:viewPreferences".intern() ;
  private static String VIEW_NON_DOCUMENT = "exo:viewNonDocument".intern() ;
  private static String VIEW_SIDEBAR = "exo:viewSideBar".intern() ;
  private static String ALL_CREATE_FOLDER = "exo:allowCreateFolder".intern() ;

  private RepositoryService repositoryService_;
  private CmsConfigurationService cmsConfigService_;
  private InitParams params_ ; 

  //private Session session_ ;  
  public ManageDrivePlugin(RepositoryService repositoryService, 
      InitParams params, CmsConfigurationService cmsConfigService) throws Exception {
    repositoryService_ = repositoryService;
    cmsConfigService_ = cmsConfigService ;
    params_ = params ;
    init() ;
  }

  private void init() throws Exception {
   Iterator<ObjectParameter> it = params_.getObjectParamIterator() ;
   Session session = null ;
    while(it.hasNext()){
      DriveData data = (DriveData)it.next().getObject() ;
      session = repositoryService_.getRepository(data.getRepository())
      .getSystemSession(cmsConfigService_.getWorkspace()) ;
      String drivesPath = cmsConfigService_.getJcrPath(BasePath.EXO_DRIVES_PATH);
      Node driveHome = (Node)session.getItem(drivesPath) ;
      
      if(!driveHome.hasNode(data.getName())){
        Node driveNode = driveHome.addNode(data.getName(), "exo:drive");
        driveNode.setProperty(WORKSPACE, data.getWorkspace()) ;
        driveNode.setProperty(PERMISSIONS, data.getPermissions()) ;
        driveNode.setProperty(PATH, data.getHomePath()) ;
        driveNode.setProperty(VIEWS, data.getViews()) ;
        driveNode.setProperty(ICON, data.getIcon()) ;
        driveNode.setProperty(VIEW_REFERENCES, Boolean.toString(data.getViewPreferences())) ;
        driveNode.setProperty(VIEW_NON_DOCUMENT, Boolean.toString(data.getViewNonDocument())) ;
        driveNode.setProperty(VIEW_SIDEBAR, Boolean.toString(data.getViewSideBar())) ;
        driveNode.setProperty(ALL_CREATE_FOLDER, data.getAllowCreateFolder()) ;
        driveHome.save() ;
      }
      session.save() ;
    }
  }
  
  public void init(String repository) throws Exception {
    Iterator<ObjectParameter> it = params_.getObjectParamIterator() ;
    Session session = null ;
     while(it.hasNext()){
       DriveData data = (DriveData)it.next().getObject() ;
       String defaultRepo = repositoryService_.getDefaultRepository().getConfiguration().getName() ;
       if(data.getRepository().equals(defaultRepo)) {
         session = repositoryService_.getRepository(repository)
         .getSystemSession(cmsConfigService_.getWorkspace(repository)) ;
         String drivesPath = cmsConfigService_.getJcrPath(BasePath.EXO_DRIVES_PATH);
         Node driveHome = (Node)session.getItem(drivesPath) ;
         
         if(!driveHome.hasNode(data.getName())){
           Node driveNode = driveHome.addNode(data.getName(), "exo:drive");
           driveNode.setProperty(WORKSPACE, data.getWorkspace()) ;
           driveNode.setProperty(PERMISSIONS, data.getPermissions()) ;
           driveNode.setProperty(PATH, data.getHomePath()) ;
           driveNode.setProperty(VIEWS, data.getViews()) ;
           driveNode.setProperty(ICON, data.getIcon()) ;
           driveNode.setProperty(VIEW_REFERENCES, Boolean.toString(data.getViewPreferences())) ;
           driveNode.setProperty(VIEW_NON_DOCUMENT, Boolean.toString(data.getViewNonDocument())) ;
           driveNode.setProperty(VIEW_SIDEBAR, Boolean.toString(data.getViewSideBar())) ;
           driveNode.setProperty(ALL_CREATE_FOLDER, data.getAllowCreateFolder()) ;
           driveHome.save() ;
         }
         session.save() ;
       }       
     }
     
   }
  
  public void addDrive(String name, String workspace, String permissions, String homePath, 
                        String views, String icon, boolean viewReferences, boolean viewNonDocument, 
                        boolean viewSideBar, String repository, String allowCreateFolder) throws Exception {
    String drivesPath = cmsConfigService_.getJcrPath(BasePath.EXO_DRIVES_PATH);
    Session session = repositoryService_.getRepository(repository)
                      .getSystemSession(cmsConfigService_.getWorkspace()) ;
    Node driveHome = (Node)session.getItem(drivesPath) ;
    if (!driveHome.hasNode(name)){
      Node driveNode = driveHome.addNode(name, "exo:drive");
      driveNode.setProperty(WORKSPACE, workspace) ;
      driveNode.setProperty(PERMISSIONS, permissions) ;
      driveNode.setProperty(PATH, homePath) ;      
      driveNode.setProperty(VIEWS, views) ;
      driveNode.setProperty(ICON, icon) ;
      driveNode.setProperty(VIEW_REFERENCES, Boolean.toString(viewReferences)) ;
      driveNode.setProperty(VIEW_NON_DOCUMENT, Boolean.toString(viewNonDocument)) ;
      driveNode.setProperty(VIEW_SIDEBAR, Boolean.toString(viewSideBar)) ;
      driveNode.setProperty(ALL_CREATE_FOLDER, allowCreateFolder) ;
      driveHome.save() ;
    }else{
      Node driveNode = driveHome.getNode(name);
      driveNode.setProperty(WORKSPACE, workspace) ;
      driveNode.setProperty(PERMISSIONS, permissions) ;
      driveNode.setProperty(PATH, homePath) ;      
      driveNode.setProperty(VIEWS, views) ;
      driveNode.setProperty(ICON, icon) ;
      driveNode.setProperty(VIEW_REFERENCES, Boolean.toString(viewReferences)) ;
      driveNode.setProperty(VIEW_NON_DOCUMENT, Boolean.toString(viewNonDocument)) ;
      driveNode.setProperty(VIEW_SIDEBAR, Boolean.toString(viewSideBar)) ;
      driveNode.setProperty(ALL_CREATE_FOLDER, allowCreateFolder) ;
      driveNode.save() ;
    }
    session.save() ;
  }
  
}
