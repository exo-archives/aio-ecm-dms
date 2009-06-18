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
package org.exoplatform.services.ecm.dms.rss;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.services.cms.rss.RSSService;
import org.exoplatform.services.ecm.dms.BaseDMSTestCase;

/**
 * Created by The eXo Platform SARL
 * June 09, 2009  
 */
public class TestRSSService extends BaseDMSTestCase {
    
  private RSSService rssService;
  
  
  public void setUp() throws Exception {
    super.setUp();
    rssService = (RSSService)container.getComponentInstanceOfType(RSSService.class);
  }
  
  public void testGenerateFeed() throws Exception {
    Map contextRss = new HashMap();
    contextRss.put("exo:feedType", "rss");
    contextRss.put("repository", "repository");
    contextRss.put("srcWorkspace", COLLABORATION_WS);
    contextRss.put("actionName", "actionName");
    contextRss.put("exo:rssVersion", "rss_2.0");
    contextRss.put("exo:feedTitle", "Hello Feed");
    contextRss.put("exo:summary", "Hello Summary");
    contextRss.put("exo:description", "Hello Description");
    contextRss.put("exo:storePath", "/Feeds");
    contextRss.put("exo:feedName", "feedName");
    contextRss.put("exo:queryPath", "/jcr:root/Documents//element(*, exo:article)");    
    contextRss.put("exo:title", "Hello Title");
    contextRss.put("exo:url", "http://www.facebook.com");    
    rssService.generateFeed(contextRss);
    
    Session mySession = repository.login(credentials, COLLABORATION_WS);
    Node myFeeds = (Node) mySession.getItem("/Feeds");
    Node myRSS = (Node) mySession.getItem("/Feeds/rss");
    Node myFeedName = (Node) mySession.getItem("/Feeds/rss/feedName");
    Node myJcrContent = myFeedName.getNode("jcr:content");    
    assertEquals("Feeds", myFeeds.getName());
    assertEquals("rss", myRSS.getName());
    assertEquals("feedName", myFeedName.getName());
    assertEquals("/Feeds/rss/feedName/jcr:content", myJcrContent.getPath());
    assertNotNull(myJcrContent.getProperty("jcr:data").getString());
    assertEquals("text/xml", myJcrContent.getProperty("jcr:mimeType").getString());
    
    Map contextPodcast = new HashMap();
    contextPodcast.put("exo:feedType", "podcast");
    contextPodcast.put("repository", "repository");
    contextPodcast.put("srcWorkspace", COLLABORATION_WS);
    contextPodcast.put("actionName", "actionName");
    contextPodcast.put("exo:rssVersion", "rss_1.0");
    contextPodcast.put("exo:feedTitle", "Hello Feed");
    contextPodcast.put("exo:link", "Testing");    
    contextPodcast.put("exo:summary", "Hello Summary");
    contextPodcast.put("exo:description", "Hello Description");
    contextPodcast.put("exo:storePath", "/Feeds");
    contextPodcast.put("exo:feedName", "podcastName");
    contextPodcast.put("exo:queryPath", "/jcr:root/Documents//element(*, exo:article)");    
    contextPodcast.put("exo:title", "Hello Title");
    contextPodcast.put("exo:url", "http://twitter.com");    
    rssService.generateFeed(contextPodcast);
    
    myFeeds = (Node) mySession.getItem("/Feeds");
    Node myPodcast = (Node) mySession.getItem("/Feeds/podcast");
    Node myPodcastName = (Node) mySession.getItem("/Feeds/podcast/podcastName");
    myJcrContent = myPodcastName.getNode("jcr:content");    
    assertEquals("Feeds", myFeeds.getName());
    assertEquals("podcast", myPodcast.getName());
    assertEquals("podcastName", myPodcastName.getName());
    assertEquals("/Feeds/podcast/podcastName/jcr:content", myJcrContent.getPath());
    assertNotNull(myJcrContent.getProperty("jcr:data").getString());
    assertEquals("text/xml", myJcrContent.getProperty("jcr:mimeType").getString());
  }  
}
