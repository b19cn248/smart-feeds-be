package com.olh.feeds.service.n8n;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class N8nWorkflowService {

    private final RestTemplate n8nRestTemplate;
    private final String n8nBaseUrl;
    private final ObjectMapper objectMapper;

    private static final String RSS_NODE_TYPE = "n8n-nodes-base.rssFeedReadTrigger";
    private static final String CODE_NODE_TYPE = "n8n-nodes-base.code";
    private static final int RSS_NODE_TYPE_VERSION = 1;

    public AddRssNodeResponse addRssNodeToWorkflow(String workflowId, AddRssNodeRequest request) {
        try {
            // Lấy thông tin workflow
            N8nWorkflow workflow = getWorkflow(workflowId);

            // Tìm node Code để kết nối
            Optional<N8nNode> codeNode = findNodeByType(workflow.getNodes(), CODE_NODE_TYPE);
            if (codeNode.isEmpty()) {
                return new AddRssNodeResponse(false,
                        "Code node not found in workflow", null, workflowId);
            }

            // Tạo RSS node mới
            N8nNode newRssNode = createRssNode(request.getNodeName(), request.getFeedUrl(), workflow.getNodes());

            // Thêm node vào workflow
            workflow.getNodes().add(newRssNode);

            // Cập nhật connections
            updateConnections(workflow, newRssNode.getName(), codeNode.get().getName());

            // Gửi cập nhật về n8n
            updateWorkflow(workflowId, workflow);

            return new AddRssNodeResponse(true,
                    "RSS node added successfully", newRssNode.getId(), workflowId);

        } catch (RestClientException e) {
            log.error("HTTP error adding RSS node to workflow {}: {}", workflowId, e.getMessage());
            return new AddRssNodeResponse(false,
                    "HTTP error: " + e.getMessage(), null, workflowId);
        } catch (Exception e) {
            log.error("Error adding RSS node to workflow {}: {}", workflowId, e.getMessage());
            return new AddRssNodeResponse(false,
                    "Error adding RSS node: " + e.getMessage(), null, workflowId);
        }
    }

    private N8nWorkflow getWorkflow(String workflowId) {
        String url = n8nBaseUrl + "/api/v1/workflows/" + workflowId;
        log.debug("Fetching workflow from URL: {}", url);

        try {
            N8nWorkflow workflow = n8nRestTemplate.getForObject(url, N8nWorkflow.class);
            if (workflow == null) {
                throw new RuntimeException("Workflow not found or response is null");
            }
            return workflow;
        } catch (RestClientException e) {
            log.error("Error fetching workflow {}: {}", workflowId, e.getMessage());
            throw new RuntimeException("Failed to fetch workflow: " + e.getMessage(), e);
        }
    }

    private N8nWorkflow updateWorkflow(String workflowId, N8nWorkflow workflow) {
        String url = n8nBaseUrl + "/api/v1/workflows/" + workflowId;
        log.debug("Updating workflow at URL: {}", url);

        // Tạo request body chỉ với các field cần thiết
        Map<String, Object> updateRequest = createUpdateRequest(workflow);

        try {
            n8nRestTemplate.put(url, updateRequest);
            log.info("Successfully updated workflow {}", workflowId);
            return workflow;
        } catch (RestClientException e) {
            log.error("Error updating workflow {}: {}", workflowId, e.getMessage());
            throw new RuntimeException("Failed to update workflow: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> createUpdateRequest(N8nWorkflow workflow) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", workflow.getName());
        request.put("nodes", workflow.getNodes());
        request.put("connections", workflow.getConnections());
        request.put("settings", workflow.getSettings());
        return request;
    }

    private Optional<N8nNode> findNodeByType(List<N8nNode> nodes, String nodeType) {
        return nodes.stream()
                .filter(node -> nodeType.equals(node.getType()))
                .findFirst();
    }

    private N8nNode createRssNode(String nodeName, String feedUrl, List<N8nNode> existingNodes) {
        // Tạo ID unique cho node mới
        String nodeId = generateUniqueNodeId(existingNodes);

        // Tính toán vị trí cho node mới (tránh trùng lặp)
        int[] position = calculateNewNodePosition(existingNodes);

        // Tạo parameters cho RSS node
        Map<String, Object> parameters = createRssNodeParameters(feedUrl);

        return new N8nNode(nodeId, nodeName, RSS_NODE_TYPE, RSS_NODE_TYPE_VERSION, position, parameters);
    }

    private Map<String, Object> createRssNodeParameters(String feedUrl) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("feedUrl", feedUrl);

        // Cấu hình pollTimes
        Map<String, Object> pollTimes = new HashMap<>();
        List<Map<String, String>> items = List.of(Map.of("mode", "everyMinute"));
        pollTimes.put("item", items);
        parameters.put("pollTimes", pollTimes);

        // Cấu hình options
        Map<String, Object> options = new HashMap<>();
        options.put("ignoreSSL", false);
        parameters.put("options", options);

        return parameters;
    }

    private String generateUniqueNodeId(List<N8nNode> existingNodes) {
        String baseId = "rss-node-" + System.currentTimeMillis();
        Set<String> existingIds = new HashSet<>();
        existingNodes.forEach(node -> existingIds.add(node.getId()));

        int counter = 1;
        String nodeId = baseId;
        while (existingIds.contains(nodeId)) {
            nodeId = baseId + "-" + counter++;
        }
        return nodeId;
    }

    private int[] calculateNewNodePosition(List<N8nNode> existingNodes) {
        // Tìm vị trí thấp nhất để đặt node mới
        int minY = existingNodes.stream()
                .mapToInt(node -> node.getPosition()[1])
                .min()
                .orElse(0);

        return new int[]{-280, minY + 200}; // Đặt node ở cột bên trái, cách node khác 200px
    }

    @SuppressWarnings("unchecked")
    private void updateConnections(N8nWorkflow workflow, String rssNodeName, String codeNodeName) {
        Map<String, Object> connections = workflow.getConnections();
        if (connections == null) {
            connections = new HashMap<>();
            workflow.setConnections(connections);
        }

        // Tạo connection từ RSS node mới đến Code node
        Map<String, Object> rssConnection = new HashMap<>();
        List<List<Map<String, Object>>> mainConnections = new ArrayList<>();
        List<Map<String, Object>> connectionList = new ArrayList<>();

        Map<String, Object> connection = new HashMap<>();
        connection.put("node", codeNodeName);
        connection.put("type", "main");
        connection.put("index", 0);
        connectionList.add(connection);

        mainConnections.add(connectionList);
        rssConnection.put("main", mainConnections);
        connections.put(rssNodeName, rssConnection);
    }
}
