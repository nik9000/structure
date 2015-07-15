package com.github.nik9000.structure;

import java.io.InputStream;
import java.io.OutputStream;

public class Bytes {
    public interface Source {
        boolean hasNext() throws Bytes.IOException;

        byte next() throws Bytes.IOException;
    }

    public interface Sync {
        void put(byte b) throws Bytes.IOException;
    }

    /**
     * Thrown when there is an error reading from the Byte.Source or writing to
     * the Byte.Sync. Note that it extends RuntimeException but otherwise you
     * can think of it just like IOException. Note: good people import this as
     * Byte.IOException and never refer to it as IOException.
     */
    public static class IOException extends RuntimeException {
        private static final long serialVersionUID = 3491898550182662389L;

        public IOException(String message, Throwable cause) {
            super(message, cause);
        }

        public IOException(String message) {
            super(message);
        }

        public IOException(Throwable cause) {
            super(cause);
        }
    }

    /**
     * Thrown when client code attempts to read from a Byte.Source when hasNext
     * would return false. Note: good people import this as Byte.EOFException
     * and never refer to it as EOFException.
     */
    public static class EOFException extends Bytes.IOException {
        private static final long serialVersionUID = -2195742134343099762L;

        public EOFException() {
            super("Unexpected end of file");
        }

        public EOFException(Throwable cause) {
            super("Unexpected end of file", cause);
        }
    }

    /**
     * Implementation of ByteSync backed by an OutputStream.
     */
    public static class OutputStreamByteSync implements Bytes.Sync {
        private final OutputStream o;

        public OutputStreamByteSync(OutputStream o) {
            this.o = o;
        }

        @Override
        public void put(byte b) {
            try {
                o.write(b);
            } catch (java.io.IOException e) {
                throw new Bytes.IOException(e);
            }
        }
    }

    /**
     * Implementation of ByteSource backed by an InputStream.
     */
    public static class InputStreamByteSource implements Bytes.Source {
        private final InputStream i;
        private int next;

        public InputStreamByteSource(InputStream i) {
            this.i = i;
            next();
        }

        @Override
        public boolean hasNext() {
            return 0 <= next && next <= 255;
        }

        @Override
        public byte next() {
            if (!hasNext()) {
                throw new Bytes.EOFException();
            }
            int ret = next;
            queueNext();
            return (byte) ret;
        }

        private void queueNext() {
            try {
                next = i.read();
            } catch (java.io.IOException e) {
                throw new Bytes.IOException(e);
            }
        }
    }

    public static class BytesRefBytesSource implements Bytes.Source {
        private final byte[] bytes;
        private final int end;
        private int index;

        public BytesRefBytesSource(byte[] bytes, int offset, int length) {
            this.bytes = bytes;
            index = offset;
            end = offset + length;
        }

        @Override
        public boolean hasNext() throws IOException {
            return index < end;
        }

        @Override
        public byte next() throws IOException {
            byte b = bytes[index];
            index += 1;
            return b;
        }
    }
}
