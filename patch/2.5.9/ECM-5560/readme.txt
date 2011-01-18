Summary

    * Status: Display problem when group drive title is too long
    * CCP Issue: CCP-725, Product Jira Issue: ECM-5560.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
When a group drive title is too long, in File Explorer portlet, the title overflows.

Fix description

How is the problem fixed?
* Modify the stylesheet to make the overflow of .UIJCRExplorerPortlet .UIDrivesBrowser .DriveContainer hidden
* Modify UIDrivesBrowser.gtmpl to show the full title of all drives (personal, group, general drives) when mousing over the link in File Explorer.

Patch file:

Tests to perform

Reproduction test
* Create group with long title
* Go to File Explorer portlet
* Look at the corresponding group drive

Tests performed at DevLevel
* Cf. above

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
* Function or ClassName change

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch validated on behalf of PM.

Support Comment
* Support review: patch validated

QA Feedbacks
*
