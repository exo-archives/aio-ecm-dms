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
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NoSuchNodeTypeException;

import org.exoplatform.container.SessionContainer;
import org.exoplatform.services.cms.voting.VotingService;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Xuan Hoa
 *          hoa.pham@exoplatform.com
 * Jan 17, 2007  
 */
public class VotingServiceImpl implements VotingService{

  final static String VOTABLE = "mix:votable".intern() ;  
  final static String VOTER_PROP = "exo:voter".intern() ;  
  final static String VOTING_RATE_PROP = "exo:votingRate".intern() ;
  final static String VOTE_TOTAL_PROP = "exo:voteTotal".intern() ;  
  
  public VotingServiceImpl() {
  }  

  public void vote(Node document, double rate) throws Exception {       
    if(!document.isNodeType(VOTABLE)) {
      if(document.canAddMixin(VOTABLE))
        document.addMixin(VOTABLE) ;
      else
        throw new NoSuchNodeTypeException() ;
    }        
    Session session = document.getSession() ;
    long voteTotal = document.getProperty(VOTE_TOTAL_PROP).getLong() ;
    double votingRate = document.getProperty(VOTING_RATE_PROP).getDouble() ;
    double newRating = ((voteTotal*votingRate)+rate)/(voteTotal+1) ;    
    DecimalFormat format = new DecimalFormat("###.##") ;
    double fomatedRating= format.parse(format.format(newRating)).doubleValue() ;
    String userId = SessionContainer.getInstance().getRemoteUser() ;    
    Value[] voters = document.getProperty(VOTER_PROP).getValues() ;        
    Value newVoter = session.getValueFactory().createValue(userId) ;    
    List<Value> newVoterList = new ArrayList<Value>() ;
    newVoterList.addAll(Arrays.<Value>asList(voters)) ;    
    newVoterList.add(newVoter) ;        

    document.setProperty(VOTE_TOTAL_PROP,voteTotal+1) ;        
    document.setProperty(VOTING_RATE_PROP,fomatedRating) ;
    document.setProperty(VOTER_PROP,newVoterList.toArray(new Value[newVoterList.size()])) ;
    document.save() ;
    session.save() ;
  }       

}
