// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: responses.proto

package com.flipkart.vbroker.proto;

/**
 * Protobuf type {@code proto.GetTopicsResponse}
 */
public final class GetTopicsResponse extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:proto.GetTopicsResponse)
    GetTopicsResponseOrBuilder {
    public static final int TOPICRESPONSES_FIELD_NUMBER = 1;
    private static final long serialVersionUID = 0L;
    // @@protoc_insertion_point(class_scope:proto.GetTopicsResponse)
    private static final com.flipkart.vbroker.proto.GetTopicsResponse DEFAULT_INSTANCE;
    private static final com.google.protobuf.Parser<GetTopicsResponse>
        PARSER = new com.google.protobuf.AbstractParser<GetTopicsResponse>() {
        public GetTopicsResponse parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
            return new GetTopicsResponse(input, extensionRegistry);
        }
    };

    static {
        DEFAULT_INSTANCE = new com.flipkart.vbroker.proto.GetTopicsResponse();
    }

    private java.util.List<com.flipkart.vbroker.proto.GetTopicResponse> topicResponses_;
    private byte memoizedIsInitialized = -1;

    // Use GetTopicsResponse.newBuilder() to construct.
    private GetTopicsResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
        super(builder);
    }
    private GetTopicsResponse() {
        topicResponses_ = java.util.Collections.emptyList();
    }

    private GetTopicsResponse(
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
                            topicResponses_ = new java.util.ArrayList<com.flipkart.vbroker.proto.GetTopicResponse>();
                            mutable_bitField0_ |= 0x00000001;
                        }
                        topicResponses_.add(
                            input.readMessage(com.flipkart.vbroker.proto.GetTopicResponse.parser(), extensionRegistry));
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
                topicResponses_ = java.util.Collections.unmodifiableList(topicResponses_);
            }
            this.unknownFields = unknownFields.build();
            makeExtensionsImmutable();
        }
    }

    public static final com.google.protobuf.Descriptors.Descriptor
    getDescriptor() {
        return com.flipkart.vbroker.proto.PResponses.internal_static_proto_GetTopicsResponse_descriptor;
    }

    public static com.flipkart.vbroker.proto.GetTopicsResponse parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }

    public static com.flipkart.vbroker.proto.GetTopicsResponse parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }

    public static com.flipkart.vbroker.proto.GetTopicsResponse parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }

    public static com.flipkart.vbroker.proto.GetTopicsResponse parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }

    public static com.flipkart.vbroker.proto.GetTopicsResponse parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }

    public static com.flipkart.vbroker.proto.GetTopicsResponse parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }

    public static com.flipkart.vbroker.proto.GetTopicsResponse parseFrom(java.io.InputStream input)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseWithIOException(PARSER, input);
    }

    public static com.flipkart.vbroker.proto.GetTopicsResponse parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static com.flipkart.vbroker.proto.GetTopicsResponse parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseDelimitedWithIOException(PARSER, input);
    }

    public static com.flipkart.vbroker.proto.GetTopicsResponse parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }

    public static com.flipkart.vbroker.proto.GetTopicsResponse parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseWithIOException(PARSER, input);
    }

    public static com.flipkart.vbroker.proto.GetTopicsResponse parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static Builder newBuilder() {
        return DEFAULT_INSTANCE.toBuilder();
    }

    public static Builder newBuilder(com.flipkart.vbroker.proto.GetTopicsResponse prototype) {
        return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }

    public static com.flipkart.vbroker.proto.GetTopicsResponse getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    public static com.google.protobuf.Parser<GetTopicsResponse> parser() {
        return PARSER;
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
        return this.unknownFields;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
    internalGetFieldAccessorTable() {
        return com.flipkart.vbroker.proto.PResponses.internal_static_proto_GetTopicsResponse_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                com.flipkart.vbroker.proto.GetTopicsResponse.class, com.flipkart.vbroker.proto.GetTopicsResponse.Builder.class);
    }

    /**
     * <code>repeated .proto.GetTopicResponse topicResponses = 1;</code>
     */
    public java.util.List<com.flipkart.vbroker.proto.GetTopicResponse> getTopicResponsesList() {
        return topicResponses_;
    }

    /**
     * <code>repeated .proto.GetTopicResponse topicResponses = 1;</code>
     */
    public java.util.List<? extends com.flipkart.vbroker.proto.GetTopicResponseOrBuilder>
    getTopicResponsesOrBuilderList() {
        return topicResponses_;
    }

    /**
     * <code>repeated .proto.GetTopicResponse topicResponses = 1;</code>
     */
    public int getTopicResponsesCount() {
        return topicResponses_.size();
    }

    /**
     * <code>repeated .proto.GetTopicResponse topicResponses = 1;</code>
     */
    public com.flipkart.vbroker.proto.GetTopicResponse getTopicResponses(int index) {
        return topicResponses_.get(index);
    }

    /**
     * <code>repeated .proto.GetTopicResponse topicResponses = 1;</code>
     */
    public com.flipkart.vbroker.proto.GetTopicResponseOrBuilder getTopicResponsesOrBuilder(
        int index) {
        return topicResponses_.get(index);
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
        for (int i = 0; i < topicResponses_.size(); i++) {
            output.writeMessage(1, topicResponses_.get(i));
        }
        unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
        int size = memoizedSize;
        if (size != -1) return size;

        size = 0;
        for (int i = 0; i < topicResponses_.size(); i++) {
            size += com.google.protobuf.CodedOutputStream
                .computeMessageSize(1, topicResponses_.get(i));
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
        if (!(obj instanceof com.flipkart.vbroker.proto.GetTopicsResponse)) {
            return super.equals(obj);
        }
        com.flipkart.vbroker.proto.GetTopicsResponse other = (com.flipkart.vbroker.proto.GetTopicsResponse) obj;

        boolean result = true;
        result = result && getTopicResponsesList()
            .equals(other.getTopicResponsesList());
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
        if (getTopicResponsesCount() > 0) {
            hash = (37 * hash) + TOPICRESPONSES_FIELD_NUMBER;
            hash = (53 * hash) + getTopicResponsesList().hashCode();
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
    public com.google.protobuf.Parser<GetTopicsResponse> getParserForType() {
        return PARSER;
    }

    public com.flipkart.vbroker.proto.GetTopicsResponse getDefaultInstanceForType() {
        return DEFAULT_INSTANCE;
    }

    /**
     * Protobuf type {@code proto.GetTopicsResponse}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:proto.GetTopicsResponse)
        com.flipkart.vbroker.proto.GetTopicsResponseOrBuilder {
        private int bitField0_;
        private java.util.List<com.flipkart.vbroker.proto.GetTopicResponse> topicResponses_ =
            java.util.Collections.emptyList();
        private com.google.protobuf.RepeatedFieldBuilderV3<
            com.flipkart.vbroker.proto.GetTopicResponse, com.flipkart.vbroker.proto.GetTopicResponse.Builder, com.flipkart.vbroker.proto.GetTopicResponseOrBuilder> topicResponsesBuilder_;

        // Construct using com.flipkart.vbroker.proto.GetTopicsResponse.newBuilder()
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
            return com.flipkart.vbroker.proto.PResponses.internal_static_proto_GetTopicsResponse_descriptor;
        }

        protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
            return com.flipkart.vbroker.proto.PResponses.internal_static_proto_GetTopicsResponse_fieldAccessorTable
                .ensureFieldAccessorsInitialized(
                    com.flipkart.vbroker.proto.GetTopicsResponse.class, com.flipkart.vbroker.proto.GetTopicsResponse.Builder.class);
        }

        private void maybeForceBuilderInitialization() {
            if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
                getTopicResponsesFieldBuilder();
            }
        }

        public Builder clear() {
            super.clear();
            if (topicResponsesBuilder_ == null) {
                topicResponses_ = java.util.Collections.emptyList();
                bitField0_ = (bitField0_ & ~0x00000001);
            } else {
                topicResponsesBuilder_.clear();
            }
            return this;
        }

        public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
            return com.flipkart.vbroker.proto.PResponses.internal_static_proto_GetTopicsResponse_descriptor;
        }

        public com.flipkart.vbroker.proto.GetTopicsResponse getDefaultInstanceForType() {
            return com.flipkart.vbroker.proto.GetTopicsResponse.getDefaultInstance();
        }

        public com.flipkart.vbroker.proto.GetTopicsResponse build() {
            com.flipkart.vbroker.proto.GetTopicsResponse result = buildPartial();
            if (!result.isInitialized()) {
                throw newUninitializedMessageException(result);
            }
            return result;
        }

        public com.flipkart.vbroker.proto.GetTopicsResponse buildPartial() {
            com.flipkart.vbroker.proto.GetTopicsResponse result = new com.flipkart.vbroker.proto.GetTopicsResponse(this);
            int from_bitField0_ = bitField0_;
            if (topicResponsesBuilder_ == null) {
                if (((bitField0_ & 0x00000001) == 0x00000001)) {
                    topicResponses_ = java.util.Collections.unmodifiableList(topicResponses_);
                    bitField0_ = (bitField0_ & ~0x00000001);
                }
                result.topicResponses_ = topicResponses_;
            } else {
                result.topicResponses_ = topicResponsesBuilder_.build();
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
            if (other instanceof com.flipkart.vbroker.proto.GetTopicsResponse) {
                return mergeFrom((com.flipkart.vbroker.proto.GetTopicsResponse) other);
            } else {
                super.mergeFrom(other);
                return this;
            }
        }

        public Builder mergeFrom(com.flipkart.vbroker.proto.GetTopicsResponse other) {
            if (other == com.flipkart.vbroker.proto.GetTopicsResponse.getDefaultInstance()) return this;
            if (topicResponsesBuilder_ == null) {
                if (!other.topicResponses_.isEmpty()) {
                    if (topicResponses_.isEmpty()) {
                        topicResponses_ = other.topicResponses_;
                        bitField0_ = (bitField0_ & ~0x00000001);
                    } else {
                        ensureTopicResponsesIsMutable();
                        topicResponses_.addAll(other.topicResponses_);
                    }
                    onChanged();
                }
            } else {
                if (!other.topicResponses_.isEmpty()) {
                    if (topicResponsesBuilder_.isEmpty()) {
                        topicResponsesBuilder_.dispose();
                        topicResponsesBuilder_ = null;
                        topicResponses_ = other.topicResponses_;
                        bitField0_ = (bitField0_ & ~0x00000001);
                        topicResponsesBuilder_ =
                            com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                                getTopicResponsesFieldBuilder() : null;
                    } else {
                        topicResponsesBuilder_.addAllMessages(other.topicResponses_);
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
            com.flipkart.vbroker.proto.GetTopicsResponse parsedMessage = null;
            try {
                parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                parsedMessage = (com.flipkart.vbroker.proto.GetTopicsResponse) e.getUnfinishedMessage();
                throw e.unwrapIOException();
            } finally {
                if (parsedMessage != null) {
                    mergeFrom(parsedMessage);
                }
            }
            return this;
        }

        private void ensureTopicResponsesIsMutable() {
            if (!((bitField0_ & 0x00000001) == 0x00000001)) {
                topicResponses_ = new java.util.ArrayList<com.flipkart.vbroker.proto.GetTopicResponse>(topicResponses_);
                bitField0_ |= 0x00000001;
            }
        }

        /**
         * <code>repeated .proto.GetTopicResponse topicResponses = 1;</code>
         */
        public java.util.List<com.flipkart.vbroker.proto.GetTopicResponse> getTopicResponsesList() {
            if (topicResponsesBuilder_ == null) {
                return java.util.Collections.unmodifiableList(topicResponses_);
            } else {
                return topicResponsesBuilder_.getMessageList();
            }
        }

        /**
         * <code>repeated .proto.GetTopicResponse topicResponses = 1;</code>
         */
        public int getTopicResponsesCount() {
            if (topicResponsesBuilder_ == null) {
                return topicResponses_.size();
            } else {
                return topicResponsesBuilder_.getCount();
            }
        }

        /**
         * <code>repeated .proto.GetTopicResponse topicResponses = 1;</code>
         */
        public com.flipkart.vbroker.proto.GetTopicResponse getTopicResponses(int index) {
            if (topicResponsesBuilder_ == null) {
                return topicResponses_.get(index);
            } else {
                return topicResponsesBuilder_.getMessage(index);
            }
        }

        /**
         * <code>repeated .proto.GetTopicResponse topicResponses = 1;</code>
         */
        public Builder setTopicResponses(
            int index, com.flipkart.vbroker.proto.GetTopicResponse value) {
            if (topicResponsesBuilder_ == null) {
                if (value == null) {
                    throw new NullPointerException();
                }
                ensureTopicResponsesIsMutable();
                topicResponses_.set(index, value);
                onChanged();
            } else {
                topicResponsesBuilder_.setMessage(index, value);
            }
            return this;
        }

        /**
         * <code>repeated .proto.GetTopicResponse topicResponses = 1;</code>
         */
        public Builder setTopicResponses(
            int index, com.flipkart.vbroker.proto.GetTopicResponse.Builder builderForValue) {
            if (topicResponsesBuilder_ == null) {
                ensureTopicResponsesIsMutable();
                topicResponses_.set(index, builderForValue.build());
                onChanged();
            } else {
                topicResponsesBuilder_.setMessage(index, builderForValue.build());
            }
            return this;
        }

        /**
         * <code>repeated .proto.GetTopicResponse topicResponses = 1;</code>
         */
        public Builder addTopicResponses(com.flipkart.vbroker.proto.GetTopicResponse value) {
            if (topicResponsesBuilder_ == null) {
                if (value == null) {
                    throw new NullPointerException();
                }
                ensureTopicResponsesIsMutable();
                topicResponses_.add(value);
                onChanged();
            } else {
                topicResponsesBuilder_.addMessage(value);
            }
            return this;
        }

        /**
         * <code>repeated .proto.GetTopicResponse topicResponses = 1;</code>
         */
        public Builder addTopicResponses(
            int index, com.flipkart.vbroker.proto.GetTopicResponse value) {
            if (topicResponsesBuilder_ == null) {
                if (value == null) {
                    throw new NullPointerException();
                }
                ensureTopicResponsesIsMutable();
                topicResponses_.add(index, value);
                onChanged();
            } else {
                topicResponsesBuilder_.addMessage(index, value);
            }
            return this;
        }

        /**
         * <code>repeated .proto.GetTopicResponse topicResponses = 1;</code>
         */
        public Builder addTopicResponses(
            com.flipkart.vbroker.proto.GetTopicResponse.Builder builderForValue) {
            if (topicResponsesBuilder_ == null) {
                ensureTopicResponsesIsMutable();
                topicResponses_.add(builderForValue.build());
                onChanged();
            } else {
                topicResponsesBuilder_.addMessage(builderForValue.build());
            }
            return this;
        }

        /**
         * <code>repeated .proto.GetTopicResponse topicResponses = 1;</code>
         */
        public Builder addTopicResponses(
            int index, com.flipkart.vbroker.proto.GetTopicResponse.Builder builderForValue) {
            if (topicResponsesBuilder_ == null) {
                ensureTopicResponsesIsMutable();
                topicResponses_.add(index, builderForValue.build());
                onChanged();
            } else {
                topicResponsesBuilder_.addMessage(index, builderForValue.build());
            }
            return this;
        }

        /**
         * <code>repeated .proto.GetTopicResponse topicResponses = 1;</code>
         */
        public Builder addAllTopicResponses(
            java.lang.Iterable<? extends com.flipkart.vbroker.proto.GetTopicResponse> values) {
            if (topicResponsesBuilder_ == null) {
                ensureTopicResponsesIsMutable();
                com.google.protobuf.AbstractMessageLite.Builder.addAll(
                    values, topicResponses_);
                onChanged();
            } else {
                topicResponsesBuilder_.addAllMessages(values);
            }
            return this;
        }

        /**
         * <code>repeated .proto.GetTopicResponse topicResponses = 1;</code>
         */
        public Builder clearTopicResponses() {
            if (topicResponsesBuilder_ == null) {
                topicResponses_ = java.util.Collections.emptyList();
                bitField0_ = (bitField0_ & ~0x00000001);
                onChanged();
            } else {
                topicResponsesBuilder_.clear();
            }
            return this;
        }

        /**
         * <code>repeated .proto.GetTopicResponse topicResponses = 1;</code>
         */
        public Builder removeTopicResponses(int index) {
            if (topicResponsesBuilder_ == null) {
                ensureTopicResponsesIsMutable();
                topicResponses_.remove(index);
                onChanged();
            } else {
                topicResponsesBuilder_.remove(index);
            }
            return this;
        }

        /**
         * <code>repeated .proto.GetTopicResponse topicResponses = 1;</code>
         */
        public com.flipkart.vbroker.proto.GetTopicResponse.Builder getTopicResponsesBuilder(
            int index) {
            return getTopicResponsesFieldBuilder().getBuilder(index);
        }

        /**
         * <code>repeated .proto.GetTopicResponse topicResponses = 1;</code>
         */
        public com.flipkart.vbroker.proto.GetTopicResponseOrBuilder getTopicResponsesOrBuilder(
            int index) {
            if (topicResponsesBuilder_ == null) {
                return topicResponses_.get(index);
            } else {
                return topicResponsesBuilder_.getMessageOrBuilder(index);
            }
        }

        /**
         * <code>repeated .proto.GetTopicResponse topicResponses = 1;</code>
         */
        public java.util.List<? extends com.flipkart.vbroker.proto.GetTopicResponseOrBuilder>
        getTopicResponsesOrBuilderList() {
            if (topicResponsesBuilder_ != null) {
                return topicResponsesBuilder_.getMessageOrBuilderList();
            } else {
                return java.util.Collections.unmodifiableList(topicResponses_);
            }
        }

        /**
         * <code>repeated .proto.GetTopicResponse topicResponses = 1;</code>
         */
        public com.flipkart.vbroker.proto.GetTopicResponse.Builder addTopicResponsesBuilder() {
            return getTopicResponsesFieldBuilder().addBuilder(
                com.flipkart.vbroker.proto.GetTopicResponse.getDefaultInstance());
        }

        /**
         * <code>repeated .proto.GetTopicResponse topicResponses = 1;</code>
         */
        public com.flipkart.vbroker.proto.GetTopicResponse.Builder addTopicResponsesBuilder(
            int index) {
            return getTopicResponsesFieldBuilder().addBuilder(
                index, com.flipkart.vbroker.proto.GetTopicResponse.getDefaultInstance());
        }

        /**
         * <code>repeated .proto.GetTopicResponse topicResponses = 1;</code>
         */
        public java.util.List<com.flipkart.vbroker.proto.GetTopicResponse.Builder>
        getTopicResponsesBuilderList() {
            return getTopicResponsesFieldBuilder().getBuilderList();
        }

        private com.google.protobuf.RepeatedFieldBuilderV3<
            com.flipkart.vbroker.proto.GetTopicResponse, com.flipkart.vbroker.proto.GetTopicResponse.Builder, com.flipkart.vbroker.proto.GetTopicResponseOrBuilder>
        getTopicResponsesFieldBuilder() {
            if (topicResponsesBuilder_ == null) {
                topicResponsesBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
                    com.flipkart.vbroker.proto.GetTopicResponse, com.flipkart.vbroker.proto.GetTopicResponse.Builder, com.flipkart.vbroker.proto.GetTopicResponseOrBuilder>(
                    topicResponses_,
                    ((bitField0_ & 0x00000001) == 0x00000001),
                    getParentForChildren(),
                    isClean());
                topicResponses_ = null;
            }
            return topicResponsesBuilder_;
        }

        public final Builder setUnknownFields(
            final com.google.protobuf.UnknownFieldSet unknownFields) {
            return super.setUnknownFieldsProto3(unknownFields);
        }

        public final Builder mergeUnknownFields(
            final com.google.protobuf.UnknownFieldSet unknownFields) {
            return super.mergeUnknownFields(unknownFields);
        }


        // @@protoc_insertion_point(builder_scope:proto.GetTopicsResponse)
    }

}

