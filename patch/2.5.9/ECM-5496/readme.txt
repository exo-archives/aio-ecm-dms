Summary

    * Status: Still existing a category on Taxonomy after deleting it on category
    * CCP Issue: [N/A, Product Jira Issue: ECM-5496.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
1. Go to Site Administration --> Add new taxonomy tree
2. Type the name. Choose the collaboration as workspace, and /sites content/live/acme/categories as Home path.
3. Click Next to add permission to this taxonomy.
Select Any and add all the permissions (read, remove, etc)
4. Click Save, then Next to add an action to a taxonomy

    * Type the name and select collaboration as Target workspace, and /sites content/live/acme/categories as Target path)
    * Click Save ==> new Taxonomy is added.
      5. Go to Site Explorer. Select Sites Management.
      Under /acme/categories you will find the created category.
      Delete this category ==> Delete successfully.
      6. Back to Site Administration/ontologies/taxonomy ==> Still see the added taxonomy.
      Click Edit the taxonomy ==> JS error on UI (Jsbug.png), and Exception at the console.

[ERROR] portal:UIPortalApplication - Error during the processAction phase <javax.jcr.ItemNotFoundException: Node not found 1c75ed8e7f0001010197e4d29e82ab1a at collaboration>javax.jcr.ItemNotFoundException: Node not found 1c75ed8e7f0001010197e4d29e82ab1a at collaboration
	at org.exoplatform.services.jcr.impl.core.SessionImpl.getNodeByUUID(SessionImpl.java:625)
	at org.exoplatform.services.cms.link.impl.LinkManagerImpl.getTarget(LinkManagerImpl.java:118)
	at org.exoplatform.services.cms.taxonomy.impl.TaxonomyServiceImpl.getTaxonomyTree(TaxonomyServiceImpl.java:171)
	at org.exoplatform.ecm.webui.component.admin.taxonomy.UITaxonomyTreeContainer.refresh(UITaxonomyTreeContainer.java:146)

...


Fix description

How is the problem fixed?

    * The Taxonomy Tree List is not automatically rebuilt after deleting a taxonomy tree. 
      So the problem is solved by updating the Taxonomy Tree List and put warning in the case a user clicks on a tree that is deleted in the list that has not been rebuilt.

Patch file: ECM-5496.patch

Tests to perform

Reproduction test
* Cf. above

Tests performed at DevLevel
* Follow the steps of Reproduction test  and a variety of adding and deleting taxonomy cases

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
* No

Validation (PM/Support/QA)

PM Comment
* Validated

Support Comment
* Validated

QA Feedbacks
*

