/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SAS, All rights reserved.          *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.services.cms.scripts.CmsScript;
import org.exoplatform.services.cms.records.RecordsService;
import org.exoplatform.services.jcr.RepositoryService;

public class PostFilePlanInterceptor implements CmsScript {

  private RepositoryService repositoryService_;
  private RecordsService recordsService_;
  
  public PostFilePlanInterceptor(RepositoryService repositoryService,
      RecordsService recordsService) {
    repositoryService_ = repositoryService;
    recordsService_ = recordsService;
  }
  
  public void execute(Object context) {
    String path = (String) context;       

    println("Post File Plan interceptor, created node: "+path);

    String[] splittedContent = StringUtils.split(path, ":");
    Session session = repositoryService_.getRepository().login(splittedContent[0]);
    Node filePlan = (Node) session.getItem(splittedContent[1]);

    recordsService_.bindFilePlanAction(filePlan);
  }

  public void setParams(String[] params) {}

}