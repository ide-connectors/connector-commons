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

import com.atlassian.theplugin.commons.cfg.ServerId;

import java.util.Arrays;
import java.util.HashMap;


public class CustomFilterBean implements CustomFilter {
	private ServerId serverUid = new ServerId();
    private String title = "";
    private String[] state = new String[0];
    private String author = "";
    private String moderator = "";
    private String creator = "";
    private String reviewer = "";
    private boolean orRoles = false;
    private boolean complete;
    private boolean allReviewersComplete;
    private String projectKey = "";
    private boolean enabled = false;
    private static final double ID_DISCRIMINATOR = 1002d;
    private static final int HASHCODE_CONSTANT = 31;
    private static final int SHIFT_32 = 32;
	public static final String FILTER_ID = "MANUAL_FILTER_ID";

	public boolean equals(Object o) {
		if (this == o) {
            return true;
        }
		if (o == null || getClass() != o.getClass()) {
            return false;
        }

		CustomFilterBean that = (CustomFilterBean) o;

		return uid == that.uid;

	}

	public int hashCode() {
		int result;
		result = (title != null ? title.hashCode() : 0);
		result = HASHCODE_CONSTANT * result + (state != null ? Arrays.hashCode(state) : 0);
		result = HASHCODE_CONSTANT * result + (author != null ? author.hashCode() : 0);
		result = HASHCODE_CONSTANT * result + (moderator != null ? moderator.hashCode() : 0);
		result = HASHCODE_CONSTANT * result + (creator != null ? creator.hashCode() : 0);
		result = HASHCODE_CONSTANT * result + (reviewer != null ? reviewer.hashCode() : 0);
		result = HASHCODE_CONSTANT * result + (orRoles ? 1 : 0);
		result = HASHCODE_CONSTANT * result + (complete ? 1 : 0);
		result = HASHCODE_CONSTANT * result + (allReviewersComplete ? 1 : 0);
		result = HASHCODE_CONSTANT * result + (projectKey != null ? projectKey.hashCode() : 0);
		result = HASHCODE_CONSTANT * result + (int) (uid ^ (uid >>> SHIFT_32));
		result = HASHCODE_CONSTANT * result + (serverUid != null ? serverUid.hashCode() : 0);
		return result;
	}

	private transient long uid = System.currentTimeMillis() + (long) (Math.random() * ID_DISCRIMINATOR);

	public String getServerUid() {
		return serverUid.toString();
	}

	public void setServerUid(String serverUid) {
		this.serverUid = new ServerId(serverUid);
	}

	public CustomFilterBean() {
	}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getState() {
        return state;
    }

    public void setState(String[] state) {
        this.state = state;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getModerator() {
        return moderator;
    }

    public void setModerator(String moderator) {
        this.moderator = moderator;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isAllReviewersComplete() {
        return allReviewersComplete;
    }

    public void setAllReviewersComplete(boolean allReviewersComplete) {
        this.allReviewersComplete = allReviewersComplete;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public boolean isOrRoles() {
        return orRoles;
    }

	public HashMap<String, String> getPropertiesMap() {
		HashMap map = new HashMap();

		String states = "";
		for (String st : state) {
			states += st + ", ";
		}
		if (states.length() > ", ".length()) {
			states = states.substring(0, states.length() - ", ".length());
		}

		map.put("Server", serverUid.toString());
		map.put("Project key", projectKey);
		if (states.length() > 0) {
			map.put("State", states);
		}

		if (author.length() > 0) {
			map.put("Author", author);
		}
		if (moderator.length() > 0) {
			map.put("Moderator", moderator);
		}
		if (creator.length() > 0) {
			map.put("Creator", creator);
		}
		if (reviewer.length() > 0) {
			map.put("Reviewer", reviewer);
		}

		if (orRoles) {
			map.put("Role", orRoles ? "true" : "false");
		}
		if (complete) {
			map.put("Complete", complete ? "true" : "false");
		}
		if (allReviewersComplete) {
			map.put("All revievers completed", allReviewersComplete ? "true" : "false");
		}
		return map;
	}

	public String toHtml() {
		String table = "<html><body><table>";
		HashMap prop = getPropertiesMap();
		for (Object key : prop.keySet()) {
			table += "<tr><td> " + key + " :</td><td>" + prop.get(key) + "</td></tr>";
		}
		table += "</table></body></html>";
		return table;
	}

	public void setOrRoles(boolean orRoles) {
        this.orRoles = orRoles;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

	public String getId() {
		return FILTER_ID;
	}
}
