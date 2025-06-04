package com.olh.feeds.service.n8n;

import com.olh.feeds.core.exception.base.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnhancedN8nWorkflowService {

    private final RestTemplate n8nRestTemplate;
    private final String n8nBaseUrl;

    private static final String RSS_NODE_TYPE = "n8n-nodes-base.rssFeedReadTrigger";
    private static final String CODE_NODE_TYPE = "n8n-nodes-base.code";
    private static final int RSS_NODE_TYPE_VERSION = 1;

    // ========== MAIN PUBLIC METHOD ==========

    public AddRssNodeResponse addRssNodeToWorkflow(String workflowId, AddRssNodeRequest request) {
        try {
            log.info("Starting RSS node addition to workflow {} with strategy: {}",
                    workflowId, request.getSelectionStrategy());

            // Lấy thông tin workflow
            N8nWorkflow workflow = getWorkflow(workflowId);

            // Chọn Code node theo strategy
            Optional<N8nNode> targetCodeNode = selectTargetCodeNode(workflow, request);

            if (targetCodeNode.isEmpty()) {
                return new AddRssNodeResponse(false,
                        "No suitable Code node found with the specified criteria",
                        null, workflowId, null, request.getSelectionStrategy().toString());
            }

            N8nNode selectedCodeNode = targetCodeNode.get();
            log.info("Selected Code node: '{}' (ID: {})", selectedCodeNode.getName(), selectedCodeNode.getId());

            // Tạo RSS node mới
            N8nNode newRssNode = createRssNode(request.getNodeName(), request.getFeedUrl(), workflow.getNodes());

            // Thêm node vào workflow
            workflow.getNodes().add(newRssNode);
            log.info("Added new RSS node: '{}' (ID: {})", newRssNode.getName(), newRssNode.getId());

            // Cập nhật connections
            updateConnections(workflow, newRssNode.getName(), selectedCodeNode.getName());
            log.info("Updated connections: {} -> {}", newRssNode.getName(), selectedCodeNode.getName());

            // Gửi cập nhật về n8n
            updateWorkflow(workflowId, workflow);

            return new AddRssNodeResponse(true,
                    String.format("RSS node '%s' connected to Code node '%s' successfully",
                            newRssNode.getName(), selectedCodeNode.getName()),
                    newRssNode.getId(), workflowId, selectedCodeNode.getName(),
                    request.getSelectionStrategy().toString());

        } catch (RestClientException e) {
            log.error("HTTP error adding RSS node to workflow {}: {}", workflowId, e.getMessage());
            return new AddRssNodeResponse(false,
                    "HTTP error: " + e.getMessage(), null, workflowId, null,
                    request.getSelectionStrategy().toString());
        } catch (Exception e) {
            log.error("Error adding RSS node to workflow {}: {}", workflowId, e.getMessage(), e);
            return new AddRssNodeResponse(false,
                    "Error adding RSS node: " + e.getMessage(), null, workflowId, null,
                    request.getSelectionStrategy().toString());
        }
    }

    // ========== CODE NODE SELECTION METHODS ==========

    private Optional<N8nNode> selectTargetCodeNode(N8nWorkflow workflow, AddRssNodeRequest request) {
        List<N8nNode> codeNodes = findAllCodeNodes(workflow.getNodes());

        if (codeNodes.isEmpty()) {
            log.warn("No Code nodes found in workflow {}", workflow.getId());
            return Optional.empty();
        }

        log.info("Found {} Code node(s) in workflow:", codeNodes.size());
        codeNodes.forEach(node -> log.info("  - '{}' (ID: {}, Position: [{}, {}])",
                node.getName(), node.getId(), node.getPosition()[0], node.getPosition()[1]));

        // Xử lý theo strategy
        Optional<N8nNode> selectedNode;
        switch (request.getSelectionStrategy()) {
            case BY_NAME:
                selectedNode = selectCodeNodeByName(codeNodes, request.getTargetCodeNodeName());
                break;

            case BY_ID:
                selectedNode = selectCodeNodeById(codeNodes, request.getTargetCodeNodeId());
                break;

            case NO_CONNECTIONS:
                selectedNode = selectCodeNodeWithoutConnections(codeNodes, workflow.getConnections());
                break;

            case LAST_CREATED:
                selectedNode = selectLastCreatedCodeNode(codeNodes);
                break;

            case CLOSEST_POSITION:
                selectedNode = selectClosestCodeNode(codeNodes, workflow.getNodes());
                break;

            case FIRST_AVAILABLE:
            default:
                selectedNode = codeNodes.stream().findFirst();
                log.info("Using FIRST_AVAILABLE strategy");
                break;
        }

        return selectedNode;
    }

    private List<N8nNode> findAllCodeNodes(List<N8nNode> nodes) {
        return nodes.stream()
                .filter(node -> CODE_NODE_TYPE.equals(node.getType()))
                .toList();
    }

    private Optional<N8nNode> selectCodeNodeByName(List<N8nNode> codeNodes, String targetName) {
        if (targetName == null || targetName.trim().isEmpty()) {
            log.warn("Target code node name is empty, falling back to first available");
            return codeNodes.stream().findFirst();
        }

        log.info("Searching for Code node with name: '{}'", targetName);

        // Exact match first
        Optional<N8nNode> exactMatch = codeNodes.stream()
                .filter(node -> targetName.equals(node.getName()))
                .findFirst();

        if (exactMatch.isPresent()) {
            log.info("Found exact match for Code node name: '{}'", targetName);
            return exactMatch;
        }

        // Fuzzy search if no exact match
        Optional<N8nNode> fuzzyMatch = codeNodes.stream()
                .filter(node -> node.getName().toLowerCase().contains(targetName.toLowerCase()))
                .findFirst();

        if (fuzzyMatch.isPresent()) {
            log.info("Found fuzzy match for Code node name: '{}' -> '{}'",
                    targetName, fuzzyMatch.get().getName());
            return fuzzyMatch;
        }

        log.warn("No Code node found with name containing '{}', falling back to first available", targetName);
        return codeNodes.stream().findFirst();
    }

    private Optional<N8nNode> selectCodeNodeById(List<N8nNode> codeNodes, String targetId) {
        if (targetId == null || targetId.trim().isEmpty()) {
            log.warn("Target code node ID is empty, falling back to first available");
            return codeNodes.stream().findFirst();
        }

        log.info("Searching for Code node with ID: '{}'", targetId);

        Optional<N8nNode> result = codeNodes.stream()
                .filter(node -> targetId.equals(node.getId()))
                .findFirst();

        if (result.isPresent()) {
            log.info("Found Code node with ID: '{}'", targetId);
        } else {
            log.warn("No Code node found with ID '{}', falling back to first available", targetId);
            result = codeNodes.stream().findFirst();
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private Optional<N8nNode> selectCodeNodeWithoutConnections(List<N8nNode> codeNodes, Map<String, Object> connections) {
        log.info("Searching for Code node without existing connections");

        if (connections == null || connections.isEmpty()) {
            log.info("No connections exist in workflow, selecting first Code node");
            return codeNodes.stream().findFirst();
        }

        // Tìm tất cả Code nodes đang được connect tới
        Set<String> connectedCodeNodes = new HashSet<>();

        for (Map.Entry<String, Object> entry : connections.entrySet()) {
            try {
                Map<String, Object> nodeConnections = (Map<String, Object>) entry.getValue();
                List<List<Map<String, Object>>> mainConnections =
                        (List<List<Map<String, Object>>>) nodeConnections.get("main");

                if (mainConnections != null) {
                    for (List<Map<String, Object>> connectionGroup : mainConnections) {
                        for (Map<String, Object> connection : connectionGroup) {
                            String targetNode = (String) connection.get("node");
                            if (targetNode != null) {
                                connectedCodeNodes.add(targetNode);
                                log.debug("Found connection to node: '{}'", targetNode);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Error parsing connections for node '{}': {}", entry.getKey(), e.getMessage());
            }
        }

        log.info("Found {} connected nodes: {}", connectedCodeNodes.size(), connectedCodeNodes);

        // Tìm Code node chưa được connect
        Optional<N8nNode> unconnectedNode = codeNodes.stream()
                .filter(node -> !connectedCodeNodes.contains(node.getName()))
                .findFirst();

        if (unconnectedNode.isPresent()) {
            log.info("Found unconnected Code node: '{}'", unconnectedNode.get().getName());
        } else {
            log.info("All Code nodes are connected, selecting first available");
            unconnectedNode = codeNodes.stream().findFirst();
        }

        return unconnectedNode;
    }

    private Optional<N8nNode> selectLastCreatedCodeNode(List<N8nNode> codeNodes) {
        log.info("Selecting last created Code node (highest Y position)");

        Optional<N8nNode> result = codeNodes.stream()
                .max(Comparator.comparingInt(node -> node.getPosition()[1]));

        result.ifPresent(n8nNode -> log.info("Selected last created Code node: '{}' at position [{}, {}]",
                n8nNode.getName(), n8nNode.getPosition()[0], n8nNode.getPosition()[1]));

        return result;
    }

    private Optional<N8nNode> selectClosestCodeNode(List<N8nNode> codeNodes, List<N8nNode> allNodes) {
        log.info("Selecting closest Code node to existing RSS nodes");

        // Tính toán vị trí trung bình của RSS nodes hiện có
        List<N8nNode> existingRssNodes = allNodes.stream()
                .filter(node -> RSS_NODE_TYPE.equals(node.getType()))
                .toList();

        if (existingRssNodes.isEmpty()) {
            log.info("No existing RSS nodes found, selecting first Code node");
            return codeNodes.stream().findFirst();
        }

        // Vị trí trung bình của RSS nodes
        double avgX = existingRssNodes.stream().mapToInt(node -> node.getPosition()[0]).average().orElse(0);
        double avgY = existingRssNodes.stream().mapToInt(node -> node.getPosition()[1]).average().orElse(0);

        log.info("Average RSS node position: [{}, {}]", avgX, avgY);

        // Tìm Code node gần nhất
        Optional<N8nNode> result = codeNodes.stream()
                .min(Comparator.comparingDouble(node -> {
                    int[] pos = node.getPosition();
                    double distance = Math.sqrt(Math.pow(pos[0] - avgX, 2) + Math.pow(pos[1] - avgY, 2));
                    log.debug("Distance from '{}' to avg position: {}", node.getName(), distance);
                    return distance;
                }));

        result.ifPresent(n8nNode -> log.info("Selected closest Code node: '{}'", n8nNode.getName()));

        return result;
    }

    // ========== N8N API METHODS ==========

    private N8nWorkflow getWorkflow(String workflowId) {
        String url = n8nBaseUrl + "/api/v1/workflows/" + workflowId;
        log.debug("Fetching workflow from URL: {}", url);

        try {
            N8nWorkflow workflow = n8nRestTemplate.getForObject(url, N8nWorkflow.class);
            if (workflow == null) {
                throw new BadRequestException();
            }
            log.info("Successfully fetched workflow: '{}' with {} nodes",
                    workflow.getName(), workflow.getNodes().size());
            return workflow;
        } catch (RestClientException e) {
            log.error("Error fetching workflow {}: {}", workflowId, e.getMessage());
            throw new BadRequestException("Failed to fetch workflow: " + e.getMessage());
        }
    }

    private void updateWorkflow(String workflowId, N8nWorkflow workflow) {
        String url = n8nBaseUrl + "/api/v1/workflows/" + workflowId;
        log.debug("Updating workflow at URL: {}", url);

        // Tạo request body chỉ với các field cần thiết
        Map<String, Object> updateRequest = createUpdateRequest(workflow);

        try {
            n8nRestTemplate.put(url, updateRequest);
            log.info("Successfully updated workflow '{}' with {} nodes and {} connections",
                    workflow.getName(), workflow.getNodes().size(),
                    workflow.getConnections() != null ? workflow.getConnections().size() : 0);
        } catch (RestClientException e) {
            log.error("Error updating workflow {}: {}", workflowId, e.getMessage());
            throw new BadRequestException("Failed to update workflow: " + e.getMessage());
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

    // ========== NODE CREATION METHODS ==========

    private N8nNode createRssNode(String nodeName, String feedUrl, List<N8nNode> existingNodes) {
        // Tạo ID unique cho node mới
        String nodeId = generateUniqueNodeId(existingNodes);

        // Tính toán vị trí cho node mới (tránh trùng lặp)
        int[] position = calculateNewNodePosition(existingNodes);

        // Tạo parameters cho RSS node
        Map<String, Object> parameters = createRssNodeParameters(feedUrl);

        log.info("Created new RSS node: '{}' with ID '{}' at position [{}, {}]",
                nodeName, nodeId, position[0], position[1]);

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

        log.debug("Generated unique node ID: {}", nodeId);
        return nodeId;
    }

    private int[] calculateNewNodePosition(List<N8nNode> existingNodes) {
        // Tìm vị trí Y thấp nhất của RSS nodes hiện có để đặt node mới phía dưới
        int minY = existingNodes.stream()
                .filter(node -> RSS_NODE_TYPE.equals(node.getType()))
                .mapToInt(node -> node.getPosition()[1])
                .min()
                .orElse(0);

        int newY = minY + 200; // Đặt node mới cách 200px so với node thấp nhất
        int[] position = new int[]{-280, newY}; // X cố định ở -280 (cột bên trái)

        log.debug("Calculated new node position: [{}, {}]", position[0], position[1]);
        return position;
    }

    // ========== CONNECTION METHODS ==========

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

        log.info("Created connection: '{}' -> '{}' (type: main, index: 0)", rssNodeName, codeNodeName);
    }
}