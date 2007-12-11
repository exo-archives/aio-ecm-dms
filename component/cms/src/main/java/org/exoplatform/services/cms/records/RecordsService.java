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
package org.exoplatform.services.cms.records;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

public interface RecordsService {
  
  public void bindFilePlanAction(Node filePlan, String repository) throws Exception ;
  
  public void addRecord(Node filePlan, Node record) throws RepositoryException;
  
  public void computeCutoffs(Node filePlan) throws RepositoryException;
  
  public void computeHolds(Node filePlan) throws RepositoryException;
  
  public void computeTransfers(Node filePlan) throws RepositoryException;
  
  public void computeAccessions(Node filePlan) throws RepositoryException;
  
  public void computeDestructions(Node filePlan) throws RepositoryException;
  
  public List<Node> getRecords(Node filePlan) throws RepositoryException;
  
  public List<Node> getVitalRecords(Node filePlan) throws RepositoryException;  
  
  public List<Node> getObsoleteRecords(Node filePlan) throws RepositoryException;  
  
  public List<Node> getSupersededRecords(Node filePlan) throws RepositoryException;  
  
  public List<Node> getCutoffRecords(Node filePlan) throws RepositoryException;  
  
  public List<Node> getHolableRecords(Node filePlan) throws RepositoryException;
  
  public List<Node> getTransferableRecords(Node filePlan) throws RepositoryException;
  
  public List<Node> getAccessionableRecords(Node filePlan) throws RepositoryException;
  
  public List<Node> getDestroyableRecords(Node filePlan) throws RepositoryException;
  
}
