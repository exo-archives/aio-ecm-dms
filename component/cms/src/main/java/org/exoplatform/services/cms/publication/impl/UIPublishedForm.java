package org.exoplatform.services.cms.publication.impl;

import javax.jcr.Node;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.form.UIForm;


@ComponentConfig (
		lifecycle = UIApplicationLifecycle.class,
		template = "classpath:conf/template/published.gtmpl"
)
public class UIPublishedForm extends UIForm {

	private Node node_;
	
	public UIPublishedForm() throws Exception {
	    addChild(UIPublicationComponent.class, null, null);
   }
	
	public void setNode(Node node) {
	   this.node_=node;
	   getChild(UIPublicationComponent.class).setNode(node);
	 }
	
	
   
	
		
	
}
