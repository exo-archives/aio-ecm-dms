Summary

    * Status: Date field is reset after clicking on "+" to add a new value in another field
    * CCP Issue: CCP-523, Product Jira Issue: ECM-5476. Depends on PORTAL-3822.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Date field is reset after clicking on "+" to add a new value in another field

Fix description

How is the problem fixed?

    * Set the default value for UIFormDateTimeInput as the selected date time if this input is multiple value.

Patch information:
Patch files:
ECM-5476.patch	  	

Tests to perform

Which test should have detected the issue ?

    * Upload a document that does have metadata (pdf, word, etc)
      When editing the metadata: date field is reset after clicking on "+" to add a new value in another field.
      Is a test missing in the TestCase file ?
    * Yes

Added UnitTest ?

    * No

Recommended Performance test?

    * No

Documentation changes

Where is the documentation for this feature?
*

Changes Needed:
*
Configuration changes

Is this bug changing the product configuration?

    * No

Describe configuration changes:

    * N/A

Will previous configuration continue to work?

    * N/A

Risks and impacts

Is there a risk applying this bug fix?

    * No

Can this bug fix have an impact on current client projects?

    * No

Is there a performance risk/cost?

    * No

Validation By PM & Support

PM Comment

    * Validated by PM

Support Comment

    * Validated patch by Support Team

QA Feedbacks

Performed Tests
*

