/*
 * */
package com.synectiks.process.server.database;

import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryListener;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoCommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * MongoDB upsert requests can fail when they are creating a new entry concurrently.
 * https://jira.mongodb.org/browse/SERVER-14322
 * This helper can be used to retry upserts if they throw a {@link DuplicateKeyException}
 */
public class MongoDBUpsertRetryer {
    private static final Logger LOG = LoggerFactory.getLogger(MongoDBUpsertRetryer.class);

    public static <T> T run(Callable<T> c) {
        final Retryer<T> retryer = RetryerBuilder.<T>newBuilder()
                .retryIfException(t -> t instanceof DuplicateKeyException && ((DuplicateKeyException) t).getErrorCode() == 11000 ||
                        t instanceof MongoCommandException && ((MongoCommandException) t).getErrorCode() == 11000)
                .withStopStrategy(StopStrategies.stopAfterAttempt(2))
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        if (attempt.hasException()) {
                            LOG.debug("Upsert failed with {}. Retrying request", attempt.getExceptionCause().toString());
                        }
                    }
                })
                .build();
        try {
            return retryer.call(c);
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        } catch (RetryException e) {
            if (e.getCause() instanceof DuplicateKeyException) {
                throw (DuplicateKeyException) e.getCause();
            }
            throw new RuntimeException(e.getCause());
        }
    }
}
