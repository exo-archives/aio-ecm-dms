Summary

    * Status: The read:any right is given to any new workspace
    * CCP Issue: CCP-481, Product Jira Issue: ECM-5478
    * Complexity: normal
   
The Proposal
Problem description

What is the problem to fix?

    *  When creating a new workspace, the "read" right is given to the any group.

Fix description

How is the problem fixed?

    *  By default, the Root Node of the newly created Workspace is given all rights (read, add node, remove, set property) for the any group. We did not fully remove these rights( read right still exists). So now we just have to remove this right in UIWorkspaceWizard.java

Patch information:
Patches files:
File ECM-5478.patch 	  	

Tests to perform

Which test should have detect the issue?
*

Is a test missing in the TestCase file?
*

Added UnitTest?
*

Recommended Performance test?
*


Documentation changes

Where is the documentation for this feature?
*

Changes Needed:
*


Configuration changes

Is this bug changing the product configuration?
*

Describe configuration changes:
*

Previous configuration will continue to work?
*


Risks and impacts

Is there a risk applying this bug fix?
*

Can this bug fix have an impact on current client projects?
*

Is there a performance risk/cost?
*


Validation By PM & Support

PM Comment
*

Support Comment
* Support validates the provided patch


QA Feedbacks

Performed Tests
*

