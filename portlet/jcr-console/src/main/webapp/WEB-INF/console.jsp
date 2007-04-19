<%@page import="javax.portlet.RenderRequest"%>
<%@page import="org.exoplatform.portlets.jcrconsole.JcrConsole"%>
<SCRIPT LANGUAGE="JavaScript" TYPE="text/javascript" SRC="/jcr-console/scripts/console.js"></SCRIPT>
<LINK REL="stylesheet"  HREF="/jcr-console/styles/styles.css" TYPE="text/css">
<DIV ID="termDiv" STYLE="position:relative; top:20px; left:100px;"></DIV>
<%
String containerName = (String)request.getAttribute(JcrConsole.SESSION_CONTAINER);
%>
<SCRIPT LANGUAGE="JavaScript">
var containerName = "<%=containerName%>";
termOpen();
</SCRIPT>
