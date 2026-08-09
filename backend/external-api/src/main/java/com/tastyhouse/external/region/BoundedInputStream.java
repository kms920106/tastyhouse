package com.tastyhouse.external.region;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 읽은 누적 바이트가 상한을 넘으면 실패하는 스트림.
 *
 * <p>원천이 예고 없이 커지거나 응답이 엉뚱한 내용으로 바뀌었을 때 배치가 힙을 모두 소진하며 죽는 것을
 * 막는다. {@code Content-Length} 헤더를 믿지 않고 <b>실제로 읽은 양</b>을 세는 이유는, 헤더가 없거나
 * (chunked) 실제와 다를 수 있기 때문이다.
 */
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

    // 이 프로젝트는 nullability 애노테이션을 쓰지 않으므로, JetBrains 외부 애노테이션이 상위
    // FilterInputStream#read의 buffer에 걸어 둔 @NotNull을 애노테이션 없이 덮게 된다. 그것만을
    // 위해 org.jetbrains:annotations 의존을 들이지 않고 경고만 억제한다(PhoneNumber 선례).
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
