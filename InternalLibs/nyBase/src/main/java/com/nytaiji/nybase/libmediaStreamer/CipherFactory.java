package com.nytaiji.nybase.libmediaStreamer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

public interface CipherFactory {
    @Nullable
    Cipher getCipher() throws GeneralSecurityException;

    @NonNull
    Cipher rebaseCipher(@NonNull byte[] var1) throws GeneralSecurityException;
}
