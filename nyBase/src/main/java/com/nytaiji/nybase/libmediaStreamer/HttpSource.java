package com.nytaiji.nybase.libmediaStreamer;

import androidx.annotation.Nullable;


import com.nytaiji.nybase.model.NyHybrid;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.crypto.Cipher;

public interface HttpSource {
    void setCipher(Cipher cipher);

    void setCipherFactory(CipherFactory cipherFactory);

    void setSource(URI var1, long offset);

    void setSource(NyHybrid nyHybrid, long offset);

    @Nullable
    String getUriString();

    boolean isExisting();

    boolean isReadable();

    @Nullable
    InputStream getInputStream() throws IOException;

    boolean isPartial();

    long getContentSize();

    long getOffset();

    long getContentLength();

    @Nullable
    String getContentType();
}

