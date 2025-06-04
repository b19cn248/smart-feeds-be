package com.olh.feeds.service.n8n;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class N8nWorkflow {
    private String id;
    private String name;
    private boolean active;
    private List<N8nNode> nodes;
    private Map<String, Object> connections;
    private Map<String, Object> settings;
    private Object staticData;
    private Object meta;
    private Map<String, Object> pinData;
    private String versionId;
    private Integer triggerCount;
    private List<Object> shared;
    private List<Object> tags;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("updatedAt")
    private String updatedAt;
}
