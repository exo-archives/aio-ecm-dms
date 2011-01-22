Summary

    * Status: Problem of display in Content Browser
    * CCP Issue: CCP-602, Product Jira Issue: ECM-5522.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * There is a display problem when we want to see a taxonomy that contains many nodes in the portlet ContentBrowser (Path view).

Fix description

How is the problem fixed?

    * Modify the groovy template and stylesheet files to change the headers of category (taxonomy) groups to multi-lines
    * Add some javascript code for showing more nodes on the global header of content browser.

Patch file: ECM-5522.patch

Tests to perform

Reproduction test

    * Create a taxonomy with many nodes (e.g. 10).
    * Set up Content Browser to that taxonomy. Choose Path view.

Tests performed at DevLevel

    * Do the same operations like the Reproduction test => the header of taxonomy groups has multi-lines.
    * With the global header, It has only maximum 3 lines and if exceeds, it has a small button to show more.

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

    * Function or ClassName change

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* PATCH VALIDATED BY PM.

Support Comment
* Support review: patch validated

QA Feedbacks
*

