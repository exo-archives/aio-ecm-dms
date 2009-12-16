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
package org.exoplatform.services.cms.lock.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Anh Vu
 *          anhvurz90@gmail.com
 * Dec 15, 2009  
 * 9:51:25 AM
 */
public class LockGroupsOrUsersConfig {
	private List<String> settingLockList = new ArrayList<String>();
	
	public List<String> getSettingLockList() { return this.settingLockList; };
	public void setSettingLockList(List<String> list) { 
		this.settingLockList = list;
	}

}
