package io.cutehat.gabby.api.protocol.payload;

public record FileMeta(String name, long sizeInBytes, String mimeType) {
}