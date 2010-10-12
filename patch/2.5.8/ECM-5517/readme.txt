Summary

    * Status: Modify the length value of tag name
    * CCP Issue: CCP-546, Product Jira Issue: ECM-5517
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
 Currently we cannot exceed 20 characters as tag name.
 Customer wants to increase this value to 30 characters.
 
Fix description

How is the problem fixed?

    * Update UITaggingForm.java to allow tag name up to 30 characters. Update messages in resource bundles.

Patch information:

    * Final files to use should be attached to this page (Jira is for the dicussion)

Patch files: ECM-5517.patch

Tests to perform

Reproduction test

    * Cf. above

Tests performed at DevLevel

    * Functional test

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

    * VALIDATED BY PM

Support Comment

    * Validated by Support Team

QA Feedbacks
*

