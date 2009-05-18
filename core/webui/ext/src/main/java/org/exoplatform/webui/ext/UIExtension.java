/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.webui.ext;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.webui.core.UIComponent;

/**
 * A Pojo that describes an UI extension
 * 
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          nicolas.filotto@exoplatform.com
 * May 04, 2009  
 */
public class UIExtension implements Comparable<UIExtension> {

  /**
   * Logger.
   */
  private static final Log LOG = ExoLogger.getLogger(UIExtension.class);

  /**
   * Indicates whether the extension is enabled
   */
  private boolean enable = true;
  
  /**
   * The type of the extension, usually it is the FQN of the component 
   * that contains the extension
   */
  private String type;

  /**
   * The name of the extension
   */
  private String name;
  
  /**
   * The category of the extension
   */
  private String category;
  
  /**
   * The rank of the extension
   */
  private int rank;
  
  /**
   * The filters to add 
   */
  private List<UIExtensionFilter> extendedFilters;

  /**
   * The FQN of the component that displays the extension
   */
  private String component;

  /**
   * The class name of the component that displays the extension
   */
  private Class<? extends UIComponent> componentClass;
  
  /**
   * The internal filters of the component
   */
  private List<UIExtensionFilter> componentFilters;
  
  /**
   * Indicates whether the component filters have already been initialized
   */
  private boolean componentFiltersInitialized;
  
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public int getRank() {
    return rank;
  }

  public void setRank(int rank) {
    this.rank = rank;
  }
  
  public List<UIExtensionFilter> getExtendedFilters() {
    return extendedFilters;
  }

  public void setExtendedFilters(List<UIExtensionFilter> extendedFilters) {
    this.extendedFilters = extendedFilters;
  }
  
  public Class<? extends UIComponent> getComponent() {
    if (componentClass == null) {
      try {
        this.componentClass = Class.forName(component, false, Thread.currentThread().getContextClassLoader()).asSubclass(UIComponent.class);
      } catch (ClassNotFoundException e) {
        throw new IllegalArgumentException("The class of the extension component cannot be found", e);
      }
    }
    return componentClass;
  }

  public void setComponent(Class<? extends UIComponent> componentClass) {
    this.componentClass = componentClass;
  }

  public void setComponent(String component) {
    this.component = component;
    this.componentClass = null;
    this.componentFiltersInitialized = false;
    this.componentFilters = null;
    this.enable = true;
  }

  public List<UIExtensionFilter> getComponentFilters() {
    if (!componentFiltersInitialized) {
      try {
        Class<? extends UIComponent> componentClass = getComponent();
        for (Method m : componentClass.getDeclaredMethods()) {
          if (m.isAnnotationPresent(UIExtensionFilters.class)) {
            @SuppressWarnings("unchecked") 
            List<UIExtensionFilter> filters = (List<UIExtensionFilter>) m.invoke(componentClass.newInstance(), (Object[]) null);
            if (filters != null) {
              // prevent from any undesired modification
              this.componentFilters = Collections.unmodifiableList(filters);              
            }
            break;
          }
        }
      } catch (Exception e) {
        // disable the extension to ensure that it won't be used
        this.enable = false;
        LOG.error("The internal filters of the component cannot be initialized", e);
      } finally {
        this.componentFiltersInitialized = true;        
      }
    }
    return componentFilters;
  }    
  
  public boolean isEnable() {
    return enable;
  }

  /**
   * {@inheritDoc}
   */
  public int compareTo(UIExtension extension) {
    int diff = 0;
    if (rank != 0 && extension.rank != 0) {
      diff = rank - extension.rank;
    } else if (rank == 0 && extension.rank != 0) {
      return 1;
    } else if (rank != 0 && extension.rank == 0) {
      return -1;
    }
    if (diff == 0) {
      if (category != null && extension.category != null) {
        diff = category.compareTo(extension.category);
      } else if (category == null && extension.category != null) {
        return 1;
      } else if (category != null && extension.category == null) {
        return -1;
      }
      if (diff == 0) {
        return name.compareTo(extension.name);
      }
    }
    return diff;
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean equals(Object o) {
    if (o instanceof UIExtension) {
      return compareTo((UIExtension) o) == 0;
    }
    return false;
  }
}
