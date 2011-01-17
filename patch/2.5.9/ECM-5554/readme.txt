Summary

    * Status: Error in the research in categories
    * CCP Issue: CCP-723, Product Jira Issue: ECM-5554.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Error in the research in categories

Fix description

How is the problem fixed?
* For exo:webContent nodes, the data normally are stored inside the children (html, css files), so when we search the result always returns the child node of web content instead of itself. 
  Therefore to fix this problem we have to check if the node has exo:webContent as its parent, then we will return its parent.

Patch file: ECM-5554.patch

Tests to perform

Reproduction test
   1. Case 1: Exception with web content.
          * Login with root.
          * Go to Site explorer.
          * Choose acme drive.
          * Click add document (e.g acme_doc), select "Free layout webcontent" template (or "Picture on head layout webcontent" template)
          * Add text in the document (e.g "test")
          * Simple search in acme, input "test"
            No result is displayed
            Exception in server:

            [ERROR] portal:Lifecycle - template : app:/groovy/webui/component/explorer/search/UISearchResult.gtmpl <java.util.NoSuchElementException>java.util.NoSuchElementException
            	at org.exoplatform.services.jcr.impl.core.query.lucene.QueryResultImpl$LazyScoreNodeIterator.nextNodeImpl(QueryResultImpl.java:372)
            	at org.exoplatform.services.jcr.impl.core.query.lucene.QueryResultImpl$LazyScoreNodeIterator.nextNode(QueryResultImpl.java:383)
            	at org.exoplatform.ecm.webui.component.explorer.search.UISearchResult.getSymlinkNode(UISearchResult.java:153)
            	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
            	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
            	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
            	at java.lang.reflect.Method.invoke(Method.java:597)
                    ...
   2. Case 2:
          * Login with root.
          * Go to Site explorer.
          * Choose acme drive.
          * Add a document (named test_doc)
          * In its content put acme_doc. Save.
          * Click on test_doc
          * In the simple search area, put the text acme_doc.
            No result found.
            Exception in server console:

            [ERROR] portal:UIPortalApplication - Error during the processAction phase <javax.jcr.RepositoryException: corresponding session has been closed>javax.jcr.RepositoryExc
            eption: corresponding session has been closed
                    at org.exoplatform.services.jcr.impl.core.query.QueryManagerImpl.sanityCheck(QueryManagerImpl.java:111)
                    at org.exoplatform.services.jcr.impl.core.query.QueryManagerImpl.createQuery(QueryManagerImpl.java:80)
                    at org.exoplatform.ecm.webui.component.explorer.control.UIAddressBar$SimpleSearchActionListener.execute(UIAddressBar.java:283)
                    at org.exoplatform.webui.event.Event.broadcast(Event.java:52)
                    at org.exoplatform.webui.core.lifecycle.UIFormLifecycle.processAction(UIFormLifecycle.java:101)
                    at org.exoplatform.webui.core.UIComponent.processAction(UIComponent.java:91)
                    at org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle.processAction(UIApplicationLifecycle.java:45)
                    at org.exoplatform.webui.core.UIComponent.processAction(UIComponent.java:91)

Tests performed at DevLevel

   1. Go to acme drive.
   2. Create a free layout webcontent named AAA with main content having "test".
   3. Search with "test" keyword, return 1 result ---> OK.
   4. Access to Business node and try to search with "test" keyword -> No result found, no exception. -> OK

Tests performed at QA/Support Level
*No

Documentation changes

Documentation changes:
* Nothing

Configuration changes

Configuration changes:
* Nothing

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * I have changed 3 files are UISearchResult.java, UISearchResult.gtmpl, UIAddressBar.java.

Is there a performance risk/cost?
* No, there isn't.

Validation (PM/Support/QA)

PM Comment
* Patch validated on behalf of PM

Support Comment
* Support review: patch validated

QA Feedbacks
*

