/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.comments.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cms.comments.CommentsService;
import org.exoplatform.services.cms.i18n.MultiLanguageService;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Xuan Hoa
 *          hoa.pham@exoplatform.com
 * Jan 28, 2007  
 */
public class CommentsServiceImpl implements CommentsService {
  
  private final static String COMMENTS = "comments".intern() ;
  private final static String COMMENTABLE = "mix:commentable".intern() ;
  private final static String EXO_COMMENTS = "exo:comments".intern() ;
  private final static String NT_UNSTRUCTURE = "nt:unstructured".intern() ;
  private final static String MESSAGE = "exo:commentContent".intern() ;
  private final static String COMMENTOR = "exo:commentor".intern() ;
  private final static String COMMENTOR_EMAIL = "exo:commentorEmail".intern() ;
  private final static String COMMENTOR_SITE = "exo:commentorSite".intern() ;
  private final static String CREATED_DATE = "exo:commentDate".intern() ;
  private static final String LANGUAGES = "languages".intern() ;
  private static final String ANONYMOUS = "anonymous".intern() ;
  
  private ExoCache commentsCache_ ;
  private MultiLanguageService multiLangService_ ;  
  
  public CommentsServiceImpl(CacheService cacheService, 
      MultiLanguageService multiLangService) throws Exception {    
    commentsCache_ = cacheService.getCacheInstance(CommentsService.class.getName()) ;
    multiLangService_ = multiLangService ;    
  }
  
  public void addComment(Node document, String commentor,String email, String site, String comment,String language) throws Exception {
    if(!document.isNodeType(COMMENTABLE)) {
      if(document.canAddMixin(COMMENTABLE)) document.addMixin(COMMENTABLE) ;
      else throw new Exception("This node does not support comments.") ;  
    }        
    Node multiLanguages =null, languageNode= null, commentNode = null ;
    
    if(!document.hasNode(LANGUAGES) || language.equals(multiLangService_.getDefault(document))) {
      if(document.hasNode(COMMENTS)) commentNode = document.getNode(COMMENTS) ;
      else commentNode = document.addNode(COMMENTS,NT_UNSTRUCTURE) ;
    } else {
      multiLanguages = document.getNode(LANGUAGES) ;
      if(multiLanguages.hasNode(language)) {
        languageNode = multiLanguages.getNode(language) ;
      } else {
        languageNode = multiLanguages.addNode(language) ;
      }
      if(languageNode.hasNode(COMMENTS)) {
        commentNode = languageNode.getNode(COMMENTS) ;
      } else{
        commentNode = languageNode.addNode(COMMENTS,NT_UNSTRUCTURE) ;
      }
    }
     
    if(commentor == null || commentor.length() == 0) {
      commentor = ANONYMOUS ;      
    }
    
    Calendar commentDate = new GregorianCalendar() ;
    String name = Long.toString(commentDate.getTimeInMillis()) ;    
    Node newComment = commentNode.addNode(name,EXO_COMMENTS) ;     
    newComment.setProperty(COMMENTOR,commentor) ;
    newComment.setProperty(CREATED_DATE,commentDate) ;
    newComment.setProperty(MESSAGE,comment) ;
    if(email!=null && email.length()>0) {
      newComment.setProperty(COMMENTOR_EMAIL,email) ;
    }
    if(site !=null && site.length()>0) {
      newComment.setProperty(COMMENTOR_SITE,site) ;
    }          
    document.save();
    document.getSession().save() ;    
    commentsCache_.remove(commentNode.getPath()) ;
  }

  @SuppressWarnings("unchecked")
  public List<Node> getComments(Node document,String language) throws Exception {    
    Node commentsNode = null ;
    Node languagesNode = null ;
    Node languageNode = null ;
    if(!isSupportedLocalize(document,language)) {
      language = document.getProperty("exo:language").getString() ; 
    }
    if(document.hasNode(LANGUAGES)) {
      languagesNode = document.getNode(LANGUAGES) ;
      if(languagesNode.hasNode(language)) {
        languageNode = languagesNode.getNode(language) ;
        if(languageNode.hasNode(COMMENTS)) commentsNode = languageNode.getNode(COMMENTS) ;
      } else if(language.equals(multiLangService_.getDefault(document))) {
        languageNode = document ;
      }
    } else {
      languageNode = document ;
    }
    if(!languageNode.hasNode(COMMENTS)) return new ArrayList<Node>() ;    
    commentsNode = languageNode.getNode(COMMENTS) ;
    String cacheKey = document.getPath().concat(commentsNode.getPath());
    Object comments = commentsCache_.get(cacheKey) ;
    if(comments !=null) return (List<Node>)comments ;        
    List<Node> list = new ArrayList<Node>() ;
    for(NodeIterator iter = commentsNode.getNodes(); iter.hasNext();) {
      list.add(iter.nextNode()) ;
    }    
    Collections.sort(list,new DateComparator()) ;
    commentsCache_.put(commentsNode.getPath(),list) ;        
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
  
  private boolean isSupportedLocalize(Node document,String language)throws Exception {
    List<String> locales= multiLangService_.getSupportedLanguages(document) ;
    if(Collections.frequency(locales,language) >0) return true ;
    return false ;
  }
  
}
