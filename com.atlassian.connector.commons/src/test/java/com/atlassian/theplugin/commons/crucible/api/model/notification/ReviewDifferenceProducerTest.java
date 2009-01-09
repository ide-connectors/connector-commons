package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import junit.framework.TestCase;

import java.util.*;

public class ReviewDifferenceProducerTest extends TestCase {
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	PermIdBean reviewId1 = new PermIdBean("CR-1");
	PermIdBean newItem1 = new PermIdBean("CRF:11");
	PermIdBean newCommentId1 = new PermIdBean("CMT:11");
	PermIdBean newVCommentId1 = new PermIdBean("CMT:12");

	PermIdBean reviewId2 = new PermIdBean("CR-2");
	PermIdBean newItem2 = new PermIdBean("CRF:21");
	PermIdBean newCommentId2 = new PermIdBean("CMT:21");
	PermIdBean newVCommentId2 = new PermIdBean("CMT:22");

	Reviewer reviewer3 = prepareReviewer("scott", "Scott", false);
	Reviewer reviewer4 = prepareReviewer("alice", "Alice", false);

	private ReviewBean prepareReview() {
		return new ReviewBean("http://bogus");
	}

	private ReviewerBean prepareReviewer(String userName, String displayName, boolean completed) {
		ReviewerBean reviewer = new ReviewerBean();
		reviewer.setUserName(userName);
		reviewer.setDisplayName(displayName);
		reviewer.setCompleted(completed);

		return reviewer;
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
		CrucibleFileInfoImpl bean = new CrucibleFileInfoImpl(null, null, permId);
		bean.setCommitDate(date);
		bean.setVersionedComments(comments);
		return bean;
	}

	private ReviewAdapter prepareReview1(State state, Date commentsDate) throws ValueNotYetInitialized {
		ReviewBean review1 = prepareReview();
		review1.setGeneralComments(new ArrayList<GeneralComment>());
		review1.setPermId(reviewId1);
		review1.setState(state);
		review1.setReviewers(
				new HashSet(Arrays.asList(prepareReviewer("bob", "Bob", false), prepareReviewer("alice", "Alice", false))));
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

		return new ReviewAdapter(review1, null);
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

		assertEquals(0, notifications.size());
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

		assertEquals(2, notifications.size());
		assertFalse(p.isShortEqual());
		assertTrue(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.NEW_GENERAL_COMMENT, notifications.get(0).getType());
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
		review1.getGeneralComments().get(0).getReplies()
				.add(prepareGeneralComment("reply", new PermIdBean("CMT:41"), new Date(), null));

		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(1, notifications.size());
		assertFalse(p.isShortEqual());
		assertTrue(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.NEW_REPLY, notifications.get(0).getType());
	}

	public void testUpdatedGeneralCommentReply() throws ValueNotYetInitialized {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		ReviewAdapter review = prepareReview1(State.REVIEW, commentDate);
		ReviewAdapter review1 = prepareReview1(State.REVIEW, commentDate);
		Date replyDate = new Date();
		review.getGeneralComments().get(0).getReplies()
				.add(prepareGeneralComment("reply", new PermIdBean("CMT:41"), replyDate, null));
		review.getGeneralComments().get(0).getReplies()
				.add(prepareGeneralComment("reply2", new PermIdBean("CMT:42"), replyDate, null));
		review1.getGeneralComments().get(0).getReplies()
				.add(prepareGeneralComment("reply", new PermIdBean("CMT:41"), replyDate, null));
		review1.getGeneralComments().get(0).getReplies()
				.add(prepareGeneralComment("reply2", new PermIdBean("CMT:42"), replyDate, null));

		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(0, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());

		((GeneralCommentBean) review1.getGeneralComments().get(0).getReplies().get(0)).setMessage("new reply message");

		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();

		assertEquals(1, notifications.size());
		assertFalse(p.isShortEqual());
		assertTrue(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.UPDATED_REPLY, notifications.get(0).getType());
	}

