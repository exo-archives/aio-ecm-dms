Summary

    * Status: User name of the author is displayed instead of the full name
    * CCP Issue: CCP-747, Product Jira Issue: ECM-5569.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * When a user adds an article or comments it, the user name of the author is displayed instead of his name and surname.

Fix description

How is the problem fixed?

    * Get full name from OrganizationService

Patch file: ECM-5569.patch

Tests to perform

Reproduction test

    * Case 1: use FCC
         1. Login as john
         2. Go to Fast Content Creator
         3. Create a new content
         4. Go to Content Browser
            Expected: Author: Name and surname
            Actual: Author: username
         5. Click to the newly created document
         6. Write some comment
            Expected: comment author: Name and surname
            Actual: username
    * Case 2: use File Explorer
         1. Login as john
         2. Go to FE/Site Management/acme.
         3. Create a new folder, named testfolder
         4. Create a new document, named doc1
         5. Comment on the new created document
            Expected: Last comment posted by: Name and surname
            Actual: Last comment posted by: Username
         6. In Content Browser portlet, configure the target folder as testfolder. Access to the doc1. The same observation as in Case 1.

Tests performed at DevLevel

    * Cf. reproduction test
    * Performance test: test 4 cases with Jmeter: before and after applying the patch w/ 100 comments and 100 (1000) users.
      Observation: there's no critical difference between the performance after and before patch applying with the given cases.

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
* To update screen shots in DMS/WCM user guide.

Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: no

Is there a performance risk/cost?
* No according to Jmeter tests.

Validation (PM/Support/QA)

PM Comment
* Validated by PL.

Support Comment
* Support review: Patch validated

QA Feedbacks
*

