/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.commons.crucible.api.model;

import java.io.Serializable;

/**
 * User abstraction.
 */
public class User implements Serializable {
    private static final int HASH_MAGIC = 31;

    protected String userName;
	protected String displayName;

    public User(String userName) {
        this.userName = userName;
    }

    public User(String userName, String displayName) {
        this.userName = userName;
        this.displayName = displayName;
    }

    public String getUsername() {
        return userName;
    }

    public String getDisplayName() {
		if (displayName == null) {
			return userName;
		}
		return displayName;
    }

    @Override
	public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        User userBean = (User) o;

        if (displayName != null ? !displayName.equals(userBean.displayName) : userBean.displayName != null) {
            return false;
        }

        if (userName != null ? !userName.equals(userBean.userName) : userBean.userName != null) {
            return false;
        }

        return true;
    }

    @Override
	public int hashCode() {
        int result;
        result = (userName != null ? userName.hashCode() : 0);
        result = HASH_MAGIC * result + (displayName != null ? displayName.hashCode() : 0);
        return result;
    }

    public int compareTo(User that) {
        return this.userName.compareTo(that.getUsername());
    }
}