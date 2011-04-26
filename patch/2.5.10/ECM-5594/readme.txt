Summary

    * Status: Bugs when displaying pagination results of search in drive acme
    * CCP Issue: CCP-909, Product Jira Issue: ECM-5594.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Log in as root
    * Go to Sites Explorer
    * Click on the drive "acme"
    * Go to the category "World"
    * Enter the keyword "ipsum"
    * The search returns 2 contents while paging displays 16 results.
    * Please see "pagination.png".

Fix description

How is the problem fixed?

    * Filtering search result before assigning it to PageIterator
    * Resize the height of SimpleSearchResult element when display search result

Patch file: ECM-5594.patch

Tests to perform

Reproduction test

    * cf above

Tests performed at DevLevel

    * cf above

Tests performed at QA/Support Level

    * cf above

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

    * N/A.

Function or ClassName change

    * core/portlet/ecm/core/main/src/main/java/org/exoplatform/ecm/webui/component/explorer/search/UISearchResult.java
    * core/portlet/ecm/core/web/src/main/webapp/groovy/webui/component/explorer/search/UISearchResult.gtmpl

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment
* Validated

Support Comment
* Validated

QA Feedbacks
*

