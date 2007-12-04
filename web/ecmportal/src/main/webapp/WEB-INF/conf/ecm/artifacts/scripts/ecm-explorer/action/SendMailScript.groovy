/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

import java.util.Map;

import org.exoplatform.services.cms.scripts.CmsScript;

/*
* Will need to get The MailService when it has been moved to exo-platform
*/
public class SendMailScript implements CmsScript {
  
  public SendMailScript() {
  }
  
  public void execute(Object context) {
    Map variables = (Map) context;       

    //TODO Should send an email
    println("Send message in SendMailScript to " + variables.get("exo:to"));
  }

  public void setParams(String[] params) {}

}