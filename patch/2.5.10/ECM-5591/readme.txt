Summary

    * Status: Bug when replacing a file in categories
    * CCP Issue: CCP-880, Product Jira Issue: ECM-5591.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
In Files Explorer, categories :
Upload a file.
Upload the same file again.
Choose "Replace data".
An error message appears "The name "Etude_Reseaux_Sociaux" is existing. You can not upload same name file except for 'nt:file'".
Fix description

How is the problem fixed?

    *  the node under categories node is actually NOT nt:file type, it has taxonomyLink node type. the nt:file node type is store in target folder; so to check the actual node type is this case we should to get the node type of target node through LinkManager as following code:

              LinkManager linkManager = getApplicationComponent(LinkManager.class);
              node = linkManager.getTarget(node);

Patch file: ECM-5591.patch

Tests to perform

Reproduction test
In Files Explorer, categories :
Upload a file.
Upload the same file again.
Choose "Replace data".

Tests performed at DevLevel
In Files Explorer, categories :
Upload a file.
Upload the same file again.
Choose "Replace data".

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
N/A
Configuration changes

Configuration changes:
N/A

Will previous configuration continue to work?
Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * N/A

Is there a performance risk/cost?
N/A

Validation (PM/Support/QA)

PM Comment
* Validated

Support Comment
* Validated

QA Feedbacks
*

