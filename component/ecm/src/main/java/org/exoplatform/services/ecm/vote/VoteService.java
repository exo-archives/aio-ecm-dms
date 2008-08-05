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
import javax.jcr.Session;
import javax.jcr.Value;

import org.exoplatform.services.jcr.core.ManageableRepository;

/**
 * Created by The eXo Platform SAS
 * Author : Ly Dinh Quang
 *          quang.ly@exoplatform.com
 *			    xxx5669@yahoo.com
 * May 7, 2008  
 */
public class VoteService {  
  final static String VOTABLE = "mix:votable".intern();  
  final static String VOTER_PROP = "exo:voter".intern();  
  final static String VOTING_RATE_PROP = "exo:votingRate".intern();
  final static String VOTE_TOTAL_PROP = "exo:voteTotal".intern();    
      
  /**
   * Voting the document that is specified by the node by giving the rate and userName
   * @param node          The node document for votting
   * @param rate          The number rate for votting
   * @param userName      The username of current user is votting. 
   *                      May be <code>null</code> or <code>blank</code>
   * @see                 Node
   * @throws Exception
   */
  public void vote(Node node, double rate, String userName) throws Exception {   
    Session session = node.getSession();    
    Session systemSession = null;
    Node document = null;
    if (userName != null && userName.trim().length() > 0) {
      document = node;
    } else {
      ManageableRepository repository = (ManageableRepository) session.getRepository();
      systemSession = repository.getSystemSession(session.getWorkspace().getName());
      document = (Node)systemSession.getItem(node.getPath());    
    }   
    if (document.canAddMixin(VOTABLE)) {
      document.addMixin(VOTABLE);
    }
    
    long voteTotal = 0;
    if (document.hasProperty(VOTE_TOTAL_PROP)) {
      voteTotal = document.getProperty(VOTE_TOTAL_PROP).getLong();
    }    
    double voteRate = 0;
    if (document.hasProperty(VOTING_RATE_PROP)) {
      voteRate = document.getProperty(VOTING_RATE_PROP).getDouble();
    }
    double newRate = (voteTotal * voteRate + rate) / (voteTotal + 1);    
    DecimalFormat format = new DecimalFormat("###.##");
    double formatedRate= format.parse(format.format(newRate)).doubleValue();    
    if (userName != null) {
      Value[] voters = {};
      if (document.hasProperty(VOTER_PROP)) {
        voters = document.getProperty(VOTER_PROP).getValues();        
      }
      Value newVoter = null;
      List<Value> newVoterList = new ArrayList<Value>();
      newVoter = document.getSession().getValueFactory().createValue(userName);
      newVoterList.addAll(Arrays.<Value>asList(voters));    
      newVoterList.add(newVoter);
      document.setProperty(VOTER_PROP, newVoterList.toArray(new Value[newVoterList.size()]));
    }    
    document.setProperty(VOTE_TOTAL_PROP, voteTotal + 1);     
    document.setProperty(VOTING_RATE_PROP, formatedRate);
    document.getSession().save();    
    //logout system session if vote for anonymous
    if (systemSession != null) {      
      systemSession.logout();
    }
  }
}
