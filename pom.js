eXo.require("eXo.projects.Project")  ;
eXo.require("eXo.projects.Product")  ;

if(eXo.module.tools  == null) eXo.load('pom.js', eXo.env.eXoProjectsDir + "/tools/trunk" ) ;
if(eXo.module.kernel == null) eXo.load('pom.js', eXo.env.eXoProjectsDir + "/kernel/trunk" ) ;
if(eXo.module.core   == null) eXo.load('pom.js', eXo.env.eXoProjectsDir + "/core/trunk" ) ;
if(eXo.module.pc     == null) eXo.load('pom.js', eXo.env.eXoProjectsDir + "/portlet-container/branches/2.0" ) ;
if(eXo.module.jcr     == null) eXo.load('pom.js', eXo.env.eXoProjectsDir + "/jcr/trunk" ) ;
if(eXo.module.portal     == null) eXo.load('pom.js', eXo.env.eXoProjectsDir + "/portal/trunk" ) ;

function ECM(kernel, core, pc, jcr, portal, version) {
  var ecm = this ;
  this.version =  version ;
  this.relativeMavenRepo =  "org/exoplatform/ecm" ;
  this.relativeSRCRepo =  "ecm/trunk" ;
  this.name =  "ecm" ;
  
  this.portlet = {} ;
  this.portlet.ecm = 
    new Project("org.exoplatform.ecm", "exo.ecm.portlet.ecm", "exo-portlet", version).
    addDependency(new Project("org.exoplatform.ecm", "exo.ecm.component.cms", "jar",  version)) .      
    addDependency(new Project("org.exoplatform.ecm", "exo.ecm.component.jcrext", "jar",  version)) .      
    addDependency(new Project("org.exoplatform.ecm", "exo.ecm.component.workflow.api", "jar", version)) .
    addDependency(new Project("org.exoplatform.ecm", "exo.ecm.component.workflow.impl.jbpm.facade", "jar", version)) .
    addDependency(new Project("org.exoplatform.ecm", "exo.ecm.component.workflow.impl.jbpm.engine", "jar", "3.0")) .
    addDependency(new Project("rome", "rome", "jar", "0.8")) .
    addDependency(new Project("com.totsp.feedpod", "itunes-com-podcast", "jar", "0.2")) .
    addDependency(new Project("ical4j", "ical4j", "jar", "0.9.20")) .
    addDependency(new Project("jdom", "jdom", "jar", "1.0")).
    addDependency(new Project("org.apache.ws.commons", "ws-commons-util", "jar", "1.0.1")) ;

  ecm.portlet.workflow = 
    new Project("org.exoplatform.ecm", "exo.ecm.portlet.workflow", "exo-portlet", version);
  
  ecm.web = {}
  ecm.web.ecmportal = 
    new Project("org.exoplatform.ecm", "exo.ecm.web.portal", "exo-portal", version).
    addDependency(portal.web.eXoResources) .
    addDependency(portal.web.eXoMacSkin) .
    addDependency(portal.web.eXoVistaSkin) .
    addDependency(portal.webui.portal) .
    addDependency(jcr.frameworks.command) .
    addDependency(jcr.frameworks.web) ;
}

eXo.module.ecm = new ECM(eXo.module.kernel, eXo.module.core, eXo.module.pc, eXo.module.jcr, eXo.module.portal, "2.0");

function eXoECMProduct() {
  var product = new Product();
  product.name = "eXoECM" ;
  product.portalwar = "portal.war" ;
      
  var tool = eXo.module.tools  ;
  var kernel = eXo.module.kernel ;
  var core = eXo.module.core ;
  var eXoPortletContainer = eXo.module.pc ;
  var eXoJcr = eXo.module.jcr ;
  var portal = eXo.module.portal ;
  var ecm = eXo.module.ecm ;
  
  product.addDependencies(ecm.web.ecmportal) ;
  product.addDependencies(portal.portlet.content) ;
  product.addDependencies(portal.portlet.exoadmin) ;
  product.addDependencies(portal.portlet.web) ;
  product.addDependencies(portal.portlet.site) ;

  product.addDependencies(ecm.portlet.ecm) ;
  product.addDependencies(ecm.portlet.workflow) ;

  product.addDependencies(portal.eXoApplication.web) ;
  product.addDependencies(portal.eXoWidget.web) ;
  product.addDependencies(portal.sample.framework) ;
  
  product.addServerPatch("tomcat", portal.server.tomcat.patch) ;
  product.addServerPatch("jboss",  portal.server.jboss.patch) ;
  product.addServerPatch("jonas",  portal.server.jonas.patch) ;
  
  product.codeRepo = "ecm/trunk" ;
  product.dependencyCodeRepos = "tools/trunk,kernel/trunk,core/trunk,portal/trunk";

  product.module = ecm ;
  product.dependencyModule = [tool, kernel, core, eXoPortletContainer, eXoJcr, portal];
  
  return product ;
}

eXo.product = {} ;
eXo.product.eXoProduct = eXoECMProduct() ;
