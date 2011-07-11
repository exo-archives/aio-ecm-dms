Summary

    * Status: ECM Portlet's titles are not translated in French
    * CCP Issue: CCP-929, Product Jira Issue: ECM-5571.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Portlet's titles are not translated in French

Fix description

How is the problem fixed?

    * Add javax.portlet.title key in .xml files of FastContentCreator, JCRExplorer, BrowseContent to translate portlet's title.

Patch file: ECM-5571.patch

Tests to perform

Reproduction test
*Steps to reproduce:

1. Login
2. Change language to French

    * Case 1:
      Go to Groups/Content Browser
      "Content Browser" should be translated: "Navigateur de contenus"
    * Case 2:
      Go to Groups/Fast Content Creator
      "Fast Content Creator" should be translated: "Création rapide de contenu"
    * Case 3:
      Go to Group/File Explorer
      "File Explorer" should be translated: "Explorateur de contenus"

Tests performed at DevLevel
*

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

    * Function or ClassName change: no

Is there a performance risk/cost?
*No

Validation (PM/Support/QA)

PM Comment
* Patch validated

Support Comment
* Patch validated

QA Feedbacks
*
