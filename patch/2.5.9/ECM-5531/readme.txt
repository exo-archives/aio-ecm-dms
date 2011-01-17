Summary

    * Status: Display's problem of long name of taxonomy
    * CCP Issue: CCP-724, Product Jira Issue: ECM-5531.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

When creating a taxonomy whose name exceeds 20 characters, there is a display problem of this name in the taxonomy tree.

Fix description

How is the problem fixed?
* When the taxonomy's name length exceeds 20 characters, display the first 18 characters and "...". 
  The full name of taxonomy is shown in the tool-tip.

Patch file: ECM-5531.patch

Tests to perform

Reproduction test
* Add a taxonomy (in cms, calendar) with the length exceeding 20 characters.

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
* N/A

Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * N/A

Is there a performance risk/cost?
* N/A

Validation (PM/Support/QA)

PM Comment
* Patch validated on behalf of PM

Support Comment
* Support review: patch validated

QA Feedbacks
*

