package org.exoplatform.services.ecm.publication.plugin.staticdirect;

import javax.jcr.Node;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.form.UIForm;


@ComponentConfig (
		lifecycle = UIApplicationLifecycle.class,
		template = "classpath:conf/templates/staticdirect/nonPublished.gtmpl"
)
public class UINonPublishedForm extends UIForm {

	private Node node_;
	
	public UINonPublishedForm() throws Exception {
	    addChild(UIPublicationComponent.class, null, null);
   }
	
	public void setNode(Node node) {
	   this.node_=node;
	   getChild(UIPublicationComponent.class).setNode(node);
	 }
	
	
}
