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
package com.atlassian.theplugin.commons.bamboo;

import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.cfg.Server;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.cfg.SubscribedPlan;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Jacek Jaroczynski
 */
public class BambooServerData extends ServerData {
	public BambooServerData(@NotNull Server server, @NotNull UserCfg defaultCredentials) {
		super(server, defaultCredentials);
	}

	public Collection<SubscribedPlan> getPlans() {
		return getServer().getPlans();
	}

	public boolean isUseFavourites() {
		return getServer().isUseFavourites();
	}

	public int getTimezoneOffset() {
		return getServer().getTimezoneOffset();
	}

	public boolean isBamboo2() {
		return getServer().isBamboo2();
	}

	protected BambooServerCfg getServer() {
		return (BambooServerCfg) super.getServer();
	}
}
