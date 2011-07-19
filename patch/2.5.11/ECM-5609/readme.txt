Summary

    * Status: Simple search result contains jcr:FrozenNode
    * CCP Issue: CCP-979, Product Jira Issue: ECM-5609.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    *   When doing a simple search in DMS drives (e.g DMS Administration, Backup Center), some results have the type jcr: frozenNode (for published content). They are not viewable, and their location is "empty".

Fix description

How is the problem fixed?

    *  Filter the search result node list, and do not allow the node in version Storage to display

Patch file: ECM-5609.patch

Tests to perform

Reproduction test
* Steps to reproduce this issue:
In DMS drives (e.g DMS Administration, Backup Center).
1/ upload a file
2/ Publish this file
3/ Make a search by a word existing in the file:

    * The result has the type jcr: frozenNode
    * The result jcr: frozenNode isn't viewable
    * Its location is empty

Tests performed at DevLevel
*cf above

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
* N/A

Function or ClassName change:
* No

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment
* Support review: Patch validated

QA Feedbacks
*
