package org.tutar.bot.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTP类操作方法集合。
 *
 * @author fish at 2015/07/27
 */
@Slf4j
public final class HttpUtils {

    public static final MediaType JSON = MediaType.parse("application/json;charset=utf-8");

    private static Pattern p = Pattern.compile("(?<=//|)((\\w[-]*)+\\.)+\\w+");

    private static ObjectMapper mapper = JsonMapper.nonEmptyMapper().getMapper();

    /**
     * 通过{@link HttpServletRequest}获取远程客户端IP地址。
     *
     * @return 客户端IP地址。
     */
    public static String getRemoteAddress(HttpServletRequest request) {
        // String ip = request.getHeader("X-Real-IP");
        String ip = request.getHeader("x-forwarded-for");
        if (null != ip && ip.length() != 0) {
            if ("unknown".equalsIgnoreCase(ip)) {
                ip = null;
            } else {
                String[] ips = ip.split(",");
                for (String tmpIP : ips) {
                    if (!"unknown".equalsIgnoreCase(ip.trim())) {
                        return tmpIP;
                    }
                }
            }
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }

    /**
     * 通过URL获取域名。
     *
     * @param url URL地址。
     * @return 域名。
     */
    public static String getDomain(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        Matcher m = p.matcher(url);
        return m.find() ? m.group() : null;
    }


    /**
     * 获取当前主机host和port
     *
     * @param url
     */
    public static String getHost(String url) {
        try {
            java.net.URL abc = new java.net.URL(url);

            String host = abc.getHost();
            int port = abc.getPort();

            if (port == -1) {
                return host;
            }
            return host + ":" + port;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    /**
     * 通过URL获取设置Cookie的主域名。
     *
     * @param url URL地址。
     * @return 域名。
     */
    public static String getCookieMainDomain(String url) {
        url = getDomain(url);
        String[] sections = url.split("\\.");
        int length = sections.length;
        if (length >= 3) {
            return "." + sections[length - 2] + "." + sections[length - 1];
        }

        return url;
    }

    /**
     * 判断域名是否匹配。
     *
     * @param target 目标匹配域名。
     * @param source 源匹配域名。
     * @return true：匹配；false：不匹配。
     */
    public static boolean isDomainMatch(String target, String source) {
        if (target == null || target.isEmpty() || source == null || source.isEmpty()) {
            return false;
        }
        if (target.charAt(0) == '.') {
            target = target.substring(1);
        }
        if (source.charAt(0) == '.') {
            source = source.substring(1);
        }
        if (target.equals(source)) {
            return true;
        }
        return (source.endsWith(target) && source.charAt(source.length() - target.length() - 1) == '.');
    }

    /**
     * 在URL后附加参键值对。
     *
     * @param url   URL地址。
     * @param name  键。
     * @param value 值。
     * @return 附加后的URL。
     */
    public static String append(String url, String name, String value) throws UnsupportedEncodingException {
        final String padPage = "/";
        if (url.contains("?")) {
            url += "&";
        } else {
            while (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }
            if (null != url && !"".equals(url)) {
                String domain = getDomain(url);
                if (url.indexOf(domain) + domain.length() == url.length()) {
                    url += padPage;
                }
            }
            url += "?";
        }
        return url + name + "=" + URLEncoder.encode(value, "UTF-8");
    }


    /**
     * 在URL后附加参键值对。
     *
     * @param url URL地址。
     * @return 附加后的URL。
     */
    public static String append(String url, Map<String, String> params) throws UnsupportedEncodingException {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            url = append(url, entry.getKey(), entry.getValue());
        }
        return url;
    }


    /**
     * 从URL中移除指定的参数。
     *
     * @param url        URL。
     * @param paramNames 参数名称集合。
     * @return 移除参数后的URL。
     */
    public static String removeUrlParameters(String url, String[] paramNames) {
        //target=http%3A%2F%2Fwww.vanke.cn%2Fseller%2Findex%3Fsso_token%3DPfwuvF0Wkl3Ow7hR%2FwbVQHKV%2Flvjce%25202eenFg4KE9n8LzwF9U32O%2FJ9ZJKT5OxMjzFeH9vjZD60Y21TiUXhjuKrEm7j8wRw%2520fHzc%2F2TBm%2520hA%2Fp5Up10BvwzhlMKg76GtPctpVZrnUnHjfaTUSa54xxZadxHihrLovQBKaz75c%2Fo%3D%26sso_token%3DjVD%252Bvwc5dQiVBipqcRYkF4DLqQK5lEAS39WZLiHWTekxpo4tStkr6LhgXI0Fla62tnaXSY7QLInCIXEiNEpDHm0Ii4ZkedlMa05h8jeZE224c6eGGaFpyrPQz%252FOgzND1TjUkzNtk707pa58%252FQj9BCOhBlAcNn9v99lUrHtMKNgU%253D%26sso_sign%3DGNvWljDTPh9ktUH2hT%252FAVm6XC7UmbokQJ2EikEuz7TPlX7HzUBHA%252BfT%252BbN3Mmdal%252F%252BnSLKnZICupuKsU5DHZpxo%252BmvjpCoChULJU4u9yEBGhqAlKINvQz2aI3%252Bq3QRd8gR4%252Be8Y8owq13SSrqdGJuHiTHEBgz90M8nkEIj3%252B1ig%253D%26sso_ticket%3D3b636f1ba0fa41ee834aabd2aed969f6%26sso_data%3Dcd00f0dc040364308a08d87487fc77efba11d13e2d93e3ce8db5437b78ec00a293d7198fde90bf50de0c5098ece79721e6fe04919567289eb09696188472d51365c00ead5745be14b02bc4d1e58d5c45563da708e3bab76d9b139ea77560cd6efd2baf193ecc74e4f9a78a1eece75417b1541fa468400bff6867ad5e1ec0fc81559fc9147e8c78fd48eceff3c56aacdb0572819ed272a901b3a1e7950dd442a3a05da0beee07b8466f01f5c24b9f6347b7c45bbf2d5ad8bd2b259098957307a67d81e947213dad0c31a135e3e50a17c53f78de1cb3b8d0a46d59cdce961506f7f515ee874f11987ec68bc8e759a928d5383c5d6cf4684e74c51b0aa13ec1cc6afcc07a0a675acef6ee14c63661bb8d36165a31740a735f8fa131043e111f3a49b706ded9ddc964d1535465b7384422a14eccae041b3788204bdc05dd91088335
        if(url.startsWith("target=")){
            url = url.substring(7,url.length());
            try {
                url=  URLDecoder.decode(url,"UTF-8");
            }catch (Exception ex){

            }
        }
        if(url.startsWith("target=")){
            url.substring(7,url.length());
        }

        String reg = null;
        for (String param : paramNames) {
            reg = "(?<=[\\?&])" + param + "=[^&]*&?";
            url = url.replaceAll(reg, "");
        }
        url = url.replaceAll("&+$", "");
        if (url.lastIndexOf("?") == url.length() - 1) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }





    /**
     * 辅助方法：将参数集合转换成地址栏参数格式，比如a=1&b=2。
     *
     * @param paramValues 参数集合。
     * @param encoding    指定编码格式。
     * @return 地址栏参数格式。
     * @throws UnsupportedEncodingException
     */
    private static String encodingParams(Map<String, String> paramValues, String encoding) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        if (null == paramValues) {
            return null;
        }

        for (Map.Entry<String, String> entry : paramValues.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(URLEncoder.encode(entry.getValue(), encoding));
        }
        return sb.toString();
    }


    /**
     * get 请求
     * @param url url
     * @param header 请求头参数
     * @param query 参数
     * @return
     */
    public static String doGet(OkHttpClient client,String url, Map<String, Object> header, Map<String, String> query) throws Exception {

        // 创建一个请求 Builder
        Request.Builder builder = new Request.Builder();
        // 创建一个 request
        Request request = builder.url(url).build();

        // 创建一个 HttpUrl.Builder
        HttpUrl.Builder urlBuilder = request.url().newBuilder();
        // 创建一个 Headers.Builder
        Headers.Builder headerBuilder = request.headers().newBuilder();

        // 装载请求头参数
        Iterator<Map.Entry<String, Object>> headerIterator = header.entrySet().iterator();
        headerIterator.forEachRemaining(e -> {
            headerBuilder.add(e.getKey(), (String) e.getValue());
        });

        // 装载请求的参数
        Iterator<Map.Entry<String, String>> queryIterator = query.entrySet().iterator();
        queryIterator.forEachRemaining(e -> {
            urlBuilder.addQueryParameter(e.getKey(),  e.getValue());
        });

        // 设置自定义的 builder
        // 因为 get 请求的参数，是在 URL 后面追加  http://xxxx:8080/?name=xxxx?sex=1
        builder.url(urlBuilder.build()).headers(headerBuilder.build());

        try (Response execute = client.newCall(builder.build()).execute()) {
            return execute.body().string();
        }
    }


    /**
     * 兼容忽略HTTPS
     *
     * @param url
     * @param param
     */
    public static Response doPost(OkHttpClient client, String url,@NotNull Object param,
                                  Map<String,String> headers) {
        try {
            // TODO content-type:application/x-www-form-urlencoded
//                FormBody.Builder formBuilder = new FormBody.Builder();
//                for (Map.Entry<String, String> entry : params.entrySet()) {
//                    formBuilder.add(entry.getKey(), entry.getValue());
//                }
//                RequestBody formBody = formBuilder.build();
            Request.Builder requestBuilder = new Request.Builder();

            for(Map.Entry<String,String> entry:headers.entrySet()){
                requestBuilder.addHeader(entry.getKey(),entry.getValue());
            }
            String paramJson = mapper.writeValueAsString(param);
            log.debug("param json:{}",paramJson);
            RequestBody body = RequestBody.create(MediaType.parse("application/json"), paramJson);
            Request request = requestBuilder
                    .url(url)
                    .method("POST", body)
                    .build();

            Response response = client.newCall(request).execute();
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *  http请求预处理
     * @param client
     * @param url
     * @return
     */
    private static OkHttpClient filterHttp(OkHttpClient client,String url){
        //兼容忽略HTTPS
        String https ="https";
        if (url.toLowerCase().startsWith(https) ) {
            SSLContext sslContext = null;
            final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(
                        X509Certificate[] chain,
                        String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(
                        X509Certificate[] chain,
                        String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    X509Certificate[] x509Certificates = new X509Certificate[0];
                    return x509Certificates;
                }
            }};
            try {
                sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts,
                        new java.security.SecureRandom());
            } catch (Exception e) {
                new RuntimeException(e);
            }
        }
        return client;
    }

    /**
     *  json字符串作为参数
     * @param client
     * @param url
     * @param json
     * @return
     */
    public static Response post(OkHttpClient client, String url, String json) {
        if (url.startsWith("https") || url.startsWith("HTTPS")) {
            SSLContext sslContext = null;
            final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(
                        X509Certificate[] chain,
                        String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(
                        X509Certificate[] chain,
                        String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    X509Certificate[] x509Certificates = new X509Certificate[0];
                    return x509Certificates;
                }
            }};
            try {
                sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts,
                        new java.security.SecureRandom());
            } catch (Exception e) {
                new RuntimeException(e);
            }
        }
        Request request = new Request.Builder()
                .url(url)
                .build();

        if(!StringUtils.isEmpty(json)){
            RequestBody formBody = RequestBody.create(JSON, json);
            request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
        }

        try {
            Response response = client.newCall(request).execute();
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}