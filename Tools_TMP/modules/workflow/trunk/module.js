eXo.require("eXo.projects.Module") ;
eXo.require("eXo.projects.Product") ;

function getModule(params) {

  //eXo.System.info("INFO", "I AM IN WORKFLOW : ");

  var kernel = params.kernel;
  var core = params.core;
  var eXoPortletContainer = params.eXoPortletContainer;
  var ws = params.ws;
  var jcr = params.eXoJcr;
  var portal = params.portal;  
  var module = new Module();
  
  module.version = "trunk" ;
  module.relativeMavenRepo =  "org/exoplatform/workflow" ;
  module.relativeSRCRepo =  "workflow/trunk" ;
  module.name =  "workflow" ;
    
  module.portlet = {}
  
  module.portlet.workflow = 
    new Project("org.exoplatform.workflow", "exo.workflow.portlet.workflow", "exo-portlet", module.version).
    addDependency(new Project("org.exoplatform.workflow", "exo.workflow.webui.workflow", "jar", module.version)).
    addDependency(new Project("org.exoplatform.workflow", "exo.workflow.component.workflow.api", "jar", module.version));     
        
// Temporary deactivate the workflow web portal
/*  module.web = {}
    
  module.web.workflowportal = 
    new Project("org.exoplatform.workflow", "exo.workflow.web.portal", "exo-portal", module.version).
    addDependency(portal.web.eXoResources) .
    addDependency(portal.web.eXoMacSkin) .
    addDependency(portal.web.eXoVistaSkin) .
    addDependency(portal.webui.portal) .
    addDependency(jcr.frameworks.command) .
    addDependency(jcr.frameworks.web) ;   
*/
  
  module.server = {}
  module.server.tomcat = {}
  module.server.tomcat.patch = 
    new Project("org.exoplatform.workflow", "exo.workflow.server.tomcat.patch", "jar", module.version);

  return module;
}
