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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.exoplatform.services.ecm.BaseECMTestCase;

/**
 * Created by The eXo Platform SAS
 * Author : Ly Dinh Quang
 *          quang.ly@exoplatform.com
 *			    xxx5669@yahoo.com
 * May 7, 2008  
 */
public class TestVoteService extends BaseECMTestCase {  
  private VoteService voteService;
  final static String VOTABLE = "mix:votable".intern() ;  
  final static String VOTER_PROP = "exo:voter".intern() ;
  final static String VOTE_TOTAL_PROP = "exo:voteTotal".intern() ; 
  final static String VOTING_RATE_PROP = "exo:votingRate".intern() ;  
  final static String VOTE_TOTAL_LANG_PROP = "exo:voteTotalOfLang".intern() ;
    
  public void testVoteService() throws Exception {
    Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS) ;    
    Node root = session.getRootNode();    
    Node test = root.addNode("Test", "nt:file");            
    Node content1 = test.addNode("jcr:content", "nt:resource");
    content1.setProperty("jcr:lastModified", Calendar.getInstance());
    content1.setProperty("jcr:mimeType", "text/xml");
    content1.setProperty("jcr:data", "");       
    session.save();
        
    //begin test
    List<Value> newVoterList1 = new ArrayList<Value>();
    List<Value> newVoterList2 = new ArrayList<Value>();
    
    voteService = (VoteService) container.getComponentInstanceOfType(VoteService.class);
    voteService.vote(test, 2, "root", null);    
    newVoterList1 = createValue(test, newVoterList2, "root");        
    newVoterList2 = getVoter(test, voteService);  

    assertEquals(1, test.getProperty(voteService.VOTE_TOTAL_PROP).getLong());    
    assertEquals(2.0, test.getProperty(voteService.VOTING_RATE_PROP).getDouble());
    assertEquals(newVoterList1, newVoterList2);
           
    /*************************/
    voteService.vote(test, 3, "root", null);    
    newVoterList1 = createValue(test, newVoterList2, "root");        
    newVoterList2 = getVoter(test, voteService);  
      
    assertEquals(2, test.getProperty(voteService.VOTE_TOTAL_PROP).getLong());   
    assertEquals(2.5, test.getProperty(voteService.VOTING_RATE_PROP).getDouble());
    assertEquals(newVoterList1, newVoterList2);
        
//    /*************************/
    voteService.vote(test, 1, "john", null);   
    newVoterList1 = createValue(test, newVoterList2, "john");        
    newVoterList2 = getVoter(test, voteService);  
    
    assertEquals(3, test.getProperty(voteService.VOTE_TOTAL_PROP).getLong());   
    assertEquals(2.0, test.getProperty(voteService.VOTING_RATE_PROP).getDouble());
    assertEquals(newVoterList1, newVoterList2);
        
    /*************************/
    voteService.vote(test, 4, "john", null);  
    newVoterList1 = createValue(test, newVoterList2, "john");        
    newVoterList2 = getVoter(test, voteService);  
    
    assertEquals(4, test.getProperty(voteService.VOTE_TOTAL_PROP).getLong()); 
    assertEquals(2.5, test.getProperty(voteService.VOTING_RATE_PROP).getDouble());    
    assertEquals(newVoterList1, newVoterList2);
    
    /*************************/
    //anonymous vote
    
    voteService.vote(test, 3, null, null);  
    newVoterList1 = createValue(test, newVoterList2, null);        
    newVoterList2 = getVoter(test, voteService);  
    
    assertEquals(5, test.getProperty(voteService.VOTE_TOTAL_PROP).getLong());  
    assertEquals(2.6, test.getProperty(voteService.VOTING_RATE_PROP).getDouble());    
    assertEquals(newVoterList1, newVoterList2);
  }
  
  private List<Value> createValue(Node node, List<Value> newVoterList, String username) throws Exception {    
    if (username != null) {
      Value newVoter = null;   
      newVoter = node.getSession().getValueFactory().createValue(username);
      newVoterList.add(newVoter) ;
    }    
    return newVoterList;
  }
  
  private List<Value> getVoter(Node node, VoteService voteService) throws ValueFormatException, PathNotFoundException, RepositoryException {
    Value[] voters = {} ;
    voters = node.getProperty(voteService.VOTER_PROP).getValues();    
    List<Value> newVoterList2 = new ArrayList<Value>() ;
    newVoterList2.addAll(Arrays.<Value>asList(voters)) ;
    return newVoterList2; 
  }
}
