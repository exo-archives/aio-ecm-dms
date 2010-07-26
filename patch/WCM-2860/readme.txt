Summary

    * Status: Search does not ignore accented characters
    * CCP Issue: CCP-495, Product Jira Issue : WCM-2860
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix ?

    *  Search within DMS/WCM doesn't ignore accented charcters, thus searching for test does not give as results tést

Fix description

How the problem is fixed ?

    *  This is fixed using a customized Lucene analyzer that 
         1. Unescape HTML characters :  since we are using FCKEditor as editor, then most of our contents are in HTML 
         2. Convert accented characters as normal characters

Patch informations:
Patches files:
WCM-2860.patch


Tests to perform

Tests performed at DevLevel ?
*

Tests performed at QA/Support Level ?
* create a document containing tést
* search for word test


Documentation changes

Documentation Changes:
*


Configuration changes

Configuration changes:
*

Previous configuration will continue to work?
*


Risks and impacts

Is this bug fix can have an impact on current client projects ?

    * Function or ClassName change ?

Is there a performance risk/cost?
*


Validation (PM/Support/QA)

PM Comment
* PATCH VALIDATED BY PM
Performance Test on this fix in the product is needed

Support Comment
* Patch provided and Validated by Support team 

QA Feedbacks
*

