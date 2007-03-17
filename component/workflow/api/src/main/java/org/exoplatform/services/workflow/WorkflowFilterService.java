/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL        All rights reserved.   *
 * Please look at license.txt in info directory for more license detail.   *
 ***************************************************************************/
package org.exoplatform.services.workflow;

import javax.servlet.Filter;

/**
 * This Service is looked up in the Workflow Filter.
 * It allows to customize the processing done for each Workflow implementation.
 * 
 * Created by Bull R&D
 * @author Brice Revenant
 * Mar 29, 2006
 */
public interface WorkflowFilterService extends Filter {
}
