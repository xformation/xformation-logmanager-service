/*
 * */
package com.synectiks.process.common.schema;

/**
 * Field names used in the standard logmanager Schema.
 *
 * @deprecated Please use the appropriate enums in this package rather than this collection of strings
 */
@Deprecated
public class GraylogSchemaFields {

    public static final String FIELD_TIMESTAMP = "timestamp";

    public static final String FIELD_ILLUMINATE_EVENT_CATEGORY = "xflog_event_category";
    public static final String FIELD_ILLUMINATE_EVENT_SUBCATEGORY = "xflog_event_subcategory";
    public static final String FIELD_ILLUMINATE_EVENT_TYPE = "xflog_event_type";
    public static final String FIELD_ILLUMINATE_EVENT_TYPE_CODE = "xflog_event_type_code";
    public static final String FIELD_ILLUMINATE_TAGS = "xflog_tags";
}
