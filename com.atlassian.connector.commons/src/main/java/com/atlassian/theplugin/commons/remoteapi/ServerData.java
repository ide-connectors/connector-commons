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
package com.atlassian.theplugin.commons.remoteapi;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.Server;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import org.jetbrains.annotations.NotNull;

/**
 * @author pmaruszak
 */
public final class ServerData {
	private Server server;
	private final String userName;
	private final String password;

	public ServerData(final Server server, final String userName, final String password) {
		this.server = server;
		this.userName = userName;
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public String getUrl() {
		return server.getUrl();
	}

	public ServerId getServerId() {
		return server.getServerId();
	}

	public String getName() {
		return server.getName();
	}


	public boolean isEnabled() {
		return server.isEnabled();
	}

	public ServerType getServerType() {
		return server.getServerType();
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final ServerData that = (ServerData) o;

		if (getServerId() != null ? !getServerId().equals(that.getServerId()) : that.getServerId() != null) {
			return false;
		}
		if (getUrl() != null ? !getUrl().equals(that.getUrl()) : that.getUrl() != null) {
			return false;
		}
		if (password != null ? !password.equals(that.password) : that.password != null) {
			return false;
		}
		if (userName != null ? !userName.equals(that.userName) : that.userName != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result;
		// todo do we want to use name for hashCode and Equals???
//		result = (name != null ? name.hashCode() : 0);
		result = (getServerId() != null ? getServerId().hashCode() : 0);
		result = 31 * result + (userName != null ? userName.hashCode() : 0);
		result = 31 * result + (password != null ? password.hashCode() : 0);
		result = 31 * result + (getUrl() != null ? getUrl().hashCode() : 0);
		return result;
	}

//	public ServerData withCredentials(String newUsername, String newPassword) {
//		return new ServerData(name, serverId, newUsername, newPassword, url);
//	}


	@NotNull
	public static ServerData create(@NotNull Server server, @NotNull UserCfg defaultCredentials) {
		final String userName;
		final String password;

		if (server.isUseDefaultCredentials()) {
			userName = defaultCredentials.getUserName();
			password = defaultCredentials.getPassword();
		} else {
			userName = server.getUserName();
			password = server.getPassword();
		}
		return new ServerData(server, userName, password);

	}
}
