/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

/*********************************************************************************
#jsfDialogFormField("inputName=testtype" "jcrPath=/node/exo:user" 
										"widget=selectbox" "script=FillSelectBoxWithNodeChildren.groovy" 
										"scriptParams=/home/users")
*********************************************************************************/

import java.util.List ;
import java.util.ArrayList ;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;

import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.core.model.SelectItemOption;

import org.exoplatform.services.cms.scripts.CmsScript;

public class FillSelectBoxWithNodeChildren implements CmsScript {
  
  private RepositoryService repositoryService_;
  private String folder_;
  
  public FillSelectBoxWithNodeChildren(RepositoryService repositoryService) {
    repositoryService_ = repositoryService;
  }
  
  public void execute(Object context) {
    UIFormSelectBox selectBox = (UIFormSelectBox) context;

    ManageableRepository jcrRepository = repositoryService_.getRepository();
    List options = new ArrayList();
    Session session = null;
    try{
      session = jcrRepository.login("production");
      QueryManager queryManager = session.getWorkspace().getQueryManager();
      
      String xpath = folder_ + "/*";
      Query q = queryManager.createQuery(xpath, Query.XPATH);
      QueryResult result = q.execute();
      NodeIterator nodeIterator = result.getNodes();
      
      while (nodeIterator.hasNext()) {
        Node n = nodeIterator.nextNode();
        options.add(new SelectItemOption(n.getName(), n.getName()));
      }
      session.logout();
    } catch (Exception e) {
      if(session !=null) {
        session.logout();
      }
	    print ("script error")
    } 
    
    selectBox.setOptions(options);
  }

  public void setParams(String[] params) {
    folder_ = params[0];
    print("  folder_ : " + folder_);
  }

}
