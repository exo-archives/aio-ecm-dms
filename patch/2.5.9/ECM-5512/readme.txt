Summary

    * Status: Category acme displayed by default has no child node
    * CCP Issue: N/A, Product Jira Issue: ECM-5512.
    * Complexity: Normal

The Proposal
Problem description

What is the problem to fix?
In AIO 1.6.6:
1. Choose to add taxonomy for a document.
2. The taxonomy by default is acme. There is no child node in this taxonomy.
3. Choose to see another taxonomy.
4. Choose again acme as taxonomy. Its child nodes are now displayed in the two panels.
The bug occurs when adding taxonomy after
* either uploading a document
* or creating a document

In AIO 1.6.7: reproduce after deleting System taxonomy.

Fix description

How is the problem fixed?
* When adding a taxonomy for a document, we list all the taxonomy trees for the user to choose. 
  But in the initializing phase, we set the initial workspace as SystemWorkspace. 
  It is true only the case that the first taxonomy tree in list is the System taxonomy tree. 
  So we fix the issue by setting the initial workspace as the workspace of the first taxonomy tree in list.

Patch file: ECM-5512.patch

Tests to perform

Reproduction test
* cf. above

Tests performed at DevLevel
* Go to Site Admin
  Delete "System" taxonomy tree
  Go to Site Explorer
  Add an article "abc".
  Try to add a new category for this document -> at the first time, "acme" taxonomy tree appears and it has child nodes -> OK

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
* Function or ClassName change:No

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch validated on behalf of PM

Support Comment
* Support review : patch validated

QA Feedbacks
*

