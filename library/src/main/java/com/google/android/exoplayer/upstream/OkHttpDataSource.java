package com.google.android.exoplayer.upstream;

import android.text.TextUtils;
import android.util.Log;

import com.google.android.exoplayer.C;
import com.google.android.exoplayer.util.Assertions;
import com.google.android.exoplayer.util.Predicate;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.internal.huc.HttpURLConnectionImpl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Dani on 2015.03.31..
 */
public class OkHttpDataSource implements DataSource {

    private final String userAgent;
    private final TransferListener listener;
    private HttpURLConnectionImpl connection;
    private InputStream inputStream;
    private long dataLength;
    private boolean opened;
    private long bytesRead;
    private DataSpec dataSpec;
    private final HashMap<String, String> requestProperties;
    private static final Pattern CONTENT_RANGE_HEADER =
            Pattern.compile("^bytes (\\d+)-(\\d+)/(\\d+)$");
    OkHttpClient client2;
    Response response;
    Headers responseHeaders;

    public OkHttpDataSource(String userAgent, Predicate<String> contentTypePredicate,
                            TransferListener listener){
        this.userAgent = Assertions.checkNotEmpty(userAgent);
        this.listener = listener;
        this.requestProperties = new HashMap<String, String>();
    }

    /**
     * Opens the {@link com.google.android.exoplayer.upstream.DataSource} to read the specified data. Calls to {@link #open(DataSpec)} and
     * {@link #close()} must be balanced.
     * <p/>
     * Note: If {@link #open(DataSpec)} throws an {@link java.io.IOException}, callers must still call
     * {@link #close()} to ensure that any partial effects of the {@link #open(DataSpec)} invocation
     * are cleaned up. Implementations of this class can assume that callers will call
     * {@link #close()} in this case.
     *
     * @param dataSpec Defines the data to be read.
     * @return The number of bytes that can be read from the opened source. For unbounded requests
     * (i.e. requests where {@link DataSpec#length} equals {C # LENGTH_UNBOUNDED}) this value
     * is the resolved length of the request, or { C # LENGTH_UNBOUNDED} if the length is still
     * unresolved. For all other requests, the value returned will be equal to the request's
     * {@link DataSpec#length}.
     * @throws java.io.IOException If an error occurs opening the source.
     */
    @Override
    public long open(DataSpec dataSpec) throws IOException {
        dataLength=0;
        bytesRead = 0;

        client2 = new OkHttpClient();
        Request request = new Request.Builder()
                .url(dataSpec.uri.toString())
                .addHeader("Range", buildRangeHeader(dataSpec))
                .get()
                .build();

        response = client2.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        responseHeaders = response.headers();
        for (int i = 0; i < responseHeaders.size(); i++) {
            System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
        }
        dataLength=Long.parseLong(responseHeaders.get("Content-Length"));
        inputStream = response.body().byteStream();

        /*
        try {
            //Establish connection
            URL url = new URL(dataSpec.uri.toString());
            OkHttpClient client = new OkHttpClient();
            client.setAuthenticator(new Authenticator() {
                @Override
                public Request authenticate(Proxy proxy, Response response) throws IOException {
                    String credential = Credentials.basic("scott", "tiger");
                    return response.request().newBuilder().header("Authorization", credential).build();
                }

                @Override
                public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                    return null;
                }
            });
            ArrayList<Protocol> l = new ArrayList();
            l.add(Protocol.HTTP_1_1);
            client.setProtocols(l);
            client.setProxySelector(new ProxySelector() {
                @Override
                public List<Proxy> select(URI uri) {
                    Proxy p = new Proxy();
                    return null;
                }

                @Override
                public void connectFailed(URI uri, SocketAddress address, IOException failure) {

                }
            });
            connection = new HttpURLConnectionImpl(url,client);
            //connection = url.openConnection();
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.setDoOutput(false);
            connection.setRequestProperty("Accept-Encoding", "deflate");
            connection.setRequestProperty("User-Agent", userAgent);
            connection.setRequestProperty("Range", buildRangeHeader(dataSpec));
            connection.setDoOutput(false);
            synchronized (requestProperties) {
                for (Map.Entry<String, String> property : requestProperties.entrySet()) {
                    connection.setRequestProperty(property.getKey(), property.getValue());
                }
            }
            connection.connect();
            long contentLength = getContentLength(connection);
            Log.d("","contentlength: "+contentLength);
            dataLength = dataSpec.length == C.LENGTH_UNBOUNDED ? contentLength : dataSpec.length;
            //Content length check
            if (dataSpec.length != C.LENGTH_UNBOUNDED && contentLength != C.LENGTH_UNBOUNDED
                    && contentLength != dataSpec.length) {
                // The DataSpec specified a length and we resolved a length from the response headers, but
                // the two lengths do not match.
                if (connection != null) {
                    connection.disconnect();
                    connection = null;
                }
                throw new Exception();
            }

            inputStream = connection.getInputStream();*/
            opened = true;
            if (listener != null) {
                listener.onTransferStart();
            }
/*

        }catch (Exception e){
            e.printStackTrace();
        }
        Log.d("","dataLength:"+dataLength);*/
        return dataLength;
    }

