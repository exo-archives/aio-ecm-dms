Summary

    * Status: Put the name of document's author instead of owner.
    * CCP Issue: CCP-342, Product Jira Issue : ECM-5440
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
In all symlinks representing a document in categories, the author is not the original creator of the document but is owner.
We need to have the original author's name displayed with each symlink.

Steps to reproduce:

   1. Login as root
   2. In JCR File Explorer, create a document in /acme/categories/acme
   3. The owner of this document in different views (admin-view, system-view, list-view, wcm-view) is __system.
      While in View Node Properties, exo:owner is root.
      However in the real file in web content, the author is root.

Fix description

How is the problem fixed?

    * When there is a symlink, we just need to get the information about author from the target node.

Patch information:
Patches files:
ECM-5440.patch

Tests to perform

Tests performed at DevLevel?
*

Tests performed at QA/Support Level?
*


Documentation changes

Documentation Changes:
*


Configuration changes

Configuration changes:
*

Will previous configuration continue to work?
*


Risks and impacts

Can this bug fix have an impact on current client projects?

    * Function or ClassName change?

Is there a performance risk/cost?
*


Validation (PM/Support/QA)

PM Comment
* VALIDATED BY PM

Support Comment
* validated by Support team

QA Feedbacks
*

