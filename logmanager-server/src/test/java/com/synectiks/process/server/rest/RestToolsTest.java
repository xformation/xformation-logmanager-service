/*
 * */
package com.synectiks.process.server.rest;

import com.google.common.collect.ImmutableList;
import com.synectiks.process.server.configuration.HttpConfiguration;
import com.synectiks.process.server.rest.RestTools;
import com.synectiks.process.server.utilities.IpSubnet;

import org.glassfish.grizzly.http.server.Request;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.net.URI;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RestToolsTest {
    @Test
    public void getRemoteAddrFromRequestReturnsClientAddressWithNoXForwardedForHeader() throws Exception {
        final Request request = mock(Request.class);
        when(request.getRemoteAddr()).thenReturn("192.168.0.1");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        final String s = RestTools.getRemoteAddrFromRequest(request, Collections.emptySet());
        assertThat(s).isEqualTo("192.168.0.1");
    }

    @Test
    public void getRemoteAddrFromRequestReturnsHeaderContentWithXForwardedForHeaderFromTrustedNetwork() throws Exception {
        final Request request = mock(Request.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.100.42");
        final String s = RestTools.getRemoteAddrFromRequest(request, Collections.singleton(new IpSubnet("127.0.0.0/8")));
        assertThat(s).isEqualTo("192.168.100.42");
    }

    @Test
    public void getRemoteAddrFromRequestReturnsClientAddressWithXForwardedForHeaderFromUntrustedNetwork() throws Exception {
        final Request request = mock(Request.class);
        when(request.getRemoteAddr()).thenReturn("192.168.0.1");
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.100.42");
        final String s = RestTools.getRemoteAddrFromRequest(request, Collections.singleton(new IpSubnet("127.0.0.0/8")));
        assertThat(s).isEqualTo("192.168.0.1");
    }

    @Test
    public void buildExternalUriReturnsDefaultUriIfHeaderIsMissing() throws Exception {
        final MultivaluedMap<String, String> httpHeaders = new MultivaluedHashMap<>();
        final URI externalUri = URI.create("http://graylog.example.com/");
        assertThat(RestTools.buildExternalUri(httpHeaders, externalUri)).isEqualTo(externalUri);
    }

    @Test
    public void buildExternalUriReturnsDefaultUriIfHeaderIsEmpty() throws Exception {
        final MultivaluedMap<String, String> httpHeaders = new MultivaluedHashMap<>();
        httpHeaders.putSingle(HttpConfiguration.OVERRIDE_HEADER, "");
        final URI externalUri = URI.create("http://graylog.example.com/");
        assertThat(RestTools.buildExternalUri(httpHeaders, externalUri)).isEqualTo(externalUri);
    }

    @Test
    public void buildExternalUriReturnsHeaderValueIfHeaderIsPresent() throws Exception {
        final MultivaluedMap<String, String> httpHeaders = new MultivaluedHashMap<>();
        httpHeaders.putSingle(HttpConfiguration.OVERRIDE_HEADER, "http://header.example.com");
        final URI externalUri = URI.create("http://graylog.example.com");
        assertThat(RestTools.buildExternalUri(httpHeaders, externalUri)).isEqualTo(URI.create("http://header.example.com/"));
    }

    @Test
    public void buildEndpointUriReturnsFirstHeaderValueIfMultipleHeadersArePresent() throws Exception {
        final MultivaluedMap<String, String> httpHeaders = new MultivaluedHashMap<>();
        httpHeaders.put(HttpConfiguration.OVERRIDE_HEADER, ImmutableList.of("http://header1.example.com", "http://header2.example.com"));
        final URI endpointUri = URI.create("http://graylog.example.com");
        assertThat(RestTools.buildExternalUri(httpHeaders, endpointUri)).isEqualTo(URI.create("http://header1.example.com/"));
    }

    @Test
    public void buildEndpointUriEnsuresTrailingSlash() {
        final MultivaluedMap<String, String> httpHeaders = new MultivaluedHashMap<>();
        final URI endpointUri = URI.create("http://graylog.example.com");
        final URI endpointUri2 = URI.create("http://graylog.example.com/");

        assertThat(RestTools.buildExternalUri(httpHeaders, endpointUri)).isEqualTo(URI.create("http://graylog.example.com/"));
        assertThat(RestTools.buildExternalUri(httpHeaders, endpointUri2)).isEqualTo(URI.create("http://graylog.example.com/"));

        httpHeaders.putSingle(HttpConfiguration.OVERRIDE_HEADER, "http://header.example.com");
        assertThat(RestTools.buildExternalUri(httpHeaders, endpointUri)).isEqualTo(URI.create("http://header.example.com/"));

        httpHeaders.putSingle(HttpConfiguration.OVERRIDE_HEADER, "http://header.example.com/");
        assertThat(RestTools.buildExternalUri(httpHeaders, endpointUri)).isEqualTo(URI.create("http://header.example.com/"));
    }

    @Test
    public void getRemoteAddrFromRequestWorksWithIPv6IfSubnetsContainsOnlyIPv4() throws Exception {
        final Request request = mock(Request.class);
        when(request.getRemoteAddr()).thenReturn("2001:DB8::42");
        when(request.getHeader("X-Forwarded-For")).thenReturn("2001:DB8::1");
        final String s = RestTools.getRemoteAddrFromRequest(request, Collections.singleton(new IpSubnet("127.0.0.1/32")));
        assertThat(s).isEqualTo("2001:DB8::42");
    }

    @Test
    public void getRemoteAddrFromRequestWorksWithIPv6IfSubnetsContainsOnlyIPv6() throws Exception {
        final Request request = mock(Request.class);
        when(request.getRemoteAddr()).thenReturn("2001:DB8::42");
        when(request.getHeader("X-Forwarded-For")).thenReturn("2001:DB8::1:2:3:4:5:6");
        final String s = RestTools.getRemoteAddrFromRequest(request, Collections.singleton(new IpSubnet("2001:DB8::/32")));
        assertThat(s).isEqualTo("2001:DB8::1:2:3:4:5:6");
    }
}
