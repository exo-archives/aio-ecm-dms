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
package org.exoplatform.services.ecm.vote;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.exoplatform.services.ecm.i18n.MultiLanguageService;
import org.exoplatform.services.jcr.core.ManageableRepository;

/**
 * Created by The eXo Platform SAS
 * Author : Ly Dinh Quang
 *          quang.ly@exoplatform.com
 *			    xxx5669@yahoo.com
 * May 7, 2008  
 */
public class VoteService {  
  final static String VOTABLE = "mix:votable".intern() ;  
  final static String VOTER_PROP = "exo:voter".intern() ;  
  final static String VOTING_RATE_PROP = "exo:votingRate".intern() ;
  final static String VOTE_TOTAL_PROP = "exo:voteTotal".intern() ; 
  final static String VOTE_TOTAL_LANG_PROP = "exo:voteTotalOfLang".intern() ;
  private static final String LANGUAGES = "languages".intern() ;

  private MultiLanguageService multiLangService_ ;  
  
  public VoteService(MultiLanguageService multiLangService) {
    multiLangService_ = multiLangService ;
  }
  
  public void vote(Node node, double rate, String userName, String language) throws Exception {    
    Node languageNode = null;
    Session session = node.getSession();    
    Session systemSession = null;
    Node document = null;
    if (userName == null) {
      ManageableRepository repository = (ManageableRepository) session.getRepository();
      systemSession = repository.getSystemSession(session.getWorkspace().getName());
      document = (Node)systemSession.getItem(node.getPath());      
    } else {
      document = node;
    }    
    if (document.canAddMixin(VOTABLE)) {
      document.addMixin(VOTABLE);
    }            
    String defaultLanguage = multiLangService_.getDefaultLanguage(document);    
    if(language != null && !language.equalsIgnoreCase(defaultLanguage)) {
      try {
        languageNode = document.getNode(LANGUAGES + "/" +language) ;
      } catch (PathNotFoundException e) {
        languageNode = document ;
      }
    } else {
      languageNode = document ;
    }       
    
    long voteTotalOfLang = 0;    
    if (languageNode.hasProperty(VOTE_TOTAL_LANG_PROP)) {
      voteTotalOfLang = languageNode.getProperty(VOTE_TOTAL_LANG_PROP).getLong(); 
    }    
    double voteRate = 0;
    if (languageNode.hasProperty(VOTING_RATE_PROP)) {
      voteRate = languageNode.getProperty(VOTING_RATE_PROP).getDouble();
    }
    double newRate = (voteTotalOfLang * voteRate + rate) / (voteTotalOfLang + 1);    
    DecimalFormat format = new DecimalFormat("###.##") ;
    double formatedRate= format.parse(format.format(newRate)).doubleValue() ;    
    if (userName != null) {
      Value[] voters = {} ;
      if(languageNode.hasProperty(VOTER_PROP)) {
        voters = languageNode.getProperty(VOTER_PROP).getValues() ;        
      }
      Value newVoter = null;
      List<Value> newVoterList = new ArrayList<Value>() ;
      newVoter = languageNode.getSession().getValueFactory().createValue(userName);
      newVoterList.addAll(Arrays.<Value>asList(voters)) ;    
      newVoterList.add(newVoter) ;
      languageNode.setProperty(VOTER_PROP, newVoterList.toArray(new Value[newVoterList.size()]));
    }    
    languageNode.setProperty(VOTE_TOTAL_PROP, getVoteTotal(document) + 1);
    languageNode.setProperty(VOTE_TOTAL_LANG_PROP, voteTotalOfLang + 1);    
    languageNode.setProperty(VOTING_RATE_PROP, formatedRate);
    languageNode.getSession().save();    
    //logout system session if vote for anonymous
    if (systemSession != null) {      
      systemSession.logout() ;
    }

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
}
