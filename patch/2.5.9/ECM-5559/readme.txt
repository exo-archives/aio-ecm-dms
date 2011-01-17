Summary

    * Status: The name of taxonomy is limited to 30 characters
    * CCP Issue: CCP-724, Product Jira Issue: ECM-5559.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * When adding a new Taxonomy under a tree of taxonomies, its name is limited to 30 characters.

Fix description

How is the problem fixed?

    * Update length limit in
          o UITaxonomyForm.java and UITaxonomyTreeCreateChildForm.java
          o ECMAdminPortlet_*.xml resource bundles.

Patch file: ECM-5559.patch

Tests to perform

Reproduction test

    * Add a new taxonomy under a tree of taxonomies.
      Set its name more than 30 characters. A pop-up message appears "Name is too long."

Tests performed at DevLevel

    * Add a new taxonomy under a tree of taxonomies.
      When the taxonomy name exceeds 150 characters, a pop-up message displays "Taxonomy name must be less than 150 characters."

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:

    * Yes, update the length limit of taxonomy name.

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

    * Support review: patch validated.

QA Feedbacks
*
