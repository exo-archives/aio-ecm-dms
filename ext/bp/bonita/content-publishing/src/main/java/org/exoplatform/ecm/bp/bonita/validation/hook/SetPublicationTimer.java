package org.exoplatform.ecm.bp.bonita.validation.hook;

import java.util.Date;

import org.exoplatform.services.workflow.impl.bonita.CommandTimer;
import org.ow2.bonita.definition.TxHook;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.runtime.ActivityBody;
import org.ow2.bonita.facade.runtime.ActivityInstance;

public class SetPublicationTimer implements TxHook {

	public void execute(APIAccessor api, ActivityInstance<ActivityBody> activity)
			throws Exception {
		// Get the publication date
		Date startDate = (Date) api.getQueryRuntimeAPI().getProcessInstanceVariable(activity.getProcessInstanceUUID(), "startDate");
		CommandTimer cTimer = new CommandTimer(activity.getProcessInstanceUUID(),activity.getActivityId(),startDate);
		api.getCommandAPI().execute(cTimer);
	}
}
