/*
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
 */
package org.exoplatform.services.ecm.dms.records;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.services.cms.records.RecordsService;
import org.exoplatform.services.ecm.dms.BaseDMSTestCase;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

/**
 * Created by The eXo Platform SARL Author : Ly Dinh Quang
 * quang.ly@exoplatform.com xxx5669@gmail.com Jun 16, 2009
 */
public class TestRecordsService extends BaseDMSTestCase {
  private RecordsService       recordsService;

  private Session              session;

  private NodeHierarchyCreator nodeHierarchyCreator;

  private Node                 rootNode;

  public void setUp() throws Exception {
    super.setUp();
    recordsService = (RecordsService) container.getComponentInstanceOfType(RecordsService.class);
    nodeHierarchyCreator = (NodeHierarchyCreator) container
        .getComponentInstanceOfType(NodeHierarchyCreator.class);
    createTree();
  }

  public void createTree() throws Exception {
    session = repository.login(credentials, COLLABORATION_WS);
    rootNode = session.getRootNode();
    Node testNode = rootNode.addNode("TestTreeNode");

    Node nodeA1 = testNode.addNode("A1", "nt:file");
    Node contentA1 = nodeA1.addNode("jcr:content", "nt:resource");
    contentA1.setProperty("jcr:lastModified", Calendar.getInstance());
    contentA1.setProperty("jcr:mimeType", "text/xml");
    contentA1.setProperty("jcr:data", "");

    Node nodeA2 = testNode.addNode("A2", "nt:file");
    Node contentA2 = nodeA2.addNode("jcr:content", "nt:resource");
    contentA2.setProperty("jcr:lastModified", Calendar.getInstance());
    contentA2.setProperty("jcr:mimeType", "text/xml");
    contentA2.setProperty("jcr:data", "");

    addNodeFilePlan("A3", testNode, "cateIdentify1", "disposition1", true, true, "mediaType1",
        "markingList1", "original1", true, false, "trigger1", false, false, false, false, "hourly");

    Node nodeA4 = addNodeFilePlan("A4", testNode, "cateIdentify2", "disposition2", true, true, "mediaType2",
        "markingList2", "original2", true, true, "trigger2", false, false, false, false,
        "quarterly");
    nodeA4.setProperty("rma:cutoffPeriod", "hourly");
    nodeA4.setProperty("rma:cutoffOnObsolete", true);
    nodeA4.setProperty("rma:cutoffOnSuperseded", false);
    session.save();
  }

  private Node addNodeFilePlan(String nodeName, Node parent, String cateIdentify,
      String disposition, boolean permanentRecord, boolean recordFolder, String mediaType,
      String markingList, String original, boolean recordIndicator, boolean cutoff,
      String eventTrigger, boolean processHold, boolean processTransfer, boolean processAccession,
      boolean processDestruction, String vitalRecordReview) throws Exception {
    Node filePlan = parent.addNode(nodeName, "rma:filePlan");
    filePlan.setProperty("rma:recordCategoryIdentifier", cateIdentify);
    filePlan.setProperty("rma:dispositionAuthority", disposition);
    filePlan.setProperty("rma:permanentRecordIndicator", permanentRecord);
    filePlan.setProperty("rma:containsRecordFolders", recordFolder);
    filePlan.setProperty("rma:defaultMediaType", mediaType);
    filePlan.setProperty("rma:defaultMarkingList", markingList);
    filePlan.setProperty("rma:defaultOriginatingOrganization", original);
    filePlan.setProperty("rma:vitalRecordIndicator", recordIndicator);
    filePlan.setProperty("rma:processCutoff", cutoff);
    filePlan.setProperty("rma:eventTrigger", eventTrigger);
    filePlan.setProperty("rma:processHold", processHold);
    filePlan.setProperty("rma:processTransfer", processTransfer);
    filePlan.setProperty("rma:processAccession", processAccession);
    filePlan.setProperty("rma:processDestruction", processDestruction);
    filePlan.setProperty("rma:vitalRecordReviewPeriod", vitalRecordReview);
    return filePlan;
  }

