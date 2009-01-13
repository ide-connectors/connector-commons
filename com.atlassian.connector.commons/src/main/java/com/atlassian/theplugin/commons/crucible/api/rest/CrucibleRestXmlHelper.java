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

package com.atlassian.theplugin.commons.crucible.api.rest;

import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.CrucibleVersion;
import static com.atlassian.theplugin.commons.crucible.api.JDomHelper.getContent;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import org.apache.commons.lang.StringUtils;
import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.*;

public final class CrucibleRestXmlHelper {
	private static final String CDATA_END = "]]>";


	///CLOVER:OFF
	private CrucibleRestXmlHelper() {
	}
	///CLOVER:ON

	public static String getChildText(Element node, String childName) {
		final Element child = node.getChild(childName);
		if (child != null) {
			return child.getText();
		}
		return "";
	}


	@SuppressWarnings("unchecked")
	public static List<Element> getChildElements(Element node, String childName) {
		return node.getChildren(childName);
	}

	public static CrucibleProjectBean parseProjectNode(Element projectNode) {
		CrucibleProjectBean project = new CrucibleProjectBean();

		project.setId(getChildText(projectNode, "id"));
		project.setKey(getChildText(projectNode, "key"));
		project.setName(getChildText(projectNode, "name"));

		return project;
	}

	public static RepositoryBean parseRepositoryNode(Element repoNode) {
		RepositoryBean repo = new RepositoryBean();
		repo.setName(getChildText(repoNode, "name"));
		repo.setType(getChildText(repoNode, "type"));
		repo.setEnabled(Boolean.parseBoolean(getChildText(repoNode, "enabled")));
		return repo;
	}

	public static SvnRepositoryBean parseSvnRepositoryNode(Element repoNode) {
		SvnRepositoryBean repo = new SvnRepositoryBean();
		repo.setName(getChildText(repoNode, "name"));
		repo.setType(getChildText(repoNode, "type"));
		repo.setEnabled(Boolean.parseBoolean(getChildText(repoNode, "enabled")));
		repo.setUrl(getChildText(repoNode, "url"));
		repo.setPath(getChildText(repoNode, "path"));
		return repo;
	}

	public static UserBean parseUserNode(Element repoNode) {
		UserBean userDataBean = new UserBean();

		CrucibleVersion version = CrucibleVersion.CRUCIBLE_15;
		Element userName = repoNode.getChild("userName");
		if (userName != null && !userName.getText().equals("")) {
			version = CrucibleVersion.CRUCIBLE_16;
		}
		if (version == CrucibleVersion.CRUCIBLE_15) {
			userDataBean.setUserName(repoNode.getText());
			userDataBean.setDisplayName(userDataBean.getUserName());
		} else {
			userDataBean.setUserName(getChildText(repoNode, "userName"));
			userDataBean.setDisplayName(getChildText(repoNode, "displayName"));
		}
		return userDataBean;
	}

	public static Action parseActionNode(Element element) {
		return Action.fromValue(getChildText(element, "name"));
	}

	public static ReviewerBean parseReviewerNode(Element reviewerNode) {
		ReviewerBean reviewerBean = new ReviewerBean();

		CrucibleVersion version = CrucibleVersion.CRUCIBLE_15;
		Element userName = reviewerNode.getChild("userName");
		if (userName != null && !userName.getText().equals("")) {
			version = CrucibleVersion.CRUCIBLE_16;
		}
		if (version == CrucibleVersion.CRUCIBLE_15) {
			reviewerBean.setUserName(reviewerNode.getText());
			reviewerBean.setDisplayName(reviewerBean.getUserName());
		} else {
			reviewerBean.setUserName(getChildText(reviewerNode, "userName"));
			reviewerBean.setDisplayName(getChildText(reviewerNode, "displayName"));
			reviewerBean.setCompleted(Boolean.parseBoolean(getChildText(reviewerNode, "completed")));
		}
		return reviewerBean;
	}

