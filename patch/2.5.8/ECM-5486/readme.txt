Summary

    * Status: Update messages of path-not-found, repository-exception
    * CCP Issue: CCP-511, Product Jira Issue: ECM-5486
    * Fix also: CCP-513, ECM-5487.
    * Complexity: Low

The Proposal
Problem description

What is the problem to fix?

    * Update messages of path-not-found and repository-exception.

Fix description

How is the problem fixed?

    * Edit messages of path-not-found and repository-exception in French and English.
    * Change message key from null-exception to path-not-found.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch file: ECM-5486.patch

Tests to perform

Reproduction test
* Steps to show the message of path-not-found:

 1. Logged as root under two browsers (FF & IE).
 2. Under IE browser add new node.
 3. Refresh FF to show the node just created.
 4. Under IE remove this node.
 5. Go to FF browser and click on the node. Message error appears to tell us that this node is deleted.

Tests performed at DevLevel
* No

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
* NO

Configuration changes

Configuration changes:
* NO

Will previous configuration continue to work?
* YES

Risks and impacts

Can this bug fix have any side effects on current client projects?
* Function or ClassName change: no.

Is there a performance risk/cost?
* NO

Validation (PM/Support/QA)

PM Comment
* PATCH VALIDATED BY PM

Support Comment
* Patch validated

QA Feedbacks
*