  public void testAddRecord() throws Exception {
    Node nodeA1 = rootNode.getNode("TestTreeNode/A1");
    Node nodeA2 = rootNode.getNode("TestTreeNode/A2");
    Node nodeA3 = rootNode.getNode("TestTreeNode/A3");
    Node nodeA4 = rootNode.getNode("TestTreeNode/A4");
    
    // record o day la nodeA1, node A2
    // fileplan o day la nodeA3, nodeA4
    recordsService.addRecord(nodeA3, nodeA1);
    assertEquals(nodeA3.getProperty("rma:recordCounter").getLong(), 1);
    assertEquals(nodeA3.getProperty("rma:vitalRecordIndicator").getBoolean(), true);
    assertEquals(nodeA3.getProperty("rma:vitalRecordReviewPeriod").getString(), "hourly");
    assertEquals(nodeA3.getProperty("rma:processCutoff").getBoolean(), false);
    
    assertEquals(nodeA1.getProperty("rma:originator").getString(), "root");
    assertEquals(nodeA1.getProperty("rma:recordIdentifier").getString(), "cateIdentify1-1 A1");
    assertEquals(nodeA1.getProperty("rma:originatingOrganization").getString(), "original1");
    
    recordsService.addRecord(nodeA4, nodeA2);
    assertEquals(nodeA4.getProperty("rma:recordCounter").getLong(), 1);
    assertEquals(nodeA4.getProperty("rma:vitalRecordIndicator").getBoolean(), true);
    assertEquals(nodeA4.getProperty("rma:vitalRecordReviewPeriod").getString(), "quarterly");
    assertEquals(nodeA4.getProperty("rma:processCutoff").getBoolean(), true);
    assertEquals(nodeA4.getProperty("rma:cutoffPeriod").getString(), "hourly");
    assertEquals(nodeA4.getProperty("rma:cutoffOnObsolete").getBoolean(), true);
    assertEquals(nodeA4.getProperty("rma:cutoffOnSuperseded").getBoolean(), false);
    assertEquals(nodeA4.getProperty("rma:eventTrigger").getString(), "trigger2");
    
    assertEquals(nodeA2.getProperty("rma:originator").getString(), "root");
    assertEquals(nodeA2.getProperty("rma:recordIdentifier").getString(), "cateIdentify2-1 A2");
    assertEquals(nodeA2.getProperty("rma:originatingOrganization").getString(), "original2");
    assertEquals(nodeA2.getProperty("rma:cutoffObsolete").getBoolean(), true);
    assertEquals(nodeA2.getProperty("rma:cutoffEvent").getString(), "trigger2");
    try {
      assertEquals(nodeA2.getProperty("rma:cutoffSuperseded").getBoolean(), false);
    } catch (PathNotFoundException e) {
      fail("Property not found rma:cutoffSuperseded");
    }
  }
  
  public void testGetRecords() throws Exception {
    Node nodeA1 = rootNode.getNode("TestTreeNode/A1");
    Node nodeA2 = rootNode.getNode("TestTreeNode/A2");
    Node nodeA3 = rootNode.getNode("TestTreeNode/A3");
    Node nodeA4 = rootNode.getNode("TestTreeNode/A4");
    
    recordsService.addRecord(nodeA3, nodeA1);
    recordsService.addRecord(nodeA4, nodeA2);
    session.save();
    
    List<Node> listRecord = recordsService.getRecords(rootNode.getNode("TestTreeNode"));
    assertEquals(listRecord.size(), 2);
    assertEquals(listRecord.get(0).getName(), "A1");
    assertEquals(listRecord.get(1).getName(), "A2");
  }
  
//  public void testGetVitalRecords() throws Exception {
//    Node nodeA1 = rootNode.getNode("TestTreeNode/A1");
//    Node nodeA2 = rootNode.getNode("TestTreeNode/A2");
//    Node nodeA3 = rootNode.getNode("TestTreeNode/A3");
//    Node nodeA4 = rootNode.getNode("TestTreeNode/A4");
//    
//    recordsService.addRecord(nodeA3, nodeA1);
//    recordsService.addRecord(nodeA4, nodeA2);
//    
//    nodeA2.setProperty("rma:nextReviewDate", new GregorianCalendar());
//    nodeA1.setProperty("rma:nextReviewDate", new GregorianCalendar());
//    session.save();
//    
////    List<Node> listRecord = recordsService.getVitalRecords(rootNode.getNode("TestTreeNode"));
////    System.out.println("\n===ListRecord: " + listRecord.size());
////    assertEquals(listRecord.size(), 2);
////    assertEquals(listRecord.get(0).getName(), "A1");
////    assertEquals(listRecord.get(2).getName(), "A2");
//    
//    QueryManager queryManager = session.getWorkspace().getQueryManager();
//    String sql = null;
//    sql = "/jcr:root/TestTreeNode//element(*,rma:vitalRecord) order by exo:dateCreated descending";
//    Query query = queryManager.createQuery(sql, Query.XPATH);
//    QueryResult result = query.execute();
//    NodeIterator iterate = result.getNodes();
//    while (iterate.hasNext()) {
//      System.out.println("\nName: " + iterate.nextNode().getName() + "\n");
//    }
//  }
  
