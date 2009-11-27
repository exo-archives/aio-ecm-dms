/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.cms.folksonomy;

import java.util.List;

import javax.jcr.Node;


/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Anh Vu
 *          anhvurz90@gmail.com
 * Nov 13, 2009  
 * 10:52:05 AM
 */
public interface NewFolksonomyService {
	
  /**
   * Property name TAG_RATE_PROP
   */
  final public static String TAG_RATE_PROP = "exo:styleRange".intern() ;
  
  /**
   * Property name HTML_STYLE_PROP
   */
  final public static String HTML_STYLE_PROP = "exo:htmlStyle".intern() ;

  final static public String EXO_TOTAL = "exo:total".intern();
  final static public String EXO_TAGGED = "exo:tagged".intern();
  final static public String EXO_UUID = "exo:uuid".intern();
  final static public String EXO_TAGSTYLE = "exo:tagStyle".intern();
  
  final static public int PUBLIC = 0;
  final static public int GROUP = 1;
  final static public int SITE = 2;
  final static public int PRIVATE = 3;
  
	
  /**
   * Add a private tag to a document. A folksonomy link will be created in a tag node
   * @param tagNames      Array of tag name as the children of tree
   * @param documentNode  Tagging this node by create a folksonomy link to node in tag
   * @param repository    Repository name
   * @param workspace     Workspace name
   * @param userName      User name
   * @throws Exception
   */
  public void addPrivateTag(String[] tagsName, Node documentNode, 
      String repository, String workspace, String userName) throws Exception ;
  
  /**
   * Add a group tag to a document. A folksonomy link will be created in a tag node
   * @param tagNames      Array of tag name as the children of tree
   * @param documentNode  Tagging this node by create a folksonomy link to node in tag
   * @param repository    Repository name
   * @param workspace     Workspace name
   * @param roles         User roles
   * @throws Exception
   */
  public void addGroupsTag(String[] tagsName, Node documentNode, 
      String repository, String workspace, String[] roles) throws Exception ;
  
  /**
   * Add a public tag to a document. A folksonomy link will be created in a tag node
   * @param treePath      Path of folksonomy tree
   * @param tagNames      Array of tag name as the children of tree
   * @param documentNode  Tagging this node by create a folksonomy link to node in tag
   * @param repository    Repository name
   * @param workspace     Workspace name
   * @throws Exception
   */
  public void addPublicTag(String treePath, String[] tagsName, Node documentNode, 
      String repository, String workspace) throws Exception ;
  
  /**
   * Add a site tag to a document. A folksonomy link will be created in a tag node
   * @param siteName      Portal name
   * @param treePath      Path of folksonomy tree
   * @param tagNames      Array of tag name as the children of tree
   * @param documentNode  Tagging this node by create a folksonomy link to node in tag
   * @param repository    Repository name
   * @param workspace     Workspace name
   * @throws Exception
   */  
  public void addSiteTag(String siteName, String treePath, String[] tagsName, Node node, 
      String repository, String workspace) throws Exception ;
  /**
   * Get all private tags
   * @param userName        User name
   * @param repository      repository name
   * @param workspace       Workspace name
   * @return List<Node>
   */
  public List<Node> getAllPrivateTags(String userName, String repository, 
      String workspace) throws Exception ; 
  
  /**
   * Get all public tags
   * @param treePath      Folksonomy tree path
   * @param repository    Repository name
   * @param workspace     Workspace name
   * @return  List<Node>
   * @throws Exception
   */
  public List<Node> getAllPublicTags(String treePath, String repository, 
      String workspace) throws Exception ;
  
  /**
   * Get all tags by groups
   * @param roles       Roles of user
   * @param repository  Repository name
   * @param workspace   Workspace name
   * @return  List<Node>
   * @throws Exception
   */
  public List<Node> getAllGroupTags(String[] roles, String repository, 
      String workspace) throws Exception ;
  