	private static void parseReview(Element reviewNode, ReviewBean review) {
		if (reviewNode.getChild("author") != null) {
			review.setAuthor(parseUserNode(reviewNode.getChild("author")));
		}
		if (reviewNode.getChild("creator") != null) {
			review.setCreator(parseUserNode(reviewNode.getChild("creator")));
		}
		if (reviewNode.getChild("moderator") != null) {
			review.setModerator(parseUserNode(reviewNode.getChild("moderator")));
		}
		review.setCreateDate(parseDateTime(getChildText(reviewNode, "createDate")));
		review.setCloseDate(parseDateTime(getChildText(reviewNode, "closeDate")));
		review.setDescription(getChildText(reviewNode, "description"));
		review.setName(getChildText(reviewNode, "name"));
		review.setProjectKey(getChildText(reviewNode, "projectKey"));
		review.setRepoName(getChildText(reviewNode, "repoName"));

		String stateString = getChildText(reviewNode, "state");
		if (!"".equals(stateString)) {
			review.setState(State.fromValue(stateString));
		}
		review.setAllowReviewerToJoin(Boolean.parseBoolean(getChildText(reviewNode, "allowReviewersToJoin")));

		if (reviewNode.getChild("permaId") != null) {
			PermIdBean permId = new PermIdBean(reviewNode.getChild("permaId").getChild("id").getText());
			review.setPermId(permId);
		}
		review.setSummary(getChildText(reviewNode, "summary"));

		try {
			review.setMetricsVersion(Integer.valueOf(getChildText(reviewNode, "metricsVersion")));
		} catch (NumberFormatException e) {
			review.setMetricsVersion(-1);
		}
	}

	public static ReviewBean parseReviewNode(String serverUrl, Element reviewNode) {
		ReviewBean review = new ReviewBean(serverUrl);
		parseReview(reviewNode, review);
		return review;
	}

	public static ReviewBean parseDetailedReviewNode(String serverUrl, String myUserName, Element reviewNode) {
		ReviewBean review = new ReviewBean(serverUrl);
		parseReview(reviewNode, review);

		List<Element> reviewersNode = getChildElements(reviewNode, "reviewers");
		Set<Reviewer> reviewers = new HashSet<Reviewer>();
		for (Element reviewer : reviewersNode) {
			List<Element> reviewerNode = getChildElements(reviewer, "reviewer");
			for (Element element : reviewerNode) {
				reviewers.add(parseReviewerNode(element));
			}
		}
		review.setReviewers(reviewers);

//		List<CrucibleReviewItemInfo> reviewItems = new ArrayList<CrucibleReviewItemInfo>();

		// ***** GeneralComments ******
		List<Element> generalCommentsNode = getChildElements(reviewNode, "generalComments");
		for (Element generalComment : generalCommentsNode) {
			List<Element> generalCommentsDataNode = getChildElements(generalComment, "generalCommentData");
			List<GeneralComment> generalComments = new ArrayList<GeneralComment>();

			for (Element generalCommentData : generalCommentsDataNode) {
				GeneralComment c = parseGeneralCommentNode(myUserName, generalCommentData);
				if (c != null) {
					generalComments.add(c);
				}
			}
			review.setGeneralComments(generalComments);
		}

		// ***** VerionedComments ******
		List<Element> versionedComments = getChildElements(reviewNode, "versionedComments");
		List<VersionedComment> comments = new ArrayList<VersionedComment>();
		for (Element element : versionedComments) {
			List<Element> versionedCommentsData = getChildElements(element, "versionedLineCommentData");
			for (Element versionedElementData : versionedCommentsData) {
				//ONLY COMMENTS NO FILES
				VersionedComment c = parseVersionedCommentNode(myUserName, versionedElementData);
				if (c != null) {
					comments.add(c);
				}
			}
		}

		// ***** Files ******
		List<Element> fileNode = getChildElements(reviewNode, "reviewItems");
		Set<CrucibleFileInfo> files = null;
		if (fileNode.size() > 0) {
			files = new HashSet<CrucibleFileInfo>();
			for (Element element : fileNode) {
				List<Element> fileElements = getChildElements(element, "reviewItem");
				for (Element file : fileElements) {
					CrucibleFileInfo fileInfo = CrucibleRestXmlHelper.parseReviewItemNode(review, file);
					files.add(fileInfo);
				}
			}
		}

		review.setFilesAndVersionedComments(files, comments);
		//	review.setReviewItems(reviewItems);

		List<Element> transitionsNode = getChildElements(reviewNode, "transitions");
		List<Action> transitions = new ArrayList<Action>();
		for (Element transition : transitionsNode) {
			List<Element> trans = getChildElements(transition, "transitionData");
			for (Element element : trans) {
				transitions.add(parseActionNode(element));
			}
		}
		review.setTransitions(transitions);

		List<Element> actionsNode = getChildElements(reviewNode, "actions");
		Set<Action> actions = new HashSet<Action>();
		for (Element action : actionsNode) {
			List<Element> act = getChildElements(action, "actionData");
			for (Element element : act) {
				actions.add(parseActionNode(element));
			}
		}
		review.setActions(actions);

		return review;
	}

