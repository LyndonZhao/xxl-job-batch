package pn.lyndon.common.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;

/**
 * Description:
 *
 * @author Administrator
 * @create 2019-11-17 10:20
 */
@Slf4j
@Configuration
public class RestTemplateConfiguration {
    /**
     *     连接池的最大连接数默认为0
      */
    @Value("${http.config.maxTotalConnect:200}")
    private int maxTotalConnect;
    /**
     *     单个主机的最大连接数
      */
    @Value("${http.config.maxConnectPerRoute:100}")
    private int maxConnectPerRoute;
    /**
     * 连接超时默认2s
     */
    @Value("${http.config.validateAfterInactivity:20}")
    private int validateAfterInactivity;
    /**
     * 连接超时默认2s
     */
    @Value("${http.config.connectTimeout:5}")
    private int connectTimeout;
    /**
     * 读取超时默认30s
     */
    @Value("${http.config.readTimeout:30}")
    private int readTimeout;
    /**
     * 读取超时默认3s
     */
    @Value("${http.config.connectionRequestTimeout:3}")
    private int connectionRequestTimeout;
    /**
     * 默认重试3次
     */
    @Value("${http.config.retryCount:3}")
    private int retryCount;
    /**
     * 默认清理超过30秒以上的idle请求
     */
    @Value("${http.config.maxIdleTime:30}")
    private int maxIdleTime;

    @Value("http.config.retryExceptions:InterruptedIOException,UnknownHostException")
    private String retryExceptions;

    private HttpRequestRetryHandler getHttpRequestRetryHandler() {
        return (IOException exception, int executionCount, HttpContext context) -> {

            if (executionCount > retryCount) {
                return false;
            }

            List<String> retryExceptionList = Arrays.asList(retryExceptions.split(","));

            ClassLoader classLoader = RestTemplateConfiguration.class.getClassLoader();

            if (retryExceptionList.contains(exception.getClass().getSimpleName())) {
                return true;
            }
            final HttpClientContext clientContext = HttpClientContext.adapt(context);
            final HttpRequest request = clientContext.getRequest();
            return request instanceof HttpEntityEnclosingRequest;
        };
    }

    /**
     *     创建HTTP客户端工厂
      */
    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        SSLContext sslContext = null;
        try {
            // setup a Trust Strategy that allows all certificates.
            sslContext = new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build();
            httpClientBuilder.setSSLContext(sslContext);
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            log.info(ExceptionUtils.getStackTrace(e));
        }

        // don't check Hostnames
        HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;

        // here's the special part:
        // -- need to create an SSL Socket Factory, to use our weakened "trust strategy";
        // -- and create a Registry, to register it.
        //
        SSLConnectionSocketFactory sslConnectionSocketFactory =
            new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
        Registry<ConnectionSocketFactory> socketFactoryRegistry =
            RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslConnectionSocketFactory).build();

        // now, we create connection-manager using our Registry.
        // -- allows multi-threaded use
        PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        connMgr.setMaxTotal(maxTotalConnect);
        connMgr.setDefaultMaxPerRoute(maxConnectPerRoute);
        connMgr.setValidateAfterInactivity(validateAfterInactivity * 1000);
        // 设置到某个路由的最大连接数，会覆盖defaultMaxPerRoute
        // connMgr.setMaxPerRoute(new HttpRoute(new HttpHost("somehost", 80)), 150);
        httpClientBuilder.setConnectionManager(connMgr);

        // set retry function
        httpClientBuilder.setRetryHandler(getHttpRequestRetryHandler());
        httpClientBuilder.setKeepAliveStrategy((final HttpResponse response, final HttpContext context)->{
            long keepAliveDuration = DefaultConnectionKeepAliveStrategy.INSTANCE.getKeepAliveDuration(response,context);
           return (keepAliveDuration != -1) ? keepAliveDuration: 30;
        });

        httpClientBuilder.setConnectionManagerShared(true);
        // 清理过期的连接
        httpClientBuilder.evictExpiredConnections();
        // 清理长时间处于idle的连接
        httpClientBuilder.evictIdleConnections(maxIdleTime * 1000, TimeUnit.SECONDS);

        // finally, build the HttpClient;
        // -- done!
        CloseableHttpClient client = httpClientBuilder.build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(client);

        factory.setConnectTimeout(connectTimeout * 1000);
        factory.setReadTimeout(readTimeout * 1000);
        factory.setConnectionRequestTimeout(connectionRequestTimeout * 1000);
        return factory;
    }

    // 初始化RestTemplate,并加入spring的Bean工厂，由spring统一管理
    @Bean
    @ConditionalOnMissingBean(RestTemplate.class)
    public RestTemplate getRestTemplate(ClientHttpRequestFactory clientHttpRequestFactory) {
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        setPersonalMessageConverter(restTemplate);
        return restTemplate;
    }

    private void setPersonalMessageConverter(RestTemplate restTemplate) {
        List<HttpMessageConverter<?>> converterList = restTemplate.getMessageConverters();

        // 重新设置StringHttpMessageConverter字符集为UTF-8，解决中文乱码问题
        converterList.removeIf(converter -> (converter instanceof StringHttpMessageConverter));
        converterList.add(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        // 加入FastJson转换器, 为什么使用FastJson而不是默认的
        // 参考 https://github.com/itzujun/ParseJson4Android
        converterList.removeIf(converter -> (converter instanceof AbstractGenericHttpMessageConverter));
        converterList.add(new FastJsonHttpMessageConverter());
    }

}