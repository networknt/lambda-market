package com.networknt.market.handler;

import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;

/**
 * The module containing all dependencies required by the {@link BusinessHandler}.
 */
public class DependencyFactory {
    private DependencyFactory() {}

    /**
     * @return an instance of LambdaClient for a region
     */
    public static LambdaClient lambdaClient(Region region) {
        return LambdaClient.builder()
                .region(region)
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .build();
    }

    /**
     * @return an instance of LambdaClient
     */
    public static LambdaClient lambdaClient() {
        return LambdaClient.builder()
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .build();
    }

}
