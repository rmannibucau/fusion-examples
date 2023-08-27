/*
 * Copyright (c) 2022-2023 - Yupiik SAS - https://www.yupiik.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.yupiik.fusion.examples.restapi.model;

import io.yupiik.fusion.http.server.api.WebServer;
import io.yupiik.fusion.json.JsonMapper;
import io.yupiik.fusion.testing.Fusion;
import io.yupiik.fusion.testing.FusionSupport;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@FusionSupport
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MobileLineTests {

    private final HttpClient client = HttpClient.newHttpClient();
    private static final AtomicReference<String> id = new AtomicReference<>("xxx");

    @Test
    @Order(1)
    void createProduct(@Fusion final WebServer.Configuration configuration, @Fusion JsonMapper jsonMapper) throws IOException, InterruptedException {
        final var res = client.send(
                HttpRequest.newBuilder()
                        .POST(HttpRequest.BodyPublishers.ofString("""
                                {
                                     "description": "Mobile Line",
                                     "name": "Mobile Line",
                                     "productSerialNumber": "123456789"
                                 }
                                """, StandardCharsets.UTF_8))
                        .uri(URI.create("http://localhost:" + configuration.port() + "/product")).build(),
                ofString());
        assertAll(
                () -> assertEquals(201, res.statusCode()),
                () -> assertTrue(res.body().contains("\"status\":\"created\""), res::body)
        );
        id.getAndSet(jsonMapper.fromString(Product.class, res.body()).id());
    }

    @Test
    @Order(2)
    void getProduct(@Fusion final WebServer.Configuration configuration) throws IOException, InterruptedException {
        final var res = client.send(
                HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create("http://localhost:" + configuration.port() + "/product/" + id.get())).build(),
                ofString());
        assertAll(
                () -> assertEquals(200, res.statusCode()),
                () -> assertTrue(res.body().contains("\"id\":\"" + id.get() + "\""), res::body)
        );
    }

    @Test
    @Order(3)
    void updateProduct(@Fusion final WebServer.Configuration configuration, @Fusion JsonMapper jsonMapper) throws IOException, InterruptedException {
        final var get = client.send(
                HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create("http://localhost:" + configuration.port() + "/product/" + id.get())).build(),
                ofString());
        assertAll(
                () -> assertEquals(200, get.statusCode()),
                () -> assertTrue(get.body().contains("\"id\":\"" + id.get() + "\""), get::body)
        );

        final var product = jsonMapper.fromString(Product.class, get.body());

        final var res = client.send(
                HttpRequest.newBuilder()
                        .method("PATCH",
                                HttpRequest.BodyPublishers.ofString("""
                                        {
                                             "status": "pendingActive"
                                         }
                                        """, StandardCharsets.UTF_8)
                                )
                        .uri(URI.create("http://localhost:" + configuration.port() + "/product/" + id.get())).build(),
                ofString());
        assertAll(
                () -> assertEquals(200, res.statusCode()),
                () -> assertTrue(res.body().contains("\"id\":\"" + id.get() + "\""), res::body),
                () -> assertTrue(res.body().contains("\"status\":\"pendingActive\""), res::body)
        );
    }

    @Test
    @Order(4)
    void deleteProduct(@Fusion final WebServer.Configuration configuration) throws IOException, InterruptedException {
        final var res = client.send(
                HttpRequest.newBuilder()
                        .DELETE()
                        .uri(URI.create("http://localhost:" + configuration.port() + "/product/" + id.get())).build(),
                ofString());
        assertAll(
                () -> assertEquals(204, res.statusCode())
        );
    }
}