	public static Element addTag(Element root, String tagName, String tagValue) {
		Element newElement = new Element(tagName);
		newElement.addContent(tagValue);
		getContent(root).add(newElement);
		return newElement;
	}

	private static String escapeForCdata(String source) {
		if (source == null) {
			return null;
		}

		StringBuffer sb = new StringBuffer();

		int index;
		int oldIndex = 0;
		while ((index = source.indexOf(CDATA_END, oldIndex)) > -1) {
			sb.append(source.substring(oldIndex, index));
			oldIndex = index + CDATA_END.length();
			sb.append("&#x5D;&#x5D;>");
		}

		sb.append(source.substring(oldIndex));
		return sb.toString();
	}

	public static Document prepareCreateReviewNode(Review review, String patch) {
		Element root = new Element("createReview");
		Document doc = new Document(root);

		getContent(root).add(prepareReviewNodeElement(review));

		if (patch != null) {
			Element patchData = new Element("patch");
			getContent(root).add(patchData);

			CDATA patchT = new CDATA(escapeForCdata(patch));
			patchData.setContent(patchT);
		}
		return doc;
	}

	public static Document prepareCloseReviewSummaryNode(String message) {
		Element root = new Element("closeReviewSummary");
		Document doc = new Document(root);

		if (message != null) {
			Element messageData = new Element("summary");
			getContent(root).add(messageData);

			CDATA patchT = new CDATA(escapeForCdata(message));
			messageData.setContent(patchT);
		}
		return doc;
	}

	public static Document prepareCreateReviewNode(Review review, List<String> revisions) {
		Element root = new Element("createReview");
		Document doc = new Document(root);

		getContent(root).add(prepareReviewNodeElement(review));

		if (!revisions.isEmpty()) {
			Element changes = new Element("changesets");
			addTag(changes, "repository", review.getRepoName());
			getContent(root).add(changes);
			for (String revision : revisions) {
				Element rev = new Element("changesetData");
				getContent(changes).add(rev);
				addTag(rev, "id", revision);
			}
		}
		return doc;
	}

	public static Document prepareAddChangesetNode(String repoName, List<String> revisions) {
		Element root = new Element("addChangeset");
		Document doc = new Document(root);

		addTag(root, "repository", repoName);

		if (!revisions.isEmpty()) {
			Element changes = new Element("changesets");
			getContent(root).add(changes);
			for (String revision : revisions) {
				Element rev = new Element("changesetData");
				getContent(changes).add(rev);
				addTag(rev, "id", revision);
			}
		}
		return doc;
	}

	public static Document prepareAddPatchNode(String repoName, String patch) {
		Element root = new Element("addPatch");
		Document doc = new Document(root);

		addTag(root, "repository", repoName);

		if (patch != null) {
			Element patchData = new Element("patch");
			getContent(root).add(patchData);
			CDATA patchT = new CDATA(escapeForCdata(patch));
			patchData.setContent(patchT);
		}
		return doc;
	}

	public static Document prepareAddItemNode(NewReviewItem item) {
		Element root = new Element("reviewItem");
		Document doc = new Document(root);

		addTag(root, "repositoryName", item.getRepositoryName());
		addTag(root, "fromPath", item.getFromPath());
		addTag(root, "fromRevision", item.getFromRevision());
		addTag(root, "toPath", item.getToPath());
		addTag(root, "toRevision", item.getToRevision());

		return doc;
	}

	public static Document prepareReviewNode(Review review) {
		Element reviewData = prepareReviewNodeElement(review);
		return new Document(reviewData);
	}

