package com.example.demo.service;

import com.example.demo.service.configuration.RestTemplateConfiguration;
import com.example.demo.service.exception.AuthorRegistryException;
import org.junit.jupiter.api.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.model.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.Parameter.param;

@Testcontainers
@SpringBootTest(
        classes = {
                HttpAuthorRegistryGateway.class,
                RestTemplateConfiguration.class
        })
public class AuthorRegistryGatewayTest {
    @Container
    public static final MockServerContainer mockServer = new MockServerContainer(DockerImageName.parse("mockserver/mockserver:5.13.2"));

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("author.registry.service.base.url", mockServer::getEndpoint);
        registry.add("author.registry.service.timeout.seconds", () -> 1);
    }

    @Autowired
    AuthorRegistryGateway authorRegistryGateway;

    @Test
    void shouldApproveAuthor() {
        var client = new MockServerClient(mockServer.getHost(), mockServer.getServerPort());
        client.when(request().withPath("/api/authors-check").withQueryStringParameters(param("firstName", "Robert"), param("lastName", "Stevenson"), param("bookTitle", "Treasure Island")))
                .respond(HttpResponse.response("true").withHeader("Content-Type", "application/json"));

        Boolean authorCheck = authorRegistryGateway.checkAuthor("Robert", "Stevenson", "Treasure Island", UUID.randomUUID().toString());

        assertEquals(true, authorCheck);
    }

    @Test
    void shouldFailWithTimeout() {
        var client = new MockServerClient(mockServer.getHost(), mockServer.getServerPort());
        client.when(request().withPath("/api/authors-check").withQueryStringParameters(param("firstName", "Lev"), param("lastName", "Tolstoy"), param("bookTitle", "War and Peace")))
                .respond(HttpResponse.response("true").withHeader("Content-Type", "application/json").withDelay(TimeUnit.SECONDS, 2));

        assertThrows(AuthorRegistryException.class, () -> authorRegistryGateway.checkAuthor("Lev", "Tolstoy", "War and Peace", UUID.randomUUID().toString()));
    }
}
