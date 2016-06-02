package db.framework.utils;

import db.framework.runner.MainRunner;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import net.lightbody.bmp.filters.RequestFilter;
import net.lightbody.bmp.filters.ResponseFilter;
import net.lightbody.bmp.util.HttpMessageContents;
import net.lightbody.bmp.util.HttpMessageInfo;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

public class ProxyFilters {
    static String envURL;
    static HashSet filterDomains = new HashSet();
    static String domain;
    static String[] excludeDomains = new String[]{
            "coremetrics",
            "thebrighttag",
            "google-analytics"
    };

    static void createFilterDomains(String url) {
        envURL = url;
        domain = getDomain(envURL);
        filterDomains.add("assets." + domain);
    }

    public static String getDomain(String url) {
        try {
            return new URL(url).getHost().replaceAll("www.", "").replaceAll("www1.", "");
        } catch (MalformedURLException e) {
            return url;
        }
    }

    public static void main(String[] args) {
        createFilterDomains("http://data.coremetrics.com/cookie-id.js?fn=eluminate967");
    }

    private static File getCacheFile(String url) {
        return new File(MainRunner.temp + DigestUtils.sha256Hex(url));
    }

    private static boolean isFilter(String url, HttpMessageContents contents) {
        String domain = getDomain(url);
        for (String exclude : excludeDomains) {
            if (domain.contains(exclude))
                return false;
        }
        if (contents == null || !contents.isText())
            return false;
        return !domain.equals(ProxyFilters.domain) || filterDomains.contains(domain);
    }

    public static class ProxyRequestFilter implements RequestFilter {
        public ProxyRequestFilter(String envUrl) {
            createFilterDomains(envUrl);
        }

        @Override
        public HttpResponse filterRequest(HttpRequest request, HttpMessageContents contents, HttpMessageInfo messageInfo) {
            String url = messageInfo.getOriginalUrl();
            if (isFilter(url, contents)) {
                File fcache = getCacheFile(url);
                if (fcache.exists()) {
                    if (fcache.length() == 0)
                        fcache.delete();
                    else {
                        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(Utils.readSmallBinaryFile(fcache)));
                        response.headers().set(CONTENT_TYPE, contents.getContentType());
                        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
                        return response;
                    }
                }
            }
            return null;
        }
    }

    public static class ProxyResponseFilter implements ResponseFilter {
        @Override
        public void filterResponse(HttpResponse response, HttpMessageContents contents, HttpMessageInfo messageInfo) {
            String url = messageInfo.getOriginalUrl();
            if (response.getStatus().equals(HttpResponseStatus.OK) && isFilter(url, contents)) {
                File fcache = getCacheFile(url);
                if (!fcache.exists()) {
                    Utils.writeSmallBinaryFile(contents.getBinaryContents(), fcache);
                }
            }
        }
    }
}
