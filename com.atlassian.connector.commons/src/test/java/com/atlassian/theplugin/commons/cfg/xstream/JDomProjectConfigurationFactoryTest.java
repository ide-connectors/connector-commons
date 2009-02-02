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

import com.atlassian.theplugin.commons.SubscribedPlan;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.FishEyeServerCfg;
import com.atlassian.theplugin.commons.cfg.PrivateConfigurationFactory;
import com.atlassian.theplugin.commons.cfg.PrivateServerCfgInfo;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ProjectConfigurationFactory;
import com.atlassian.theplugin.commons.cfg.ProjectConfigurationFactoryTest;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfgFactoryException;
import com.atlassian.theplugin.commons.cfg.ServerId;
import static com.atlassian.theplugin.commons.cfg.xstream.JDomProjectConfigurationFactory.createPrivateProjectConfiguration;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.commons.util.StringUtil;
import com.spartez.util.junit3.IAction;
import com.spartez.util.junit3.TestUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * JDomProjectConfigurationFactory Tester.
 *
 * @author wseliga
 */
public class JDomProjectConfigurationFactoryTest extends ProjectConfigurationFactoryTest {
	private final BambooServerCfg bamboo1 = new BambooServerCfg("mybamboo1", new ServerId("141d662c-e744-4690-a5f8-6e127c0bc84f"));
	private final BambooServerCfg bamboo2 = new BambooServerCfg("mybamboo2", new ServerId("241d662c-e744-4690-a5f8-6e127c0bc84f"));
	private final CrucibleServerCfg crucible1 = new CrucibleServerCfg("mycrucible1", new ServerId("341d662c-e744-4690-a5f8-6e127c0bc84f"));
	private final CrucibleServerCfg crucible2 = new CrucibleServerCfg("mycrucible2", new ServerId("341d662c-e744-4690-a5f8-6e127c0bc84e"));
	private final FishEyeServerCfg fisheye1 = new FishEyeServerCfg("myfisheye1", new ServerId("341d662c-e744-4690-a5f8-6e127c0bc84d"));
	private final PrivateConfigurationFactory PRIVATE_CFG_FACTORY = new MemoryPrivateConfigurationFactory();


	private ProjectConfiguration projectCfg;

	private static final String FAKE_CLASS_NAME = "whateverfakeclasshere";

	private Element element = new Element("test");
	private JDomProjectConfigurationFactory jdomFactory = new JDomProjectConfigurationFactory(element, PRIVATE_CFG_FACTORY);
	private static final String EXPECTED_OUTPUT_XML = "expected-output.xml";

	@Override
	protected ProjectConfigurationFactory getProjectConfigurationFactory() {
		return jdomFactory;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		projectCfg = new ProjectConfiguration(MiscUtil.buildArrayList(bamboo1, fisheye1));
		bamboo1.setPassword("mycleartextpassword");
		bamboo1.setPasswordStored(true);
		fisheye1.setUrl("https://lech.atlassian.pl/fisheye");
		fisheye1.setUsername("user1");
		fisheye1.setPassword("password23");
		fisheye1.setPasswordStored(true);
	}

	public void testJDomSaveLoadGlobalConfiguration() throws IOException, ServerCfgFactoryException {
		
		final JDomProjectConfigurationFactory factory = new JDomProjectConfigurationFactory(element, PRIVATE_CFG_FACTORY);
		factory.save(projectCfg);

		assertEquals(1, element.getChildren().size());


		final JDomProjectConfigurationFactory loadFactory = new JDomProjectConfigurationFactory(element, PRIVATE_CFG_FACTORY);
		ProjectConfiguration readCfg = loadFactory.load();
		assertEquals(projectCfg, readCfg);
	}


	public void testLoadOldSaveNew() {		
		final JDomProjectConfigurationFactory factory = new JDomProjectConfigurationFactory(element, PRIVATE_CFG_FACTORY);
		factory.save(projectCfg);
	}

