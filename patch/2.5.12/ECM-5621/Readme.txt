Summary

    * Status: Adding an exo:createRSSFeedAction is not working
    * CCP Issue: CCP-1170, Product Jira Issue: ECM-5621.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Steps to reproduce :

    * Go under drive "Managed sites" and open "/classic/documents" folder
    * Under Admin View, open "Manage actions"
    * Create new action of type "exo:createRSSFeedAction", call it test
    * Fill in the fields of the RSS feed, choose "add" life cycle
    * Save and close the pop-up
    * Now create a new document of type "exo:article" under "/classic/documents" folder
    * Go to" http://localhost:8080/rest/jcr/repository/collaboration/Feeds/rss/test" (if you have already named your action as test)
    * The RSS Feed is created and shows a link to the created document
    * Click on the link, we are redirected to the home page of the portal and not to the document's link

Fix description

How is the problem fixed?

    * Get the correct navigation node of Portlet Name to retrieve the exactly URL.

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
* Support review: Patch validated

QA Feedbacks
*

