Summary

    * Status: Link can't be distinguished in document view mode
    * CCP Issue: CCP-427, Product Jira Issue: ECM-5564.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
In File Explorer: 
1. Add a document, eg: article, Free layout WebContent.
2. In the content of this document, add a link using Insert/Edit link icon
3. Save document and view it. The link style is bad, it couldn't be distinguished from the other content.

Fix description

How is the problem fixed?

    * Add a stylesheet for a tag in UIDocumentWorkspace (color:blue; text-decoration: underline;)

Patch file: ECM-5564.patch

Tests to perform

Reproduction test
* Cf. above

Tests performed at DevLevel
1- Do the steps like Reproduction test
2- Check the link display, it's changed to blue & underlined.

Tests performed at QA/Support Level
* No

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
* N/A

Validation (PM/Support/QA)

PM Comment
* Patch validated on behalf of PM.

Support Comment
* Support review: Patch validated

QA Feedbacks
*