	public void testHashedPassword() throws ServerCfgFactoryException, IOException {
		MyPrivateConfigurationFactory pcf = new MyPrivateConfigurationFactory();
		final JDomProjectConfigurationFactory factory = new JDomProjectConfigurationFactory(element, pcf);
		factory.save(projectCfg);

		final StringWriter writer = new StringWriter();

		new XMLOutputter(Format.getPrettyFormat()).output(element, writer);
		assertTrue(writer.toString().indexOf(bamboo1.getName()) != -1);
		// password should be hashed
		assertEquals(-1, writer.toString().indexOf(bamboo1.getPassword()));

		final StringWriter privateWriter = new StringWriter();
		new XMLOutputter(Format.getPrettyFormat()).output(pcf.documentMap.get(bamboo1.getServerId()), privateWriter);
		assertTrue(privateWriter.toString().indexOf(bamboo1.getServerId().getUuid().toString()) != -1);
		// password should be hashed - so it should not be found in resulting xml stream
		assertEquals(-1, privateWriter.toString().indexOf(bamboo1.getPassword()));
	}


	public void testPublicSerialization() throws ServerCfgFactoryException, IOException, JDOMException {
		bamboo1.getSubscribedPlans().add(new SubscribedPlan("MYID"));
		bamboo1.getSubscribedPlans().add(new SubscribedPlan("PLANID2"));
		bamboo1.setUrl("http://mygreaturl");
		bamboo1.setUsername("mytestuser");

		projectCfg.setDefaultCrucibleProject("CRUC");
		projectCfg.setDefaultCrucibleServerId(crucible2.getServerId());
		projectCfg.setDefaultFishEyeServerId(crucible1.getServerId());
		projectCfg.setDefaultCrucibleRepo("Repo1");
		projectCfg.setFishEyeProjectPath("FishEye/Path/To");
		projectCfg.setDefaultFishEyeRepo("FishRepo");

		final JDomProjectConfigurationFactory factory = new JDomProjectConfigurationFactory(element, PRIVATE_CFG_FACTORY);
		factory.save(projectCfg);

		StringWriter writer = new StringWriter();

		writeXml(element, writer);
		// password should be hashed
		String expected = StringUtil.slurp(getClass().getResourceAsStream(EXPECTED_OUTPUT_XML));
		assertEquals(expected, writer.toString());

		// and also vice-versa
		Document doc = new SAXBuilder(false).build(getClass().getResourceAsStream(EXPECTED_OUTPUT_XML));
		final JDomProjectConfigurationFactory loadFactory = new JDomProjectConfigurationFactory(doc.getRootElement(), PRIVATE_CFG_FACTORY);
		final ProjectConfiguration readCfg = loadFactory.load();
		assertEquals(projectCfg, readCfg);
	}


	public void testPublicOnlyDeSerialization() throws ServerCfgFactoryException, IOException, JDOMException {
		bamboo1.getSubscribedPlans().add(new SubscribedPlan("MYID"));
		bamboo1.getSubscribedPlans().add(new SubscribedPlan("PLANID2"));
		bamboo1.setUrl("http://mygreaturl");
		bamboo1.setUsername("mytestuser");

		final JDomProjectConfigurationFactory factory = new JDomProjectConfigurationFactory(element, PRIVATE_CFG_FACTORY);
		factory.save(projectCfg);

		StringWriter writer = new StringWriter();
		writeXml(element, writer);

		final Document doc = new SAXBuilder(false).build(new StringReader(writer.toString()));

		// load public info only
		final JDomProjectConfigurationFactory loadFactory = new JDomProjectConfigurationFactory(doc.getRootElement(),
				new MemoryPrivateConfigurationFactory());
		final ProjectConfiguration readCfg = loadFactory.load();
		assertEquals(2, readCfg.getServers().size());
		final ServerCfg readServer = readCfg.getServerCfg(bamboo1.getServerId());
		assertEquals("", readServer.getPassword());
		assertEquals("", readServer.getUsername());
		assertEquals(bamboo1.getUrl(), readServer.getUrl());
		assertEquals(bamboo1.getName(), readServer.getName());

		final ServerCfg fishServer = readCfg.getServerCfg(fisheye1.getServerId());
		assertEquals("", fishServer.getPassword());
		assertEquals("", fishServer.getUsername());
		assertEquals(fisheye1.getUrl(), fishServer.getUrl());
		assertEquals(fisheye1.getName(), fishServer.getName());
	}

