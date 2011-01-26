Summary

    * Status: The clipboard doesn't work correctly with multiple pastes
    * CCP Issue: N/A, Product Jira Issue: ECM-5508.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
When Copy & Paste file in Sites Explorer, the clipboard doesn't work properly
1. Create 2 files aaa and bbb
2. Duplicate file aaa
3. Select 2 files aaa
4. Copy & paste 2 aaa files in the same folder. So we have 4 aaa files in this folder
5. Copy & paste file bbb in the same folder => BUG: 2 last aaa files appear instead of file bbb.

Fix description
Problem analysis
    * The problem occurrs when multiple paste functions are called. 
      This is because when every multiple paste action is finished, the virtual clipboard list does not clear out the history of the past commands to replace with the new ones.

How is the problem fixed?
    * Clear out the command history from the virtual clipboards after every multiple paste action is done.

Patch file: ECM-5508.patch

Tests to perform

Reproduction test
* Cf. above

Tests performed at DevLevel
* Repeat the steps of reproduction test 
* Check particular multiple paste cases. 

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

    * Function or ClassName change: none

Is there a performance risk/cost?
* no

Validation (PM/Support/QA)

PM Comment
* Patch validated on behalf of PM

Support Comment
* Support review : patch validated

QA Feedbacks
*

