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
package com.synectiks.process.server.streams;

import com.synectiks.process.server.plugin.streams.StreamRuleType;
import com.synectiks.process.server.streams.matchers.AlwaysMatcher;
import com.synectiks.process.server.streams.matchers.ContainsMatcher;
import com.synectiks.process.server.streams.matchers.ExactMatcher;
import com.synectiks.process.server.streams.matchers.FieldPresenceMatcher;
import com.synectiks.process.server.streams.matchers.GreaterMatcher;
import com.synectiks.process.server.streams.matchers.InputMatcher;
import com.synectiks.process.server.streams.matchers.RegexMatcher;
import com.synectiks.process.server.streams.matchers.SmallerMatcher;
import com.synectiks.process.server.streams.matchers.StreamRuleMatcher;

public class StreamRuleMatcherFactory {
    public static StreamRuleMatcher build(StreamRuleType ruleType) throws InvalidStreamRuleTypeException {
        switch (ruleType) {
            case EXACT:
                return new ExactMatcher();
            case REGEX:
                return new RegexMatcher();
            case GREATER:
                return new GreaterMatcher();
            case SMALLER:
                return new SmallerMatcher();
            case PRESENCE:
                return new FieldPresenceMatcher();
            case CONTAINS:
                return new ContainsMatcher();
            case ALWAYS_MATCH:
                return new AlwaysMatcher();
            case MATCH_INPUT:
                return new InputMatcher();
            default:
                throw new InvalidStreamRuleTypeException();
        }
    }
}
