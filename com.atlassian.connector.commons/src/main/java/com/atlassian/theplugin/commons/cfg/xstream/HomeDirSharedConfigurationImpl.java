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

/**
 * @autrhor pmaruszak
 * @date Jun 10, 2010
 */


public class HomeDirSharedConfigurationImpl
        extends BasePrivateConfigurationDao<SharedServerList>
        implements UserSharedConfigurationDao {
    private static final String GLOBAL_SERVERS_FILE_NAME = "shared-servers";

    public void save(SharedServerList serversInfo) throws ServerCfgFactoryException {
        Document document = createJDom(serversInfo);

            try {

                final File outputFile = new File(getPrivateCfgDirectorySavePath(),
                        GLOBAL_SERVERS_FILE_NAME);
                if (outputFile.exists() && outputFile.canWrite()) {
                    //clean up file;
                    outputFile.delete();
                }
                writeXmlFile(document.getRootElement(), outputFile);
            } catch (Exception e) {
                final ServerCfgFactoryException ex = new ServerCfgFactoryException(e.getMessage());
                ex.initCause(e);
                throw ex;
            }
    }

    public SharedServerList load() throws ServerCfgFactoryException {
        final File atlassianDir = getPrivateCfgDirectorySavePath();

		if (isDirReady()) {
			final File sharedConfigFile = new File(atlassianDir.getAbsolutePath(), GLOBAL_SERVERS_FILE_NAME);
			if (sharedConfigFile.isFile() && sharedConfigFile.canRead()) {
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