  public void testGetObsoleteRecords() throws Exception {
    Node nodeA1 = rootNode.getNode("TestTreeNode/A1");
    Node nodeA2 = rootNode.getNode("TestTreeNode/A2");
    Node nodeA3 = rootNode.getNode("TestTreeNode/A3");
    Node nodeA4 = rootNode.getNode("TestTreeNode/A4");
    
    recordsService.addRecord(nodeA3, nodeA1);
    recordsService.addRecord(nodeA4, nodeA2);
    session.save();
    
    List<Node> listRecord = recordsService.getObsoleteRecords(rootNode.getNode("TestTreeNode"));
    assertEquals(listRecord.size(), 0);
    
    nodeA1.setProperty("rma:isObsolete", true);
    session.save();
    listRecord = recordsService.getObsoleteRecords(rootNode.getNode("TestTreeNode"));
    assertEquals(listRecord.size(), 1);
    assertEquals(listRecord.get(0).getName(), "A1");
  }
  
  public void testGetSupersededRecords() throws Exception {
    Node nodeA1 = rootNode.getNode("TestTreeNode/A1");
    Node nodeA2 = rootNode.getNode("TestTreeNode/A2");
    Node nodeA3 = rootNode.getNode("TestTreeNode/A3");
    Node nodeA4 = rootNode.getNode("TestTreeNode/A4");
    
    recordsService.addRecord(nodeA3, nodeA1);
    recordsService.addRecord(nodeA4, nodeA2);
    session.save();
    
    List<Node> listRecord = recordsService.getSupersededRecords(rootNode.getNode("TestTreeNode"));
    assertEquals(listRecord.size(), 0);
    
    nodeA1.setProperty("rma:superseded", true);
    nodeA2.setProperty("rma:superseded", true);
    
    nodeA2.setProperty("rma:dateReceived", new GregorianCalendar());
    nodeA1.setProperty("rma:dateReceived", new GregorianCalendar());
    
    session.save();
    listRecord = recordsService.getSupersededRecords(rootNode.getNode("TestTreeNode"));
    assertEquals(listRecord.size(), 2);
    assertEquals(listRecord.get(0).getName(), "A2");
    assertEquals(listRecord.get(1).getName(), "A1");
  }
  
//  public void testGetCutoffRecords() throws Exception {
//    Node nodeA1 = rootNode.getNode("TestTreeNode/A1");
//    Node nodeA2 = rootNode.getNode("TestTreeNode/A2");
//    Node nodeA3 = rootNode.getNode("TestTreeNode/A3");
//    Node nodeA4 = rootNode.getNode("TestTreeNode/A4");
//    
//    recordsService.addRecord(nodeA3, nodeA1);
//    recordsService.addRecord(nodeA4, nodeA2);
//    
//    nodeA1.addMixin("rma:cutoffable");
//    nodeA1.setProperty("rma:cutoffExecuted", true);
//    nodeA1.setProperty("rma:cutoffDateTime", new GregorianCalendar());
//    
//    nodeA2.setProperty("rma:cutoffDateTime", new GregorianCalendar());
//    nodeA2.setProperty("rma:cutoffExecuted", false);
//    
//    nodeA2.setProperty("rma:dateReceived", new GregorianCalendar());
//    nodeA1.setProperty("rma:dateReceived", new GregorianCalendar());
//    session.save();
//    
////    List<Node> listRecord = recordsService.getCutoffRecords(rootNode.getNode("TestTreeNode"));
////    System.out.println("\n" + listRecord.size() + "\n");
////    assertEquals(listRecord.size(), 2);
////    System.out.println("\n" + listRecord.get(0).getName() + "\n");
////    assertEquals(listRecord.get(0).getName(), "A2");
////    assertEquals(listRecord.get(1).getName(), "A1");
//    
//    
//    
//    QueryManager queryManager = session.getWorkspace().getQueryManager();
//    String sql = null;
//    sql = "select * from rma:cutoffable where rma:cutoffExecuted='false' order by rma:dateReceived DESC";
//    Query query = queryManager.createQuery(sql, Query.SQL);
//    QueryResult result = query.execute();
//    NodeIterator iterate = result.getNodes();
//    while (iterate.hasNext()) {
//      System.out.println("\nName: " + iterate.nextNode().getName() + "\n");
//    }
//  }
  
  
  public void testGetHolableRecords() throws Exception {
    Node nodeA1 = rootNode.getNode("TestTreeNode/A1");
    Node nodeA2 = rootNode.getNode("TestTreeNode/A2");
    Node nodeA3 = rootNode.getNode("TestTreeNode/A3");
    Node nodeA4 = rootNode.getNode("TestTreeNode/A4");
    
    recordsService.addRecord(nodeA3, nodeA1);
    recordsService.addRecord(nodeA4, nodeA2);
    session.save();
    
    List<Node> listRecord = recordsService.getHolableRecords(rootNode.getNode("TestTreeNode"));
    assertEquals(listRecord.size(), 0);
    
    nodeA1.addMixin("rma:holdable");
    nodeA1.setProperty("rma:holdExecuted", true);
    nodeA2.addMixin("rma:holdable");
    nodeA2.setProperty("rma:holdExecuted", false);
    session.save();
    
    
    listRecord = recordsService.getHolableRecords(rootNode.getNode("TestTreeNode"));
    assertEquals(listRecord.size(), 1);
    assertEquals(listRecord.get(0).getName(), "A2");
  }
  
