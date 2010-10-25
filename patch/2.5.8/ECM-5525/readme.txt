Summary

    * Status: Exception when go to Content Browser after deleting a folder
    * CCP Issue: CCP-616, Product Jira Issue: ECM-5525.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
   
   1. Create a folder named Test
   2. Go to Content Browser, create new configuration using path
   3. Select category path: Test
   4. Save.
   5. Change to View mode. View content of Test folder
   6. Back to File Explorer, delete the test folder
   7. Bach to Content Browser --> exception:

      [ERROR] UIBrowseContainer - PathNotFoundException when get node by path = /Documents/test <javax.jcr.PathNotFoundException: Can't find path: /Documents/test>javax.jcr.PathNotFoundException: Can't find path: /Documents/test

Fix description

How is the problem fixed?

    * If the node path doesn't exist, we set SelectedTabPath and CurrentNodePath to null. This way we avoid the NPE and those attributes are set to the default values.
    * ECM-4771 fixed the same problem in case of Tree list view.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch file:	ECM-5525.patch

Tests to perform

Reproduction test

    * cf above.

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

    * Patch validated

QA Feedbacks
*

