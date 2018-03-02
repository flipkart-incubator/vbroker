// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: responses.proto

package com.flipkart.vbroker.proto;

/**
 * Protobuf type {@code proto.SubscriptionLag}
 */
public  final class SubscriptionLag extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:proto.SubscriptionLag)
    SubscriptionLagOrBuilder {
private static final long serialVersionUID = 0L;
  // Use SubscriptionLag.newBuilder() to construct.
  private SubscriptionLag(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private SubscriptionLag() {
    subscriptionId_ = 0;
    topicId_ = 0;
    partitionLags_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private SubscriptionLag(
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

            subscriptionId_ = input.readInt32();
            break;
          }
          case 16: {

            topicId_ = input.readInt32();
            break;
          }
          case 26: {
            if (!((mutable_bitField0_ & 0x00000004) == 0x00000004)) {
              partitionLags_ = new java.util.ArrayList<com.flipkart.vbroker.proto.PartitionLag>();
              mutable_bitField0_ |= 0x00000004;
            }
            partitionLags_.add(
                input.readMessage(com.flipkart.vbroker.proto.PartitionLag.parser(), extensionRegistry));
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
      if (((mutable_bitField0_ & 0x00000004) == 0x00000004)) {
        partitionLags_ = java.util.Collections.unmodifiableList(partitionLags_);
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.flipkart.vbroker.proto.PResponses.internal_static_proto_SubscriptionLag_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.flipkart.vbroker.proto.PResponses.internal_static_proto_SubscriptionLag_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.flipkart.vbroker.proto.SubscriptionLag.class, com.flipkart.vbroker.proto.SubscriptionLag.Builder.class);
  }

  private int bitField0_;
  public static final int SUBSCRIPTIONID_FIELD_NUMBER = 1;
  private int subscriptionId_;
  /**
   * <code>int32 subscriptionId = 1;</code>
   */
  public int getSubscriptionId() {
    return subscriptionId_;
  }

  public static final int TOPICID_FIELD_NUMBER = 2;
  private int topicId_;
  /**
   * <code>int32 topicId = 2;</code>
   */
  public int getTopicId() {
    return topicId_;
  }

  public static final int PARTITIONLAGS_FIELD_NUMBER = 3;
  private java.util.List<com.flipkart.vbroker.proto.PartitionLag> partitionLags_;
  /**
   * <code>repeated .proto.PartitionLag partitionLags = 3;</code>
   */
  public java.util.List<com.flipkart.vbroker.proto.PartitionLag> getPartitionLagsList() {
    return partitionLags_;
  }
  /**
   * <code>repeated .proto.PartitionLag partitionLags = 3;</code>
   */
  public java.util.List<? extends com.flipkart.vbroker.proto.PartitionLagOrBuilder> 
      getPartitionLagsOrBuilderList() {
    return partitionLags_;
  }
  /**
   * <code>repeated .proto.PartitionLag partitionLags = 3;</code>
   */
  public int getPartitionLagsCount() {
    return partitionLags_.size();
  }
  /**
   * <code>repeated .proto.PartitionLag partitionLags = 3;</code>
   */
  public com.flipkart.vbroker.proto.PartitionLag getPartitionLags(int index) {
    return partitionLags_.get(index);
  }
  /**
   * <code>repeated .proto.PartitionLag partitionLags = 3;</code>
   */
  public com.flipkart.vbroker.proto.PartitionLagOrBuilder getPartitionLagsOrBuilder(
      int index) {
    return partitionLags_.get(index);
  }

  private byte memoizedIsInitialized = -1;
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (subscriptionId_ != 0) {
      output.writeInt32(1, subscriptionId_);
    }
    if (topicId_ != 0) {
      output.writeInt32(2, topicId_);
    }
    for (int i = 0; i < partitionLags_.size(); i++) {
      output.writeMessage(3, partitionLags_.get(i));
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (subscriptionId_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(1, subscriptionId_);
    }
    if (topicId_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(2, topicId_);
    }
    for (int i = 0; i < partitionLags_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(3, partitionLags_.get(i));
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
    if (!(obj instanceof com.flipkart.vbroker.proto.SubscriptionLag)) {
      return super.equals(obj);
    }
    com.flipkart.vbroker.proto.SubscriptionLag other = (com.flipkart.vbroker.proto.SubscriptionLag) obj;

    boolean result = true;
    result = result && (getSubscriptionId()
        == other.getSubscriptionId());
    result = result && (getTopicId()
        == other.getTopicId());
    result = result && getPartitionLagsList()
        .equals(other.getPartitionLagsList());
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
    hash = (37 * hash) + SUBSCRIPTIONID_FIELD_NUMBER;
    hash = (53 * hash) + getSubscriptionId();
    hash = (37 * hash) + TOPICID_FIELD_NUMBER;
    hash = (53 * hash) + getTopicId();
    if (getPartitionLagsCount() > 0) {
      hash = (37 * hash) + PARTITIONLAGS_FIELD_NUMBER;
      hash = (53 * hash) + getPartitionLagsList().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.flipkart.vbroker.proto.SubscriptionLag parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.flipkart.vbroker.proto.SubscriptionLag parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.flipkart.vbroker.proto.SubscriptionLag parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.flipkart.vbroker.proto.SubscriptionLag parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.flipkart.vbroker.proto.SubscriptionLag parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.flipkart.vbroker.proto.SubscriptionLag parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.flipkart.vbroker.proto.SubscriptionLag parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.flipkart.vbroker.proto.SubscriptionLag parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.flipkart.vbroker.proto.SubscriptionLag parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.flipkart.vbroker.proto.SubscriptionLag parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.flipkart.vbroker.proto.SubscriptionLag parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.flipkart.vbroker.proto.SubscriptionLag parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(com.flipkart.vbroker.proto.SubscriptionLag prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
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
  /**
   * Protobuf type {@code proto.SubscriptionLag}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:proto.SubscriptionLag)
      com.flipkart.vbroker.proto.SubscriptionLagOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.flipkart.vbroker.proto.PResponses.internal_static_proto_SubscriptionLag_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.flipkart.vbroker.proto.PResponses.internal_static_proto_SubscriptionLag_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.flipkart.vbroker.proto.SubscriptionLag.class, com.flipkart.vbroker.proto.SubscriptionLag.Builder.class);
    }

    // Construct using com.flipkart.vbroker.proto.SubscriptionLag.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
        getPartitionLagsFieldBuilder();
      }
    }
    public Builder clear() {
      super.clear();
      subscriptionId_ = 0;

      topicId_ = 0;

      if (partitionLagsBuilder_ == null) {
        partitionLags_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000004);
      } else {
        partitionLagsBuilder_.clear();
      }
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.flipkart.vbroker.proto.PResponses.internal_static_proto_SubscriptionLag_descriptor;
    }

    public com.flipkart.vbroker.proto.SubscriptionLag getDefaultInstanceForType() {
      return com.flipkart.vbroker.proto.SubscriptionLag.getDefaultInstance();
    }

    public com.flipkart.vbroker.proto.SubscriptionLag build() {
      com.flipkart.vbroker.proto.SubscriptionLag result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.flipkart.vbroker.proto.SubscriptionLag buildPartial() {
      com.flipkart.vbroker.proto.SubscriptionLag result = new com.flipkart.vbroker.proto.SubscriptionLag(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      result.subscriptionId_ = subscriptionId_;
      result.topicId_ = topicId_;
      if (partitionLagsBuilder_ == null) {
        if (((bitField0_ & 0x00000004) == 0x00000004)) {
          partitionLags_ = java.util.Collections.unmodifiableList(partitionLags_);
          bitField0_ = (bitField0_ & ~0x00000004);
        }
        result.partitionLags_ = partitionLags_;
      } else {
        result.partitionLags_ = partitionLagsBuilder_.build();
      }
      result.bitField0_ = to_bitField0_;
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
      if (other instanceof com.flipkart.vbroker.proto.SubscriptionLag) {
        return mergeFrom((com.flipkart.vbroker.proto.SubscriptionLag)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.flipkart.vbroker.proto.SubscriptionLag other) {
      if (other == com.flipkart.vbroker.proto.SubscriptionLag.getDefaultInstance()) return this;
      if (other.getSubscriptionId() != 0) {
        setSubscriptionId(other.getSubscriptionId());
      }
      if (other.getTopicId() != 0) {
        setTopicId(other.getTopicId());
      }
      if (partitionLagsBuilder_ == null) {
        if (!other.partitionLags_.isEmpty()) {
          if (partitionLags_.isEmpty()) {
            partitionLags_ = other.partitionLags_;
            bitField0_ = (bitField0_ & ~0x00000004);
          } else {
            ensurePartitionLagsIsMutable();
            partitionLags_.addAll(other.partitionLags_);
          }
          onChanged();
        }
      } else {
        if (!other.partitionLags_.isEmpty()) {
          if (partitionLagsBuilder_.isEmpty()) {
            partitionLagsBuilder_.dispose();
            partitionLagsBuilder_ = null;
            partitionLags_ = other.partitionLags_;
            bitField0_ = (bitField0_ & ~0x00000004);
            partitionLagsBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getPartitionLagsFieldBuilder() : null;
          } else {
            partitionLagsBuilder_.addAllMessages(other.partitionLags_);
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
      com.flipkart.vbroker.proto.SubscriptionLag parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (com.flipkart.vbroker.proto.SubscriptionLag) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private int subscriptionId_ ;
    /**
     * <code>int32 subscriptionId = 1;</code>
     */
    public int getSubscriptionId() {
      return subscriptionId_;
    }
    /**
     * <code>int32 subscriptionId = 1;</code>
     */
    public Builder setSubscriptionId(int value) {
      
      subscriptionId_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int32 subscriptionId = 1;</code>
     */
    public Builder clearSubscriptionId() {
      
      subscriptionId_ = 0;
      onChanged();
      return this;
    }

    private int topicId_ ;
    /**
     * <code>int32 topicId = 2;</code>
     */
    public int getTopicId() {
      return topicId_;
    }
    /**
     * <code>int32 topicId = 2;</code>
     */
    public Builder setTopicId(int value) {
      
      topicId_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int32 topicId = 2;</code>
     */
    public Builder clearTopicId() {
      
      topicId_ = 0;
      onChanged();
      return this;
    }

    private java.util.List<com.flipkart.vbroker.proto.PartitionLag> partitionLags_ =
      java.util.Collections.emptyList();
    private void ensurePartitionLagsIsMutable() {
      if (!((bitField0_ & 0x00000004) == 0x00000004)) {
        partitionLags_ = new java.util.ArrayList<com.flipkart.vbroker.proto.PartitionLag>(partitionLags_);
        bitField0_ |= 0x00000004;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.flipkart.vbroker.proto.PartitionLag, com.flipkart.vbroker.proto.PartitionLag.Builder, com.flipkart.vbroker.proto.PartitionLagOrBuilder> partitionLagsBuilder_;

    /**
     * <code>repeated .proto.PartitionLag partitionLags = 3;</code>
     */
    public java.util.List<com.flipkart.vbroker.proto.PartitionLag> getPartitionLagsList() {
      if (partitionLagsBuilder_ == null) {
        return java.util.Collections.unmodifiableList(partitionLags_);
      } else {
        return partitionLagsBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .proto.PartitionLag partitionLags = 3;</code>
     */
    public int getPartitionLagsCount() {
      if (partitionLagsBuilder_ == null) {
        return partitionLags_.size();
      } else {
        return partitionLagsBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .proto.PartitionLag partitionLags = 3;</code>
     */
    public com.flipkart.vbroker.proto.PartitionLag getPartitionLags(int index) {
      if (partitionLagsBuilder_ == null) {
        return partitionLags_.get(index);
      } else {
        return partitionLagsBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .proto.PartitionLag partitionLags = 3;</code>
     */
    public Builder setPartitionLags(
        int index, com.flipkart.vbroker.proto.PartitionLag value) {
      if (partitionLagsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensurePartitionLagsIsMutable();
        partitionLags_.set(index, value);
        onChanged();
      } else {
        partitionLagsBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .proto.PartitionLag partitionLags = 3;</code>
     */
    public Builder setPartitionLags(
        int index, com.flipkart.vbroker.proto.PartitionLag.Builder builderForValue) {
      if (partitionLagsBuilder_ == null) {
        ensurePartitionLagsIsMutable();
        partitionLags_.set(index, builderForValue.build());
        onChanged();
      } else {
        partitionLagsBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .proto.PartitionLag partitionLags = 3;</code>
     */
    public Builder addPartitionLags(com.flipkart.vbroker.proto.PartitionLag value) {
      if (partitionLagsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensurePartitionLagsIsMutable();
        partitionLags_.add(value);
        onChanged();
      } else {
        partitionLagsBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .proto.PartitionLag partitionLags = 3;</code>
     */
    public Builder addPartitionLags(
        int index, com.flipkart.vbroker.proto.PartitionLag value) {
      if (partitionLagsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensurePartitionLagsIsMutable();
        partitionLags_.add(index, value);
        onChanged();
      } else {
        partitionLagsBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .proto.PartitionLag partitionLags = 3;</code>
     */
    public Builder addPartitionLags(
        com.flipkart.vbroker.proto.PartitionLag.Builder builderForValue) {
      if (partitionLagsBuilder_ == null) {
        ensurePartitionLagsIsMutable();
        partitionLags_.add(builderForValue.build());
        onChanged();
      } else {
        partitionLagsBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .proto.PartitionLag partitionLags = 3;</code>
     */
    public Builder addPartitionLags(
        int index, com.flipkart.vbroker.proto.PartitionLag.Builder builderForValue) {
      if (partitionLagsBuilder_ == null) {
        ensurePartitionLagsIsMutable();
        partitionLags_.add(index, builderForValue.build());
        onChanged();
      } else {
        partitionLagsBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .proto.PartitionLag partitionLags = 3;</code>
     */
    public Builder addAllPartitionLags(
        java.lang.Iterable<? extends com.flipkart.vbroker.proto.PartitionLag> values) {
      if (partitionLagsBuilder_ == null) {
        ensurePartitionLagsIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, partitionLags_);
        onChanged();
      } else {
        partitionLagsBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .proto.PartitionLag partitionLags = 3;</code>
     */
    public Builder clearPartitionLags() {
      if (partitionLagsBuilder_ == null) {
        partitionLags_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000004);
        onChanged();
      } else {
        partitionLagsBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .proto.PartitionLag partitionLags = 3;</code>
     */
    public Builder removePartitionLags(int index) {
      if (partitionLagsBuilder_ == null) {
        ensurePartitionLagsIsMutable();
        partitionLags_.remove(index);
        onChanged();
      } else {
        partitionLagsBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .proto.PartitionLag partitionLags = 3;</code>
     */
    public com.flipkart.vbroker.proto.PartitionLag.Builder getPartitionLagsBuilder(
        int index) {
      return getPartitionLagsFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .proto.PartitionLag partitionLags = 3;</code>
     */
    public com.flipkart.vbroker.proto.PartitionLagOrBuilder getPartitionLagsOrBuilder(
        int index) {
      if (partitionLagsBuilder_ == null) {
        return partitionLags_.get(index);  } else {
        return partitionLagsBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .proto.PartitionLag partitionLags = 3;</code>
     */
    public java.util.List<? extends com.flipkart.vbroker.proto.PartitionLagOrBuilder> 
         getPartitionLagsOrBuilderList() {
      if (partitionLagsBuilder_ != null) {
        return partitionLagsBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(partitionLags_);
      }
    }
    /**
     * <code>repeated .proto.PartitionLag partitionLags = 3;</code>
     */
    public com.flipkart.vbroker.proto.PartitionLag.Builder addPartitionLagsBuilder() {
      return getPartitionLagsFieldBuilder().addBuilder(
          com.flipkart.vbroker.proto.PartitionLag.getDefaultInstance());
    }
    /**
     * <code>repeated .proto.PartitionLag partitionLags = 3;</code>
     */
    public com.flipkart.vbroker.proto.PartitionLag.Builder addPartitionLagsBuilder(
        int index) {
      return getPartitionLagsFieldBuilder().addBuilder(
          index, com.flipkart.vbroker.proto.PartitionLag.getDefaultInstance());
    }
    /**
     * <code>repeated .proto.PartitionLag partitionLags = 3;</code>
     */
    public java.util.List<com.flipkart.vbroker.proto.PartitionLag.Builder> 
         getPartitionLagsBuilderList() {
      return getPartitionLagsFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.flipkart.vbroker.proto.PartitionLag, com.flipkart.vbroker.proto.PartitionLag.Builder, com.flipkart.vbroker.proto.PartitionLagOrBuilder> 
        getPartitionLagsFieldBuilder() {
      if (partitionLagsBuilder_ == null) {
        partitionLagsBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            com.flipkart.vbroker.proto.PartitionLag, com.flipkart.vbroker.proto.PartitionLag.Builder, com.flipkart.vbroker.proto.PartitionLagOrBuilder>(
                partitionLags_,
                ((bitField0_ & 0x00000004) == 0x00000004),
                getParentForChildren(),
                isClean());
        partitionLags_ = null;
      }
      return partitionLagsBuilder_;
    }
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFieldsProto3(unknownFields);
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:proto.SubscriptionLag)
  }

  // @@protoc_insertion_point(class_scope:proto.SubscriptionLag)
  private static final com.flipkart.vbroker.proto.SubscriptionLag DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.flipkart.vbroker.proto.SubscriptionLag();
  }

  public static com.flipkart.vbroker.proto.SubscriptionLag getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<SubscriptionLag>
      PARSER = new com.google.protobuf.AbstractParser<SubscriptionLag>() {
    public SubscriptionLag parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new SubscriptionLag(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<SubscriptionLag> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<SubscriptionLag> getParserForType() {
    return PARSER;
  }

  public com.flipkart.vbroker.proto.SubscriptionLag getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

