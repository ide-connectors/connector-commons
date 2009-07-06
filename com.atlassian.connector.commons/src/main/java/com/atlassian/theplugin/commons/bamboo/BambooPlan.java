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

import java.io.Serializable;

/**
 * This class is immutable
 */
public class BambooPlan implements Serializable {
	private final String name;
	private final String key;
	private final boolean favourite;
	private final boolean enabled;

	public BambooPlan(String name, String key) {
		this(name, key, true);
	}

	public BambooPlan(String name, String key, boolean isEnabled) {
		this(name, key, isEnabled, false);
	}

	public BambooPlan(String name, String key, boolean isEnabled, boolean isFavourite) {
		this.name = name;
		this.key = key;
		this.enabled = isEnabled;
		this.favourite = isFavourite;
	}


	public String getPlanName() {
		return this.name;
	}

	public String getPlanKey() {
		return this.key;
	}

	public boolean isFavourite() {
		return favourite;
	}

	/**
	 * Returns copy of this object with favourit information set.
	 * 
	 * @param isFavourite requested favourit state
	 * @return copy of this object
	 */
	public BambooPlan withFavourite(boolean isFavourite) {
		return new BambooPlan(name, key, enabled, isFavourite);
	}

	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
            return true;
        }
		if (!(o instanceof BambooPlan)) {
            return false;
        }

		BambooPlan that = (BambooPlan) o;

		//noinspection RedundantIfStatement
		if (key != null ? !key.equals(that.key) : that.key != null) {
            return false;
        }

		return true;
	}

	@Override
	public int hashCode() {
		return (key != null ? key.hashCode() : 0);
	}
}
