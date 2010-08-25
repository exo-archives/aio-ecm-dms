Summary

    * Status: Cannot delete tags for documents
    * CCP Issue: CCP-419, Product Jira Issue: ECM-5491.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Currently we cannot delete tags of documents. Such kind of functionality has been available in WCM 2.0 GA but can't be backported due to differences in tag management.

Fix description

How is the problem fixed?

    * Allow tag manager to delete tags of document.

*Patch information:
Patch files:
ECM-5491.patch 	  	

Tests to perform

Reproduction test
* In JCR File Explorer, open a document.

   1. Select Tagging this document. Add some tags for this document.
   2. Now it is impossible to delete these tags.

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*


Documentation changes

Documentation changes:

    * The patch added a possibility to delete tags of a document.
    * Limitations (already available in WCM 2.0 GA):
          o Although there's an invocation to Edit tags of a document, this functionality has not yet supported.
          o There's no way to delete tags from the tag list of JCR.

Configuration changes

Configuration changes:
* None

Will previous configuration continue to work?
* Yes


Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

Is there a performance risk/cost?
*


Validation (PM/Support/QA)

PM Comment
*VALIDATED BY PM, tested and verified by Product team.

Support Comment
*Validated by Support

QA Feedbacks
*

