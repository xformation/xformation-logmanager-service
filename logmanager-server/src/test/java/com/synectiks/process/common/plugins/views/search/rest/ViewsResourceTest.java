/*
 * */
package com.synectiks.process.common.plugins.views.search.rest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.synectiks.process.common.plugins.views.search.rest.ViewsResource;
import com.synectiks.process.common.plugins.views.search.rest.ViewsRestPermissions;
import com.synectiks.process.common.plugins.views.search.views.ViewDTO;
import com.synectiks.process.common.plugins.views.search.views.ViewService;
import com.synectiks.process.common.security.UserContext;
import com.synectiks.process.server.dashboards.events.DashboardDeletedEvent;
import com.synectiks.process.server.events.ClusterEventBus;
import com.synectiks.process.server.plugin.database.users.User;
import com.synectiks.process.server.security.PasswordAlgorithmFactory;
import com.synectiks.process.server.shared.bindings.GuiceInjectorHolder;
import com.synectiks.process.server.shared.security.Permissions;
import com.synectiks.process.server.shared.users.UserService;
import com.synectiks.process.server.users.UserImpl;

import org.apache.shiro.subject.Subject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.annotation.Nullable;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ViewsResourceTest {
    @Before
    public void setUpInjector() {
        GuiceInjectorHolder.createInjector(Collections.emptyList());
    }

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Mock
    private Subject subject;

    @Mock
    private User currentUser;

    @Mock
    private ViewService viewService;

    @Mock
    private ViewDTO view;

    @Mock
    private ClusterEventBus clusterEventBus;

    @Mock
    private UserService userService;

    private ViewsResource viewsResource;

    class ViewsTestResource extends ViewsResource {
        ViewsTestResource(ViewService viewService, ClusterEventBus clusterEventBus, UserService userService) {
            super(viewService, clusterEventBus);
            this.userService = userService;
        }

        @Override
        protected Subject getSubject() {
            return subject;
        }

        @Nullable
        @Override
        protected User getCurrentUser() {
            return currentUser;
        }
    }

    @Before
    public void setUp() throws Exception {
        this.viewsResource = new ViewsTestResource(viewService, clusterEventBus, userService);
        when(subject.isPermitted("dashboards:create")).thenReturn(true);
    }

    @Test
    public void creatingViewAddsCurrentUserAsOwner() throws Exception {
        final ViewDTO.Builder builder = mock(ViewDTO.Builder.class);

        when(view.toBuilder()).thenReturn(builder);
        when(view.type()).thenReturn(ViewDTO.Type.DASHBOARD);
        when(builder.owner(any())).thenReturn(builder);
        when(builder.build()).thenReturn(view);

        final UserImpl testUser = new UserImpl(mock(PasswordAlgorithmFactory.class), new Permissions(ImmutableSet.of()), ImmutableMap.of("username", "testuser"));

        final UserContext userContext = mock(UserContext.class);
        when(userContext.getUser()).thenReturn(testUser);
        when(userContext.getUserId()).thenReturn("testuser");
        when(currentUser.getName()).thenReturn("testuser");
        when(currentUser.isLocalAdmin()).thenReturn(true);

        this.viewsResource.create(view, userContext);

        final ArgumentCaptor<String> ownerCaptor = ArgumentCaptor.forClass(String.class);
        verify(builder, times(1)).owner(ownerCaptor.capture());
        assertThat(ownerCaptor.getValue()).isEqualTo("testuser");
    }

    @Test
    public void shouldNotCreateADashboardWithoutPermission() {
        when(view.type()).thenReturn(ViewDTO.Type.DASHBOARD);

        when(subject.isPermitted("dashboards:create")).thenReturn(false);

        assertThatThrownBy(() -> this.viewsResource.create(view, null))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    public void invalidObjectIdReturnsViewNotFoundException() {
        expectedException.expect(NotFoundException.class);
        this.viewsResource.get("invalid");
    }

    @Test
    public void deletingDashboardTriggersEvent() {
        final String viewId = "foobar";
        when(subject.isPermitted(ViewsRestPermissions.VIEW_DELETE + ":" + viewId)).thenReturn(true);
        when(view.type()).thenReturn(ViewDTO.Type.DASHBOARD);
        when(view.id()).thenReturn(viewId);
        when(viewService.get(viewId)).thenReturn(Optional.of(view));
        when(userService.loadAll()).thenReturn(Collections.emptyList());

        this.viewsResource.delete(viewId);

        final ArgumentCaptor<DashboardDeletedEvent> eventCaptor = ArgumentCaptor.forClass(DashboardDeletedEvent.class);
        verify(clusterEventBus, times(1)).post(eventCaptor.capture());
        final DashboardDeletedEvent dashboardDeletedEvent = eventCaptor.getValue();

        assertThat(dashboardDeletedEvent.dashboardId()).isEqualTo("foobar");
    }
}
