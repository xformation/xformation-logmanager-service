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

import org.junit.Test;

import com.synectiks.process.server.plugin.streams.StreamRuleType;
import com.synectiks.process.server.streams.StreamRuleMatcherFactory;
import com.synectiks.process.server.streams.matchers.AlwaysMatcher;
import com.synectiks.process.server.streams.matchers.ExactMatcher;
import com.synectiks.process.server.streams.matchers.FieldPresenceMatcher;
import com.synectiks.process.server.streams.matchers.GreaterMatcher;
import com.synectiks.process.server.streams.matchers.InputMatcher;
import com.synectiks.process.server.streams.matchers.RegexMatcher;
import com.synectiks.process.server.streams.matchers.SmallerMatcher;

import static org.assertj.core.api.Assertions.assertThat;

public class StreamRuleMatcherFactoryTest {
    @Test
    public void buildReturnsCorrectStreamRuleMatcher() throws Exception {
        assertThat(StreamRuleMatcherFactory.build(StreamRuleType.EXACT)).isInstanceOf(ExactMatcher.class);
        assertThat(StreamRuleMatcherFactory.build(StreamRuleType.REGEX)).isInstanceOf(RegexMatcher.class);
        assertThat(StreamRuleMatcherFactory.build(StreamRuleType.GREATER)).isInstanceOf(GreaterMatcher.class);
        assertThat(StreamRuleMatcherFactory.build(StreamRuleType.SMALLER)).isInstanceOf(SmallerMatcher.class);
        assertThat(StreamRuleMatcherFactory.build(StreamRuleType.PRESENCE)).isInstanceOf(FieldPresenceMatcher.class);
        assertThat(StreamRuleMatcherFactory.build(StreamRuleType.ALWAYS_MATCH)).isInstanceOf(AlwaysMatcher.class);
        assertThat(StreamRuleMatcherFactory.build(StreamRuleType.MATCH_INPUT)).isInstanceOf(InputMatcher.class);
    }
}
