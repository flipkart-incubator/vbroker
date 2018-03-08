// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: responses.proto

package com.flipkart.vbroker.proto;

/**
 * Protobuf type {@code proto.CreateTopicsResponse}
 */
public final class CreateTopicsResponse extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:proto.CreateTopicsResponse)
    CreateTopicsResponseOrBuilder {
    public static final int CREATETOPICSRESPONSE_FIELD_NUMBER = 1;
    private static final long serialVersionUID = 0L;
    // @@protoc_insertion_point(class_scope:proto.CreateTopicsResponse)
    private static final com.flipkart.vbroker.proto.CreateTopicsResponse DEFAULT_INSTANCE;
    private static final com.google.protobuf.Parser<CreateTopicsResponse>
        PARSER = new com.google.protobuf.AbstractParser<CreateTopicsResponse>() {
        public CreateTopicsResponse parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
            return new CreateTopicsResponse(input, extensionRegistry);
        }
    };

    static {
        DEFAULT_INSTANCE = new com.flipkart.vbroker.proto.CreateTopicsResponse();
    }

    private java.util.List<com.flipkart.vbroker.proto.CreateTopicResponse> createTopicsResponse_;
    private byte memoizedIsInitialized = -1;

    // Use CreateTopicsResponse.newBuilder() to construct.
    private CreateTopicsResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
        super(builder);
    }
    private CreateTopicsResponse() {
        createTopicsResponse_ = java.util.Collections.emptyList();
    }

    private CreateTopicsResponse(
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
                            createTopicsResponse_ = new java.util.ArrayList<com.flipkart.vbroker.proto.CreateTopicResponse>();
                            mutable_bitField0_ |= 0x00000001;
                        }
                        createTopicsResponse_.add(
                            input.readMessage(com.flipkart.vbroker.proto.CreateTopicResponse.parser(), extensionRegistry));
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
                createTopicsResponse_ = java.util.Collections.unmodifiableList(createTopicsResponse_);
            }
            this.unknownFields = unknownFields.build();
            makeExtensionsImmutable();
        }
    }

    public static final com.google.protobuf.Descriptors.Descriptor
    getDescriptor() {
        return com.flipkart.vbroker.proto.PResponses.internal_static_proto_CreateTopicsResponse_descriptor;
    }

    public static com.flipkart.vbroker.proto.CreateTopicsResponse parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }

    public static com.flipkart.vbroker.proto.CreateTopicsResponse parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }

    public static com.flipkart.vbroker.proto.CreateTopicsResponse parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }

    public static com.flipkart.vbroker.proto.CreateTopicsResponse parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }

    public static com.flipkart.vbroker.proto.CreateTopicsResponse parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }

    public static com.flipkart.vbroker.proto.CreateTopicsResponse parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }

    public static com.flipkart.vbroker.proto.CreateTopicsResponse parseFrom(java.io.InputStream input)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseWithIOException(PARSER, input);
    }

    public static com.flipkart.vbroker.proto.CreateTopicsResponse parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static com.flipkart.vbroker.proto.CreateTopicsResponse parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseDelimitedWithIOException(PARSER, input);
    }

    public static com.flipkart.vbroker.proto.CreateTopicsResponse parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }

    public static com.flipkart.vbroker.proto.CreateTopicsResponse parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseWithIOException(PARSER, input);
    }

    public static com.flipkart.vbroker.proto.CreateTopicsResponse parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static Builder newBuilder() {
        return DEFAULT_INSTANCE.toBuilder();
    }

    public static Builder newBuilder(com.flipkart.vbroker.proto.CreateTopicsResponse prototype) {
        return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }

    public static com.flipkart.vbroker.proto.CreateTopicsResponse getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    public static com.google.protobuf.Parser<CreateTopicsResponse> parser() {
        return PARSER;
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
        return this.unknownFields;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
    internalGetFieldAccessorTable() {
        return com.flipkart.vbroker.proto.PResponses.internal_static_proto_CreateTopicsResponse_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                com.flipkart.vbroker.proto.CreateTopicsResponse.class, com.flipkart.vbroker.proto.CreateTopicsResponse.Builder.class);
    }

    /**
     * <code>repeated .proto.CreateTopicResponse createTopicsResponse = 1;</code>
     */
    public java.util.List<com.flipkart.vbroker.proto.CreateTopicResponse> getCreateTopicsResponseList() {
        return createTopicsResponse_;
    }

    /**
     * <code>repeated .proto.CreateTopicResponse createTopicsResponse = 1;</code>
     */
    public java.util.List<? extends com.flipkart.vbroker.proto.CreateTopicResponseOrBuilder>
    getCreateTopicsResponseOrBuilderList() {
        return createTopicsResponse_;
    }

    /**
     * <code>repeated .proto.CreateTopicResponse createTopicsResponse = 1;</code>
     */
    public int getCreateTopicsResponseCount() {
        return createTopicsResponse_.size();
    }

    /**
     * <code>repeated .proto.CreateTopicResponse createTopicsResponse = 1;</code>
     */
    public com.flipkart.vbroker.proto.CreateTopicResponse getCreateTopicsResponse(int index) {
        return createTopicsResponse_.get(index);
    }

    /**
     * <code>repeated .proto.CreateTopicResponse createTopicsResponse = 1;</code>
     */
    public com.flipkart.vbroker.proto.CreateTopicResponseOrBuilder getCreateTopicsResponseOrBuilder(
        int index) {
        return createTopicsResponse_.get(index);
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
        for (int i = 0; i < createTopicsResponse_.size(); i++) {
            output.writeMessage(1, createTopicsResponse_.get(i));
        }
        unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
        int size = memoizedSize;
        if (size != -1) return size;

        size = 0;
        for (int i = 0; i < createTopicsResponse_.size(); i++) {
            size += com.google.protobuf.CodedOutputStream
                .computeMessageSize(1, createTopicsResponse_.get(i));
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
        if (!(obj instanceof com.flipkart.vbroker.proto.CreateTopicsResponse)) {
            return super.equals(obj);
        }
        com.flipkart.vbroker.proto.CreateTopicsResponse other = (com.flipkart.vbroker.proto.CreateTopicsResponse) obj;

        boolean result = true;
        result = result && getCreateTopicsResponseList()
            .equals(other.getCreateTopicsResponseList());
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
        if (getCreateTopicsResponseCount() > 0) {
            hash = (37 * hash) + CREATETOPICSRESPONSE_FIELD_NUMBER;
            hash = (53 * hash) + getCreateTopicsResponseList().hashCode();
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
    public com.google.protobuf.Parser<CreateTopicsResponse> getParserForType() {
        return PARSER;
    }

    public com.flipkart.vbroker.proto.CreateTopicsResponse getDefaultInstanceForType() {
        return DEFAULT_INSTANCE;
    }

    /**
     * Protobuf type {@code proto.CreateTopicsResponse}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:proto.CreateTopicsResponse)
        com.flipkart.vbroker.proto.CreateTopicsResponseOrBuilder {
        private int bitField0_;
        private java.util.List<com.flipkart.vbroker.proto.CreateTopicResponse> createTopicsResponse_ =
            java.util.Collections.emptyList();
        private com.google.protobuf.RepeatedFieldBuilderV3<
            com.flipkart.vbroker.proto.CreateTopicResponse, com.flipkart.vbroker.proto.CreateTopicResponse.Builder, com.flipkart.vbroker.proto.CreateTopicResponseOrBuilder> createTopicsResponseBuilder_;

        // Construct using com.flipkart.vbroker.proto.CreateTopicsResponse.newBuilder()
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
            return com.flipkart.vbroker.proto.PResponses.internal_static_proto_CreateTopicsResponse_descriptor;
        }

        protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
            return com.flipkart.vbroker.proto.PResponses.internal_static_proto_CreateTopicsResponse_fieldAccessorTable
                .ensureFieldAccessorsInitialized(
                    com.flipkart.vbroker.proto.CreateTopicsResponse.class, com.flipkart.vbroker.proto.CreateTopicsResponse.Builder.class);
        }

        private void maybeForceBuilderInitialization() {
            if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
                getCreateTopicsResponseFieldBuilder();
            }
        }

        public Builder clear() {
            super.clear();
            if (createTopicsResponseBuilder_ == null) {
                createTopicsResponse_ = java.util.Collections.emptyList();
                bitField0_ = (bitField0_ & ~0x00000001);
            } else {
                createTopicsResponseBuilder_.clear();
            }
            return this;
        }

        public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
            return com.flipkart.vbroker.proto.PResponses.internal_static_proto_CreateTopicsResponse_descriptor;
        }

        public com.flipkart.vbroker.proto.CreateTopicsResponse getDefaultInstanceForType() {
            return com.flipkart.vbroker.proto.CreateTopicsResponse.getDefaultInstance();
        }

        public com.flipkart.vbroker.proto.CreateTopicsResponse build() {
            com.flipkart.vbroker.proto.CreateTopicsResponse result = buildPartial();
            if (!result.isInitialized()) {
                throw newUninitializedMessageException(result);
            }
            return result;
        }

        public com.flipkart.vbroker.proto.CreateTopicsResponse buildPartial() {
            com.flipkart.vbroker.proto.CreateTopicsResponse result = new com.flipkart.vbroker.proto.CreateTopicsResponse(this);
            int from_bitField0_ = bitField0_;
            if (createTopicsResponseBuilder_ == null) {
                if (((bitField0_ & 0x00000001) == 0x00000001)) {
                    createTopicsResponse_ = java.util.Collections.unmodifiableList(createTopicsResponse_);
                    bitField0_ = (bitField0_ & ~0x00000001);
                }
                result.createTopicsResponse_ = createTopicsResponse_;
            } else {
                result.createTopicsResponse_ = createTopicsResponseBuilder_.build();
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
            if (other instanceof com.flipkart.vbroker.proto.CreateTopicsResponse) {
                return mergeFrom((com.flipkart.vbroker.proto.CreateTopicsResponse) other);
            } else {
                super.mergeFrom(other);
                return this;
            }
        }

        public Builder mergeFrom(com.flipkart.vbroker.proto.CreateTopicsResponse other) {
            if (other == com.flipkart.vbroker.proto.CreateTopicsResponse.getDefaultInstance()) return this;
            if (createTopicsResponseBuilder_ == null) {
                if (!other.createTopicsResponse_.isEmpty()) {
                    if (createTopicsResponse_.isEmpty()) {
                        createTopicsResponse_ = other.createTopicsResponse_;
                        bitField0_ = (bitField0_ & ~0x00000001);
                    } else {
                        ensureCreateTopicsResponseIsMutable();
                        createTopicsResponse_.addAll(other.createTopicsResponse_);
                    }
                    onChanged();
                }
            } else {
                if (!other.createTopicsResponse_.isEmpty()) {
                    if (createTopicsResponseBuilder_.isEmpty()) {
                        createTopicsResponseBuilder_.dispose();
                        createTopicsResponseBuilder_ = null;
                        createTopicsResponse_ = other.createTopicsResponse_;
                        bitField0_ = (bitField0_ & ~0x00000001);
                        createTopicsResponseBuilder_ =
                            com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                                getCreateTopicsResponseFieldBuilder() : null;
                    } else {
                        createTopicsResponseBuilder_.addAllMessages(other.createTopicsResponse_);
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
            com.flipkart.vbroker.proto.CreateTopicsResponse parsedMessage = null;
            try {
                parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                parsedMessage = (com.flipkart.vbroker.proto.CreateTopicsResponse) e.getUnfinishedMessage();
                throw e.unwrapIOException();
            } finally {
                if (parsedMessage != null) {
                    mergeFrom(parsedMessage);
                }
            }
            return this;
        }

        private void ensureCreateTopicsResponseIsMutable() {
            if (!((bitField0_ & 0x00000001) == 0x00000001)) {
                createTopicsResponse_ = new java.util.ArrayList<com.flipkart.vbroker.proto.CreateTopicResponse>(createTopicsResponse_);
                bitField0_ |= 0x00000001;
            }
        }

        /**
         * <code>repeated .proto.CreateTopicResponse createTopicsResponse = 1;</code>
         */
        public java.util.List<com.flipkart.vbroker.proto.CreateTopicResponse> getCreateTopicsResponseList() {
            if (createTopicsResponseBuilder_ == null) {
                return java.util.Collections.unmodifiableList(createTopicsResponse_);
            } else {
                return createTopicsResponseBuilder_.getMessageList();
            }
        }

        /**
         * <code>repeated .proto.CreateTopicResponse createTopicsResponse = 1;</code>
         */
        public int getCreateTopicsResponseCount() {
            if (createTopicsResponseBuilder_ == null) {
                return createTopicsResponse_.size();
            } else {
                return createTopicsResponseBuilder_.getCount();
            }
        }

        /**
         * <code>repeated .proto.CreateTopicResponse createTopicsResponse = 1;</code>
         */
        public com.flipkart.vbroker.proto.CreateTopicResponse getCreateTopicsResponse(int index) {
            if (createTopicsResponseBuilder_ == null) {
                return createTopicsResponse_.get(index);
            } else {
                return createTopicsResponseBuilder_.getMessage(index);
            }
        }

        /**
         * <code>repeated .proto.CreateTopicResponse createTopicsResponse = 1;</code>
         */
        public Builder setCreateTopicsResponse(
            int index, com.flipkart.vbroker.proto.CreateTopicResponse value) {
            if (createTopicsResponseBuilder_ == null) {
                if (value == null) {
                    throw new NullPointerException();
                }
                ensureCreateTopicsResponseIsMutable();
                createTopicsResponse_.set(index, value);
                onChanged();
            } else {
                createTopicsResponseBuilder_.setMessage(index, value);
            }
            return this;
        }

        /**
         * <code>repeated .proto.CreateTopicResponse createTopicsResponse = 1;</code>
         */
        public Builder setCreateTopicsResponse(
            int index, com.flipkart.vbroker.proto.CreateTopicResponse.Builder builderForValue) {
            if (createTopicsResponseBuilder_ == null) {
                ensureCreateTopicsResponseIsMutable();
                createTopicsResponse_.set(index, builderForValue.build());
                onChanged();
            } else {
                createTopicsResponseBuilder_.setMessage(index, builderForValue.build());
            }
            return this;
        }

        /**
         * <code>repeated .proto.CreateTopicResponse createTopicsResponse = 1;</code>
         */
        public Builder addCreateTopicsResponse(com.flipkart.vbroker.proto.CreateTopicResponse value) {
            if (createTopicsResponseBuilder_ == null) {
                if (value == null) {
                    throw new NullPointerException();
                }
                ensureCreateTopicsResponseIsMutable();
                createTopicsResponse_.add(value);
                onChanged();
            } else {
                createTopicsResponseBuilder_.addMessage(value);
            }
            return this;
        }

        /**
         * <code>repeated .proto.CreateTopicResponse createTopicsResponse = 1;</code>
         */
        public Builder addCreateTopicsResponse(
            int index, com.flipkart.vbroker.proto.CreateTopicResponse value) {
            if (createTopicsResponseBuilder_ == null) {
                if (value == null) {
                    throw new NullPointerException();
                }
                ensureCreateTopicsResponseIsMutable();
                createTopicsResponse_.add(index, value);
                onChanged();
            } else {
                createTopicsResponseBuilder_.addMessage(index, value);
            }
            return this;
        }

        /**
         * <code>repeated .proto.CreateTopicResponse createTopicsResponse = 1;</code>
         */
        public Builder addCreateTopicsResponse(
            com.flipkart.vbroker.proto.CreateTopicResponse.Builder builderForValue) {
            if (createTopicsResponseBuilder_ == null) {
                ensureCreateTopicsResponseIsMutable();
                createTopicsResponse_.add(builderForValue.build());
                onChanged();
            } else {
                createTopicsResponseBuilder_.addMessage(builderForValue.build());
            }
            return this;
        }

        /**
         * <code>repeated .proto.CreateTopicResponse createTopicsResponse = 1;</code>
         */
        public Builder addCreateTopicsResponse(
            int index, com.flipkart.vbroker.proto.CreateTopicResponse.Builder builderForValue) {
            if (createTopicsResponseBuilder_ == null) {
                ensureCreateTopicsResponseIsMutable();
                createTopicsResponse_.add(index, builderForValue.build());
                onChanged();
            } else {
                createTopicsResponseBuilder_.addMessage(index, builderForValue.build());
            }
            return this;
        }

        /**
         * <code>repeated .proto.CreateTopicResponse createTopicsResponse = 1;</code>
         */
        public Builder addAllCreateTopicsResponse(
            java.lang.Iterable<? extends com.flipkart.vbroker.proto.CreateTopicResponse> values) {
            if (createTopicsResponseBuilder_ == null) {
                ensureCreateTopicsResponseIsMutable();
                com.google.protobuf.AbstractMessageLite.Builder.addAll(
                    values, createTopicsResponse_);
                onChanged();
            } else {
                createTopicsResponseBuilder_.addAllMessages(values);
            }
            return this;
        }

        /**
         * <code>repeated .proto.CreateTopicResponse createTopicsResponse = 1;</code>
         */
        public Builder clearCreateTopicsResponse() {
            if (createTopicsResponseBuilder_ == null) {
                createTopicsResponse_ = java.util.Collections.emptyList();
                bitField0_ = (bitField0_ & ~0x00000001);
                onChanged();
            } else {
                createTopicsResponseBuilder_.clear();
            }
            return this;
        }

        /**
         * <code>repeated .proto.CreateTopicResponse createTopicsResponse = 1;</code>
         */
        public Builder removeCreateTopicsResponse(int index) {
            if (createTopicsResponseBuilder_ == null) {
                ensureCreateTopicsResponseIsMutable();
                createTopicsResponse_.remove(index);
                onChanged();
            } else {
                createTopicsResponseBuilder_.remove(index);
            }
            return this;
        }

        /**
         * <code>repeated .proto.CreateTopicResponse createTopicsResponse = 1;</code>
         */
        public com.flipkart.vbroker.proto.CreateTopicResponse.Builder getCreateTopicsResponseBuilder(
            int index) {
            return getCreateTopicsResponseFieldBuilder().getBuilder(index);
        }

        /**
         * <code>repeated .proto.CreateTopicResponse createTopicsResponse = 1;</code>
         */
        public com.flipkart.vbroker.proto.CreateTopicResponseOrBuilder getCreateTopicsResponseOrBuilder(
            int index) {
            if (createTopicsResponseBuilder_ == null) {
                return createTopicsResponse_.get(index);
            } else {
                return createTopicsResponseBuilder_.getMessageOrBuilder(index);
            }
        }

        /**
         * <code>repeated .proto.CreateTopicResponse createTopicsResponse = 1;</code>
         */
        public java.util.List<? extends com.flipkart.vbroker.proto.CreateTopicResponseOrBuilder>
        getCreateTopicsResponseOrBuilderList() {
            if (createTopicsResponseBuilder_ != null) {
                return createTopicsResponseBuilder_.getMessageOrBuilderList();
            } else {
                return java.util.Collections.unmodifiableList(createTopicsResponse_);
            }
        }

        /**
         * <code>repeated .proto.CreateTopicResponse createTopicsResponse = 1;</code>
         */
        public com.flipkart.vbroker.proto.CreateTopicResponse.Builder addCreateTopicsResponseBuilder() {
            return getCreateTopicsResponseFieldBuilder().addBuilder(
                com.flipkart.vbroker.proto.CreateTopicResponse.getDefaultInstance());
        }

        /**
         * <code>repeated .proto.CreateTopicResponse createTopicsResponse = 1;</code>
         */
        public com.flipkart.vbroker.proto.CreateTopicResponse.Builder addCreateTopicsResponseBuilder(
            int index) {
            return getCreateTopicsResponseFieldBuilder().addBuilder(
                index, com.flipkart.vbroker.proto.CreateTopicResponse.getDefaultInstance());
        }

        /**
         * <code>repeated .proto.CreateTopicResponse createTopicsResponse = 1;</code>
         */
        public java.util.List<com.flipkart.vbroker.proto.CreateTopicResponse.Builder>
        getCreateTopicsResponseBuilderList() {
            return getCreateTopicsResponseFieldBuilder().getBuilderList();
        }

        private com.google.protobuf.RepeatedFieldBuilderV3<
            com.flipkart.vbroker.proto.CreateTopicResponse, com.flipkart.vbroker.proto.CreateTopicResponse.Builder, com.flipkart.vbroker.proto.CreateTopicResponseOrBuilder>
        getCreateTopicsResponseFieldBuilder() {
            if (createTopicsResponseBuilder_ == null) {
                createTopicsResponseBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
                    com.flipkart.vbroker.proto.CreateTopicResponse, com.flipkart.vbroker.proto.CreateTopicResponse.Builder, com.flipkart.vbroker.proto.CreateTopicResponseOrBuilder>(
                    createTopicsResponse_,
                    ((bitField0_ & 0x00000001) == 0x00000001),
                    getParentForChildren(),
                    isClean());
                createTopicsResponse_ = null;
            }
            return createTopicsResponseBuilder_;
        }

        public final Builder setUnknownFields(
            final com.google.protobuf.UnknownFieldSet unknownFields) {
            return super.setUnknownFieldsProto3(unknownFields);
        }

        public final Builder mergeUnknownFields(
            final com.google.protobuf.UnknownFieldSet unknownFields) {
            return super.mergeUnknownFields(unknownFields);
        }


        // @@protoc_insertion_point(builder_scope:proto.CreateTopicsResponse)
    }

}

