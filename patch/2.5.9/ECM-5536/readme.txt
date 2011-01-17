Summary

    * Status: In Content Browser portlet the search doesn't run when hitting 'ENTER' key
    * CCP Issue: CCP-652, Product Jira Issue: ECM-5536.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
*  Hitting Enter "button" doesn't start the search in the content browser. 
   The page seems refreshed but the results do not appear in the list if we do not start the search by clicking on the "Search Now".

Fix description

How is the problem fixed?
*  Catch the key press from search form and execute the action search if the key code = 13 (The code of ENTER key)

Patch file: ECM-5536.patch

Tests to perform

Reproduction test
* cf. above

Tests performed at DevLevel
* The same as in description.

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
* Patch validated by PM

Support Comment
* Support review: patch validated

QA Feedbacks
*
Labels parameters

