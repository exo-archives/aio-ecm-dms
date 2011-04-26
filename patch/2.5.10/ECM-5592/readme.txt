Summary

    * Status: Bugs when searching documents which names contain apostrophes in Simple search.
    * CCP Issue: CCP-884, Product Jira Issue: ECM-5592.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Steps to reproduce:

1. Go to portal ACME ->Groups ->Sites Explorer
2. Upload new document which name contains apostrophes
3. Go to parent folder
4. Enter the name of the created document (which contain apostrophes)
-> A pop-up message display "There are some invalid characters in this field. Type another value, please!" (ECM_ERROR_SEARCH.png)
5. Change the name of searching: replace 's by %27s

Fix description

How is the problem fixed?

    * Remove SearchValidator from SimpleSearch StringInput
    * catch Exception and show message about unsupported characters

Patch file: ECM-5592.patch

Tests to perform

Reproduction test

   1. In AIO, Go to portal ACME ->Groups ->Sites Explorer
   2. Upload new document which name contains apostrophes
   3. Go to parent folder
   4. Enter the name of the created document (which contain apostrophes) -> A pop-up message display "There are some invalid characters in this field. Type another value, please!" (ECM_ERROR_SEARCH.png)
   5. Change the name of searching: replace 's by %27s => search result is displayed but incorrect format (name of content contains "%27"s instead of apostrophes)

Tests performed at DevLevel

   1. Do the same tasks like reproduction test
   2. in step 4, Enter the name of the created document which contains apostrophes => have no pop-up message is displayed
   3. in step 5, the name of document is display correctly in search result table. (contains apostrophes)

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

    * Function or ClassName change

Is there a performance risk/cost?
*No

Validation (PM/Support/QA)

PM Comment
* Validated

Support Comment
* Validated

QA Feedbacks
*

