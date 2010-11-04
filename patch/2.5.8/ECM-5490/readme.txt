Summary

    * Status: Problem in UIJCRExplorer when using SessionLeakDetector
    * CCP Issue: CCP-530, Product Jira Issue: ECM-5490.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Using sessionLeakDetector with AIO-1.6.5, we get the following stackTrace after navigating in File Explorer:

      java.lang.Exception
              at org.exoplatform.services.jcr.impl.core.SessionReference.<init>(SessionReference.java:113)
              at org.exoplatform.services.jcr.impl.core.TrackedXASession.<init>(TrackedXASession.java:32)
              at org.exoplatform.services.jcr.impl.core.SessionFactory.createSession(SessionFactory.java:128)
              at org.exoplatform.services.jcr.impl.core.RepositoryImpl.internalLogin(RepositoryImpl.java:521)
              at org.exoplatform.services.jcr.impl.core.RepositoryImpl.login(RepositoryImpl.java:484)
              at org.exoplatform.services.jcr.impl.core.RepositoryImpl.login(RepositoryImpl.java:464)
              at org.exoplatform.services.jcr.ext.common.SessionProvider.getSession(SessionProvider.java:155)
              at org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer.getSession(UIJCRExplorer.java:265)
              at org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer.getCurrentNode(UIJCRExplorer.java:169)
              at org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer.isPreferenceNode(UIJCRExplorer.java:706)
              at org.exoplatform.ecm.webui.component.explorer.UIWorkingArea.isPreferenceNode(UIWorkingArea.java:183)
              at org.exoplatform.ecm.webui.component.explorer.UIDocumentInfo.isPreferenceNode(UIDocumentInfo.java:293)

      getSession method of UIJCRExplorer is using getSession(String workspaceName, ManageableRepository repository) of org.exoplatform.services.jcr.ext.common.SessionProvider. The mentioned method is getting session from the cache. We increased the MaxAge of sessionLeakDetector guessing that the opened session is conserved to the cache and then distructed. It wasn't the case since the same stackTrace appears with sessionLeakDetector.
      So, We are wondering if this is really a memory leak problem.

    * Environment: AIO-1.6.5, DMS-2.5.6

Fix description

How is the problem fixed?

    * Session was created in the services but not yet logged out. So the solution is add the blocks code inside try/catch and log out session in the finally.

Patch information:
Patch files:
ECM-5490.patch

Tests to perform

Reproduction test

1. Add session leak detector configuration inside eXo.sh and run tomcat server
2. Create a document and try to add comment and relation
3. Wait for a while and we will see the exception which raised by leak detector.

Tests performed at DevLevel
* Same steps to reproduction test and no exception appear in console

Tests performed at QA/Support Level
*


Documentation changes

Documentation changes:
* None


Configuration changes

Configuration changes:
* None

Will previous configuration continue to work?
* Yes


Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

Is there a performance risk/cost?
* None


Validation (PM/Support/QA)

PM Comment

    * VALIDATED BY PM

Support Comment

    * Support review : Proposed Patch Validated

QA Feedbacks
*

