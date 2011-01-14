Summary

    * Status: Script action never executes with remove lifecycle
    * CCP Issue: CCP-501, Product Jira Issue: ECM-5556.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Add an action to a document with "add" lifecycle, it works well and the script is executed.
      But with remove lifecycle, we notice that the script is never executed.

Problem analysis
* Before returning a target node in LinkManagerImpl.getTarget() function, "session.logout();" removes the necessary information of the node which is needed for further processing. 
  In this particular case, the session is logged out, all information about changed nodes of this session is lost. "remove" lifecycle script cannot get the list of removed node. The script never executes.

Fix description

How is the problem fixed?

 * Remove "session.logout();" in LinkManagerImpl.getTarget() function.

Patch file: ECM-5556.patch

Tests to perform

Reproduction test
* Steps to reproduce: in WCM/AIO
1. Go to acme/documents
2. Add a folder
3. Add a document (e.g an Article named Test) in the folder
4. Select the folder and select Manage Actions
5. Select Add Action
6. Select exo:sendMailAcion, remove Lifecycle
7. Input mail address
8. Save
9. Delete Test document
  No notification mail is sent: not OK

Tests performed at DevLevel
* No

Tests performed at QA/Support Level
* No

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
* No

Validation (PM/Support/QA)

PM Comment
* PATCH VALIDATED BY PM

Support Comment
* Support review: patch validated

QA Feedbacks
*

