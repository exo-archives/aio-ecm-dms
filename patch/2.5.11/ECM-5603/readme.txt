Summary

    * Status: Search in taxonomy do not look on associated content
    * CCP Issue: CCP-954, Product Jira Issue: ECM-5603.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Search in taxonomy do not look on associated content

Fix description

How is the problem fixed?

    * Search all the node that matches with keyword and check all the symlink/taxonomyLink if their target matches with the keyword.

Patch file: ECM-5603.patch

Tests to perform

Reproduction test
Case 1:

   1. Login
   2. Go to Managed Sites>acme>documents
   3. Select conditions.doc then Manage Categories
   4. Select category from Acme->world
   5. Go to Acme drive and do simple search on "conditions" or "help". It doesn't return results.

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
* N/A

Configuration changes

Configuration changes:
* N/A

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: no

Is there a performance risk/cost?
* Perform more processes, but that is necessary because the current search gave completed wrong result with symlink/taxonomyLink.

Validation (PM/Support/QA)

PM Comment
* Patch validated

Support Comment
* Patch validated

QA Feedbacks
*
