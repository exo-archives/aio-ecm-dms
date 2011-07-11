Summary

    * Status: Problem when using the "Save as" function on webdav
    * CCP Issue: CCP-942, Product Jira Issue: ECM-5608.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
There is a problem when using the "Save As" function with Microsoft Office tools and when using the 2.5.x version. But, it isn't the case when using the 2.3.x version (Regression).
Steps to reproduce:
    * Copy a doc file in the jcr (with webdav)
    * Open this file with Word.
    * Use the Save As to save the file with a different name

- AIO 1.6.7 (dms 2.5.8)
  An error appears, the file with the new name is created, but it is empty.
  Note: When clicking on the OK button of the error, the Save window is displayed again. If we choose the just created empty file, the operation of save works fine

- AIO 1.6.8 (dms 2.5.9)
  Another error appears, there isn't a new file creation.

Fix description

How is the problem fixed?

    * Changing lock and unlock methods in WebDavServiceImpl
         1. Before: Checking the existing of item. If not exist, System will throw PathNotFoundException and cannot lock or unlock item.
         2. After: If item does not exist, system will still lock and unlock item with "repoPath" original

Patch file: ECM-5608.patch

Tests to perform

Reproduction test

    * Copy a doc file in the jcr (with webdav)
    * Open this file with Word.
    * Use the Save As to save the file with a different name =>An error appears

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

    * Function or ClassName change: no


Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment
* PL review: patch validated

Support Comment
* Support review: patch validated

QA Feedbacks
*

