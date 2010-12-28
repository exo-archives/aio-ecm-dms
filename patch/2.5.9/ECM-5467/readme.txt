Summary

    * Status: Labels are not translated in French
    * CCP Issue: CCP-470, Product Jira Issue: ECM-5467
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Some labels are not translated in French

Fix description

How is the problem fixed?
Modify some labels:

    * Sauver -> Enregistrer
    * template -> modèle
    * Save -> Enregistrer
    * Cancel -> Annuler
      Translate English labels into French in:
    * JCRExplorerPortlet_fr.xml
    * FastContentCreatorPortlet_fr.xml
    * ECMAdminPortlet_fr.xml

Patch information:
Patch files: ECM-5467.patch

Tests to perform

Reproduction test
* Check DMS standalone in FR.

Tests performed at DevLevel
* Cf. above

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
* No.

Configuration changes

Configuration changes:
* No.

Will previous configuration continue to work?
* Yes.

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: no

Is there a performance risk/cost?
* No.

Validation (PM/Support/QA)

PM Comment
* Patch validated.

Support Comment
* patch validated

QA Feedbacks
*

