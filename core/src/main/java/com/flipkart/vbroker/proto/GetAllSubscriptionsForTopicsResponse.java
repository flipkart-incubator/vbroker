// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: responses.proto

package com.flipkart.vbroker.proto;

/**
 * Protobuf type {@code proto.GetAllSubscriptionsForTopicsResponse}
 */
public final class GetAllSubscriptionsForTopicsResponse extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:proto.GetAllSubscriptionsForTopicsResponse)
    GetAllSubscriptionsForTopicsResponseOrBuilder {
    public static final int GETALLSUBSCRIPTIONSFORTOPICRESPONSES_FIELD_NUMBER = 1;
    private static final long serialVersionUID = 0L;
    // @@protoc_insertion_point(class_scope:proto.GetAllSubscriptionsForTopicsResponse)
    private static final com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse DEFAULT_INSTANCE;
    private static final com.google.protobuf.Parser<GetAllSubscriptionsForTopicsResponse>
        PARSER = new com.google.protobuf.AbstractParser<GetAllSubscriptionsForTopicsResponse>() {
        public GetAllSubscriptionsForTopicsResponse parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
            return new GetAllSubscriptionsForTopicsResponse(input, extensionRegistry);
        }
    };

    static {
        DEFAULT_INSTANCE = new com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse();
    }

    private java.util.List<com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse> getAllSubscriptionsForTopicResponses_;
    private byte memoizedIsInitialized = -1;

    // Use GetAllSubscriptionsForTopicsResponse.newBuilder() to construct.
    private GetAllSubscriptionsForTopicsResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
        super(builder);
    }

    private GetAllSubscriptionsForTopicsResponse() {
        getAllSubscriptionsForTopicResponses_ = java.util.Collections.emptyList();
    }

    private GetAllSubscriptionsForTopicsResponse(
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
                            getAllSubscriptionsForTopicResponses_ = new java.util.ArrayList<com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse>();
                            mutable_bitField0_ |= 0x00000001;
                        }
                        getAllSubscriptionsForTopicResponses_.add(
                            input.readMessage(com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse.parser(), extensionRegistry));
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
                getAllSubscriptionsForTopicResponses_ = java.util.Collections.unmodifiableList(getAllSubscriptionsForTopicResponses_);
            }
            this.unknownFields = unknownFields.build();
            makeExtensionsImmutable();
        }
    }

    public static final com.google.protobuf.Descriptors.Descriptor
    getDescriptor() {
        return com.flipkart.vbroker.proto.PResponses.internal_static_proto_GetAllSubscriptionsForTopicsResponse_descriptor;
    }

    public static com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }

    public static com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }

    public static com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }

    public static com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }

    public static com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }

    public static com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }

    public static com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse parseFrom(java.io.InputStream input)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseWithIOException(PARSER, input);
    }

    public static com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseDelimitedWithIOException(PARSER, input);
    }

    public static com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }

    public static com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseWithIOException(PARSER, input);
    }

    public static com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static Builder newBuilder() {
        return DEFAULT_INSTANCE.toBuilder();
    }

    public static Builder newBuilder(com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse prototype) {
        return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }

    public static com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    public static com.google.protobuf.Parser<GetAllSubscriptionsForTopicsResponse> parser() {
        return PARSER;
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
        return this.unknownFields;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
    internalGetFieldAccessorTable() {
        return com.flipkart.vbroker.proto.PResponses.internal_static_proto_GetAllSubscriptionsForTopicsResponse_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse.class, com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse.Builder.class);
    }

    /**
     * <code>repeated .proto.GetAllSubscriptionsForTopicResponse getAllSubscriptionsForTopicResponses = 1;</code>
     */
    public java.util.List<com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse> getGetAllSubscriptionsForTopicResponsesList() {
        return getAllSubscriptionsForTopicResponses_;
    }

    /**
     * <code>repeated .proto.GetAllSubscriptionsForTopicResponse getAllSubscriptionsForTopicResponses = 1;</code>
     */
    public java.util.List<? extends com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponseOrBuilder>
    getGetAllSubscriptionsForTopicResponsesOrBuilderList() {
        return getAllSubscriptionsForTopicResponses_;
    }

    /**
     * <code>repeated .proto.GetAllSubscriptionsForTopicResponse getAllSubscriptionsForTopicResponses = 1;</code>
     */
    public int getGetAllSubscriptionsForTopicResponsesCount() {
        return getAllSubscriptionsForTopicResponses_.size();
    }

    /**
     * <code>repeated .proto.GetAllSubscriptionsForTopicResponse getAllSubscriptionsForTopicResponses = 1;</code>
     */
    public com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse getGetAllSubscriptionsForTopicResponses(int index) {
        return getAllSubscriptionsForTopicResponses_.get(index);
    }

    /**
     * <code>repeated .proto.GetAllSubscriptionsForTopicResponse getAllSubscriptionsForTopicResponses = 1;</code>
     */
    public com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponseOrBuilder getGetAllSubscriptionsForTopicResponsesOrBuilder(
        int index) {
        return getAllSubscriptionsForTopicResponses_.get(index);
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
        for (int i = 0; i < getAllSubscriptionsForTopicResponses_.size(); i++) {
            output.writeMessage(1, getAllSubscriptionsForTopicResponses_.get(i));
        }
        unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
        int size = memoizedSize;
        if (size != -1) return size;

        size = 0;
        for (int i = 0; i < getAllSubscriptionsForTopicResponses_.size(); i++) {
            size += com.google.protobuf.CodedOutputStream
                .computeMessageSize(1, getAllSubscriptionsForTopicResponses_.get(i));
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
        if (!(obj instanceof com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse)) {
            return super.equals(obj);
        }
        com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse other = (com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse) obj;

        boolean result = true;
        result = result && getGetAllSubscriptionsForTopicResponsesList()
            .equals(other.getGetAllSubscriptionsForTopicResponsesList());
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
        if (getGetAllSubscriptionsForTopicResponsesCount() > 0) {
            hash = (37 * hash) + GETALLSUBSCRIPTIONSFORTOPICRESPONSES_FIELD_NUMBER;
            hash = (53 * hash) + getGetAllSubscriptionsForTopicResponsesList().hashCode();
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
    public com.google.protobuf.Parser<GetAllSubscriptionsForTopicsResponse> getParserForType() {
        return PARSER;
    }

    public com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse getDefaultInstanceForType() {
        return DEFAULT_INSTANCE;
    }

    /**
     * Protobuf type {@code proto.GetAllSubscriptionsForTopicsResponse}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:proto.GetAllSubscriptionsForTopicsResponse)
        com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponseOrBuilder {
        private int bitField0_;
        private java.util.List<com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse> getAllSubscriptionsForTopicResponses_ =
            java.util.Collections.emptyList();
        private com.google.protobuf.RepeatedFieldBuilderV3<
            com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse, com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse.Builder, com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponseOrBuilder> getAllSubscriptionsForTopicResponsesBuilder_;

        // Construct using com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse.newBuilder()
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
            return com.flipkart.vbroker.proto.PResponses.internal_static_proto_GetAllSubscriptionsForTopicsResponse_descriptor;
        }

        protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
            return com.flipkart.vbroker.proto.PResponses.internal_static_proto_GetAllSubscriptionsForTopicsResponse_fieldAccessorTable
                .ensureFieldAccessorsInitialized(
                    com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse.class, com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse.Builder.class);
        }

        private void maybeForceBuilderInitialization() {
            if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
                getGetAllSubscriptionsForTopicResponsesFieldBuilder();
            }
        }

        public Builder clear() {
            super.clear();
            if (getAllSubscriptionsForTopicResponsesBuilder_ == null) {
                getAllSubscriptionsForTopicResponses_ = java.util.Collections.emptyList();
                bitField0_ = (bitField0_ & ~0x00000001);
            } else {
                getAllSubscriptionsForTopicResponsesBuilder_.clear();
            }
            return this;
        }

        public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
            return com.flipkart.vbroker.proto.PResponses.internal_static_proto_GetAllSubscriptionsForTopicsResponse_descriptor;
        }

        public com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse getDefaultInstanceForType() {
            return com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse.getDefaultInstance();
        }

        public com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse build() {
            com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse result = buildPartial();
            if (!result.isInitialized()) {
                throw newUninitializedMessageException(result);
            }
            return result;
        }

        public com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse buildPartial() {
            com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse result = new com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse(this);
            int from_bitField0_ = bitField0_;
            if (getAllSubscriptionsForTopicResponsesBuilder_ == null) {
                if (((bitField0_ & 0x00000001) == 0x00000001)) {
                    getAllSubscriptionsForTopicResponses_ = java.util.Collections.unmodifiableList(getAllSubscriptionsForTopicResponses_);
                    bitField0_ = (bitField0_ & ~0x00000001);
                }
                result.getAllSubscriptionsForTopicResponses_ = getAllSubscriptionsForTopicResponses_;
            } else {
                result.getAllSubscriptionsForTopicResponses_ = getAllSubscriptionsForTopicResponsesBuilder_.build();
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
            if (other instanceof com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse) {
                return mergeFrom((com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse) other);
            } else {
                super.mergeFrom(other);
                return this;
            }
        }

        public Builder mergeFrom(com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse other) {
            if (other == com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse.getDefaultInstance())
                return this;
            if (getAllSubscriptionsForTopicResponsesBuilder_ == null) {
                if (!other.getAllSubscriptionsForTopicResponses_.isEmpty()) {
                    if (getAllSubscriptionsForTopicResponses_.isEmpty()) {
                        getAllSubscriptionsForTopicResponses_ = other.getAllSubscriptionsForTopicResponses_;
                        bitField0_ = (bitField0_ & ~0x00000001);
                    } else {
                        ensureGetAllSubscriptionsForTopicResponsesIsMutable();
                        getAllSubscriptionsForTopicResponses_.addAll(other.getAllSubscriptionsForTopicResponses_);
                    }
                    onChanged();
                }
            } else {
                if (!other.getAllSubscriptionsForTopicResponses_.isEmpty()) {
                    if (getAllSubscriptionsForTopicResponsesBuilder_.isEmpty()) {
                        getAllSubscriptionsForTopicResponsesBuilder_.dispose();
                        getAllSubscriptionsForTopicResponsesBuilder_ = null;
                        getAllSubscriptionsForTopicResponses_ = other.getAllSubscriptionsForTopicResponses_;
                        bitField0_ = (bitField0_ & ~0x00000001);
                        getAllSubscriptionsForTopicResponsesBuilder_ =
                            com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                                getGetAllSubscriptionsForTopicResponsesFieldBuilder() : null;
                    } else {
                        getAllSubscriptionsForTopicResponsesBuilder_.addAllMessages(other.getAllSubscriptionsForTopicResponses_);
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
            com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse parsedMessage = null;
            try {
                parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                parsedMessage = (com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicsResponse) e.getUnfinishedMessage();
                throw e.unwrapIOException();
            } finally {
                if (parsedMessage != null) {
                    mergeFrom(parsedMessage);
                }
            }
            return this;
        }

        private void ensureGetAllSubscriptionsForTopicResponsesIsMutable() {
            if (!((bitField0_ & 0x00000001) == 0x00000001)) {
                getAllSubscriptionsForTopicResponses_ = new java.util.ArrayList<com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse>(getAllSubscriptionsForTopicResponses_);
                bitField0_ |= 0x00000001;
            }
        }

        /**
         * <code>repeated .proto.GetAllSubscriptionsForTopicResponse getAllSubscriptionsForTopicResponses = 1;</code>
         */
        public java.util.List<com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse> getGetAllSubscriptionsForTopicResponsesList() {
            if (getAllSubscriptionsForTopicResponsesBuilder_ == null) {
                return java.util.Collections.unmodifiableList(getAllSubscriptionsForTopicResponses_);
            } else {
                return getAllSubscriptionsForTopicResponsesBuilder_.getMessageList();
            }
        }

        /**
         * <code>repeated .proto.GetAllSubscriptionsForTopicResponse getAllSubscriptionsForTopicResponses = 1;</code>
         */
        public int getGetAllSubscriptionsForTopicResponsesCount() {
            if (getAllSubscriptionsForTopicResponsesBuilder_ == null) {
                return getAllSubscriptionsForTopicResponses_.size();
            } else {
                return getAllSubscriptionsForTopicResponsesBuilder_.getCount();
            }
        }

        /**
         * <code>repeated .proto.GetAllSubscriptionsForTopicResponse getAllSubscriptionsForTopicResponses = 1;</code>
         */
        public com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse getGetAllSubscriptionsForTopicResponses(int index) {
            if (getAllSubscriptionsForTopicResponsesBuilder_ == null) {
                return getAllSubscriptionsForTopicResponses_.get(index);
            } else {
                return getAllSubscriptionsForTopicResponsesBuilder_.getMessage(index);
            }
        }

        /**
         * <code>repeated .proto.GetAllSubscriptionsForTopicResponse getAllSubscriptionsForTopicResponses = 1;</code>
         */
        public Builder setGetAllSubscriptionsForTopicResponses(
            int index, com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse value) {
            if (getAllSubscriptionsForTopicResponsesBuilder_ == null) {
                if (value == null) {
                    throw new NullPointerException();
                }
                ensureGetAllSubscriptionsForTopicResponsesIsMutable();
                getAllSubscriptionsForTopicResponses_.set(index, value);
                onChanged();
            } else {
                getAllSubscriptionsForTopicResponsesBuilder_.setMessage(index, value);
            }
            return this;
        }

        /**
         * <code>repeated .proto.GetAllSubscriptionsForTopicResponse getAllSubscriptionsForTopicResponses = 1;</code>
         */
        public Builder setGetAllSubscriptionsForTopicResponses(
            int index, com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse.Builder builderForValue) {
            if (getAllSubscriptionsForTopicResponsesBuilder_ == null) {
                ensureGetAllSubscriptionsForTopicResponsesIsMutable();
                getAllSubscriptionsForTopicResponses_.set(index, builderForValue.build());
                onChanged();
            } else {
                getAllSubscriptionsForTopicResponsesBuilder_.setMessage(index, builderForValue.build());
            }
            return this;
        }

        /**
         * <code>repeated .proto.GetAllSubscriptionsForTopicResponse getAllSubscriptionsForTopicResponses = 1;</code>
         */
        public Builder addGetAllSubscriptionsForTopicResponses(com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse value) {
            if (getAllSubscriptionsForTopicResponsesBuilder_ == null) {
                if (value == null) {
                    throw new NullPointerException();
                }
                ensureGetAllSubscriptionsForTopicResponsesIsMutable();
                getAllSubscriptionsForTopicResponses_.add(value);
                onChanged();
            } else {
                getAllSubscriptionsForTopicResponsesBuilder_.addMessage(value);
            }
            return this;
        }

        /**
         * <code>repeated .proto.GetAllSubscriptionsForTopicResponse getAllSubscriptionsForTopicResponses = 1;</code>
         */
        public Builder addGetAllSubscriptionsForTopicResponses(
            int index, com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse value) {
            if (getAllSubscriptionsForTopicResponsesBuilder_ == null) {
                if (value == null) {
                    throw new NullPointerException();
                }
                ensureGetAllSubscriptionsForTopicResponsesIsMutable();
                getAllSubscriptionsForTopicResponses_.add(index, value);
                onChanged();
            } else {
                getAllSubscriptionsForTopicResponsesBuilder_.addMessage(index, value);
            }
            return this;
        }

        /**
         * <code>repeated .proto.GetAllSubscriptionsForTopicResponse getAllSubscriptionsForTopicResponses = 1;</code>
         */
        public Builder addGetAllSubscriptionsForTopicResponses(
            com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse.Builder builderForValue) {
            if (getAllSubscriptionsForTopicResponsesBuilder_ == null) {
                ensureGetAllSubscriptionsForTopicResponsesIsMutable();
                getAllSubscriptionsForTopicResponses_.add(builderForValue.build());
                onChanged();
            } else {
                getAllSubscriptionsForTopicResponsesBuilder_.addMessage(builderForValue.build());
            }
            return this;
        }

        /**
         * <code>repeated .proto.GetAllSubscriptionsForTopicResponse getAllSubscriptionsForTopicResponses = 1;</code>
         */
        public Builder addGetAllSubscriptionsForTopicResponses(
            int index, com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse.Builder builderForValue) {
            if (getAllSubscriptionsForTopicResponsesBuilder_ == null) {
                ensureGetAllSubscriptionsForTopicResponsesIsMutable();
                getAllSubscriptionsForTopicResponses_.add(index, builderForValue.build());
                onChanged();
            } else {
                getAllSubscriptionsForTopicResponsesBuilder_.addMessage(index, builderForValue.build());
            }
            return this;
        }

        /**
         * <code>repeated .proto.GetAllSubscriptionsForTopicResponse getAllSubscriptionsForTopicResponses = 1;</code>
         */
        public Builder addAllGetAllSubscriptionsForTopicResponses(
            java.lang.Iterable<? extends com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse> values) {
            if (getAllSubscriptionsForTopicResponsesBuilder_ == null) {
                ensureGetAllSubscriptionsForTopicResponsesIsMutable();
                com.google.protobuf.AbstractMessageLite.Builder.addAll(
                    values, getAllSubscriptionsForTopicResponses_);
                onChanged();
            } else {
                getAllSubscriptionsForTopicResponsesBuilder_.addAllMessages(values);
            }
            return this;
        }

        /**
         * <code>repeated .proto.GetAllSubscriptionsForTopicResponse getAllSubscriptionsForTopicResponses = 1;</code>
         */
        public Builder clearGetAllSubscriptionsForTopicResponses() {
            if (getAllSubscriptionsForTopicResponsesBuilder_ == null) {
                getAllSubscriptionsForTopicResponses_ = java.util.Collections.emptyList();
                bitField0_ = (bitField0_ & ~0x00000001);
                onChanged();
            } else {
                getAllSubscriptionsForTopicResponsesBuilder_.clear();
            }
            return this;
        }

        /**
         * <code>repeated .proto.GetAllSubscriptionsForTopicResponse getAllSubscriptionsForTopicResponses = 1;</code>
         */
        public Builder removeGetAllSubscriptionsForTopicResponses(int index) {
            if (getAllSubscriptionsForTopicResponsesBuilder_ == null) {
                ensureGetAllSubscriptionsForTopicResponsesIsMutable();
                getAllSubscriptionsForTopicResponses_.remove(index);
                onChanged();
            } else {
                getAllSubscriptionsForTopicResponsesBuilder_.remove(index);
            }
            return this;
        }

        /**
         * <code>repeated .proto.GetAllSubscriptionsForTopicResponse getAllSubscriptionsForTopicResponses = 1;</code>
         */
        public com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse.Builder getGetAllSubscriptionsForTopicResponsesBuilder(
            int index) {
            return getGetAllSubscriptionsForTopicResponsesFieldBuilder().getBuilder(index);
        }

        /**
         * <code>repeated .proto.GetAllSubscriptionsForTopicResponse getAllSubscriptionsForTopicResponses = 1;</code>
         */
        public com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponseOrBuilder getGetAllSubscriptionsForTopicResponsesOrBuilder(
            int index) {
            if (getAllSubscriptionsForTopicResponsesBuilder_ == null) {
                return getAllSubscriptionsForTopicResponses_.get(index);
            } else {
                return getAllSubscriptionsForTopicResponsesBuilder_.getMessageOrBuilder(index);
            }
        }

        /**
         * <code>repeated .proto.GetAllSubscriptionsForTopicResponse getAllSubscriptionsForTopicResponses = 1;</code>
         */
        public java.util.List<? extends com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponseOrBuilder>
        getGetAllSubscriptionsForTopicResponsesOrBuilderList() {
            if (getAllSubscriptionsForTopicResponsesBuilder_ != null) {
                return getAllSubscriptionsForTopicResponsesBuilder_.getMessageOrBuilderList();
            } else {
                return java.util.Collections.unmodifiableList(getAllSubscriptionsForTopicResponses_);
            }
        }

        /**
         * <code>repeated .proto.GetAllSubscriptionsForTopicResponse getAllSubscriptionsForTopicResponses = 1;</code>
         */
        public com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse.Builder addGetAllSubscriptionsForTopicResponsesBuilder() {
            return getGetAllSubscriptionsForTopicResponsesFieldBuilder().addBuilder(
                com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse.getDefaultInstance());
        }

        /**
         * <code>repeated .proto.GetAllSubscriptionsForTopicResponse getAllSubscriptionsForTopicResponses = 1;</code>
         */
        public com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse.Builder addGetAllSubscriptionsForTopicResponsesBuilder(
            int index) {
            return getGetAllSubscriptionsForTopicResponsesFieldBuilder().addBuilder(
                index, com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse.getDefaultInstance());
        }

        /**
         * <code>repeated .proto.GetAllSubscriptionsForTopicResponse getAllSubscriptionsForTopicResponses = 1;</code>
         */
        public java.util.List<com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse.Builder>
        getGetAllSubscriptionsForTopicResponsesBuilderList() {
            return getGetAllSubscriptionsForTopicResponsesFieldBuilder().getBuilderList();
        }

        private com.google.protobuf.RepeatedFieldBuilderV3<
            com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse, com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse.Builder, com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponseOrBuilder>
        getGetAllSubscriptionsForTopicResponsesFieldBuilder() {
            if (getAllSubscriptionsForTopicResponsesBuilder_ == null) {
                getAllSubscriptionsForTopicResponsesBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
                    com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse, com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponse.Builder, com.flipkart.vbroker.proto.GetAllSubscriptionsForTopicResponseOrBuilder>(
                    getAllSubscriptionsForTopicResponses_,
                    ((bitField0_ & 0x00000001) == 0x00000001),
                    getParentForChildren(),
                    isClean());
                getAllSubscriptionsForTopicResponses_ = null;
            }
            return getAllSubscriptionsForTopicResponsesBuilder_;
        }

        public final Builder setUnknownFields(
            final com.google.protobuf.UnknownFieldSet unknownFields) {
            return super.setUnknownFieldsProto3(unknownFields);
        }

        public final Builder mergeUnknownFields(
            final com.google.protobuf.UnknownFieldSet unknownFields) {
            return super.mergeUnknownFields(unknownFields);
        }


        // @@protoc_insertion_point(builder_scope:proto.GetAllSubscriptionsForTopicsResponse)
    }

}
