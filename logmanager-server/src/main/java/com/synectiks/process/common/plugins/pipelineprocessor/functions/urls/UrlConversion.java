/*
 * */
package com.synectiks.process.common.plugins.pipelineprocessor.functions.urls;

import com.google.common.base.Throwables;
import com.synectiks.process.common.plugins.pipelineprocessor.EvaluationContext;
import com.synectiks.process.common.plugins.pipelineprocessor.ast.functions.AbstractFunction;
import com.synectiks.process.common.plugins.pipelineprocessor.ast.functions.FunctionArgs;
import com.synectiks.process.common.plugins.pipelineprocessor.ast.functions.FunctionDescriptor;
import com.synectiks.process.common.plugins.pipelineprocessor.ast.functions.ParameterDescriptor;

import java.net.MalformedURLException;
import java.util.Optional;

public class UrlConversion extends AbstractFunction<URL> {

    public static final String NAME = "to_url";

    private final ParameterDescriptor<Object, Object> urlParam = ParameterDescriptor.object("url").description("Value to convert").build();
    private final ParameterDescriptor<String, String> defaultParam = ParameterDescriptor.string("default").optional().description("Used when 'url' is null or malformed").build();

    @Override
    public URL evaluate(FunctionArgs args, EvaluationContext context) {
        final String urlString = String.valueOf(urlParam.required(args, context));
        try {
            return new URL(urlString);
        } catch (IllegalArgumentException e) {
            log.debug("Unable to parse URL for string {}", urlString, e);

            final Optional<String> defaultUrl = defaultParam.optional(args, context);
            if (!defaultUrl.isPresent()) {
                return null;
            }
            try {
                return new URL(defaultUrl.get());
            } catch (IllegalArgumentException e1) {
                log.warn("Parameter `default` for to_url() is not a valid URL: {}", defaultUrl.get());
                throw Throwables.propagate(e1);
            }
        }
    }

    @Override
    public FunctionDescriptor<URL> descriptor() {
        return FunctionDescriptor.<URL>builder()
                .name(NAME)
                .returnType(URL.class)
                .params(urlParam,
                        defaultParam)
                .description("Converts a value to a valid URL using its string representation")
                .build();
    }
}
