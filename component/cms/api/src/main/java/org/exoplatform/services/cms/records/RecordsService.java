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
