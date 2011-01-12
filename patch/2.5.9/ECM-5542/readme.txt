Summary

    * Status: Impossible to edit a WYSIWYGField with multiple value
    * CCP Issue: CCP-698, Product Jira Issue: ECM-5542.
    * Fixes also: CCP-696/ECM-5549
    * Complexity: Normal

The Proposal
Problem description

What is the problem to fix?

    * When create a new dialog template with multivalued WYSIWYG field, data of this field cannot be saved.

Fix description

How is the problem fixed?

    * In UIDialogForm class, we did not take into account the case that WYSIWYG field is multiple, we just dealt with the single value situation. 
      We modify therefore addWYSIWYGField function in UIDialogForm class: check and manage the multi-value situation.

Patch file: ECM-5542.patch

Tests to perform

Reproduction test: 
* Create a document template which contains a multivalued WYSIWYG field.
* Create a document using this template. Save.
* Open to edit this document: there is only one value of this WYSIWYGField field.

Tests performed at DevLevel
* 
* Create new document of the newly created node type, fill the multi value field. After saving you can see that the multi value field is saved successfully.

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
* Patch validated on behalf of PM.

Support Comment
* Support review: patch validated

QA Feedbacks
*

