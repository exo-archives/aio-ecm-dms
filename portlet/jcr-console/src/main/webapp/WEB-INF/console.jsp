<%@page import="javax.portlet.RenderRequest"%>
<%@page import="org.exoplatform.portlets.jcrconsole.JcrConsole"%>
<SCRIPT LANGUAGE="JavaScript" TYPE="text/javascript" SRC="/jcr-console/scripts/console.js"></SCRIPT>
<LINK REL="stylesheet"  HREF="/jcr-console/styles/styles.css" TYPE="text/css">
<DIV ID="termDiv" STYLE="position:relative; top:20px; left:100px;"></DIV>
<%
String cont_name = (String)request.getAttribute(JcrConsole.SESSION_CONTAINER);
%>
<SCRIPT LANGUAGE="JavaScript">
var context = "<%=cont_name%>";
termOpen();
</SCRIPT>
