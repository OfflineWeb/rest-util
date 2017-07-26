/*
 *
 *   The MIT License
 *
 *   Copyright 2017 papa.
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
 */

package offlineweb.common.restconnector;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import offlineweb.common.logger.annotations.Loggable;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple REST client
 * @author papa
 *         created on 7/24/17.
 */
@Loggable
public class RESTClient {

    /**
     * Handler of HTTP GET request
     * @param URL URL to connect
     * @param <T> JSON object type, default is LinkedHashMap
     * @return An object of type T, default is LinkedHashMap
     * @throws IOException
     */
    public static <T> T get(String URL) throws IOException {
        return get(URL, null, null);
    }

    /**
     * Handler of HTTP GET request
     * @param URL URL to connect
     * @param queryParams queries to append to the URL
     * @param <T> JSON object type, default is LinkedHashMap
     * @return An object of type T, default is LinkedHashMap
     * @throws IOException
     */
    public static <T> T get(String URL, Map<String, String> queryParams)
            throws IOException {
        return get(URL, queryParams, null);
    }

    /**
     * Handler of HTTP GET request
     * @param URL URL to connect
     * @param queryParams queries to append to the URL
     * @param headers Additional headers, key-value pair
     * @param <T> JSON object type, default is LinkedHashMap
     * @return An object of type T, default is LinkedHashMap
     * @throws IOException
     */
    public static <T> T get(String URL, Map<String, String> queryParams,
                            Map<String, String > headers)  throws IOException {

        CloseableHttpClient httpClient = getHttpClient();
        HttpGet httpGet = new HttpGet(formUrl(URL, queryParams));
        addHeader(httpGet, headers);
        addCommonHeader(httpGet);

        ResponseHandler<T> responseHandler = new ResponseHandler<T>() {

            public T handleResponse(final HttpResponse response)
                    throws ClientProtocolException, IOException {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    if (entity == null) {
                        return null;
                    } else {
                        T returnObj = getObjectMapper()
                                .readValue(entity.getContent(), new TypeReference<T>(){});
                        return returnObj;
                    }

                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            }

        };

        T responseBody = httpClient.execute(httpGet, responseHandler);

        closeHttpClient(httpClient);
        return responseBody;
    }

    /**
     * Handler of HTTP PUT request
     * @param URL URL to connect
     * @param <T> JSON object type, default is LinkedHashMap
     * @return An object of type T, default is LinkedHashMap
     * @throws IOException
     */
    public static <T> T put(String URL) throws IOException {
        return put(URL, null, null, null);
    }

    /**
     * Handler of HTTP PUT request
     * @param URL URL to connect
     * @param queryParams queries to append to the URL
     * @param <T> JSON object type, default is LinkedHashMap
     * @return An object of type T, default is LinkedHashMap
     * @throws IOException
     */
    public static <T> T put(String URL, Map<String, String> queryParams)
            throws IOException {
        return post(URL, queryParams, null, null);
    }

    public static <T, M> T put(String URL, Map<String, String> queryParams, M requestBody)
            throws IOException {
        return post(URL, queryParams, requestBody, null);
    }
    /**
     * Handler of HTTP PUT request
     * @param URL URL to connect
     * @param queryParams queries to append to the URL
     * @param headers Additional headers, key-value pair
     * @param <T> JSON object type, default is LinkedHashMap
     * @return An object of type T, default is LinkedHashMap
     * @throws IOException
     */
    public static <T, M> T put(String URL, Map<String, String> queryParams,
                             M requestBody, Map<String, String > headers)  throws IOException {

        CloseableHttpClient httpClient = getHttpClient();
        HttpPut httpPut = new HttpPut(formUrl(URL, queryParams));
        addHeader(httpPut, headers);
        addCommonHeader(httpPut);
        ObjectMapper objectMapper = getObjectMapper();

        StringEntity jsonBody = new StringEntity(objectMapper.writeValueAsString(requestBody));
        ResponseHandler<T> responseHandler = new ResponseHandler<T>() {

            public T handleResponse(final HttpResponse response)
                    throws ClientProtocolException, IOException {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    if (entity == null) {
                        return null;
                    } else {
                        T returnObj = getObjectMapper()
                                .readValue(entity.getContent(), new TypeReference<T>(){});
                        return returnObj;
                    }

                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            }

        };

        T responseBody = httpClient.execute(httpPut, responseHandler);

        closeHttpClient(httpClient);
        return responseBody;
    }

