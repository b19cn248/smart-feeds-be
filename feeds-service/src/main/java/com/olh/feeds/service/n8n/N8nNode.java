package com.olh.feeds.service.n8n;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class N8nNode {
    private String id;
    private String name;
    private String type;
    private Integer typeVersion;
    private int[] position;
    private Map<String, Object> parameters;
}
