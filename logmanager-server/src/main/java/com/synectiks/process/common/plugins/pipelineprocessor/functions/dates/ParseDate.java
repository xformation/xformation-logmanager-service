/*
 * */
package com.synectiks.process.common.plugins.pipelineprocessor.functions.dates;

import com.google.common.collect.ImmutableList;
import com.synectiks.process.common.plugins.pipelineprocessor.EvaluationContext;
import com.synectiks.process.common.plugins.pipelineprocessor.ast.functions.FunctionArgs;
import com.synectiks.process.common.plugins.pipelineprocessor.ast.functions.ParameterDescriptor;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;
import java.util.Optional;

public class ParseDate extends TimezoneAwareFunction {
    public static final String NAME = "parse_date";

    private static final String VALUE = "value";
    private static final String PATTERN = "pattern";
    private static final String LOCALE = "locale";

    private final ParameterDescriptor<String, String> valueParam;
    private final ParameterDescriptor<String, String> patternParam;
    private final ParameterDescriptor<String, String> localeParam;

    public ParseDate() {
        valueParam = ParameterDescriptor.string(VALUE).description("Date string to parse").build();
        patternParam = ParameterDescriptor.string(PATTERN).description("The pattern to parse the date with, see http://www.joda.org/joda-time/apidocs/org/joda/time/format/DateTimeFormat.html").build();
        localeParam = ParameterDescriptor.string(LOCALE).optional().description("The locale to parse the date with, see https://docs.oracle.com/javase/8/docs/api/java/util/Locale.html").build();
    }

    @Override
    protected String getName() {
        return NAME;
    }

    @Override
    protected ImmutableList<ParameterDescriptor> params() {
        return ImmutableList.of(
                valueParam,
                patternParam,
                localeParam
        );
    }

    @Override
    public DateTime evaluate(FunctionArgs args, EvaluationContext context, DateTimeZone timezone) {
        final String dateString = valueParam.required(args, context);
        final String pattern = patternParam.required(args, context);
        final Optional<String> localeString = localeParam.optional(args, context);

        if (dateString == null || pattern == null) {
            return null;
        }

        final Locale locale = localeString.map(Locale::forLanguageTag).orElse(Locale.getDefault());

        final DateTimeFormatter formatter = DateTimeFormat
                .forPattern(pattern)
                .withLocale(locale)
                .withZone(timezone);

        return formatter.parseDateTime(dateString);
    }

    @Override
    protected String description() {
        return "Parses a date string using the given date format";
    }
}
