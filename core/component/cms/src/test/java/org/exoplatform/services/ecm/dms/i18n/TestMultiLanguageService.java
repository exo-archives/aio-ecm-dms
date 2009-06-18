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
package org.exoplatform.services.ecm.dms.i18n;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Value;

import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.i18n.MultiLanguageService;
import org.exoplatform.services.ecm.dms.BaseDMSTestCase;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;

/**
 * Created by The eXo Platform SARL
 * Author : Hoang Van Hung
 *          hunghvit@gmail.com
 * Jun 17, 2009  
 */
public class TestMultiLanguageService extends BaseDMSTestCase {
  
  private static final String I18NMixin = "mix:i18n";

  private static final String VOTEABLE = "mix:votable";
  
  private static final String ARTICLE = "exo:article";

  private static final String PODCAST = "exo:podcast";

  private static final String FILE = "nt:file";

  private static final String RESOURCE = "nt:resource";

  private static final String TITLE = "exo:title";

  private static final String SUMMARY = "exo:summary";
  
  private static final String TEXT = "exo:text";
  
  private static final String CONTENT = "jcr:content";
  
  private static final String MIMETYPE = "jcr:mimeType";

  private static final String LASTMODIFIED = "jcr:lastModified";

  private static final String DATA = "jcr:data";
  
  private static final String LINK = "exo:link";

  
  
  private MultiLanguageService multiLanguageService;
  
  public void setUp() throws Exception {
    super.setUp();
    multiLanguageService = (MultiLanguageService) container.getComponentInstanceOfType(MultiLanguageService.class);
  }
  
  //Test method MultiLanguagetService.getSupportedLanguages()
  public void testGetSupportedLanguages() throws Exception {
    Node test = session.getRootNode().addNode("test", ARTICLE);
    test.addMixin(I18NMixin);
    test.setProperty(TITLE, "sport");
    test.setProperty(SUMMARY, "report of season");
    test.setProperty(TEXT, "sport is exciting");
    session.save();
    multiLanguageService.addLanguage(test, createMapInput1(), "vi", false);
    List<String> lstLanguages = multiLanguageService.getSupportedLanguages(test);
    assertTrue(lstLanguages.contains("English"));
    assertTrue(lstLanguages.contains("vi"));
    
  }
  
