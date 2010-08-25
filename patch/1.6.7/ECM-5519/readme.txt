Summary

    * Status: Can't delete a webcontent created in a category from the web contents folder
    * CCP Issue: CCP-532, Product Jira Issue: ECM-5519.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Can't delete a webcontent created in a category from the web contents folder:

How to reproduce:
==================

   1. Go to the classic drive in the site explorer.
   2. Go to "classic/categories/classic".
   3. Create a new "Free layout webcontent" file.
   4. Go to "/classic/web contents/YYYY/MM/DD".
   5. Try to delete the file newly created.
   6. We get the "msg" message and the following exception:

      [ERROR] JCRExceptionManager - The following error occurs : javax.jcr.InvalidItemStateException: [collaboration] DELETE NODE. Can not delete parent till childs exists. Item []:1[]si
      tes content:1[]live:1[]classic:1[]web contents:1[]2010:1[]07:1[]21:1[]aaaaaaaaaaa:1 f535496aa9fed36a006a47d91061b756. Cause >>>> Integrity constraint violation JCR_FK_SITEM_PARENT
      table: JCR_SITEM in statement [delete from JCR_SITEM where ID=?]: Integrity constraint violation JCR_FK_SITEM_PARENT table: JCR_SITEM in statement [delete from JCR_SITEM where ID=?
      ]: [collaboration] DELETE NODE. Can not delete parent till childs exists. Item []:1[]sites content:1[]live:1[]classic:1[]web contents:1[]2010:1[]07:1[]21:1[]aaaaaaaaaaa:1 f535496aa
      9fed36a006a47d91061b756. Cause >>>> Integrity constraint violation JCR_FK_SITEM_PARENT table: JCR_SITEM in statement [delete from JCR_SITEM where ID=?]: Integrity constraint violat
      ion JCR_FK_SITEM_PARENT table: JCR_SITEM in statement [delete from JCR_SITEM where ID=?]: Integrity constraint violation JCR_FK_SITEM_PARENT table: JCR_SITEM in statement [delete f
      rom JCR_SITEM where ID=?]

Fix description

How is the problem fixed?

    * Update the JCR session object when AddTaxonomyActionScript is invoked.

Patch information:
Patch files:
ECM-5519.patch 	  	

Tests to perform

Reproduction test
*

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*


Documentation changes

Documentation changes:
*


Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* No


Risks and impacts

Can this bug fix have any side effects on current client projects?

    * No

Is there a performance risk/cost?
* No


Validation (PM/Support/QA)

PM Comment

    * VALIDATED BY PM

Support Comment

    * Patch validated by the support team

QA Feedbacks
*

