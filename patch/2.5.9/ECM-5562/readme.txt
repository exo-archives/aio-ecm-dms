Summary

    * Status: Exception when adding a select box of multi value in edit meta data form
    * CCP Issue: CCP-732, Product Jira Issue: ECM-5562.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Create a metadata form containing a multi valued select box.
    * Uploaded a content and edit its metadata:
      Fill values, select some elements from the select box-> Save
      => Exception with the following stack trace

[ERROR] portal:UIPortalApplication - Error during the processAction phase <java.lang.ClassCastException: org.exoplatform.webui.form.UIFormSelectBox>java.lang.ClassCast
Exception: org.exoplatform.webui.form.UIFormSelectBox
        at org.exoplatform.ecm.webui.component.explorer.upload.UIAddMetadataForm$SaveActionListener.execute(UIAddMetadataForm.java:108)

A problem of cast is in execute method of UIAddMetadataForm@SaveActionListener when the selectbox is multivalued.

Fix description

How is the problem fixed?

    * Change the way to get values if the input UI is the SelectBox.

Patch file: ECM-5562.patch

Tests to perform

Reproduction test
* Cf. above

Tests performed at DevLevel
*

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
* Function or ClassName change: no

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Validated by PM

Support Comment
* Patch validated

QA Feedbacks
*

