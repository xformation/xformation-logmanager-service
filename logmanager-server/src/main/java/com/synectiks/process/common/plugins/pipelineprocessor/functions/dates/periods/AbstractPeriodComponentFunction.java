/*
 * */
package com.synectiks.process.common.plugins.pipelineprocessor.functions.dates.periods;

import com.google.common.primitives.Ints;
import com.synectiks.process.common.plugins.pipelineprocessor.EvaluationContext;
import com.synectiks.process.common.plugins.pipelineprocessor.ast.functions.AbstractFunction;
import com.synectiks.process.common.plugins.pipelineprocessor.ast.functions.FunctionArgs;
import com.synectiks.process.common.plugins.pipelineprocessor.ast.functions.FunctionDescriptor;
import com.synectiks.process.common.plugins.pipelineprocessor.ast.functions.ParameterDescriptor;

import org.joda.time.Period;

import javax.annotation.Nonnull;

public abstract class AbstractPeriodComponentFunction extends AbstractFunction<Period> {

    private final ParameterDescriptor<Long, Period> value =
            ParameterDescriptor
                    .integer("value", Period.class)
                    .transform(this::getPeriodOfInt)
                    .build();

    private Period getPeriodOfInt(long period) {
        return getPeriod(Ints.saturatedCast(period));
    }

    @Nonnull
    protected abstract Period getPeriod(int period);

    @Override
    public Period evaluate(FunctionArgs args, EvaluationContext context) {
        return value.required(args, context);
    }

    @Override
    public FunctionDescriptor<Period> descriptor() {
        return FunctionDescriptor.<Period>builder()
                .name(getName())
                .description(getDescription())
                .pure(true)
                .returnType(Period.class)
                .params(value)
                .build();
    }

    @Nonnull
    protected abstract String getName();

    @Nonnull
    protected abstract String getDescription();
}
