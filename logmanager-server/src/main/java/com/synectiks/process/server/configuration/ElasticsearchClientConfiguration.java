/*
 * */
package com.synectiks.process.server.configuration;

import com.github.joschi.jadconfig.Parameter;
import com.github.joschi.jadconfig.util.Duration;
import com.github.joschi.jadconfig.validators.PositiveDurationValidator;
import com.github.joschi.jadconfig.validators.PositiveIntegerValidator;
import com.synectiks.process.server.configuration.converters.MajorVersionConverter;
import com.synectiks.process.server.configuration.converters.URIListConverter;
import com.synectiks.process.server.configuration.validators.ElasticsearchVersionValidator;
import com.synectiks.process.server.configuration.validators.HttpOrHttpsSchemeValidator;
import com.synectiks.process.server.configuration.validators.ListOfURIsWithHostAndSchemeValidator;
import com.synectiks.process.server.configuration.validators.NonEmptyListValidator;
import com.synectiks.process.server.plugin.Version;

import java.net.URI;
import java.util.Collections;
import java.util.List;

public class ElasticsearchClientConfiguration {
    @Parameter(value = "elasticsearch_version", converter = MajorVersionConverter.class, validators = {ElasticsearchVersionValidator.class})
    Version elasticsearchVersion;

    @Parameter(value = "elasticsearch_hosts", converter = URIListConverter.class, validators = {NonEmptyListValidator.class, ListOfURIsWithHostAndSchemeValidator.class})
    List<URI> elasticsearchHosts = Collections.singletonList(URI.create("http://127.0.0.1:9200"));

    @Parameter(value = "elasticsearch_connect_timeout", validators = {PositiveDurationValidator.class})
    Duration elasticsearchConnectTimeout = Duration.seconds(10);

    @Parameter(value = "elasticsearch_socket_timeout", validators = {PositiveDurationValidator.class})
    Duration elasticsearchSocketTimeout = Duration.seconds(60);

    @Parameter(value = "elasticsearch_idle_timeout")
    Duration elasticsearchIdleTimeout = Duration.seconds(-1L);

    @Parameter(value = "elasticsearch_max_total_connections", validators = {PositiveIntegerValidator.class})
    int elasticsearchMaxTotalConnections = 200;

    @Parameter(value = "elasticsearch_max_total_connections_per_route", validators = {PositiveIntegerValidator.class})
    int elasticsearchMaxTotalConnectionsPerRoute = 20;

    @Parameter(value = "elasticsearch_max_retries", validators = {PositiveIntegerValidator.class})
    int elasticsearchMaxRetries = 2;

    @Parameter(value = "elasticsearch_discovery_enabled")
    boolean discoveryEnabled = false;

    @Parameter(value = "elasticsearch_discovery_filter")
    String discoveryFilter = null;

    @Parameter(value = "elasticsearch_discovery_frequency", validators = {PositiveDurationValidator.class})
    Duration discoveryFrequency = Duration.seconds(30L);

    @Parameter(value = "elasticsearch_discovery_default_scheme", validators = {HttpOrHttpsSchemeValidator.class})
    String defaultSchemeForDiscoveredNodes = "http";

    @Parameter(value = "elasticsearch_discovery_default_user")
    String defaultUserForDiscoveredNodes = null;

    @Parameter(value = "elasticsearch_discovery_default_password")
    String defaultPasswordForDiscoveredNodes = null;

    @Parameter(value = "elasticsearch_compression_enabled")
    boolean compressionEnabled = false;

    @Parameter(value = "elasticsearch_use_expect_continue")
    boolean useExpectContinue = true;
}
