/*
 * */
package com.synectiks.process.server.contentpacks.facades;

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;
import com.synectiks.process.server.contentpacks.EntityDescriptorIds;
import com.synectiks.process.server.contentpacks.model.entities.Entity;
import com.synectiks.process.server.contentpacks.model.entities.EntityDescriptor;
import com.synectiks.process.server.contentpacks.model.entities.EntityExcerpt;
import com.synectiks.process.server.contentpacks.model.entities.NativeEntity;
import com.synectiks.process.server.contentpacks.model.entities.NativeEntityDescriptor;
import com.synectiks.process.server.contentpacks.model.entities.references.ValueReference;
import com.synectiks.process.server.plugin.indexer.searches.timeranges.InvalidRangeParametersException;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface EntityFacade<T> {
    /**
     * Create an exportable model of a native entity referenced by an {@link EntityDescriptor}
     * including optional constraints.
     *
     * @param entityDescriptor the descriptor of the native entity to export
     * @param entityDescriptorIds the IDs for all entity descriptors
     * @return an exportable (serializable) model of the entity including optional constraints,
     * or {@link Optional#empty()} if the entity couldn't be found.
     */
    Optional<Entity> exportEntity(EntityDescriptor entityDescriptor, EntityDescriptorIds entityDescriptorIds);

    /**
     * Create a native entity of type {@code T} from an entity model.
     *
     * @param entity         the entity model from which a native entity should be created
     * @param parameters     user-provided parameters to resolve parameters in the entity model
     * @param nativeEntities existing native entities to reference during the creation of the native entity
     * @param username       the name of the user creating the entity
     * @return the created native entity wrapped in {@link NativeEntity<T>}
     * @see Entity
     * @see NativeEntity
     */
    NativeEntity<T> createNativeEntity(Entity entity,
                                       Map<String, ValueReference> parameters,
                                       Map<EntityDescriptor, Object> nativeEntities,
                                       String username) throws InvalidRangeParametersException;

    /**
     * Find an existing instance of the native entity described by the entity model.
     *
     * @param entity     the entity model from which a native entity should be created
     * @param parameters user-provided parameters to resolve parameters in the entity model
     * @return the existing native entity in the database wrapped in {@link NativeEntity<T>},
     * or {@link Optional#empty()} if the entity couldn't be found.
     * @see Entity
     * @see NativeEntity
     */
    default Optional<NativeEntity<T>> findExisting(Entity entity, Map<String, ValueReference> parameters) {
        return Optional.empty();
    }

    /**
     * Loads the native entity instance for the given native entity descriptor.
     *
     * @param nativeEntityDescriptor the native entity descriptor
     * @return the existing native entity in the database wrapped in {@link NativeEntity<T>},
     * or {@link Optional#empty()} if the native entity doesn't exist.
     */
    Optional<NativeEntity<T>> loadNativeEntity(NativeEntityDescriptor nativeEntityDescriptor);

    /**
     * Delete the given native entity.
     *
     * @param nativeEntity The native entity to delete
     */
    void delete(T nativeEntity);

    /**
     * Create an excerpt (id, type, title) of a native entity for display purposes.
     *
     * @param nativeEntity The native entity to create an excerpt of
     * @return The entity excerpt of the native entity
     * @see EntityExcerpt
     */
    EntityExcerpt createExcerpt(T nativeEntity);

    /**
     * Create entity excerpts of all native entities of type {@code T}.
     *
     * @return A collection of entity excerpts of all native entities of type {@code T}
     * @see EntityExcerpt
     */
    Set<EntityExcerpt> listEntityExcerpts();

    /**
     * Create the dependency graph of a native entity described by the given entity descriptor.
     *
     * @param entityDescriptor the descriptor of the native entity to resolve dependencies for
     * @return A directed graph of the native entity with entity descriptors as nodes.
     * @see Graph
     */
    default Graph<EntityDescriptor> resolveNativeEntity(EntityDescriptor entityDescriptor) {
        final MutableGraph<EntityDescriptor> mutableGraph = GraphBuilder.directed().build();
        mutableGraph.addNode(entityDescriptor);
        return ImmutableGraph.copyOf(mutableGraph);
    }

    /**
     * Create the dependency graph of an entity described by the given entity model during content pack installation.
     *
     * @param entity the entity model to resolve dependencies for
     * @return A directed graph of the native entity with entity models as nodes.
     * @see Graph
     */
    default Graph<Entity> resolveForInstallation(Entity entity,
                                                 Map<String, ValueReference> parameters,
                                                 Map<EntityDescriptor, Entity> entities) {
        final MutableGraph<Entity> mutableGraph = GraphBuilder.directed().build();
        mutableGraph.addNode(entity);
        return ImmutableGraph.copyOf(mutableGraph);
    }
}
