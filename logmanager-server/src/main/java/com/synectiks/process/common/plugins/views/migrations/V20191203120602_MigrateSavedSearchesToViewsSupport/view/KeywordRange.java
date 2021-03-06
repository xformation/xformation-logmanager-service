/*
 * */
package com.synectiks.process.common.plugins.views.migrations.V20191203120602_MigrateSavedSearchesToViewsSupport.view;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class KeywordRange extends TimeRange {
    static final String KEYWORD = "keyword";

    @JsonProperty
    @Override
    public abstract String type();

    @JsonProperty
    abstract String keyword();

    @JsonCreator
    static KeywordRange create(@JsonProperty("type") String type, @JsonProperty("keyword") String keyword) {
        return builder().type(type).keyword(keyword).build();
    }

    public static KeywordRange create(String keyword) {
        return create(KEYWORD, keyword);
    }

    private static Builder builder() {
        return new AutoValue_KeywordRange.Builder();
    }

    String getKeyword() {
        return keyword();
    }

    @AutoValue.Builder
    abstract static class Builder {
        abstract Builder type(String type);

        abstract Builder keyword(String keyword);

        abstract String keyword();

        abstract KeywordRange build();
    }
}

