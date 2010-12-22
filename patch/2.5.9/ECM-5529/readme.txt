Summary

    * Status: Impossible to use JAAS to allow access to File Explorer
    * CCP Issue: CCP-193, Product Jira Issue: ECM-5529.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * When you try to access to File Explorer portlet by any user who does not exist in organization service and who has been authenticated using JAAS custom module, you get the following error message:
      "There is no drive in this repository or you do not have permission to access any drive in this repository"

Steps to reproduce

Once PORTAL-3854 Issue fixed, to reproduce the problem you should follow these steps :

1)Get DMS 2.5.7 deployed under Jboss server (we can reproduce the same bug in tomcat but step 2 will be little bit different).

2)Place the attached files :jboss-service.xml and login-config.xml under $JBOSS_HOME\server\default\deploy\exoplatform.sar\META-INF(to enable JAAS) and place the exo-teste-lm.jar(Custom login modules using JAAS) from teste-exo-lm-bug.zip under $JBOSS_HOME\server\default\deploy\exoplatform.sar then start the server

3)Type this URL "http://localhost:8080/portal/private/classic/" in your browser and login using teste/teste , click the File Explorer navigation and you will get the message written above despite "teste" has the necessary memberships in login-config.xml.
Fix description

How is the problem fixed?

This problem is due to the fact that file explorer drive browser bypasses the IdentityRegistry and gets the authenticated user groups and memberships from Organization Service (instead of the IdentityRegistry).Thus, users who does not exist in Organization service and who are authenticated using JAAS can not see file explorer drive browser even if they have the necessary memberships in JAAS configuration files.

So, we propose to change the org.exoplatform.ecm.webui.utils.Utils.getMemberships() static method which has been called by  the method "public List<DriveData> getDrives(String repoName) throws Exception; " in the "org.exoplatform.ecm.webui.component.explorer.UIDrivesBrowser" class.

The modification (see Utils.java.patch) is to retrieve memberships of the authenticated user using the IdentityRegistry Service instead of Organization Service to allow JAAS based authorization.

Patch information:
Patch files: ECM-5529.patch

Tests to perform

Reproduction test
*The same as Steps to reproduce in problem description section

Tests performed at DevLevel
*The same as Steps to reproduce in problem description section

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

    * PATCH VALIDATED BY PM

Support Comment

    * Patch validated

QA Feedbacks
*

