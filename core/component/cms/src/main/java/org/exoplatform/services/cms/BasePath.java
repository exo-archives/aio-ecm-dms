/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.services.cms;

/**
 * @author benjaminmestrallet
 */
public interface BasePath {
  public static final String CMS_PATH = "cmsPath";
  public static final String CMS_TEMPLATES_PATH = "templatesPath";
  public static final String CMS_HOME_PATH = "homePath";
  public static final String CMS_USERS_PATH = "usersPath";
  public static final String CMS_GROUPS_PATH = "groupsPath";
  public static final String CMS_PUBLICATIONS_PATH = "cmsPublicationsPath";
  public static final String CMS_RULES_PATH = "cmsRulesPath";   
  public static final String CMS_VIEWTEMPLATES_PATH = "viewTemplatesPath" ;
  public static final String CMS_VIEWS_PATH = "userViewsPath" ;  
  public static final String EXO_ECM_SYSTEM_PATH = "exoECMSystemPath" ;
  public static final String EXO_TAXONOMIES_PATH = "exoTaxonomiesPath" ;
  public static final String CALENDAR_CATEGORIES_PATH = "calendarPath" ;
  public static final String TAXONOMIES_TREE_DEFINITION_PATH = "exoTaxoTreesDefinitionPath";
  public static final String TAXONOMIES_TREE_STORAGE_PATH = "exoTaxoTreesStoragePath";
  public static final String EXO_FOLKSONOMY_PATH = "exoFolksonomiesPath";
  public static final String EXO_TAGS_PATH = "exoTagsPath" ;
  public static final String EXO_TAG_STYLE_PATH = "exoTagStylePath" ;
  public static final String EXO_NEW_TAG_STYLE_PATH = "exoNewTagStylePath" ;  
  public static final String METADATA_PATH = "metadataPath" ;
  public static final String QUERIES_PATH = "queriesPath" ;
  public static final String EXO_DRIVES_PATH = "exoDrivesPath" ;
  public static final String ECM_BUSINESS_PROCESSES_PATH ="businessProcessesPath" ;
  
  public static final String ECM_EXPLORER_TEMPLATES = "ecmExplorerTemplates" ;
  public static final String CONTENT_BROWSER_TEMPLATES = "contentBrowserTemplates" ;
  public static final String CB_PATH_TEMPLATES = "cbPathTemplates" ;
  public static final String CB_QUERY_TEMPLATES = "cbQueryTemplates" ;
  public static final String CB_SCRIPT_TEMPLATES = "cbScriptTemplates" ;
  public static final String CB_DETAIL_VIEW_TEMPLATES = "cbDetaiViewTemplates" ;
  
  public static final String CMS_SCRIPTS_PATH = "cmsScriptsPath".intern() ;  
  public static final String CONTENT_BROWSER_SCRIPTS = "contentBrowserScripts".intern() ;
  public static final String ECM_EXPLORER_SCRIPTS = "ecmExplorerScripts".intern() ;
  public static final String ECM_ACTION_SCRIPTS = "ecmActionScripts".intern() ;
  public static final String ECM_INTERCEPTOR_SCRIPTS = "ecmInterceptorScripts".intern() ;
  public static final String ECM_WIDGET_SCRIPTS = "ecmWidgetScripts".intern() ;
  
}
