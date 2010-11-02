Summary

    * Status: Changes in permissions not taken in consideration by SymLinks
    * CCP Issue: CCP-536, Product Jira Issue: ECM-5538.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Changes in permissions are not taken in consideration by SymLinks

Fix description

How is the problem fixed?

    * In the past, when changing the permissions for a node, only one symlink of this node is updated. 
      Now we update the permission for all symlinks of this node.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch files:
There are currently no attachments on this page.
Tests to perform

Reproduction test
* Steps:

   1. Add a document in Managed Sites/acme/documents
   2. Delete any and *:/platform/web-contributors permissions for this document
   3. Add acme/World into document's category. Go to acme/World, you will find the Symlink
   4. Come back to the document.
   5. Add any with all rights.
      any is added to the permission list of the Symlink
   6. Disconnect, and reconnect with a normal user (e.g. marry).
   7. Go to acme/World in File Explorer. The Symlink is not found there although the user has the permission on it.

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

    * Function or ClassName change: none

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* VALIDATED BY PM

Support Comment
* validated by Support Team

QA Feedbacks
*

