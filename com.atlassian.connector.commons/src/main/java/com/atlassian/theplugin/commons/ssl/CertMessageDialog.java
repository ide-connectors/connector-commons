package com.atlassian.theplugin.commons.ssl;

import java.security.cert.X509Certificate;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Jul 2, 2009
 * Time: 3:11:19 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CertMessageDialog {
    void show(String host, String message, X509Certificate[] chain);
    boolean isOK();
    boolean isTemporarily();
}
