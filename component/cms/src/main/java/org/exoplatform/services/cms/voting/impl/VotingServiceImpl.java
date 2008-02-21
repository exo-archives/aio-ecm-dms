/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
import org.exoplatform.services.jcr.core.ManageableRepository;

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
      if(languageNode.hasProperty(VOTE_TOTAL_LANG_PROP)) {
        voteTotal = voteTotal + languageNode.getProperty(VOTE_TOTAL_LANG_PROP).getLong() ;
      }
    }
    return voteTotal ;
  }
  
  public void vote(Node node, double rate, String userName, String language) throws Exception {
    Session session = node.getSession() ;
    ManageableRepository repository = (ManageableRepository)session.getRepository() ;
    Session systemSession = repository.getSystemSession(session.getWorkspace().getName()) ;
    //TODO check if need delegate to system session
    Node document = (Node)systemSession.getItem(node.getPath()) ; 
    if(!document.isNodeType(VOTABLE)) {
      if(document.canAddMixin(VOTABLE)) document.addMixin(VOTABLE) ;
      else throw new NoSuchNodeTypeException() ;
    }        
    String defaultLang = multiLangService_.getDefault(document) ;           
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
    Value newVoter = systemSession.getValueFactory().createValue(userName) ;    
    List<Value> newVoterList = new ArrayList<Value>() ;
    newVoterList.addAll(Arrays.<Value>asList(voters)) ;    
    newVoterList.add(newVoter) ;        

    document.setProperty(VOTE_TOTAL_PROP,getVoteTotal(document)+1) ; 
    languageNode.setProperty(VOTE_TOTAL_LANG_PROP,voteTotalOfLang+1) ;
    languageNode.setProperty(VOTING_RATE_PROP,fomatedRating) ;
    languageNode.setProperty(VOTER_PROP,newVoterList.toArray(new Value[newVoterList.size()])) ;
    document.save() ;
    systemSession.save();
    systemSession.logout();
  }       
}
