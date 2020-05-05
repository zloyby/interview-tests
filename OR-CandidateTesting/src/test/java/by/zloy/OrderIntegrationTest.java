/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package by.zloy;

import io.helidon.microprofile.server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.spi.CDI;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

class OrderIntegrationTest {
    private static Server server;

    @BeforeAll
    public static void startTheServer() throws Exception {
        server = Main.startServer();
    }

    @Test
    void testHelloWorld() {
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(getConnectionString("/metrics"))
                .request()
                .get();
        Assertions.assertEquals(200, response.getStatus(), "GET metrics status code");

        response = client
                .target(getConnectionString("/health"))
                .request()
                .get();
        Assertions.assertEquals(200, response.getStatus(), "GET health status code is not 200");

        JsonObject jsonObject = client
                .target(getConnectionString("/orders"))
                .request()
                .post(Entity.entity("{\"coffee\" : \"Latte\"}", MediaType.APPLICATION_JSON), JsonObject.class);
        Assertions.assertNotNull(jsonObject);
        Assertions.assertNotNull(jsonObject.getJsonString("message"));
        Assertions.assertNotNull(jsonObject.getJsonString("data"));
        String id = jsonObject.getJsonString("data").getString();
        Assertions.assertTrue(validUUID(id));

        jsonObject = client
                .target(getConnectionString("/orders/" + id))
                .request()
                .get(JsonObject.class);
        Assertions.assertNotNull(jsonObject);
        Assertions.assertNotNull(jsonObject.getJsonString("message"));
        Assertions.assertNotNull(jsonObject.getJsonString("data"));
        Assertions.assertEquals("0", jsonObject.getJsonString("data").getString(),
                "GET by ID returns not 0 as progress");
    }

    @AfterAll
    static void destroyClass() {
        CDI<Object> current = CDI.current();
        ((SeContainer) current).close();
    }

    private boolean validUUID(String line) {
        return line.split("-").length == 5; // Just simplest validation
    }

    private String getConnectionString(String path) {
        return "http://localhost:" + server.port() + path;
    }
}