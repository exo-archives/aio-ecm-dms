Summary

    * Status: Impossible to sign in DMS standalone after PORTAL-3815
    * CCP Issue: N/A, Product Jira Issue: ECM-5576.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
After fixing PORTAL-3815, it is impossible to sign in DMS standalone.

When clicking Sign in button in Sign in form, there is a blank page and error in server console.

[ERROR] portal:Lifecycle - template : system:/groovy/portal/webui/workspace/UIExoStart.gtmpl <java.lang.NullPointerException: Cannot invoke method getTime24hFormat() on null object>java.lang.NullPointerException: Cannot invoke method getTime24hFormat() on null object
	at org.codehaus.groovy.runtime.NullObject.invokeMethod(NullObject.java:77)
	at org.codehaus.groovy.runtime.InvokerHelper.invokePogoMethod(InvokerHelper.java:784)
	at org.codehaus.groovy.runtime.InvokerHelper.invokeMethod(InvokerHelper.java:758)
	at org.codehaus.groovy.runtime.ScriptBytecodeAdapter.invokeMethodN(ScriptBytecodeAdapter.java:170)
	at org.codehaus.groovy.runtime.ScriptBytecodeAdapter.invokeMethod0(ScriptBytecodeAdapter.java:198)
	at script1296031978012.run(script1296031978012.groovy:314)
	at org.exoplatform.groovyscript.text.SimpleTemplateEngine$SimpleTemplate$1.writeTo(SimpleTemplateEngine.java:128)
	at org.exoplatform.groovyscript.text.TemplateService.merge(TemplateService.java:72)
	at org.exoplatform.webui.core.lifecycle.Lifecycle.renderTemplate(Lifecycle.java:111)
	at org.exoplatform.webui.core.lifecycle.Lifecycle.processRender(Lifecycle.java:70)
	at org.exoplatform.webui.core.UIComponent.processRender(UIComponent.java:100)
	at org.exoplatform.webui.core.UIContainer.renderChild(UIContainer.java:220)
...

This problem doesn't occur in WCM/AIO, eXo start button and time display are not used in WCM/AIO.

Fix description
Problem analysis:
* DMS standalone uses UIExoStart template, and has its own portal-configuration.xml.
* In Portal module, this template has been modified in PORTAL-3815, with the parameter added in GlobalPortalConfigService.
  The service has been added in PORTAL-3895.

How is the problem fixed?
* Update portal-configuration.xml of DMS with the configuration of GlobalPortalConfigService.

Patch file: ECM-5576.patch

Tests to perform

Reproduction test
* Cf. above.

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
* No

Configuration changes

Configuration changes:
* Cf. above

Will previous configuration continue to work?
* No.

Risks and impacts

Can this bug fix have any side effects on current client projects?
* Function or ClassName change: no

Is there a performance risk/cost?
* No.

Validation (PM/Support/QA)

PM Comment
* Validated by PM.

Support Comment
* Patch validated by Support.

QA Feedbacks
*
