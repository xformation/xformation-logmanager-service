/*
 * */
package com.synectiks.process.server.indexer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.synectiks.process.server.indexer.indexset.IndexSetConfig;
import com.synectiks.process.server.plugin.Message;
import com.synectiks.process.server.plugin.Tools;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Representing the message type mapping in Elasticsearch. This is giving ES more
 * information about what the fields look like and how it should analyze them.
 */
public abstract class IndexMapping implements IndexMappingTemplate {
    public static final String TYPE_MESSAGE = "message";

    @Override
    public Map<String, Object> toTemplate(IndexSetConfig indexSetConfig, String indexPattern, int order) {
        return messageTemplate(indexPattern, indexSetConfig.indexAnalyzer(), order);
    }

    protected Map<String, Object> analyzerKeyword() {
        return ImmutableMap.of("analyzer_keyword", ImmutableMap.of(
                "tokenizer", "keyword",
                "filter", "lowercase"));
    }

    public Map<String, Object> messageTemplate(final String template, final String analyzer, final int order) {
        final Map<String, Object> settings = Collections.singletonMap(
                "analysis", Collections.singletonMap("analyzer", analyzerKeyword())
                );
        final Map<String, Object> mappings = mapping(analyzer);

        return createTemplate(template, order, settings, mappings);
    }

    Map<String, Object> createTemplate(String template, int order, Map<String, Object> settings, Map<String, Object> mappings) {
        return ImmutableMap.of(
                "template", template,
                "order", order,
                "settings", settings,
                "mappings", mappings
        );
    }

    protected Map<String, Object> mapping(String analyzer) {
        return ImmutableMap.of(TYPE_MESSAGE, messageMapping(analyzer));
    }

    protected Map<String, Object> messageMapping(final String analyzer) {
        return ImmutableMap.of(
                "properties", fieldProperties(analyzer),
                "dynamic_templates", dynamicTemplate(),
                "_source", enabled());
    }

    private Map<String, Map<String, Object>> internalFieldsMapping() {
        return ImmutableMap.of("internal_fields",
                ImmutableMap.of(
                        "match", "xflog_*",
                        "match_mapping_type", "string",
                        "mapping", notAnalyzedString())
        );
    }

    protected List<Map<String, Map<String, Object>>> dynamicTemplate() {
        final Map<String, Map<String, Object>> templateInternal = internalFieldsMapping();

        final Map<String, Map<String, Object>> templateAll = ImmutableMap.of("store_generic", dynamicStrings());

        return ImmutableList.of(templateInternal, templateAll);
    }

    abstract Map<String, Object> dynamicStrings();

    protected Map<String, Map<String, Object>> fieldProperties(String analyzer) {
        return ImmutableMap.<String, Map<String, Object>>builder()
                .put("message", analyzedString(analyzer, false))
                .put("full_message", analyzedString(analyzer, false))
                // http://joda-time.sourceforge.net/api-release/org/joda/time/format/DateTimeFormat.html
                // http://www.elasticsearch.org/guide/reference/mapping/date-format.html
                .put("timestamp", typeTimeWithMillis())
                .put(Message.FIELD_XFLOG_ACCOUNTED_MESSAGE_SIZE, typeLong())
                .put(Message.FIELD_XFLOG_RECEIVE_TIMESTAMP, typeTimeWithMillis())
                .put(Message.FIELD_XFLOG_PROCESSING_TIMESTAMP, typeTimeWithMillis())
                // to support wildcard searches in source we need to lowercase the content (wildcard search lowercases search term)
                .put("source", analyzedString("analyzer_keyword", true))
                .put("streams", notAnalyzedString())
                .build();
    }

    Map<String, Object> notAnalyzedString() {
        return ImmutableMap.of("type", "keyword");
    }
    Map<String, Object> analyzedString(String analyzer, boolean fieldData) {
        return ImmutableMap.of(
                "type", "text",
                "analyzer", analyzer,
                "fielddata", fieldData);
    }

    protected Map<String, Object> typeTimeWithMillis() {
        return ImmutableMap.of(
                "type", "date",
                "format", Tools.ES_DATE_FORMAT);
    }

    protected Map<String, Object> typeLong() {
        return ImmutableMap.of("type", "long");
    }

    private Map<String, Boolean> enabled() {
        return ImmutableMap.of("enabled", true);
    }
}
