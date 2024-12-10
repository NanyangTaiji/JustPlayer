package com.nytaiji.nybase.crypt;


import static com.nytaiji.nybase.crypt.EncryptUtil.CipherFromPath;
import static com.nytaiji.nybase.crypt.EncryptUtil.CipherPackageFromPath;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nytaiji.nybase.libmediaStreamer.CipherFactory;

import java.security.GeneralSecurityException;
import java.util.Objects;

import javax.crypto.Cipher;

public class NyCipherFactory implements CipherFactory {
    private String path;

    public NyCipherFactory(String path){
        this.path=path;
    }

    @Nullable
    @Override
    public Cipher getCipher() throws GeneralSecurityException {
        byte[] initialIV = CipherPackageFromPath(path).cipher.getIV();
        return rebaseCipher(initialIV);
    }

    @NonNull
    @Override
    public Cipher rebaseCipher(@NonNull byte[] bytes) throws GeneralSecurityException {
        return Objects.requireNonNull(CipherFromPath(path));
    }
}
