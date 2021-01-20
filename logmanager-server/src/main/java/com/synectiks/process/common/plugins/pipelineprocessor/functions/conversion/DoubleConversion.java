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
package com.synectiks.process.common.plugins.pipelineprocessor.functions.conversion;

import com.google.common.primitives.Doubles;
import com.synectiks.process.common.plugins.pipelineprocessor.EvaluationContext;
import com.synectiks.process.common.plugins.pipelineprocessor.ast.functions.AbstractFunction;
import com.synectiks.process.common.plugins.pipelineprocessor.ast.functions.FunctionArgs;
import com.synectiks.process.common.plugins.pipelineprocessor.ast.functions.FunctionDescriptor;
import com.synectiks.process.common.plugins.pipelineprocessor.ast.functions.ParameterDescriptor;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.collect.ImmutableList.of;
import static com.synectiks.process.common.plugins.pipelineprocessor.ast.functions.ParameterDescriptor.floating;
import static com.synectiks.process.common.plugins.pipelineprocessor.ast.functions.ParameterDescriptor.object;

public class DoubleConversion extends AbstractFunction<Double> {

    public static final String NAME = "to_double";

    private static final String VALUE = "value";
    private static final String DEFAULT = "default";
    private final ParameterDescriptor<Object, Object> valueParam;
    private final ParameterDescriptor<Double, Double> defaultParam;

    public DoubleConversion() {
        valueParam = object(VALUE).description("Value to convert").build();
        defaultParam = floating(DEFAULT).optional().description("Used when 'value' is null, defaults to 0").build();
    }

    @Override
    public Double evaluate(FunctionArgs args, EvaluationContext context) {
        final Object evaluated = valueParam.required(args, context);
        final Double defaultValue = defaultParam.optional(args, context).orElse(0d);

        if (evaluated == null) {
            return defaultValue;
        } else if (evaluated instanceof Number) {
            return ((Number) evaluated).doubleValue();
        } else {
            final String s = String.valueOf(evaluated);
            return firstNonNull(Doubles.tryParse(s), defaultValue);
        }
    }

    @Override
    public FunctionDescriptor<Double> descriptor() {
        return FunctionDescriptor.<Double>builder()
                .name(NAME)
                .returnType(Double.class)
                .params(of(
                        valueParam,
                        defaultParam
                ))
                .description("Converts a value to a double value using its string representation")
                .build();
    }
}
