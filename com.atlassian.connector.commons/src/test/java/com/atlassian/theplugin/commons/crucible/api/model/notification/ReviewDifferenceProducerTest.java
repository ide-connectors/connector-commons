package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import junit.framework.TestCase;

import java.util.*;

public class ReviewDifferenceProducerTest extends TestCase {

	final PermId reviewId1 = new PermId("CR-1");

	final PermId newItem1 = new PermId("CRF:11");

	final PermId newCommentId1 = new PermId("CMT:11");

	final PermId newVCommentId1 = new PermId("CMT:12");

	final PermId reviewId2 = new PermId("CR-2");

	final PermId newItem2 = new PermId("CRF:21");

	final PermId newCommentId2 = new PermId("CMT:21");

	final PermId newVCommentId2 = new PermId("CMT:22");

	final Reviewer reviewer3 = prepareReviewer("scott", "Scott", false);

	final Reviewer reviewer4 = prepareReviewer("alice", "Alice", false);

	private Review prepareReview() {
		return new Review("http://bogus", "TEST", reviewer3, reviewer4);
	}

	private Reviewer prepareReviewer(String userName, String displayName, boolean completed) {
		return new Reviewer(userName, displayName, completed);
	}

	private GeneralComment prepareGeneralComment(final String message, final PermId permId, final Date date,
			final GeneralComment reply) {
		GeneralCommentBean bean = new GeneralCommentBean();
		bean.setMessage(message);
		bean.setPermId(permId);
		bean.setCreateDate(date);
		if (reply != null) {
			bean.getReplies().add(reply);
		}

		return bean;
	}

	private VersionedComment prepareVersionedComment(final String message, final PermId permId, final Date date,
			final VersionedComment reply) {
		VersionedCommentBean bean = new VersionedCommentBean();
		bean.setMessage(message);
		bean.setPermId(permId);
		bean.setCreateDate(date);
		if (reply != null) {
			bean.getReplies().add(reply);
		}

		return bean;
	}

	private CrucibleFileInfo prepareCrucibleFileInfo(final PermId permId, final Date date,
			final List<VersionedComment> comments) {
		VersionedVirtualFile oldFileInfo = new VersionedVirtualFile("http://old.file", "1.1");
		VersionedVirtualFile newFileInfo = new VersionedVirtualFile("http://new.file", "1.3");
		CrucibleFileInfoImpl bean = new CrucibleFileInfoImpl(newFileInfo, oldFileInfo, permId);
		bean.setCommitDate(date);
		bean.setVersionedComments(comments);
		return bean;
	}

	private ReviewAdapter prepareReview1(State state, Date commentsDate) throws ValueNotYetInitialized {
		Review review1 = prepareReview();
		review1.setGeneralComments(new ArrayList<GeneralComment>());
		review1.setPermId(reviewId1);
		review1.setState(state);
		review1.setReviewers(MiscUtil.<Reviewer> buildHashSet(prepareReviewer("bob", "Bob", false), prepareReviewer(
				"alice", "Alice", false)));
		review1.getGeneralComments().add(prepareGeneralComment("message", newCommentId1, commentsDate, null));
		review1.getGeneralComments().add(prepareGeneralComment("message2", newCommentId2, commentsDate, null));
		List<VersionedComment> vComments = new ArrayList<VersionedComment>();
		vComments.add(prepareVersionedComment("versionedMessage", newVCommentId1, commentsDate, null));
		vComments.add(prepareVersionedComment("versionedMessage2", newVCommentId2, commentsDate, null));
		List<VersionedComment> vComments2 = new ArrayList<VersionedComment>();
		vComments2.add(prepareVersionedComment("versionedMessage", newVCommentId1, commentsDate, null));
		vComments2.add(prepareVersionedComment("versionedMessage2", newVCommentId2, commentsDate, null));
		CrucibleFileInfo file1 = prepareCrucibleFileInfo(newItem1, commentsDate, vComments);
		CrucibleFileInfo file2 = prepareCrucibleFileInfo(newItem2, commentsDate, vComments2);
		Set<CrucibleFileInfo> files1 = new HashSet<CrucibleFileInfo>();
		files1.add(file1);
		files1.add(file2);

		review1.setFilesAndVersionedComments(files1, null);

		return new ReviewAdapter(review1, new ServerData(
				new CrucibleServerCfg("Name", new ServerIdImpl()), new UserCfg("", "", false)));
	}

