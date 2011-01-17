Summary

    * Status: Disable the versionning/publication on Webcontents sub-nodes
    * CCP Issue: CCP-412, Product Jira Issue: ECM-5566.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    *  Some users by mistake activate versioning on Webcontent's sub-nodes (default.html, default.css, default.js)
      When we export and then import these Webcontent nodes with its version history, we get exceptions and the imported node is corrupted.

Fix description

How is the problem fixed?

    *  Create a new filter to disable the version and publication buttons if the current selected node is child node of web content node.

Patch file: ECM-5566.patch

Tests to perform

Reproduction test
1. Active version of child node of web content node
2. Export the web content node to XML file
3. Import the the XML file --> an exception occurs

Tests performed at DevLevel
* Cf. above

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
* N/A

Configuration changes

Configuration changes:
* N/A

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?
* Function or ClassName change: no.

Is there a performance risk/cost?
* Performance is down a little because when selecting a node, the program must run through once more filter to check if the current node is child node of web content node or not until meet root node.

Validation (PM/Support/QA)

PM Comment
* Patch validated on behalf of PM

Support Comment
* Support review: Patch validated

QA Feedbacks
*

