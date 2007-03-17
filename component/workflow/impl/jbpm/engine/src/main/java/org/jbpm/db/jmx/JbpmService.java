package org.jbpm.db.jmx;

import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;

import org.jboss.naming.NonSerializableFactory;
import org.jboss.system.ServiceMBeanSupport;
import org.jbpm.db.JbpmSessionFactory;

public class JbpmService extends ServiceMBeanSupport implements JbpmServiceMBean {

  String jndiName = null;

  protected void startService() throws Exception {
    log.debug("starting jbpmSessionFactory for '" + jndiName + "'...");
    // Configuration configuration = JbpmSessionFactory.createConfiguration("hibernate.cfg.xml");
    JbpmSessionFactory jbpmSessionFactory = JbpmSessionFactory.buildJbpmSessionFactory();
    InitialContext rootCtx = new InitialContext();
    Name fullName = rootCtx.getNameParser("").parse(jndiName);
    log.info("binding JbpmSessionFactory '" + jndiName + "' into JNDI...");
    NonSerializableFactory.rebind(fullName, jbpmSessionFactory, true);
  }

  protected void stopService() throws Exception {
    try {
      InitialContext rootCtx = new InitialContext();
      rootCtx.unbind(jndiName);
      NonSerializableFactory.unbind(jndiName);
    } catch (NamingException e) {
      log.error("Failed to unbind jbpmSessionFactory", e);
    }
  }

  public String getJndiName() {
    return jndiName;
  }
  public void setJndiName(String jndiName) {
    this.jndiName = jndiName;
  }
}
