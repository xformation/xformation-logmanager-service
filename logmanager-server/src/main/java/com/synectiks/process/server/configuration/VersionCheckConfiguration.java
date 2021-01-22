/*
 * */
package com.synectiks.process.server.configuration;

import com.github.joschi.jadconfig.Parameter;

import java.net.URI;

public class VersionCheckConfiguration {
    @Parameter(value = "versionchecks")
    private boolean enabled = true;

    @Parameter(value = "versionchecks_uri")
    private URI uri = URI.create("https://versioncheck.graylog.com/check");

    public boolean isEnabled() {
        return enabled;
    }

    public URI getUri() {
        return uri;
    }
}
