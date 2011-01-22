Summary

    * Status: Impossible to save changes in editable node properties
    * CCP Issue: N/A, Product Jira Issue: ECM-5511.
    * Fixes also CCP-527/ECM-5488
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

It is possible to edit some node properties. However, these changes are not saved.
- exo:folksonomy
- exo:title
- publication:history
- etc.

Fix description

How is the problem fixed?
* Change the condition to check the possibility of setting value for the edited property.

Patch file: ECM-5511.patch

Tests to perform

Reproduction test
1. Go to Sites Explorer -> Sites Management -> acme -> documents.
2. Select conditions.doc document. Create some tags for it.
3. Click on View Node Properties to show all properties.
4. Try to edit exo:folksonomy property, we will get as value a list of created tags.
5. Click Delete button to remove some of the values -> it seems work fine. However, when you click Save button, these tags still appear in exo:folksonomy.

Tests performed at DevLevel
1. Log in
2. Add some tags (folksonomies) for a document.
3. Click on View Node Properties to show all properties.
4. Click Edit button to edit exo:folksonomy property, we will get as value a list of created tags.
5. Click Delete button to remove some values of the property
6. Click Save => value of exo:folksonomy property was edited successfully.

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
* No

Validation (PM/Support/QA)

PM Comment
* Validated by PM

Support Comment
* Patch validated

QA Feedbacks
*

