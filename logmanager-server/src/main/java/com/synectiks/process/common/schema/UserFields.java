/*
 * */
package com.synectiks.process.common.schema;

public class UserFields {
    public static final String USER = "user";
    public static final String USER_COMMAND = "user_command";
    public static final String USER_COMMAND_PATH = "user_command_path";
    public static final String USER_DOMAIN = "user_domain";
    public static final String USER_EMAIL = "user_email";
    public static final String USER_ID = "user_id";
    public static final String USER_NAME = "user_name";
    public static final String USER_SESSION_ID = "user_session_id";

    // Derived and Enriched Fields
    public static final String USER_CATEGORY = "user_category";
    public static final String USER_NAME_MAPPED = "user_name_mapped";
    public static final String USER_PRIORITY = "user_priority";
    public static final String USER_PRIORITY_LEVEL = "user_priority_level";
    public static final String USER_TYPE = "user_type";

    // Target User Fields
    private static final String TARGET_PREFIX = "target_";

    public static final String TARGET_USER = TARGET_PREFIX + USER;
    public static final String TARGET_USER_EMAIL = TARGET_PREFIX + USER_EMAIL;
    public static final String TARGET_USER_ID = TARGET_PREFIX + USER_ID;
    public static final String TARGET_USER_NAME = TARGET_PREFIX + USER_NAME;
}
