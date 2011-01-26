Summary

    * Status: Problem of opening a binary file with name containing illegal JCR character ' (coverflow view)
    * CCP Issue: N/A, Product Jira Issue: ECM-5493.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
   1. Choose Cover Flow view for acme drive
   2. Upload a binary file with name containing apostrophe ' (this character not supported by JCR).
   3. In the right panel, double click to the file icon. A popup message appears.

Fix description

How is the problem fixed?

    * Escape illegal JCR character when generating action link on the thumbnail image. 
    * Fixed the problem of frames break while viewing Cover Flow on stylesheet.

Patch file: ECM-5493.patch

Tests to perform

Reproduction test
   1. Choose Cover Flow view for acme drive
   2. Upload a binary file with name containing apostrophe ' (this character not supported by JCR).
   3. In the right panel, double click to the file icon. A popup message appears

Tests performed at DevLevel
* Follow the steps of reproduction test and a variety of related cases

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
* Function or ClassName change: no

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch validated on behalf of PM

Support Comment
* Patch validated by Support team

QA Feedbacks
*
