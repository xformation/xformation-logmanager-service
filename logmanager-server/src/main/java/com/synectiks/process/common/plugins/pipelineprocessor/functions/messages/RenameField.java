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
package com.synectiks.process.common.plugins.pipelineprocessor.functions.messages;

import static com.synectiks.process.common.plugins.pipelineprocessor.ast.functions.ParameterDescriptor.string;
import static com.synectiks.process.common.plugins.pipelineprocessor.ast.functions.ParameterDescriptor.type;

import com.synectiks.process.common.plugins.pipelineprocessor.EvaluationContext;
import com.synectiks.process.common.plugins.pipelineprocessor.ast.functions.AbstractFunction;
import com.synectiks.process.common.plugins.pipelineprocessor.ast.functions.FunctionArgs;
import com.synectiks.process.common.plugins.pipelineprocessor.ast.functions.FunctionDescriptor;
import com.synectiks.process.common.plugins.pipelineprocessor.ast.functions.ParameterDescriptor;
import com.synectiks.process.server.plugin.Message;

public class RenameField extends AbstractFunction<Void> {

    public static final String NAME = "rename_field";

    private final ParameterDescriptor<String, String> oldFieldParam;
    private final ParameterDescriptor<String, String> newFieldParam;
    private final ParameterDescriptor<Message, Message> messageParam;

    public RenameField() {
        oldFieldParam = string("old_field").description("The old name of the field").build();
        newFieldParam = string("new_field").description("The new name of the field").build();
        messageParam = type("message", Message.class).optional().description("The message to use, defaults to '$message'").build();
    }

    @Override
    public Void evaluate(FunctionArgs args, EvaluationContext context) {
        final String oldName = oldFieldParam.required(args, context);
        final String newName = newFieldParam.required(args, context);

        // exit early if the field names are the same (so we don't drop the field)
        if (oldName != null && oldName.equals(newName)) {
            return null;
        }
        final Message message = messageParam.optional(args, context).orElse(context.currentMessage());

        if (message.hasField(oldName)) {
            message.addField(newName, message.getField(oldName));
            message.removeField(oldName);
        }

        return null;
    }

    @Override
    public FunctionDescriptor<Void> descriptor() {
        return FunctionDescriptor.<Void>builder()
                .name(NAME)
                .returnType(Void.class)
                .params(oldFieldParam, newFieldParam, messageParam)
                .description("Rename a message field")
                .build();
    }
}
