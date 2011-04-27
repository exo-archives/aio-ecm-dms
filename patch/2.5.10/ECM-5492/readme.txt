Summary

    * Status: Problem of displaying document name containing illegal JCR character ' (Node properties, Manage Versions)
    * CCP Issue: N/A, Product Jira Issue: ECM-5492.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
In File Explorer: add a document, set its name to contain illegal JCR character '
This character isn't escaped in:

    * Version form (click Manage Versions)
    * Node properties (click View Node Properties): exo:title, publication:history

Furthermore, when clicking Edit to modify the values of these properties, the modification can't be saved: to fix in ECM-5511.
Fix description

How is the problem fixed?

    * Unescape illegal JCR characters in the templates UIVersionInfo.gtmpl and UIPropertyTab.gtmpl

Patch files: ECM-5492.patch

Tests to perform

Reproduction test

    * cf.above

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

    * Function or ClassName change: None

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment

    * PL review: Patch validated

Support Comment

    * Support review: Patch validated

QA Feedbacks
*

