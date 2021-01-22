/*
 * */
package com.synectiks.process.server.inputs.extractors;

import com.codahale.metrics.MetricRegistry;
import com.synectiks.process.server.ConfigurationException;
import com.synectiks.process.server.plugin.inputs.Converter;
import com.synectiks.process.server.plugin.inputs.Extractor;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;

public class RegexReplaceExtractor extends Extractor {
    private static final String CONFIG_REGEX = "regex";
    private static final String CONFIG_REPLACEMENT = "replacement";
    private static final String CONFIG_REPLACE_ALL = "replace_all";
    private static final String DEFAULT_REPLACE_VALUE = "$1";

    private final Pattern pattern;
    private final String replacement;
    private final boolean replaceAll;

    public RegexReplaceExtractor(final MetricRegistry metricRegistry,
                                 final String id,
                                 final String title,
                                 final long order,
                                 final CursorStrategy cursorStrategy,
                                 final String sourceField,
                                 final String targetField,
                                 final Map<String, Object> extractorConfig,
                                 final String creatorUserId,
                                 final List<Converter> converters,
                                 final ConditionType conditionType,
                                 final String conditionValue) throws ReservedFieldException, ConfigurationException {
        super(metricRegistry, id, title, order, Type.REGEX_REPLACE, cursorStrategy, sourceField, targetField, extractorConfig, creatorUserId, converters, conditionType, conditionValue);

        if (extractorConfig == null || extractorConfig.isEmpty()) {
            throw new ConfigurationException("Missing configuration");
        }

        final Object configRegexValue = extractorConfig.get(CONFIG_REGEX);
        if (!(configRegexValue instanceof String) || ((String) configRegexValue).isEmpty()) {
            throw new ConfigurationException("Missing configuration field: " + CONFIG_REGEX);
        }

        final Object configReplaceValue = extractorConfig.get(CONFIG_REPLACEMENT);
        if (configReplaceValue != null && !(configReplaceValue instanceof String)) {
            throw new ConfigurationException("Missing configuration field: " + CONFIG_REPLACEMENT);
        }

        final Object configReplaceAll = extractorConfig.get(CONFIG_REPLACE_ALL);
        if (configReplaceAll != null && !(configReplaceAll instanceof Boolean)) {
            throw new ConfigurationException("Missing configuration field: " + CONFIG_REPLACE_ALL);
        }

        this.pattern = Pattern.compile((String) configRegexValue, Pattern.DOTALL);
        this.replacement = isNullOrEmpty((String) configReplaceValue) ? DEFAULT_REPLACE_VALUE : (String) configReplaceValue;
        this.replaceAll = configReplaceAll != null && (boolean) configReplaceAll;
    }

    @Override
    protected Result[] run(String value) {
        final Result result = runExtractor(value);
        return result == null ? null : new Result[]{result};
    }

    public Result runExtractor(String value) {
        final Matcher matcher = pattern.matcher(value);

        final boolean found = matcher.find();
        if (!found) {
            return null;
        }

        final int start = matcher.groupCount() > 0 ? matcher.start(1) : -1;
        final int end = matcher.groupCount() > 0 ? matcher.end(1) : -1;

        final String s;
        try {
            s = replaceAll ? matcher.replaceAll(replacement) : matcher.replaceFirst(replacement);
        } catch (Exception e) {
            throw new RuntimeException("Error while trying to replace string", e);
        }

        return new Result(s, start, end);
    }

}
