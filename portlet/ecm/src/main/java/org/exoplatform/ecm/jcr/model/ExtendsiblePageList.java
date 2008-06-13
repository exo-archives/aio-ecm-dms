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
package org.exoplatform.ecm.jcr.model;

import java.util.List;

import org.exoplatform.commons.exception.ExoMessageException;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jun 13, 2008  
 */
abstract public class ExtendsiblePageList {
  
  private int pageSize_ ;
  protected int available_ = 0;
  protected int availablePage_  = 1;
  protected int currentPage_ = -1 ;
  protected List currentListPage_ ;
  
  public ExtendsiblePageList(int pageSize) {
    pageSize_ = pageSize ;
  }
  
  public int getPageSize() { return pageSize_  ; }
  public void setPageSize(int pageSize) {
    pageSize_ = pageSize ;
    setAvailablePage(available_) ;
  }
  
  public int getCurrentPage() { return currentPage_ ; }
  public int getAvailable() { return available_ ; }
  
  public int getAvailablePage() { return availablePage_ ; }
  
  public List currentPage() throws Exception {
    if(currentListPage_ == null) {
      populateCurrentPage(currentPage_) ;
    }
    return currentListPage_  ;
  }
  
  abstract protected void populateCurrentPage(int page) throws Exception   ;
  
  public List getPage(int page) throws Exception   {
    checkAndSetPage(page) ;
    populateCurrentPage(page) ;
    return currentListPage_ ;
  }
  
  abstract public List getAll() throws Exception  ;
  
  protected void checkAndSetPage(int page) throws Exception  {
    if(page < 1 || page > availablePage_) {
      Object[] args = { Integer.toString(page), Integer.toString(availablePage_) } ;
      throw new ExoMessageException("PageList.page-out-of-range", args) ;
    }
    currentPage_ =  page ;
  }
  
  protected void setAvailablePage(int available) {
    available_ = available ;
    if (available == 0)  {
      availablePage_ = 1 ; 
      currentPage_ =  1 ;
    } else {
      int pages = available / pageSize_ ;
      if ( available % pageSize_ > 0) pages++ ;
      availablePage_ = pages ;
      currentPage_ =  1 ;
    }
  }
  
  abstract public long getPageNumberEstimate() ;
  
  public int getFrom() { 
    return (currentPage_ - 1) * pageSize_ ; 
  }
  
  public int getTo() { 
    int to = currentPage_  * pageSize_ ; 
    if (to > available_ ) to = available_ ;
    return to ;
  }
}
