Summary

    * Status: End tag for "img" omitted
    * CCP Issue: CCP-668, Product Jira Issue: ECM-5543.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Go to Groups/Site Administration:
    * Validate this page by using W3C validator => ERROR:

      [1]end tag for X omitted, but OMITTAG NO was specified ✉
      You may have neglected to close an element, or perhaps you meant to "self-close" an element, that is, ending it with "/>" instead of ">".
      Line 1791, column 369: end tag for "img" omitted, but OMITTAG NO was specified
      ...mp;ajaxRequest=true')" title="Edit taxonomy Tree" alt="" class="Edit16x16Icon">

Fix description

How is the problem fixed?

    * Modify end of IMG tag from ">" to "/>"

Patch information:
Patch files: ECM-5543.patch

Tests to perform

Reproduction test

    * cf above

Tests performed at DevLevel

    * No

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

    * No

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment
*

Support Comment

    * Support review : patch validated

QA Feedbacks
*

