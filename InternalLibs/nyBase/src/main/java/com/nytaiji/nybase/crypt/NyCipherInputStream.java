package com.nytaiji.nybase.crypt;


import java.io.*;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

public class NyCipherInputStream extends FilterInputStream {
    private InputStream encryptedInputStream;
    private CipherInputStream cipherInputStream;


    public NyCipherInputStream(InputStream encryptedInputStream, Cipher cipher) throws IOException {
        super(new CipherInputStream(encryptedInputStream, cipher));
        this.encryptedInputStream = encryptedInputStream;
        this.cipherInputStream = new CipherInputStream(encryptedInputStream, cipher);
    }


    @Override
    public int read() throws IOException {
        return cipherInputStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return cipherInputStream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return cipherInputStream.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return encryptedInputStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return cipherInputStream.available();
    }

    @Override
    public void close() throws IOException {
        cipherInputStream.close();
        encryptedInputStream.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        cipherInputStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        cipherInputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return cipherInputStream.markSupported();
    }
}

