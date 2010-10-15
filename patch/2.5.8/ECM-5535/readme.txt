Summary

    * Status: Document display exceeds the WCM Explorer in full screen mode
    * CCP Issue: CCP-467, Product Jira Issue: ECM-5535
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
    * In Firefox when we use the full screen mode (F11), the article display exceeds the File Explorer.

Fix description

How is the problem fixed?
    * Display web content type without out of panel in full screen mode.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch files: ECM-5535.patch

Tests to perform

Reproduction test

   1. Go to File Explorer.
   2. Create a new web content (the text content should be long).
   3. Open the new file.
   4. Go to full screen mode (F11).
   5. Refresh (F5)
   6. The document display exceeds the File Explorer

Tests performed at DevLevel
* Functional test

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
No

Configuration changes

Configuration changes:
No

Will previous configuration continue to work?
Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

No

Is there a performance risk/cost?
No
Validation (PM/Support/QA)

PM Comment

    * VALIDATED BY PM

Support Comment

    * Support review: Validated new patch

QA Feedbacks
*

