Summary

    * Status: Impossible manipulation of a document containing & in its name
    * CCP Issue: CCP-1161, Product Jira Issue: ECM-5620.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
To reproduce this problem:
1/ Create a document containing & in its name on local (EX mac&cheese.doc)
2/ Copy the document in a workspace (EX Collaboration)by webdav =>

In File Explorer, the document appears in the right name but we can only download this document. If we try to do any actions (rename, copy, cut, delete), a popup appears (see attachment)and an error appears also in the log:
?
ERROR [JCRExceptionManager] The following error occurs : javax.jcr.PathNotFoundException: Can't find path: /Documents/Live/mac
    WARN [UIJCRExplorer] The node cannot be found at /Documents/Live/mac into the workspace collaboration

In Webdav:
In IE7, Windows XP: the document appears with an incorrect name ( it considers only characters before & and the document will be without extension (in our case it will be mac) and we can't do any actions also (rename, copy...)
In Ubuntu-11.10: document's name is correct, we can copy, delete, rename it

3/ Upload the same document in File Explorer and verify:

In File Explorer: The name of document is correct and we can do all actions
In Webdav IE7 and Ubuntu-11.10: the name of document is also correct but we can only copy it. We can't rename, cut and delete it.
Fix description

How is the problem fixed?

    * this problem is caused by the special caracter '&' in the name of the doc added by webdav.So the manipulation of this file/folder in FileExplorer need this:
    * "&" should be escaped to "%26" by Text.escapeIllegalJcrChars from upload by both webdav and Site Explorer and if file name renamed from Webdav, "&" also escaped to "%26".
    * so we modified in the class WebDavServiceImpl.java  in the method put() like this:

?
repoPath = Text.escapeIllegalJcrChars(repoPath);
 Item item = nodeFinder.getItem(repoName, workspaceName(repoPath), LinkUtils.getParentPath(path(repoPath)), true);

    * And we added a new methode move() to manipulate this doc in File Explorer:

?
@HTTPMethod(WebDavMethods.MOVE)
 @URITemplate("/{repoName}/{repoPath}/")
 @InputTransformer(XMLInputTransformer.class)
 @OutputTransformer(PassthroughOutputTransformer.class)
 public Response move(@URIParam("repoName") String repoName, @URIParam("repoPath") String repoPath,
 @HeaderParam(WebDavHeaders.DESTINATION) String destinationHeader,
 @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader, @HeaderParam(WebDavHeaders.IF) String ifHeader,
 @HeaderParam(WebDavHeaders.DEPTH) String depthHeader,
 @HeaderParam(WebDavHeaders.OVERWRITE) String overwriteHeader, @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI,
 HierarchicalProperty body)
 {
 try {
int lastIndexOfSlash = destinationHeader.lastIndexOf('/');
 String newPath = destinationHeader.substring(0, lastIndexOfSlash);
 String newFileName = Text.escape(Text.escapeIllegalJcrChars(Text.unescape(destinationHeader.substring(lastIndexOfSlash + 1))));
 destinationHeader = newPath + "/" + newFileName;
 Item item = nodeFinder.getItem(repoName, workspaceName(repoPath), path(Text.escapeIllegalJcrChars(repoPath)), true);
 repoPath = item.getSession().getWorkspace().getName() + item.getPath();
 } catch (PathNotFoundException exc) {
 return Response.Builder.notFound().build();
 } catch (NoSuchWorkspaceException exc) {
 return Response.Builder.notFound().build();
 } catch (Exception e) {
 log.warn("Cannot find the item at " + repoName + "/" + repoPath, e);
 return Response.Builder.serverError().build();
}
 return super.move(repoName, repoPath, destinationHeader, lockTokenHeader, ifHeader,
 depthHeader, overwriteHeader, baseURI, body);
 
 }

Patch files:ECM-5620.patch

Tests to perform

Reproduction test
*

To reproduce this problem:
1/ Create a document containing & in its name on local (EX mac&cheese.doc)
2/ Copy the document in a workspace (EX Collaboration)by webdav =>
In File Explorer, the document appears in the right name but we can only download this document. If we try to do any actions (rename, copy, cut, delete), a popup appears (see attachment)and an error appears also in the log:
?
ERROR [JCRExceptionManager] The following error occurs : javax.jcr.PathNotFoundException: Can't find path: /Documents/Live/mac
    WARN [UIJCRExplorer] The node cannot be found at /Documents/Live/mac into the workspace collaboration

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
*No
Configuration changes

Configuration changes:
*No

Will previous configuration continue to work?
*Yes
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * N/A

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
* N/A

Support Comment
* SL3VN review: Patch validated

QA Feedbacks
