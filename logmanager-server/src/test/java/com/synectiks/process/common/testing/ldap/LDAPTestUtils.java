/*
 * */
package com.synectiks.process.common.testing.ldap;

import com.google.common.io.Resources;
import com.synectiks.process.common.testing.ResourceUtil;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.security.KeyStore;

public class LDAPTestUtils {
    private static final String RESOURCE_ROOT = "org/graylog/testing/ldap";

    public static final String BASE_LDIF = RESOURCE_ROOT + "/ldif/base.ldif";
    public static final String NESTED_GROUPS_LDIF = RESOURCE_ROOT + "/ldif/nested-groups.ldif";

    public static String testTLSCertsPath(String filename) {
        return getResourcePath(RESOURCE_ROOT + "/certs/" + filename);
    }

    public static KeyStore getKeystore(String filename) {
        try {
            final KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(Resources.getResource(RESOURCE_ROOT + "/" + filename).openStream(), "changeit".toCharArray());
            return keystore;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getResourcePath(String resourcePath) {
        final URL resourceUrl = Resources.getResource(resourcePath);
        try {
            // If the resource is located inside a JAR file, we need to write it to a temporary file to make it
            // accessible in the file system. (e.g. to mount it into a Docker container)
            if ("jar".equals(resourceUrl.getProtocol())) {
                return ResourceUtil.resourceURLToTmpFile(resourceUrl).toAbsolutePath().toString();
            }
            return Paths.get(resourceUrl.toURI()).toAbsolutePath().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