  public void testGetTransferableRecords() throws Exception {
    Node nodeA1 = rootNode.getNode("TestTreeNode/A1");
    Node nodeA2 = rootNode.getNode("TestTreeNode/A2");
    Node nodeA3 = rootNode.getNode("TestTreeNode/A3");
    Node nodeA4 = rootNode.getNode("TestTreeNode/A4");
    
    recordsService.addRecord(nodeA3, nodeA1);
    recordsService.addRecord(nodeA4, nodeA2);
    session.save();
    System.out.println("\nNode A1 co phai la nodetype cua rma:transferable?? " + nodeA1.isNodeType("rma:transferable"));
    System.out.println("\nNode A2 co phai la nodetype cua rma:transferable?? " + nodeA2.isNodeType("rma:transferable"));

    List<Node> listRecord = recordsService.getTransferableRecords(rootNode.getNode("TestTreeNode"));
    assertEquals(listRecord.size(), 0);

    nodeA2.addMixin("rma:transferable");
    nodeA2.setProperty("rma:transferDate", new GregorianCalendar());
    nodeA2.setProperty("rma:transferLocation", "location2");
    nodeA2.setProperty("rma:transferExecuted", false);
    nodeA2.setProperty("rma:dateReceived", new GregorianCalendar());
    
    nodeA1.addMixin("rma:transferable");
    nodeA1.setProperty("rma:transferDate", new GregorianCalendar());
    nodeA1.setProperty("rma:transferLocation", "location1");
    nodeA1.setProperty("rma:transferExecuted", false);
    nodeA1.setProperty("rma:dateReceived", new GregorianCalendar());
    session.save();
    
    listRecord = recordsService.getTransferableRecords(rootNode.getNode("TestTreeNode"));
    assertEquals(listRecord.size(), 2);
    assertEquals(listRecord.get(0).getName(), "A2");
    assertEquals(listRecord.get(1).getName(), "A1");
  }
  
