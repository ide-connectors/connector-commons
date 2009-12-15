package com.atlassian.theplugin.commons.crucible.api.rest;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;
import com.atlassian.theplugin.crucible.api.rest.cruciblemock.CrucibleMockUtil;
import junit.framework.TestCase;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import java.io.IOException;
import java.util.List;

/**
 * @author pmaruszak
 * @date Sep 17, 2009
 */
public class CrucibleRestXmlHelperTest extends TestCase {
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
    }
}
