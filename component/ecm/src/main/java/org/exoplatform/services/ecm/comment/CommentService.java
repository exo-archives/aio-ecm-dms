/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.ecm.comment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

import org.exoplatform.services.ecm.i18n.MultiLanguageService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.organization.OrganizationService;

/**
 * Created by The eXo Platform SAS
 * Author : Ly Dinh Quang
 *          quang.ly@exoplatform.com
 *			    xxx5669@yahoo.com
 * May 9, 2008  
 */
public class CommentService {
  final static String COMMENTS = "comments".intern() ;
  final static String COMMENTABLE = "mix:commentable".intern() ;
  final static String EXO_COMMENTS = "exo:comments".intern() ;
  final static String NT_UNSTRUCTURE = "nt:unstructured".intern() ;
  final static String MESSAGE = "exo:commentContent".intern() ;
  final static String COMMENTOR = "exo:commentor".intern() ;
  final static String COMMENTOR_EMAIL = "exo:commentorEmail".intern() ;
  final static String COMMENTOR_SITE = "exo:commentorSite".intern() ;
  final static String CREATED_DATE = "exo:commentDate".intern() ;
  static final String LANGUAGES = "languages".intern() ;
  static final String ANONYMOUS = "anonymous".intern() ;
  
  private MultiLanguageService multiLanguageService_;
  private OrganizationService orgService_ ;
  public CommentService(MultiLanguageService multiLanguageService) throws Exception {
    multiLanguageService_ = multiLanguageService;    
  }
  
  public void addComment(Node node, String commentor,String email, String site, String comment,String language) throws Exception {   
    Node document = null;
    Node commentNode = null;
    Session session = node.getSession();
    Session systemSession = null;
    if (commentor != null && commentor.length() > 0) {
      document = node;
    } else {
      ManageableRepository repository = (ManageableRepository)session.getRepository();
      systemSession = repository.getSystemSession(session.getWorkspace().getName());
      document = (Node)systemSession.getItem(node.getPath());
      commentor = ANONYMOUS;
    }    
    if (!document.isNodeType(COMMENTABLE)) {
      document.addMixin(COMMENTABLE);
    }
    String defaultLanguage = multiLanguageService_.getDefaultLanguage(document);
    Node languageNode = null;
    if (language != null && !language.equalsIgnoreCase(defaultLanguage)) {
      try {
        languageNode = document.getNode(LANGUAGES);        
      } catch (PathNotFoundException e) {
        languageNode = document;
      }
    } else {
      languageNode = document;      
    }   
    try {
      commentNode = languageNode.getNode(COMMENTS);
    } catch (PathNotFoundException e) {      
      commentNode = languageNode.addNode(COMMENTS, NT_UNSTRUCTURE);      
    }
    Calendar commentDate = new GregorianCalendar();
    String name = Long.toString(commentDate.getTimeInMillis());
    Node newComment = commentNode.addNode(name, EXO_COMMENTS);
    newComment.setProperty(COMMENTOR, commentor) ;
    newComment.setProperty(CREATED_DATE, commentDate) ;
    newComment.setProperty(MESSAGE, comment) ;
    if (email != null && email.length() > 0) {
      newComment.setProperty(COMMENTOR_EMAIL, email);
    }
    if (site == null) {
      site = "";      
    }
    newComment.setProperty(COMMENTOR_SITE, site);
    document.getSession().save();
    if(systemSession != null) {
      systemSession.logout();
    }
  }  
  
  private boolean isSupportedLocalize(Node document, String language) throws Exception {
    List<String> locales = multiLanguageService_.getAvailableLanguages(document) ;
    if (locales != null &&locales.contains(language)) return true;
    return false ;
  }
  
  public List<Node> getComment(Node node, String language) throws Exception {
    Node commentsNode = null, languageNode = null;
    if (!isSupportedLocalize(node, language)) {
      language = node.getProperty("exo:language").getString();
    }
    try {
      languageNode = node.getNode(LANGUAGES + "/" + language);      
    } catch (Exception e) {
      languageNode = node;
    }
    
    if (languageNode.hasNode(COMMENTS)) {
      commentsNode = languageNode.getNode(COMMENTS) ;
    } else {
      return new ArrayList<Node>();
    }
    
    List<Node> list = new ArrayList<Node>() ;
    NodeIterator iterate = commentsNode.getNodes();
    while (iterate.hasNext()) {
      list.add(iterate.nextNode());
    }       
    Collections.sort(list,new DateComparator()) ;   
    return list;
  }
  
  private class DateComparator implements Comparator<Node> {

    public int compare(Node node1, Node node2) {
      try{
        Date date1 = node1.getProperty(CREATED_DATE).getDate().getTime() ;
        Date date2 = node2.getProperty(CREATED_DATE).getDate().getTime() ;
        return date2.compareTo(date1) ;
      }catch (Exception e) {        
      }            
      return 0;
    }        
  }
}
