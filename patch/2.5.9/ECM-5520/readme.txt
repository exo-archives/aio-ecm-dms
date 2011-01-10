Summary

    * Status: Impossible to retrieve the lock on a node even by the root
    * CCP Issue: CCP-587, Product Jira Issue: ECM-5520.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    *  Impossible to retrieve the lock on a node even by the root

Fix description

How is the problem fixed?

    * When a user tries to release the lock, check if the user is superuser.
    * In the case of superuser, grant the user as system, then release the lock using system session.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch file: ECM-5520

Tests to perform

Reproduction test
* To reproduce this issue:

1. Upload a document word
2. Access to this document in webdav
3. Close Word by killing the process in the windows task manager, or by shuting down the connection of the user who opened the document
4. The document seems as if it's locked. If we want to retrieve the lock, the message " You don't have permission to unlock this node. Please contact with administrator to get correct right" was appeared.

Tests performed at DevLevel
* Cf. above

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
* N/A

Validation (PM/Support/QA)

PM Comment
* Patch validated on behalf of PM. 
 
Support Comment
* Support review: patch validated.	

QA Feedbacks
*

