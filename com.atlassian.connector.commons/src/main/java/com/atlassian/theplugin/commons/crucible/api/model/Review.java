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

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.EnumSet;
import java.util.Collection;

public interface Review {

	boolean isCompleted();

	@NotNull
	User getAuthor();

	User getCreator();

	/**
	 *
	 * @return Statement of Objectives
	 */
	String getDescription();

	@NotNull
	User getModerator();

	String getName();

	PermId getParentReview();

	PermId getPermId();

	@NotNull
	String getProjectKey();

	String getRepoName();

	State getState();

	boolean isAllowReviewerToJoin();

	int getMetricsVersion();

	Date getCreateDate();

	Date getCloseDate();

	String getSummary();

	Set<Reviewer> getReviewers() throws ValueNotYetInitialized;

	List<GeneralComment> getGeneralComments() throws ValueNotYetInitialized;

//    List<VersionedComment> getVersionedComments() throws ValueNotYetInitialized;

//    List<CrucibleFileInfo> getFiles() throws ValueNotYetInitialized;

//	List<CrucibleReviewItemInfo> getReviewItems();

	@Nullable
	CrucibleFileInfo getFileByPermId(PermId id) throws ValueNotYetInitialized;

	EnumSet<CrucibleAction> getTransitions() throws ValueNotYetInitialized;

	EnumSet<CrucibleAction> getActions() throws ValueNotYetInitialized;

	void removeGeneralComment(final GeneralComment comment);

	void removeVersionedComment(final VersionedComment vComment, final CrucibleFileInfo file) throws ValueNotYetInitialized;

	void setFilesAndVersionedComments(Set<CrucibleFileInfo> files, List<VersionedComment> commentList);

	Set<CrucibleFileInfo> getFiles() throws ValueNotYetInitialized;

	void setReviewers(Set<Reviewer> reviewers);

	void setGeneralComments(List<GeneralComment> generalComments);

	void setTransitions(Collection<CrucibleAction> transitions);

	void setActions(Set<CrucibleAction> actions);

	void setAuthor(@NotNull User value);

	void setCreator(User value);

	void setDescription(String value);

	void setModerator(@NotNull User value);

	void setName(String value);

	void setParentReview(PermId value);

	void setPermId(PermId value);

	void setProjectKey(@NotNull String value);

	void setRepoName(String value);

	void setState(State value);

	void setAllowReviewerToJoin(boolean allowReviewerToJoin);

	void setMetricsVersion(int metricsVersion);

	void setCreateDate(Date createDate);

	void setCloseDate(Date closeDate);

	void setSummary(String summary);

//	void setReviewItems(List<CrucibleReviewItemInfo> reviewItems);

	int getNumberOfVersionedComments() throws ValueNotYetInitialized;

	int getNumberOfVersionedCommentsDefects() throws ValueNotYetInitialized;

	int getNumberOfGeneralCommentsDefects() throws ValueNotYetInitialized;

	int getNumberOfGeneralComments() throws ValueNotYetInitialized;

	int getNumberOfGeneralCommentsDefects(final String userName) throws ValueNotYetInitialized;

	int getNumberOfVersionedCommentsDefects(final String userName) throws ValueNotYetInitialized;

	int getNumberOfVersionedCommentsDrafts() throws ValueNotYetInitialized;

	int getNumberOfGeneralCommentsDrafts() throws ValueNotYetInitialized;

	int getNumberOfGeneralCommentsDrafts(final String userName) throws ValueNotYetInitialized;

	int getNumberOfVersionedCommentsDrafts(final String userName) throws ValueNotYetInitialized;

	int getNumberOfVersionedComments(final String userName) throws ValueNotYetInitialized;

	int getNumberOfGeneralComments(final String userName) throws ValueNotYetInitialized;

	void setFiles(final Set<CrucibleFileInfo> files);

	CrucibleProject getCrucibleProject();
}