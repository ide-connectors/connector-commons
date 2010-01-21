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

import static com.atlassian.theplugin.commons.crucible.api.JDomHelper.getContent;
import com.atlassian.connector.commons.misc.IntRanges;
import com.atlassian.connector.commons.misc.IntRangesParser;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.CrucibleVersion;
import com.atlassian.theplugin.commons.crucible.api.PathAndRevision;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CommentBean;
import com.atlassian.theplugin.commons.crucible.api.model.CommitType;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfoImpl;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleVersionInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CustomField;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldBean;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldDefBean;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldValue;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldValueType;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.FileType;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralCommentBean;
import com.atlassian.theplugin.commons.crucible.api.model.NewReviewItem;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.crucible.api.model.RepositoryType;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.RevisionData;
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.commons.crucible.api.model.SvnRepository;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedCommentBean;
import com.atlassian.theplugin.commons.crucible.api.model.changes.Change;
import com.atlassian.theplugin.commons.crucible.api.model.changes.Changes;
import com.atlassian.theplugin.commons.crucible.api.model.changes.Link;
import com.atlassian.theplugin.commons.crucible.api.model.changes.Revision;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.commons.util.MiscUtil;
import org.apache.commons.lang.StringUtils;
import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CrucibleRestXmlHelper {
	private static final String CDATA_END = "]]>";
    private static final String PRESERVE_OLD = "{preserve-old}";

    ///CLOVER:OFF
	private CrucibleRestXmlHelper() {
	}
	///CLOVER:ON

	@NotNull
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

	public static CrucibleProject parseProjectNode(Element projectNode) {
		return new CrucibleProject(
				getChildText(projectNode, "id"),
				getChildText(projectNode, "key"),
				getChildText(projectNode, "name"),
                parseUserNames(projectNode.getChild("allowedReviewers")));
	}

	public static Repository parseRepositoryNode(Element repoNode) {
		return new Repository(
				getChildText(repoNode, "name"),
				getChildText(repoNode, "type"),
				Boolean.parseBoolean(getChildText(repoNode, "enabled")));
	}

	public static SvnRepository parseSvnRepositoryNode(Element repoNode) {
		return new SvnRepository(
				getChildText(repoNode, "name"),
				getChildText(repoNode, "type"),
				Boolean.parseBoolean(getChildText(repoNode, "enabled")),
				getChildText(repoNode, "url"),
				getChildText(repoNode, "path"));
	}

	public static User parseUserNode(Element repoNode) {
		CrucibleVersion version = CrucibleVersion.CRUCIBLE_15;
		Element userName = repoNode.getChild("userName");
		if (userName != null && !userName.getText().equals("")) {
			version = CrucibleVersion.CRUCIBLE_16;
		}
		if (version == CrucibleVersion.CRUCIBLE_15) {
			return new User(repoNode.getText(), repoNode.getText());
		} else {
			return new User(getChildText(repoNode, "userName"),
					getChildText(repoNode, "displayName"));
		}
	}

	public static CrucibleAction parseActionNode(Element element) {
		return CrucibleAction.fromValue(getChildText(element, "name"));
	}

	public static Reviewer parseReviewerNode(Element reviewerNode) {
		CrucibleVersion version = CrucibleVersion.CRUCIBLE_15;
		Element userName = reviewerNode.getChild("userName");
		if (userName != null && !userName.getText().equals("")) {
			version = CrucibleVersion.CRUCIBLE_16;
		}
		if (version == CrucibleVersion.CRUCIBLE_15) {
			return new Reviewer(reviewerNode.getText(),
					reviewerNode.getText());
		} else {
			return new Reviewer(
					getChildText(reviewerNode, "userName"),
					getChildText(reviewerNode, "displayName"),
					Boolean.parseBoolean(getChildText(reviewerNode, "completed")));
		}
	}


    @Nullable
	private static Collection<String> parseUserNames(Element userNamesNode) {
        if (userNamesNode != null) {
            Collection<String> userNamesList = new ArrayList<String>();
            for (Element userName : getChildElements(userNamesNode, "userName")) {
                userNamesList.add(userName.getText());
            }

            return userNamesList;
        }
        return null;
    }

	private static void parseReview(Element reviewNode, Review review, boolean trimWikiMarkers) {
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
        String soo = getChildText(reviewNode, "description");
        if (trimWikiMarkers) {
            soo = removeWikiMarkers(soo);
        }
        review.setDescription(soo);
		review.setName(getChildText(reviewNode, "name"));
		review.setProjectKey(getChildText(reviewNode, "projectKey"));
		review.setRepoName(getChildText(reviewNode, "repoName"));

		String stateString = getChildText(reviewNode, "state");
		if (!"".equals(stateString)) {
			review.setState(State.fromValue(stateString));
		}
		review.setAllowReviewerToJoin(Boolean.parseBoolean(getChildText(reviewNode, "allowReviewersToJoin")));

		if (reviewNode.getChild("permaId") != null) {
			PermId permId = new PermId(reviewNode.getChild("permaId").getChild("id").getText());
			review.setPermId(permId);
		}
		review.setSummary(getChildText(reviewNode, "summary"));

		try {
			review.setMetricsVersion(Integer.valueOf(getChildText(reviewNode, "metricsVersion")));
		} catch (NumberFormatException e) {
			review.setMetricsVersion(-1);
		}
	}

	public static Review parseReviewNode(String serverUrl, Element reviewNode, boolean trimWikiMarkers) {
		Review review = new Review(serverUrl);
		parseReview(reviewNode, review, trimWikiMarkers);
		return review;
	}

	public static Review parseDetailedReviewNode(String serverUrl, String myUsername,
                                                 Element reviewNode, boolean trimWikiMarkers) {
		Review review = new Review(serverUrl);
		parseReview(reviewNode, review, trimWikiMarkers);

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
			List<Comment> generalComments = new ArrayList<Comment>();

			for (Element generalCommentData : generalCommentsDataNode) {
				Comment c = parseGeneralCommentNode(myUsername, generalCommentData, trimWikiMarkers);
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
				VersionedComment c = parseVersionedCommentNode(myUsername, versionedElementData, trimWikiMarkers);
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
		List<CrucibleAction> transitions = new ArrayList<CrucibleAction>();
		for (Element transition : transitionsNode) {
			List<Element> trans = getChildElements(transition, "transitionData");
			for (Element element : trans) {
				transitions.add(parseActionNode(element));
			}
		}
		review.setTransitions(transitions);

		List<Element> actionsNode = getChildElements(reviewNode, "actions");
		Set<CrucibleAction> actions = new HashSet<CrucibleAction>();
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

	public static Element prepareRevisionData(RevisionData revData) {
		Element root = new Element("revisionData");
		Element source = new Element("source");
		source.addContent(revData.getSource());
		getContent(root).add(source);

		Element path = new Element("path");
		path.addContent(revData.getPath());
		getContent(root).add(path);

		for (String revision : revData.getRevisions()) {
			Element rev = new Element("rev");
			rev.addContent(revision);
			getContent(root).add(rev);
		}
		return root;
	}

	public static Document prepareRevisions(Collection<RevisionData> revisions) {
		Element root = new Element("revisions");
		Document doc = new Document(root);

		if (!revisions.isEmpty()) {
			for (RevisionData revData : revisions) {
				getContent(root).add(prepareRevisionData(revData));
			}
		}
		return doc;
	}

    public static Document prepareRevisionDataNode(String repository, List<PathAndRevision> files) {
        Element root = new Element("revisions");
        Document doc = new Document(root);

        for (PathAndRevision file : files) {
            Element revData = new Element("revisionData");
            for (String revision : file.getRevisions()) {
                addTag(revData, "rev", revision);
            }
            addTag(revData, "source", repository);
            addTag(revData, "path", file.getPath());
            getContent(root).add(revData);
        }
        addTag(root, "repository", repository);

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
		addTag(authorElement, "userName", review.getAuthor().getUsername());

		Element creatorElement = new Element("creator");
		getContent(reviewData).add(creatorElement);
		addTag(creatorElement, "userName", review.getCreator().getUsername());

		Element moderatorElement = new Element("moderator");
		getContent(reviewData).add(moderatorElement);
		addTag(moderatorElement, "userName", review.getModerator().getUsername());

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

		final String fromContentUrl = getChildText(reviewItemNode, "fromContentUrl");
		if (!"".equals(fromContentUrl)) {
			reviewItem.getOldFileDescriptor().setContentUrl(fromContentUrl);
		}
		final String toContentUrl = getChildText(reviewItemNode, "toContentUrl");
		if (!"".equals(toContentUrl)) {
			reviewItem.getFileDescriptor().setContentUrl(toContentUrl);
		}

		String c = getChildText(reviewItemNode, "commitType");
		if (!"".equals(c)) {
			reviewItem.setCommitType(CommitType.valueOf(c));
			// adjust to case when instead of moved status CRU returns added and removed files
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
		if (reviewItem.getRepositoryName().startsWith(RepositoryType.UPLOAD.name())) {
			reviewItem.setRepositoryType(RepositoryType.UPLOAD);
		} else {
			if (reviewItem.getRepositoryName().startsWith(RepositoryType.PATCH.name())) {
				reviewItem.setRepositoryType(RepositoryType.PATCH);
			} else {
				reviewItem.setRepositoryType(RepositoryType.SCM);
			}
		}
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
			PermId permId = new PermId(reviewItemNode.getChild("permId").getChild("id").getText());
			reviewItem.setFilePermId(permId);
		}

		return reviewItem;
	}

	private static boolean parseGeneralComment(String myUsername, GeneralCommentBean commentBean,
			Element reviewCommentNode, boolean trimWikiMarkers) {

		if (!parseComment(myUsername, commentBean, reviewCommentNode, trimWikiMarkers)) {
			return false;
		}
		List<Element> replies = getChildElements(reviewCommentNode, "replies");
		if (replies != null) {
			List<Comment> rep = new ArrayList<Comment>();
			for (Element repliesNode : replies) {
				List<Element> entries = getChildElements(repliesNode, "generalCommentData");
				for (Element replyNode : entries) {
					GeneralCommentBean reply = parseGeneralCommentNode(myUsername, replyNode, trimWikiMarkers);
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

	private static boolean parseVersionedComment(String myUsername, VersionedCommentBean commentBean,
			Element reviewCommentNode, boolean trimWikiMarkers) {

		if (!parseComment(myUsername, commentBean, reviewCommentNode, trimWikiMarkers)) {
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
				commentBean.setReviewItemId(new PermId(id.getText()));
				break;
			}
			break;
		}

		List<Element> replies = getChildElements(reviewCommentNode, "replies");
		if (replies != null) {
			List<Comment> rep = new ArrayList<Comment>();
			for (Element repliesNode : replies) {
				List<Element> entries = getChildElements(repliesNode, "generalCommentData");
				for (Element replyNode : entries) {
					VersionedCommentBean reply =
							parseVersionedCommentNodeWithHints(myUsername, replyNode,
							commentBean.isFromLineInfo(),
							commentBean.getFromStartLine(),
							commentBean.getToStartLine(),
							commentBean.isToLineInfo(),
							commentBean.getFromEndLine(),
							commentBean.getToEndLine(),
                            commentBean.getLineRanges(),
                            trimWikiMarkers
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

	private static boolean parseComment(String myUsername, CommentBean commentBean,
                                        Element reviewCommentNode, boolean trimWikiMarkers) {

		boolean isDraft = Boolean.parseBoolean(getChildText(reviewCommentNode, "draft"));
		for (Element element : getChildElements(reviewCommentNode, "user")) {
			User commentAuthor = parseUserNode(element);

			// drop comments in draft state where I am the author - bug PL-772 and PL-900
			if (isDraft && !commentAuthor.getUsername().equals(myUsername)) {
				return false;
			}
			commentBean.setAuthor(commentAuthor);
		}
		commentBean.setDraft(isDraft);

        String message = getChildText(reviewCommentNode, "message");
        if (trimWikiMarkers) {
            message = removeWikiMarkers(message);
        }
        commentBean.setMessage(message);
		commentBean.setDefectRaised(Boolean.parseBoolean(getChildText(reviewCommentNode, "defectRaised")));
		commentBean.setDefectApproved(Boolean.parseBoolean(getChildText(reviewCommentNode, "defectApproved")));
		commentBean.setDeleted(Boolean.parseBoolean(getChildText(reviewCommentNode, "deleted")));
		commentBean.setCreateDate(parseDateTime(getChildText(reviewCommentNode, "createDate")));
        String readStatus = getChildText(reviewCommentNode, "readStatus");
        commentBean.setReadState(readStatusStringToState(readStatus));

		PermId permId = null;
		if (reviewCommentNode.getChild("permaId") != null) {
			permId = new PermId(reviewCommentNode.getChild("permaId").getChild("id").getText());
			commentBean.setPermId(permId);
		}
		// try old way
		if (commentBean.getPermId() == null) {
			permId = new PermId(getChildText(reviewCommentNode, "permaIdAsString"));
			commentBean.setPermId(permId);
		}

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

    private static String removeWikiMarkers(String message) {
        if (message.startsWith(PRESERVE_OLD)) {
            message = message.substring(PRESERVE_OLD.length());
        }
        if (message.endsWith(PRESERVE_OLD)) {
            message = message.substring(0, message.lastIndexOf(PRESERVE_OLD));
        }
        return message;
    }

    private static Comment.ReadState readStatusStringToState(String readStatus) {
        if (readStatus == null) {
            return Comment.ReadState.UNKNOWN;
        }
        if (readStatus.equals("READ")) {
            return Comment.ReadState.READ;
        } else if (readStatus.equals("UNREAD")) {
            return Comment.ReadState.UNREAD;
        } else if (readStatus.equals("LEAVE_UNREAD")) {
            return Comment.ReadState.LEAVE_UNREAD;
        }
        return Comment.ReadState.UNKNOWN;
    }

    private static void prepareComment(Comment comment, Element commentNode) {
		String date = COMMENT_TIME_FORMAT.print(comment.getCreateDate().getTime());
		String strangeDate = date.substring(0, date.length() - 2);
		strangeDate += ":00";
		addTag(commentNode, "createDate", strangeDate);
		Element userElement = new Element("user");
		getContent(commentNode).add(userElement);
		addTag(userElement, "userName", comment.getAuthor().getUsername());
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

	public static GeneralCommentBean parseGeneralCommentNode(String myUsername,
                                                             Element reviewCommentNode, boolean trimWikiMarkers) {
		GeneralCommentBean reviewCommentBean = new GeneralCommentBean();
		if (!parseGeneralComment(myUsername, reviewCommentBean, reviewCommentNode, trimWikiMarkers)) {
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
	public static VersionedCommentBean parseVersionedCommentNodeWithHints(
            String myUsername, Element reviewCommentNode, boolean fromLineInfo, int fromStartLine, int toStartLine,
            boolean toLineInfo, int fromEndLine, int toEndLine, Map<String, IntRanges> lineRanges,
            boolean trimWikiMarkers) {

		VersionedCommentBean result = parseVersionedCommentNode(myUsername, reviewCommentNode, trimWikiMarkers);
		if (result == null) {
			return null;
		}
        if (result.getLineRanges() == null && lineRanges != null) {
            result.setLineRanges(lineRanges);
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

	public static VersionedCommentBean parseVersionedCommentNode(String myUsername,
                                                                 Element reviewCommentNode, boolean trimWikiMarkers) {
		VersionedCommentBean comment = new VersionedCommentBean();
		if (!parseVersionedComment(myUsername, comment, reviewCommentNode, trimWikiMarkers)) {
			return null;
		}

		if (reviewCommentNode.getChild("reviewItemId") != null) {
			PermId reviewItemId = new PermId(reviewCommentNode.getChild("reviewItemId").getChild("id").getText());
			comment.setReviewItemId(reviewItemId);

		}

		if (reviewCommentNode.getChild("fromLineRange") != null) {
			final String fromLineRange = getChildText(reviewCommentNode, "fromLineRange");
			if (fromLineRange.trim().length() > 0) {
				try {
					comment.setFromLineRanges(IntRangesParser.parse(fromLineRange));
				} catch (NumberFormatException e) {
                    LoggerImpl.getInstance().error(e);
				}
			}
		}

		if (reviewCommentNode.getChild("toLineRange") != null) {
			final String toLineRange = getChildText(reviewCommentNode, "toLineRange");
			if (toLineRange.trim().length() > 0) {
				try {
					comment.setToLineRanges(IntRangesParser.parse(toLineRange));
				} catch (NumberFormatException e) {
                    LoggerImpl.getInstance().error(e);
				}
			}
		}

        Element child = reviewCommentNode.getChild("lineRanges");
        if (child != null) {
            parseAndFillLineRanges(comment, child);
        }
		return comment;
	}

    private static void parseAndFillLineRanges(VersionedCommentBean comment, Element lineRangesNode) {
        Map<String, IntRanges> rangesMap = new LinkedHashMap<String, IntRanges>();
        List<Element> entries = getChildElements(lineRangesNode, "lineRange");
        for (Element rangeNode : entries) {
            String revisionStr = rangeNode.getAttributeValue("revision");
            String rangeStr = rangeNode.getAttributeValue("range");
            try {
                rangesMap.put(revisionStr, IntRangesParser.parse(rangeStr));
            } catch (NumberFormatException e) {
                LoggerImpl.getInstance().error(e);
            }
        }
        comment.setLineRanges(rangesMap);
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
				try {
					newValue.setValue(Integer.valueOf(getChildText(element, "value")));
				} catch (NumberFormatException e) {
					newValue.setValue(0);
				}
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
		return new CrucibleVersionInfo(getChildText(element, "releaseNumber"), getChildText(element, "buildDate"));
	}

	public static Changes parseChangesNode(Element changesNode) {
		List<Change> changes = MiscUtil.buildArrayList();
		for (Element changeNode : getChildElements(changesNode, "change")) {
			changes.add(parseChangeNode(changeNode));
		}
		return new Changes(Boolean.parseBoolean(changesNode.getAttributeValue("olderChangeSetsExist")), Boolean
				.parseBoolean(changesNode.getAttributeValue("newerChangeSetsExist")), changes);
	}

	private static Change parseChangeNode(Element changeNode) {
		String author = changeNode.getAttributeValue("author");
		String csid = changeNode.getAttributeValue("csid");
		Date date = parseDateTime(changeNode.getAttributeValue("date"));
		Link link = parseLinkNode(changeNode.getChild("link"));
		String comment = getChildText(changeNode, "comment");
		List<Revision> revisions = MiscUtil.buildArrayList();
		for (Element revisionNode : getChildElements(changeNode, "revision")) {
			revisions.add(parseRevisionNode(revisionNode));
		}
		return new Change(author, date, csid, link, comment, revisions);
	}

	private static Revision parseRevisionNode(Element revisionNode) {
		String revision = revisionNode.getAttributeValue("revision");
		String path = revisionNode.getAttributeValue("path");
		List<Link> links = MiscUtil.buildArrayList();
		for (Element linkNode : getChildElements(revisionNode, "link")) {
			links.add(parseLinkNode(linkNode));
		}
		return new Revision(revision, path, links);
	}

	private static Link parseLinkNode(Element child) {
		return new Link(child.getAttributeValue("rel"), child.getAttributeValue("href"));
	}

	public static void parseErrorAndThrowIt(Element errorNode) throws RemoteApiException {
		throw new RemoteApiException(getChildText(errorNode, "message"), getChildText(errorNode, "stacktrace"));
	}
}
