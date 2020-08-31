package org.householdgoods.hilt;

import org.householdgoods.BuildConfig;
import org.householdgoods.retrofit.LoggingInterceptor;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;


public class OkHttpClientModule {


    public OkHttpClient getOkHttpClient(Interceptor interceptor, String key, String secret) throws Exception {
        OkHttpClient.Builder httpClient = new OkHttpClient().newBuilder()
                .connectTimeout(40, TimeUnit.SECONDS)
                .readTimeout(40, TimeUnit.SECONDS)
                .writeTimeout(40, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .header("Authorization", "Basic " + key + ":" + secret)
                            .method(original.method(), original.body())
                            .build();

                    return chain.proceed(request);
                });
        if (BuildConfig.DEBUG) {
            overrideSslSocketFactory(httpClient);
        }
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(6);
        httpClient.dispatcher(dispatcher);
        httpClient.connectTimeout(30, TimeUnit.SECONDS);
        httpClient.readTimeout(30, TimeUnit.SECONDS);
        if (interceptor != null) {
            httpClient.addInterceptor(interceptor);
        }
        return httpClient.build();
    }

    public Interceptor getInterceptor() {
        return new LoggingInterceptor();
    }

    public void overrideSslSocketFactory(OkHttpClient.Builder builder) throws NoSuchAlgorithmException, KeyManagementException {
        // Create a trust manager that does not validate certificate chains
        final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };

        // Install the all-trusting trust manager
        final SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        // Create an ssl socket factory with our all-trusting manager
        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
        builder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
    }
}

