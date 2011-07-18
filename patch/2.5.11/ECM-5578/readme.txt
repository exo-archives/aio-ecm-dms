Summary

    * Status: Automatic scroll in content explorer directory
    * CCP Issue: CCP-770, Product Jira Issue: ECM-5578.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    *  Automatic scroll in content explorer directory

Fix description

How is the problem fixed?

    * Disable scrolling event while mouse moves on Tree Explorer unless "Drag and Drop" event occurs.

Patch file: ECM-5578.patch

Tests to perform

Reproduction test
    * Steps to reproduce:
      When using sites explorer, we open in explorer left part (directories tree) several directories containing many sub-directories so that too many directories are displayed and a vertical scroll bar appears. There are two problems.
      First, directories are scrolling down and up while we only move the mouse.
      Second, when the scroll bar is in the bottom and we click the center button of the three button mouse to move up the directories, the scroll falls automatically.

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
    * Function or ClassName change: no

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment
* Patch validated

Support Comment
* Patch validated

QA Feedbacks
*
