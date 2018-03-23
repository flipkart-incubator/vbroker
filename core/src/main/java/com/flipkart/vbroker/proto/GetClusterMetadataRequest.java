// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: requests.proto

package com.flipkart.vbroker.proto;

/**
 * <pre>
 * cluster metadata request
 * </pre>
 * <p>
 * Protobuf type {@code proto.GetClusterMetadataRequest}
 */
public final class GetClusterMetadataRequest extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:proto.GetClusterMetadataRequest)
    GetClusterMetadataRequestOrBuilder {
    public static final int TOPICIDS_FIELD_NUMBER = 1;
    private static final long serialVersionUID = 0L;
    // @@protoc_insertion_point(class_scope:proto.GetClusterMetadataRequest)
    private static final com.flipkart.vbroker.proto.GetClusterMetadataRequest DEFAULT_INSTANCE;
    private static final com.google.protobuf.Parser<GetClusterMetadataRequest>
        PARSER = new com.google.protobuf.AbstractParser<GetClusterMetadataRequest>() {
        public GetClusterMetadataRequest parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
            return new GetClusterMetadataRequest(input, extensionRegistry);
        }
    };

    static {
        DEFAULT_INSTANCE = new com.flipkart.vbroker.proto.GetClusterMetadataRequest();
    }

    private java.util.List<java.lang.Integer> topicIds_;
    private int topicIdsMemoizedSerializedSize = -1;
    private byte memoizedIsInitialized = -1;
    // Use GetClusterMetadataRequest.newBuilder() to construct.
    private GetClusterMetadataRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
        super(builder);
    }

    private GetClusterMetadataRequest() {
        topicIds_ = java.util.Collections.emptyList();
    }

    private GetClusterMetadataRequest(
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
                    case 8: {
                        if (!((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
                            topicIds_ = new java.util.ArrayList<java.lang.Integer>();
                            mutable_bitField0_ |= 0x00000001;
                        }
                        topicIds_.add(input.readInt32());
                        break;
                    }
                    case 10: {
                        int length = input.readRawVarint32();
                        int limit = input.pushLimit(length);
                        if (!((mutable_bitField0_ & 0x00000001) == 0x00000001) && input.getBytesUntilLimit() > 0) {
                            topicIds_ = new java.util.ArrayList<java.lang.Integer>();
                            mutable_bitField0_ |= 0x00000001;
                        }
                        while (input.getBytesUntilLimit() > 0) {
                            topicIds_.add(input.readInt32());
                        }
                        input.popLimit(limit);
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
                topicIds_ = java.util.Collections.unmodifiableList(topicIds_);
            }
            this.unknownFields = unknownFields.build();
            makeExtensionsImmutable();
        }
    }

    public static final com.google.protobuf.Descriptors.Descriptor
    getDescriptor() {
        return com.flipkart.vbroker.proto.PRequests.internal_static_proto_GetClusterMetadataRequest_descriptor;
    }

    public static com.flipkart.vbroker.proto.GetClusterMetadataRequest parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }

    public static com.flipkart.vbroker.proto.GetClusterMetadataRequest parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }

    public static com.flipkart.vbroker.proto.GetClusterMetadataRequest parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }

    public static com.flipkart.vbroker.proto.GetClusterMetadataRequest parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }

    public static com.flipkart.vbroker.proto.GetClusterMetadataRequest parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }

    public static com.flipkart.vbroker.proto.GetClusterMetadataRequest parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }

    public static com.flipkart.vbroker.proto.GetClusterMetadataRequest parseFrom(java.io.InputStream input)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseWithIOException(PARSER, input);
    }

    public static com.flipkart.vbroker.proto.GetClusterMetadataRequest parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static com.flipkart.vbroker.proto.GetClusterMetadataRequest parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseDelimitedWithIOException(PARSER, input);
    }

    public static com.flipkart.vbroker.proto.GetClusterMetadataRequest parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }

    public static com.flipkart.vbroker.proto.GetClusterMetadataRequest parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseWithIOException(PARSER, input);
    }

    public static com.flipkart.vbroker.proto.GetClusterMetadataRequest parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
            .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static Builder newBuilder() {
        return DEFAULT_INSTANCE.toBuilder();
    }

    public static Builder newBuilder(com.flipkart.vbroker.proto.GetClusterMetadataRequest prototype) {
        return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }

    public static com.flipkart.vbroker.proto.GetClusterMetadataRequest getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    public static com.google.protobuf.Parser<GetClusterMetadataRequest> parser() {
        return PARSER;
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
        return this.unknownFields;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
    internalGetFieldAccessorTable() {
        return com.flipkart.vbroker.proto.PRequests.internal_static_proto_GetClusterMetadataRequest_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                com.flipkart.vbroker.proto.GetClusterMetadataRequest.class, com.flipkart.vbroker.proto.GetClusterMetadataRequest.Builder.class);
    }

    /**
     * <code>repeated int32 topicIds = 1;</code>
     */
    public java.util.List<java.lang.Integer>
    getTopicIdsList() {
        return topicIds_;
    }

    /**
     * <code>repeated int32 topicIds = 1;</code>
     */
    public int getTopicIdsCount() {
        return topicIds_.size();
    }

    /**
     * <code>repeated int32 topicIds = 1;</code>
     */
    public int getTopicIds(int index) {
        return topicIds_.get(index);
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
        getSerializedSize();
        if (getTopicIdsList().size() > 0) {
            output.writeUInt32NoTag(10);
            output.writeUInt32NoTag(topicIdsMemoizedSerializedSize);
        }
        for (int i = 0; i < topicIds_.size(); i++) {
            output.writeInt32NoTag(topicIds_.get(i));
        }
        unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
        int size = memoizedSize;
        if (size != -1) return size;

        size = 0;
        {
            int dataSize = 0;
            for (int i = 0; i < topicIds_.size(); i++) {
                dataSize += com.google.protobuf.CodedOutputStream
                    .computeInt32SizeNoTag(topicIds_.get(i));
            }
            size += dataSize;
            if (!getTopicIdsList().isEmpty()) {
                size += 1;
                size += com.google.protobuf.CodedOutputStream
                    .computeInt32SizeNoTag(dataSize);
            }
            topicIdsMemoizedSerializedSize = dataSize;
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
        if (!(obj instanceof com.flipkart.vbroker.proto.GetClusterMetadataRequest)) {
            return super.equals(obj);
        }
        com.flipkart.vbroker.proto.GetClusterMetadataRequest other = (com.flipkart.vbroker.proto.GetClusterMetadataRequest) obj;

        boolean result = true;
        result = result && getTopicIdsList()
            .equals(other.getTopicIdsList());
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
        if (getTopicIdsCount() > 0) {
            hash = (37 * hash) + TOPICIDS_FIELD_NUMBER;
            hash = (53 * hash) + getTopicIdsList().hashCode();
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
    public com.google.protobuf.Parser<GetClusterMetadataRequest> getParserForType() {
        return PARSER;
    }

    public com.flipkart.vbroker.proto.GetClusterMetadataRequest getDefaultInstanceForType() {
        return DEFAULT_INSTANCE;
    }

    /**
     * <pre>
     * cluster metadata request
     * </pre>
     * <p>
     * Protobuf type {@code proto.GetClusterMetadataRequest}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:proto.GetClusterMetadataRequest)
        com.flipkart.vbroker.proto.GetClusterMetadataRequestOrBuilder {
        private int bitField0_;
        private java.util.List<java.lang.Integer> topicIds_ = java.util.Collections.emptyList();

        // Construct using com.flipkart.vbroker.proto.GetClusterMetadataRequest.newBuilder()
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
            return com.flipkart.vbroker.proto.PRequests.internal_static_proto_GetClusterMetadataRequest_descriptor;
        }

        protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
            return com.flipkart.vbroker.proto.PRequests.internal_static_proto_GetClusterMetadataRequest_fieldAccessorTable
                .ensureFieldAccessorsInitialized(
                    com.flipkart.vbroker.proto.GetClusterMetadataRequest.class, com.flipkart.vbroker.proto.GetClusterMetadataRequest.Builder.class);
        }

        private void maybeForceBuilderInitialization() {
            if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
            }
        }

        public Builder clear() {
            super.clear();
            topicIds_ = java.util.Collections.emptyList();
            bitField0_ = (bitField0_ & ~0x00000001);
            return this;
        }

        public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
            return com.flipkart.vbroker.proto.PRequests.internal_static_proto_GetClusterMetadataRequest_descriptor;
        }

        public com.flipkart.vbroker.proto.GetClusterMetadataRequest getDefaultInstanceForType() {
            return com.flipkart.vbroker.proto.GetClusterMetadataRequest.getDefaultInstance();
        }

        public com.flipkart.vbroker.proto.GetClusterMetadataRequest build() {
            com.flipkart.vbroker.proto.GetClusterMetadataRequest result = buildPartial();
            if (!result.isInitialized()) {
                throw newUninitializedMessageException(result);
            }
            return result;
        }

        public com.flipkart.vbroker.proto.GetClusterMetadataRequest buildPartial() {
            com.flipkart.vbroker.proto.GetClusterMetadataRequest result = new com.flipkart.vbroker.proto.GetClusterMetadataRequest(this);
            int from_bitField0_ = bitField0_;
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                topicIds_ = java.util.Collections.unmodifiableList(topicIds_);
                bitField0_ = (bitField0_ & ~0x00000001);
            }
            result.topicIds_ = topicIds_;
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
            if (other instanceof com.flipkart.vbroker.proto.GetClusterMetadataRequest) {
                return mergeFrom((com.flipkart.vbroker.proto.GetClusterMetadataRequest) other);
            } else {
                super.mergeFrom(other);
                return this;
            }
        }

        public Builder mergeFrom(com.flipkart.vbroker.proto.GetClusterMetadataRequest other) {
            if (other == com.flipkart.vbroker.proto.GetClusterMetadataRequest.getDefaultInstance()) return this;
            if (!other.topicIds_.isEmpty()) {
                if (topicIds_.isEmpty()) {
                    topicIds_ = other.topicIds_;
                    bitField0_ = (bitField0_ & ~0x00000001);
                } else {
                    ensureTopicIdsIsMutable();
                    topicIds_.addAll(other.topicIds_);
                }
                onChanged();
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
            com.flipkart.vbroker.proto.GetClusterMetadataRequest parsedMessage = null;
            try {
                parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                parsedMessage = (com.flipkart.vbroker.proto.GetClusterMetadataRequest) e.getUnfinishedMessage();
                throw e.unwrapIOException();
            } finally {
                if (parsedMessage != null) {
                    mergeFrom(parsedMessage);
                }
            }
            return this;
        }

        private void ensureTopicIdsIsMutable() {
            if (!((bitField0_ & 0x00000001) == 0x00000001)) {
                topicIds_ = new java.util.ArrayList<java.lang.Integer>(topicIds_);
                bitField0_ |= 0x00000001;
            }
        }

        /**
         * <code>repeated int32 topicIds = 1;</code>
         */
        public java.util.List<java.lang.Integer>
        getTopicIdsList() {
            return java.util.Collections.unmodifiableList(topicIds_);
        }

        /**
         * <code>repeated int32 topicIds = 1;</code>
         */
        public int getTopicIdsCount() {
            return topicIds_.size();
        }

        /**
         * <code>repeated int32 topicIds = 1;</code>
         */
        public int getTopicIds(int index) {
            return topicIds_.get(index);
        }

        /**
         * <code>repeated int32 topicIds = 1;</code>
         */
        public Builder setTopicIds(
            int index, int value) {
            ensureTopicIdsIsMutable();
            topicIds_.set(index, value);
            onChanged();
            return this;
        }

        /**
         * <code>repeated int32 topicIds = 1;</code>
         */
        public Builder addTopicIds(int value) {
            ensureTopicIdsIsMutable();
            topicIds_.add(value);
            onChanged();
            return this;
        }

        /**
         * <code>repeated int32 topicIds = 1;</code>
         */
        public Builder addAllTopicIds(
            java.lang.Iterable<? extends java.lang.Integer> values) {
            ensureTopicIdsIsMutable();
            com.google.protobuf.AbstractMessageLite.Builder.addAll(
                values, topicIds_);
            onChanged();
            return this;
        }

        /**
         * <code>repeated int32 topicIds = 1;</code>
         */
        public Builder clearTopicIds() {
            topicIds_ = java.util.Collections.emptyList();
            bitField0_ = (bitField0_ & ~0x00000001);
            onChanged();
            return this;
        }

        public final Builder setUnknownFields(
            final com.google.protobuf.UnknownFieldSet unknownFields) {
            return super.setUnknownFieldsProto3(unknownFields);
        }

        public final Builder mergeUnknownFields(
            final com.google.protobuf.UnknownFieldSet unknownFields) {
            return super.mergeUnknownFields(unknownFields);
        }


        // @@protoc_insertion_point(builder_scope:proto.GetClusterMetadataRequest)
    }

}

