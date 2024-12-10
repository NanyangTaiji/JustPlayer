package com.nytaiji.cloud;

/* renamed from: b.a.a.b.c */
public enum CloudAccounts {

    CLOUDRAIL("57dbd94b626e592ca46ae96d", "57dbd94b626e592ca46ae96d"),
    GDRIVE("772998477150-337sriv9en0738dofme4vp65frl5714s.apps.googleusercontent.com", "PRODUCTION"),
    DROPBOX("35x9e2zmn2m6q6t", "fv3yv6yqxlkkdjm"),
    BOX("4xmnmy28nikxdo9dbywrys4cjp72tpz4", "xxa88WhmbXPcPtiavc2lvERw0xVOIRGC"),
    ONEDRIVE("cade9aa0-5f5b-4bea-a032-d49cbb00f84b", "FmoStfOKxecQvKvdo3YZwze"),
    PCLOUD("zWpOFIGjwx8", "XbMO9h33RjkaRTwFytkGdS96MnDy");

  //  DROPBOX("zy0s8xgg42qhuo3", "6nmlvliiy8un4xe"),
  //  ONEDRIVE("11c45762-1cb9-4cd5-a8bb-37cb076859f4", "hyjHQPW208)%{nbbhLOE74{"),
    

    /* renamed from: a */
    private String clientId;

    /* renamed from: b */
    private String clientKey;

    private CloudAccounts(String id, String key) {
        this.clientId = id;
        this.clientKey = key;
    }

    /* renamed from: a */
    public String getClientId() {
        return this.clientId;
    }

    /* renamed from: b */
    public String getClientKey() {
        return this.clientKey;
    }
}
