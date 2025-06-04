package com.olh.feeds.service.facade.impl;

import com.olh.feeds.dto.request.source.AddSourceRequest;
import com.olh.feeds.service.SourceService;
import com.olh.feeds.service.facade.SourceFacadeService;
import com.olh.feeds.service.n8n.AddRssNodeRequest;
import com.olh.feeds.service.n8n.CodeNodeSelectionStrategy;
import com.olh.feeds.service.n8n.EnhancedN8nWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class SourceFacadeServiceImpl implements SourceFacadeService {

    private final SourceService sourceService;
    private final EnhancedN8nWorkflowService enhancedN8nWorkflowService;

    @Override
    @Transactional
    public void addSource(AddSourceRequest addSourceRequest) {

        enhancedN8nWorkflowService.addRssNodeToWorkflow(
                "F8rfMBLeoEmOeQV0",
                AddRssNodeRequest.builder()
                        .nodeName(addSourceRequest.getName())
                        .feedUrl(addSourceRequest.getUrl())
                        .targetCodeNodeName("CODE TRANSFER")
                        .selectionStrategy(CodeNodeSelectionStrategy.BY_NAME)
                        .build()
        );

        sourceService.addSource(addSourceRequest);
    }
}