	private static Element prepareReviewNodeElement(Review review) {
		Element reviewData = new Element("reviewData");

		Element authorElement = new Element("author");
		getContent(reviewData).add(authorElement);
		addTag(authorElement, "userName", review.getAuthor().getUserName());

		Element creatorElement = new Element("creator");
		getContent(reviewData).add(creatorElement);
		addTag(creatorElement, "userName", review.getCreator().getUserName());

		Element moderatorElement = new Element("moderator");
		getContent(reviewData).add(moderatorElement);
		addTag(moderatorElement, "userName", review.getModerator().getUserName());

		addTag(reviewData, "description", review.getDescription());
		addTag(reviewData, "name", review.getName());
		addTag(reviewData, "projectKey", review.getProjectKey());
//		addTag(reviewData, "repoName", review.getRepoName());
		if (review.getState() != null) {
			addTag(reviewData, "state", review.getState().value());
		}
		addTag(reviewData, "allowReviewersToJoin", Boolean.toString(review.isAllowReviewerToJoin()));
		if (review.getPermId() != null) {
			Element permIdElement = new Element("permaId");
			getContent(reviewData).add(permIdElement);
			addTag(permIdElement, "id", review.getPermId().getId());
		}

		return reviewData;
	}


	public static CrucibleFileInfo parseReviewItemNode(final Review review, final Element reviewItemNode) {
		CrucibleFileInfoImpl reviewItem = new CrucibleFileInfoImpl(
				new VersionedVirtualFile(
						getChildText(reviewItemNode, "toPath"),
						getChildText(reviewItemNode, "toRevision")
				),
				new VersionedVirtualFile(
						getChildText(reviewItemNode, "fromPath"),
						getChildText(reviewItemNode, "fromRevision")
				),
				null
		);

		String c = getChildText(reviewItemNode, "commitType");
		if (!"".equals(c)) {
			reviewItem.setCommitType(CommitType.valueOf(c));
			if (reviewItem.getCommitType() == CommitType.Added) {
				if (!"".equals(reviewItem.getOldFileDescriptor().getRevision())
						&& !"".equals(reviewItem.getFileDescriptor().getRevision())) {
					reviewItem.setCommitType(CommitType.Moved);
				}
			}
		} else {
			if (!"".equals(reviewItem.getOldFileDescriptor().getRevision())
					&& !"".equals(reviewItem.getFileDescriptor().getRevision())) {
				reviewItem.setCommitType(CommitType.Modified);
			} else {
				if ("".equals(reviewItem.getOldFileDescriptor().getRevision())
						&& !"".equals(reviewItem.getFileDescriptor().getRevision())) {
					reviewItem.setCommitType(CommitType.Added);
				} else {
					if ("".equals(reviewItem.getOldFileDescriptor().getRevision())
							&& !"".equals(reviewItem.getFileDescriptor().getRevision())) {
						reviewItem.setCommitType(CommitType.Deleted);
					} else {
						reviewItem.setCommitType(CommitType.Unknown);
					}
				}
			}
		}
		reviewItem.setRepositoryName(getChildText(reviewItemNode, "repositoryName"));
		reviewItem.setAuthorName(getChildText(reviewItemNode, "authorName"));
		reviewItem.setCommitDate(parseDateTime(getChildText(reviewItemNode, "commitDate")));
		final String fileType = getChildText(reviewItemNode, "fileType");
		if (fileType != null && !"".equals(fileType)) {
			try {
				reviewItem.setFileType(FileType.valueOf(fileType));
			} catch (IllegalArgumentException ex) {
				reviewItem.setFileType(FileType.Unknown);
			}
		}
		if (reviewItemNode.getChild("permId") != null) {
			PermIdBean permId = new PermIdBean(reviewItemNode.getChild("permId").getChild("id").getText());
			reviewItem.setFilePermId(permId);
		}

		return reviewItem;
	}

	private static boolean parseGeneralComment(String myUserName, GeneralCommentBean commentBean,
			Element reviewCommentNode) {

		if (!parseComment(myUserName, commentBean, reviewCommentNode)) {
			return false;
		}
		List<Element> replies = getChildElements(reviewCommentNode, "replies");
		if (replies != null) {
			List<GeneralComment> rep = new ArrayList<GeneralComment>();
			for (Element repliesNode : replies) {
				List<Element> entries = getChildElements(repliesNode, "generalCommentData");
				for (Element replyNode : entries) {
					GeneralCommentBean reply = parseGeneralCommentNode(myUserName, replyNode);
					if (reply != null) {
						reply.setReply(true);
						rep.add(reply);
					}
				}
			}
			commentBean.setReplies(rep);
		}

		return true;
	}

