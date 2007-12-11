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
package org.exoplatform.services.workflow.impl.bonita;

import hero.interfaces.ProjectSessionHome;
import hero.interfaces.ProjectSessionLocal;
import hero.interfaces.ProjectSessionLocalHome;
import hero.interfaces.ProjectSessionUtil;
import hero.interfaces.XPDLSessionImportLocal;
import hero.interfaces.XPDLSessionImportLocalHome;
import hero.interfaces.XPDLSessionImportUtil;
import hero.util.ServerType;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.exoplatform.services.workflow.FileDefinition;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * This class maps in memory a deployable Business Process Archive.
 * Among others, it stores array of bytes corresponding to the XPDL definition
 * file, the form definition file (ie forms.xml) and the resource bundles. 
 * 
 * Created by Bull R&D
 * @author Brice Revenant
 * @author Patrick Silani
 * Feb 21, 2005
 */
public class XPDLFileDefinition implements FileDefinition {

  /** Contains bytes corresponding to the files in the archive */
  private Hashtable<String, byte[]> entries;
  
  /** Caches the parsed Form definition to spare some CPU */
  private Element formDefinition;
  
  /** URI identifying the XPDL namespace in XML documents */
  private static final String XPDL_NAMESPACE_URI =
    "http://www.wfmc.org/2002/XPDL1.0";

