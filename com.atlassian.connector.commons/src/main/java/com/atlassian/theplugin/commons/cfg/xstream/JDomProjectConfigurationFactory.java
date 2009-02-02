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

import com.atlassian.theplugin.commons.cfg.PrivateConfigurationFactory;
import com.atlassian.theplugin.commons.cfg.PrivateProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.PrivateServerCfgInfo;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ProjectConfigurationFactory;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfgFactoryException;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.JDomReader;
import com.thoughtworks.xstream.io.xml.JDomWriter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JDomProjectConfigurationFactory implements ProjectConfigurationFactory {

	private final Element publicElement;
	private final PrivateConfigurationFactory privateConfigurationFactory;


	public JDomProjectConfigurationFactory(final Element element,
			@NotNull PrivateConfigurationFactory privateConfigurationFactory) {
		if (element == null) {
			throw new IllegalArgumentException(Element.class.getSimpleName() + " cannot be null");
		}
		// we compile using Maven2. @NotNull has no meaning in product
		//noinspection ConstantConditions
		if (privateConfigurationFactory == null) {
			throw new IllegalArgumentException(PrivateConfigurationFactory.class.getSimpleName() + " cannot be null");
		}
		this.publicElement = element;
		this.privateConfigurationFactory = privateConfigurationFactory;

	}

	public ProjectConfiguration load() throws ServerCfgFactoryException {
		PrivateProjectConfiguration ppc = new PrivateProjectConfiguration();
		ProjectConfiguration res = load(publicElement, ProjectConfiguration.class);

		for (ServerCfg serverCfg : res.getServers()) {
			try {
				@Nullable final PrivateServerCfgInfo privateServerCfgInfo
						= privateConfigurationFactory.load(serverCfg.getServerId());
				if (privateServerCfgInfo != null) {
					ppc.add(privateServerCfgInfo);
				}
			} catch (ServerCfgFactoryException e) {
				// let us ignore problem for the moment - there will be no private settings for such servers
			}
		}

		return merge(res, ppc);
	}

	public PrivateProjectConfiguration loadOldPrivateConfiguration(@NotNull Element privateElement)
			throws ServerCfgFactoryException {
		return load(privateElement, PrivateProjectConfiguration.class);
	}


	private <T> T load(final Element rootElement, Class<T> clazz) throws ServerCfgFactoryException {
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


	private ProjectConfiguration merge(final ProjectConfiguration projectConfiguration,
			final PrivateProjectConfiguration privateProjectConfiguration) {
		for (ServerCfg serverCfg : projectConfiguration.getServers()) {
			PrivateServerCfgInfo psci = privateProjectConfiguration.getPrivateServerCfgInfo(serverCfg.getServerId());
			serverCfg.mergePrivateConfiguration(psci);
		}
		return projectConfiguration;
	}

	public void save(final ProjectConfiguration projectConfiguration) {
		save(projectConfiguration, publicElement);
		for (ServerCfg serverCfg : projectConfiguration.getServers()) {
			try {
				privateConfigurationFactory.save(serverCfg.createPrivateProjectConfiguration());
			} catch (ServerCfgFactoryException e) {
				LoggerImpl.getInstance().error("Cannot write private cfg file for server Uuid = "
						+ serverCfg.getServerId().getUuid());
			}
		}
	}

	void save(final Object object, final Element rootElement) {
		if (object == null) {
			throw new NullPointerException("Serialized object cannot be null");
		}
		final JDomWriter writer = new JDomWriter(rootElement);
		final XStream xStream = JDomXStreamUtil.getProjectJDomXStream();
		xStream.marshal(object, writer);

	}

	static PrivateServerCfgInfo createPrivateProjectConfiguration(final ServerCfg serverCfg) {
		return serverCfg.createPrivateProjectConfiguration();
}
}