  /**
   * Get all tags by group
   * @param role       Role of user
   * @param repository  Repository name
   * @param workspace   Workspace name
   * @return  List<Node>
   * @throws Exception
   */
  public List<Node> getAllGroupTags(String role, String repository, 
      String workspace) throws Exception ;
  
  
  /**
   * Get all tags of Site
   * @param siteName    Portal name
   * @param treePath    Folksonomy tree path
   * @param repository  Repository name
   * @param workspace   Workspace name
   * @return  List<Node>
   * @throws Exception
   */
  public List<Node> getAllSiteTags(String siteName, String treePath, String repository, 
      String workspace) throws Exception ;
  /**
   * Get all document which storing in tag
   * @param treeName              Name of folksonomy tree
   * @param tagName               Name of tag
   * @param repository      Repository name
   * @return                List of documents in tag
   * @throws Exception
   */
  
  /**
   * Get all documents by tag
   */
  public List<Node> getAllDocumentsByTag(String tagPath, String repository, 
      String workspace) throws Exception ;
  
  
  /**
   * Get HTML_STYLE_PROP property in styleName node in repository
   * @param tagPath       Tag path
   * @param workspace     Workspace name  
   * @param repository    Repository name
   * @return  value of property of styleName node
   * @throws Exception
   */
  public String getTagStyle(String tagPath, String repository, String workspace) throws Exception ;
  
  /**
   * Update property TAG_RATE_PROP, HTML_STYLE_PROP following value tagRate, htmlStyle
   * for node in tagPath in repository
   * @param styleName     Style name
   * @param tagRate       The range of tag numbers
   * @param htmlStyle     Tag style
   * @param repository    Repository name
   * @param workspace     Workspace name
   * @throws Exception
   */
  public void addTagStyle(String styleName, String tagRange, String htmlStyle, 
      String repository, String workspace) throws Exception ;
  
  /**
   * Update property TAG_RATE_PROP, HTML_STYLE_PROP following value tagRate, htmlStyle
   * for node in tagPath in repository
   * @param styleName     Style name
   * @param tagRate       The range of tag numbers
   * @param htmlStyle     Tag style
   * @param repository    Repository name
   * @param workspace     Workspace name
   * @throws Exception
   */
  public void updateTagStyle(String styleName, String tagRange, String htmlStyle, 
      String repository, String workspace) throws Exception ;
  
  
  /**
   *  Get all tag style base of folksonomy tree
   * @param repository Repository name
   * @param workspace Workspace name
   * @return List<Node> List tag styles
   * @throws Exception
   */
  public List<Node> getAllTagStyle(String repository, String workspace) throws Exception ;
  
  /**
   * Init all TagStylePlugin with session in repository name
   * @param repository     repository name
   */
  public void init(String repository) throws Exception ;
  
  /**
   * Remove tag of given document
   * @param treeName    Name of folksonomy tree
   * @param tagName     Name of tag
   * @param document    Document which added a link to tagName
   * @param repository  Repository name
   * @return
   * @throws Exception
   */
  public void removeTagOfDocument(String tagPath, Node document, 
      String repository, String workspace) throws Exception;
  
  /**
   * Remove tag
   * @param tagPath     Path of tag
   * @param repository  Repository name
   * @param workspace   Workspace name
   */
  public void removeTag(String tagPath, String repository, String workspace) throws Exception;
  
  /**
   * Modify tag name
   * @param tagPath     Path of tag
   * @param newTagName  New tag name
   * @param repository  Repository name
   * @param workspace   Workspace name
   * @return
   * @throws Exception
   */
  public Node modifyTagName(String tagPath, String newTagName, String repository, 
      String workspace) throws Exception;

  /**
   * Get all tags linked to given document
   * @param documentNode 		Document node
   * @param repository			Repository name
   * @param workspace				Workspace name
   * @return					
   * @throws Exception
   */
  public List<Node> getLinkedTagsOfDocument(Node documentNode, String repository, 
  		String workspace) throws Exception;
  
  /**
   * Get all tags linked to given document by scope
   * @param documentNode 		Document node
   * @param repository			Repository name
   * @param workspace				Workspace name
   * @return					
   * @throws Exception
   */
  public List<Node> getLinkedTagsOfDocumentByScope(int scope, String value, Node documentNode, String repository, 
  		String workspace) throws Exception;
  
}