    /**
     * Handler of HTTP POST request
     * @param URL URL to connect
     * @param <T> JSON object type, default is LinkedHashMap
     * @return An object of type T, default is LinkedHashMap
     * @throws IOException
     */
    public static <T> T post(String URL) throws IOException {
        return post(URL, null, null, null);
    }

    /**
     * Handler of HTTP POST request
     * @param URL URL to connect
     * @param queryParams queries to append to the URL
     * @param <T> JSON object type, default is LinkedHashMap
     * @return An object of type T, default is LinkedHashMap
     * @throws IOException
     */
    public static <T> T post(String URL, Map<String, String> queryParams)
            throws IOException {
        return post(URL, queryParams, null, null);
    }

    /**
     * Handler of HTTP POST request
     * @param URL URL to connect
     * @param queryParams queries to append to the URL
     * @param requestBody JSON body
     * @param <T> JSON object type, default is LinkedHashMap
     * @param <M> JSON object type, default is Object
     * @return
     * @throws IOException
     */
    public static <T, M> T post(String URL, Map<String, String> queryParams, M requestBody)
            throws IOException {
        return post(URL, queryParams, requestBody, null);
    }
    /**
     * Handler of HTTP POST request
     * @param URL URL to connect
     * @param queryParams queries to append to the URL
     * @param headers Additional headers, key-value pair
     * @param <T> JSON object type, default is LinkedHashMap
     * @param <M> JSON object type, default is Object
     * @return An object of type T, default is LinkedHashMap
     * @throws IOException
     */
    public static <T, M> T post(String URL, Map<String, String> queryParams,
                                M requestBody, Map<String, String > headers)  throws IOException {

        CloseableHttpClient httpClient = getHttpClient();
        HttpPost httpPost = new HttpPost(formUrl(URL, queryParams));
        addHeader(httpPost, headers);
        addCommonHeader(httpPost);
        ObjectMapper objectMapper = getObjectMapper();

        StringEntity jsonBody = new StringEntity(objectMapper.writeValueAsString(requestBody));
        ResponseHandler<T> responseHandler = new ResponseHandler<T>() {

            public T handleResponse(final HttpResponse response)
                    throws ClientProtocolException, IOException {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    if (entity == null) {
                        return null;
                    } else {
                        T returnObj = getObjectMapper()
                                .readValue(entity.getContent(), new TypeReference<T>(){});
                        return returnObj;
                    }

                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            }

        };

        T responseBody = httpClient.execute(httpPost, responseHandler);

        closeHttpClient(httpClient);
        return responseBody;
    }


    private static void addCommonHeader(HttpRequestBase httpRequest) {
        Map<String, String> commonHeaders = new HashMap<String, String>();
        commonHeaders.put("Content-Type", "application/json");
        commonHeaders.put("Accept", "application/json");
        addHeader(httpRequest, commonHeaders);
    }

    private static void addHeader(HttpRequestBase httpRequest, Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> header: headers.entrySet()) {
            httpRequest.addHeader(header.getKey(), header.getValue());
        }
    }

    private static String formUrl(String URL, Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return URL;
        }

        StringBuilder urlBuilder = new StringBuilder(URL).append("?");
        for (Map.Entry<String, String> param: queryParams.entrySet()) {
            urlBuilder.append(param.getKey())
                    .append("=")
                    .append(param.getValue())
                    .append("&");
        }

        return urlBuilder.substring(0, urlBuilder.length() - 1);
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    protected static CloseableHttpClient getHttpClient() {
        final CloseableHttpClient httpClient = HttpClients.createDefault();
        return httpClient;
    }

    private static void closeHttpClient(CloseableHttpClient httpClient) {
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (Exception e) {
            // do nothing
        }
    }


    protected static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
