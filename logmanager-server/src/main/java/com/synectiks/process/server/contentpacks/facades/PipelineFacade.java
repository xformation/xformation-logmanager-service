/*
 * */
package com.synectiks.process.server.contentpacks.facades;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;
import com.synectiks.process.common.plugins.pipelineprocessor.ast.Pipeline;
import com.synectiks.process.common.plugins.pipelineprocessor.ast.Stage;
import com.synectiks.process.common.plugins.pipelineprocessor.db.PipelineDao;
import com.synectiks.process.common.plugins.pipelineprocessor.db.PipelineService;
import com.synectiks.process.common.plugins.pipelineprocessor.db.PipelineStreamConnectionsService;
import com.synectiks.process.common.plugins.pipelineprocessor.db.RuleDao;
import com.synectiks.process.common.plugins.pipelineprocessor.db.RuleService;
import com.synectiks.process.common.plugins.pipelineprocessor.parser.PipelineRuleParser;
import com.synectiks.process.common.plugins.pipelineprocessor.rest.PipelineConnections;
import com.synectiks.process.server.contentpacks.EntityDescriptorIds;
import com.synectiks.process.server.contentpacks.exceptions.MissingNativeEntityException;
import com.synectiks.process.server.contentpacks.model.ModelId;
import com.synectiks.process.server.contentpacks.model.ModelType;
import com.synectiks.process.server.contentpacks.model.ModelTypes;
import com.synectiks.process.server.contentpacks.model.entities.Entity;
import com.synectiks.process.server.contentpacks.model.entities.EntityDescriptor;
import com.synectiks.process.server.contentpacks.model.entities.EntityExcerpt;
import com.synectiks.process.server.contentpacks.model.entities.EntityV1;
import com.synectiks.process.server.contentpacks.model.entities.NativeEntity;
import com.synectiks.process.server.contentpacks.model.entities.NativeEntityDescriptor;
import com.synectiks.process.server.contentpacks.model.entities.PipelineEntity;
import com.synectiks.process.server.contentpacks.model.entities.references.ValueReference;
import com.synectiks.process.server.database.NotFoundException;
import com.synectiks.process.server.plugin.Tools;
import com.synectiks.process.server.plugin.streams.Stream;
import com.synectiks.process.server.streams.StreamService;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class PipelineFacade implements EntityFacade<PipelineDao> {
    private static final Logger LOG = LoggerFactory.getLogger(PipelineFacade.class);

    public static final ModelType TYPE_V1 = ModelTypes.PIPELINE_V1;

    private final ObjectMapper objectMapper;
    private final PipelineService pipelineService;
    private final PipelineStreamConnectionsService connectionsService;
    private final PipelineRuleParser pipelineRuleParser;
    private final RuleService ruleService;
    private final StreamService streamService;

    @Inject
    public PipelineFacade(ObjectMapper objectMapper,
                          PipelineService pipelineService,
                          PipelineStreamConnectionsService connectionsService,
                          PipelineRuleParser pipelineRuleParser,
                          RuleService rulesService,
                          StreamService streamService
    ) {
        this.objectMapper = objectMapper;
        this.pipelineService = pipelineService;
        this.connectionsService = connectionsService;
        this.pipelineRuleParser = pipelineRuleParser;
        this.ruleService = rulesService;
        this.streamService = streamService;
    }

    @VisibleForTesting
    Entity exportNativeEntity(PipelineDao pipelineDao, EntityDescriptorIds entityDescriptorIds) {
        final Set<ValueReference> connectedStreams = connectedStreams(pipelineDao.id(), entityDescriptorIds);
        final PipelineEntity pipelineEntity = PipelineEntity.create(
                ValueReference.of(pipelineDao.title()),
                ValueReference.of(pipelineDao.description()),
                ValueReference.of(pipelineDao.source()),
                connectedStreams);
        final JsonNode data = objectMapper.convertValue(pipelineEntity, JsonNode.class);
        return EntityV1.builder()
                .id(ModelId.of(entityDescriptorIds.getOrThrow(pipelineDao.id(), ModelTypes.PIPELINE_V1)))
                .type(ModelTypes.PIPELINE_V1)
                .data(data)
                .build();
    }

    private Set<ValueReference> connectedStreams(String pipelineId, EntityDescriptorIds entityDescriptorIds) {
        final Set<PipelineConnections> connections = connectionsService.loadByPipelineId(pipelineId);
        return connections.stream()
                .map(pipelineConnections -> entityDescriptorIds.getOrThrow(pipelineConnections.streamId(), ModelTypes.STREAM_V1))
                .map(ValueReference::of)
                .collect(Collectors.toSet());
    }

    @Override
    public NativeEntity<PipelineDao> createNativeEntity(Entity entity,
                                                        Map<String, ValueReference> parameters,
                                                        Map<EntityDescriptor, Object> nativeEntities,
                                                        String username) {
        if (entity instanceof EntityV1) {
            return decode((EntityV1) entity, parameters, nativeEntities);
        } else {
            throw new IllegalArgumentException("Unsupported entity version: " + entity.getClass());
        }
    }

    private NativeEntity<PipelineDao> decode(EntityV1 entity,
                                             Map<String, ValueReference> parameters,
                                             Map<EntityDescriptor, Object> nativeEntities) {
        final DateTime now = Tools.nowUTC();
        final PipelineEntity pipelineEntity = objectMapper.convertValue(entity.data(), PipelineEntity.class);
        final ValueReference description = pipelineEntity.description();
        final PipelineDao pipelineDao = PipelineDao.builder()
                .title(pipelineEntity.title().asString(parameters))
                .description(description == null ? null : description.asString(parameters))
                .source(pipelineEntity.source().asString(parameters))
                .createdAt(now)
                .modifiedAt(now)
                .build();
        final PipelineDao savedPipelineDao = pipelineService.save(pipelineDao);
        final String pipelineId = requireNonNull(savedPipelineDao.id(), "Saved pipeline ID must not be null");
        final Set<EntityDescriptor> connectedStreamEntities = pipelineEntity.connectedStreams().stream()
                .map(valueReference -> valueReference.asString(parameters))
                .map(streamId -> EntityDescriptor.create(streamId, ModelTypes.STREAM_V1))
                .collect(Collectors.toSet());
        final Set<Stream> connectedStreams = connectedStreams(connectedStreamEntities, nativeEntities);
        createPipelineConnections(pipelineId, connectedStreams);

        return NativeEntity.create(entity.id(), pipelineId, TYPE_V1, savedPipelineDao.title(), savedPipelineDao);
    }

    private Set<Stream> connectedStreams(Set<EntityDescriptor> connectedStreamEntities, Map<EntityDescriptor, Object> nativeEntities) {
        final ImmutableSet.Builder<Stream> streams = ImmutableSet.builder();
        for (EntityDescriptor descriptor : connectedStreamEntities) {
            final Object stream = nativeEntities.get(descriptor);
            if (stream instanceof Stream) {
                streams.add((Stream) stream);
            } else {
                if (EntityDescriptorIds.isDefaultStreamDescriptor(descriptor)) {
                    try {
                        streams.add(streamService.load(descriptor.id().id()));
                    }
                    catch (NotFoundException e) {
                        LOG.warn("Default stream {} not found!", descriptor.id().id(), e);
                        throw new MissingNativeEntityException(descriptor);
                    }
                }
                else {
                    throw new MissingNativeEntityException(descriptor);
                }
            }
        }
        return streams.build();
    }

    private void createPipelineConnections(String pipelineId, Set<Stream> connectedStreams) {
        for (Stream stream : connectedStreams) {
            final String streamId = stream.getId();
            try {
                final PipelineConnections connections = connectionsService.load(streamId);
                final Set<String> newPipelines = ImmutableSet.<String>builder()
                        .addAll(connections.pipelineIds())
                        .add(pipelineId)
                        .build();
                final PipelineConnections newConnections = connections.toBuilder()
                        .pipelineIds(newPipelines)
                        .build();
                final PipelineConnections savedConnections = connectionsService.save(newConnections);
                LOG.trace("Saved pipeline connections: {}", savedConnections);
            } catch (NotFoundException e) {
                final PipelineConnections connections = PipelineConnections.builder()
                        .streamId(streamId)
                        .pipelineIds(Collections.singleton(pipelineId))
                        .build();
                final PipelineConnections savedConnections = connectionsService.save(connections);
                LOG.trace("Saved pipeline connections: {}", savedConnections);
            }
        }
    }

    @Override
    public Optional<NativeEntity<PipelineDao>> loadNativeEntity(NativeEntityDescriptor nativeEntityDescriptor) {
        try {
            final PipelineDao pipeline = pipelineService.load(nativeEntityDescriptor.id().id());
            return Optional.of(NativeEntity.create(nativeEntityDescriptor, pipeline));
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public void delete(PipelineDao nativeEntity) {
        final Set<PipelineConnections> pipelineConnections = connectionsService.loadByPipelineId(nativeEntity.id());
        for (PipelineConnections connections : pipelineConnections) {
            final Set<String> pipelineIds = connections.pipelineIds().stream()
                    .filter(pipelineId -> !pipelineId.equals(nativeEntity.id()))
                    .collect(Collectors.toSet());

            if (pipelineIds.isEmpty()) {
                LOG.trace("Removing pipeline connections for stream {}", connections.streamId());
                connectionsService.delete(connections.streamId());
            } else {
                final PipelineConnections newConnections = connections.toBuilder()
                        .pipelineIds(pipelineIds)
                        .build();
                LOG.trace("Saving updated pipeline connections: {}", newConnections);
                connectionsService.save(newConnections);
            }
        }

        pipelineService.delete(nativeEntity.id());
    }

    @Override
    public EntityExcerpt createExcerpt(PipelineDao pipeline) {
        return EntityExcerpt.builder()
                .id(ModelId.of(pipeline.id()))
                .type(ModelTypes.PIPELINE_V1)
                .title(pipeline.title())
                .build();
    }

    @Override
    public Set<EntityExcerpt> listEntityExcerpts() {
        return pipelineService.loadAll().stream()
                .map(this::createExcerpt)
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<Entity> exportEntity(EntityDescriptor entityDescriptor, EntityDescriptorIds entityDescriptorIds) {
        final ModelId modelId = entityDescriptor.id();
        try {
            final PipelineDao pipelineDao = pipelineService.load(modelId.id());
            return Optional.of(exportNativeEntity(pipelineDao, entityDescriptorIds));
        } catch (NotFoundException e) {
            LOG.debug("Couldn't find pipeline {}", entityDescriptor, e);
            return Optional.empty();
        }
    }

    @Override
    public Graph<EntityDescriptor> resolveNativeEntity(EntityDescriptor entityDescriptor) {
        final MutableGraph<EntityDescriptor> mutableGraph = GraphBuilder.directed().build();
        mutableGraph.addNode(entityDescriptor);

        final ModelId modelId = entityDescriptor.id();
        try {
            final PipelineDao pipelineDao = pipelineService.load(modelId.id());
            final String pipelineSource = pipelineDao.source();
            final Collection<String> referencedRules = referencedRules(pipelineSource);
            referencedRules.stream()
                    .map(ModelId::of)
                    .map(id -> EntityDescriptor.create(id, ModelTypes.PIPELINE_RULE_V1))
                    .forEach(rule -> mutableGraph.putEdge(entityDescriptor, rule));

            final Set<PipelineConnections> pipelineConnections = connectionsService.loadByPipelineId(pipelineDao.id());
            pipelineConnections.stream()
                    .map(PipelineConnections::streamId)
                    .map(ModelId::of)
                    .map(id -> EntityDescriptor.create(id, ModelTypes.STREAM_V1))
                    .forEach(stream -> mutableGraph.putEdge(entityDescriptor, stream));
        } catch (NotFoundException e) {
            LOG.debug("Couldn't find pipeline {}", entityDescriptor, e);
        }

        return ImmutableGraph.copyOf(mutableGraph);
    }

    @Override
    public Graph<Entity> resolveForInstallation(Entity entity,
                                                Map<String, ValueReference> parameters,
                                                Map<EntityDescriptor, Entity> entities) {
        if (entity instanceof EntityV1) {
            return resolveForInstallation((EntityV1) entity, parameters, entities);
        } else {
            throw new IllegalArgumentException("Unsupported entity version: " + entity.getClass());
        }
    }

    private Graph<Entity> resolveForInstallation(EntityV1 entity,
                                                 Map<String, ValueReference> parameters,
                                                 Map<EntityDescriptor, Entity> entities) {
        final MutableGraph<Entity> mutableGraph = GraphBuilder.directed().build();
        mutableGraph.addNode(entity);

        final PipelineEntity pipelineEntity = objectMapper.convertValue(entity.data(), PipelineEntity.class);
        final String source = pipelineEntity.source().asString(parameters);
        final Collection<String> referencedRules = referencedRules(source);
        referencedRules.stream()
                .map(ModelId::of)
                .map(modelId -> EntityDescriptor.create(modelId, ModelTypes.PIPELINE_RULE_V1))
                .map(entities::get)
                .filter(Objects::nonNull)
                .forEach(ruleEntity -> mutableGraph.putEdge(entity, ruleEntity));

        pipelineEntity.connectedStreams().stream()
                .map(valueReference -> valueReference.asString(parameters))
                .map(ModelId::of)
                .map(modelId -> EntityDescriptor.create(modelId, ModelTypes.STREAM_V1))
                .map(entities::get)
                .filter(Objects::nonNull)
                .forEach(streamEntity -> mutableGraph.putEdge(entity, streamEntity));

        return ImmutableGraph.copyOf(mutableGraph);
    }

    private Collection<String> referencedRules(String pipelineSource) {
        final Pipeline pipeline = pipelineRuleParser.parsePipeline("dummy", pipelineSource);
        return pipeline.stages().stream()
                .map(Stage::ruleReferences)
                .flatMap(Collection::stream)
                .map(ruleService::findByName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(RuleDao::id)
                .collect(Collectors.toSet());
    }
}
