/*
 * */
package com.synectiks.process.common.plugins.sidecar.services;

import com.codahale.metrics.MetricRegistry;
import com.github.joschi.jadconfig.util.Duration;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.AbstractIdleService;
import com.synectiks.process.common.plugins.sidecar.common.SidecarPluginConfiguration;
import com.synectiks.process.server.events.ClusterEventBus;
import com.synectiks.process.server.metrics.CacheStatsSet;
import com.synectiks.process.server.shared.metrics.MetricUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.codahale.metrics.MetricRegistry.name;

@Singleton
public class EtagService extends AbstractIdleService {
    private static final Logger LOG = LoggerFactory.getLogger(EtagService.class);

    private final Cache<String, Boolean> cache;
    private MetricRegistry metricRegistry;
    private EventBus eventBus;
    private ClusterEventBus clusterEventBus;

    @Inject
    public EtagService(SidecarPluginConfiguration pluginConfiguration,
                       MetricRegistry metricRegistry,
                       EventBus eventBus,
                       ClusterEventBus clusterEventBus) {
        this.metricRegistry = metricRegistry;
        this.eventBus = eventBus;
        this.clusterEventBus = clusterEventBus;
        Duration cacheTime = pluginConfiguration.getCacheTime();
        cache = CacheBuilder.newBuilder()
                .recordStats()
                .expireAfterWrite(cacheTime.getQuantity(), cacheTime.getUnit())
                .maximumSize(pluginConfiguration.getCacheMaxSize())
                .build();
    }

    @Subscribe
    public void handleEtagInvalidation(EtagCacheInvalidation event) {
        if (event.etag().equals("")) {
            LOG.trace("Invalidating all collector configuration etags");
            cache.invalidateAll();
        } else {
            LOG.trace("Invalidating collector configuration etag {}", event.etag());
            cache.invalidate(event.etag());
        }
    }

    public boolean isPresent(String etag) {
        return cache.getIfPresent(etag) != null;
    }

    public void put(String etag) {
        cache.put(etag, Boolean.TRUE);
    }

    public void invalidate(String etag) {
        clusterEventBus.post(EtagCacheInvalidation.etag(etag));
    }

    public void invalidateAll() {
        cache.invalidateAll();
        clusterEventBus.post(EtagCacheInvalidation.etag(""));
    }

    @Override
    protected void startUp() throws Exception {
        eventBus.register(this);
        MetricUtils.safelyRegisterAll(metricRegistry, new CacheStatsSet(name(ConfigurationService.class, "etag-cache"), cache));
    }

    @Override
    protected void shutDown() throws Exception {
        eventBus.unregister(this);
        metricRegistry.removeMatching((name, metric) -> name.startsWith(name(ConfigurationService.class, "etag-cache")));
    }
}
