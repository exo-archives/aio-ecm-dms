Summary

    * Status: In the content browser portlet the search doesn't run when hitting 'ENTER' key
    * CCP Issue: CCP-652, Product Jira Issue: ECM-5536.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Hitting Enter "button" doesn't start the search in the content browser, however the page seems to be refreshing, but the results do not appear in the list if we do not start the search by clicking on the "Search Now".

Fix description

How is the problem fixed?

    * Add onKeyPress event for input field (use attachEvent() function in IE and setAttribute() function in orther browser) 
    * Catch the key press from search form and execute the action search if the key code = 13 (The code of enter key)

Patch file: ECM-5536.patch

Tests to perform

Reproduction test

    * In ContentBrowser portlet, click Search button
    * Fill data into input field, hit "Enter" => search process doesn't start.

Tests performed at DevLevel

    * Do the same tasks like Reproduction Test => Search process starts

Tests performed at QA/Support Level
* No

Documentation changes

Documentation changes:
* None

Configuration changes

Configuration changes:
* None

Will previous configuration continue to work?
* Yes.

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

Is there a performance risk/cost?
* No, there is no risk.

Validation (PM/Support/QA)

PM Comment
* Validated by PM

Support Comment
* Patch validated

QA Feedbacks
*