	private static boolean parseVersionedComment(String myUserName, VersionedCommentBean commentBean,
			Element reviewCommentNode) {

		if (!parseComment(myUserName, commentBean, reviewCommentNode)) {
			return false;
		}

		// read following xml
		// <reviewItemId>
		// 	<id>CFR-126</id>
		// </reviewItemId>
		List<Element> reviewIds = getChildElements(reviewCommentNode, "reviewItemId");
		for (Element reviewId : reviewIds) {
			List<Element> ids = getChildElements(reviewId, "id");
			for (Element id : ids) {
				commentBean.setReviewItemId(new PermIdBean(id.getText()));
				break;
			}
			break;
		}

		List<Element> replies = getChildElements(reviewCommentNode, "replies");
		if (replies != null) {
			List<VersionedComment> rep = new ArrayList<VersionedComment>();
			for (Element repliesNode : replies) {
				List<Element> entries = getChildElements(repliesNode, "generalCommentData");
				for (Element replyNode : entries) {
					VersionedCommentBean reply = parseVersionedCommentNodeWithHints(myUserName, replyNode,
							commentBean.isFromLineInfo(),
							commentBean.getFromStartLine(),
							commentBean.getToStartLine(),
							commentBean.isToLineInfo(),
							commentBean.getFromEndLine(),
							commentBean.getToEndLine()
					);
					if (reply != null) {
						reply.setReply(true);
						rep.add(reply);
					}
				}
			}
			commentBean.setReplies(rep);
		}

		return true;
	}

	private static boolean parseComment(String myUserName, CommentBean commentBean, Element reviewCommentNode) {

		boolean isDraft = Boolean.parseBoolean(getChildText(reviewCommentNode, "draft"));
		for (Element element : getChildElements(reviewCommentNode, "user")) {
			UserBean commentAuthor = parseUserNode(element);

			// drop comments in draft state where I am the author - bug PL-772 and PL-900
			if (isDraft && !commentAuthor.getUserName().equals(myUserName)) {
				return false;
			}
			commentBean.setAuthor(commentAuthor);
		}
		commentBean.setDraft(isDraft);

		commentBean.setMessage(getChildText(reviewCommentNode, "message"));
		commentBean.setDefectRaised(Boolean.parseBoolean(getChildText(reviewCommentNode, "defectRaised")));
		commentBean.setDefectApproved(Boolean.parseBoolean(getChildText(reviewCommentNode, "defectApproved")));
		commentBean.setDeleted(Boolean.parseBoolean(getChildText(reviewCommentNode, "deleted")));
		commentBean.setCreateDate(parseDateTime(getChildText(reviewCommentNode, "createDate")));
		PermIdBean permId = new PermIdBean(getChildText(reviewCommentNode, "permaIdAsString"));
		commentBean.setPermId(permId);


		List<Element> metrics = getChildElements(reviewCommentNode, "metrics");
		if (metrics != null) {
			for (Element metric : metrics) {
				List<Element> entries = getChildElements(metric, "entry");
				for (Element entry : entries) {
					String key = getChildText(entry, "key");
					List<Element> values = getChildElements(entry, "value");
					for (Element value : values) {
						CustomFieldBean field = new CustomFieldBean();
						field.setConfigVersion(Integer.parseInt(getChildText(value, "configVersion")));
						field.setValue(getChildText(value, "value"));
						commentBean.getCustomFields().put(key, field);
						break;
					}
				}
			}
		}
		return true;
	}

	private static void prepareComment(Comment comment, Element commentNode) {
		String date = COMMENT_TIME_FORMAT.print(comment.getCreateDate().getTime());
		String strangeDate = date.substring(0, date.length() - 2);
		strangeDate += ":00";
		addTag(commentNode, "createDate", strangeDate);
		Element userElement = new Element("user");
		getContent(commentNode).add(userElement);
		addTag(userElement, "userName", comment.getAuthor().getUserName());
		addTag(commentNode, "defectRaised", Boolean.toString(comment.isDefectRaised()));
		addTag(commentNode, "defectApproved", Boolean.toString(comment.isDefectApproved()));
		addTag(commentNode, "deleted", Boolean.toString(comment.isDeleted()));
		addTag(commentNode, "draft", Boolean.toString(comment.isDraft()));
		addTag(commentNode, "message", comment.getMessage());
		Element metrics = new Element("metrics");
		getContent(commentNode).add(metrics);

		for (String key : comment.getCustomFields().keySet()) {
			Element entry = new Element("entry");
			getContent(metrics).add(entry);
			addTag(entry, "key", key);
			CustomField field = comment.getCustomFields().get(key);
			getContent(entry).add(prepareCustomFieldValue(field));
		}

		Element replies = new Element("replies");
		getContent(commentNode).add(replies);
	}

