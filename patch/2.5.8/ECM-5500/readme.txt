Summary

    * Status: Search result error on exo:article document if it contains accented characters in summary or content
    * CCP Issue: CCP-552, Product Jira Issue: ECM-5500.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * To reproduce the problem:

   1. Add a new exo:article document.
   2. Put some accented characters in summary and/or content.
   3. Use Advanced search in JCR Explorer
   4. Search this document by those accented characters => No result

Fix description

How is the problem fixed?

    * Problem with document that contains accented characters in summary or content.
    * Problem with XPATH Query when users search on node with path contains whitespace characters (Convert to SQL Query)

Patch information:

    * Final files to use should be attached to this page (Jira is for the dicussion)

Patch file: ECM-5500.patch

Reproduction test
* Cf. above

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

    * Function or ClassName change: no

Is there a performance risk/cost?
*
Validation (PM/Support/QA)

PM Comment
* PATCH VALIDATED BY PM

Support Comment
* Patch validated

QA Feedbacks
*
