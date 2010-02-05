package com.atlassian.theplugin.commons.crucible.api.rest;

import com.atlassian.connector.commons.misc.IntRange;
import com.atlassian.connector.commons.misc.IntRanges;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.crucible.api.rest.cruciblemock.CrucibleMockUtil;
import com.spartez.util.junit3.IAction;
import com.spartez.util.junit3.TestUtil;
import junit.framework.TestCase;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;

/**
 * @author pmaruszak
 * @date Sep 17, 2009
 */
public class CrucibleRestXmlHelperTest extends TestCase {

    public void testParsePermId() throws JDOMException, IOException {
        final XPath xpath = XPath.newInstance("reviewItem");
        final SAXBuilder builder = new SAXBuilder();

        Document doc = builder.build(new CrucibleMockUtil().getResource("reviewItemNode_220_M3.xml"));
        List<Element> elements = xpath.selectNodes(doc);
        CrucibleFileInfo fileInfo = null;

        try {
            fileInfo = CrucibleRestXmlHelper.parseReviewItemNode((elements.get(0)));
            assertEquals("CFR-32137", fileInfo.getPermId().getId());
        } catch (ParseException e) {
            fail();
        }

        doc = builder.build(new CrucibleMockUtil().getResource("reviewItemNode_below_220.xml"));
        elements = xpath.selectNodes(doc);
        try {
            fileInfo = CrucibleRestXmlHelper.parseReviewItemNode((elements.get(0)));
            assertEquals("CFR-32137", fileInfo.getPermId().getId());
        } catch (ParseException e) {
            fail();
        }

        TestUtil.assertThrows(ParseException.class, new IAction() {
            public void run() throws Throwable {
                Document doc = builder.build(new CrucibleMockUtil().getResource("reviewItemNode_nopermId.xml"));
                List<Element> elements = xpath.selectNodes(doc);
                CrucibleRestXmlHelper.parseReviewItemNode((elements.get(0)));
            }
        });

        TestUtil.assertThrows(ParseException.class, new IAction() {
            public void run() throws Throwable {
                Document doc = builder.build(new CrucibleMockUtil().getResource("reviewItemNode_empty_permId.xml"));
                List<Element> elements = xpath.selectNodes(doc);
                CrucibleRestXmlHelper.parseReviewItemNode((elements.get(0)));
            }
        });

    }

    public void testParseProjectNode() throws JDOMException, IOException {
        XPath xpath = XPath.newInstance("projectData");
        final SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new CrucibleMockUtil().getResource("projectDataCrucible1_6.xml"));

        @SuppressWarnings("unchecked")
        List<Element> elements = xpath.selectNodes(doc);
        CrucibleProject cp = CrucibleRestXmlHelper.parseProjectNode(elements.get(0));

        assertEquals(cp.getAllowedReviewers(), null);


        doc = builder.build(new CrucibleMockUtil().getResource("projectDataCrucible2_0.xml"));
        elements = xpath.selectNodes(doc);
        cp = CrucibleRestXmlHelper.parseProjectNode(elements.get(0));

        assertTrue(cp != null);
        assertEquals(5, cp.getAllowedReviewers().size());


        doc = builder.build(new CrucibleMockUtil().getResource("reviewDetailsResponse-testLineRanges.xml"));
        xpath = XPath.newInstance("detailedReviewData");
        elements = xpath.selectNodes(doc);

        Review review = null;
        try {
            review = CrucibleRestXmlHelper.parseFullReview("http://localhost", "pstefaniak", elements.get(0), false);
        } catch (ParseException e) {
            fail(); // check "reviewDetailsResponse-testLineRanges.xml" - should be valid .xml...
        }
        assertTrue(review != null);

        Iterator<CrucibleFileInfo> it = review.getFiles().iterator();
        List<VersionedComment> versionedComments = it.next().getVersionedComments();

        VersionedComment comment = versionedComments.get(0);
        assertEquals(comment.getMessage(), "this is yellow coment");
        assertEquals(comment.getFromStartLine(), 0);
        assertEquals(comment.getFromEndLine(), 0);
        assertEquals(comment.getToStartLine(), 0);
        assertEquals(comment.getToEndLine(), 0);
        assertNull(comment.getToLineRanges());
        assertNull(comment.getFromLineRanges());
        assertNull(comment.getLineRanges());

        comment = versionedComments.get(1);
        assertEquals(comment.getMessage(), "this is green comment");
        assertEquals(comment.getFromStartLine(), 0);
        assertEquals(comment.getFromEndLine(), 0);
        assertEquals(comment.getToStartLine(), 52);
        assertEquals(comment.getToEndLine(), 52);
        assertEquals(comment.getToLineRanges(), new IntRanges(new IntRange(52)));
        assertNull(comment.getFromLineRanges());
        assertEquals(comment.getLineRanges().get("51224"), new IntRanges(new IntRange(52)));

        comment = versionedComments.get(2);
        assertEquals(comment.getMessage(), "this is red coment");
        assertEquals(comment.getFromStartLine(), 0);
        assertEquals(comment.getFromEndLine(), 0);
        assertEquals(comment.getToStartLine(), 51);
        assertEquals(comment.getToEndLine(), 51);
        assertEquals(comment.getToLineRanges(), new IntRanges(new IntRange(51)));
        assertNull(comment.getFromLineRanges());
        assertEquals(comment.getLineRanges().get("38347"), new IntRanges(new IntRange(51)));

        comment = versionedComments.get(3);
        assertEquals(comment.getMessage(), "this is blue comment (lines 49, 52, 53)");
        assertEquals(comment.getFromStartLine(), 49);
        assertEquals(comment.getFromEndLine(), 53);
        assertEquals(comment.getToStartLine(), 49);
        assertEquals(comment.getToEndLine(), 54);
        assertEquals(comment.getToLineRanges(), new IntRanges(new IntRange(49), new IntRange(53, 54)));
        assertEquals(comment.getFromLineRanges(), new IntRanges(new IntRange(49), new IntRange(52, 53)));
        assertEquals(comment.getLineRanges().get("38347"), new IntRanges(new IntRange(49), new IntRange(52, 53)));
        assertEquals(comment.getLineRanges().get("51224"), new IntRanges(new IntRange(49), new IntRange(53, 54)));


    }
}
