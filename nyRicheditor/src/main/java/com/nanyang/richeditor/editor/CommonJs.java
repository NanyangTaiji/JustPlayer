package com.nanyang.richeditor.editor;

/**
 * @author Rex on 2019/6/20.
 */
public class CommonJs {

    /**
     * 为全局图片加上点击事件 回调 imageOnclick.openImage imageOnclick.openImage需要客户端对应实现
     * 时机为window.onload
     */
    public final static String IMG_CLICK_JS = "<script type='text/javascript'>window.onload = function(){" +
            "var $img = document.getElementsByTagName('img');" +
            "for(var p in  $img){" +
            "    if (typeof $img[p] === 'object') {" +
            "        $img[p].style.width = '100%';" +
            "        $img[p].style.height ='auto';" +
            "        $img[p].onclick = function(e){" +
            "            ImgClick(e.srcElement.src);" +
            "        };" +
            "    }" +
            "}" +
            "};" +
            "function ImgClick(src) {" +
            "    var message = {" +
            "        'imgUrl' : src," +
            "    };" +
            "   window.imageOnclick.openImage(src);" +
            "};" +
            "</script>";

    /**
     * 为全局图片加上点击事件 回调 imageOnclick.openImage imageOnclick.openImage需要客户端对应实现
     * 时机为window.onload
     */
    public final static String VIDEO_CLICK_JS = "<script type='text/javascript'>window.onload = function(){" +
            "var $video = document.getElementsByTagName('video');" +
            "for(var p in  $video){" +
            "    if (typeof $video[p] === 'object') {" +
            "        $video[p].style.width = '100%';" +
            "        $video[p].style.height ='auto';" +
            "        $video[p].onclick = function(e){" +
            "            VideoClick(e.srcElement.src);" +
            "        };" +
            "    }" +
            "}" +
            "};" +
            "function VideoClick(src) {" +
            "    var message = {" +
            "        'videoUrl' : src," +
            "    };" +
            "   window.videoOnclick.openVideo(src);" +
            "};" +
            "</script>";
}
