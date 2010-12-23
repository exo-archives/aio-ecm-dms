Summary

    * Status: The HTTP header ifModifiedSince needs to be given to enable the cache-control
    * CCP Issue: CCP-628, Product Jira Issue: ECM-5518.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * The cache control on resources doesn't work even after adding the cache-control configuration to the webdav component configuration ([]) as below:

<component>
	  <key>org.exoplatform.services.jcr.webdav.WebDavServiceImpl</key>
	  <type>org.exoplatform.services.cms.webdav.WebDavServiceImpl</type>
	  <init-params>
			...
			<value-param>
				<name>cache-control</name>
				<value>image/jpg,image/png,image/gif:max-age=555;</value>
			</value-param>
		</init-params>
</component>

    * Problem analysis: the value of ifModifiedSince is not given to org.exoplatform.services.jcr.webdav.WebDavServiceImpl.

Fix description

How is the problem fixed?

    * pass the value of ifModifiedSince property to WEBDAV service to use the Cache Control by adding this value in get() method of WebDavServiceImpl class to

Patch information:
Patch files: ECM-5518.patch

Tests to perform

Reproduction test
 

1- Start WCM 1.2.6
2- Go to Site Acme -> OverView
3-Use firebug addon (choose tab: Net/All) to view information of resources got from WEBDAV service (example: http://localhost:8080/portal/rest/jcr/repository/collaboration/sites%20content/live/acme/web%20contents/site%20artifacts/Searchbox/medias/images/Search.gif)

=> if-modified-since property doesn't exist in HTTP header

Tests performed at DevLevel 

  1. Do the same operations like reproduction test => HTTP header has the if-modified-since propert, status is 200 OK

  2. Refresh page, the status is changed to 304 Non Modified

Tests performed at QA/Support Level
*


Documentation changes

Documentation changes:
* None


Configuration changes

Configuration changes:
* None

Will previous configuration continue to work?
* Yes


Risks and impacts

Can this bug fix have any side effects on current client projects?

    * No.

Is there a performance risk/cost?
* No, there isn't.


Validation (PM/Support/QA)

PM Comment
*

Support Comment
*

QA Feedbacks
*

