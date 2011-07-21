Summary

    * Status: NPE when Content Browser displays a category
    * CCP Issue: N/A, Product Jira Issue: ECM-5611.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * NPE and UI error when Content Browser is configured to display a content whose owner is _system. That's a side effect of ECM-5569.

Fix description

How is the problem fixed?

    * Take care the situation when userId = "system". In this case, User object for this userId does not exist. It caused the exception before. Also, when a symlink is displayed, we should print the owner of the target node, not the owner of symlink (It is alway "system")

Patch file: ECM-5611.patch

Tests to perform

Reproduction test
Steps to reproduce:

   1. Go to Content Browser (this portlet is available in AIO. In WCM standalone, you might need to log in as root, import ecm portlet, then add Content Browser portlet to a page)
   2. Edit the configuration:
      Case 1: configure CB to display a category
      Workspace: collaboration
      Categories Path: /sites content/live/acme/categories/acme
   3. Return to View mode: UI error and exception in the server console:

    * Template: PathList: UI error and error in server console

                  [ERROR] portal:Lifecycle - template : /exo:ecm/views/templates/content-browser/path/PathList <java.lang.NullPointerException>java.lang.NullPointerException
                  	at org.exoplatform.ecm.webui.component.browsecontent.UIBrowseContainer.getOwner(UIBrowseContainer.java:563)
                  	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
                  	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
                  ...

    * Template: TreeList: similar result: UI error and error in server console

                  [ERROR] portal:Lifecycle - template : /exo:ecm/views/templates/content-browser/path/TreeList <java.lang.NullPointerException>java.lang.NullPointerException
                  	at org.exoplatform.ecm.webui.component.browsecontent.UIBrowseContainer.getOwner(UIBrowseContainer.java:563)
                  	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
                  	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
                  	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
                  ...

      Case 2: configure CB to display a template folder, for example:

    * Workspace: dms-system
    * Categories path: /exo:ecm/templates/exo:addMetadataAction

The owner of these templates is _system.
The similar result as in Case 1 is reproduced in WCM 1.2.9 and AIO 1.6.9.
In DMS 2.5.8, there isn't exception in server console, but in CB, there's no content, see DMS-CB-addMetadataAction.png.

Tests performed at DevLevel
* cf above

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
*No

Configuration changes

Configuration changes:
*No

Will previous configuration continue to work?
*Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?
* Function or ClassName change: no

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch validated

Support Comment
* Patch validated

QA Feedbacks
*

