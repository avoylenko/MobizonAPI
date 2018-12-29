package ua.mobizon;

import okhttp3.*;
import ua.mobizon.exception.MobizonException;
import ua.mobizon.utils.OkHttpUnsecureBuilder;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MobizonApi {

    /**
     * String HTTP(S) API server address. api.mobizon.com is deprecated and will be disabled soon.
     * Default value will be removed soon. Only OLD keys will be accepted by this endpoint till it's final shutdown.
     * API domain depends on user site of registration and could be found in 'API connection setup guide'.
     */
    private String apiServer = "api.mobizon.com";

    /**
     * API key - copy it from your Mobizon account
     */
    private String apiKey;

    /**
     * Force use HTTP connection instead of HTTPS. Not recommended, but if your system does not support secure connections,
     * then you don't have any other choice.
     */
    private boolean forceHTTP = false;

    /**
     * Set true to force client bypass SSL certificate checks. Changing this option is not recommended,
     * but you could use it in case, if some temporary problems with certificate transmission takes place.
     * If forceHTTP is true, then this option will be ignored
     */
    private boolean skipVerifySSL = false;

    /**
     * API version - don't change it if you are not sure
     */
    private String apiVersion = "v1";

    /**
     * API response timeout in seconds
     */
    private int timeout = 30;

    /**
     * Default API response format - possible formats see in allowedFormats
     */
    private String format = "json";

    /**
     * Possible API response formats
     */
    private static final String[] allowedFormats = {"xml", "json"};

    /**
     * Allowed SMS message params
     */
    private static final String[] allowedSMSMessageParams = {"name", "deferredToTs", "mclass", "validity"};


    /**
     * @param apiKey    User API key. API key should be passed either as first string param or as apiKey in params.
     * @param apiServer User API server depends on user initial registration site. Correct API domain could be found in 'API connection setup guide'
     * @throws IllegalArgumentException
     */
    public MobizonApi(String apiKey, String apiServer) {
        if (!apiKey.matches("[a-z0-9]{40}|[a-z0-9]{70}")) {
            throw new IllegalArgumentException("Incorrect api key");
        }
        this.apiKey = apiKey;

        if (!apiServer.matches("[a-z0-9][-a-z0-9]+(?:.[a-z0-9][-a-z0-9]*)+")) {
            throw new IllegalArgumentException("Incorrect api server");
        }
        this.apiServer = apiServer;
    }

    /**
     * @param apiKey        User API key. API key should be passed either as first string param or as apiKey in params.
     * @param apiServer     User API server depends on user initial registration site. Correct API domain could be found in 'API connection setup guide'
     * @param format        API response format. Available formats: xml|json. Default: json.
     * @param timeout       API response timeout in seconds. Default: 30.
     * @param apiVersion    API version. Default: v1.
     * @param skipVerifySSL Flag to disable SSL verification procedure during handshake with API server. Default: false (verification should be passed). Omitting if forceHTTP=true
     * @param forceHTTP     Flag to forcibly disable SSL connection. Default: false (means all API requests will be made over HTTPS).
     * @throws IllegalArgumentException
     */
    public MobizonApi(String apiKey, String apiServer, String format, int timeout, String apiVersion, boolean skipVerifySSL, boolean forceHTTP) {
        this(apiKey, apiServer);

        if (timeout < 0) {
            throw new IllegalArgumentException("Timeout can not be less than 0");
        }
        this.timeout = timeout;

        if (!apiVersion.substring(0, 1).equals("v") || Character.getNumericValue(apiVersion.charAt(apiVersion.length() - 1)) < 1) {
            throw new IllegalArgumentException("Incorrect api version");
        }
        this.apiVersion = apiVersion;
        if (!Arrays.asList(allowedFormats).contains(format)) {
            throw new IllegalArgumentException("Incorrect api response format");
        }
        this.format = format;
        if (!forceHTTP) {
            this.skipVerifySSL = skipVerifySSL;
        }
        this.forceHTTP = forceHTTP;
    }

    /**
     * @throws MobizonException
     */
    public String getOwnBalance() throws MobizonException {
        try {
            return sendAndGetAPIResponse(getAPIURL("service/user/getownbalance").toString());
        } catch (Exception e) {
            throw new MobizonException(e);
        }
    }

    /**
     * @throws MobizonException
     */
    public String getAllSMSMessages() throws MobizonException {
        try {
            return sendAndGetAPIResponse(getAPIURL("service/message/list").toString());
        } catch (Exception e) {
            throw new MobizonException(e);
        }
    }

    /**
     * @param txtMessage  Text message to recipient.
     * @param phoneNumber Phone number of recipient
     * @throws MobizonException
     */
    public String sendSMSMessage(String txtMessage, String phoneNumber) throws MobizonException {
        URL sendSMSMessageURL = getAPIURL("service/message/sendsmsmessage");
        RequestBody formBody = null;
        try {
            formBody = new FormBody.Builder()
                    .add("recipient", getValidPhoneNumber(phoneNumber))
                    .add("text", URLEncoder.encode(txtMessage, "UTF-8"))
                    .build();
            return sendAndGetAPIResponse(sendSMSMessageURL.toString(), formBody);
        } catch (Exception e) {
            throw new MobizonException(e);
        }
    }

    /**
     * @param txtMessage  Text message to recipient.
     * @param phoneNumber Phone number of recipient
     * @param params Extra SMS params
     * @throws MobizonException
     */
    public String sendSMSMessage(String txtMessage, String phoneNumber, HashMap<String, String> params) throws MobizonException {
        URL sendSMSMessageURL = getAPIURL("service/message/sendsmsmessage");
        FormBody.Builder formBuilder = new FormBody.Builder();
        try {
            formBuilder.add("text", URLEncoder.encode(txtMessage, "UTF-8"));
            formBuilder.add("recipient", getValidPhoneNumber(phoneNumber));
            Iterator it = params.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if (Arrays.asList(allowedSMSMessageParams).contains(pair.getKey())) {
                    formBuilder.add("params[" + pair.getKey() + "]", pair.getValue().toString());
                }
            }
            RequestBody formBody = formBuilder.build();
            return sendAndGetAPIResponse(sendSMSMessageURL.toString(), formBody);
        } catch (Exception e) {
            throw new MobizonException(e);
        }
    }

    /**
     * @param txtMessage  Text message to recipient
     * @param phoneNumber Phone number of recipient
     * @param alphaname   Registered alphaname
     * @throws MobizonException
     */
    public String sendSMSMessage(String txtMessage, String phoneNumber, String alphaname) throws MobizonException {
        URL sendSMSMessageURL = getAPIURL("service/message/sendsmsmessage");
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("recipient", getValidPhoneNumber(phoneNumber))
                    .add("from", alphaname)
                    .add("text", URLEncoder.encode(txtMessage, "UTF-8"))
                    .build();
            return sendAndGetAPIResponse(sendSMSMessageURL.toString(), formBody);
        } catch (Exception e) {
            throw new MobizonException(e);
        }
    }

    /**
     * @param messageId SMS message id
     * @throws MobizonException
     */
    public String getSMSStatus(int messageId) throws MobizonException {
        if (messageId <= 0) {
            throw new IllegalArgumentException("Incorrect messageId number");
        }
        URL getSMSStatusURL = getAPIURL("service/message/getSMSStatus");
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("ids", String.valueOf(messageId))
                    .build();
            return sendAndGetAPIResponse(getSMSStatusURL.toString(), formBody);
        } catch (Exception e) {
            throw new MobizonException(e);
        }
    }

    public String getSMSStatus(int[] messageId) throws MobizonException {
        if (messageId.length == 0 || messageId.length > 100) {
            throw new IllegalArgumentException("Incorrect size of messageId array");
        }
        URL getSMSStatusURL = getAPIURL("service/message/getSMSStatus");
        FormBody.Builder formBuilder = new FormBody.Builder();
        StringBuilder sb = new StringBuilder();
        for (int id : messageId) {
            if (sb.length() > 0) sb.append(',');
            sb.append(String.valueOf(id));
        }
        formBuilder.add("ids", sb.toString());
        RequestBody formBody = formBuilder.build();
        try {
            return sendAndGetAPIResponse(getSMSStatusURL.toString(), formBody);
        } catch (Exception e) {
            throw new MobizonException(e);
        }
    }

    private String sendAndGetAPIResponse(String url) throws KeyManagementException, NoSuchAlgorithmException, IOException {
        OkHttpClient okHttpClient = getOkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Response response = okHttpClient.newCall(request).execute();
        return response.body().string();
    }

    private String sendAndGetAPIResponse(String url, RequestBody requestBody) throws KeyManagementException, NoSuchAlgorithmException, IOException {
        OkHttpClient okHttpClient = getOkHttpClient();
        Request request = new Request.Builder().url(url).post(requestBody).build();
        Response response = okHttpClient.newCall(request).execute();
        return response.body().string();
    }

    private URL getAPIURL(String pathSegments) {
        return new HttpUrl.Builder()
                .scheme(getUrlScheme())
                .host(apiServer)
                .addPathSegments(pathSegments)
                .addQueryParameter("output", format)
                .addQueryParameter("api", apiVersion)
                .addQueryParameter("apiKey", apiKey)
                .build().url();
    }

    private String getValidPhoneNumber(String number) {
        if (number.startsWith("+")) {
            number = number.substring(1);
        }
        return number;
    }

    private String getUrlScheme() {
        if (forceHTTP) {
            return "http";
        }
        return "https";
    }

    private OkHttpClient getOkHttpClient() throws NoSuchAlgorithmException, KeyManagementException {
        OkHttpClient okHttpClient = null;
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(timeout, TimeUnit.SECONDS);
        if (skipVerifySSL) {
            builder = OkHttpUnsecureBuilder.configureToIgnoreCertificate(builder);
        }
        okHttpClient = builder.build();
        return okHttpClient;
    }

}

