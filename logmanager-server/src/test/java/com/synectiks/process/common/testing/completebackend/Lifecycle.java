/*
 * */
package com.synectiks.process.common.testing.completebackend;

/**
 * Controls the lifecycle of the {@link GraylogBackend} used in tests
 */
public enum Lifecycle {
    /**
     * {@link GraylogBackend} will be reused for all tests in a class. Use this, if you can make sure
     * that the individual tests will not interfere with each other, e.g., by creating test data that
     * would affect the outcome of a different test.
     */
    CLASS,
    /**
     * A fresh {@link GraylogBackend} will be instantiated for each tests in a class. This is the safest
     * way to isolate tests. Test execution will take much longer due to the time it takes to spin up
     * the necessary container, especially the server node itself.
     */
    METHOD {
        @Override
        void afterEach(GraylogBackend backend) {
            backend.fullReset();
        }
    };

    void afterEach(GraylogBackend backend) {
    }
}
