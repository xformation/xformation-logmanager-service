/*
 * */
package com.synectiks.process.server.rest.models.system.indexer.responses;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.synectiks.process.server.rest.models.count.responses.MessageCountResponse;
import com.synectiks.process.server.rest.models.system.deflector.responses.DeflectorSummary;

import org.graylog.autovalue.WithBeanGetter;

import java.util.Map;

@AutoValue
@WithBeanGetter
@JsonAutoDetect
public abstract class IndexerOverview {
    @JsonProperty("deflector")
    public abstract DeflectorSummary deflectorSummary();

    @JsonProperty("indexer_cluster")
    public abstract IndexerClusterOverview indexerCluster();

    @JsonProperty("counts")
    public abstract MessageCountResponse messageCountResponse();

    @JsonProperty("indices")
    public abstract Map<String, IndexSummary> indices();

    @JsonCreator
    public static IndexerOverview create(@JsonProperty("deflector_summary") DeflectorSummary deflectorSummary,
                                         @JsonProperty("indexer_cluster") IndexerClusterOverview indexerCluster,
                                         @JsonProperty("counts") MessageCountResponse messageCountResponse,
                                         @JsonProperty("indices") Map<String, IndexSummary> indices) {
        return new AutoValue_IndexerOverview(deflectorSummary, indexerCluster, messageCountResponse, indices);
    }
}
