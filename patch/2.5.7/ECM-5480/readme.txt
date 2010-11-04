Summary

    * Status: UIOneNodePathSelector must hide the children in the left Panel
    * CCP Issue: CCP-484, Product Jira Issue: ECM-5480
    * Complexity: normal
    
The Proposal
Problem description

What is the problem to fix?

    * Using the UIOneNodePathSelector component we notice that in the left panel, non-folder nodes appear.
      We must keep these nodes hidden to improve the behavior of this component, the non-folder nodes only appear in the right panel. 

Fix description

How is the problem fixed?

    * We allow users to enable or disable the appearance of non-folder nodes in left panel by adding a boolean variable and its getter, setter functions in UIOneNodePathSelector.java file. According to this value, we filter nodes in the left panel to show the appropriated nodes.

Patch information:
Patches files:
ECM-5480.patch 	  	

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
*Patch validated by Support Team


QA Feedbacks

Performed Tests
*

