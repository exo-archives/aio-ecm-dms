package org.exoplatform.services.cms.drives;

import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
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
  

  private RepositoryService repositoryService_;
  private CmsConfigurationService cmsConfigService_;
  private InitParams params_ ; 

  private Session session_ ;  
  public ManageDrivePlugin(RepositoryService repositoryService, 
      InitParams params, CmsConfigurationService cmsConfigService) throws Exception {
    repositoryService_ = repositoryService;
    cmsConfigService_ = cmsConfigService ;
    session_ = repositoryService_.getRepository().getSystemSession(cmsConfigService_.getWorkspace()) ; 
    params_ = params ;
    initRepository() ;
  }

  private void initRepository() throws Exception {
   Iterator<ObjectParameter> it = params_.getObjectParamIterator() ;       
    String drivesPath = cmsConfigService_.getJcrPath(BasePath.EXO_DRIVES_PATH);
    Node driveHome = (Node)session_.getItem(drivesPath) ;
    
    while(it.hasNext()){
      DriveData data = (DriveData)it.next().getObject() ;
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
        driveHome.save() ;
      }        
    }
    session_.save() ;
  }

  public void addDrive(String name, String workspace, String permissions, String homePath, 
                        String views, String icon, boolean viewReferences, boolean viewNonDocument, 
                        boolean viewSideBar) throws Exception {
    String drivesPath = cmsConfigService_.getJcrPath(BasePath.EXO_DRIVES_PATH);
    Node driveHome = (Node)session_.getItem(drivesPath) ;
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
      driveNode.save() ;
    }
    session_.save() ;
  }
  
}
