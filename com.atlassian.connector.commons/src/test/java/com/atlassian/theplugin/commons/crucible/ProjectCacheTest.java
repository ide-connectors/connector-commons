package com.atlassian.theplugin.commons.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProjectBean;
import com.atlassian.theplugin.commons.crucible.api.rest.CrucibleSessionImpl;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import junit.framework.TestCase;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

public class ProjectCacheTest extends TestCase {
	private ProjectCache projectCache;

	private CrucibleSessionImpl sessionMock;

	private List<CrucibleProject> projectsList;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		sessionMock = Mockito.mock(CrucibleSessionImpl.class);
		projectCache = new ProjectCache(sessionMock);
		projectsList = createProjectsList();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetProjectsEmptySessionResponse() throws Exception {

		// use Mock
		assertEquals(0, projectCache.getProjects().size());
		projectCache.getProjects();

		// verify Mock
		Mockito.verify(sessionMock, Mockito.times(2)).getProjectsFromServer();
	}

	public void testGetProjectsNonEmptySessionResponse() throws Exception {

		// stub Mock
		Mockito.when(sessionMock.getProjectsFromServer()).thenReturn(projectsList);

		// use Mock
		assertEquals(2, projectCache.getProjects().size());
		projectCache.getProjects();

		// verify Mock
		Mockito.verify(sessionMock, Mockito.times(1)).getProjectsFromServer();
	}

	public void testGetProjectNotExistsOnTheServer() throws Exception {

		// use Mock
		assertNull(projectCache.getProject(projectsList.get(0).getKey()));
		assertNull(projectCache.getProject(projectsList.get(0).getKey()));

		// verify Mock
		Mockito.verify(sessionMock, Mockito.times(2)).getProjectsFromServer();
	}

	public void testGetProjectExistsOnTheServer() throws RemoteApiException {

		// stub Mock
		Mockito.when(sessionMock.getProjectsFromServer()).thenReturn(projectsList);

		// use Mock
		assertEquals(projectsList.get(0), projectCache.getProject(projectsList.get(0).getKey()));

		// verify Mock
		Mockito.verify(sessionMock, Mockito.times(1)).getProjectsFromServer();
	}

	public void testGetProjectClearCache() throws RemoteApiException {
		List<CrucibleProject> projectsList2 = createProjectsList2();

		// stub Mock
		Mockito.when(sessionMock.getProjectsFromServer()).thenReturn(projectsList);
		// use Mock
		assertEquals(projectsList.get(0), projectCache.getProject(projectsList.get(0).getKey()));

		// stub Mock
		Mockito.when(sessionMock.getProjectsFromServer()).thenReturn(projectsList2);
		assertEquals(projectsList2.get(0), projectCache.getProject(projectsList2.get(0).getKey()));

		projectCache.getProject(projectsList.get(0).getKey());

		// verify Mock
		Mockito.verify(sessionMock, Mockito.times(3)).getProjectsFromServer();
	}

	private List<CrucibleProject> createProjectsList() {

		CrucibleProjectBean project1 = new CrucibleProjectBean();
		project1.setKey("A");
		CrucibleProjectBean project2 = new CrucibleProjectBean();
		project2.setKey("B");
		return Arrays.asList((CrucibleProject) project1, (CrucibleProject) project2);

	}

	private List<CrucibleProject> createProjectsList2() {

		CrucibleProjectBean project1 = new CrucibleProjectBean();
		project1.setKey("C");
		CrucibleProjectBean project2 = new CrucibleProjectBean();
		project2.setKey("D");
		return Arrays.asList((CrucibleProject) project1, (CrucibleProject) project2);

	}
}
