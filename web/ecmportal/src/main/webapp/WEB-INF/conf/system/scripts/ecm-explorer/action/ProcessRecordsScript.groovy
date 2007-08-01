import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.services.cms.scripts.CmsScript;
import org.exoplatform.services.cms.records.RecordsService;
import org.exoplatform.services.jcr.RepositoryService;

public class ProcessRecordsScript implements CmsScript {

  private RecordsService recordsService = null; 
  private RepositoryService repositoryService = null; 
   

  public ProcessRecordsScript(RepositoryService repositoryService,
       RecordsService recordsService) {                        
    this.recordsService = recordsService;
    this.repositoryService = repositoryService;
  }
  
  public void execute(Object context) {
    Session session = null ;
    try {
      String workspace = (String) ((Map) context).get("srcWorkspace") ;
      session = repositoryService.getRepository().login(workspace);
      Node filePlan = (Node) session.getItem((String)((Map) context).get("srcPath")); 
      Node record = (Node) session.getItem((String)((Map) context).get("nodePath"));
      recordsService.addRecord(filePlan, record);
      session.save();
      session.logout();
    }
    catch (Exception e) {
      if(session !=null) {
        session.logout();
      }
      e.printStackTrace();
    }
  }

  public void setParams(String[] params) {}
}