  /**
   * Deploy the classes contained by this File Definition
   * 
   * @throws Exception if a problem occured
   */
  public void deployClasses() throws Exception {
    // Process each entry whose name ends with .class
    for(String entryName : entries.keySet()) {
      if(entryName.endsWith(".class")) {
        
        // Compute the full path of the class to create on the filesystem
        File file = new File(ServerType.getServerBase()
                             + File.separator
                             + "bonitaScripts"
                             + (entryName.startsWith("classes") ?
                                 entryName.substring("classes".length()) :
                                 "/".concat(entryName)));

        // Create the directory if not existing yet
        file.getParentFile().mkdirs();
        
        // Initialize input and output streams
        ByteArrayInputStream in =
          new ByteArrayInputStream(entries.get(entryName));
        FileOutputStream out =
          new FileOutputStream(file);
        
        // Create the class from the bytes contained by the entry
        byte[] buffer = new byte[8192];
        int    count  = 0;
        while((count = in.read(buffer, 0, buffer.length)) >= 0) {
          out.write(buffer, 0, count);
        }
        
        // Close the File Output Stream
        out.close();
      }
    }
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.impl.bonita.FileDefinition#deploy()
   */
  public void deploy() throws Exception {
    // Import the XPDL in Bonita
    this.importXPDL();
        
    // Deploy the classes in bonitaScripts directory
    this.deployClasses();
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.impl.bonita.FileDefinition#getProcessModelName()
   */
  public String getProcessModelName() {
    XPath xPath             = XPathFactory.newInstance().newXPath();
    final String expression = "/xpdl:Package/xpdl:WorkflowProcesses/" +
                              "xpdl:WorkflowProcess/@Name";

    try {
      // XPDL leverages Namespace, it is needed to specify it
      NamespaceResolver namespaceContext = new NamespaceResolver();
      namespaceContext.setNamespace("xpdl",
                                    XPDL_NAMESPACE_URI);
      xPath.setNamespaceContext(namespaceContext);
      
      return xPath.evaluate(expression,
                            new InputSource(new ByteArrayInputStream(
                              this.getXPDLDefinition())));
    }
    catch (Exception e)
    {
      throw new RuntimeException("Error while getting the process model name",
                                 e);
    }
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.impl.bonita.FileDefinition#getResourceBundle(java.lang.String, java.util.Locale)
   */
  public ResourceBundle getResourceBundle(String stateName, Locale locale) {
    String bundleName    = this.getResourceBundleName(stateName);
    byte[] localizedFile = this.entries.get(bundleName +
                                            "_" +
                                            locale.getLanguage() +
                                            ".properties");
    byte[] defaultFile   = this.entries.get(bundleName +
                                            ".properties");
    byte[] selectedFile  = null;
    
    // Select which byte array contains the Resource Bundle to be returned
    try {
      if(localizedFile != null) {
        // The Resource Bundle is localized
        selectedFile = localizedFile;
      }
      else if(defaultFile != null) {
        // The Resource Bundle is not localized 
        selectedFile = defaultFile;
      }
      else {
        // No Resource Bundle found. Will cause havoc.
        selectedFile = new byte[0];
      }
      
      // Create a Resource Bundle from the selected byte array
      return new PropertyResourceBundle(new ByteArrayInputStream(selectedFile));
    }
    catch(Exception e) {
      throw new RuntimeException("Error while retrieving the Resource " +
                                 "Bundle for state " +
                                 stateName);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.impl.bonita.FileDefinition#getCustomizedView(java.lang.String)
   */
  public String getCustomizedView(String stateName) {
    XPath xPath             = XPathFactory.newInstance().newXPath();
    final String expression = "/forms/form[state-name=\"" +
                              stateName +
                              "\"]/customized-view/text()";

    try {
      return xPath.evaluate(expression, getFormsDefinition());
    }
    catch (Exception e)
    {
      throw new RuntimeException("Error while getting the Customized View " +
                                 "of Form " +
                                 stateName,
                                 e);
    }
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.impl.bonita.FileDefinition#getEntry(java.lang.String)
   */
  public byte[] getEntry(String path) throws Exception {
    
    // Retrieve the specified entry
    byte[] entry = entries.get(path);
    
    if(entry == null) {
      throw new Exception("The specified entry is not found");
    }
    
    // Return the retrieved entry
    return entry;
  }
  
  /**
   * Retrieves a Form corresponding to a State as a DOM Element
   * 
   * @param  stateName name of the state
   * @return a DOM Element corresponding to the requested Form
   */
  public Element getForm(String stateName) {
    XPath xPath             = XPathFactory.newInstance().newXPath();
    final String expression = "/forms/form[state-name=\"" +
                              stateName +
                              "\"]";

    try {
      return (Element) xPath.evaluate(expression,
                                      getFormsDefinition(),
                                      XPathConstants.NODE);
    }
    catch (Exception e)
    {
      throw new RuntimeException("Error while getting the Form " +
                                 stateName,
                                 e);
    }
  }
  
  /**
   * Retrieves the whole Form definition as a DOM Element
   * 
   * @return a DOM Element corresponding to the Form definition file
   */
  public Element getFormsDefinition() {
    Element ret           = null;
    byte[] xpdlDefinition = entries.get("forms.xml");

    try {
      if(xpdlDefinition == null) {
        // The XPDL file definition is not contained by the archive. Throw an
        // exception that will be used as a cause by the outer catch block.
        throw new Exception("The XPDL definition file was not retrieved.");
      }

      // The XPDL file definition is contained by the archive
      if(formDefinition == null) {
        // The definition has not been parsed yet
        InputStream inputStream =
          new ByteArrayInputStream(xpdlDefinition);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        formDefinition = builder.parse(inputStream).getDocumentElement();
      }
      
      // Return what is contained by the cache
      ret = formDefinition;
    }
    catch(Exception e) {
      throw new RuntimeException("Error while parsing the XPDL Definition", e);
    }

    return ret;
  }
  
  /**
   * Retrieves the Bundle name of a Form corresponding to a State name
   * 
   * @param  stateName identifies the Form
   * @return the requested bundle name
   */
  public String getResourceBundleName(String stateName) {
    XPath xPath             = XPathFactory.newInstance().newXPath();
    final String expression = "/forms/form[state-name=\"" +
                              stateName +
                              "\"]/resource-bundle/text()";

    try {
      return (String) xPath.evaluate(expression,
                                     getFormsDefinition(),
                                     XPathConstants.STRING);
    }
    catch (Exception e)
    {
      throw new RuntimeException("Error while getting the Bundle name " +
                                 "of Form " +
                                 stateName,
                                 e);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.impl.bonita.FileDefinition#getVariables(java.lang.String)
   */
  public List<Map<String, Object>> getVariables(String stateName) {
    List<Map<String, Object>>  ret = new ArrayList<Map<String, Object>>(); 
    XPath xPath                    = XPathFactory.newInstance().newXPath();
    final String expression        = "/forms/form[state-name=\"" +
                                     stateName +
                                     "\"]/variable";

    try {
      NodeList nodeSet = (NodeList) xPath.evaluate(expression,
                                                   getFormsDefinition(),
                                                   XPathConstants.NODESET);

      // Process each Node representing a variable in the Form
      for(int i = 0 ; i < nodeSet.getLength() ; i ++) {
        Element element = (Element) nodeSet.item(i);
        Map<String, Object> attributes = new HashMap<String, Object>();
        
        // Process the Name attribute
        Attr name      = (Attr) element.getAttributeNode("name");
        if(name != null) {
          attributes.put("name", name.getValue());
        }

        // Process the Component attribute
        Attr component = (Attr) element.getAttributeNode("component");
        if(component != null) {
          attributes.put("component", component.getValue());
        }
        
        // Process the Editable attribute
        Attr editable  = (Attr) element.getAttributeNode("editable");
        if(editable != null) {
          attributes.put("editable", editable.getValue());
        }
        
        // Process the Mandatory attribute
        Attr mandatory = (Attr) element.getAttributeNode("mandatory");
        if(mandatory != null) {
          attributes.put("mandatory", mandatory.getValue());
        }
        
        // Add the attributes to the returned List
        ret.add(attributes);
      }
      
      return ret;
    }
    catch (Exception e)
    {
      throw new RuntimeException("Error while retrieving variables of Form " +
                                 stateName,
                                 e);
    }
  }
  
  /**
   * Retrieves the XPDL Process definition
   *
   * @return the requested item or <tt>null</tt> if not found
   */
  public byte[] getXPDLDefinition() {
    // Searches for a key that matches the requested item
    for(String key : entries.keySet()) {
      if(key.matches(".*\\.xpdl$|")) {
        return entries.get(key);
      }
    }

    // The requested item is not found
    return null;
  }
  
  /**
   * Import in Bonita the XPDL contained by this File Definition
   * 
   * @throws Exception if a problem occured
   */
  public void importXPDL() throws Exception {
    XPDLSessionImportLocal xpdlSessionImport = null;
    
    try {
      // Import the XPDL in Bonita
      XPDLSessionImportLocalHome xpdlSessionImportHome =
        XPDLSessionImportUtil.getLocalHome();
      xpdlSessionImport = xpdlSessionImportHome.create();
      xpdlSessionImport.openMainDocument("",
                                         "",
                                         new String(this.getXPDLDefinition()));
    }
    finally {
      try {
        xpdlSessionImport.remove();
      }
      catch(Exception ignore) {
      }
    }
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.impl.bonita.FileDefinition#isDelegatedView(java.lang.String)
   */
  public boolean isDelegatedView(String stateName) {
    XPath xPath             = XPathFactory.newInstance().newXPath();
    final String expression = "/forms/form[state-name=\"" +
                              stateName +
                              "\"]/delegated-view/text()";

    try {
      return "true".equals((String) xPath.evaluate(expression,
                                                   getFormsDefinition(),
                                                   XPathConstants.STRING));
    }
    catch (Exception e)
    {
      throw new RuntimeException("Error while determining if the view " +
                                 "of Form " +
                                 stateName +
                                 " is delegated",
                                 e);
    }
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.impl.bonita.FileDefinition#isFormDefined(java.lang.String)
   */
  public boolean isFormDefined(String stateName) {
    return getForm(stateName) != null;
  }
  
  /**
   * This inner class enables to set the Namespace Context while processing
   * with XPath XML documents leveraging namespaces.
   */
  private class NamespaceResolver implements NamespaceContext {
    private Map<String, String> map = new HashMap<String, String>();

    public String getNamespaceURI(String prefix) {
      return (String) map.get(prefix);
    }

    public String getPrefix(String namespaceURI) {
      Set<String> keys = map.keySet();
      for (Iterator iterator = keys.iterator(); iterator.hasNext();) 
      {
          String prefix = (String) iterator.next();
          String uri    = (String) map.get(prefix);
          if (uri.equals(namespaceURI)) return prefix;
      }
      return null;
    }

    public Iterator getPrefixes(String namespaceURI) {
      List<String> prefixes = new ArrayList<String>();
      Set<String> keys = map.keySet();
      for (Iterator iterator = keys.iterator(); iterator.hasNext();) 
      {
          String prefix = (String) iterator.next();
          String uri = (String) map.get(prefix);
          if (uri.equals(namespaceURI)) prefixes.add(prefix);
      }
      return prefixes.iterator();
    }

    public void setNamespace(String prefix, String namespaceURI) {
      map.put(prefix, namespaceURI);
    }
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.workflow.impl.bonita.FileDefinition#getEntries()
   */
  public Hashtable<String, byte[]> getEntries() {
    return entries;
  }
  
  /**
   * This constructor builds a file definition based on an Input Stream
   * corresponding to a Business Process Archive. It is typically invoked
   * while deploying a new process in eXo.
   *
   * @param inputStream Input Stream corresponding to the process archive
   */
  public XPDLFileDefinition(InputStream inputStream) throws IOException {
    // Initialization
    JarInputStream jarInputStream = new JarInputStream(inputStream);
    entries                       = new Hashtable<String, byte[]>();
    byte[] buffer                 = new byte[8192];

    // Process each entry of the Jar
    JarEntry entry = null;
    while((entry = jarInputStream.getNextJarEntry()) != null) {
      // Retrieve the name
      String entryName = entry.getName();

      if(entryName.toLowerCase().matches(".*\\.xpdl$|" +
                                         "forms.xml|" +
                                         ".*\\.properties$|" +
                                         ".*\\.vm$|" +
                                         ".*\\.gtmpl$|" +
                                         ".*\\.gif$|" +
                                         ".*\\.class$")) {

        // The entry is to be processed.
        BufferedInputStream in    = new BufferedInputStream(jarInputStream);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int count = 0;

        // Retrieve the corresponding bytes
        while((count = in.read(buffer, 0, buffer.length)) >= 0) {
          out.write(buffer, 0, count);
        }

        // Store the bytes in the hashtable
        entries.put(entryName, out.toByteArray());
      }
    }
  }

  /**
   * This constructor builds a file definition from a Node stored in the JCR 
   * representing a Business Process. It is typically invoked after a restart
   * of eXo.
   * 
   * @param node stored in the JCR which represents the process model
   */
  public XPDLFileDefinition(Node node) {
    // Initialization
    entries       = new Hashtable<String, byte[]>();
    byte[] buffer = new byte[8192];

    try {
      // Retrieves child nodes
      QueryManager qm = node.getSession().getWorkspace().getQueryManager();
      String nodePath = node.getPath();
      Query q = qm.createQuery(
        "select * from nt:base where jcr:path like '" + nodePath + "/%'",
        Query.SQL);      
      QueryResult result = q.execute();
      NodeIterator it = result.getNodes();

      // Process each child nodes
      while (it.hasNext()) {
        // Current node
        Node n = it.nextNode();

        // Process only jcr:content node type
        if (n.getName().equals("jcr:content")) {
          // Retrieve the binary content
          Property data             = n.getProperty("jcr:data");
          InputStream stream        = data.getStream();
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          int read                  = 0 ;
          while((read = stream.read(buffer)) > 0) {
            out.write(buffer, 0, read);
          }

          // Retrieve the path. Remove "/jcr:system/exo:ecm/business processes/
          // model name" +1 to remove the last "/"
          String name_ = n.getPath().substring(node.getPath().length() + 1);
          
          // Remove "jcr:content/" at the end of the string
          String name = name_.substring(0,name_.length()
                        - "jcr:content/".length());
          
          // Put the entry in the hashtable
          entries.put(name, out.toByteArray());
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
