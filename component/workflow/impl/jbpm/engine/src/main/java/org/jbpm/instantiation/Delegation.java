package org.jbpm.instantiation;

import java.io.*;
import java.util.*;

import org.apache.commons.logging.*;
import org.dom4j.*;
import org.dom4j.Node;
import org.dom4j.io.*;
import org.jbpm.graph.def.*;
import org.jbpm.jpdl.xml.*;

public class Delegation implements Parsable, Serializable {

  private static final long serialVersionUID = 1L;
  
  protected static Map instantiatorCache = new HashMap();
  static {
    instantiatorCache.put(null, new FieldInstantiator());
    instantiatorCache.put("field", new FieldInstantiator());
    instantiatorCache.put("bean", new BeanInstantiator());
    instantiatorCache.put("constructor", new ConstructorInstantiator());
    instantiatorCache.put("configuration-property", new ConfigurationPropertyInstantiator());
  }

  long id = 0;
  protected String className = null;
  protected String configuration = null;
  protected String configType = null;
  protected ProcessDefinition processDefinition = null;
  private transient Object instance = null;

  public Delegation() {
  }

  public Delegation(Object instance) {
    this.instance = instance;
  }

  public Delegation(String className) {
    this.className = className;
  }

  public void read(Element delegateElement, JpdlXmlReader jpdlReader) {
    processDefinition = jpdlReader.getProcessDefinition();
    className = delegateElement.attributeValue("class");
    if (className==null) {
      jpdlReader.addWarning("no class specified in "+delegateElement.asXML());
    }
      
    configType = delegateElement.attributeValue("config-type");
    if ( delegateElement.hasContent() ) {
      try {
        StringWriter stringWriter = new StringWriter();
        // when parsing, it could be to store the config in the database, so we want to make the configuration compact
        XMLWriter xmlWriter = new XMLWriter( stringWriter, OutputFormat.createCompactFormat() );
        Iterator iter = delegateElement.content().iterator();
        while (iter.hasNext()) {
          xmlWriter.write( iter.next() );
        }
        xmlWriter.flush();
        configuration = stringWriter.toString();
      } catch (IOException e) {
        jpdlReader.addWarning("io problem while parsing the configuration of "+delegateElement.asXML());
      }
    }
  }

  public void write(Element element) {
    element.addAttribute("class", className);
    element.addAttribute("config-type", configType);
    String configuration = this.configuration;
    if (configuration!=null) {
      try {
        Element actionElement = DocumentHelper.parseText( "<action>"+configuration+"</action>" ).getRootElement();
        Iterator iter = new ArrayList( actionElement.content() ).iterator();
        while (iter.hasNext()) {
          Node node = (Node)iter.next();
          node.setParent(null);
          element.add( node );
        }
      } catch (DocumentException e) {
        log.error("couldn't create dom-tree for action configuration '"+configuration+"'", e);
      }
    }
  }

  public Object getInstance() {
    if (instance==null) {
      instance = instantiate();
    }
    return instance;
  }

  public Object instantiate() {

    Object newInstance = null; 

    // find the classloader to use
    ClassLoader classLoader = ClassLoaderUtil.getProcessClassLoader(processDefinition);
    
    // load the class that needs to be instantiated
    Class clazz = null;
    try {
      clazz = classLoader.loadClass(className);
    } catch (ClassNotFoundException e) {
      log.error("couldn't load delegation class '"+className+"'", e);
    }

    Instantiator instantiator = null;
    try {
      // find the instantiator
      instantiator = (Instantiator) instantiatorCache.get(configType);
      if (instantiator == null) {
        // load the instantiator class
        Class instantiatorClass = classLoader.loadClass(configType);
        // instantiate the instantiator with the default constructor
        instantiator = (Instantiator) instantiatorClass.newInstance();
        instantiatorCache.put(configType, instantiator);
      }
    } catch (Exception e) {
      log.error(e);
      throw new RuntimeException("couldn't instantiate custom instantiator '" + configType + "'", e);
    }
      
    try {
      // instantiate the object
      newInstance = instantiator.instantiate(clazz, configuration);
    } catch (RuntimeException e) {
      log.error("couldn't instantiate delegation class '"+className+"'", e);
    }

    return newInstance; 
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String getConfiguration() {
    return configuration;
  }

  public void setConfiguration(String configuration) {
    this.configuration = configuration;
  }

  public String getConfigType() {
    return configType;
  }

  public void setConfigType(String instantiatorType) {
    this.configType = instantiatorType;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public ProcessDefinition getProcessDefinition() {
    return processDefinition;
  }
  public void setProcessDefinition(ProcessDefinition processDefinition) {
    this.processDefinition = processDefinition;
  }

  private static final Log log = LogFactory.getLog(Delegation.class);

}
