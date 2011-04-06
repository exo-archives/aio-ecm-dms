Summary

    * Status: Display author comment's name for documents inside WCM Presentation view
    * CCP Issue: CCP-891, Product Jira Issue: ECM-5593.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Display author comment's name for documents inside WCM Presentation view

Fix description

How is the problem fixed?

    * Add missing method getCmtOwner to UIBaseNodePresentation. This class is extended by other one (in ECM-5569) dealing with WCM Presentation view.

Patch file: ECM-5593.patch

Tests to perform

Reproduction test
* Steps to reproduce:

   1. Create an article inside Management sites/acme/documents and publish it
   2. Go to Front office and create a page with SCV
   3. Edit SCV and insert the created article on it.
   4. In BO comment the article and publish it again.
   5. Come back to the created page.
      There is a display problem with the SCV (comment part).
      There's also an error in server console due to missing getCmtOwner method in UIPresentation class:

      portal:Lifecycle - template : /exo:ecm/templates/exo:article/views/view1 <groovy.lang.MissingMethodException: No signature of method: org.exoplatform.wcm.webui.scv.UIPresentation.getCmtOwner() is applicable for argument types: (java.lang.String) values: {"root"}>groovy.lang.MissingMethodException: No signature of method: org.exoplatform.wcm.webui.scv.UIPresentation.getCmtOwner() is applicable for argument types: (java.lang.String) values: {"root"}
      	at org.codehaus.groovy.runtime.ScriptBytecodeAdapter.unwrap(ScriptBytecodeAdapter.java:55)
      	at org.codehaus.groovy.runtime.ScriptBytecodeAdapter.invokeMethodN(ScriptBytecodeAdapter.java:172)
      	at script1301974045097.run(script1301974045097.groovy:32)
              ...

Tests performed at DevLevel
* cf. reproduction test

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
* Function or ClassName change: add getCmtOwner method to UIBaseNodePresentation class

Is there a performance risk/cost?
* N/A
Validation (PM/Support/QA)

PM Comment
* Patch validated by PM

Support Comment

    * Proposed patch validated

QA Feedbacks
*
