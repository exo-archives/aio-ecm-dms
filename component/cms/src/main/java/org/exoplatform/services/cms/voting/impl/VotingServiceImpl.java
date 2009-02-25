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

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cms.i18n.MultiLanguageService;
import org.exoplatform.services.cms.voting.VotingService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

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
      if(node.getPrimaryNodeType().getName().equals("nt:file")) {
        languageNode = getFileLangNode(languageNode) ;
      }
      if(languageNode.hasProperty(VOTE_TOTAL_LANG_PROP)) {
        voteTotal = voteTotal + languageNode.getProperty(VOTE_TOTAL_LANG_PROP).getLong() ;
      }
    }
    return voteTotal ;
  }
  
  public Node getFileLangNode(Node currentNode) throws Exception {
    if(currentNode.getNodes().getSize() > 0) {
      NodeIterator nodeIter = currentNode.getNodes() ;
      while(nodeIter.hasNext()) {
        Node ntFile = nodeIter.nextNode() ;
        if(ntFile.getPrimaryNodeType().getName().equals("nt:file")) {
          return ntFile ;
        }
      }
      return currentNode ;
    }
    return currentNode ;
  }
  
  public void vote(Node node, double rate, String userName, String language) throws Exception {
    Session session = node.getSession();
    if (userName == null) {
      String strWorkspaceName = node.getSession().getWorkspace().getName();
      ExoContainer eXoContainer = ExoContainerContext.getCurrentContainer();
      RepositoryService repositoryService = (RepositoryService) eXoContainer
          .getComponentInstanceOfType(RepositoryService.class);
      ManageableRepository manageRepository = repositoryService.getCurrentRepository();
      session = SessionProvider.createSystemProvider().getSession(strWorkspaceName,
          manageRepository);
      String uid = node.getUUID();
      node = session.getNodeByUUID(uid);
    }
    
    if(!node.isNodeType(VOTABLE)) {
      if(node.canAddMixin(VOTABLE)) node.addMixin(VOTABLE) ;
      else throw new NoSuchNodeTypeException() ;
    }        
    String defaultLang = multiLangService_.getDefault(node) ;           
    Node multiLanguages =null, languageNode= null ;
    if((defaultLang == null && language == null) || language.equals(defaultLang)) {
      languageNode = node ;
    } else {
      if(node.hasNode(LANGUAGES)) {
        multiLanguages = node.getNode(LANGUAGES) ;
        if(multiLanguages.hasNode(language)) {
          languageNode = multiLanguages.getNode(language) ;
          if(node.getPrimaryNodeType().getName().equals("nt:file")) {
            languageNode = getFileLangNode(languageNode) ;
          } 
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

    node.setProperty(VOTE_TOTAL_PROP,getVoteTotal(node)+1) ; 
    languageNode.setProperty(VOTE_TOTAL_LANG_PROP,voteTotalOfLang+1) ;
    languageNode.setProperty(VOTING_RATE_PROP,fomatedRating) ;
    languageNode.setProperty(VOTER_PROP,newVoterList.toArray(new Value[newVoterList.size()])) ;
    node.save() ;
    session.save() ;
    session.logout();
  }       
}