	private void writeXml(final Element rootElement, final StringWriter writer) throws IOException {
		final Format prettyFormat = Format.getPrettyFormat();
		prettyFormat.setLineSeparator("\n");
		new XMLOutputter(prettyFormat).output(rootElement, writer);
	}

	public void testPrivateSerialization() throws ServerCfgFactoryException, IOException, JDOMException {
		bamboo1.setUsername("mytestuser");
		bamboo1.setPassword("mypassword1");
		bamboo1.setPasswordStored(true);
		bamboo2.setUsername("mytestuser2");
		bamboo2.setPassword("mypassword2");
		bamboo2.setPasswordStored(false);
		bamboo2.setTimezoneOffset(1);
		crucible1.setUsername("xyz");
		crucible1.setPassword("passwordxyz");
		crucible1.setPasswordStored(true);
		crucible1.setEnabled(false);
		projectCfg.getServers().add(bamboo2);
		projectCfg.getServers().add(crucible1);

		MyPrivateConfigurationFactory pcf = new MyPrivateConfigurationFactory();
		final JDomProjectConfigurationFactory factory = new JDomProjectConfigurationFactory(element, pcf);
		factory.save(projectCfg);

		assertCorrectOutput(pcf, bamboo1, "expected-private-output-bamboo1.xml");
		assertCorrectOutput(pcf, bamboo2, "expected-private-output-bamboo2.xml");
		assertCorrectOutput(pcf, fisheye1, "expected-private-output-fisheye1.xml");
		assertCorrectOutput(pcf, crucible1, "expected-private-output-crucible1.xml");

		// and also vice-versa
		final JDomProjectConfigurationFactory loadFactory = new JDomProjectConfigurationFactory(element, pcf);
		loadFactory.load();
		TestUtil.assertHasOnlyElements(pcf.getInfos(), createPrivateProjectConfiguration(bamboo1),
				createPrivateProjectConfiguration(bamboo2), createPrivateProjectConfiguration(crucible1),
				createPrivateProjectConfiguration(fisheye1));
	}

	private void assertCorrectOutput(final MyPrivateConfigurationFactory pcf, final ServerCfg serverCfg,
			final String filename) throws IOException {
		StringWriter writer = new StringWriter();
		writeXml(pcf.documentMap.get(serverCfg.getServerId()).getRootElement(), writer);

		final String expected = StringUtil.slurp(getClass().getResourceAsStream(filename));
		assertEquals(expected, writer.toString());
	}

	public void testPrivateSerializationEmptyUsernamePassword() throws ServerCfgFactoryException, IOException, JDOMException {
		bamboo1.setUsername("");
		bamboo1.setPassword("");
		bamboo1.setPasswordStored(true);
		bamboo2.setUsername("");
		bamboo2.setPassword("");
		bamboo2.setPasswordStored(false);
		crucible1.setUsername("xyz");
		crucible1.setPassword("passwordxyz");
		crucible1.setPasswordStored(true);
		crucible1.setEnabled(false);
		projectCfg.getServers().add(bamboo2);
		projectCfg.getServers().add(crucible1);

		final MyPrivateConfigurationFactory factory1 = new MyPrivateConfigurationFactory();
		final JDomProjectConfigurationFactory factory = new JDomProjectConfigurationFactory(element, factory1);
		factory.save(projectCfg);

		// and also vice-versa
		final JDomProjectConfigurationFactory loadFactory = new JDomProjectConfigurationFactory(element, factory1);
		loadFactory.load();
		TestUtil.assertHasOnlyElements(factory1.getInfos(), createPrivateProjectConfiguration(bamboo1),
				createPrivateProjectConfiguration(bamboo2), createPrivateProjectConfiguration(crucible1),
				createPrivateProjectConfiguration(fisheye1));
	}


