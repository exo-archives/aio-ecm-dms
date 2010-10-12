Summary

    * Status: ListView generates a ConcurrentAccessModificationException
    * CCP Issue: CCP-521, Product Jira Issue: ECM-5475.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Sometimes (randomly) we have a display anomaly (look at the capture)

It's because concurent access (need many users to be reproduced)

[ERROR] portal:Lifecycle - template : /jcr:system/exo:ecm/views/templates/ecm-explorer/ListView <java.util.ConcurrentModificationException>java.util.ConcurrentModificationException
at java.util.WeakHashMap$HashIterator.nextEntry(WeakHashMap.java:744)
at java.util.WeakHashMap$ValueIterator.next(WeakHashMap.java:771)
at org.exoplatform.services.jcr.impl.core.SessionDataManager$ItemReferencePool.getDescendats(SessionDataManager.java:1610)
at org.exoplatform.services.jcr.impl.core.SessionDataManager.refresh(SessionDataManager.java:1283)
at org.exoplatform.services.jcr.impl.core.ItemImpl.refresh(ItemImpl.java:582)

the problem disappears when refreshing the session

Fix description

How is the problem fixed?

    * Remove the useless code block which made the bug happen.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch files: ECM-5475.patch

Tests to perform

Reproduction test
* Above

Tests performed at DevLevel
* Functional test

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
* No

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
    * VALIDATED BY PM

Support Comment
    * Validated

QA Feedbacks
*