	public void testSameReviewsWithoutFiles() throws ValueNotYetInitialized {
		Date commentDate = new Date();
		ReviewAdapter review = prepareReview1(State.REVIEW, commentDate);
		review.getFiles().clear();

		ReviewAdapter review1 = prepareReview1(State.REVIEW, commentDate);

		// test the same review - empty files collection
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(0, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());

		// test the same review - no files and versioned comments
		review.setFilesAndVersionedComments(null, null);
		p = new ReviewDifferenceProducer(review, review);
		notifications = p.getDiff();

		assertEquals(0, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());

		// test the same content - one review null files, second empty collection
		review1.getFiles().clear();

		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();
		assertEquals(0, notifications.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
	}

	public void testSameReviews() throws ValueNotYetInitialized {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		ReviewAdapter review = prepareReview1(State.REVIEW, commentDate);

		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(0, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
	}

	public void testSameGeneralComments() throws ValueNotYetInitialized {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		ReviewAdapter review = prepareReview1(State.REVIEW, commentDate);

		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(0, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
	}

	public void testNullGeneralComments() throws ValueNotYetInitialized {
		Date commentDate = new Date();
		ReviewAdapter review = prepareReview1(State.REVIEW, commentDate);
		ReviewAdapter review1 = prepareReview1(State.REVIEW, commentDate);
		review.setGeneralComments(null);

		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(1, notifications.size());
		assertFalse(p.isShortEqual());
		assertTrue(p.isFilesEqual());

		review1.setGeneralComments(null);

		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();

		assertEquals(0, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
	}

	public void testAddedGeneralComment() throws ValueNotYetInitialized {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		ReviewAdapter review = prepareReview1(State.REVIEW, commentDate);
		ReviewAdapter review1 = prepareReview1(State.REVIEW, commentDate);

		// reset general for first review
		review.getGeneralComments().clear();
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(3, notifications.size());
		assertFalse(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.NEW_GENERAL_COMMENT, notifications.get(1).getType());
	}

	public void testReviewItemAdded() throws ValueNotYetInitialized {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		ReviewAdapter review = prepareReview1(State.REVIEW, commentDate);
		ReviewAdapter review1 = prepareReview1(State.REVIEW, commentDate);
		review.getFiles().clear();

		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(6, notifications.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.NEW_REVIEW_ITEM, notifications.get(0).getType());
		assertEquals(CrucibleNotificationType.NEW_REVIEW_ITEM, notifications.get(1).getType());
		assertEquals(CrucibleNotificationType.NEW_VERSIONED_COMMENT, notifications.get(2).getType());
		assertEquals(CrucibleNotificationType.NEW_VERSIONED_COMMENT, notifications.get(3).getType());
		assertEquals(CrucibleNotificationType.NEW_VERSIONED_COMMENT, notifications.get(4).getType());
		assertEquals(CrucibleNotificationType.NEW_VERSIONED_COMMENT, notifications.get(5).getType());
	}

	public void testReviewItemRemoved() throws ValueNotYetInitialized {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		ReviewAdapter review = prepareReview1(State.REVIEW, commentDate);
		ReviewAdapter review1 = prepareReview1(State.REVIEW, commentDate);
		review1.getFiles().clear();

		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(2, notifications.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.REMOVED_REVIEW_ITEM, notifications.get(0).getType());
		assertEquals(CrucibleNotificationType.REMOVED_REVIEW_ITEM, notifications.get(1).getType());
	}

	public void testAddedGeneralCommentReply() throws ValueNotYetInitialized {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		ReviewAdapter review = prepareReview1(State.REVIEW, commentDate);
		ReviewAdapter review1 = prepareReview1(State.REVIEW, commentDate);
		review1.getGeneralComments().get(0).getReplies().add(
				prepareGeneralComment("reply", new PermId("CMT:41"), new Date(), null));

		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(2, notifications.size());
		assertFalse(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.NEW_REPLY, notifications.get(1).getType());
	}

	public void testUpdatedGeneralCommentReply() throws ValueNotYetInitialized {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		ReviewAdapter review = prepareReview1(State.REVIEW, commentDate);
		ReviewAdapter review1 = prepareReview1(State.REVIEW, commentDate);
		Date replyDate = new Date();
		review.getGeneralComments().get(0).getReplies().add(
				prepareGeneralComment("reply", new PermId("CMT:41"), replyDate, null));
		review.getGeneralComments().get(0).getReplies().add(
				prepareGeneralComment("reply2", new PermId("CMT:42"), replyDate, null));
		review1.getGeneralComments().get(0).getReplies().add(
				prepareGeneralComment("reply", new PermId("CMT:41"), replyDate, null));
		review1.getGeneralComments().get(0).getReplies().add(
				prepareGeneralComment("reply2", new PermId("CMT:42"), replyDate, null));

		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(0, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());

		((GeneralCommentBean) review1.getGeneralComments().get(0).getReplies().get(0)).setMessage("new reply message");

		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();

		assertEquals(2, notifications.size());
		assertFalse(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.UPDATED_REPLY, notifications.get(1).getType());
	}

	public void testRemovedGeneralCommentReply() throws ValueNotYetInitialized {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		ReviewAdapter review = prepareReview1(State.REVIEW, commentDate);
		ReviewAdapter review1 = prepareReview1(State.REVIEW, commentDate);
		review.getGeneralComments().get(0).getReplies().add(
				prepareGeneralComment("reply", new PermId("CMT:41"), new Date(), null));

		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(2, notifications.size());
		assertFalse(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.REMOVED_REPLY, notifications.get(1).getType());
	}

	public void testEditedGeneralComment() throws ValueNotYetInitialized {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		ReviewAdapter review = prepareReview1(State.REVIEW, commentDate);
		ReviewAdapter review1 = prepareReview1(State.REVIEW, commentDate);

		// change general for second review
		((GeneralCommentBean) review1.getGeneralComments().get(0)).setMessage("new message");
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(2, notifications.size());
		assertFalse(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.UPDATED_GENERAL_COMMENT, notifications.get(1).getType());
	}

	public void testRemovedGeneralComment() throws ValueNotYetInitialized {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		ReviewAdapter review = prepareReview1(State.REVIEW, commentDate);
		ReviewAdapter review1 = prepareReview1(State.REVIEW, commentDate);

		// reset general for second review
		review1.getGeneralComments().clear();
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(3, notifications.size());
		assertFalse(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.REMOVED_GENERAL_COMMENT, notifications.get(1).getType());
	}

	public void testAddedVersionedComment() throws ValueNotYetInitialized {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		ReviewAdapter review = prepareReview1(State.REVIEW, commentDate);
		ReviewAdapter review1 = prepareReview1(State.REVIEW, commentDate);

		// reset versioned for first file review
		Iterator<CrucibleFileInfo> iter = review.getFiles().iterator();
		iter.next().getVersionedComments().clear();
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(2, notifications.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.NEW_VERSIONED_COMMENT, notifications.get(0).getType());
		assertEquals(CrucibleNotificationType.NEW_VERSIONED_COMMENT, notifications.get(1).getType());

		iter.next().getVersionedComments().clear();
		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();

		assertEquals(4, notifications.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.NEW_VERSIONED_COMMENT, notifications.get(0).getType());
		assertEquals(CrucibleNotificationType.NEW_VERSIONED_COMMENT, notifications.get(1).getType());
		assertEquals(CrucibleNotificationType.NEW_VERSIONED_COMMENT, notifications.get(2).getType());
		assertEquals(CrucibleNotificationType.NEW_VERSIONED_COMMENT, notifications.get(3).getType());
	}

	public void testEditedVersionedComment() throws ValueNotYetInitialized {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		ReviewAdapter review = prepareReview1(State.REVIEW, commentDate);
		ReviewAdapter review1 = prepareReview1(State.REVIEW, commentDate);

		Iterator<CrucibleFileInfo> iter = review.getFiles().iterator();

		((VersionedCommentBean) iter.next().getVersionedComments().get(0)).setMessage("new message");
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(1, notifications.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.UPDATED_VERSIONED_COMMENT, notifications.get(0).getType());

		((VersionedCommentBean) iter.next().getVersionedComments().get(0)).setMessage("new message");
		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();

		assertEquals(2, notifications.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.UPDATED_VERSIONED_COMMENT, notifications.get(0).getType());
		assertEquals(CrucibleNotificationType.UPDATED_VERSIONED_COMMENT, notifications.get(1).getType());
	}

	public void testRemovedVersionedComment() throws ValueNotYetInitialized {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		ReviewAdapter review = prepareReview1(State.REVIEW, commentDate);
		ReviewAdapter review1 = prepareReview1(State.REVIEW, commentDate);

		// reset versioned for first file review
		Iterator<CrucibleFileInfo> iter = review1.getFiles().iterator();
		iter.next().getVersionedComments().clear();
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(2, notifications.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.REMOVED_VERSIONED_COMMENT, notifications.get(0).getType());
		assertEquals(CrucibleNotificationType.REMOVED_VERSIONED_COMMENT, notifications.get(1).getType());

		iter.next().getVersionedComments().clear();
		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();

		assertEquals(4, notifications.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.REMOVED_VERSIONED_COMMENT, notifications.get(0).getType());
		assertEquals(CrucibleNotificationType.REMOVED_VERSIONED_COMMENT, notifications.get(1).getType());
		assertEquals(CrucibleNotificationType.REMOVED_VERSIONED_COMMENT, notifications.get(2).getType());
		assertEquals(CrucibleNotificationType.REMOVED_VERSIONED_COMMENT, notifications.get(3).getType());
	}

	public void testAddedVersionedCommentReply() throws ValueNotYetInitialized {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		ReviewAdapter review = prepareReview1(State.REVIEW, commentDate);
		ReviewAdapter review1 = prepareReview1(State.REVIEW, commentDate);

		Iterator<CrucibleFileInfo> iter = review1.getFiles().iterator();
		iter.next().getVersionedComments().get(0).getReplies().add(
				prepareVersionedComment("reply", new PermId("CMT:41"), commentDate, null));
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(1, notifications.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.NEW_REPLY, notifications.get(0).getType());
	}

	public void testUpdatedVersionedCommentReply() throws ValueNotYetInitialized {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		ReviewAdapter review = prepareReview1(State.REVIEW, commentDate);
		ReviewAdapter review1 = prepareReview1(State.REVIEW, commentDate);

		Iterator<CrucibleFileInfo> iter = review.getFiles().iterator();
		iter.next().getVersionedComments().get(0).getReplies().add(
				prepareVersionedComment("reply", new PermId("CMT:41"), commentDate, null));

		Iterator<CrucibleFileInfo> iter1 = review1.getFiles().iterator();
		iter1.next().getVersionedComments().get(0).getReplies().add(
				prepareVersionedComment("updated reply", new PermId("CMT:41"), commentDate, null));
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(1, notifications.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.UPDATED_REPLY, notifications.get(0).getType());
	}

	public void testRemovedVersionedCommentReply() throws ValueNotYetInitialized {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		ReviewAdapter review = prepareReview1(State.REVIEW, commentDate);
		ReviewAdapter review1 = prepareReview1(State.REVIEW, commentDate);

		Iterator<CrucibleFileInfo> iter = review.getFiles().iterator();
		iter.next().getVersionedComments().get(0).getReplies().add(
				prepareVersionedComment("reply", new PermId("CMT:41"), commentDate, null));
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(1, notifications.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.REMOVED_REPLY, notifications.get(0).getType());
	}

	public void testStateChanges() throws ValueNotYetInitialized {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		ReviewAdapter review = prepareReview1(State.DRAFT, commentDate);
		ReviewAdapter review1 = prepareReview1(State.REVIEW, commentDate);
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();
		assertEquals(2, notifications.size());
		assertFalse(p.isShortEqual());
		assertTrue(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.REVIEW_STATE_CHANGED, notifications.get(0).getType());

		review = prepareReview1(State.REVIEW, commentDate);
		review1 = prepareReview1(State.REVIEW, commentDate);
		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();
		assertEquals(0, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());

		review1 = prepareReview1(State.CLOSED, commentDate);
		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();
		assertEquals(2, notifications.size());
		assertFalse(p.isShortEqual());
		assertTrue(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.REVIEW_STATE_CHANGED, notifications.get(0).getType());
	}

	public void testReviewersChanges() throws ValueNotYetInitialized {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		ReviewAdapter review = prepareReview1(State.REVIEW, commentDate);
		ReviewAdapter review1 = prepareReview1(State.REVIEW, commentDate);

		// just add reviewer - no notification ???
		review1.getReviewers().add(reviewer3);
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();
		assertEquals(1, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());

		// just remove reviewer - no notification ???
		review1.getReviewers().remove(reviewer3);
		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();
		assertEquals(0, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());

		// complete reviewer1
		Iterator<Reviewer> iter = review1.getReviewers().iterator();
		Reviewer reviewer = iter.next();
		review1.getReviewers().remove(reviewer);
		review1.getReviewers().add(new Reviewer(reviewer.getUserName(), reviewer.getDisplayName(), true));

		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();
		assertEquals(1, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.REVIEWER_COMPLETED, notifications.get(0).getType());

		// complete all reviewers
		Reviewer[] reviewers = review1.getReviewers().toArray(new Reviewer[review1.getReviewers().size()]);
		review1.getReviewers().clear();
		for (Reviewer r : reviewers) {
			review1.getReviewers().add(new Reviewer(r.getUserName(), r.getDisplayName(), true));
		}

		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();
		assertEquals(3, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.REVIEWER_COMPLETED, notifications.get(0).getType());
		assertEquals(CrucibleNotificationType.REVIEWER_COMPLETED, notifications.get(1).getType());
		assertEquals(CrucibleNotificationType.REVIEW_COMPLETED, notifications.get(2).getType());

		review.getReviewers().clear();
		review1.getReviewers().clear();
		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();
		assertEquals(0, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
	}

	private interface MyCallback {
		void handle(Review r1, Review r2, String s1, String s2);
	}

	private final String[][] stringPairs = { { "abc", "bcde" }, { "", "abc" }, { "abc", "" }, { "abc", "abc" },
			{ "", "" }, };

	private void testHelper(final CrucibleNotificationType notificationType, MyCallback myCallback) {
		final Review r1 = prepareReview();
		final Review r2 = prepareReview();
		final ReviewDifferenceProducer p = new ReviewDifferenceProducer(new ReviewAdapter(r1, null), new ReviewAdapter(
				r2, null));

		for (String[] stringPair : stringPairs) {
			final String s1 = stringPair[0];
			final String s2 = stringPair[1];
			myCallback.handle(r1, r2, s1, s2);
			final List<CrucibleNotification> diff = p.getDiff();
			final String msg = "Checking " + s1 + " vs " + s2;
			if (MiscUtil.isEqual(s1, s2)) {
				assertEquals(msg, 0, diff.size());
			} else {
				assertEquals(msg, 1, diff.size());
				assertEquals(msg, notificationType, diff.get(0).getType());
			}
		}
	}

	public void testStatementOfObjectivesChanged() {
		testHelper(CrucibleNotificationType.STATEMENT_OF_OBJECTIVES_CHANGED, new MyCallback() {
			public void handle(final Review r1, final Review r2, final String s1, final String s2) {
				r1.setDescription(s1);
				r2.setDescription(s2);
			}
		});

	}

	public void testNameChanged() {
		testHelper(CrucibleNotificationType.NAME_CHANGED, new MyCallback() {
			public void handle(final Review r1, final Review r2, final String s1, final String s2) {
				r1.setName(s1);
				r2.setName(s2);
			}
		});
	}

	public void testModeratorChanged() {
		testHelper(CrucibleNotificationType.MODERATOR_CHANGED, new MyCallback() {
			public void handle(final Review r1, final Review r2, final String s1, final String s2) {
				r1.setModerator(new User(s1));
				r2.setModerator(new User(s2));
			}
		});
	}

	public void testAuthorChanged() {
		testHelper(CrucibleNotificationType.AUTHOR_CHANGED, new MyCallback() {
			public void handle(final Review r1, final Review r2, final String s1, final String s2) {
				r1.setAuthor(new User(s1));
				r2.setAuthor(new User(s2));
			}
		});
	}

	public void testSummmaryChanged() {
		testHelper(CrucibleNotificationType.SUMMARY_CHANGED, new MyCallback() {
			public void handle(final Review r1, final Review r2, final String s1, final String s2) {
				r1.setSummary(s1);
				r2.setSummary(s2);
			}
		});
	}

	public void testProjectChanged() {
		testHelper(CrucibleNotificationType.PROJECT_CHANGED, new MyCallback() {
			public void handle(final Review r1, final Review r2, final String s1, final String s2) {
				r1.setProjectKey(s1);
				r2.setProjectKey(s2);
			}
		});
	}

	private static class Pair<T, E> {
		private final T first;

		private final E second;

		public Pair(final T first, final E second) {
			this.first = first;
			this.second = second;
		}
	}

	@SuppressWarnings("unchecked")
	public void testReviewersChanged() {
		final Review r1 = prepareReview();
		final Review r2 = prepareReview();
		final Reviewer rv1 = new Reviewer("user1", true);
		final Reviewer rv2 = new Reviewer("user2", true);
		final Reviewer rv3 = new Reviewer("user3", true);

		Collection<Pair<Set<Reviewer>, Set<Reviewer>>> reviewers = MiscUtil.buildArrayList(
				new Pair<Set<Reviewer>, Set<Reviewer>>(MiscUtil.buildHashSet(rv1), MiscUtil.<Reviewer> buildHashSet()),
				new Pair<Set<Reviewer>, Set<Reviewer>>(MiscUtil.buildHashSet(rv1), MiscUtil.buildHashSet(rv1)),
				new Pair<Set<Reviewer>, Set<Reviewer>>(MiscUtil.buildHashSet(rv1), MiscUtil.buildHashSet(rv2)),
				new Pair<Set<Reviewer>, Set<Reviewer>>(MiscUtil.buildHashSet(rv1, rv2), MiscUtil.buildHashSet(rv1)),
				new Pair<Set<Reviewer>, Set<Reviewer>>(MiscUtil.buildHashSet(rv1, rv2), MiscUtil.buildHashSet(rv2, rv3)),
				new Pair<Set<Reviewer>, Set<Reviewer>>(MiscUtil.buildHashSet(rv1, rv2, rv3), MiscUtil.buildHashSet(rv2,
						rv3, rv1)));

		final ReviewDifferenceProducer p = new ReviewDifferenceProducer(new ReviewAdapter(r1, null), new ReviewAdapter(
				r2, null));
		for (Pair<Set<Reviewer>, Set<Reviewer>> reviewersPair : reviewers) {
			r1.setReviewers(reviewersPair.first);
			r2.setReviewers(reviewersPair.second);
			final List<CrucibleNotification> diff = p.getDiff();
			final String msg = "Checking " + reviewersPair.first + " vs " + reviewersPair.second;
			if (MiscUtil.isEqual(reviewersPair.first, reviewersPair.second)) {
				assertEquals(msg, 0, diff.size());
			} else {
				assertEquals(msg, 1, diff.size());
				assertEquals(msg, CrucibleNotificationType.REVIEWERS_CHANGED, diff.get(0).getType());
			}
		}
	}

	/**
	 * Both reviews are the same, except on has file with different revisions
	 * @throws ValueNotYetInitialized
 	 */
	public void testRevisionChanged() throws ValueNotYetInitialized {
		final Date dt = new Date();
		final ReviewAdapter r1 = prepareReview1(State.REVIEW, dt);
		final ReviewAdapter r2 = prepareReview1(State.REVIEW, dt);
		for (CrucibleFileInfo file : r1.getFiles()) {
			file.getOldFileDescriptor().setRevision(
					file.getOldFileDescriptor().getRevision() + ".23");
		}

		final ReviewDifferenceProducer p = new ReviewDifferenceProducer(r1, r2);
		final List<CrucibleNotification> diff = p.getDiff();
		assertNotNull(diff);
		assertEquals(4, diff.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.NEW_REVIEW_ITEM, diff.get(0).getType());
		assertEquals(CrucibleNotificationType.NEW_REVIEW_ITEM, diff.get(1).getType());
		assertEquals(CrucibleNotificationType.REMOVED_REVIEW_ITEM, diff.get(2).getType());
		assertEquals(CrucibleNotificationType.REMOVED_REVIEW_ITEM, diff.get(3).getType());
	}
}
