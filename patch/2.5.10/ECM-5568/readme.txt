Summary

    * Status: Labels aren't translated in French in Manage Actions
    * CCP Issue: CCP-737, Product Jira Issue: ECM-5568.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
*  Labels aren't translated in French in Manage Actions

Fix description

How is the problem fixed?
*  In dialogs_*.xml files add missing keys.

Patch file: ECM-5568.patch

Tests to perform

Reproduction test
* Step to reproduce:
1. Login
2. Change language to French
3. Go to Sites Explorer, acme/documents
4. Select a document
5. Manage actions
6. Add Action
Create an Action of Type: Select a type (e.g exo:autoVersioning)
Values in Life cycle field aren't translated in French: "modify" -> "modifier"; "User Action" -> "Action d'utilisateur"; "add" -> "ajouter"; "remove" -> "supprimer"; "schedule" -> "plan"

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

    * Function or ClassName change

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch approved

Support Comment
* Patch validated

QA Feedbacks
*
