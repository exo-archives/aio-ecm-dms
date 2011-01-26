Summary

    * Status: Label in Advanced Search in French
    * CCP Issue: CCP-737, Product Jira Issue: ECM-5563.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
1. Login
2. Change language to French
3. Go to File Explorer
4. Click on Advanced Search icon. Open "Advanced Search" tab.
   Some labels aren't translated in French:
    * And/or → Et/ou
    * Click "Show/hide the constraint form" button:
      - Contain → Contient; Created → Créé
      - Add Properties: Add → Ajouter; Cancel → Annuler; Property -> Propriété
      - Add Category: Close → Fermer

Fix description

How is the problem fixed?
* Modify in resource bundles (JCRExplorerPortlet_*.xml), java files (UISimpleSearch.java, UIConstraintsForm.java) and groovy templates (UIConstraintsForm.gtmpl).

Patch file: ECM-5563.patch

Tests to perform

Reproduction test
* cf. above

Tests performed at DevLevel
* cf. above

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
* None

Is there a performance risk/cost?
* None

Validation (PM/Support/QA)

PM Comment
* Validated

Support Comment
* Validated

QA Feedbacks
*
