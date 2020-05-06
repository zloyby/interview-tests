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
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

class OrderIntegrationTest {
    private static Server server;

    @BeforeAll
    public static void startTheServer() {
        server = Main.startServer();
    }

    @Test
    void testHealthAndMetrics() {
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(getConnectionString("/metrics"))
                .request()
                .get();
        Assertions.assertEquals(200, response.getStatus(), "metrics status code is not 200");

        response = client
                .target(getConnectionString("/health"))
                .request()
                .get();
        Assertions.assertEquals(200, response.getStatus(), "health status code is not 200");
    }

    @Test
    void testGetAllCoffeeMachinesAndCheckAvailabilityAfterAddOrder() {
        Client client = ClientBuilder.newClient();

        // Initially Hulk have 6 min velocity
        JsonObject jsonObject = client
                .target(getConnectionString("/machines"))
                .request()
                .get(JsonObject.class);
        Assertions.assertNotNull(jsonObject);
        Assertions.assertNotNull(jsonObject.getJsonString("message"));
        Assertions.assertNotNull(jsonObject.getJsonArray("payload"));
        JsonArray payload = jsonObject.getJsonArray("payload");
        Assertions.assertEquals(2, payload.size());
        int readyMin = ("Hulk".equals(payload.getJsonObject(0).getString("Coffee machine")))
                ? payload.getJsonObject(0).getInt("You coffee will be ready in (min)")
                : payload.getJsonObject(1).getInt("You coffee will be ready in (min)");
        Assertions.assertEquals(6, readyMin);

        // make new order to Hulk
        client
                .target(getConnectionString("/orders"))
                .request()
                .post(Entity.entity("{\"coffee\" : \"Latte\", \"machine\" : \"Hulk\"}", MediaType.APPLICATION_JSON), JsonObject.class);

        //check ready time, as velocity is 6 - should be (6-1)+6, -1 because machine already started to make coffee
        jsonObject = client
                .target(getConnectionString("/machines"))
                .request()
                .get(JsonObject.class);
        Assertions.assertNotNull(jsonObject);
        Assertions.assertNotNull(jsonObject.getJsonString("message"));
        Assertions.assertNotNull(jsonObject.getJsonArray("payload"));
        payload = jsonObject.getJsonArray("payload");
        Assertions.assertEquals(2, payload.size());
        readyMin = ("Hulk".equals(payload.getJsonObject(0).getString("Coffee machine")))
                ? payload.getJsonObject(0).getInt("You coffee will be ready in (min)")
                : payload.getJsonObject(1).getInt("You coffee will be ready in (min)");
        Assertions.assertEquals(11, readyMin);

        // make another order to Hulk
        client
                .target(getConnectionString("/orders"))
                .request()
                .post(Entity.entity("{\"coffee\" : \"Espresso\", \"machine\" : \"Hulk\"}", MediaType.APPLICATION_JSON), JsonObject.class);

        // check ready time, as velocity is 6 - should be (6-1)+6+6
        jsonObject = client
                .target(getConnectionString("/machines"))
                .request()
                .get(JsonObject.class);
        Assertions.assertNotNull(jsonObject);
        Assertions.assertNotNull(jsonObject.getJsonString("message"));
        Assertions.assertNotNull(jsonObject.getJsonArray("payload"));
        payload = jsonObject.getJsonArray("payload");
        Assertions.assertEquals(2, payload.size());
        readyMin = ("Hulk".equals(payload.getJsonObject(0).getString("Coffee machine")))
                ? payload.getJsonObject(0).getInt("You coffee will be ready in (min)")
                : payload.getJsonObject(1).getInt("You coffee will be ready in (min)");
        Assertions.assertEquals(17, readyMin);
    }

    @Test
    void testCreateOrderAndGetStatus() throws InterruptedException {
        Client client = ClientBuilder.newClient();

        // make new order
        JsonObject jsonObject = client
                .target(getConnectionString("/orders"))
                .request()
                .post(Entity.entity("{\"coffee\" : \"Latte\", \"machine\" : \"Lion\"}", MediaType.APPLICATION_JSON), JsonObject.class);
        Assertions.assertNotNull(jsonObject);
        Assertions.assertNotNull(jsonObject.getJsonString("message"));
        Assertions.assertNotNull(jsonObject.getJsonString("payload"));
        String id = jsonObject.getJsonString("payload").getString();
        Assertions.assertTrue(validUUID(id), "order ID is not UUID");

        // check this order
        jsonObject = client
                .target(getConnectionString("/orders/" + id))
                .request()
                .get(JsonObject.class);
        Assertions.assertNotNull(jsonObject);
        Assertions.assertNotNull(jsonObject.getJsonString("message"));
        Assertions.assertNotNull(jsonObject.getJsonString("payload"));
        String dateTime = jsonObject.getJsonString("payload").getString();
        OffsetDateTime parse = OffsetDateTime.parse(dateTime);
        Assertions.assertTrue(parse.compareTo(OffsetDateTime.now()) > 0,
                "Ready dateTime is less than current dateTime");

        // sleep 1 min (as machine Lion has 1 min velocity) and re-check order
        TimeUnit.MINUTES.sleep(1L);
        jsonObject = client
                .target(getConnectionString("/orders/" + id))
                .request()
                .get(JsonObject.class);
        Assertions.assertNotNull(jsonObject);
        Assertions.assertNotNull(jsonObject.getJsonString("message"));
        Assertions.assertNotNull(jsonObject.getJsonString("payload"));
        dateTime = jsonObject.getJsonString("payload").getString();
        parse = OffsetDateTime.parse(dateTime);
        Assertions.assertTrue(parse.compareTo(OffsetDateTime.now()) < 0,
                "Ready dateTime is more than current dateTime, even after 1 min sleep");

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
