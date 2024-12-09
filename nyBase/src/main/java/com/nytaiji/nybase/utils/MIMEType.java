/*
 *  This file is part of VidSnap.
 *
 *  VidSnap is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  VidSnap is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with VidSnap.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.nytaiji.nybase.utils;

public class MIMEType {
    public static final String VIDEO_MP4 = "video/mp4";
    public static final String VIDEO_WEBM = "video/webm";
    public static final String AUDIO_MP4 = "audio/mp4";
    public static final String AUDIO_WEBM = "audio/webm";
    public static final String AUDIO_MPEG = "audio/mpeg";
    public static final String IMAGE_JPEG = "image/jpeg";
    public static final String TWITCH_URL = "Twitched";

    public static final String fileExtensionZip = "zip",
            fileExtensionJar = "jar",
            fileExtensionApk = "apk",
            fileExtensionApks = "apks";
    public static final String fileExtensionTar = "tar";
    public static final String fileExtensionGzipTarLong = "tar.gz", fileExtensionGzipTarShort = "tgz";
    public static final String fileExtensionBzip2TarLong = "tar.bz2",
            fileExtensionBzip2TarShort = "tbz";
    public static final String fileExtensionRar = "rar";
    public static final String fileExtension7zip = "7z";
    public static final String fileExtensionTarLzma = "tar.lzma";
    public static final String fileExtensionTarXz = "tar.xz";
    public static final String fileExtensionXz = "xz";
    public static final String fileExtensionLzma = "lzma";
    public static final String fileExtensionGz = "gz";
    public static final String fileExtensionBzip2 = "bz2";

    public static boolean isFileExtractable(String path) {
        String type = getExtension(path);

        return isZip(type)
                || isTar(type)
                || isRar(type)
                || isGzippedTar(type)
                || is7zip(type)
                || isBzippedTar(type)
                || isXzippedTar(type)
                || isLzippedTar(type)
                || isBzip2(type)
                || isGzip(type)
                || isLzma(type)
                || isXz(type);
    }

    private static boolean isZip(String type) {
        return type.endsWith(fileExtensionZip)
                || type.endsWith(fileExtensionJar)
                || type.endsWith(fileExtensionApk)
                || type.endsWith(fileExtensionApks);
    }

    private static boolean isTar(String type) {
        return type.endsWith(fileExtensionTar);
    }

    private static boolean isGzippedTar(String type) {
        return type.endsWith(fileExtensionGzipTarLong) || type.endsWith(fileExtensionGzipTarShort);
    }

    private static boolean isBzippedTar(String type) {
        return type.endsWith(fileExtensionBzip2TarLong) || type.endsWith(fileExtensionBzip2TarShort);
    }

    private static boolean isRar(String type) {
        return type.endsWith(fileExtensionRar);
    }

    private static boolean is7zip(String type) {
        return type.endsWith(fileExtension7zip);
    }

    private static boolean isXzippedTar(String type) {
        return type.endsWith(fileExtensionTarXz);
    }

    private static boolean isLzippedTar(String type) {
        return type.endsWith(fileExtensionTarLzma);
    }

    private static boolean isXz(String type) {
        return type.endsWith(fileExtensionXz) && !isXzippedTar(type);
    }

    private static boolean isLzma(String type) {
        return type.endsWith(fileExtensionLzma) && !isLzippedTar(type);
    }

    private static boolean isGzip(String type) {
        return type.endsWith(fileExtensionGz) && !isGzippedTar(type);
    }

    private static boolean isBzip2(String type) {
        return type.endsWith(fileExtensionBzip2) && !isBzippedTar(type);
    }

    private static String getExtension(String path) {
        return path.substring(path.indexOf('.') + 1).toLowerCase();
    }

}