Summary

    * Status: Update of a new document (file) does not change the publication status
    * CCP Issue: CCP-449, Product Jira Issue: ECM-5471
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix ?

    * Updating a new document (file) does not change the publication status

Fix description

How is the problem fixed?

    * Remove the checkin, checkout operations after replacing data.
    * Add the PostSaveEventListener after replacing data

Patch information:
Patches files:
File ECM-5471.patch

Tests to perform

Reproduction test
Assume that you have a file test.pdf, named in the JCR as FOO and stored in /classic/Documents/
This file is currently in version 1 and "published"
To update this document you need to:

   1. Upload the new file in the same location and give the same NAME. (test)
   2. Choose "Replace Data"in the upload file dialog.
   3. The file is uploaded in the JCR, a new version is created.
      BUG: the status of the document returns to "null" while it should be as draft (see publication_status.jpg and publication_history.jpg in ECM-5471). Uploading a new document is like "editing" a document and should change the state of the document to "draft". This allows the editor to "publish" it to change its state.

Tests performed at DevLevel ?
*

Tests performed at QA/Support Level ?
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

Can this bug fix have an impact on current client projects ?

    * Function or ClassName change ?

Is there a performance risk/cost?
*


Validation (PM/Support/QA)

PM Comment
*

Support Comment
*

QA Feedbacks
*

