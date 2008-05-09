/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.cms.publication;

import java.util.HashMap;

import javax.jcr.Node;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.webui.form.UIForm;

/**
 * Base class of Publication plugins.
 * Publication plugins implement a publication lifecycle. Each time a new
 * custom lifecycle needs to be defined, a new plugin has to be implemented
 * and registered with the Publication Service.
 * 
 * The getName() method in the parent class is used to identify the lifecycle.
 * The getDescription() method in the parent class is used to describe the
 * lifecycle. Internationalization resource bundles are used in the
 * implementation of the method.
 */
public abstract class PublicationPlugin extends BaseComponentPlugin {
    
    /**
     * Retrieves all possible states in the publication lifecycle
     * 
     * @return an array of Strings giving the names of all possible states
     */
    public abstract String[] getPossibleStates();
    
    /**
     * Change the state of the specified Node.
     * The implementation of this method basically retrieves the current
     * state from the publication mixin of the specified Node. Then, based on
     * the newState, it is able to determine if the update is possible. If
     * yes, appropriate action is made (eg: launch a publication workflow). In
     * all cases, the current state information is updated in the publication
     * mixin of the specified Node.
     * 
     * @param node the Node whose state needs to be changed
     * @param newState the new state.
     * @param context a Hashmap containing contextual information needed
     * to change the state. The information set is defined on a State basis.
     * @throws IncorrectStateUpdateLifecycleException if the update is not
     * allowed
     */
    public abstract void changeState(Node node,
                                     String newState,
                                     HashMap<String, String> context)
        throws IncorrectStateUpdateLifecycleException;

    /**
     * Retrieves the WebUI form corresponding to the current state of the
     * specified node.
     * There are two cases here. Either the form contains read only fields (when
     * the state is supposed to be processed by an external entity such as a
     * Workflow). Or the form has editable fields or buttons (in the case the
     * user can interfere. In that case, some action listeners are leveraged.).
     * In all cases, all UI and listener classes are provided in the JAR
     * corresponding to the PublicationPlugin.
     * The method first inspects the specified Node. If it does not contain
     * a publication mixin, then it throws a NotInPublicationLifecycleException
     * exception. Else, it retrieves the lifecycle name from the mixin,
     * selects the appropriate publication plugin and delegates the call to it.
     * 
     * @param node the Node from which the state UI should be retrieved
     * @return a WebUI form corresponding to the current state and node.
     */
    public abstract UIForm getStateUI(Node node);
    
    /**
     * Retrieves an image showing the lifecycle state of the specified Node.
     * The implementation of this method typically retrieves the current state
     * of the specified Node, then fetches the bytes of an appropriate image
     * found in the jar of the plugin. This image is supposed to be shown in
     * the publication dialog of the JCR File Explorer Portlet. 
     *
     * @param node the node from which the image should be obtained
     * @return an array of bytes corresponding to the image to be shown to the
     * user
     */
    public abstract byte[] getStateImage(Node node);
    
    /**
     * Retrieves description information explaining to the user the current
     * publication state of the specified Node. Possible examples are
     * - "The document has been submitted to the following group for validation:
     * /organization/management.".
     * - "The document has been validated and will be published from
     * May 3rd 10:00am to May 3rd 10:00pm. At that time, it will be unpublished
     * and put in a backup state.".
     * - "The document is in draft state. At any time you can turn it to
     * published state."
     * 
     * The returned message should be obtained from internationalization
     * resource bundles (ie not hardcoded).
     * 
     * @param node the node from which the publication state should be retrieved
     * @return a String giving the current state.
     */
    public abstract String getUserInfo(Node node);
}

