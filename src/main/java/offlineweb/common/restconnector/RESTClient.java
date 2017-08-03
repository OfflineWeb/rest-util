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
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple REST client
 * @author papa
 *         created on 7/24/17.
 */
@Loggable
public class RESTClient {

    /**
     * Handler of HTTP HEAD request
     * @param URL URL to connect
     * @param pathParams a list of path parameters
     * @return header key-value, Map<String, String>
     * @throws IOException
     */
    public static Map<String, Object> head(String URL, List<String> pathParams,
                                           Map<String, String > queryParams) throws IOException {
        CloseableHttpClient httpClient = getHttpClient();
        HttpHead httpHead = new HttpHead(formUrl(URL, pathParams, queryParams));
        addCommonHeader(httpHead);

        ResponseHandler<Map<String, Object>> responseHandler
                = new ResponseHandler<Map<String, Object>>() {
            public Map<String, Object> handleResponse(HttpResponse httpResponse)
                    throws ClientProtocolException {
                Map<String, Object> headerMap = new HashMap<String, Object>();
                for (Header header : httpResponse.getAllHeaders()) {
                    headerMap.put(header.getName(), header.getValue());
                }
                headerMap.put("status", httpResponse.getStatusLine().getStatusCode());
                headerMap.put("msg", httpResponse.getStatusLine().getReasonPhrase());

                return headerMap;
            }
        };
        Map<String, Object> headerMap = httpClient.execute(httpHead, responseHandler);
        closeHttpClient(httpClient);
        return headerMap;
    }

    /**
     * Handler of HTTP GET request
     * @param URL URL to connect
     * @param <T> JSON object type, default is LinkedHashMap
     * @return An object of type T, default is LinkedHashMap
     * @throws IOException
     */
    public static <T> T get(String URL) throws IOException {
        return get(URL, null, null, null);
    }

    /**
     * Handler of HTTP GET request
     * @param URL URL to connect
     * @param pathParams a list of path parameters
     * @param <T> JSON object type, default is LinkedHashMap
     * @return An object of type T, default is LinkedHashMap
     * @throws IOException
     */
    public static <T> T get(String URL, List<String> pathParams) throws IOException {
        return get(URL, pathParams, null, null);
    }

    /**
     * Handler of HTTP GET request
     * @param URL URL to connect
     * @param pathParams a list of path parameters
     * @param queryParams queries to append to the URL
     * @param <T> JSON object type, default is LinkedHashMap
     * @return An object of type T, default is LinkedHashMap
     * @throws IOException
     */
    public static <T> T get(String URL, List<String> pathParams,
                            Map<String, String> queryParams)
            throws IOException {
        return get(URL, pathParams, queryParams, null);
    }

