package com.atlassian.theplugin.commons.crucible.api;

/**
 * User: kalamon
 * Date: 2009-11-20
 * Time: 13:24:41
 */
public class PathAndRevision {
    private String path;
    private String revision;

    public PathAndRevision() {
    }

    public PathAndRevision(String path, String revision) {
        this.path = path;
        this.revision = revision;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }
}
