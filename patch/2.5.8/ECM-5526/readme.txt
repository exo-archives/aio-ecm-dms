Summary

    * Status: Manual refresh of Content browser after adding or deleting a folder
    * CCP Issue: CCP-619, Product Jira Issue: ECM-5526.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
After adding or deleting a folder which is shown in Content Browser, we need to refresh Content Browser manually to update the modification.

    * Step to reproduce:

   1. Create a folder named Test
   2. Create a folder named folder1 in Test
   3. Go to Content Browser. Select Edit item.
   4. Select category path: Test
   5. Select Tree List view
   6. Save. Change to View item.
   7. View content of Test folder (folder1)
   8. Back to FE, delete folder1
   9. Go to Content Browser. folder-1 still exists in the category tree
  10. Click on the root node of the tree to refresh it
  The same thing when we add a folder.


Fix description

How is the problem fixed?

    * If the content path is not null, we rebuild the Tree list using this path and refresh it.

Patch file: ECM-5526.patch

Tests to perform

Reproduction test

    * cf above

Tests performed at DevLevel

    * No

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

    * PATCH VALIDATED BY PM

Support Comment

    * Proposed patch validated

QA Feedbacks
*

