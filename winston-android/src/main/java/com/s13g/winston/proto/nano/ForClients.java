// Generated by the protocol buffer compiler.  DO NOT EDIT!

package com.s13g.winston.proto.nano;

@SuppressWarnings("hiding")
public interface ForClients {

  public static final class ChannelData extends
      com.google.protobuf.nano.MessageNano {

    public static final class Channel extends
        com.google.protobuf.nano.MessageNano {

      public static final class ChannelValue extends
          com.google.protobuf.nano.MessageNano {

        private static volatile ChannelValue[] _emptyArray;
        public static ChannelValue[] emptyArray() {
          // Lazily initializes the empty array
          if (_emptyArray == null) {
            synchronized (
                com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
              if (_emptyArray == null) {
                _emptyArray = new ChannelValue[0];
              }
            }
          }
          return _emptyArray;
        }

        // optional string id = 1;
        public java.lang.String id;

        // optional string mode = 2;
        public java.lang.String mode;

        public ChannelValue() {
          clear();
        }

        public ChannelValue clear() {
          id = "";
          mode = "";
          cachedSize = -1;
          return this;
        }

        @Override
        public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
            throws java.io.IOException {
          if (!this.id.equals("")) {
            output.writeString(1, this.id);
          }
          if (!this.mode.equals("")) {
            output.writeString(2, this.mode);
          }
          super.writeTo(output);
        }

        @Override
        protected int computeSerializedSize() {
          int size = super.computeSerializedSize();
          if (!this.id.equals("")) {
            size += com.google.protobuf.nano.CodedOutputByteBufferNano
                .computeStringSize(1, this.id);
          }
          if (!this.mode.equals("")) {
            size += com.google.protobuf.nano.CodedOutputByteBufferNano
                .computeStringSize(2, this.mode);
          }
          return size;
        }

        @Override
        public ChannelValue mergeFrom(
                com.google.protobuf.nano.CodedInputByteBufferNano input)
            throws java.io.IOException {
          while (true) {
            int tag = input.readTag();
            switch (tag) {
              case 0:
                return this;
              default: {
                if (!com.google.protobuf.nano.WireFormatNano.parseUnknownField(input, tag)) {
                  return this;
                }
                break;
              }
              case 10: {
                this.id = input.readString();
                break;
              }
              case 18: {
                this.mode = input.readString();
                break;
              }
            }
          }
        }

        public static ChannelValue parseFrom(byte[] data)
            throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
          return com.google.protobuf.nano.MessageNano.mergeFrom(new ChannelValue(), data);
        }

        public static ChannelValue parseFrom(
                com.google.protobuf.nano.CodedInputByteBufferNano input)
            throws java.io.IOException {
          return new ChannelValue().mergeFrom(input);
        }
      }

      private static volatile Channel[] _emptyArray;
      public static Channel[] emptyArray() {
        // Lazily initializes the empty array
        if (_emptyArray == null) {
          synchronized (
              com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
            if (_emptyArray == null) {
              _emptyArray = new Channel[0];
            }
          }
        }
        return _emptyArray;
      }

      // optional string id = 1;
      public java.lang.String id;

      // optional string module_type = 2;
      public java.lang.String moduleType;

      // optional string type = 3;
      public java.lang.String type;

      // optional string name = 4;
      public java.lang.String name;

      // repeated .com.s13g.winston.proto.ChannelData.Channel.ChannelValue value = 5;
      public com.s13g.winston.proto.nano.ForClients.ChannelData.Channel.ChannelValue[] value;

      public Channel() {
        clear();
      }

      public Channel clear() {
        id = "";
        moduleType = "";
        type = "";
        name = "";
        value = com.s13g.winston.proto.nano.ForClients.ChannelData.Channel.ChannelValue.emptyArray();
        cachedSize = -1;
        return this;
      }

      @Override
      public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
          throws java.io.IOException {
        if (!this.id.equals("")) {
          output.writeString(1, this.id);
        }
        if (!this.moduleType.equals("")) {
          output.writeString(2, this.moduleType);
        }
        if (!this.type.equals("")) {
          output.writeString(3, this.type);
        }
        if (!this.name.equals("")) {
          output.writeString(4, this.name);
        }
        if (this.value != null && this.value.length > 0) {
          for (int i = 0; i < this.value.length; i++) {
            com.s13g.winston.proto.nano.ForClients.ChannelData.Channel.ChannelValue element = this.value[i];
            if (element != null) {
              output.writeMessage(5, element);
            }
          }
        }
        super.writeTo(output);
      }

      @Override
      protected int computeSerializedSize() {
        int size = super.computeSerializedSize();
        if (!this.id.equals("")) {
          size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeStringSize(1, this.id);
        }
        if (!this.moduleType.equals("")) {
          size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeStringSize(2, this.moduleType);
        }
        if (!this.type.equals("")) {
          size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeStringSize(3, this.type);
        }
        if (!this.name.equals("")) {
          size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeStringSize(4, this.name);
        }
        if (this.value != null && this.value.length > 0) {
          for (int i = 0; i < this.value.length; i++) {
            com.s13g.winston.proto.nano.ForClients.ChannelData.Channel.ChannelValue element = this.value[i];
            if (element != null) {
              size += com.google.protobuf.nano.CodedOutputByteBufferNano
                .computeMessageSize(5, element);
            }
          }
        }
        return size;
      }

      @Override
      public Channel mergeFrom(
              com.google.protobuf.nano.CodedInputByteBufferNano input)
          throws java.io.IOException {
        while (true) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              return this;
            default: {
              if (!com.google.protobuf.nano.WireFormatNano.parseUnknownField(input, tag)) {
                return this;
              }
              break;
            }
            case 10: {
              this.id = input.readString();
              break;
            }
            case 18: {
              this.moduleType = input.readString();
              break;
            }
            case 26: {
              this.type = input.readString();
              break;
            }
            case 34: {
              this.name = input.readString();
              break;
            }
            case 42: {
              int arrayLength = com.google.protobuf.nano.WireFormatNano
                  .getRepeatedFieldArrayLength(input, 42);
              int i = this.value == null ? 0 : this.value.length;
              com.s13g.winston.proto.nano.ForClients.ChannelData.Channel.ChannelValue[] newArray =
                  new com.s13g.winston.proto.nano.ForClients.ChannelData.Channel.ChannelValue[i + arrayLength];
              if (i != 0) {
                java.lang.System.arraycopy(this.value, 0, newArray, 0, i);
              }
              for (; i < newArray.length - 1; i++) {
                newArray[i] = new com.s13g.winston.proto.nano.ForClients.ChannelData.Channel.ChannelValue();
                input.readMessage(newArray[i]);
                input.readTag();
              }
              // Last one without readTag.
              newArray[i] = new com.s13g.winston.proto.nano.ForClients.ChannelData.Channel.ChannelValue();
              input.readMessage(newArray[i]);
              this.value = newArray;
              break;
            }
          }
        }
      }

      public static Channel parseFrom(byte[] data)
          throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
        return com.google.protobuf.nano.MessageNano.mergeFrom(new Channel(), data);
      }

      public static Channel parseFrom(
              com.google.protobuf.nano.CodedInputByteBufferNano input)
          throws java.io.IOException {
        return new Channel().mergeFrom(input);
      }
    }

    private static volatile ChannelData[] _emptyArray;
    public static ChannelData[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new ChannelData[0];
          }
        }
      }
      return _emptyArray;
    }

    // repeated .com.s13g.winston.proto.ChannelData.Channel channel = 1;
    public com.s13g.winston.proto.nano.ForClients.ChannelData.Channel[] channel;

    public ChannelData() {
      clear();
    }

    public ChannelData clear() {
      channel = com.s13g.winston.proto.nano.ForClients.ChannelData.Channel.emptyArray();
      cachedSize = -1;
      return this;
    }

    @Override
    public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
        throws java.io.IOException {
      if (this.channel != null && this.channel.length > 0) {
        for (int i = 0; i < this.channel.length; i++) {
          com.s13g.winston.proto.nano.ForClients.ChannelData.Channel element = this.channel[i];
          if (element != null) {
            output.writeMessage(1, element);
          }
        }
      }
      super.writeTo(output);
    }

    @Override
    protected int computeSerializedSize() {
      int size = super.computeSerializedSize();
      if (this.channel != null && this.channel.length > 0) {
        for (int i = 0; i < this.channel.length; i++) {
          com.s13g.winston.proto.nano.ForClients.ChannelData.Channel element = this.channel[i];
          if (element != null) {
            size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeMessageSize(1, element);
          }
        }
      }
      return size;
    }

    @Override
    public ChannelData mergeFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      while (true) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            return this;
          default: {
            if (!com.google.protobuf.nano.WireFormatNano.parseUnknownField(input, tag)) {
              return this;
            }
            break;
          }
          case 10: {
            int arrayLength = com.google.protobuf.nano.WireFormatNano
                .getRepeatedFieldArrayLength(input, 10);
            int i = this.channel == null ? 0 : this.channel.length;
            com.s13g.winston.proto.nano.ForClients.ChannelData.Channel[] newArray =
                new com.s13g.winston.proto.nano.ForClients.ChannelData.Channel[i + arrayLength];
            if (i != 0) {
              java.lang.System.arraycopy(this.channel, 0, newArray, 0, i);
            }
            for (; i < newArray.length - 1; i++) {
              newArray[i] = new com.s13g.winston.proto.nano.ForClients.ChannelData.Channel();
              input.readMessage(newArray[i]);
              input.readTag();
            }
            // Last one without readTag.
            newArray[i] = new com.s13g.winston.proto.nano.ForClients.ChannelData.Channel();
            input.readMessage(newArray[i]);
            this.channel = newArray;
            break;
          }
        }
      }
    }

    public static ChannelData parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new ChannelData(), data);
    }

    public static ChannelData parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new ChannelData().mergeFrom(input);
    }
  }
}
