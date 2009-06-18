/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
 *
 **************************************************************************/
package org.exoplatform.services.ecm.dms.cms.voting;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Value;

import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.i18n.MultiLanguageService;
import org.exoplatform.services.cms.voting.VotingService;
import org.exoplatform.services.ecm.dms.BaseDMSTestCase;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Created by eXo Platform
 * Author : Nguyen Manh Cuong
 *          manhcuongpt@gmail.com
 * Jun 17, 2009  
 */

/**
 * Unit test for VotingService
 * Methods need to test
 * 1. Vote method
 * 2. Get Vote Total method
 */
public class TestVotingService extends BaseDMSTestCase {
  
  private final static String I18NMixin = "mix:i18n";

  private final static String VOTEABLE = "mix:votable";
  
  private final static String VOTER_PROP = "exo:voter".intern();
  
  private final static String VOTE_TOTAL_PROP = "exo:voteTotal".intern();
  
  private final static String VOTING_RATE_PROP = "exo:votingRate".intern();

  private final static String VOTE_TOTAL_LANG_PROP = "exo:voteTotalOfLang".intern();  
  
  private final static String ARTICLE = "exo:article";
  
  private final static String CONTENT = "jcr:content";
  
  private final static String MIMETYPE = "jcr:mimeType";  
  
  private final static String DATA = "jcr:data";
  
  private final static String LASTMODIFIED = "jcr:lastModified";
  
  private final static String FILE = "nt:file";
  
  private final static String RESOURCE = "nt:resource";
  
  private final static String TITLE = "exo:title";

  private final static String SUMMARY = "exo:summary";

  private final static String TEXT = "exo:text";

  private VotingService votingService = null;

