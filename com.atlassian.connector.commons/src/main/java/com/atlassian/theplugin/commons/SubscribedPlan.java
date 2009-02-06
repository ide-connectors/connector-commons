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

package com.atlassian.theplugin.commons;

/**
 * This class is immutable
 */
public final class SubscribedPlan {
	private final String key;

	public SubscribedPlan(final SubscribedPlan cfg) {
		key = cfg.getKey();
	}

    public SubscribedPlan(final String key) {
        this.key = key;
    }

    public String getKey() {
		return key;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		SubscribedPlan that = (SubscribedPlan) o;

		//noinspection RedundantIfStatement
		if (!key.equals(that.key)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}
}
