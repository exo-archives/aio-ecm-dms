Summary

    * Status: Error in console when switching to Detail view in File Explorer
    * CCP Issue: CCP-1018, Product Jira Issue: ECM-5586.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Error in console when switching to Detail view in File Explorer
      Example one case of steps to reproduce:
         1. Login
         2. Go to File Explorer/Document Management Center
         3. Switch to Detail view
         4. Click to the Trash folder
            --> View error in console
            ?
            [ERROR] NodeTypeManagerImpl - Error obtaining node type javax.jcr.nodetype.NoSuchNodeTypeException: NodeTypeManager.getNodeType(): NodeType '[http://www.exoplatform.com/jcr/exo/1.0]webContent' not found.
            [ERROR] NodeTypeManagerImpl - Error obtaining node type javax.jcr.nodetype.NoSuchNodeTypeException: NodeTypeManager.getNodeType(): NodeType '[http://www.exoplatform.com/jcr/exo/1.0]webContent' not found.
            [ERROR] NodeTypeManagerImpl - Error obtaining node type javax.jcr.nodetype.NoSuchNodeTypeException: NodeTypeManager.getNodeType(): NodeType '[http://www.exoplatform.com/jcr/exo/1.0]webContent' not found.
            [ERROR] NodeTypeManagerImpl - Error obtaining node type javax.jcr.nodetype.NoSuchNodeTypeException: NodeTypeManager.getNodeType(): NodeType '[http://www.exoplatform.com/jcr/exo/1.0]webContent' not found.
            [ERROR] NodeTypeManagerImpl - Error obtaining node type javax.jcr.nodetype.NoSuchNodeTypeException: NodeTypeManager.getNodeType(): NodeType '[http://www.exoplatform.com/jcr/exo/1.0]webContent' not found.
            [ERROR] NodeTypeManagerImpl - Error obtaining node type javax.jcr.nodetype.NoSuchNodeTypeException: NodeTypeManager.getNodeType(): NodeType '[http://www.exoplatform.com/jcr/exo/1.0]webContent' not found.
            [ERROR] NodeTypeManagerImpl - Error obtaining node type javax.jcr.nodetype.NoSuchNodeTypeException: NodeTypeManager.getNodeType(): NodeType '[http://www.exoplatform.com/jcr/exo/1.0]webContent' not found.
            [ERROR] NodeTypeManagerImpl - Error obtaining node type javax.jcr.nodetype.NoSuchNodeTypeException: NodeTypeManager.getNodeType(): NodeType '[http://www.exoplatform.com/jcr/exo/1.0]webContent' not found.
            [ERROR] NodeTypeManagerImpl - Error obtaining node type javax.jcr.nodetype.NoSuchNodeTypeEx

    * Note:
         1. This ERROR appears when switch to Detail view and then do some actions in File Explorer. Example above is one of many cases to reproduce this issue.
         2. This ERROR displays only on DMS standalone

Fix description

How is the problem fixed?

    * This issue is caused by adding a new filter to disable the version and publication button if the current selected node is child node of web content node in ECM-5566 issue.
      In this filter we will check if current node is a web content or not. To do that, we need get the node type of web content. The problem is in DMS standalone, we have no node type is "exo:webContent". So when we checking node type by block code following:

        parrentNode.isNodeType(Utils.EXO_WEBCONTENT);

      will have exception in the log console.

    * To fix it, before calling isNodeType() function we need check that node type is exist or not.

        ExoContainer exoContainer = ExoContainerContext.getCurrentContainer() ;
        RepositoryService repositoryService = (RepositoryService) exoContainer.getComponentInstanceOfType(RepositoryService.class);
        ExtendedNodeTypeManager ntmanager = repositoryService.getCurrentRepository().getNodeTypeManager();
        try {        
          ntmanager.getNodeType(Utils.EXO_WEBCONTENT);
        } catch (NoSuchNodeTypeException e) {
          return true;
        }

Tests to perform

Reproduction test

    * Error in console when switching to Detail view in File Explorer
         1. Login
         2. Go to File Explorer/Document Management Center
         3. Switch to Detail view
         4. Click to the Trash folder
            --> View error in console
            ?
            [ERROR] NodeTypeManagerImpl - Error obtaining node type javax.jcr.nodetype.NoSuchNodeTypeException: NodeTypeManager.getNodeType(): NodeType '[http://www.exoplatform.com/jcr/exo/1.0]webContent' not found.
            [ERROR] NodeTypeManagerImpl - Error obtaining node type javax.jcr.nodetype.NoSuchNodeTypeException: NodeTypeManager.getNodeType(): NodeType '[http://www.exoplatform.com/jcr/exo/1.0]webContent' not found.
            [ERROR] NodeTypeManagerImpl - Error obtaining node type javax.jcr.nodetype.NoSuchNodeTypeException: NodeTypeManager.getNodeType(): NodeType '[http://www.exoplatform.com/jcr/exo/1.0]webContent' not found.
            [ERROR] NodeTypeManagerImpl - Error obtaining node type javax.jcr.nodetype.NoSuchNodeTypeException: NodeTypeManager.getNodeType(): NodeType '[http://www.exoplatform.com/jcr/exo/1.0]webContent' not found.
            [ERROR] NodeTypeManagerImpl - Error obtaining node type javax.jcr.nodetype.NoSuchNodeTypeException: NodeTypeManager.getNodeType(): NodeType '[http://www.exoplatform.com/jcr/exo/1.0]webContent' not found.
            [ERROR] NodeTypeManagerImpl - Error obtaining node type javax.jcr.nodetype.NoSuchNodeTypeException: NodeTypeManager.getNodeType(): NodeType '[http://www.exoplatform.com/jcr/exo/1.0]webContent' not found.
            [ERROR] NodeTypeManagerImpl - Error obtaining node type javax.jcr.nodetype.NoSuchNodeTypeException: NodeTypeManager.getNodeType(): NodeType '[http://www.exoplatform.com/jcr/exo/1.0]webContent' not found.
            [ERROR] NodeTypeManagerImpl - Error obtaining node type javax.jcr.nodetype.NoSuchNodeTypeException: NodeTypeManager.getNodeType(): NodeType '[http://www.exoplatform.com/jcr/exo/1.0]webContent' not found.
            [ERROR] NodeTypeManagerImpl - Error obtaining node type javax.jcr.nodetype.NoSuchNodeTypeEx

Tests performed at DevLevel

    * cf. above

Tests performed at QA/Support Level

    * cf. above

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

    * N/A

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment

    * N/A

Support Comment

    * Support review: Patch validated

QA Feedbacks
*