	public void testCreatePrivateProjectConfiguration() {
		bamboo1.setUsername("mytestuser");
		bamboo1.setPassword("mypassword1");
		bamboo1.setPasswordStored(true);
		bamboo2.setUsername("mytestuser2");
		bamboo2.setPassword("mypassword2");
		bamboo2.setPasswordStored(false);
		bamboo2.setEnabled(false);
		final PrivateServerCfgInfo privateCfg = createPrivateProjectConfiguration(bamboo1);
		assertEquals(bamboo1.getUsername(), privateCfg.getUsername());
		assertEquals(bamboo1.getPassword(), privateCfg.getPassword());
		assertEquals(bamboo1.getServerId(), privateCfg.getServerId());

		final PrivateServerCfgInfo privateCfg2 = createPrivateProjectConfiguration(bamboo2);
		assertEquals(bamboo2.getUsername(), privateCfg2.getUsername());
		assertEquals(null, privateCfg2.getPassword());
		assertEquals(bamboo2.getServerId(), privateCfg2.getServerId());
		assertEquals(bamboo2.isEnabled(), privateCfg2.isEnabled());

		bamboo2.setPassword("");
		bamboo2.setPasswordStored(true);
		bamboo2.setUsername("");
		final PrivateServerCfgInfo privateCfg3 = createPrivateProjectConfiguration(bamboo2);
		assertEquals(bamboo2.getUsername(), privateCfg3.getUsername());
		assertEquals("", privateCfg3.getPassword());
		assertEquals(bamboo2.getServerId(), privateCfg3.getServerId());
		assertEquals(bamboo2.isEnabled(), privateCfg3.isEnabled());

	}


	public void testFullSaveLoad() throws ServerCfgFactoryException {
		bamboo1.setUsername("mytestuser");
		bamboo1.setPassword("mypassword1");
		bamboo1.getSubscribedPlans().add(new SubscribedPlan("myplan"));
		bamboo1.setPasswordStored(true);
		bamboo2.setUsername("mytestuser2");
		bamboo2.setPassword("mypassword2");
		bamboo2.setPasswordStored(true);
		crucible1.setPasswordStored(false);
		projectCfg = new ProjectConfiguration(MiscUtil.<ServerCfg>buildArrayList(bamboo1, crucible1, bamboo2));

		projectCfg.setDefaultCrucibleProject("CRUC");
		projectCfg.setDefaultCrucibleServerId(crucible2.getServerId());
		projectCfg.setDefaultFishEyeServerId(null);
		projectCfg.setDefaultCrucibleRepo("Repo2");

		final JDomProjectConfigurationFactory factory = new JDomProjectConfigurationFactory(element, PRIVATE_CFG_FACTORY);
		factory.save(projectCfg);
		final ProjectConfiguration res = factory.load();
		assertEquals(projectCfg, res);
		assertNotSame(projectCfg, res);

		element.getChildren().clear();

//		final JDomProjectConfigurationFactory factory2 = new JDomProjectConfigurationFactory(element, privateElement, PRIVATE_CFG_FACTORY);
		// now after reloading bamboo2 password will be lost
		bamboo2.setPasswordStored(false);
		factory.save(projectCfg);
		final ProjectConfiguration withoutPassword = factory.load();
		final BambooServerCfg bamboo2WithNoPassword = bamboo2.getClone();
		bamboo2WithNoPassword.setPassword("");
		TestUtil.assertNotEquals(projectCfg, withoutPassword);
		TestUtil.assertHasOnlyElements(withoutPassword.getServers(), bamboo1, crucible1, bamboo2WithNoPassword);

	}


