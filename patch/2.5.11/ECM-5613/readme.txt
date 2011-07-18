Summary

    * Status: Unknown error when create new drive with special character
    * CCP Issue: N/A, Product Jira Issue: ECM-5613.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Unknown error when create new drive with special character

Fix description

How is the problem fixed?

    * Automatic remove space characters at leading and tailing of driver name.
    * Add slash and quote to illegal characters for driver name

Patch file: ECM-5613.patch

Tests to perform

Reproduction test

   1. Exception appears when try to create new drive with space character at leading and tailing of driver name.
   2. Same exception if try to create new drive whose name contains slash character ( / ) and/or quote ( " )

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:

    * No

Configuration changes

Configuration changes:

    * N/A

Will previous configuration continue to work?

    * Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * N/A

Is there a performance risk/cost?

    * N/A

Validation (PM/Support/QA)

PM Comment

    * Patch validated

Support Comment

    * Patch validated

QA Feedbacks
*