  private MultiLanguageService multiLanguageService = null;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    votingService = (VotingService) container.getComponentInstanceOfType(VotingService.class);
    multiLanguageService = (MultiLanguageService) container.getComponentInstanceOfType(MultiLanguageService.class);
  }
  
  /**
   * Test Method: vote()
   * Input: Test node is set English default language, but not set MultiLanguage. 
   *        Voter: root, rate: 3.0, voter's language is default language.
   * Expected:
   *        Value of VOTER_PROP property of test node contains "root"
   *        Vote total of test with default language = 1
   *        rate = 3.0
   */
  @SuppressWarnings("unchecked")
  public void testVote() throws Exception {
    Node test = session.getRootNode().addNode("Test");
    if (test.canAddMixin(I18NMixin)) {
      test.addMixin(I18NMixin);
    }
    session.save();
    votingService.vote(test, 3, "root", multiLanguageService.getDefault(test));
    List voters = Arrays.asList(new String[] {"root"});
    Value[] value = test.getProperty(VOTER_PROP).getValues();
    for (Value val : value) {
      assertTrue(voters.contains(val.getString()));
    }
    assertEquals(1, test.getProperty(VOTE_TOTAL_LANG_PROP).getValue().getLong());
    assertEquals(3.0, test.getProperty(VOTING_RATE_PROP).getValue().getDouble());
  }
  
  /**
   * Test Method: vote()
   * Input: Test node is set English default language, but not set MultiLanguage 
   *        Voter's language is not default language
   * Expected: throws exception
   */  
  public void testVote1() throws Exception{
    Node test = session.getRootNode().addNode("Test");
    if (test.canAddMixin(I18NMixin)) {
      test.addMixin(I18NMixin);
    }
    session.save();
    Exception e = null;
    try {
      votingService.vote(test, 3, "root", "fr");
    } catch (NullPointerException ex) {
      e = ex;
    }
    assertNotNull(e);
  }
  
  /**
   * Test Method: vote()
   * Input: test node is set English default language.
   *        adding vote for test node by French
   *        userName = null
   *        rate = 3 
   * Expected:
   *        user is not voter, value of VOTER_PRO doesn't exist.
   *        rate = 3.0
   *        total of vote: 1.
   */
  public void testVote2() throws Exception {
    Node test = initNode();
    votingService.vote(test, 3, null, "fr");
    Node fr = multiLanguageService.getLanguage(test, "fr");
    
    assertEquals(0, fr.getProperty(VOTER_PROP).getValues().length);
    assertEquals(3.0, fr.getProperty(VOTING_RATE_PROP).getValue().getDouble());
    assertEquals(1, fr.getProperty(VOTE_TOTAL_LANG_PROP).getValue().getLong());
  }  
  
  /**
   * Test Method: vote()
   * Input: Test node is set default language not equals voter's language
   *        In this case: voter's language is French
   *        Example userName: root, rate: 3, there is only "root" to vote
   * Expected:
   *        Voter that uses French is "root"
   *        Total of vote of French is 1
   *        Rating is 3
   */
  @SuppressWarnings("unchecked")
  public void testVote3() throws Exception {
    Node test = initNode();
    votingService.vote(test, 3, "root", "fr");
    Node fr = multiLanguageService.getLanguage(test, "fr");
    List voters = Arrays.asList(new String[] { "root" });
    Property voterProperty = fr.getProperty(VOTER_PROP);
    Value[] value = voterProperty.getValues();
    for (Value val : value) {
      assertTrue(voters.contains(val.getString()));
    }
    assertEquals(3.0, fr.getProperty(VOTING_RATE_PROP).getValue().getDouble());
    assertEquals(1, fr.getProperty(VOTE_TOTAL_LANG_PROP).getValue().getLong());
  }
  
  /**
   * Test Method: vote()
   * Input: Test node is set default language and is not equals voter's language.
   *        Voter: root, rate: 3.0, language French
   * Expected: 
   *        Each language add "jcr:contest" node and their data is equals data of "jcr:content" of test node.
   *        Voter who uses French is "root"
   *        Total of vote of French is 1
   *        Rating = 3.0
   */
  @SuppressWarnings("unchecked")
  public void testVote4() throws Exception{
    Node test = session.getRootNode().addNode("test", FILE);
    Node testFile = test.addNode(CONTENT, RESOURCE);
    testFile.setProperty(DATA, getClass().getResource("/conf/standalone/system-configuration.xml").openStream());
    testFile.setProperty(MIMETYPE, "text/xml");
    testFile.setProperty(LASTMODIFIED, new GregorianCalendar());
    if (test.canAddMixin(I18NMixin)) {
      test.addMixin(I18NMixin);
    }
    if (test.canAddMixin(VOTEABLE)) {
      test.addMixin(VOTEABLE);
    }
    session.save();
    multiLanguageService.addLanguage(test, createFileInput(), "fr", false, "jcr:content");
    multiLanguageService.addLanguage(test, createFileInput(), "en", false, "jcr:content");
    multiLanguageService.addLanguage(test, createFileInput(), "vi", false, "jcr:content");
    votingService.vote(test, 3, "root", "fr");
    Node viLangNode = multiLanguageService.getLanguage(test, "vi");
    Node enLangNode = multiLanguageService.getLanguage(test, "en");
    Node frLangNode = multiLanguageService.getLanguage(test, "fr");
    List voters = Arrays.asList(new String[] { "root" });
    Property voterProperty = frLangNode.getProperty(VOTER_PROP);
    Value[] value = voterProperty.getValues();
    for (Value val : value) {
      assertTrue(voters.contains(val.getString()));
    }
    assertEquals(testFile.getProperty(MIMETYPE).getString(), frLangNode.getNode(CONTENT).getProperty(MIMETYPE).getString());
    assertEquals(testFile.getProperty(DATA).getValue(), frLangNode.getNode(CONTENT).getProperty(DATA).getValue());
    assertEquals(testFile.getProperty(MIMETYPE).getString(), viLangNode.getNode(CONTENT).getProperty(MIMETYPE).getString());
    assertEquals(testFile.getProperty(DATA).getValue(), viLangNode.getNode(CONTENT).getProperty(DATA).getValue());
    assertEquals(testFile.getProperty(MIMETYPE).getString(), enLangNode.getNode(CONTENT).getProperty(MIMETYPE).getString());
    assertEquals(testFile.getProperty(DATA).getValue(), enLangNode.getNode(CONTENT).getProperty(DATA).getValue());
    assertEquals(3.0, frLangNode.getProperty(VOTING_RATE_PROP).getValue().getDouble());
    assertEquals(1, frLangNode.getProperty(VOTE_TOTAL_LANG_PROP).getValue().getLong());
  }
  
  /**
   * Test Method: vote()
   * Input: test node is set default language
   *        voter's language is null
   * Expected: throws Exception
   */
  public void testVote5() throws Exception {
    Exception e = null;
    try {
      Node test = initNode();
      multiLanguageService.setDefault(test, "en", REPO_NAME);
      votingService.vote(test, 3, "root", null);
    } catch (Exception ex) {
      e = ex;
    }
    assertNotNull(e);
  }   
  
  /**
   * Test Method: vote()
   * Input: Test node is not set default language
   * Expected: throws Exception
   */  
  public void testVote6() throws Exception{
    Exception e = null;
    try {
      Node test = session.getRootNode().addNode("Test");
      session.save();
      votingService.vote(test, 3, "root", "fr");
    } catch (Exception ex) {
      e = ex;
    }
    assertNotNull(e);
  }

  /**
   * Test Method: getVoteTotal()
   * Input: Test node is set English default language and doesn't have MultiLanguage
   *        Voter's language equals default language.
   * Expected: 
   *        Total of test's vote = value of VOTE_TOTAL_LANG_PROP property. 
   */
  public void testGetVoteTotal() throws Exception{
    Node test = session.getRootNode().addNode("Test");
    if (test.canAddMixin(I18NMixin)) {
      test.addMixin(I18NMixin);
    }
    session.save();
    votingService.vote(test, 3, "root", multiLanguageService.getDefault(test));
    long voteTotal = votingService.getVoteTotal(test);
    assertEquals(voteTotal, test.getProperty(VOTE_TOTAL_LANG_PROP).getValue().getLong());
  }
  
  /**
   * Test Method: getVoteTotal()
   * Input: test node is set English default language and has MultiLanguage
   *        test node is voted 4 times: root votes 2 times using English
   *                                    john votes 1 times using English
   *                                    marry votes 2 times using both French and Vi
   * Expected:
   *       Total of votes of test node = value of VOTE_TOTAL_PROP property of test node.
   *       In this case: total = 5.
   */
  public void testGetVoteTotal1() throws Exception{
    Node test1 = initNode();
    String DefaultLang = multiLanguageService.getDefault(test1);
    votingService.vote(test1, 3, "root", DefaultLang);
    votingService.vote(test1, 3, "john", DefaultLang);
    votingService.vote(test1, 3, "marry", "en");
    votingService.vote(test1, 3, "marry", "fr");
    votingService.vote(test1, 3, "marry", "vi");
    long voteTotal = votingService.getVoteTotal(test1);
    assertNull(multiLanguageService.getLanguage(test1, multiLanguageService.getDefault(test1)));
    assertEquals(voteTotal, test1.getProperty(VOTE_TOTAL_PROP).getValue().getLong());
    assertEquals(2, test1.getProperty(VOTE_TOTAL_LANG_PROP).getValue().getLong());
  }
  
  /**
   * Create a map to use for MultilLanguageService
   */
  private Map<String, JcrInputProperty>  createMapInput() {
    Map<String, JcrInputProperty> map = new HashMap<String, JcrInputProperty>();
    String titlePath = CmsService.NODE + "/" + TITLE;
    String summaryPath = CmsService.NODE + "/" + SUMMARY;
    String textPath = CmsService.NODE + "/" + TEXT;
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(CmsService.NODE);

    inputProperty.setValue("test");
    map.put(CmsService.NODE, inputProperty);

    inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(titlePath);
    inputProperty.setValue("this is title");
    map.put(titlePath, inputProperty);

    inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(summaryPath);
    inputProperty.setValue("this is summary");
    map.put(summaryPath, inputProperty);

    inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(textPath);
    inputProperty.setValue("this is article content");
    map.put(textPath, inputProperty);
    return map;
  }  
  
  /**
   * Create binary data
   */
  private Map<String, JcrInputProperty> createFileInput() throws IOException {
    Map<String, JcrInputProperty> map = new HashMap<String, JcrInputProperty>();
    String data = CmsService.NODE + "/" + CONTENT + "/" + DATA;
    String mimeType = CmsService.NODE + "/" + CONTENT + "/" + MIMETYPE;
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(data);
    inputProperty.setValue(getClass().getResource("/conf/standalone/system-configuration.xml").openStream());
    map.put(data, inputProperty);
    inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(mimeType);
    inputProperty.setValue("text/xml");
    map.put(mimeType, inputProperty);
    return map;
  }  
  
  
  /**
   * This method will create a node which is added MultiLanguage
   */
  private Node initNode() throws Exception{
    Node test = session.getRootNode().addNode("test", ARTICLE);
    if (test.canAddMixin(I18NMixin)) {
      test.addMixin(I18NMixin);
    }
    if (test.canAddMixin(VOTEABLE)) {
      test.addMixin(VOTEABLE);
    }
    test.setProperty(TITLE, "sport");
    test.setProperty(SUMMARY, "report of season");
    test.setProperty(TEXT, "sport is exciting");
    session.save();
    multiLanguageService.addLanguage(test, createMapInput(), "en", false);
    multiLanguageService.addLanguage(test, createMapInput(), "vi", false);
    multiLanguageService.addLanguage(test, createMapInput(), "fr", false);
    return test;
  }
    
}
