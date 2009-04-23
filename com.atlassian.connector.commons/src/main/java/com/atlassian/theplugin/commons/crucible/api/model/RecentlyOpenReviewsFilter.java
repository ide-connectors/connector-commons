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

import java.util.LinkedList;

/**
 * @author Jacek Jaroczynski
 */
public class RecentlyOpenReviewsFilter implements CrucibleFilter {

	private LinkedList<ReviewRecentlyOpenBean> recentlyOpenReviews = new LinkedList<ReviewRecentlyOpenBean>();
	private boolean enabled = false;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	public LinkedList<ReviewRecentlyOpenBean> getRecentlyOpenReviews() {
		return recentlyOpenReviews;
	}

	public void setRecentlyOpenReviews(final LinkedList<ReviewRecentlyOpenBean> recentlyOpenReviews) {
		this.recentlyOpenReviews = recentlyOpenReviews;
	}

	public String getFilterName() {
		return "Recently Viewed Reviews";
	}

	public String getFilterUrl() {
		return null;
	}

	public void addRecentlyOpenReview(final ReviewAdapter review) {
		if (review != null) {
			String reviewId = review.getPermId().getId();
			String serverId = review.getServerData().getServerId();

			// add element and make sure it is not duplicated and it is insterted at the top
			ReviewRecentlyOpenBean r = new ReviewRecentlyOpenBean(serverId, reviewId);

			recentlyOpenReviews.remove(r);
			recentlyOpenReviews.addFirst(r);

			while (recentlyOpenReviews.size() > 10) {
				recentlyOpenReviews.removeLast();
			}
		}
	}
}
