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
package com.atlassian.theplugin.commons.cfg.xstream;

import com.atlassian.theplugin.commons.cfg.PrivateConfigurationDao;
import com.atlassian.theplugin.commons.cfg.PrivateServerCfgInfo;
import com.atlassian.theplugin.commons.cfg.ServerCfgFactoryException;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.JDomReader;
import com.thoughtworks.xstream.io.xml.JDomWriter;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * User: pmaruszak
 */

public class HomeDirPrivateConfigurationDao implements PrivateConfigurationDao {
	private static final String ATLASSIAN_DIR_NAME = ".atlassian";
	private static final String ATLASSIAN_IDE_CONNECTOR_DIR_NAME = "ide-connector";
	private static final String ROOT_ELEMENT_NAME = "single-server-private-cfg";
	private static final String USE_DEFAULT_CREDENTIALS_NAME = "use-default-credentials";


	public PrivateServerCfgInfo load(final ServerId id) throws ServerCfgFactoryException {
		final File atlassianDir = getPrivateCfgDirectorySavePath();

		if (atlassianDir.isDirectory() && atlassianDir.canRead()) {
			final File serverCfgFile = new File(atlassianDir.getAbsolutePath(), id.getUuid().toString());
			if (serverCfgFile.isFile() && serverCfgFile.canRead()) {
				Document doc;

				final SAXBuilder builder = new SAXBuilder(false);
				try {

					doc = builder.build(serverCfgFile.getAbsolutePath());
				} catch (JDOMException e) {
					throw new ServerCfgFactoryException("Cannot parse server cfg file " + e.getMessage());
				} catch (IOException e) {
					throw new ServerCfgFactoryException("Cannot read sever cfg file " + e.getMessage());
				}

				PrivateServerCfgInfo privateServerCfgInfo = null;
				if (doc != null) {
					privateServerCfgInfo = load(doc);
				}
				return privateServerCfgInfo;
			} else {
				return null;
			}

		} else {
			throw new ServerCfgFactoryException("Cannot read private configuration stored in directory ["
					+ atlassianDir.getAbsolutePath() + "]. Directory does not exist or is not accessible");
		}

	}

	static PrivateServerCfgInfo load(final Document doc) throws ServerCfgFactoryException {
		return loadJDom(doc.getRootElement(), PrivateServerCfgInfo.class);
	}

	public void save(@NotNull final PrivateServerCfgInfo info) throws ServerCfgFactoryException {
		Document document = createJDom(info);

		try {
			//document.setRootElement(new Element("private-server-cfg"));
			writeXmlFile(document.getRootElement(), new File(getPrivateCfgDirectorySavePath(),
					info.getServerId().getUuid().toString()));
		} catch (IOException e) {
			final ServerCfgFactoryException ex = new ServerCfgFactoryException(e.getMessage());
			ex.initCause(e);
			throw ex;
		}

	}

	static Document createJDom(final PrivateServerCfgInfo info) {
		Document document = new Document(new Element(ROOT_ELEMENT_NAME));
		saveJDom(info, document.getRootElement());
		return document;
	}


	/*Target filr in  $HOME/.atlassian/ide-connector/atlassina-ide-connector*/
	private File getPrivateCfgDirectorySavePath() throws ServerCfgFactoryException {

		final File ideConnectorHomeDir = new File(getPrivateCfgDirectoryPath());
		if (ideConnectorHomeDir.exists() == false) {
			if (ideConnectorHomeDir.mkdirs() == false) {
				throw new ServerCfgFactoryException("Cannot create directory [" + ideConnectorHomeDir.getAbsolutePath() + "]");
			}
		}


		if (ideConnectorHomeDir.isDirectory() && ideConnectorHomeDir.canWrite()) {
			return ideConnectorHomeDir;
		}
		throw new ServerCfgFactoryException("[" + ideConnectorHomeDir.getAbsolutePath() + "] is not writable"
				+ " or is not a directory");
	}


	private String getPrivateCfgDirectoryPath() {
		return System.getProperty("user.home") + File.separator + ATLASSIAN_DIR_NAME
				+ File.separator + ATLASSIAN_IDE_CONNECTOR_DIR_NAME;
	}


	private void writeXmlFile(final Element element, @NotNull final File outputFile) throws IOException {
		final FileWriter writer = new FileWriter(outputFile);
		try {
			new XMLOutputter(Format.getPrettyFormat()).output(element, writer);
		} finally {
			writer.close();
		}
	}

	static void saveJDom(final Object object, final Element rootElement) {
		if (object == null) {
			throw new NullPointerException("Serialized object cannot be null");
		}
		final JDomWriter writer = new JDomWriter(rootElement);
		final XStream xStream = JDomXStreamUtil.getProjectJDomXStream();
		xStream.marshal(object, writer);


	}

	private static <T> T loadJDom(final Element rootElement, Class<T> clazz) throws ServerCfgFactoryException {
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
