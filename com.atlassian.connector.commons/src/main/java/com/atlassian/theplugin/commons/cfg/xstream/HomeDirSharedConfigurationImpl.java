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

import com.atlassian.theplugin.commons.cfg.ServerCfgFactoryException;
import com.atlassian.theplugin.commons.cfg.SharedServerList;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * @autrhor pmaruszak
 * @date Jun 10, 2010
 */


public class HomeDirSharedConfigurationImpl
		extends BasePrivateConfigurationDao<SharedServerList>
		implements UserSharedConfigurationDao {
	private static final String GLOBAL_SERVERS_FILE_NAME = "shared-servers";

	/**
	 * This method is blocking for a very short period of time in some cases
	 */
	public synchronized void save(SharedServerList serversInfo) throws ServerCfgFactoryException {
		Document document;
		FileLock lock = null;
		File outputFile = null;

		try {
			outputFile = new File(getPrivateCfgDirectorySavePath(), GLOBAL_SERVERS_FILE_NAME);
			if (!outputFile.exists()) {
				outputFile = new File(getPrivateCfgDirectorySavePath(), GLOBAL_SERVERS_FILE_NAME);

			}
			final FileChannel channel = new RandomAccessFile(outputFile, "rw").getChannel();
			for (int i = 0; i < 3; i++) {
				try {
					lock = channel.tryLock();
					break;
				} catch (IOException e) {
					wait(2 ^ i * 100);

				}
			}
			if (lock == null) {
			   throw new ServerCfgFactoryException("Cannot lock shared file: " + outputFile.getAbsolutePath());
			}
			SharedServerList storedList = load();
			if (outputFile.exists() && outputFile.canWrite()) {
//				if (storedList != null && serversInfo.size() > 0) {
					//merge existing cfg
					//document = createJDom(SharedServerList.merge(serversInfo, storedList));
//                    document = createJDom(serversInfo);
//				} else {
					document = createJDom(serversInfo);
//				}

				writeXmlFile(document.getRootElement(), outputFile);
			} else {
				throw new ServerCfgFactoryException("Shared configuration hasn't been saved");
			}

		} catch (Exception e) {
			final ServerCfgFactoryException ex = new ServerCfgFactoryException(e.getMessage());
			ex.initCause(e);
			throw ex;
		} finally {
			if (lock != null) {
				try {
					lock.release();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public SharedServerList load() throws ServerCfgFactoryException {
		final File atlassianDir = getPrivateCfgDirectorySavePath();

		if (isDirReady()) {
			final File sharedConfigFile = new File(atlassianDir.getAbsolutePath(), GLOBAL_SERVERS_FILE_NAME);
			if (sharedConfigFile.isFile() && sharedConfigFile.canRead() && sharedConfigFile.length() > 0) {
				Document doc;

				final SAXBuilder builder = new SAXBuilder(false);
				try {

					doc = builder.build(sharedConfigFile.getAbsolutePath());
				} catch (JDOMException e) {
					throw new ServerCfgFactoryException("Cannot parse shared cfg file " + e.getMessage());
				} catch (IOException e) {
					throw new ServerCfgFactoryException("Cannot read shared cfg file " + e.getMessage());
				}

				SharedServerList globalServerInfos = null;
				if (doc != null) {
					globalServerInfos = loadJDom(doc.getRootElement(), SharedServerList.class, true);
				}
				return globalServerInfos;
			} else {
				return null;
			}

		} else {
			throw new ServerCfgFactoryException("Cannot read shared configuration stored in directory ["
					+ atlassianDir.getAbsolutePath() + "]. Directory does not exist or is not accessible");
		}
	}


	@Override
	String getRootElementName() {
		return GLOBAL_SERVERS_FILE_NAME;
	}
}
