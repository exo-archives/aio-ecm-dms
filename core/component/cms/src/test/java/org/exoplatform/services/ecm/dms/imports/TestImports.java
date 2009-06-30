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
package org.exoplatform.services.ecm.dms.imports;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.version.Version;

import org.exoplatform.services.ecm.dms.BaseDMSTestCase;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.util.VersionHistoryImporter;

/**
 * Created by The eXo Platform SARL
 * June 09, 2009  
 */
public class TestImports extends BaseDMSTestCase {
  
  List<String> versionList = new ArrayList<String>();
  
  public void setUp() throws Exception {
    super.setUp();
  }
  
  public void testExportsAndImports() throws Exception {
    Node root = session.getRootNode();
    Node aaa = root.addNode("AAA");    
    Node bbb = root.addNode("BBB");
    Node ccc = root.addNode("CCC");
    session.save();
    
    // Export Action
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    aaa.getSession().exportDocumentView(aaa.getPath(), bos, false, false);
    ByteArrayInputStream is = new ByteArrayInputStream(bos.toByteArray());
    bbb.getSession().importXML(bbb.getPath(), is, 1);
    session.save();
    
    String[] paths = new String[] {"AAA", "BBB", "BBB/AAA"};
    for (String path : paths) {
      if (bbb.hasNode(path)) {
      }
    }
    
    Node hello = aaa.addNode("hello", "exo:article");
    hello.setProperty("exo:title", "hello");
    hello.addMixin("mix:versionable");
    session.save();
    
    Version version = hello.checkin();
    hello.checkout();
    
    /*
    hello.addMixin("publication:staticAndDirectPublication");
    hello.addMixin("exo:privilegeable");    
    Value[] predecessors = hello.getProperty("jcr:predecessors").getValues();
    StringBuilder predecessorsBuilder = new StringBuilder();
    for(Value value : predecessors) {
      if(predecessorsBuilder.length() > 0) predecessorsBuilder.append(",") ;
      predecessorsBuilder.append(value.getString());
    }        
    hello.setProperty("publication:lifecycleName", "StaticAndDirect");
    hello.setProperty("publication:currentState", "enrolled");
    hello.setProperty("publication:visibility", "public");    
    String newStringValue = version.getUUID() + ",published";
    Value value2add = session.getValueFactory().createValue(newStringValue);
    Value[] values = {value2add};    
    hello.setProperty("publication:versionsPublicationStates", values);    
    String stringValue = "20090629.162900.640,non published,root,PublicationService.StaticAndDirectPublicationPlugin.nodeCreated,1,private";
    Value valuexadd = session.getValueFactory().createValue(stringValue);
    Value[] historyValues = {valuexadd};
    hello.setProperty("publication:history", historyValues);
    */
    session.save();
    
    /**
     * Before import this node has one version
     */
    Version rootVersion = hello.getVersionHistory().getRootVersion();    
    getListVersion(rootVersion);
    assertEquals(1, versionList.size());
    
    // Export VersionHistory
    InputStream inputVersion = null;
    if(hello.isNodeType("mix:versionable")) {
      ByteArrayOutputStream bosVersion = new ByteArrayOutputStream();
      hello.getSession().exportDocumentView(hello.getVersionHistory().getPath(), bosVersion, false, false);      
      inputVersion = new ByteArrayInputStream(bosVersion.toByteArray());
    }
    String versionHistory = hello.getProperty("jcr:versionHistory").getValue().getString();
    String baseVersion = hello.getProperty("jcr:baseVersion").getValue().getString();
    Value[] jcrPredecessors = hello.getProperty("jcr:predecessors").getValues();
    String[] predecessorsHistory; 
    StringBuilder jcrPredecessorsBuilder = new StringBuilder();
    for(Value value : jcrPredecessors) {
      if(jcrPredecessorsBuilder.length() > 0) jcrPredecessorsBuilder.append(",") ;
      jcrPredecessorsBuilder.append(value.getString());
    }
    if(jcrPredecessorsBuilder.toString().indexOf(",") > -1) {
      predecessorsHistory = jcrPredecessorsBuilder.toString().split(",");
    } else {
      predecessorsHistory = new String[] { jcrPredecessorsBuilder.toString() };
    }
    
    // Export Action
    ByteArrayOutputStream bosHello = new ByteArrayOutputStream();
    hello.getSession().exportDocumentView(hello.getPath(), bosHello, false, false);
    ByteArrayInputStream isHello = new ByteArrayInputStream(bosHello.toByteArray());
    ccc.getSession().importXML(ccc.getPath(), isHello, 1);
    session.save();
        
    /**
     * Import VersionHistory
     * After import version history, the node has no version
     * Errors: Lose version when import version history
     */ 
    Node helloImport = (Node) session.getItem("/CCC/hello");
    importHistory((NodeImpl)helloImport, inputVersion, baseVersion, predecessorsHistory, versionHistory);
    versionList.clear();
    Version rootVersionImport = helloImport.getVersionHistory().getRootVersion();    
    getListVersion(rootVersionImport);
    assertEquals(0, versionList.size());
  }
  
  public void getListVersion(Version version) {
    try {      
      String uuid = version.getUUID();
      QueryManager queryManager = session.getWorkspace().getQueryManager();
      Query query = queryManager.createQuery("//element(*, nt:version)[@jcr:predecessors='" + uuid + "']", Query.XPATH);
      QueryResult queryResult = query.execute();
      NodeIterator iterate = queryResult.getNodes();
      while (iterate.hasNext()) {
        Version version1 = (Version) iterate.nextNode();
        versionList.add(version1.getUUID());
        getListVersion(version1);
      }
    } catch (Exception e) {
    }
  }  
  
  private void importHistory(
      NodeImpl versionableNode, 
      InputStream versionHistoryStream, 
      String baseVersionUuid, 
      String[] predecessors, 
      String versionHistory) throws RepositoryException, IOException {
    VersionHistoryImporter versionHistoryImporter = 
      new VersionHistoryImporter(versionableNode, versionHistoryStream, baseVersionUuid, predecessors, versionHistory);
    versionHistoryImporter.doImport();
  }
}
