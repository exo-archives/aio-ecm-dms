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
package org.exoplatform.publication.component;

import javax.jcr.Node;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jun 24, 2008 5:03:44 PM
 */
@ComponentConfig (
    lifecycle = UIApplicationLifecycle.class,
    template = "classpath:conf/templates/published.gtmpl"
)
public class UIPublishedForm extends UIForm {

  private Node node_;

  public UIPublishedForm() throws Exception {
    addChild(UIPublicationComponent.class, null, null);
  }

  public void setNode(Node node) {
    node_=node;
    getChild(UIPublicationComponent.class).setNode(node);
  }

}