	public static GeneralCommentBean parseGeneralCommentNode(String myUserName, Element reviewCommentNode) {
		GeneralCommentBean reviewCommentBean = new GeneralCommentBean();
		if (!parseGeneralComment(myUserName, reviewCommentBean, reviewCommentNode)) {
			return null;
		}
		return reviewCommentBean;
	}

	public static Document prepareGeneralComment(Comment comment) {
		Element commentNode = new Element("generalCommentData");
		Document doc = new Document(commentNode);
		prepareComment(comment, commentNode);
		return doc;
	}

	public static Document prepareVersionedComment(PermId riId, VersionedComment comment) {
		if (comment.getToStartLine() > comment.getToEndLine()) {
			throw new IllegalArgumentException("Comment start cannot be after comment end!");
		}
		Element commentNode = new Element("versionedLineCommentData");
		Document doc = new Document(commentNode);
		prepareComment(comment, commentNode);
		Element reviewItemId = new Element("reviewItemId");
		getContent(commentNode).add(reviewItemId);
		addTag(reviewItemId, "id", riId.getId());
		if (comment.getFromStartLine() > 0 && comment.getFromEndLine() > 0) {
			addTag(commentNode, "fromLineRange", comment.getFromStartLine() + "-" + comment.getFromEndLine());
		}
		if (comment.getToStartLine() > 0 && comment.getToEndLine() > 0) {
			addTag(commentNode, "toLineRange", comment.getToStartLine() + "-" + comment.getToEndLine());
		}
		return doc;
	}

	///CHECKSTYLE:OFF
	public static VersionedCommentBean parseVersionedCommentNodeWithHints(String myUserName, Element reviewCommentNode,
			boolean fromLineInfo,
			int fromStartLine,
			int toStartLine,
			boolean toLineInfo,
			int fromEndLine,
			int toEndLine) {
		VersionedCommentBean result = parseVersionedCommentNode(myUserName, reviewCommentNode);
		if (result == null) {
			return null;
		}
		if (!result.isFromLineInfo() && fromLineInfo) {
			result.setFromLineInfo(true);
			result.setFromStartLine(fromStartLine);
			result.setFromEndLine(fromEndLine);
		}
		if (!result.isToLineInfo() && toLineInfo) {
			result.setToLineInfo(true);
			result.setToStartLine(toStartLine);
			result.setToEndLine(toEndLine);
		}
		return result;
	}
	///CHECKSTYLE:ON

	public static VersionedCommentBean parseVersionedCommentNode(String myUserName, Element reviewCommentNode) {
		VersionedCommentBean comment = new VersionedCommentBean();
		if (!parseVersionedComment(myUserName, comment, reviewCommentNode)) {
			return null;
		}

		if (reviewCommentNode.getChild("reviewItemId") != null) {
			PermIdBean reviewItemId = new PermIdBean(reviewCommentNode.getChild("reviewItemId").getChild("id").getText());
			comment.setReviewItemId(reviewItemId);

		}

		if (reviewCommentNode.getChild("fromLineRange") != null) {
			String toLineRange = getChildText(reviewCommentNode, "fromLineRange");
			String[] tokens = toLineRange.split("-");
			if (tokens.length > 0) {
				comment.setFromLineInfo(true);
				try {
					int start = Integer.parseInt(tokens[0]);
					comment.setFromStartLine(start);
				} catch (NumberFormatException e) {
					// leave 0 value
				}
				if (tokens.length > 1) {
					try {
						int stop = Integer.parseInt(tokens[1]);
						comment.setFromEndLine(stop);
					} catch (NumberFormatException e) {
						// leave 0 value
					}
				}
			}
		}

		if (reviewCommentNode.getChild("toLineRange") != null) {
			String toLineRange = getChildText(reviewCommentNode, "toLineRange");
			String[] tokens = toLineRange.split("-");
			if (tokens.length > 0) {
				comment.setToLineInfo(true);
				try {
					int start = Integer.parseInt(tokens[0]);
					comment.setToStartLine(start);
				} catch (NumberFormatException e) {
					// leave 0 value
				}
				if (tokens.length > 1) {
					try {
						int stop = Integer.parseInt(tokens[1]);
						comment.setToEndLine(stop);
					} catch (NumberFormatException e) {
						// leave 0 value
					}
				}
			}
		}

		return comment;
	}

