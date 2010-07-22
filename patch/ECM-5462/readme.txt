Summary

    * Status: Problem when returning to home page
    * CCP Issue: CCP-437, Product Jira Issue : ECM-5462
    * Complexity: LOW
    * Impacted Client(s): CG95 and probably all.
    * Client expectations (date/content): N/A

The Proposal
Problem description

What is the problem to fix ?

    * Two different behaviours observed from the same binary deployed on two differents machines (same config). One works well. The other (not always observed) raises an error. 

Fix description

How the problem is fixed ?

    * We have two javascript files: UIWorkspace.js used in Portal level and UIDMSWorkspace.js used in DMS level.

    * In both files, there are two functions showWorkspace() which have the same name and prototype, so these two functions may be called incorrectly.

    * The only difference between them is in function showWorkspace() of UIDMSWorkspace, we call another function : checkAvailableSpace() of ECMUtils.js and it causes error if UIDMSWorkspace.showWorkspace() is called instead of UIWorkspace.showWorkspace() in Portal level. So, to fix this problem, we check in function checkAvailableSpace(), if we are in Portal level, then this function will stop immmediately.

Patch informations:
Patches files:
File ECM-5462.patch 	  	

Tests to perform

Which test should have detect the issue ?
*

Is a test missing in the TestCase file ?
*

Added UnitTest ?
*

Recommended Performance test?
*


Documentation changes

Where is the documentation for this feature ?
*

Changes Needed:
*


Configuration changes

Is this bug changing the product configuration ?
*

Describe configuration changes:
*

Previous configuration will continue to work?
*


Risks and impacts

Is there a risk applying this bug fix ?
*

Is this bug fix can have an impact on current client projects ?
*

Is there a performance risk/cost?
*


Validation By PM & Support

PM Comment
*

Support Comment
* We asked customer to validate this patch, he didn't answer. We will validate this issue and will open another one if there will be other problems related to it.


QA Feedbacks

Performed Tests
*

