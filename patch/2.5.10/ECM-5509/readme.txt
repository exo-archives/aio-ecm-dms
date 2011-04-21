Summary

    * Status: Unable to use validator for CalendarField
    * CCP Issue: CCP-624, Product Jira Issue: ECM-5509.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
In dialog.gtmpl, we can add validator to verify data before saving document (e.g. validate=empty). We can also add a specific validator.

But for calendarField, and some other one, the parameter "validate" is not taken into account.
Fix description

How is the problem fixed?

    * Add DateTime Validator when creating calendar field.

Patch file: ECM-5509.patch

Tests to perform

Reproduction test

   1. Login acme by admin
   2. Choose Groups/Content Explorer
   3. In Sites Management, add a podcast document
   4. Enter value for Name and Link fields. Publish Date field is blank
   5. Click Save as Draft button
   6. DateTime Validator error doesn't display and Podcast is still created => Not OK

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
* N/A

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch validated

Support Comment
* Patch validated

QA Feedbacks
*
