/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

import java.util.Map;
import java.util.Properties;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.GregorianCalendar ;

import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;

import javax.jcr.Node;
//import javax.jcr.Session;

import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository ;

import org.exoplatform.services.cms.scripts.CmsScript;

/*
* Will need to get The MailService when it has been moved to exo-platform
*/
public class GetMailScript implements CmsScript {
  
  private RepositoryService repositoryService_;
  private NodeHierarchyCreator nodeHierarchyCreator_ ;
  
  public GetMailScript(RepositoryService repositoryService, NodeHierarchyCreator nodeHierarchyCreator) {
    repositoryService_ = repositoryService;
    nodeHierarchyCreator_ = nodeHierarchyCreator ;
  }
  
  public void execute(Object context) {
    Map variables = (Map) context;
    
    String protocol = (String)context.get("exo:protocol") ;
		String host = (String)context.get("exo:host") ;
		String port = (String)context.get("exo:port") ;
		String box = (String)context.get("exo:folder") ;
		String userName = (String)context.get("exo:userName") ;
		String password = (String)context.get("exo:password") ;
		String storePath = (String)context.get("exo:storePath") ;			
		GregorianCalendar gc = new GregorianCalendar() ;
    println("\n\n " + gc.getTime());
    println("\n ### Getting mail from " + host + " ... !");
    
    try{
			Properties props = System.getProperties();
			if(protocol.equals("pop3")) {
				props.setProperty("mail.pop3.socketFactory.fallback", "false");
				props.setProperty( "mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			}else {
				props.setProperty("mail.imap.socketFactory.fallback", "false");
				props.setProperty( "mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			}
			
			Session session = Session.getDefaultInstance(props);
			URLName url = new URLName(protocol, host, Integer.valueOf(port), box, userName, password) ;
			Store store = session.getStore(url) ;
			store.connect();
			Folder folder = store.getFolder(box);
			folder.open(Folder.READ_ONLY);
			Message[] mess = folder.getMessages() ;
			int totalMess = mess.length ;
			System.out.println("\n Total: " + mess.length + " message(s)") ;
			if(totalMess > 0) {
				Node storeNode = createStoreNode(storePath + "/" + box) ;
				int i = 0 ;
				while(i < totalMess){				
					Message mes = mess[i] ;
					Node newMail = storeNode.addNode(mes.getSubject(), "exo:mail") ;
					newMail.setProperty("exo:from", getAddress(mes.getFrom())) ;
					newMail.setProperty("exo:to", getAddress(mes.getRecipients(Message.RecipientType.TO))) ;
					newMail.setProperty("exo:cc", getAddress(mes.getRecipients(Message.RecipientType.CC))) ;
					newMail.setProperty("exo:bcc", getAddress(mes.getRecipients(Message.RecipientType.BCC))) ;
					newMail.setProperty("exo:subject", mes.getSubject()) ;
					if(mes.getSentDate() != null) {
						gc.setTime(mes.getSentDate()) ;
    				newMail.setProperty("exo:sendDate", gc.getInstance()) ;
					}
					if(mes.getReceivedDate() != null) {
						gc.setTime(mes.getReceivedDate()) ;
						newMail.setProperty("exo:receivedDate", gc.getInstance()) ;
					} 
					Object obj = mes.getContent() ;									
					if (obj instanceof Multipart) {
						saveMultipartMail((Multipart)obj, newMail);
					} else {
						saveMail(mes, newMail);
        	}
					
					i ++ ;
				}	
				storeNode.save() ;
				storeNode.getSession().save() ;					
      }      
      folder.close(false);
    	store.close();			
		}catch (Exception e) {
			e.printStackTrace() ;
    }
    println("\n ### Finished ");
  }

  public void setParams(String[] params) {}
  
  private void saveMultipartMail(Multipart multipart, Node newMail){
  	try {
			int i = 0 ;
			int n = multipart.getCount() ;
			while( i < n) {
				saveMail(multipart.getBodyPart(i), newMail);
				i++ ;
			}			
		}catch(Exception e) {
			e.printStackTrace() ;
		}		
		
	}
	
	private void saveMail(Part part, Node newMail){
		try {			
			String disposition = part.getDisposition();
			String contentType = part.getContentType();
			if (disposition == null) {				
				if(part.isMimeType("text/plain")){
					newMail.setProperty("exo:content", (String)part.getContent());
				}else if(!part.isMimeType("text/html")){
					MimeMultipart mimeMultiPart = (MimeMultipart)part.getContent() ;
					newMail.setProperty("exo:content", (String)mimeMultiPart.getBodyPart(0).getContent());				
				}				
			} else if (disposition.equalsIgnoreCase(Part.ATTACHMENT) || disposition.equalsIgnoreCase(Part.INLINE)) {
				Node attachment = newMail.addNode(part.getFileName(), "nt:file") ;
				Node content = attachment.addNode("jcr:content", "nt:resource") ;
				content.setProperty("jcr:encoding", "UTF-8") ;
				if(contentType.indexOf(";") > 0) {
					String[] type = contentType.split(";") ;
					content.setProperty("jcr:mimeType", type[0]) ;					
				}else {
					content.setProperty("jcr:mimeType", contentType) ;
				}
				GregorianCalendar gc = new GregorianCalendar() ;
				content.setProperty("jcr:lastModified", gc.getInstance()) ;
				InputStream is = part.getInputStream();
				byte[] buf = new byte[is.available()];
        is.read(buf);
				content.setProperty("jcr:data", new ByteArrayInputStream(buf)) ;							
				
			}
		
		}catch(Exception e) {
			e.printStackTrace() ;
		}
  }
  
  private Node createStoreNode(String storePath) {
  	try{
      ManageableRepository manaRepository = repositoryService_.getDefaultRepository() ;
      Node rootNode = manaRepository.getSystemSession(manaRepository.getConfiguration().getSystemWorkspaceName()).getRootNode();
			//Node rootNode = session.getRootNode();
			String[] array = storePath.split("/") ;
			int i = 0 ;
			int n = array.length ;
			while(i < n) {
				if(array[i] != null && array[i].trim().length() > 0) {
					if(rootNode.hasNode(array[i].trim())) {
						rootNode = rootNode.getNode(array[i].trim()) ;
					}else {
						rootNode.addNode(array[i].trim(),"nt:unstructured") ;
						rootNode.save() ;
						rootNode = rootNode.getNode(array[i].trim()) ;
					}
				}
				i++ ;
			}
      rootNode.getSession().save() ;
      return rootNode ;
  	}catch(Exception e) {
  		e.printStackTrace() ;
  	}
  	return null ;
  }
  
  private String getAddress(Address[] addr) {
		String str = "" ;
		int i = 0
		if(addr != null && addr.length > 0) {
			while (i < addr.length) {
				if(str.length() < 1)	{
					str = addr[i].toString() ;							
				}else {
					str = str + ", " + addr[i].toString() ;
				}						
				i++ ;
			}
		}		
		return str ;
  }

}