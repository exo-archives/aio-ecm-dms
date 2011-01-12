Summary

    * Status: Date field is shown instead of text area field when property is multiple
    * CCP Issue: CCP-697, Product Jira Issue: ECM-5550.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
* A date field appears instead of text area field when property is multiple.

Fix description

How is the problem fixed?
* Use UIFormTextAreaInput.class instead of UIFormDateTimeInput.class

Patch file: ECM-5550.patch

Tests to perform

Reproduction test
* Create a template containing a text area field. In dialog1.gtmpl, set "multiValues=true" for this field.
* Create a document using this template.
  Observation: A mini calendar appears when focusing on this text area.

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
* N/A

Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?
* N/A

Is there a performance risk/cost?
* N/A

Validation (PM/Support/QA)

PM Comment
* Patch validated on behalf of PM

Support Comment
* Support review: patch validated

QA Feedbacks
*

