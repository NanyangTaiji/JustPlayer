package com.nytaiji.nybase.home;


import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.nytaiji.nybase.R;


public class RootInfo {

    public String rootId;
    public int icon;
    public int derivedColor;
    public String title;
    public String path;
    public long availableBytes;
    public long totalBytes;
    public int type;


    public RootInfo() {
        reset();
    }

    public void reset() {
        rootId = null;
        title = null;
        path = null;
        icon = 0;
        availableBytes = -1;
        totalBytes = -1;
        derivedColor = R.color.colorPrimary;
    }


  /*  public RootInfo(String rootId, int derivedColor) {

        // TODO: remove these special case icons
        if (isInternalStorage()) {
            derivedIcon = R.drawable.ic_root_internal;
            derivedTag = "storage";
        } else if (isExternalStorage()) {
            derivedIcon = R.drawable.ic_root_sdcard;
            derivedTag = "external_storage";
        } else if (isPhoneStorage()) {
            derivedIcon = R.drawable.ic_root_device;
            derivedTag = "phone";
        } else if (isSecondaryStorage()) {
            derivedIcon = R.drawable.ic_root_sdcard;
            if (isUsb() || isSecondaryStorageUSB()) {
                derivedIcon = R.drawable.ic_root_usb;
            } else if (isSecondaryStorageHDD()) {
                derivedIcon = R.drawable.ic_root_hdd;
            }
            derivedTag = "secondary_storage";
        } else if (isUsbStorage()) {
            derivedIcon = R.drawable.ic_root_usb;
            derivedTag = "usb_storage";
        } else if (isDownloadsFolder()) {
            derivedIcon = R.drawable.ic_root_download;
            derivedTag = "downloads";
        } else if (isEncryptionFolder()) {
            derivedIcon = R.drawable.icons8_data_encryption_30;
            derivedTag = "encryption";
        } else if (isBluetoothFolder()) {
            derivedIcon = R.drawable.ic_root_bluetooth;
            derivedTag = "bluetooth";
        } else if (isAppBackupFolder()) {
            derivedIcon = R.drawable.ic_root_appbackup;
            derivedTag = "appbackup";
        } else if (isBookmarkFolder()) {
            derivedIcon = R.drawable.ic_root_bookmark;
            derivedTag = "bookmark";
        } else if (isHiddenFolder()) {
            derivedIcon = R.drawable.ic_root_hidden;
            derivedTag = "hidden";
        } else if (isDownloads()) {
            derivedIcon = R.drawable.ic_root_download;
            derivedTag = "downloads";
        } else if (isImages()) {
            derivedIcon = R.drawable.ic_root_image;
            derivedColor = R.color.item_doc_image;
            derivedTag = "images";
        } else if (isVideos()) {
            derivedIcon = R.drawable.ic_root_video;
            derivedColor = R.color.item_doc_video;
            derivedTag = "videos";
        } else if (isAudio()) {
            derivedIcon = R.drawable.ic_root_audio;
            derivedColor = R.color.item_doc_audio;
            derivedTag = "audio";
        } else if (isDocument()) {
            derivedIcon = R.drawable.ic_root_document;
            derivedColor = R.color.item_doc_pdf;
            derivedTag = "document";
        } else if (isArchive()) {
            derivedIcon = R.drawable.ic_root_archive;
            derivedColor = R.color.item_doc_compressed;
            derivedTag = "archive";
        } else if (isApk()) {
            derivedIcon = R.drawable.ic_root_apk;
            derivedColor = R.color.item_doc_apk;
            derivedTag = "apk";
        } else if (isUserApp()) {
            derivedIcon = R.drawable.ic_root_apps;
            derivedColor = R.color.item_doc_apps;
            derivedTag = "user_apps";
        } else if (isSystemApp()) {
            derivedIcon = R.drawable.ic_root_system_apps;
            derivedColor = R.color.item_doc_apps;
            derivedTag = "system_apps";
        } else if (isAppProcess()) {
            derivedIcon = R.drawable.ic_root_process;
            derivedTag = "process";
        } else if (isRecents()) {
            derivedIcon = R.drawable.ic_root_recent;
            derivedTag = "recent";
        } else if (isHome()) {
            derivedIcon = R.drawable.ic_root_home;
            derivedTag = "home";
        } else if (isConnections()) {
            derivedIcon = R.drawable.ic_root_connections;
            derivedTag = "connections";
        } else if (isServerStorage()) {
            derivedIcon = R.drawable.ic_root_server;
            derivedColor = R.color.item_connection_server;
            derivedTag = "server";
        } else if (isNetworkStorage()) {
            derivedIcon = R.drawable.ic_root_network;
            derivedColor = R.color.item_connection_client;
            derivedTag = "network";
        } else if (isCloudStorage()) {
            if (isCloudGDrive()) {
                derivedIcon = R.drawable.ic_root_gdrive;
            } else if (isCloudDropBox()) {
                derivedIcon = R.drawable.ic_root_dropbox;
            } else if (isCloudOneDrive()) {
                derivedIcon = R.drawable.ic_root_onedrive;
            } else if (isCloudBox()) {
                derivedIcon = R.drawable.ic_root_box;
            } else {
                derivedIcon = R.drawable.ic_root_cloud;
            }
            derivedColor = R.color.item_connection_cloud;
            derivedTag = "cloud";
        } else if (isExtraStorage()) {
            if (isWhatsApp()) {
                derivedIcon = R.drawable.ic_root_whatsapp;
                derivedColor = R.color.item_whatsapp;
                derivedTag = "whatsapp";
            } else if (isTelegram()) {
                derivedIcon = R.drawable.ic_root_telegram;
                derivedColor = R.color.item_telegram;
                derivedTag = "telegram";
            } else if (isTelegramX()) {
                derivedIcon = R.drawable.ic_root_telegram;
                derivedColor = R.color.item_telegramx;
                derivedTag = "telegramx";
            }
        } else if (isTransfer()) {
            derivedIcon = R.drawable.ic_root_transfer;
            derivedColor = R.color.item_transfer;
            derivedTag = "transfer";
        } else if (isCast()) {
            derivedIcon = R.drawable.ic_root_cast;
            derivedColor = R.color.item_cast;
            derivedTag = "cast";
        } else if (isReceiveFolder()) {
            derivedIcon = R.drawable.ic_stat_download;
            derivedTag = "receivefiles";
        }
    }*/

    public Drawable loadIcon(Context context) {
        return IconUtils.applyTintAttr(context, icon,
                android.R.attr.textColorPrimary);

    }

    public Drawable loadDrawerIcon(Context context) {
        // if (derivedIcon != 0) {
        return IconUtils.applyTintAttr(context, icon,
                android.R.attr.textColorPrimary);
        // } else {
        // return IconUtils.loadPackageIcon(context, authority, icon);
        // }
    }

    public Drawable loadGridIcon(Context context) {
        return IconUtils.applyTintAttr(context, icon,
                android.R.attr.textColorPrimaryInverse);

    }

   /* public Drawable loadToolbarIcon(Context context) {
        return IconUtils.applyTintAttr(context, icon, R.attr.colorControlNormal);
    }*/


    public Drawable loadShortcutIcon(Context context) {
        return IconUtils.applyTint(context, icon,
                ContextCompat.getColor(context, android.R.color.white));
    }

}