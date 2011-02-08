Summary

    * Status: Problem with document name having space at the beginning and the end
    * CCP Issue: N/A, Product Jira Issue: ECM-5567.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
         1. Login as john
         2. Go to Site Explorer/Site Management/acme/categories/acme/Business
         3. Create a new document, its name contains space at the beginning and/or the end (eg: " Name ")
         5. Publish this document
         6. Create a page, add a CLV to this page
         7. Change to Edit Mode, edit the new CLV. Select Manual mode.
         8. Click to Select Folder Path
         9. Go to General Drivers/acme/Business
            The new document at right panel appears as "%20Name%20" => NOT OK
        10. Select this document; click Save
            The path of this document in ContentPath table as:
            /sites content/live/acme/categories/acme/Business/%20Name%20
        11. Click Save
        12. Click on the name of document in CLV => Pop-up message: content-not-found => NOT OK

Fix description

How is the problem fixed?
*  Use trim() function to remove the beginning and ending space character of document name

Patch file: ECM-5567.patch

Tests to perform

Reproduction test
* Cf. above

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
* Function or ClassName change: no

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch validated on behalf of PM

Support Comment
* Support review: patch validated

QA Feedbacks
*

