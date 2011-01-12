Summary

    * Status: Content Browser folders do not get collapsed when clicking the - button
    * CCP Issue: CCP-614, Product Jira Issue: ECM-5524
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * To reproduce the problem you should follow these steps:
         1. Create a tree (T1) of many nested folders using File Explorer
         2. Navigate to Content Browser and edit it like the following:
               1. choose the parent Folder of your tree in the categories path field.
               2. choose TreeList template in the template listbox.
         3. Save Content Browser Portlet and go to portlet view mode.
         4. In the categories tree, choose a folder "F1" having subfolders and expand it by clicking + Button.
         5. Try to collapse the same folder "F1" by clicking the - button but the folder cannot be collapsed.
            In addition, "F1" get collapsed only if we expand another folder in the same level as "F1".

Fix description

How is the problem fixed?

    * Create new Collap event. This event will run when the user collapses the tree (Click "-" button).
    * In the CollapActionListener: call Expand event of parent node.

Patch file: ECM-5524.patch

Tests to perform

Reproduction test

    * cf above

Tests performed at DevLevel

    * cf above

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

Can this bug fix have an impact any side effects on current client projects?
* N/A

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch validated on behalf of PM.

Support Comment
* Patch validated

QA Feedbacks
*

