/******************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL. All rights reserved.            *
 * Please look at license.txt in info directory for more license detail.      *
 ******************************************************************************/
package org.exoplatform.services.workflow;

/**
 * This interface specifies the contract of the Service which manages File
 * Definitions. A File Definition contains among other things Forms definitions.
 * It is required to persist File definitions somewhere as Forms need to be
 * retrieved if eXo is restarted. It was decided to define a Service to do that,
 * which makes it possible to allow various types of storage (eg: File System,
 * ECM).
 * 
 * <i>This interface is currently part of the Bonita package as jBPM has a 
 * built-in facility to manage File Definition. It may however be a good idea to
 * move it to the api package to make things common.</i>
 * 
 * Created by Bull R&D
 * @author Brice Revenant
 * Feb 27, 2005
 */
public interface WorkflowFileDefinitionService {
  
  /**
   * Remove a File Definition
   * 
   * @param processId identifies the File Definition to remove
   */
  public void remove(String processId);
  
  /**
   * If the implementation features a cache to increase performances, removes
   * the File Definition corresponding to the specified Process identifier.
   * This method is notably used while reloading a File Definition.
   * 
   * @param processId identifies the Process to be removed from the cache
   */
  public void removeFromCache(String processId);
  
  /**
   * Retrieves a File Definition
   * 
   * @param  processId identifies the File Definition to retrieve
   * @return the requested File Definition or <tt>null</tt> if not found
   */
  public FileDefinition retrieve(String processId);
  
  /**
   * Stores a File Definition
   * 
   * @param fileDefinition the File Definition to store
   * @param processId      identifies the File Definition to store
   */
  public void store(FileDefinition fileDefinition, String processId);
}
