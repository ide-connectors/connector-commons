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

import java.util.UUID;

public class ServerId implements IServerId {
	private UUID uuid;

	public ServerId() {
		uuid = UUID.randomUUID();
		assert uuid != null;
	}

	public ServerId(final String uuid) {
		this.uuid = UUID.fromString(uuid);
		assert uuid != null;
	}

	public String getStringId() {
		return uuid.toString();
	}

	@Override
	public String toString() {
		return uuid.toString();
	}

	public UUID getUuid() {
		return uuid;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final ServerId serverId = (ServerId) o;

		if (!uuid.equals(serverId.uuid)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return uuid.hashCode();
	}
}
