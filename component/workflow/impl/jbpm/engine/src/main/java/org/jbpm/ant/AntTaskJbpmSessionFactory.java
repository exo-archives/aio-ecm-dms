package org.jbpm.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.BuildException;
import org.hibernate.cfg.Configuration;
import org.jbpm.db.JbpmSessionFactory;

/**
 * common strategy for jbpm ant tasks to obtain a {@link org.jbpm.db.JbpmSessionFactory}.
 */
public abstract class AntTaskJbpmSessionFactory {
  
  private final static Map jbpmSessionFactoryCache = new HashMap();
  private final static Map configurationCache = new HashMap();
  
  public static JbpmSessionFactory getJbpmSessionFactory(String cfg, String properties) {
    JbpmSessionFactory jbpmSessionFactory = null;
    
    List key = getKey(cfg, properties);
    
    log.debug("checking jbpm session factory cache for key "+key);
    log.debug("jbpm session factory cache: "+jbpmSessionFactoryCache);
    if (jbpmSessionFactoryCache.containsKey(key)) {
      log.debug("getting jbpm session factory from cache");
      jbpmSessionFactory = (JbpmSessionFactory) jbpmSessionFactoryCache.get(key);
    } else {
      log.debug("creating new jbpm session factory");
      jbpmSessionFactory = createJbpmSessionFactory(cfg, properties);
      jbpmSessionFactoryCache.put(key, jbpmSessionFactory);
    }
    
    return jbpmSessionFactory;
  }

  public static Configuration getConfiguration(String cfg, String properties) {
    Configuration configuration = null;
    
    List key = getKey(cfg, properties);
    
    log.debug("checking hibernate config cache for key "+key);
    log.debug("hibernate config cache: "+configurationCache);
    if (configurationCache.containsKey(key)) {
      log.debug("getting hibernate config from cache");
      configuration = (Configuration) configurationCache.get(key);
    } else {
      log.debug("creating new hibernate config");
      configuration = createConfiguration(cfg, properties);
      configurationCache.put(key, configuration);
    }
    
    return configuration;
  }

  private static List getKey(String cfg, String properties) {
    List key = new ArrayList();
    key.add(cfg);
    key.add(properties);
    return key;
  }

  private static JbpmSessionFactory createJbpmSessionFactory(String cfg, String properties) {
    JbpmSessionFactory jbpmSessionFactory = null;
    try {
      Configuration configuration = getConfiguration(cfg, properties);

      // creating the session factory
      jbpmSessionFactory = new JbpmSessionFactory(configuration);

    } catch (Exception e) {
      e.printStackTrace();
      throw new BuildException( "couldn't create JbpmSessionFactory: " + e.getMessage() );
    }
    return jbpmSessionFactory;
  }

  private static Configuration createConfiguration(String cfg, String properties) {
    // configure the hibernate configuration 
    Configuration configuration = null;
    
    try {
      if (cfg!=null) {
        File cfgFile = new File(cfg);
        log.debug("using '"+cfgFile+"' for hibernate configuration");
        configuration = new Configuration().configure(cfgFile);
      } else {
        log.debug("using the default jbpm configured hibernate configuration");
        configuration = JbpmSessionFactory.createConfiguration();
      }
      
      // if specified, overwrite the properties with the properties 
      // in the specified properties file 
      if (properties!=null) {
        InputStream inputStream = new FileInputStream(new File(properties));
        Properties props = new Properties();
        props.load(inputStream);
        configuration.setProperties(props);
      }
      List key = getKey(cfg, properties);
      configurationCache.put(key, configuration);
      
    } catch (Exception e) {
      e.printStackTrace();
      throw new BuildException( "couldn't create configuration: " + e.getMessage() );
    }

    return configuration;
  }

  private static final Log log = LogFactory.getLog(AntTaskJbpmSessionFactory.class);
}
