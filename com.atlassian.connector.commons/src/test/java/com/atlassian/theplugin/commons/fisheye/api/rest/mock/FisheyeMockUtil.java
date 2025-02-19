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

package com.atlassian.theplugin.commons.fisheye.api.rest.mock;

import static junit.framework.Assert.fail;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.atlassian.theplugin.util.AbstractMockUtil;

public final class FisheyeMockUtil extends AbstractMockUtil {

	private static final String RESOURCE_BASE = "/mock/fisheye/api/rest/";

	public FisheyeMockUtil() {
	}

    protected String getBase() {
        return RESOURCE_BASE;
    }
}