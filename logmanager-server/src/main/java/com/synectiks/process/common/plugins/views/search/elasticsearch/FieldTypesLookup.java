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
package com.synectiks.process.common.plugins.views.search.elasticsearch;

import com.google.common.collect.Sets;
import com.synectiks.process.server.indexer.fieldtypes.FieldTypeDTO;
import com.synectiks.process.server.indexer.fieldtypes.IndexFieldTypesService;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class FieldTypesLookup {
    private final IndexFieldTypesService indexFieldTypesService;

    @Inject
    public FieldTypesLookup(IndexFieldTypesService indexFieldTypesService) {
        this.indexFieldTypesService = indexFieldTypesService;
    }

    private Map<String, Set<String>> get(Set<String> streamIds) {
        return this.indexFieldTypesService.findForStreamIds(streamIds)
                .stream()
                .flatMap(indexFieldTypes -> indexFieldTypes.fields().stream())
                .collect(Collectors.toMap(
                        FieldTypeDTO::fieldName,
                        fieldType -> Collections.singleton(fieldType.physicalType()),
                        Sets::union
                ));
    }

    public Optional<String> getType(Set<String> streamIds, String field) {
        final Map<String, Set<String>> allFieldTypes = this.get(streamIds);
        final Set<String> fieldTypes = allFieldTypes.get(field);
        return fieldTypes == null || fieldTypes.size() > 1 ? Optional.empty() : fieldTypes.stream().findFirst();
    }
}
