package com.tastyhouse.external.file;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

public class ByteArrayMultipartFile implements MultipartFile {

    private final String filename;
    private final String contentType;
    private final byte[] content;

    public ByteArrayMultipartFile(String filename, String contentType, byte[] content) {
        this.filename = filename;
        this.contentType = contentType;
        this.content = content;
    }

    @Override @NonNull public String getName() { return filename; }
    @Override @Nullable public String getOriginalFilename() { return filename; }
    @Override @Nullable public String getContentType() { return contentType; }
    @Override public boolean isEmpty() { return content.length == 0; }
    @Override public long getSize() { return content.length; }
    @Override @NonNull public byte[] getBytes() { return content; }
    @Override @NonNull public InputStream getInputStream() { return new ByteArrayInputStream(content); }
    @Override public void transferTo(@NonNull File dest) { throw new UnsupportedOperationException(); }
}
