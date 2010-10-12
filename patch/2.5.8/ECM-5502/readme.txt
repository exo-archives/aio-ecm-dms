Summary

    * Status: Bad behavior of page iterator in JCR explorer
    * CCP Issue: CCP-553, Product Jira Issue: ECM-5502.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * To reproduce the problem:

   1. Under acme/documents, import node in the attachment of ECM-5502.
   2. Open the node just imported and edit the preference setting, choose 30 nodes per page as value.
   3. Under FF, the page iterator is at the right of the panel, and in IE it's in the left and over the last file details.

Fix description

How is the problem fixed?

    * Show page iterator without having interlace.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch files: ECM-5502.patch

Tests to perform

Reproduction test
* Above

Tests performed at DevLevel
* Functional test

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
* No

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Validated by PM.

Support Comment
* Support review: patch validated.

QA Feedbacks
*

