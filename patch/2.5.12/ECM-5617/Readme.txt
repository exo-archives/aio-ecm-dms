Summary

    * Status: Problem with Left container paginator for folders names including special characters
    * CCP Issue: CCP-1028, Product Jira Issue: ECM-5617.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Go to Managed Sites->Acme->Documents
2- Create folder called test'e
3- Add a document on it. Copy the added Document more than 10 times
4- Two paginators appear in right and left container
5- Clicking on a different page in the left one, We get js error.
There is a problem of ' encode When render parameter to the js function
Fix description

How is the problem fixed?

    * Encoding the path of node tree.

Tests to perform

Reproduction test

    * cf. above

Tests performed at DevLevel
*

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

    * Function or ClassName change: None

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment

    * N/A

Support Comment

    * SL3VN review: Patch validated

QA Feedbacks
*

