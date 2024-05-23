
package com.networknt.market.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.networknt.config.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.LambdaException;

/**
 * This is the generated BusinessHandler for developers to write business logic for the Lambda function. Once this file
 * is generated, it won't be overwritten with another generation in the same folder.
 */
public class BusinessHandler {
    private static final Logger logger = LoggerFactory.getLogger(BusinessHandler.class);
    private static final String FUNCTION_NAME = "MarketNativeLambdaProxyFunction";
    private static final String REGION = "us-east-2";

    private final LambdaClient lambdaClient;

    private BusinessHandler() {
        lambdaClient = DependencyFactory.lambdaClient(Region.of(REGION));
    }

    private static final class InstanceHolder {
        private static final BusinessHandler INSTANCE = new BusinessHandler();
    }

    public static BusinessHandler getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("service_id", "com.networknt.petstore-1.0.0");

        APIGatewayProxyRequestEvent petstoreRequestEvent = new APIGatewayProxyRequestEvent();
        petstoreRequestEvent.setHeaders(headers);
        petstoreRequestEvent.setHttpMethod("GET");
        petstoreRequestEvent.setPath("/v1/pets");
        petstoreRequestEvent.setQueryStringParameters(new HashMap<>());
        String requestString = JsonMapper.toJson(petstoreRequestEvent);
        if(logger.isTraceEnabled()) logger.trace("petstoreRequestEvent = {} to functionName {}", requestString, FUNCTION_NAME);

        String petstoreResponse = null;
        try {
            var payload = SdkBytes.fromUtf8String(requestString);
            var request = InvokeRequest.builder()
                    .functionName(FUNCTION_NAME)
                    .payload(payload)
                    .build();
            var res = lambdaClient.invoke(request);
            if (logger.isDebugEnabled()) {
                logger.debug("lambda call function error:{}", res.functionError());
                logger.debug("lambda logger result:{}", res.logResult());
                logger.debug("lambda call status:{}", res.statusCode());
            }
            petstoreResponse = res.payload().asUtf8String();
        } catch (LambdaException e) {
            logger.error("LambdaException", e);
        }
        APIGatewayProxyResponseEvent responseEvent = JsonMapper.fromJson(petstoreResponse, APIGatewayProxyResponseEvent.class);;
        return responseEvent;
    }
}
