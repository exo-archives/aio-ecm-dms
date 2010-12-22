Summary

    * Status: Download binary with name containing illegal jcr characters
    * CCP Issue: CCP-404, Product Jira Issue: ECM-5453.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

   1. Impossible to download a binary file with name containing a character like '
   2. In Firefox, impossible to "Copy URL to clipboard".

Fix description

How is the problem fixed?

    * Allow binary downloading with name includes JCR illegal characters.
    * Correct the box size of "Copy URL to clipboard" item.

Patch information:
Patch files: ECM-5453

Tests to perform

Reproduction test

   1. Using FireFox, upload a binary file with name containing a character like '
   2. Right click on the uploaded file
   3. Select "Download And Allow Edition": no response
   4. Select "Copy URL to clipboard" (there is also a problem, it is not supported in FF),

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*


Documentation changes

Documentation changes:
* Update these features in DMS/WCM user guides.


Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* Yes


Risks and impacts

Can this bug fix have any side effects on current client projects?
* No

Is there a performance risk/cost?
* No


Validation (PM/Support/QA)

PM Comment

    * PATCH VALIDATED BY PM

Support Comment

    * validated by support team

QA Feedbacks
*