	private static Element prepareCustomFieldValue(CustomField value) {
		Element entry = new Element("value");
		addTag(entry, "configVersion", Integer.toString(value.getConfigVersion()));
		addTag(entry, "value", value.getValue());
		return entry;
	}

	private static CustomFieldValue getCustomFieldValue(CustomFieldValueType type, Element element) {
		CustomFieldValue newValue = new CustomFieldValue();
		newValue.setName(getChildText(element, "name"));
		switch (type) {
			case INTEGER:
				newValue.setValue(Integer.valueOf(getChildText(element, "value")));
				break;
			case STRING:
				newValue.setValue(getChildText(element, "value"));
				break;
			case BOOLEAN:
				newValue.setValue(Boolean.valueOf(getChildText(element, "value")));
				break;
			case DATE:
				// date not set by default at this moment - not sure date representation
			default:
				newValue.setValue(null);
		}
		return newValue;
	}

	public static CustomFieldDefBean parseMetricsNode(Element element) {
		CustomFieldDefBean field = new CustomFieldDefBean();

		field.setName(getChildText(element, "name"));
		field.setLabel(getChildText(element, "label"));
		field.setType(CustomFieldValueType.valueOf(getChildText(element, "type")));
		field.setConfigVersion(Integer.parseInt(getChildText(element, "configVersion")));

		List<Element> defaultValue = getChildElements(element, "defaultValue");
		for (Element value : defaultValue) {
			field.setDefaultValue(getCustomFieldValue(field.getType(), value));
		}
		List<Element> values = getChildElements(element, "values");
		for (Element value : values) {
			field.getValues().add(getCustomFieldValue(field.getType(), value));
		}

		return field;
	}

	public static Document prepareCustomFilter(CustomFilter filter) {
		Element filterData = prepareFilterNodeElement(filter);
		return new Document(filterData);
	}

	private static Element prepareFilterNodeElement(CustomFilter filter) {
		Element filterData = new Element("customFilterData");

		addTag(filterData, CustomFilter.AUTHOR, filter.getAuthor() != null ? filter.getAuthor() : "");
		addTag(filterData, CustomFilter.CREATOR, filter.getCreator() != null ? filter.getCreator() : "");
		addTag(filterData, CustomFilter.MODERATOR, filter.getModerator() != null ? filter.getModerator() : "");
		addTag(filterData, CustomFilter.REVIEWER, filter.getReviewer() != null ? filter.getReviewer() : "");
		addTag(filterData, CustomFilter.PROJECT, filter.getProjectKey() != null ? filter.getProjectKey() : "");
		String state = filter.getStates();
		// BEWARE - state instead of CustomFIlter.STATE
		if (!StringUtils.isEmpty(state)) {
			addTag(filterData, "state", state);
		}
		if (filter.isComplete() != null) {
			addTag(filterData, CustomFilter.COMPLETE, Boolean.toString(filter.isComplete()));
		}
		addTag(filterData, CustomFilter.ORROLES, Boolean.toString(filter.isOrRoles()));
		if (filter.isAllReviewersComplete() != null) {
			addTag(filterData, CustomFilter.ALLCOMPLETE, Boolean.toString(filter.isAllReviewersComplete()));
		}

		return filterData;
	}

	private static final DateTimeFormatter COMMENT_TIME_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	private static Date parseDateTime(String date) {
		if (date != null && !date.equals("")) {
			return COMMENT_TIME_FORMAT.parseDateTime(date).toDate();
		} else {
			return null;
		}
	}

	public static CrucibleVersionInfo parseVersionNode(Element element) {
		CrucibleVersionInfoBean version = new CrucibleVersionInfoBean();
		version.setBuildDate(getChildText(element, "buildDate"));
		version.setReleaseNumber(getChildText(element, "releaseNumber"));
		return version;
	}
}
