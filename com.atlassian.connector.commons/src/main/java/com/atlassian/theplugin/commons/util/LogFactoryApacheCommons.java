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
package com.atlassian.theplugin.commons.util;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.impl.LogFactoryImpl;
import org.apache.commons.logging.impl.SimpleLog;

/**
 * @autrhor pmaruszak
 * @date Feb 25, 2010
 */
public class LogFactoryApacheCommons extends LogFactoryImpl {
    @Override
    public Log getInstance(Class clazz) throws LogConfigurationException {
        if (clazz.equals(HttpMethodBase.class)) {
            return new ErrorSimpleLog();
        }
        return super.getInstance(clazz);
    }

    @Override
    public Log getInstance(String name) throws LogConfigurationException {
        if (name.equals(HttpMethodBase.class)) {
            return new ErrorSimpleLog();
        }
        return super.getInstance(name);
    }


    private static class ErrorSimpleLog extends SimpleLog {
        private ErrorSimpleLog() {
            super(HttpMethodBase.class.getName());
        }

        @Override
        protected void log(int type, Object message, Throwable t) {
            if (type >= SimpleLog.LOG_LEVEL_ERROR) {
                super.log(type, message, t);
            }
        }
    }
}