    private String buildRangeHeader(DataSpec dataSpec) {
        String rangeRequest = "bytes=" + dataSpec.position + "-";
        if (dataSpec.length != C.LENGTH_UNBOUNDED) {
            rangeRequest += (dataSpec.position + dataSpec.length - 1);
        }
        return rangeRequest;
    }

    /**
     * Sets the value of a request header field. The value will be used for subsequent connections
     * established by the source.
     *
     * @param name The name of the header field.
     * @param value The value of the field.
     */
    public void setRequestProperty(String name, String value) {
        Assertions.checkNotNull(name);
        Assertions.checkNotNull(value);
        synchronized (requestProperties) {
            requestProperties.put(name, value);
        }
    }

    private long getContentLength(HttpURLConnection connection) {
        long contentLength = C.LENGTH_UNBOUNDED;
        String contentLengthHeader = connection.getHeaderField("Content-Length");
        if (!TextUtils.isEmpty(contentLengthHeader)) {
            try {
                contentLength = Long.parseLong(contentLengthHeader);
            } catch (NumberFormatException e) {
                Log.e("", "Unexpected Content-Length [" + contentLengthHeader + "]");
            }
        }
        String contentRangeHeader = connection.getHeaderField("Content-Range");
        if (!TextUtils.isEmpty(contentRangeHeader)) {
            Matcher matcher = CONTENT_RANGE_HEADER.matcher(contentRangeHeader);
            if (matcher.find()) {
                try {
                    long contentLengthFromRange =
                            Long.parseLong(matcher.group(2)) - Long.parseLong(matcher.group(1)) + 1;
                    if (contentLength < 0) {
                        // Some proxy servers strip the Content-Length header. Fall back to the length
                        // calculated here in this case.
                        contentLength = contentLengthFromRange;
                    } else if (contentLength != contentLengthFromRange) {
                        // If there is a discrepancy between the Content-Length and Content-Range headers,
                        // assume the one with the larger value is correct. We have seen cases where carrier
                        // change one of them to reduce the size of a request, but it is unlikely anybody would
                        // increase it.
                        Log.w("", "Inconsistent headers [" + contentLengthHeader + "] [" + contentRangeHeader +
                                "]");
                        contentLength = Math.max(contentLength, contentLengthFromRange);
                    }
                } catch (NumberFormatException e) {
                    Log.e("", "Unexpected Content-Range [" + contentRangeHeader + "]");
                }
            }
        }
        return contentLength;
    }

    /**
     * Closes the {@link com.google.android.exoplayer.upstream.DataSource}.
     * <p/>
     * Note: This method will be called even if the corresponding call to {@link #open(com.google.android.exoplayer.upstream.DataSpec)}
     * threw an {@link java.io.IOException}. See {@link #open(com.google.android.exoplayer.upstream.DataSpec)} for more details.
     *
     * @throws java.io.IOException If an error occurs closing the source.
     */
    @Override
    public void close() throws IOException {
        try {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {

                }
                inputStream = null;
            }
        } finally {
            if (opened) {
                opened = false;
                if (listener != null) {
                    listener.onTransferEnd();
                }
                if (connection != null) {
                    connection.disconnect();
                    connection = null;
                }
            }
        }
    }

    /**
     * Reads up to {@code length} bytes of data and stores them into {@code buffer}, starting at
     * index {@code offset}. This method blocks until at least one byte of data can be read, the end
     * of the opened range is detected, or an exception is thrown.
     *
     * @param buffer     The buffer into which the read data should be stored.
     * @param offset     The start offset into {@code buffer} at which data should be written.
     * @param readLength The maximum number of bytes to read.
     * @return The actual number of bytes read, or -1 if the end of the opened range is reached.
     * @throws java.io.IOException If an error occurs reading from the source.
     */
    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        int read = 0;
        try {
            read = inputStream.read(buffer, offset, readLength);
        } catch (IOException e) {

        }

        if (read > 0) {
            bytesRead += read;
            if (listener != null) {
                listener.onBytesTransferred(read);
            }
        } else if (dataLength != C.LENGTH_UNBOUNDED && dataLength != bytesRead) {
            // Check for cases where the server closed the connection having not sent the correct amount
            // of data. We can only do this if we know the length of the data we were expecting.

        }

        return read;
    }
}
