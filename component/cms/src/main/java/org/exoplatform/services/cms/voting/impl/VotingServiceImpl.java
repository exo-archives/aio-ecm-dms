/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cms.voting.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NoSuchNodeTypeException;

import org.exoplatform.services.cms.i18n.MultiLanguageService;
import org.exoplatform.services.cms.voting.VotingService;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Xuan Hoa
 *          hoa.pham@exoplatform.com
 * Jan 17, 2007  
 */
public class VotingServiceImpl implements VotingService {

  final static String VOTABLE = "mix:votable".intern() ;  
  final static String VOTER_PROP = "exo:voter".intern() ;  
  final static String VOTING_RATE_PROP = "exo:votingRate".intern() ;
  final static String VOTE_TOTAL_PROP = "exo:voteTotal".intern() ; 
  final static String VOTE_TOTAL_LANG_PROP = "exo:voteTotalOfLang".intern() ;
  private static final String LANGUAGES = "languages".intern() ;
  
  private MultiLanguageService multiLangService_ ;  
  
  public VotingServiceImpl(MultiLanguageService multiLangService) {
    multiLangService_ = multiLangService ;
  }  

  public long getVoteTotal(Node node) throws Exception {
    long voteTotal = 0;
    if(!node.hasNode(LANGUAGES) && node.hasProperty(VOTE_TOTAL_PROP)) {
      return node.getProperty(VOTE_TOTAL_LANG_PROP).getLong() ;
    }
    Node multiLanguages = node.getNode(LANGUAGES) ;
    voteTotal = node.getProperty(VOTE_TOTAL_LANG_PROP).getLong() ;
    NodeIterator nodeIter = multiLanguages.getNodes() ;
    while(nodeIter.hasNext()) {
      Node languageNode = nodeIter.nextNode() ;
      voteTotal = voteTotal + languageNode.getProperty(VOTE_TOTAL_LANG_PROP).getLong() ;
    }
    return voteTotal ;
  }
  
  public void vote(Node document, double rate, String userName, String language) throws Exception {       
    if(!document.isNodeType(VOTABLE)) {
      if(document.canAddMixin(VOTABLE)) document.addMixin(VOTABLE) ;
      else throw new NoSuchNodeTypeException() ;
    }        
    String defaultLang = multiLangService_.getDefault(document) ;
    Session session = document.getSession() ;
    Node multiLanguages =null, languageNode= null ;
    if(language.equals(defaultLang)) {
      languageNode = document ;
    } else {
      if(document.hasNode(LANGUAGES)) {
        multiLanguages = document.getNode(LANGUAGES) ;
        if(multiLanguages.hasNode(language)) {
          languageNode = multiLanguages.getNode(language) ;
        }
      }
    }
    long voteTotalOfLang = languageNode.getProperty(VOTE_TOTAL_LANG_PROP).getLong() ;
    double votingRate = languageNode.getProperty(VOTING_RATE_PROP).getDouble() ;
    double newRating = ((voteTotalOfLang*votingRate)+rate)/(voteTotalOfLang+1) ;    
    DecimalFormat format = new DecimalFormat("###.##") ;
    double fomatedRating= format.parse(format.format(newRating)).doubleValue() ;
    Value[] voters = {} ;
    if(languageNode.hasProperty(VOTER_PROP)) {
      voters = languageNode.getProperty(VOTER_PROP).getValues() ;        
    }
    Value newVoter = session.getValueFactory().createValue(userName) ;    
    List<Value> newVoterList = new ArrayList<Value>() ;
    newVoterList.addAll(Arrays.<Value>asList(voters)) ;    
    newVoterList.add(newVoter) ;        

    document.setProperty(VOTE_TOTAL_PROP,getVoteTotal(document)+1) ; 
    languageNode.setProperty(VOTE_TOTAL_LANG_PROP,voteTotalOfLang+1) ;
    languageNode.setProperty(VOTING_RATE_PROP,fomatedRating) ;
    languageNode.setProperty(VOTER_PROP,newVoterList.toArray(new Value[newVoterList.size()])) ;
    document.save() ;
    session.save() ;
  }       
}
