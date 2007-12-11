/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.jbpm.graph.node;

import org.jbpm.graph.def.*;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.jpdl.exe.*;

public class MilestoneEvent implements ActionHandler {

  private static final long serialVersionUID = 1L;
  
  private String milestoneName = null;
  private String relativeTokenPath = null;
  
  public MilestoneEvent() {
  }

  public MilestoneEvent( String milestoneName, String relativeTokenPath ) {
    this.milestoneName = milestoneName;
    this.relativeTokenPath = relativeTokenPath;
  }

  public void execute(ExecutionContext ac) {
    MilestoneInstance mi = MilestoneInstance.getMilestoneInstance(milestoneName, ac.getToken());
    mi.setReached(true);
    mi.notifyListeners();
  }
  



  public String getMilestoneName() {
    return milestoneName;
  }
  public void setMilestoneName(String milestoneName) {
    this.milestoneName = milestoneName;
  }
  public String getRelativeTokenPath() {
    return relativeTokenPath;
  }
  public void setRelativeTokenPath(String relativeTokenPath) {
    this.relativeTokenPath = relativeTokenPath;
  }
}
