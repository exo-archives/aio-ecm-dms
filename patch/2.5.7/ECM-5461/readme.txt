Summary

    * Status: Change permissions of a document fails
    * CCP Issue: CCPID, Product Jira Issue : ECM-5461
    * Complexity: LOW
   
The Proposal
Problem description

What is the problem to fix?

    * The condition to check permission is not enough

Fix description

How the problem is fixed?

    * Update the condition 
    * Try to save node before updating the grid

Patch information:
Patches files:
ECM-5461.patch 	  	

Tests to perform

Which test should have detect the issue?
* Try to update the permission of a symlink node

Is a test missing in the TestCase file?
* Yes

Added UnitTest?
* No

Recommended Performance test?
* No


Documentation changes

Where is the documentation for this feature?
*

Changes Needed:
*


Configuration changes

Is this bug changing the product configuration?
* No

Describe configuration changes:
* N/A

Previous configuration will continue to work?
* N/A


Risks and impacts

Is there a risk applying this bug fix?
* No

Can this bug fix have an impact on current client projects?
* No

Is there a performance risk/cost?
* No


Validation By PM & Support

PM Comment
*

Support Comment
* Patch validated by Support team : tested on DMS 2.5.x


QA Feedbacks

Performed Tests
*

