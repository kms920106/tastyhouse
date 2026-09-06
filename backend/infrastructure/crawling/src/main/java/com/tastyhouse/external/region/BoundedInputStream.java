package com.tastyhouse.external.region;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

final class BoundedInputStream extends FilterInputStream {

    private final long maxBytes;
    private long readBytes;

    BoundedInputStream(InputStream delegate, long maxBytes) {
        super(delegate);
        this.maxBytes = maxBytes;
    }

    @Override
    public int read() throws IOException {
        int value = super.read();
        if (value != -1) {
            countRead(1);
        }
        return value;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        int count = super.read(buffer, offset, length);
        if (count > 0) {
            countRead(count);
        }
        return count;
    }

    private void countRead(int count) throws IOException {
        this.readBytes += count;
        if (this.readBytes > this.maxBytes) {
            throw new IOException("응답이 허용 크기(" + this.maxBytes + " bytes)를 초과했습니다.");
        }
    }
}
