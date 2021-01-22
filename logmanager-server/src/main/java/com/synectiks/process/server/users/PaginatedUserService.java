/*
 * */
package com.synectiks.process.server.users;

import org.mongojack.DBQuery;
import org.mongojack.DBSort;

import com.synectiks.process.server.bindings.providers.MongoJackObjectMapperProvider;
import com.synectiks.process.server.database.MongoConnection;
import com.synectiks.process.server.database.PaginatedDbService;
import com.synectiks.process.server.database.PaginatedList;
import com.synectiks.process.server.search.SearchQuery;

import javax.inject.Inject;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.http.util.TextUtils.isBlank;

public class PaginatedUserService extends PaginatedDbService<UserOverviewDTO> {
    private static final String COLLECTION_NAME = "users";

    @Inject
    public PaginatedUserService(MongoConnection mongoConnection,
                                MongoJackObjectMapperProvider mapper) {
        super(mongoConnection, mapper, UserOverviewDTO.class, COLLECTION_NAME);
    }

    public long count() {
        return db.count();
    }

    public PaginatedList<UserOverviewDTO> findPaginated(SearchQuery searchQuery, int page,
                                                        int perPage, String sortField, String order) {
        final DBQuery.Query dbQuery = searchQuery.toDBQuery();
        final DBSort.SortBuilder sortBuilder = getSortBuilder(order, sortField);
        return findPaginatedWithQueryAndSort(dbQuery, sortBuilder, page, perPage);
    }

    public PaginatedList<UserOverviewDTO> findPaginatedByUserId(SearchQuery searchQuery, int page,
                                                                int perPage, String sortField, String order,
                                                                Set<String> userIds) {
        final DBQuery.Query dbQuery = searchQuery.toDBQuery()
                .in("_id", userIds);
        final DBSort.SortBuilder sortBuilder = getSortBuilder(order, sortField);
        return findPaginatedWithQueryAndSort(dbQuery, sortBuilder, page, perPage);
    }

    public PaginatedList<UserOverviewDTO> findPaginatedByRole(SearchQuery searchQuery, int page,
                                                              int perPage, String sortField, String order,
                                                              Set<String> roleIds) {
        final DBQuery.Query dbQuery = searchQuery.toDBQuery()
                .in(UserImpl.ROLES, roleIds);
        final DBSort.SortBuilder sortBuilder = getSortBuilder(order, sortField);
        return findPaginatedWithQueryAndSort(dbQuery, sortBuilder, page, perPage);
    }

    public PaginatedList<UserOverviewDTO> findPaginatedByAuthServiceBackend(SearchQuery searchQuery,
                                                                            int page,
                                                                            int perPage,
                                                                            String sortField,
                                                                            String order,
                                                                            String authServiceBackendId) {
        checkArgument(!isBlank(authServiceBackendId), "authServiceBackendId cannot be blank");

        final DBQuery.Query query = DBQuery.and(
                DBQuery.is(UserImpl.AUTH_SERVICE_ID, Optional.of(authServiceBackendId)),
                searchQuery.toDBQuery()
        );
        return findPaginatedWithQueryAndSort(query, getSortBuilder(order, sortField), page, perPage);
    }
}
