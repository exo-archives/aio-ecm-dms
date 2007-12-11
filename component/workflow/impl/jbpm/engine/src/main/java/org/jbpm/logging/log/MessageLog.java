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
package org.jbpm.logging.log;

public class MessageLog extends ProcessLog {

  private static final long serialVersionUID = 1L;
  
  String message = null;
  
  public MessageLog() {
  }

  public MessageLog(String message) {
    this.message = message;
  }

  public String toString() {
    return "message["+message+"]";
  }

  public String getMessage() {
    return message;
  }
}
