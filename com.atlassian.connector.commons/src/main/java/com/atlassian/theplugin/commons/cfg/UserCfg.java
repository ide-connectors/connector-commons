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
package com.atlassian.theplugin.commons.cfg;

/**
 * User: pmaruszak
 */
public final class UserCfg implements User {
	private String userName = "";
	private String password = "";
	private final boolean passwordStored;

	public UserCfg(String userName, String password, final boolean passwordStored) {
		this.userName = userName;
		this.password = password;
		this.passwordStored = passwordStored;
	}

	public UserCfg(String userName, String password) {
		this(userName, password, false);
	}

	public String getUserName() {
		return userName;
	}

	public User setPassword(String password) {
		return new UserCfg(this.userName, password, passwordStored);
	}

	public User setPasswordStored(final boolean passwordStored) {
		return new UserCfg(this.userName, this.password, passwordStored);

	}

	public String getPassword() {
		return password;
	}

	public boolean isPasswordStored() {
		return passwordStored;
	}

	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof UserCfg)) {
			return false;
		}

		final UserCfg userCfg = (UserCfg) o;

		if (passwordStored != userCfg.passwordStored) {
			return false;
		}
		if (!password.equals(userCfg.password)) {
			return false;
		}
		if (!userName.equals(userCfg.userName)) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		int result;
		result = userName.hashCode();
		result = 31 * result + password.hashCode();
		result = 31 * result + (passwordStored ? 1 : 0);
		return result;
	}
}
