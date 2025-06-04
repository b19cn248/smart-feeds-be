package com.olh.feeds.service.n8n;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class N8nConnection {
    private String node;
    private String type;
    private Integer index;
}