    /**
     * Handler of HTTP GET request
     * @param URL URL to connect
     * @param pathParams a list of path parameters
     * @param queryParams queries to append to the URL
     * @param headers Additional headers, key-value pair
     * @param <T> JSON object type, default is LinkedHashMap
     * @return An object of type T, default is LinkedHashMap
     * @throws IOException
     */
    public static <T> T get(String URL, List<String> pathParams,
                            Map<String, String> queryParams,
                            Map<String, String > headers)  throws IOException {

        CloseableHttpClient httpClient = getHttpClient();
        HttpGet httpGet = new HttpGet(formUrl(URL, pathParams, queryParams));
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
                    throw new RESTConnectorException(response.getStatusLine());
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
     * @param pathParams a list of path parameters
     * @param <T> JSON object type, default is LinkedHashMap
     * @return An object of type T, default is LinkedHashMap
     * @throws IOException
     */
    public static <T> T put(String URL, List<String> pathParams) throws IOException {
        return put(URL, pathParams, null, null, null);
    }

    /**
     * Handler of HTTP PUT request
     * @param URL URL to connect
     * @param pathParams a list of path parameters
     * @param queryParams queries to append to the URL
     * @param <T> JSON object type, default is LinkedHashMap
     * @return An object of type T, default is LinkedHashMap
     * @throws IOException
     */
    public static <T> T put(String URL, List<String> pathParams,
                            Map<String, String> queryParams)
            throws IOException {
        return put(URL, pathParams, queryParams, null, null);
    }

    public static <T, M> T put(String URL, List<String> pathParams,
                               Map<String, String> queryParams, M requestBody)
            throws IOException {
        return put(URL, pathParams, queryParams, requestBody, null);
    }
    /**
     * Handler of HTTP PUT request
     * @param URL URL to connect
     * @param pathParams a list of path parameters
     * @param queryParams queries to append to the URL
     * @param headers Additional headers, key-value pair
     * @param <T> JSON object type, default is LinkedHashMap
     * @return An object of type T, default is LinkedHashMap
     * @throws IOException
     */
    public static <T, M> T put(String URL, List<String> pathParams,
                               Map<String, String> queryParams,
                             M requestBody, Map<String, String > headers)  throws IOException {

        CloseableHttpClient httpClient = getHttpClient();
        HttpPut httpPut = new HttpPut(formUrl(URL, pathParams, queryParams));
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
                    throw new RESTConnectorException(response.getStatusLine());
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
        return post(URL, null, null, null, null);
    }

    /**
     * Handler of HTTP POST request
     * @param URL URL to connect
     * @param pathParams a list of path parameters
     * @param <T> JSON object type, default is LinkedHashMap
     * @return An object of type T, default is LinkedHashMap
     * @throws IOException
     */
    public static <T> T post(String URL, List<String> pathParams)
            throws IOException {
        return post(URL, pathParams, null, null, null);
    }

    /**
     * Handler of HTTP POST request
     * @param URL URL to connect
     * @param pathParams a list of path parameters
     * @param queryParams queries to append to the URL
     * @param <T> JSON object type, default is LinkedHashMap
     * @return An object of type T, default is LinkedHashMap
     * @throws IOException
     */
    public static <T> T post(String URL, List<String> pathParams, Map<String, String> queryParams)
            throws IOException {
        return post(URL, pathParams, queryParams, null, null);
    }

    /**
     * Handler of HTTP POST request
     * @param URL URL to connect
     * @param pathParams a list of path parameters
     * @param queryParams queries to append to the URL
     * @param requestBody JSON body
     * @param <T> JSON object type, default is LinkedHashMap
     * @param <M> JSON object type, default is Object
     * @return
     * @throws IOException
     */
    public static <T, M> T post(String URL, List<String> pathParams,
                                Map<String, String> queryParams, M requestBody)
            throws IOException {
        return post(URL, pathParams, queryParams, requestBody, null);
    }
    /**
     * Handler of HTTP POST request
     * @param URL URL to connect
     * @param pathParams a list of path parameters
     * @param queryParams queries to append to the URL
     * @param headers Additional headers, key-value pair
     * @param <T> JSON object type, default is LinkedHashMap
     * @param <M> JSON object type, default is Object
     * @return An object of type T, default is LinkedHashMap
     * @throws IOException
     */
    public static <T, M> T post(String URL, List<String> pathParams,
                                Map<String, String> queryParams,
                                M requestBody, Map<String, String > headers)  throws IOException {

        CloseableHttpClient httpClient = getHttpClient();
        HttpPost httpPost = new HttpPost(formUrl(URL, pathParams, queryParams));
        addHeader(httpPost, headers);
        addCommonHeader(httpPost);

        StringEntity jsonBody = new StringEntity(getObjectMapper()
                .writeValueAsString(requestBody));
        httpPost.setEntity(jsonBody);
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
                    throw new RESTConnectorException(response.getStatusLine());
                }
            }

        };

        T responseBody = httpClient.execute(httpPost, responseHandler);

        closeHttpClient(httpClient);
        return responseBody;
    }

    /**
     * Handler of HTTP DELETE request
     * @param URL URL to connect
     * @param <T> JSON object type, default is LinkedHashMap
     * @return An object of type T, default is LinkedHashMap
     * @throws IOException
     */
    public static <T> T delete(String URL) throws IOException {
        return delete(URL, null, null, null, null);
    }

    /**
     * Handler of HTTP DELETE request
     * @param URL URL to connect
     * @param pathParams a list of path parameters
     * @param <T> JSON object type, default is LinkedHashMap
     * @return An object of type T, default is LinkedHashMap
     * @throws IOException
     */
    public static <T> T delete(String URL, List<String> pathParams)
            throws IOException {
        return delete(URL, pathParams, null, null, null);
    }

    /**
     * Handler of HTTP DELETE request
     * @param URL URL to connect
     * @param pathParams a list of path parameters
     * @param queryParams queries to append to the URL
     * @param <T> JSON object type, default is LinkedHashMap
     * @return An object of type T, default is LinkedHashMap
     * @throws IOException
     */
    public static <T> T delete(String URL, List<String> pathParams, Map<String, String> queryParams)
            throws IOException {
        return delete(URL, pathParams, queryParams, null, null);
    }

    /**
     * Handler of HTTP DELETE request
     * @param URL URL to connect
     * @param pathParams a list of path parameters
     * @param queryParams queries to append to the URL
     * @param requestBody JSON body
     * @param <T> JSON object type, default is LinkedHashMap
     * @param <M> JSON object type, default is Object
     * @return
     * @throws IOException
     */
    public static <T, M> T delete(String URL, List<String> pathParams,
                                Map<String, String> queryParams, M requestBody)
            throws IOException {
        return delete(URL, pathParams, queryParams, requestBody, null);
    }

    /**
     * Handler of HTTP DELETE request
     * @param URL URL to connect
     * @param pathParams a list of path parameters
     * @param queryParams queries to append to the URL
     * @param headers Additional headers, key-value pair
     * @param <T> JSON object type, default is LinkedHashMap
     * @return An object of type T, default is LinkedHashMap
     * @throws IOException
     */
    public static <T, M> T delete(String URL, List<String> pathParams,
                               Map<String, String> queryParams,
                               M requestBody, Map<String, String > headers)  throws IOException {

        CloseableHttpClient httpClient = getHttpClient();
        HttpDelete httpDelete = new HttpDelete(formUrl(URL, pathParams, queryParams));
        addHeader(httpDelete, headers);
        addCommonHeader(httpDelete);
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
                    throw new RESTConnectorException(response.getStatusLine());
                }
            }

        };

        T responseBody = httpClient.execute(httpDelete, responseHandler);

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
            httpRequest.setHeader(header.getKey(), header.getValue());
        }
    }

    private static String formUrl(String URL, List<String> pathParams,
                                  Map<String, String> queryParams) {
        if ((pathParams == null || pathParams.isEmpty())
                && (queryParams == null || queryParams.isEmpty())) {
            return URL;
        }

        StringBuilder urlBuilder = new StringBuilder(URL);
        if (pathParams != null && !pathParams.isEmpty()) {
            urlBuilder.append("/");
            for (String param : pathParams) {
                urlBuilder.append(param).append("/");
            }
        }

        urlBuilder = urlBuilder.deleteCharAt(urlBuilder.length() - 1);

        if (queryParams != null && !queryParams.isEmpty()) {
            urlBuilder = urlBuilder.append("?");
            for (Map.Entry<String, String> param: queryParams.entrySet()) {
                urlBuilder.append(param.getKey())
                        .append("=")
                        .append(param.getValue())
                        .append("&");
            }
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
