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
package com.atlassian.theplugin.commons.cfg;

import com.atlassian.theplugin.commons.cfg.xstream.JDomXStreamUtil;
import com.atlassian.theplugin.commons.exception.ThePluginException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.JDomReader;
import com.thoughtworks.xstream.io.xml.JDomWriter;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * User: pmaruszak
 */

public class PrivateConfigurationFactoryImpl implements  PrivateConfigurationFactory {
	private static final String ATLASSIAN_DIR_NAME = ".atlassian";
	private static final String ATLASSIAN_IDE_CONNECTOR_DIR_NAME = "ide-connector";
	private static final String ROOT_ELEMENT_NAME = "single-server-private-cfg";


	public void load(Element rootElement) {
		Element privateRoot = new Element("atlassian-ide-plugin-private");

	}
	public PrivateServerCfgInfo load(final ServerId id) throws ThePluginException, ServerCfgFactoryException {
		final File atlassianDir = new File(getPrivateCfgDirectoryLoadPath());

		if (atlassianDir.isDirectory() && atlassianDir.canRead()) {
			final File serverCfgFile = new File(atlassianDir.getAbsolutePath() + File.separator + id.getUuid());
			if (serverCfgFile.isFile() && serverCfgFile.canRead()) {
				Document doc = null;

				final SAXBuilder builder = new SAXBuilder(false);
				try {

					doc = builder.build(serverCfgFile.getAbsolutePath());
				} catch (JDOMException e) {
					throw new ServerCfgFactoryException("Cannot parse server cfg file " +  e.getMessage());
				} catch (IOException e) {
					throw new ServerCfgFactoryException("Cannot read sever cfg file " + e.getMessage());
				}

				PrivateServerCfgInfo privateServerCfgInfo = null;
				if (doc != null) {
					privateServerCfgInfo = loadJDom(doc.getRootElement(), PrivateServerCfgInfo.class);
				}
				return privateServerCfgInfo;
			}

		}
		
		throw new ThePluginException("Cannot read configuration stored in home directory. Loading from old location.");
	}

	public void save(final PrivateServerCfgInfo info) throws ThePluginException {
		Document document = new Document(new Element(ROOT_ELEMENT_NAME));
		saveJDom(info, document.getRootElement());
		

		try {
			//document.setRootElement(new Element("private-server-cfg"));
			writeXmlFile(document.getRootElement(),
					getPrivateCfgDirectorySavePath() + File.separator + info.getServerId().getUuid());
		} catch (IOException e) {
			throw new ThePluginException(e.getMessage());
		}

	}

	public void save(final Element element, String fileName) throws ThePluginException {
		Document document = new Document(new Element(ROOT_ELEMENT_NAME));
		document.addContent(element);
		try {
			writeXmlFile(document.getRootElement(),
					getPrivateCfgDirectorySavePath() + File.separator + fileName);
		} catch (IOException e) {
			throw new ThePluginException(e.getMessage());
		}

	}

	/*Target filr in  $HOME/.atlassian/ide-connector/atlassina-ide-connector*/
	private String getPrivateCfgDirectorySavePath() {

		final File ideConnectorHomeDir = new File(System.getProperty("user.home")
				+ File.separator + ATLASSIAN_DIR_NAME + File.separator + ATLASSIAN_IDE_CONNECTOR_DIR_NAME);

		ideConnectorHomeDir.mkdirs();

		if (ideConnectorHomeDir.isDirectory() && ideConnectorHomeDir.canWrite()) {
			return ideConnectorHomeDir.getAbsolutePath();
		}

		return null;
	}


	private void writeXmlFile(final Element element, final String filepath) throws IOException {
		if (filepath == null) {
			return; // handlig for instance default dummy project
		}
		final FileWriter writer = new FileWriter(filepath);
		new XMLOutputter(Format.getPrettyFormat()).output(element, writer);
		writer.close();
	}

	public void saveJDom(final Object object, final Element rootElement) {
		if (object == null) {
			throw new NullPointerException("Serialized object cannot be null");
		}
		final JDomWriter writer = new JDomWriter(rootElement);
		final XStream xStream = JDomXStreamUtil.getProjectJDomXStream();
		xStream.marshal(object, writer);


	}

	public String getPrivateCfgDirectoryLoadPath() {
		return System.getProperty("user.home") + File.separator + ATLASSIAN_DIR_NAME + File.separator +
						File.separator + ATLASSIAN_IDE_CONNECTOR_DIR_NAME;
	}


	<T> T loadJDom(final Element rootElement, Class<T> clazz) throws ServerCfgFactoryException {
		final int childCount = rootElement.getChildren().size();
		if (childCount != 1) {
			throw new ServerCfgFactoryException("Cannot travers JDom tree. Exactly one child node expected, but found ["
					+ childCount + "]");
		}
		final JDomReader reader = new JDomReader((Element) rootElement.getChildren().get(0));
		final XStream xStream = JDomXStreamUtil.getProjectJDomXStream();
		try {
			return clazz.cast(xStream.unmarshal(reader));
		} catch (ClassCastException e) {
			throw new ServerCfgFactoryException("Cannot load " + clazz.getSimpleName() + " due to ClassCastException: "
					+ e.getMessage(), e);
		} catch (Exception e) {
			throw new ServerCfgFactoryException("Cannot load " + clazz.getSimpleName() + ": "
					+ e.getMessage(), e);
		}
	}	
}
