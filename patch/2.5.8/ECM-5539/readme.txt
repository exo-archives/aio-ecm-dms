Summary

    * Status: The File explorer simple search doesn't work in categories
    * CCP Issue: CCP-439, Product Jira Issue: ECM-5539
    * Complexity: HIGH

The Proposal
Problem description

What is the problem to fix?

    * The File explorer simple search doesn't work in categories.

Fix description

How is the problem fixed?

    * Actually, a node created in categories is symlink and doesn't contain contents itself. So when we search at categories,we have to refer to storage and search with the original node.

Patch information:
Patches files:
ECM-5539.patch 	  	

Tests to perform

Reproduction test
*How to reproduce:

   1. Connect as ROOT and go in ACME
   2. Go to "Sites explorer->Sites management->acme/categories/acme/Business"
   3. Create multiple contents: "document1" , "document2", "document3" and publish the content
   4. Try a simple search on "document1" for example
   5. No result found!

Tests performed at DevLevel

   1. Connect as ROOT and go in ACME
   2. Go to "Sites explorer->Sites management->acme/categories/acme/Business"
   3. Create multiple contents: "document1" , "document2", "document3" and publish the content
   4. Try a simple search on "document1" for example
   5. Found document1 in the result list.

Tests performed at QA/Support Level
*


Documentation changes

Documentation changes:
* None


Configuration changes

Configuration changes:
* None

Will previous configuration continue to work?
* Yes


Risks and impacts

Can this bug fix have any side effects on current client projects?
    * No effects.

Is there a performance risk/cost?
* yes, there is a little performance problem here. We have to get back the link node from target node to show in the search result.
Validation By PM & Support

PM Comment
* VALIDATED BY PM

Support Comment
* Support Patch Review : validated

QA Feedbacks
*

