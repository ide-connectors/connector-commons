package com.atlassian.theplugin.commons.crucible.api.content;

import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;

/**
 * User: mwent
 * Date: Mar 12, 2009
 * Time: 3:00:18 PM
 */
public interface ReviewFileContentProvider {

	ReviewFileContent getContent(final ReviewAdapter review,
			final VersionedVirtualFile fileInfo)
			throws ReviewFileContentException;

	CrucibleFileInfo getFileInfo();
}
