Summary

    * Status: Problem for displaying documents with names containing illegal JCR characters using nt:file view

    * CCP Issue: CCP-403, Product Jira Issue : ECM-5452
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix ?

    *  

Fix description

How the problem is fixed ?

    *  

Patch informations:

    * Final files to use should be attached to this page (Jira is for the dicussion)

Patches files:
There are currently no attachments on this page.
Tests to perform

Tests performed at DevLevel ?
*

To reproduce this problem:
1- Choose a drive in the File Explorer
2- Upload a binary document containing illegal JCR chars like '
3- double click on the uploaded document
4- The nt:file view is shown and the illegal JCR chars are changed in %number(%27 for ')

When displaying document's title, The unescapeIllegalJcrChars method of org.exoplatform.ecm.utils.text.Text should be applied in order to revert changes done to save node in the JCR. The changes should be done in the view1.gtmpl related to the nt:file

Tests performed at QA/Support Level ?
*

