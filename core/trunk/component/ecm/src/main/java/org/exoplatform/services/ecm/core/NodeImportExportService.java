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
package org.exoplatform.services.ecm.core;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * May 9, 2008  
 */
public interface NodeImportExportService {
  /* The purpose of this service is: Leverage node import/export feature from jcr
   * Import/Export in UI need use this service to handle import/export function
   * Some improvement need implement:
   * - import/export a huge node in diamond thread(use notification service to notify that import/export sucess or not)
   * - import/export from/to zip file
   * - Support full import/export feature from jcr
   * */
}
