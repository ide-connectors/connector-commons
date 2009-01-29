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

import com.atlassian.theplugin.commons.cfg.*;
import com.atlassian.theplugin.commons.exception.ThePluginException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.JDomReader;
import com.thoughtworks.xstream.io.xml.JDomWriter;
import org.jdom.Element;

public class JDomProjectConfigurationFactory implements ProjectConfigurationFactory {

	private final Element publicElement;
	private final PrivateConfigurationFactory privateConfigurationFactory;
	private final Element privateElement;


	public JDomProjectConfigurationFactory(final Element element, final Element privateElement,
			PrivateConfigurationFactory privateConfigurationFactory) {
		this.privateElement = privateElement;
		if (element == null) {
			throw new NullPointerException(Element.class.getSimpleName() + " cannot be null");
		}
		this.publicElement = element;
		this.privateConfigurationFactory = privateConfigurationFactory;

	}

	public ProjectConfiguration load() throws ServerCfgFactoryException {
		PrivateProjectConfiguration ppc = new PrivateProjectConfiguration();
		PrivateProjectConfiguration oldPpc = new PrivateProjectConfiguration();
		ProjectConfiguration res = load(publicElement, ProjectConfiguration.class);

		try {
			oldPpc = (privateElement != null)
					? load(privateElement, PrivateProjectConfiguration.class)
					: new PrivateProjectConfiguration();
		} catch (ServerCfgFactoryException e) {
			//ignore we want to migrate to new location
		}

		for (ServerCfg serverCfg : res.getServers()) {
			try {
				PrivateServerCfgInfo privateServerCfgInfo = privateConfigurationFactory.load(serverCfg.getServerId());
				ppc.add(privateServerCfgInfo);
			} catch (ThePluginException e) {
				//no new configuration use load from old location
				ppc = oldPpc;

			} catch (ServerCfgFactoryException e) {
				//server is not dfined in new cfg location try to read from old one
				PrivateServerCfgInfo privateCfg = oldPpc.getPrivateServerCfgInfo(serverCfg.getServerId());
				if (privateCfg != null) {
					ppc.add(privateCfg);
				}
			}
		}

		return merge(res, ppc);
	}


	<T> T load(final Element rootElement, Class<T> clazz) throws ServerCfgFactoryException {
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
		final PrivateProjectConfiguration privateCfg = getPrivateProjectConfiguration(projectConfiguration);
		save(privateCfg, privateElement);
	}

	void save(final Object object, final Element rootElement) {
		if (object == null) {
			throw new NullPointerException("Serialized object cannot be null");
		}
		final JDomWriter writer = new JDomWriter(rootElement);
		final XStream xStream = JDomXStreamUtil.getProjectJDomXStream();
		xStream.marshal(object, writer);

	}

	PrivateProjectConfiguration getPrivateProjectConfiguration(final ProjectConfiguration projectConfiguration) {
		final PrivateProjectConfiguration res = new PrivateProjectConfiguration();
		for (ServerCfg serverCfg : projectConfiguration.getServers()) {
			res.add(createPrivateProjectConfiguration(serverCfg));
		}
		return res;
	}

	static PrivateServerCfgInfo createPrivateProjectConfiguration(final ServerCfg serverCfg) {
		return serverCfg.createPrivateProjectConfiguration();
}
}