	public void testRemovedGeneralCommentReply() throws ValueNotYetInitialized {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		ReviewAdapter review = prepareReview1(State.REVIEW, commentDate);
		ReviewAdapter review1 = prepareReview1(State.REVIEW, commentDate);
		review.getGeneralComments().get(0).getReplies()
				.add(prepareGeneralComment("reply", new PermIdBean("CMT:41"), new Date(), null));

		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(1, notifications.size());
		assertFalse(p.isShortEqual());
		assertTrue(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.REMOVED_REPLY, notifications.get(0).getType());
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

		assertEquals(1, notifications.size());
		assertFalse(p.isShortEqual());
		assertTrue(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.UPDATED_GENERAL_COMMENT, notifications.get(0).getType());
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

		assertEquals(2, notifications.size());
		assertFalse(p.isShortEqual());
		assertTrue(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.REMOVED_GENERAL_COMMENT, notifications.get(0).getType());
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
		assertTrue(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.NEW_VERSIONED_COMMENT, notifications.get(0).getType());
		assertEquals(CrucibleNotificationType.NEW_VERSIONED_COMMENT, notifications.get(1).getType());

		iter.next().getVersionedComments().clear();
		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();

		assertEquals(4, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
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
		assertTrue(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.UPDATED_VERSIONED_COMMENT, notifications.get(0).getType());

		((VersionedCommentBean) iter.next().getVersionedComments().get(0)).setMessage("new message");
		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();

		assertEquals(2, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
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
		assertTrue(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.REMOVED_VERSIONED_COMMENT, notifications.get(0).getType());
		assertEquals(CrucibleNotificationType.REMOVED_VERSIONED_COMMENT, notifications.get(1).getType());

		iter.next().getVersionedComments().clear();
		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();

		assertEquals(4, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
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
		iter.next().getVersionedComments().get(0).getReplies()
				.add(prepareVersionedComment("reply", new PermIdBean("CMT:41"), commentDate, null));
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(1, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.NEW_REPLY, notifications.get(0).getType());
	}

	public void testUpdatedVersionedCommentReply() throws ValueNotYetInitialized {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		ReviewAdapter review = prepareReview1(State.REVIEW, commentDate);
		ReviewAdapter review1 = prepareReview1(State.REVIEW, commentDate);

		Iterator<CrucibleFileInfo> iter = review.getFiles().iterator();
		iter.next().getVersionedComments().get(0).getReplies()
				.add(prepareVersionedComment("reply", new PermIdBean("CMT:41"), commentDate, null));

		Iterator<CrucibleFileInfo> iter1 = review1.getFiles().iterator();
		iter1.next().getVersionedComments().get(0).getReplies()
				.add(prepareVersionedComment("updated reply", new PermIdBean("CMT:41"), commentDate, null));
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(1, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.UPDATED_REPLY, notifications.get(0).getType());
	}

	public void testRemovedVersionedCommentReply() throws ValueNotYetInitialized {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		ReviewAdapter review = prepareReview1(State.REVIEW, commentDate);
		ReviewAdapter review1 = prepareReview1(State.REVIEW, commentDate);

		Iterator<CrucibleFileInfo> iter = review.getFiles().iterator();
		iter.next().getVersionedComments().get(0).getReplies()
				.add(prepareVersionedComment("reply", new PermIdBean("CMT:41"), commentDate, null));
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(1, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.REMOVED_REPLY, notifications.get(0).getType());
	}

	public void testStateChanges() throws ValueNotYetInitialized {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		ReviewAdapter review = prepareReview1(State.DRAFT, commentDate);
		ReviewAdapter review1 = prepareReview1(State.REVIEW, commentDate);
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();
		assertEquals(1, notifications.size());
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
		assertEquals(1, notifications.size());
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
		assertEquals(0, notifications.size());
		assertFalse(p.isShortEqual());
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
		((ReviewerBean) iter.next()).setCompleted(true);
		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();
		assertEquals(1, notifications.size());
		assertFalse(p.isShortEqual());
		assertTrue(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.REVIEWER_COMPLETED, notifications.get(0).getType());

		((ReviewerBean) iter.next()).setCompleted(true);
		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();
		assertEquals(3, notifications.size());
		assertFalse(p.isShortEqual());
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
}
