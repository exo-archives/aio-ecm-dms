package org.jbpm.db.jmx;

import org.jboss.system.ServiceMBean;

public interface JbpmServiceMBean extends ServiceMBean {
  String getJndiName();
  void setJndiName(String jndiName);
}
