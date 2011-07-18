Summary

    * Status: Unknown error when add permission for node
    * CCP Issue: CCP-933, Product Jira Issue: ECM-5581.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    *  Unknown error when add permission for node

Fix description

How is the problem fixed?

    * The problem occurs when target node doesn't contain any symlink nodes. So to fix this problem we have to check in the case symlink has not been created yet, an empty array list is returned.

Patch file: ECM-5581.patch

Tests to perform

Reproduction test
* Steps to reproduce:

    * Go to Site Explorer, select 1 drive
    * Select 1 node
    * Add permission for this node --> Unknown error
      ?
      [ERROR] portal:UIPortalApplication - Error during the processAction phase <javax.jcr.UnsupportedRepositoryOperationException: Node /sites content/live/acme is not referenceable>javax.jcr.UnsupportedRepositoryOperationException: Node /sites content/live/acme is not referenceable
      at org.exoplatform.services.jcr.impl.core.NodeImpl.getUUID(NodeImpl.java:430)
      at org.exoplatform.services.cms.link.LinkUtils.getAllSymlinks(LinkUtils.java:204)
      at org.exoplatform.ecm.webui.component.explorer.popup.info.UIPermissionForm$SaveActionListener.execute(UIPermissionForm.java:273)
      at org.exoplatform.webui.event.Event.broadcast(Event.java:52)
      at org.exoplatform.webui.core.lifecycle.UIFormLifecycle.processAction(UIFormLifecycle.java:101)
      at org.exoplatform.webui.core.UIComponent.processAction(UIComponent.java:91)
      at org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle.processAction(UIApplicationLifecycle.java:45)
      at org.exoplatform.webui.core.UIComponent.processAction(UIComponent.java:91)
      at org.exoplatform.webui.core.UIApplication.processAction(UIApplication.java:76)
      at org.exoplatform.webui.application.portlet.PortletApplication.processAction(PortletApplication.java:142)

Tests performed at DevLevel
* Steps to test after apply the patch

    * Go to Site Explorer, select 1 drive
    * Select 1 node
    * Add permission for this node
    * Permission added well and no exception in the console

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
* No

Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* Yes
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: LinkUtils.java class

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch validated

Support Comment
* Patch validated

QA Feedbacks
*
