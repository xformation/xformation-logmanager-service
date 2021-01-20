/*
 * Copyright (C) 2020 Graylog, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */
package com.synectiks.process.server.lookup;

import com.google.inject.Scopes;
import com.synectiks.process.server.lookup.adapters.CSVFileDataAdapter;
import com.synectiks.process.server.lookup.adapters.DSVHTTPDataAdapter;
import com.synectiks.process.server.lookup.adapters.DnsLookupDataAdapter;
import com.synectiks.process.server.lookup.adapters.HTTPJSONPathDataAdapter;
import com.synectiks.process.server.lookup.caches.CaffeineLookupCache;
import com.synectiks.process.server.lookup.caches.NullCache;
import com.synectiks.process.server.plugin.inject.Graylog2Module;
import com.synectiks.process.server.system.urlwhitelist.UrlWhitelistNotificationService;
import com.synectiks.process.server.system.urlwhitelist.UrlWhitelistService;

public class LookupModule extends Graylog2Module {

    @Override
    protected void configure() {
        serviceBinder().addBinding().to(UrlWhitelistService.class).in(Scopes.SINGLETON);
        binder().bind(UrlWhitelistNotificationService.class).in(Scopes.SINGLETON);

        serviceBinder().addBinding().to(LookupTableService.class).asEagerSingleton();

        installLookupCache(NullCache.NAME,
                NullCache.class,
                NullCache.Factory.class,
                NullCache.Config.class);

        installLookupCache(CaffeineLookupCache.NAME,
                CaffeineLookupCache.class,
                CaffeineLookupCache.Factory.class,
                CaffeineLookupCache.Config.class);

        installLookupDataAdapter(CSVFileDataAdapter.NAME,
                CSVFileDataAdapter.class,
                CSVFileDataAdapter.Factory.class,
                CSVFileDataAdapter.Config.class);

        installLookupDataAdapter2(DnsLookupDataAdapter.NAME,
                                 DnsLookupDataAdapter.class,
                                 DnsLookupDataAdapter.Factory.class,
                                 DnsLookupDataAdapter.Config.class);

        installLookupDataAdapter2(HTTPJSONPathDataAdapter.NAME,
                HTTPJSONPathDataAdapter.class,
                HTTPJSONPathDataAdapter.Factory.class,
                HTTPJSONPathDataAdapter.Config.class);

        installLookupDataAdapter(DSVHTTPDataAdapter.NAME,
                DSVHTTPDataAdapter.class,
                DSVHTTPDataAdapter.Factory.class,
                DSVHTTPDataAdapter.Config.class);
    }

}
