Summary

    * Status: The results of an initial research not cleared during a second search
    * CCP Issue: CCP-805, Product Jira Issue: ECM-5584.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
*  If one searches with a word "auto" which appears for example in 5 documents, 5 documents are displayed, and then performs a search with a word "photo" which appears in 100 documents or less, the results of the first research are erased and the 100 documents of the second query are displayed. 
   However, if the second query contains more than 100 documents, the results of the second search will be added to the first, so there is no crushing of the results of the first search

Fix description

How is the problem fixed?
*  Clean the list contains search results that is used for both less than and over 100 documents.

Patch file: ECM-5584.patch

Tests to perform

Reproduction test
* cf. above

Tests performed at DevLevel
* cf. above
  Test with  < 10 and  > 100 search results continuously 

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
* Function or ClassName change: None

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch validated by PM

Support Comment
* Support review: Patch validated

QA Feedbacks
*
