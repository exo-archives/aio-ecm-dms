Summary

    * Status: Bug when deleting and recreating a document in a category
    * CCP Issue: CCP-879, Product Jira Issue: ECM-5590.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    *  Bug when deleting and recreating a document in a category

Fix description

How is the problem fixed?

    *  create a Job which used to clean the orphan symlinks

Patch files: ECM-5590.patch

Tests to perform

Reproduction test

    * In a category A, upload a document.
    * Open the document and click on "manage categories".
    * Add a category B to the document.
    * Delete the real document in Site explorer/Sites mangament/acme/webcontents/yyyy/MM/dd.
    * Check the symlink in the category A folder => It still exists => NOK

Tests performed at DevLevel

    * Do the same tasks as the reproduction test => the symlinks is deleted when the real document is deleted.

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

    * Function or ClassName change: None

Is there a performance risk/cost?
*Yes
Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment
* Support review: Patch validated

QA Feedbacks
*

