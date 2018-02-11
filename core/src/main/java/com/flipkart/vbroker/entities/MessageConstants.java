package com.flipkart.vbroker.entities;

public class MessageConstants {
    //The response code and the response of the message. Useful for debugging and for showing on UI
    public static final String RESPONSE_CODE = "X_RESTBUS_RESPONSE_CODE";
    public static final String RESPONSE_BODY = "X_RESTBUS_RESPONSE_BODY";
    public static final String MESSAGE_ID_HEADER = "X_RESTBUS_MESSAGE_ID";
    public static final String SUBSCRIBER_ID_HEADER = "X_RESTBUS_SUBSCRIBER_ID";
    public static final String TRANSACTION_ID_HEADER = "X_RESTBUS_TRANSACTION_ID";
    public static final String GROUP_ID_HEADER = "X_RESTBUS_GROUP_ID";
    public static final String CORRELATION_ID_HEADER = "X_RESTBUS_CORRELATION_ID";
    public static final String BRIDGED = "X_RESTBUS_BRIDGED";
    public static final String BRIDGED_COUNT = "X_RESTBUS_BRIDGED_COUNT";
    public static final String REPLY_TO_HEADER = "X_RESTBUS_REPLY_TO";
    public static final String HTTP_URI_HEADER = "X_RESTBUS_HTTP_URI";
    public static final String HTTP_METHOD_HEADER = "X_RESTBUS_HTTP_METHOD";
    public static final String HTTP_CONTENT_TYPE = "X_RESTBUS_CONTENT_TYPE";
    public static final String CONTEXT_HEADER = "X_RESTBUS_CONTEXT";
    public static final String REPLY_TO_HTTP_URI_HEADER = "X_RESTBUS_REPLY_TO_HTTP_URI";
    public static final String REPLY_TO_HTTP_METHOD_HEADER = "X_RESTBUS_REPLY_TO_HTTP_METHOD";
    public static final String ACK = "X_RESTBUS_ACK";
    public static final String DESTINATION_RESPONSE_STATUS = "X_RESTBUS_DESTINATION_RESPONSE_STATUS";
    public static final String USER = "X_RESTBUS_USER";
    public static final String REQUEST_TIMEOUT = "X_RESTBUS_REQUEST_TIMEOUT";
    public static final String DESTINATION_TOPIC = "X_RESTBUS_DESTINATION_TOPIC";
    public static final String COMMAND_NAME = "X_RESTBUS_COMMAND_NAME";
    public static final String ORIGINAL_TOPIC_NAME = "X_RESTBUS_ORIGINAL_TOPIC_NAME";
    public static final String PRODUCER_APP_ID = "X_PRODUCER_APP_ID";
    public static final String PRODUCER_APP_ID_2 = "X_RESTBUS_PRODUCER_APP_ID";

    public static final String SUBSCRIPTION_APP_ID = "X_RESTBUS_CONSUMER_APP_ID";

    public static final String SUBSCRIBER_TEAM_HEADER = "X_SUBSCRIBER_TEAM";
    public static final String EXCHANGE_NAME_HEADER = "X_EXCHANGE_NAME";
    public static final String APP_ID_HEADER = "X_APP_ID";
    public static final String ROUTING_KEY_HEADER = "X_ROUTING_KEY";
    public static final String MAIN_EXCHANGE_NAME = "X_MAIN_EXCHANGE_NAME";
    public static final String MAIN_SUBSCRIBER_ID = "X_SUBSCRIBER_ID";
    public static final String SIDELINE_EXCHANGE_NAME = "X_SIDELINE_EXCHANGE_NAME";
    public static final String UNSIDELINE_EXCHANGE_NAME = "X_UNSIDELINE_EXCHANGE_NAME";
    public static final String IS_GROUPED = "X_IS_GROUPED";

    //Should be json of the format [{"from": 200, "to": 299}, {"from": 400, "to": 499}, {"from": 502, "to": 502}]
    public static final String CALLBACK_CODES = "X_CALLBACK_CODES";
    public static final String CHECKSUM_HEADER = "X_RESTBUS_CHECKSUM";

    public static final String REQUEST_ID = "X-REQUEST-ID";
    public static final String CUSTOM_AUTH_HEADER = "X_CUSTOM_CRD_AUTH_HEADER";
    public static final String CUSTOM_AUTH_HEADER_VAlUE = "dhfsUYtrkeh";
    public static final String CUSTOM_AUTH_HEADER_UPDATE_VAlUE = "dhfsUYtrkeh";

    public static final String PROXY_USER_HEADER = "X-Proxy-User";
}