  private Map<String, JcrInputProperty>  createMapInput1() {
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

  private Map<String, JcrInputProperty>  createMapInput2() {
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
    inputProperty.setValue("football");
    map.put(titlePath, inputProperty);
    
    inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(summaryPath);
    inputProperty.setValue("report of season");
    map.put(summaryPath, inputProperty);
    
    inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(textPath);
    inputProperty.setValue("standing table");
    map.put(textPath, inputProperty);
    return map;
  }
  
  // Create binary data
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
  
  //Add mixin for node
  private Map<String, JcrInputProperty> createPodcastMapInput() throws IOException {
    Map<String, JcrInputProperty> map = new HashMap<String, JcrInputProperty>();
    String titlePath = CmsService.NODE + "/" + TITLE;
    String linkPath = CmsService.NODE + "/" + LINK;
    String data = CmsService.NODE + "/" + CONTENT + "/" + DATA;
    String mimeType = CmsService.NODE + "/" + CONTENT + "/" + MIMETYPE;
    String lastModified = CmsService.NODE + "/" + CONTENT + "/" + LASTMODIFIED;
    
    JcrInputProperty inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(titlePath);
    inputProperty.setValue("this is podcast");
    map.put(titlePath, inputProperty);

    inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(linkPath);
    inputProperty.setValue("connect");
    map.put(linkPath, inputProperty);
    
    inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(data);
    inputProperty.setValue(getClass().getResource("/conf/standalone/system-configuration.xml").openStream());
    map.put(data, inputProperty);
    
    inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(mimeType);
    inputProperty.setValue("text/xml");
    map.put(mimeType, inputProperty);

    inputProperty = new JcrInputProperty();
    inputProperty.setJcrPath(lastModified);
    inputProperty.setValue(new GregorianCalendar());
    map.put(lastModified, inputProperty);
    
    return map;
  }

  //Test method MultiLanguagetService.addLanguage(Node node, Map inputs, String language, boolean isDefault, String nodeType)
  public void testAddLanguage1() throws Exception {
    Node test = session.getRootNode().addNode("test", ARTICLE);
    test.addMixin(I18NMixin);
    test.setProperty(TITLE, "sport");
    test.setProperty(SUMMARY, "report of season");
    test.setProperty(TEXT, "sport is exciting");
    session.save();
   
    multiLanguageService.addLanguage(test, createMapInput1(), "fr", false);
    assertTrue(test.hasNode("languages/fr"));
    Node testlanguage = test.getNode("languages/fr");
    assertEquals("this is title", testlanguage.getProperty(TITLE).getString());
    assertEquals("this is summary", testlanguage.getProperty(SUMMARY).getString());
    assertEquals("this is article content", testlanguage.getProperty(TEXT).getString());
    assertEquals("sport", test.getProperty(TITLE).getString());
    assertEquals("report of season", test.getProperty(SUMMARY).getString());
    assertEquals("sport is exciting", test.getProperty(TEXT).getString());
  }
  
  
  //Test method MultiLanguagetService.addLanguage(Node node, Map inputs, String language, boolean isDefault)
  public void testAddLanguage2() throws Exception {
    Node test = session.getRootNode().addNode("test", ARTICLE);
    test.addMixin(I18NMixin);
    test.setProperty(TITLE, "sport");
    test.setProperty(SUMMARY, "supporter");
    test.setProperty(TEXT, "sport is exciting");
    session.save();
   
    multiLanguageService.addLanguage(test, createMapInput1(), "fr", false);
    assertTrue(test.hasNode("languages/fr"));
    Node testlanguage = test.getNode("languages/fr");
    assertEquals("this is title", testlanguage.getProperty(TITLE).getString());
    assertEquals("this is summary", testlanguage.getProperty(SUMMARY).getString());
    assertEquals("this is article content", testlanguage.getProperty(TEXT).getString());
    assertEquals("sport", test.getProperty(TITLE).getString());
    assertEquals("supporter", test.getProperty(SUMMARY).getString());
    assertEquals("sport is exciting", test.getProperty(TEXT).getString());
   
    multiLanguageService.addLanguage(test, createMapInput2(), "fr", true);
    assertEquals("fr", test.getProperty(MultiLanguageService.EXO_LANGUAGE).getString());
    assertFalse(test.hasNode("languages/fr"));
    assertEquals("football", test.getProperty(TITLE).getString());
    assertEquals("report of season", test.getProperty(SUMMARY).getString());
    assertEquals("standing table", test.getProperty(TEXT).getString());
  }

  //Test method MultiLanguagetService.addLanguage(Node node, Map inputs, String language, boolean isDefault)
  public void testAddLanguage3() throws Exception {
    Node test = session.getRootNode().addNode("test", FILE);
    Node testFile = test.addNode(CONTENT, RESOURCE);
    testFile.setProperty(DATA, getClass().getResource("/conf/standalone/system-configuration.xml").openStream());
    testFile.setProperty(MIMETYPE, "text/xml");
    testFile.setProperty(LASTMODIFIED, new GregorianCalendar());
    test.addMixin(I18NMixin);
    session.save();

    multiLanguageService.addLanguage(test, createFileInput(), "fr", false, "jcr:content");
    String defaultLanguage = test.getProperty(MultiLanguageService.EXO_LANGUAGE).getString();
    assertEquals("English", defaultLanguage);
    assertTrue(test.hasNode("languages/fr"));
    Node testlanguage = test.getNode("languages/fr");
    assertTrue(testlanguage.hasNode(CONTENT));
    assertTrue(compareInputStream(getClass().getResource("/conf/standalone/system-configuration.xml").openStream(), testlanguage.getNode(CONTENT).getProperty(DATA).getStream()));
    
    multiLanguageService.addLanguage(test, createFileInput(), "vi", true, CONTENT);
    defaultLanguage = test.getProperty(MultiLanguageService.EXO_LANGUAGE).getString();
    assertEquals("vi", defaultLanguage);
    assertTrue(test.hasNode(CONTENT));
    assertTrue(compareInputStream(getClass().getResource("/conf/standalone/system-configuration.xml").openStream(), test.getNode(CONTENT).getProperty(DATA).getStream()));
  }
  
  //Test method MultiLanguagetService.setDefault()
  public void testSetDefault() throws Exception {
    Node test = session.getRootNode().addNode("test", ARTICLE);
    test.addMixin(I18NMixin);
    test.setProperty(TITLE, "sport");
    test.setProperty(TEXT, "sport is exciting");
    session.save();
    multiLanguageService.addLanguage(test, createMapInput1(), "fr", false);
    assertTrue(test.hasNode("languages/fr"));
    Node testlanguage = test.getNode("languages/fr");
    assertEquals("this is title", testlanguage.getProperty(TITLE).getString());
    assertEquals("this is summary", testlanguage.getProperty(SUMMARY).getString());
    assertEquals("this is article content", testlanguage.getProperty(TEXT).getString());
    multiLanguageService.setDefault(test, "fr", REPO_NAME);
    String defaultLanguage = test.getProperty(MultiLanguageService.EXO_LANGUAGE).getString();
    assertEquals("fr", defaultLanguage);
    assertFalse(test.hasNode("languages/fr"));
  }

  //Test method MultiLanguagetService.getDefault()
  public void testGetDefault() throws Exception {
    Node test = session.getRootNode().addNode("test", ARTICLE);
    test.setProperty(TITLE, "Document");
    test.addMixin(I18NMixin);
    session.save();
    String defaultLanguage = multiLanguageService.getDefault(test);
    assertEquals("English", defaultLanguage);
    multiLanguageService.addLanguage(test, createMapInput1(), "fr", false);
    multiLanguageService.setDefault(test, "fr", REPO_NAME);
    assertEquals("fr", multiLanguageService.getDefault(test));
  }
  
  //Test method MultiLanguagetService.addFileLanguage(Node node, String fileName, Value value, String mimeType, String language, String repositoryName, boolean isDefault)
  public void testAddFileLanguage1() throws Exception {
    Node test = session.getRootNode().addNode("test", FILE);
    Node testFile = test.addNode(CONTENT, RESOURCE);
    testFile.setProperty(DATA, getClass().getResource("/conf/standalone/test-configuration.xml").openStream());
    testFile.setProperty(MIMETYPE, "text/xml");
    testFile.setProperty(LASTMODIFIED, new GregorianCalendar());
    test.addMixin(I18NMixin);
    test.addMixin(VOTEABLE);
    
    session.save();
    ValueFactoryImpl valueFactory = session.getValueFactory();
    InputStream is = getClass().getResource("/conf/standalone/system-configuration.xml").openStream();
    Value contentValue = valueFactory.createValue(is);
    
    multiLanguageService.addFileLanguage(test, "system-configuration.xml" , contentValue, "text/xml", "fr", REPO_NAME, false);
    String defaultLanguage = test.getProperty(MultiLanguageService.EXO_LANGUAGE).getString();
    assertEquals("English", defaultLanguage);
    Node testlanguage = test.getNode("languages/fr/system-configuration.xml");
    is = getClass().getResource("/conf/standalone/system-configuration.xml").openStream();
    assertTrue(compareInputStream(getClass().getResource("/conf/standalone/system-configuration.xml").openStream(), testlanguage.getNode(CONTENT).getProperty(DATA).getStream()));

    Value contentValue1 = valueFactory.createValue(getClass().getResource("/conf/standalone/test-configuration.xml").openStream());
    multiLanguageService.addFileLanguage(test, "test-configuration.xml" , contentValue1, "text/xml", "vi", REPO_NAME, true);
    defaultLanguage = test.getProperty(MultiLanguageService.EXO_LANGUAGE).getString();
    assertEquals("vi", defaultLanguage);
    assertTrue(test.hasNode("languages/English/test/jcr:content"));
    assertTrue(compareInputStream(getClass().getResource("/conf/standalone/test-configuration.xml").openStream(), test.getNode("languages/English/test/jcr:content").getProperty(DATA).getStream()));
    assertTrue(compareInputStream(getClass().getResource("/conf/standalone/test-configuration.xml").openStream(), test.getNode(CONTENT).getProperty(DATA).getStream()));
    
  }

  //Test method MultiLanguagetService.addFileLanguage(Node node, String language, Map mappings, boolean isDefault)
  public void testAddFileLanguage2() throws Exception {
    Node test = session.getRootNode().addNode("test", PODCAST);
    Node testFile = test.addNode(CONTENT, RESOURCE);
    testFile.setProperty(MIMETYPE, "text/xml");
    testFile.setProperty(LASTMODIFIED, new GregorianCalendar());
    testFile.setProperty(DATA, getClass().getResource("/conf/standalone/system-configuration.xml").openStream());
    test.addMixin(I18NMixin);
    session.save();
    
    multiLanguageService.addFileLanguage(test, "fr" , createPodcastMapInput(), false);
    String defaultLanguage = test.getProperty(MultiLanguageService.EXO_LANGUAGE).getString();
    assertEquals("English", defaultLanguage);
    assertTrue(test.hasNode("languages/fr"));
    Node testlanguage = test.getNode("languages/fr");
    assertTrue(compareInputStream(getClass().getResource("/conf/standalone/system-configuration.xml").openStream(), testlanguage.getNode(CONTENT).getProperty(DATA).getStream()));
    assertEquals("this is podcast", testlanguage.getProperty(TITLE).getString());
    assertEquals("connect", testlanguage.getProperty(LINK).getString());
    assertTrue(compareInputStream(getClass().getResource("/conf/standalone/system-configuration.xml").openStream(), testlanguage.getNode(CONTENT).getProperty(DATA).getStream()));
    
    multiLanguageService.addFileLanguage(test, "vi" , createPodcastMapInput(), true);
    defaultLanguage = test.getProperty(MultiLanguageService.EXO_LANGUAGE).getString();
    assertEquals("vi", defaultLanguage);
    assertTrue(compareInputStream(getClass().getResource("/conf/standalone/system-configuration.xml").openStream(), test.getNode(CONTENT).getProperty(DATA).getStream()));
    assertEquals("this is podcast", test.getProperty(TITLE).getString());
    assertEquals("connect", test.getProperty(LINK).getString());
    assertTrue(compareInputStream(getClass().getResource("/conf/standalone/system-configuration.xml").openStream(), test.getNode(CONTENT).getProperty(DATA).getStream()));
  }

  
  /*
  //Get language node by language
  public void testGetLanguage() throws Exception {
    Node test = session.getRootNode().addNode("test", PODCAST);
    Node testFile = test.addNode(CONTENT, RESOURCE);
    testFile.setProperty(MIMETYPE, "text/xml");
    testFile.setProperty(LASTMODIFIED, new GregorianCalendar());
    testFile.setProperty(DATA, getClass().getResource("/conf/standalone/test-configuration.xml").openStream());
    test.addMixin(I18NMixin);
    session.save();
    multiLanguageService.addLanguage(test, createPodcastMapInput(), "fr", false);
    assertTrue(test.hasNode("languages/fr"));
    Node node = multiLanguageService.getLanguage(test, "fr");
    assertEquals("this is podcast", node.getProperty(TITLE).getString());
    assertEquals("connect", node.getProperty(LINK).getString());
    multiLanguageService.addLanguage(test, createPodcastMapInput(), "fr", true);
    assertNull(multiLanguageService.getLanguage(test, "fr"));
  }*/
  
  //Compare two input stream
  private boolean compareInputStream(InputStream is1, InputStream is2) throws IOException {
    int b1, b2;
    do {
      b1 = is1.read();
      b2 = is2.read();
      if (b1 != b2) return false;
    } while ((b1 !=-1) && (b2!=-1));
    return true;
  }

  String convertStreamToString(InputStream is) {
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    StringBuilder sb = new StringBuilder();
    String line = null;
    try {
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    return sb.toString();
}
  //Clean data test
  public void tearDown() throws Exception {
    if (session.itemExists("/test")) {
      Node test = session.getRootNode().getNode("test");
      test.remove();
      session.save();
    }
    super.tearDown();
  }

}
