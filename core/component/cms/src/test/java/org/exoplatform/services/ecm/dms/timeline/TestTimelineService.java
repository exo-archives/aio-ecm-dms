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
package org.exoplatform.services.ecm.dms.timeline;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.cms.timeline.TimelineService;
import org.exoplatform.services.ecm.dms.BaseDMSTestCase;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 22, 2009  
 * 10:50:05 AM
 */
public class TestTimelineService extends BaseDMSTestCase {

  private TimelineService timelineService;
  
  public void setUp() throws Exception {
    super.setUp();
    timelineService = (TimelineService)container.getComponentInstanceOfType(TimelineService.class);
  }
  
  public void testGetDocumentsOfToday() throws Exception {
    timelineService.getDocumentsOfToday(REPO_NAME, COLLABORATION_WS, createSessionProvider(), "root");
  }
  
  /**
   * private method create sessionProvider instance.
   * @return SessionProvider 
   */
  private SessionProvider createSessionProvider() {
    SessionProviderService sessionProviderService = (SessionProviderService) container
        .getComponentInstanceOfType(SessionProviderService.class);
    return sessionProviderService.getSessionProvider(null);
  }  
  
  public void tearDown() throws Exception {
    super.tearDown();
  }
}
