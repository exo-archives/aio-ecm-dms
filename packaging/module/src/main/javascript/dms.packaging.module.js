eXo.require("eXo.projects.Module") ;
eXo.require("eXo.projects.Product") ;

function getModule(params) {

  var kernel = params.kernel;
  var core = params.core;
  var eXoPortletContainer = params.eXoPortletContainer;
  var ws = params.ws;
  var jcr = params.eXoJcr;
  var portal = params.portal;  
  var module = new Module();
  
  module.version = "${project.version}" ;
  module.relativeMavenRepo =  "org/exoplatform/ecm/dms" ;
  module.relativeSRCRepo =  "ecm/dms" ;
  module.name =  "dms" ;
    
  module.portlet = {}
  module.portlet.dms = 
    new Project("org.exoplatform.ecm.dms.core", "exo.ecm.dms.core.portlet.ecm.core.web", "exo-portlet", module.version).
    addDependency(new Project("org.exoplatform.ecm.dms.core", "exo.ecm.dms.core.component.cms", "jar",  module.version)) .     
    addDependency(new Project("org.exoplatform.ecm.dms.core", "exo.ecm.dms.core.component.deployment", "jar",  module.version)) .    
    addDependency(new Project("org.exoplatform.ecm.dms.core", "exo.ecm.dms.core.component.publication", "jar", module.version)).
    addDependency(new Project("org.exoplatform.ecm.dms.core", "exo.ecm.dms.core.connector.fckeditor", "jar", module.version)).
    addDependency(new Project("org.exoplatform.ecm.dms.core", "exo.ecm.dms.core.webui.dms", "jar", module.version)).
    addDependency(new Project("org.exoplatform.ecm.dms.core", "exo.ecm.dms.core.webui.ext", "jar", module.version)).
    addDependency(new Project("org.exoplatform.ecm.dms.core", "exo.ecm.dms.core.portlet.ecm.ext.config", "jar", module.version)).
    addDependency(new Project("rome", "rome", "jar", "0.9")) .
    addDependency(new Project("com.totsp.feedpod", "itunes-com-podcast", "jar", "0.2")) .
    addDependency(new Project("ical4j", "ical4j", "jar", "0.9.20")) .
    addDependency(new Project("jdom", "jdom", "jar", "1.0")).
    addDependency(new Project("org.apache.ws.commons", "ws-commons-util", "jar", "1.0.1")).
    addDependency(new Project("com.sun.xml.stream", "sjsxp", "jar", "1.0")).
    addDependency(new Project("pdfbox", "pdfbox", "jar", "0.7.2")).
    addDependency(new Project("net.sf.ehcache", "ehcache", "jar", "1.6.0")).    
    addDependency(new Project("org.hibernate", "hibernate", "jar", "3.2.6.ga")).
    addDependency(new Project("org.hibernate", "hibernate-annotations", "jar", "3.4.0.GA")).
    addDependency(new Project("org.hibernate", "hibernate-core", "jar", "3.3.2.GA")).
    addDependency(new Project("javassist", "javassist", "jar", "3.4.GA")).
    addDependency(new Project("org.slf4j", "slf4j-api", "jar", "1.5.6")).
    addDependency(new Project("org.slf4j", "slf4j-jdk14", "jar", "1.5.6"));
    module.portlet.dms.deployName = "ecm";
  
  module.portlet.jcr_console = 
    new Project("org.exoplatform.ecm.dms.core", "exo.ecm.dms.core.portlet.jcr-console", "exo-portlet", module.version).
    addDependency(new Project("exo-weblogic", "exo-weblogic-authproviders", "jar", "1.0")).
	  addDependency(new Project("exo-weblogic", "exo-weblogic-loginmodule", "jar", "1.0")).  
	  addDependency(new Project("commons-logging", "commons-logging", "jar", "1.0.4"));
  
  module.gadgets = 
    new Project("org.exoplatform.ecm.dms.core", "exo.ecm.dms.core.gadgets", "war", module.version).
    addDependency(ws.frameworks.json);  
    module.gadgets.deployName = "eXoDMSGadgets";
  
  module.application = {}
  module.application.rest = new Project("org.exoplatform.ecm.dms.core", "exo.ecm.dms.core.component.publication","jar", module.version).
  	addDependency(ws.frameworks.json);
  
  module.web = {}
  module.web.eXoDMSResources = 
    new Project("org.exoplatform.ecm.dms.core", "exo.ecm.dms.core.web.eXoDMSResources", "war", module.version) ;  
  module.web.eXoDMSResources.deployName = "eXoDMSResources" ;
      
  module.web.dmsportal = 
    new Project("org.exoplatform.ecm.dms.core", "exo.ecm.dms.core.web.portal", "exo-portal", module.version).
    addDependency(portal.web.eXoResources) .
    addDependency(portal.web.eXoMacSkin) .
    addDependency(portal.web.eXoVistaSkin) .
    addDependency(portal.webui.portal) .
    addDependency(jcr.frameworks.command) .
    addDependency(jcr.frameworks.web) ;   
  
  return module;
}
