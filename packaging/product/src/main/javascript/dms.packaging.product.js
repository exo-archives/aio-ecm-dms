eXo.require("eXo.projects.Module") ;
eXo.require("eXo.projects.Product") ;

function getProduct(version) {
  var product = new Product();
  
  product.name = "eXoDMS" ;
  product.portalwar = "portal.war" ;
  product.codeRepo = "ecm/dms" ;
  product.useContentvalidation = true;
  product.version = "${project.version}" ;
  product.contentvalidationVersion = "${project.version}";
  product.workflowVersion = "${org.exoplatform.ecm.workflow.version}" ;
  product.serverPluginVersion = "${org.exoplatform.portal.version}" ;
  product.workflowJbpmVersion = "${org.exoplatform.ecm.jbpm.version}";
  product.workflowBonitaVersion = "${org.bonita.version}";

  var kernel = Module.GetModule("kernel") ;
  var core = Module.GetModule("core") ;
  var ws = Module.GetModule("ws");
  var eXoJcr = Module.GetModule("jcr", {kernel : kernel, core : core, ws : ws}) ;
  var portal = Module.GetModule("portal", {kernel : kernel, ws:ws, core : core, eXoJcr : eXoJcr });
  var dms = Module.GetModule("dms", {kernel : kernel, core : core, ws : ws, eXoJcr : eXoJcr, portal : portal});

	portal.starter = new Project("org.exoplatform.portal", "exo.portal.starter.war", "war", portal.version);
  portal.starter.deployName = "starter";
  product.addDependencies(portal.starter);  
  //product.addDependencies(dms.web.dmsportal);
  
  product.addDependencies(portal.web.rest);
	product.addDependencies(portal.portlet.exoadmin);
  product.addDependencies(portal.portlet.web);
	product.addDependencies(portal.web.portal);
  product.addDependencies(portal.portlet.dashboard);
	product.addDependencies(portal.eXoGadgetServer);
	product.addDependencies(portal.eXoGadgets);  
	product.addDependencies(portal.webui.portal);
  product.addDependencies(portal.web.eXoResources);
	
	product.addDependencies(dms.web.eXoDMSResources);
  product.addDependencies(dms.portlet.dms);
  product.addDependencies(dms.portlet.jcr_console);
  product.addDependencies(dms.gadgets);
	product.addDependencies(dms.extension.webapp);
	
	portal.starter = new Project("org.exoplatform.portal", "exo.portal.starter.war", "war", portal.version);
  portal.starter.deployName = "starter";
  product.addDependencies(portal.starter);

    
  product.addServerPatch("tomcat", portal.server.tomcat.patch) ;
  product.addServerPatch("jboss",  portal.server.jboss.patch) ;
  product.addServerPatch("jbossear",  portal.server.jbossear.patch) ;  
	
	/* cleanup duplicated lib */
  product.removeDependency(new Project("commons-collections", "commons-collections", "jar", "3.1"));
	product.removeDependency(new Project("commons-lang", "commons-lang", "jar", "2.3"));

  product.module = dms ;
  product.dependencyModule = [kernel, core, ws, eXoJcr, portal];

  return product ;
}