	public void testInvalidJDomElement() {
		final JDomProjectConfigurationFactory factory = new JDomProjectConfigurationFactory(new Element("element"), PRIVATE_CFG_FACTORY);
		TestUtil.assertThrows(ServerCfgFactoryException.class, new IAction() {

			public void run() throws Throwable {
				factory.load();
			}
		});

		add(element, new Element(FAKE_CLASS_NAME));
		final JDomProjectConfigurationFactory factory2 = new JDomProjectConfigurationFactory(element, PRIVATE_CFG_FACTORY);
		TestUtil.assertThrows(ServerCfgFactoryException.class, new IAction() {

			public void run() throws Throwable {
				factory2.load();
			}
		});
	}

	private void add(final Element parent, final Element child) {
		@SuppressWarnings("unchecked")
		final List<Element> children = parent.getChildren();
		children.add(child);
	}

	public void testInvalidClass() throws ServerCfgFactoryException {
		// just let us forge a simple DOM which instead of ProjectConfiguration contains just ServerId
		final Element serverId = new Element(ServerId.class.getName());
		serverId.setText(new ServerId().getUuid().toString());
		add(element, serverId);
		final JDomProjectConfigurationFactory factory2 = new JDomProjectConfigurationFactory(element, PRIVATE_CFG_FACTORY);
		TestUtil.assertThrowsAndMsgContainsRe(ServerCfgFactoryException.class, 
				"Cannot load ProjectConfiguration.*ClassCastException",
				new IAction() {

			public void run() throws Throwable {
				factory2.load();
			}
		});

	}

	public void testNullDomElement() {
		TestUtil.assertThrows(IllegalArgumentException.class, new IAction() {
			public void run() throws Throwable {
				new JDomProjectConfigurationFactory(null, PRIVATE_CFG_FACTORY);
			}
		});
		TestUtil.assertThrows(IllegalArgumentException.class, new IAction() {
			public void run() throws Throwable {
				//noinspection ConstantConditions
				new JDomProjectConfigurationFactory(null, null);
			}
		});
	}

	public void testNullConfiguration() {
		TestUtil.assertThrows(NullPointerException.class, new IAction() {
			public void run() throws Throwable {
				jdomFactory.save(null);
			}
		});
	}


	public void testMissingPrivateCfg() throws ServerCfgFactoryException {
		final JDomProjectConfigurationFactory factory = new JDomProjectConfigurationFactory(element, new PrivateConfigurationFactory() {
			public PrivateServerCfgInfo load(final ServerId id)  {
				return null;
			}

			public void save(@NotNull final PrivateServerCfgInfo info) {
			}
		});
		factory.save(projectCfg);
		final ProjectConfiguration cfg = factory.load();
		final ServerCfg serverRead = cfg.getServerCfg(bamboo1.getServerId());
		assertEquals(bamboo1.getName(), serverRead.getName());
		assertTrue(serverRead.isEnabled());

	}

	private static class MyPrivateConfigurationFactory implements PrivateConfigurationFactory {
		private Map<ServerId, Document> documentMap = MiscUtil.buildHashMap();

		public PrivateServerCfgInfo load(final ServerId id) throws ServerCfgFactoryException {
			final Document document = documentMap.get(id);
			if (document == null) {
				return null;
			}
			return PrivateConfigurationFactoryImpl.load(document);
		}

		public void save(@NotNull final PrivateServerCfgInfo info) {
			final Document jDom = PrivateConfigurationFactoryImpl.createJDom(info);
			documentMap.put(info.getServerId(), jDom);
		}

		public Collection<PrivateServerCfgInfo> getInfos() throws ServerCfgFactoryException {
			final ArrayList<PrivateServerCfgInfo> res = MiscUtil.buildArrayList();
			for (Map.Entry<ServerId, Document> entry : documentMap.entrySet()) {
				res.add(load(entry.getKey()));
			}
			return res;
		}
	}

	private static class MemoryPrivateConfigurationFactory implements PrivateConfigurationFactory {
		private Map<ServerId, PrivateServerCfgInfo> map = MiscUtil.buildHashMap();

		public PrivateServerCfgInfo load(final ServerId id) throws ServerCfgFactoryException {
			return map.get(id);
		}

		public void save(@NotNull final PrivateServerCfgInfo info) {
			map.put(info.getServerId(), info);
		}
	}
}
