Summary

    * Status: Can't edit or remove comments
    * CCP Issue: N/A, Product Jira Issue: ECM-5612.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    *  Can't edit or remove comment

Fix description

How is the problem fixed?

    *  Get the user ID instead of user full name to check user permission whether (s)he can edit or remove comment.

Patch file:

Tests to perform

Reproduction test
* Steps to reproduce:
    * Go to File Explorer
    * Create an article
    * Add comment(s)
    * Show comments
    * There's no icon to Edit and remove our own comment.

Tests performed at DevLevel
* cf above

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
*No

Configuration changes

Configuration changes:
*No

Will previous configuration continue to work?
*Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?
* No

Function or ClassName change
* No

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
*

Support Comment
*

QA Feedbacks
*

