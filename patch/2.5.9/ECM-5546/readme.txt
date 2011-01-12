Summary

    * Status: Template Service unit test fails
    * CCP Issue: N/A, Product Jira Issue: ECM-5546.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * On certain computers, build fails because of this error in TestTemplateService unit test:
      -------------------------------------------------------------------------------
      Test set: org.exoplatform.services.ecm.dms.template.TestTemplateService
      -------------------------------------------------------------------------------
      Tests run: 17, Failures: 0, Errors: 1, Skipped: 0, Time elapsed: 0.239 sec <<< FAILURE!
      testGetTemplate(org.exoplatform.services.ecm.dms.template.TestTemplateService)  Time elapsed: 0.017 sec  <<< ERROR!
      javax.jcr.PathNotFoundException: Node not found /exo:ecm/templates/exo:article/views/view1
              at org.exoplatform.services.jcr.impl.core.NodeImpl.getNode(NodeImpl.java:195)
              at org.exoplatform.services.cms.templates.impl.TemplateServiceImpl.getTemplateNode(TemplateServiceImpl.java:581)
              at org.exoplatform.services.cms.templates.impl.TemplateServiceImpl.getTemplate(TemplateServiceImpl.java:368)
              at org.exoplatform.services.ecm.dms.template.TestTemplateService.testGetTemplate(TestTemplateService.java:194)
      It doesn't fail on hudson.

Environment: 
Java(TM) 2 Runtime Environment, Standard Edition (build 1.5.0_22-b03)
Ubuntu 10.04

Fix description

How is the problem fixed?
* In the method testRemoveTemplate of TestTemplateServices, we've removed the default templates, after that we call assert to compare data, so the process will be wrong. 
  To fix this issue, create a new template and try to remove it instead of removing the default one.

Patch file: ECM-5546.patch

Tests to perform

Reproduction test
* Build DMS 2.5.x

Tests performed at DevLevel
1. Compile DMS.
2. Build successful. No test failure ---> OK.

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
* None.

Configuration changes

Configuration changes:
* None

Will previous configuration continue to work?
* Yes.

Risks and impacts

Can this bug fix have any side effects on current client projects?
* N/A

Is there a performance risk/cost?
* No, there isn't risk.

Validation (PM/Support/QA)

PM Comment
* Validated by PM

Support Comment
*Support review : patch validated

QA Feedbacks
*
