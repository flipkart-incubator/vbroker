// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: responses.proto

package com.flipkart.vbroker.proto;

/**
 * Protobuf type {@code proto.GetSubscriptionsResponse}
 */
public final class GetSubscriptionsResponse extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:proto.GetSubscriptionsResponse)
    GetSubscriptionsResponseOrBuilder {
    public static final int SUBSCRIPTIONRESPONSES_FIELD_NUMBER = 1;
    private static final long serialVersionUID = 0L;
    // @@protoc_insertion_point(class_scope:proto.GetSubscriptionsResponse)
    private static final com.flipkart.vbroker.proto.GetSubscriptionsResponse DEFAULT_INSTANCE;
    private static final com.google.protobuf.Parser<GetSubscriptionsResponse>
        PARSER = new com.google.protobuf.AbstractParser<GetSubscriptionsResponse>() {
        public GetSubscriptionsResponse parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
            return new GetSubscriptionsResponse(input, extensionRegistry);
        }
    };

    static {
        DEFAULT_INSTANCE = new com.flipkart.vbroker.proto.GetSubscriptionsResponse();
    }

    private java.util.List<com.flipkart.vbroker.proto.GetSubscriptionResponse> subscriptionResponses_;
    private byte memoizedIsInitialized = -1;

    // Use GetSubscriptionsResponse.newBuilder() to construct.
    private GetSubscriptionsResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
        super(builder);
    }
    private GetSubscriptionsResponse() {
        subscriptionResponses_ = java.util.Collections.emptyList();
    }

    private GetSubscriptionsResponse(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        this();
        if (extensionRegistry == null) {
            throw new java.lang.NullPointerException();
        }
        int mutable_bitField0_ = 0;
        com.google.protobuf.UnknownFieldSet.Builder unknownFields =
            com.google.protobuf.UnknownFieldSet.newBuilder();
        try {
            boolean done = false;
            while (!done) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        done = true;
                        break;
                    default: {
                        if (!parseUnknownFieldProto3(
                            input, unknownFields, extensionRegistry, tag)) {
                            done = true;
                        }
                        break;
                    }
                    case 10: {
                        if (!((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
                            subscriptionResponses_ = new java.util.ArrayList<com.flipkart.vbroker.proto.GetSubscriptionResponse>();
                            mutable_bitField0_ |= 0x00000001;
                        }
                        subscriptionResponses_.add(
                            input.readMessage(com.flipkart.vbroker.proto.GetSubscriptionResponse.parser(), extensionRegistry));
                        break;
                    }
                }
            }
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
            throw e.setUnfinishedMessage(this);
        } catch (java.io.IOException e) {
            throw new com.google.protobuf.InvalidProtocolBufferException(
                e).setUnfinishedMessage(this);
        } finally {
            if (((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
                subscriptionResponses_ = java.util.Collections.unmodifiableList(subscriptionResponses_);
            }
            this.unknownFields = unknownFields.build();
            makeExtensionsImmutable();
        }
    }

    public static final com.google.protobuf.Descriptors.Descriptor
    getDescriptor() {
        return com.flipkart.vbroker.proto.PResponses.internal_static_proto_GetSubscriptionsResponse_descriptor;
    }

    public static com.flipkart.vbroker.proto.GetSubscriptionsResponse parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }

    public static com.flipkart.vbroker.proto.GetSubscriptionsResponse parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }

    public static com.flipkart.vbroker.proto.GetSubscriptionsResponse parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }

    public static com.flipkart.vbroker.proto.GetSubscriptionsResponse parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }

    public static com.flipkart.vbroker.proto.GetSubscriptionsResponse parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }

    public static com.flipkart.vbroker.proto.GetSubscriptionsResponse parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }

    public static com.flipkart.vbroker.proto.GetSubscriptionsResponse parseFrom(java.io.InputStream input)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseWithIOException(PARSER, input);
    }

    public static com.flipkart.vbroker.proto.GetSubscriptionsResponse parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static com.flipkart.vbroker.proto.GetSubscriptionsResponse parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseDelimitedWithIOException(PARSER, input);
    }

    public static com.flipkart.vbroker.proto.GetSubscriptionsResponse parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }

    public static com.flipkart.vbroker.proto.GetSubscriptionsResponse parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseWithIOException(PARSER, input);
    }

    public static com.flipkart.vbroker.proto.GetSubscriptionsResponse parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static Builder newBuilder() {
        return DEFAULT_INSTANCE.toBuilder();
    }

    public static Builder newBuilder(com.flipkart.vbroker.proto.GetSubscriptionsResponse prototype) {
        return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }

    public static com.flipkart.vbroker.proto.GetSubscriptionsResponse getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    public static com.google.protobuf.Parser<GetSubscriptionsResponse> parser() {
        return PARSER;
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
        return this.unknownFields;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
    internalGetFieldAccessorTable() {
        return com.flipkart.vbroker.proto.PResponses.internal_static_proto_GetSubscriptionsResponse_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                com.flipkart.vbroker.proto.GetSubscriptionsResponse.class, com.flipkart.vbroker.proto.GetSubscriptionsResponse.Builder.class);
    }

    /**
     * <code>repeated .proto.GetSubscriptionResponse subscriptionResponses = 1;</code>
     */
    public java.util.List<com.flipkart.vbroker.proto.GetSubscriptionResponse> getSubscriptionResponsesList() {
        return subscriptionResponses_;
    }

    /**
     * <code>repeated .proto.GetSubscriptionResponse subscriptionResponses = 1;</code>
     */
    public java.util.List<? extends com.flipkart.vbroker.proto.GetSubscriptionResponseOrBuilder>
    getSubscriptionResponsesOrBuilderList() {
        return subscriptionResponses_;
    }

    /**
     * <code>repeated .proto.GetSubscriptionResponse subscriptionResponses = 1;</code>
     */
    public int getSubscriptionResponsesCount() {
        return subscriptionResponses_.size();
    }

    /**
     * <code>repeated .proto.GetSubscriptionResponse subscriptionResponses = 1;</code>
     */
    public com.flipkart.vbroker.proto.GetSubscriptionResponse getSubscriptionResponses(int index) {
        return subscriptionResponses_.get(index);
    }

    /**
     * <code>repeated .proto.GetSubscriptionResponse subscriptionResponses = 1;</code>
     */
    public com.flipkart.vbroker.proto.GetSubscriptionResponseOrBuilder getSubscriptionResponsesOrBuilder(
        int index) {
        return subscriptionResponses_.get(index);
    }

    public final boolean isInitialized() {
        byte isInitialized = memoizedIsInitialized;
        if (isInitialized == 1) return true;
        if (isInitialized == 0) return false;

        memoizedIsInitialized = 1;
        return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
        throws java.io.IOException {
        for (int i = 0; i < subscriptionResponses_.size(); i++) {
            output.writeMessage(1, subscriptionResponses_.get(i));
        }
        unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
        int size = memoizedSize;
        if (size != -1) return size;

        size = 0;
        for (int i = 0; i < subscriptionResponses_.size(); i++) {
            size += com.google.protobuf.CodedOutputStream
                .computeMessageSize(1, subscriptionResponses_.get(i));
        }
        size += unknownFields.getSerializedSize();
        memoizedSize = size;
        return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof com.flipkart.vbroker.proto.GetSubscriptionsResponse)) {
            return super.equals(obj);
        }
        com.flipkart.vbroker.proto.GetSubscriptionsResponse other = (com.flipkart.vbroker.proto.GetSubscriptionsResponse) obj;

        boolean result = true;
        result = result && getSubscriptionResponsesList()
            .equals(other.getSubscriptionResponsesList());
        result = result && unknownFields.equals(other.unknownFields);
        return result;
    }

    @java.lang.Override
    public int hashCode() {
        if (memoizedHashCode != 0) {
            return memoizedHashCode;
        }
        int hash = 41;
        hash = (19 * hash) + getDescriptor().hashCode();
        if (getSubscriptionResponsesCount() > 0) {
            hash = (37 * hash) + SUBSCRIPTIONRESPONSES_FIELD_NUMBER;
            hash = (53 * hash) + getSubscriptionResponsesList().hashCode();
        }
        hash = (29 * hash) + unknownFields.hashCode();
        memoizedHashCode = hash;
        return hash;
    }

    public Builder newBuilderForType() {
        return newBuilder();
    }

    public Builder toBuilder() {
        return this == DEFAULT_INSTANCE
            ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        Builder builder = new Builder(parent);
        return builder;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<GetSubscriptionsResponse> getParserForType() {
        return PARSER;
    }

    public com.flipkart.vbroker.proto.GetSubscriptionsResponse getDefaultInstanceForType() {
        return DEFAULT_INSTANCE;
    }

    /**
     * Protobuf type {@code proto.GetSubscriptionsResponse}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:proto.GetSubscriptionsResponse)
        com.flipkart.vbroker.proto.GetSubscriptionsResponseOrBuilder {
        private int bitField0_;
        private java.util.List<com.flipkart.vbroker.proto.GetSubscriptionResponse> subscriptionResponses_ =
            java.util.Collections.emptyList();
        private com.google.protobuf.RepeatedFieldBuilderV3<
            com.flipkart.vbroker.proto.GetSubscriptionResponse, com.flipkart.vbroker.proto.GetSubscriptionResponse.Builder, com.flipkart.vbroker.proto.GetSubscriptionResponseOrBuilder> subscriptionResponsesBuilder_;

        // Construct using com.flipkart.vbroker.proto.GetSubscriptionsResponse.newBuilder()
        private Builder() {
            maybeForceBuilderInitialization();
        }

        private Builder(
            com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
            super(parent);
            maybeForceBuilderInitialization();
        }

        public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
            return com.flipkart.vbroker.proto.PResponses.internal_static_proto_GetSubscriptionsResponse_descriptor;
        }

        protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
            return com.flipkart.vbroker.proto.PResponses.internal_static_proto_GetSubscriptionsResponse_fieldAccessorTable
                .ensureFieldAccessorsInitialized(
                    com.flipkart.vbroker.proto.GetSubscriptionsResponse.class, com.flipkart.vbroker.proto.GetSubscriptionsResponse.Builder.class);
        }

        private void maybeForceBuilderInitialization() {
            if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
                getSubscriptionResponsesFieldBuilder();
            }
        }

        public Builder clear() {
            super.clear();
            if (subscriptionResponsesBuilder_ == null) {
                subscriptionResponses_ = java.util.Collections.emptyList();
                bitField0_ = (bitField0_ & ~0x00000001);
            } else {
                subscriptionResponsesBuilder_.clear();
            }
            return this;
        }

        public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
            return com.flipkart.vbroker.proto.PResponses.internal_static_proto_GetSubscriptionsResponse_descriptor;
        }

        public com.flipkart.vbroker.proto.GetSubscriptionsResponse getDefaultInstanceForType() {
            return com.flipkart.vbroker.proto.GetSubscriptionsResponse.getDefaultInstance();
        }

        public com.flipkart.vbroker.proto.GetSubscriptionsResponse build() {
            com.flipkart.vbroker.proto.GetSubscriptionsResponse result = buildPartial();
            if (!result.isInitialized()) {
                throw newUninitializedMessageException(result);
            }
            return result;
        }

        public com.flipkart.vbroker.proto.GetSubscriptionsResponse buildPartial() {
            com.flipkart.vbroker.proto.GetSubscriptionsResponse result = new com.flipkart.vbroker.proto.GetSubscriptionsResponse(this);
            int from_bitField0_ = bitField0_;
            if (subscriptionResponsesBuilder_ == null) {
                if (((bitField0_ & 0x00000001) == 0x00000001)) {
                    subscriptionResponses_ = java.util.Collections.unmodifiableList(subscriptionResponses_);
                    bitField0_ = (bitField0_ & ~0x00000001);
                }
                result.subscriptionResponses_ = subscriptionResponses_;
            } else {
                result.subscriptionResponses_ = subscriptionResponsesBuilder_.build();
            }
            onBuilt();
            return result;
        }

        public Builder clone() {
            return (Builder) super.clone();
        }

        public Builder setField(
            com.google.protobuf.Descriptors.FieldDescriptor field,
            java.lang.Object value) {
            return (Builder) super.setField(field, value);
        }

        public Builder clearField(
            com.google.protobuf.Descriptors.FieldDescriptor field) {
            return (Builder) super.clearField(field);
        }

        public Builder clearOneof(
            com.google.protobuf.Descriptors.OneofDescriptor oneof) {
            return (Builder) super.clearOneof(oneof);
        }

        public Builder setRepeatedField(
            com.google.protobuf.Descriptors.FieldDescriptor field,
            int index, java.lang.Object value) {
            return (Builder) super.setRepeatedField(field, index, value);
        }

        public Builder addRepeatedField(
            com.google.protobuf.Descriptors.FieldDescriptor field,
            java.lang.Object value) {
            return (Builder) super.addRepeatedField(field, value);
        }

        public Builder mergeFrom(com.google.protobuf.Message other) {
            if (other instanceof com.flipkart.vbroker.proto.GetSubscriptionsResponse) {
                return mergeFrom((com.flipkart.vbroker.proto.GetSubscriptionsResponse) other);
            } else {
                super.mergeFrom(other);
                return this;
            }
        }

        public Builder mergeFrom(com.flipkart.vbroker.proto.GetSubscriptionsResponse other) {
            if (other == com.flipkart.vbroker.proto.GetSubscriptionsResponse.getDefaultInstance()) return this;
            if (subscriptionResponsesBuilder_ == null) {
                if (!other.subscriptionResponses_.isEmpty()) {
                    if (subscriptionResponses_.isEmpty()) {
                        subscriptionResponses_ = other.subscriptionResponses_;
                        bitField0_ = (bitField0_ & ~0x00000001);
                    } else {
                        ensureSubscriptionResponsesIsMutable();
                        subscriptionResponses_.addAll(other.subscriptionResponses_);
                    }
                    onChanged();
                }
            } else {
                if (!other.subscriptionResponses_.isEmpty()) {
                    if (subscriptionResponsesBuilder_.isEmpty()) {
                        subscriptionResponsesBuilder_.dispose();
                        subscriptionResponsesBuilder_ = null;
                        subscriptionResponses_ = other.subscriptionResponses_;
                        bitField0_ = (bitField0_ & ~0x00000001);
                        subscriptionResponsesBuilder_ =
                            com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                                getSubscriptionResponsesFieldBuilder() : null;
                    } else {
                        subscriptionResponsesBuilder_.addAllMessages(other.subscriptionResponses_);
                    }
                }
            }
            this.mergeUnknownFields(other.unknownFields);
            onChanged();
            return this;
        }

        public final boolean isInitialized() {
            return true;
        }

        public Builder mergeFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
            com.flipkart.vbroker.proto.GetSubscriptionsResponse parsedMessage = null;
            try {
                parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                parsedMessage = (com.flipkart.vbroker.proto.GetSubscriptionsResponse) e.getUnfinishedMessage();
                throw e.unwrapIOException();
            } finally {
                if (parsedMessage != null) {
                    mergeFrom(parsedMessage);
                }
            }
            return this;
        }

        private void ensureSubscriptionResponsesIsMutable() {
            if (!((bitField0_ & 0x00000001) == 0x00000001)) {
                subscriptionResponses_ = new java.util.ArrayList<com.flipkart.vbroker.proto.GetSubscriptionResponse>(subscriptionResponses_);
                bitField0_ |= 0x00000001;
            }
        }

        /**
         * <code>repeated .proto.GetSubscriptionResponse subscriptionResponses = 1;</code>
         */
        public java.util.List<com.flipkart.vbroker.proto.GetSubscriptionResponse> getSubscriptionResponsesList() {
            if (subscriptionResponsesBuilder_ == null) {
                return java.util.Collections.unmodifiableList(subscriptionResponses_);
            } else {
                return subscriptionResponsesBuilder_.getMessageList();
            }
        }

        /**
         * <code>repeated .proto.GetSubscriptionResponse subscriptionResponses = 1;</code>
         */
        public int getSubscriptionResponsesCount() {
            if (subscriptionResponsesBuilder_ == null) {
                return subscriptionResponses_.size();
            } else {
                return subscriptionResponsesBuilder_.getCount();
            }
        }

        /**
         * <code>repeated .proto.GetSubscriptionResponse subscriptionResponses = 1;</code>
         */
        public com.flipkart.vbroker.proto.GetSubscriptionResponse getSubscriptionResponses(int index) {
            if (subscriptionResponsesBuilder_ == null) {
                return subscriptionResponses_.get(index);
            } else {
                return subscriptionResponsesBuilder_.getMessage(index);
            }
        }

        /**
         * <code>repeated .proto.GetSubscriptionResponse subscriptionResponses = 1;</code>
         */
        public Builder setSubscriptionResponses(
            int index, com.flipkart.vbroker.proto.GetSubscriptionResponse value) {
            if (subscriptionResponsesBuilder_ == null) {
                if (value == null) {
                    throw new NullPointerException();
                }
                ensureSubscriptionResponsesIsMutable();
                subscriptionResponses_.set(index, value);
                onChanged();
            } else {
                subscriptionResponsesBuilder_.setMessage(index, value);
            }
            return this;
        }

        /**
         * <code>repeated .proto.GetSubscriptionResponse subscriptionResponses = 1;</code>
         */
        public Builder setSubscriptionResponses(
            int index, com.flipkart.vbroker.proto.GetSubscriptionResponse.Builder builderForValue) {
            if (subscriptionResponsesBuilder_ == null) {
                ensureSubscriptionResponsesIsMutable();
                subscriptionResponses_.set(index, builderForValue.build());
                onChanged();
            } else {
                subscriptionResponsesBuilder_.setMessage(index, builderForValue.build());
            }
            return this;
        }

        /**
         * <code>repeated .proto.GetSubscriptionResponse subscriptionResponses = 1;</code>
         */
        public Builder addSubscriptionResponses(com.flipkart.vbroker.proto.GetSubscriptionResponse value) {
            if (subscriptionResponsesBuilder_ == null) {
                if (value == null) {
                    throw new NullPointerException();
                }
                ensureSubscriptionResponsesIsMutable();
                subscriptionResponses_.add(value);
                onChanged();
            } else {
                subscriptionResponsesBuilder_.addMessage(value);
            }
            return this;
        }

        /**
         * <code>repeated .proto.GetSubscriptionResponse subscriptionResponses = 1;</code>
         */
        public Builder addSubscriptionResponses(
            int index, com.flipkart.vbroker.proto.GetSubscriptionResponse value) {
            if (subscriptionResponsesBuilder_ == null) {
                if (value == null) {
                    throw new NullPointerException();
                }
                ensureSubscriptionResponsesIsMutable();
                subscriptionResponses_.add(index, value);
                onChanged();
            } else {
                subscriptionResponsesBuilder_.addMessage(index, value);
            }
            return this;
        }

        /**
         * <code>repeated .proto.GetSubscriptionResponse subscriptionResponses = 1;</code>
         */
        public Builder addSubscriptionResponses(
            com.flipkart.vbroker.proto.GetSubscriptionResponse.Builder builderForValue) {
            if (subscriptionResponsesBuilder_ == null) {
                ensureSubscriptionResponsesIsMutable();
                subscriptionResponses_.add(builderForValue.build());
                onChanged();
            } else {
                subscriptionResponsesBuilder_.addMessage(builderForValue.build());
            }
            return this;
        }

        /**
         * <code>repeated .proto.GetSubscriptionResponse subscriptionResponses = 1;</code>
         */
        public Builder addSubscriptionResponses(
            int index, com.flipkart.vbroker.proto.GetSubscriptionResponse.Builder builderForValue) {
            if (subscriptionResponsesBuilder_ == null) {
                ensureSubscriptionResponsesIsMutable();
                subscriptionResponses_.add(index, builderForValue.build());
                onChanged();
            } else {
                subscriptionResponsesBuilder_.addMessage(index, builderForValue.build());
            }
            return this;
        }

        /**
         * <code>repeated .proto.GetSubscriptionResponse subscriptionResponses = 1;</code>
         */
        public Builder addAllSubscriptionResponses(
            java.lang.Iterable<? extends com.flipkart.vbroker.proto.GetSubscriptionResponse> values) {
            if (subscriptionResponsesBuilder_ == null) {
                ensureSubscriptionResponsesIsMutable();
                com.google.protobuf.AbstractMessageLite.Builder.addAll(
                    values, subscriptionResponses_);
                onChanged();
            } else {
                subscriptionResponsesBuilder_.addAllMessages(values);
            }
            return this;
        }

        /**
         * <code>repeated .proto.GetSubscriptionResponse subscriptionResponses = 1;</code>
         */
        public Builder clearSubscriptionResponses() {
            if (subscriptionResponsesBuilder_ == null) {
                subscriptionResponses_ = java.util.Collections.emptyList();
                bitField0_ = (bitField0_ & ~0x00000001);
                onChanged();
            } else {
                subscriptionResponsesBuilder_.clear();
            }
            return this;
        }

        /**
         * <code>repeated .proto.GetSubscriptionResponse subscriptionResponses = 1;</code>
         */
        public Builder removeSubscriptionResponses(int index) {
            if (subscriptionResponsesBuilder_ == null) {
                ensureSubscriptionResponsesIsMutable();
                subscriptionResponses_.remove(index);
                onChanged();
            } else {
                subscriptionResponsesBuilder_.remove(index);
            }
            return this;
        }

        /**
         * <code>repeated .proto.GetSubscriptionResponse subscriptionResponses = 1;</code>
         */
        public com.flipkart.vbroker.proto.GetSubscriptionResponse.Builder getSubscriptionResponsesBuilder(
            int index) {
            return getSubscriptionResponsesFieldBuilder().getBuilder(index);
        }

        /**
         * <code>repeated .proto.GetSubscriptionResponse subscriptionResponses = 1;</code>
         */
        public com.flipkart.vbroker.proto.GetSubscriptionResponseOrBuilder getSubscriptionResponsesOrBuilder(
            int index) {
            if (subscriptionResponsesBuilder_ == null) {
                return subscriptionResponses_.get(index);
            } else {
                return subscriptionResponsesBuilder_.getMessageOrBuilder(index);
            }
        }

        /**
         * <code>repeated .proto.GetSubscriptionResponse subscriptionResponses = 1;</code>
         */
        public java.util.List<? extends com.flipkart.vbroker.proto.GetSubscriptionResponseOrBuilder>
        getSubscriptionResponsesOrBuilderList() {
            if (subscriptionResponsesBuilder_ != null) {
                return subscriptionResponsesBuilder_.getMessageOrBuilderList();
            } else {
                return java.util.Collections.unmodifiableList(subscriptionResponses_);
            }
        }

        /**
         * <code>repeated .proto.GetSubscriptionResponse subscriptionResponses = 1;</code>
         */
        public com.flipkart.vbroker.proto.GetSubscriptionResponse.Builder addSubscriptionResponsesBuilder() {
            return getSubscriptionResponsesFieldBuilder().addBuilder(
                com.flipkart.vbroker.proto.GetSubscriptionResponse.getDefaultInstance());
        }

        /**
         * <code>repeated .proto.GetSubscriptionResponse subscriptionResponses = 1;</code>
         */
        public com.flipkart.vbroker.proto.GetSubscriptionResponse.Builder addSubscriptionResponsesBuilder(
            int index) {
            return getSubscriptionResponsesFieldBuilder().addBuilder(
                index, com.flipkart.vbroker.proto.GetSubscriptionResponse.getDefaultInstance());
        }

        /**
         * <code>repeated .proto.GetSubscriptionResponse subscriptionResponses = 1;</code>
         */
        public java.util.List<com.flipkart.vbroker.proto.GetSubscriptionResponse.Builder>
        getSubscriptionResponsesBuilderList() {
            return getSubscriptionResponsesFieldBuilder().getBuilderList();
        }

        private com.google.protobuf.RepeatedFieldBuilderV3<
            com.flipkart.vbroker.proto.GetSubscriptionResponse, com.flipkart.vbroker.proto.GetSubscriptionResponse.Builder, com.flipkart.vbroker.proto.GetSubscriptionResponseOrBuilder>
        getSubscriptionResponsesFieldBuilder() {
            if (subscriptionResponsesBuilder_ == null) {
                subscriptionResponsesBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
                    com.flipkart.vbroker.proto.GetSubscriptionResponse, com.flipkart.vbroker.proto.GetSubscriptionResponse.Builder, com.flipkart.vbroker.proto.GetSubscriptionResponseOrBuilder>(
                    subscriptionResponses_,
                    ((bitField0_ & 0x00000001) == 0x00000001),
                    getParentForChildren(),
                    isClean());
                subscriptionResponses_ = null;
            }
            return subscriptionResponsesBuilder_;
        }

        public final Builder setUnknownFields(
            final com.google.protobuf.UnknownFieldSet unknownFields) {
            return super.setUnknownFieldsProto3(unknownFields);
        }

        public final Builder mergeUnknownFields(
            final com.google.protobuf.UnknownFieldSet unknownFields) {
            return super.mergeUnknownFields(unknownFields);
        }


        // @@protoc_insertion_point(builder_scope:proto.GetSubscriptionsResponse)
    }

}

