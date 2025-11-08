package org.hknu.healthcare.Serivce;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hknu.healthcare.DTO.DrugInfoDto;
import org.hknu.healthcare.util.HtmlUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.*;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Optional;

@Service
public class DrugInfoService {

    @Value("${drug.api.base-url}")
    private String baseUrl;

    @Value("${drug.api.path}")
    private String path;

    @Value("${drug.api.service-key}")
    private String serviceKey;

    private WebClient buildClient() {
        HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofSeconds(5));
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Cacheable(cacheNames = "drugInfo", key = "#itemName")
    public Optional<DrugInfoDto> findByItemName(String itemName) {
        WebClient client = buildClient();

        JsonNode root = client.get()
                .uri(uri -> uri.path(path)
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("itemName", itemName)
                        .queryParam("type", "json")
                        .queryParam("numOfRows", "1")
                        .queryParam("pageNo", "1")
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, r -> r.bodyToMono(String.class)
                        .map(msg -> new RuntimeException("API 오류: " + msg)))
                .bodyToMono(JsonNode.class)
                .block(Duration.ofSeconds(8));

        if (root == null) return Optional.empty();

        JsonNode header = root.path("response").path("header");
        String code = header.path("resultCode").asText();
        if (!"00".equals(code)) return Optional.empty();

        JsonNode items = root.path("response").path("body").path("items");
        ObjectNode first = extractFirst(items);
        if (first == null) return Optional.empty();

        String name = text(first, "itemName");
        String usage = HtmlUtil.strip(text(first, "useMethodQesitm"));
        String precautions = HtmlUtil.strip(text(first, "atpnQesitm"));
        String side = HtmlUtil.strip(text(first, "seQesitm"));

        return Optional.of(new DrugInfoDto(name, usage, precautions, side));
    }

    private ObjectNode extractFirst(JsonNode items) {
        if (items == null || items.isNull()) return null;
        if (items.isArray() && items.size() > 0 && items.get(0).isObject()) return (ObjectNode) items.get(0);
        if (items.isObject() && items.has("item")) {
            JsonNode i = items.get("item");
            if (i.isObject()) return (ObjectNode) i;
            if (i.isArray() && i.size() > 0 && i.get(0).isObject()) return (ObjectNode) i.get(0);
        }
        return null;
    }

    private String text(ObjectNode node, String key) {
        JsonNode v = node.get(key);
        return v != null && !v.isNull() ? v.asText() : null;
    }
}