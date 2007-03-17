package org.jbpm.db;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.jbpm.JbpmConfiguration;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.instantiation.ClassLoaderUtil;

/**
 * creates JbpmSessions.
 * Obtain a JbpmSessionFactory with
 * <pre>
 * static JbpmSessionFactory jbpmSessionFactory = JbpmSessionFactory.buildJbpmSessionFactory();
 * </pre>
 * and store it somewhere static.  It takes quite some time to create a DbSessionFactory,
 * but you only have to do it once.  After that, creating DbSession's is really fast.
 */
public class JbpmSessionFactory implements Serializable {
  
  private static final long serialVersionUID = 1L;

  private static String jndiName = JbpmConfiguration.getString("jbpm.session.factory.jndi.name");

  private Configuration configuration = null;
  private SessionFactory sessionFactory = null;
  private Collection hibernatableLongIdClasses = null;
  private Collection hibernatableStringIdClasses = null;
  private JbpmSchema jbpmSchema = null;
  
  private static JbpmSessionFactory instance = null;
  /**
   * a singleton is kept in JbpmSessionFactory as a convenient central location.
   */
  public static JbpmSessionFactory getInstance() {
    if (instance==null) {
      
      // if there is a JNDI name configured
      if (jndiName!=null) {
        try {
          // fetch the JbpmSessionFactory from JNDI
          log.debug("fetching JbpmSessionFactory from '"+jndiName+"'");
          InitialContext initialContext = new InitialContext();
          Object o = initialContext.lookup(jndiName);
          instance = (JbpmSessionFactory) PortableRemoteObject.narrow(o, JbpmSessionFactory.class);
        } catch (Exception e) {
          throw new RuntimeException("couldn't fetch JbpmSessionFactory from jndi '"+jndiName+"'");
        }
        
      } else { // else there is no JNDI name configured
        // create a new default instance.
        log.debug("building singleton JbpmSessionFactory");
        instance = buildJbpmSessionFactory();
      }
    }
    return instance;
  }
  
  public JbpmSessionFactory(Configuration configuration) {
    this( configuration, buildSessionFactory(configuration) );
  }

  public JbpmSessionFactory(Configuration configuration, SessionFactory sessionFactory) {
    this.configuration = configuration;
    this.sessionFactory = sessionFactory;
  }
  
  public static JbpmSessionFactory buildJbpmSessionFactory() {
    return buildJbpmSessionFactory(getConfigResource());
  }

  public static JbpmSessionFactory buildJbpmSessionFactory(String configResource) {
    return buildJbpmSessionFactory(createConfiguration(configResource));
  }
  
  public static JbpmSessionFactory buildJbpmSessionFactory(Configuration configuration) {
    return new JbpmSessionFactory(configuration);
  }

  private static String getConfigResource() {
    return JbpmConfiguration.getString("jbpm.hibernate.cfg.xml");
  }

  public static Configuration createConfiguration() {
    return createConfiguration(getConfigResource());
  }

  public static Configuration createConfiguration(String configResource) {
    Configuration configuration = null;
    // create the hibernate configuration
    configuration = new Configuration();
    if (configResource!=null) {
      log.debug("using '"+configResource+"' as hibernate configuration for jbpm");
      configuration.configure(configResource);
    } else {
      log.debug("using the default hibernate configuration file: hibernate.cfg.xml");
      configuration.configure();
    }
    
    // check if the properties in the hibernate.cfg.xml need to be overwritten by a separate properties file.
    String hibernatePropertiesResource = JbpmConfiguration.getString("jbpm.hibernate.properties");
    if (hibernatePropertiesResource!=null) {
      Properties hibernateProperties = new Properties();
      try {
        hibernateProperties.load( ClassLoaderUtil.getStream(hibernatePropertiesResource) );
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException("couldn't load the hibernate properties from resource '"+hibernatePropertiesResource+"'", e);
      }
      log.debug("overriding hibernate properties with "+ hibernateProperties);
      configuration.setProperties(hibernateProperties);
    }
    
    return configuration;
  }

  public static SessionFactory buildSessionFactory(Configuration configuration) {
    SessionFactory sessionFactory = null;
    // create the hibernate session factory
    log.debug("building hibernate session factory");
    sessionFactory = configuration.buildSessionFactory();
    return sessionFactory;
  }

  /**
   * obtains a jdbc connection as specified in the hibernate configurations and 
   * creates a DbSession with it.
   */
  public JbpmSession openJbpmSession() {
    return openJbpmSession((Connection)null);
  }

  /**
   * creates a DbSession around the given connection.  Note that you are 
   * responsible for closing the connection so closing the DbSession will 
   * not close the jdbc connection.
   */
  public JbpmSession openJbpmSession(Connection jdbcConnection) {
    JbpmSession dbSession = null;
    
    try {
      Session session = null;
      
      if ( jdbcConnection == null ) {
        // use the hibernate properties in the nwsp.properties file to 
        // create a jdbc connection for the created hibernate session.
        session = getSessionFactory().openSession();
      } else {
        // use the client provided jdbc connection in  
        // the created hibernate session.
        session = getSessionFactory().openSession(jdbcConnection);
      }
      
      dbSession = new JbpmSession( this, session );
      
    } catch (HibernateException e) {
      log.error( e );
      throw new RuntimeException( "couldn't create a hibernate persistence session", e );
    }
    return dbSession;
  }

  public JbpmSession openJbpmSession(Session session) {
    return new JbpmSession(null, session);
  }

  public JbpmSession openJbpmSessionAndBeginTransaction() {
    JbpmSession dbSession = openJbpmSession((Connection)null);
    dbSession.beginTransaction();
    return dbSession;
  }
    
  public SessionFactory getSessionFactory() {
    return sessionFactory;
  }
  
  public Configuration getConfiguration() {
    return configuration;
  }
  
  /**
   * clears the process definitions from hibernate's second level cache.
   */
  public void evictCachedProcessDefinitions() {
    sessionFactory.evict(ProcessDefinition.class);
  }

  /**
   * checks if the given class is persistable with hibernate and has an id of type long.
   */
  public boolean isHibernatableWithLongId(Class clazz) {
    if (hibernatableLongIdClasses==null) {
      initHibernatableClasses();
    }
    return hibernatableLongIdClasses.contains(clazz);
  }

  /**
   * checks if the given class is persistable with hibernate and has an id of type string.
   */
  public boolean isHibernatableWithStringId(Class clazz) {
    if (hibernatableStringIdClasses==null) {
      initHibernatableClasses();
    }
    return hibernatableStringIdClasses.contains(clazz);
  }
  
  public JbpmSchema getJbpmSchema() {
    if (jbpmSchema==null) {
      jbpmSchema = new JbpmSchema(configuration);
    }
    return jbpmSchema;
  }

  private void initHibernatableClasses() {
    hibernatableLongIdClasses = new HashSet();
    hibernatableStringIdClasses = new HashSet();
    Iterator iter = configuration.getClassMappings();
    while (iter.hasNext()) {
      PersistentClass persistentClass = (PersistentClass) iter.next();
      if (LongType.class==persistentClass.getIdentifier().getType().getClass()) {
        hibernatableLongIdClasses.add( persistentClass.getMappedClass() );
      } else if (StringType.class==persistentClass.getIdentifier().getType().getClass()) {
        hibernatableStringIdClasses.add( persistentClass.getMappedClass() );
      }
    }
  }

  private static final Log log = LogFactory.getLog(JbpmSessionFactory.class);
}
