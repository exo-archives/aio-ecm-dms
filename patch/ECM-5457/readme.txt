Summary

    * Status: Exception while "Viewing Metadata"
    * CCP Issue: N/A, Product Jira Issue: ECM-5457
    * Complexity: N/A
    * Impacted Client(s): N/A
    * Client expectations (date/content): N/A

The Proposal
Problem description

What is the problem to fix?

    * The error log is shown in the console

Fix description

How is the problem fixed?

    * Don't allow to print the exception stack to the console.  

Patch information:
Patches files:
ECM-5457.patch 	  	

Tests to perform

Which test should have detect the issue ?
* To reproduce the problem you have to:

    * Add a new Action with a exo:addMetadaAction as action type and dc:elementset as Metadata for a directory (ie:/Documents/Live).
    * Add new document into your directory.
    * View Metadata for the document you created.
    * At the server console you will have this exception:
      [ERROR] UIJCRExplorer - The node cannot be seen <javax.jcr.ItemNotFoundException: Primary item not found for /Documents/Live/new article>javax.jcr.ItemNotFoundException: Primary item not found for /Documents/Live/new article

Is a test missing in the TestCase file ?
* Yes

Added UnitTest ?
* No

Recommended Performance test?
* No


Documentation changes

Where is the documentation for this feature ?
*

Changes Needed:
*


Configuration changes

Is this bug changing the product configuration ?
* No

Describe configuration changes:
* N/A

Previous configuration will continue to work?
* N/A


Risks and impacts

Is there a risk applying this bug fix ?
* No

Is this bug fix can have an impact on current client projects ?
* No

Is there a performance risk/cost?
* No


Validation By PM & Support

PM Comment
*

Support Comment
*


QA Feedbacks

Performed Tests
*