  public void testGetAccessionableRecords() throws Exception {
    Node nodeA1 = rootNode.getNode("TestTreeNode/A1");
    Node nodeA2 = rootNode.getNode("TestTreeNode/A2");
    Node nodeA3 = rootNode.getNode("TestTreeNode/A3");
    Node nodeA4 = rootNode.getNode("TestTreeNode/A4");
    
    recordsService.addRecord(nodeA3, nodeA1);
    recordsService.addRecord(nodeA4, nodeA2);
    session.save();

    List<Node> listRecord = recordsService.getAccessionableRecords(rootNode.getNode("TestTreeNode"));
    assertEquals(listRecord.size(), 0);

    nodeA2.addMixin("rma:accessionable");
    nodeA2.setProperty("rma:accessionExecuted", false);
    nodeA2.setProperty("rma:accessionDate", new GregorianCalendar());
    nodeA2.setProperty("rma:dateReceived", new GregorianCalendar());
    
    nodeA1.addMixin("rma:accessionable");
    nodeA1.setProperty("rma:accessionExecuted", false);
    nodeA1.setProperty("rma:accessionDate", new GregorianCalendar());
    nodeA1.setProperty("rma:dateReceived", new GregorianCalendar());
    session.save();
    
    listRecord = recordsService.getAccessionableRecords(rootNode.getNode("TestTreeNode"));
    assertEquals(listRecord.size(), 2);
    assertEquals(listRecord.get(0).getName(), "A2");
    assertEquals(listRecord.get(1).getName(), "A1");
  }
  
  public void testGetDestroyableRecords() throws Exception {
    Node nodeA1 = rootNode.getNode("TestTreeNode/A1");
    Node nodeA2 = rootNode.getNode("TestTreeNode/A2");
    Node nodeA3 = rootNode.getNode("TestTreeNode/A3");
    Node nodeA4 = rootNode.getNode("TestTreeNode/A4");
    
    recordsService.addRecord(nodeA3, nodeA1);
    recordsService.addRecord(nodeA4, nodeA2);
    session.save();

    List<Node> listRecord = recordsService.getDestroyableRecords(rootNode.getNode("TestTreeNode"));
    assertEquals(listRecord.size(), 0);

    nodeA2.addMixin("rma:destroyable");
    nodeA2.setProperty("rma:destructionDate", new GregorianCalendar());
    nodeA2.setProperty("rma:dateReceived", new GregorianCalendar());
    
    nodeA1.addMixin("rma:destroyable");
    nodeA1.setProperty("rma:destructionDate", new GregorianCalendar());
    nodeA1.setProperty("rma:dateReceived", new GregorianCalendar());
    session.save();
    
    listRecord = recordsService.getDestroyableRecords(rootNode.getNode("TestTreeNode"));
    assertEquals(listRecord.size(), 2);
//    assertEquals(listRecord.get(0).getName(), "A2");
//    assertEquals(listRecord.get(1).getName(), "A1");
  }
  
//  public void testComputeHolds() throws Exception {
//    Node nodeA1 = rootNode.getNode("TestTreeNode/A1");
//    Node nodeA2 = rootNode.getNode("TestTreeNode/A2");
//    Node nodeA3 = rootNode.getNode("TestTreeNode/A3");
//    Node nodeA4 = rootNode.getNode("TestTreeNode/A4");
//    
//    recordsService.addRecord(nodeA3, nodeA1);
//    recordsService.addRecord(nodeA4, nodeA2);
//    nodeA1.addMixin("rma:holdable");
//    nodeA1.setProperty("rma:holdExecuted", true);
//    nodeA2.addMixin("rma:holdable");
//    nodeA2.setProperty("rma:holdExecuted", false);
//    nodeA2.setProperty("rma:holdUntil", new GregorianCalendar());
//    nodeA2.setProperty("rma:processAccession", true);
//    nodeA2.setProperty("rma:processDestruction", true);
//    
//    session.save();
//
//    recordsService.computeHolds(rootNode.getNode("TestTreeNode"));
//    assertEquals(nodeA2.getProperty("rma:holdExecuted"), true);
//  }
  
  public void tearDown() throws Exception {
    try {
      Node testNode = rootNode.getNode("TestTreeNode");
      NodeIterator iter = testNode.getNodes();
      while (iter.hasNext()) {
        Node node = iter.nextNode();
        NodeType[] mixins = node.getMixinNodeTypes();
        for (NodeType mixinNode : mixins) {
          node.removeMixin(mixinNode.getName());
        }
      }
      testNode.remove();
      session.save();
    } catch (Exception e) {
      e.printStackTrace();
    }
    super.tearDown();
  }
}
