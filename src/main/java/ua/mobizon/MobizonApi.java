package ua.mobizon;

import okhttp3.*;
import ua.mobizon.exception.MobizonException;
import ua.mobizon.params.CreateCampaignParams;
import ua.mobizon.params.CreateLinkParams;
import ua.mobizon.params.SMSMessageParams;
import ua.mobizon.utils.OkHttpUnsecureBuilder;

import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
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
     * @throws MobizonException
     */
    public String getSMSMessageById(int messageId) throws MobizonException {
        URL getSMSMessageURL = getAPIURL("service/message/list");
        RequestBody formBody = null;
        try {
            formBody = new FormBody.Builder()
                    .add("criteria[id]", String.valueOf(messageId))
                    .add("withNumberInfo", "1")
                    .build();
            return sendAndGetAPIResponse(getSMSMessageURL.toString(), formBody);
        } catch (Exception e) {
            throw new MobizonException(e);
        }
    }

    /**
     * @throws MobizonException
     */
    public String getSMSMessagesByCampaignId(int campaignId) throws MobizonException {
        URL getSMSMessageURL = getAPIURL("service/message/list");
        RequestBody formBody = null;
        try {
            formBody = new FormBody.Builder()
                    .add("criteria[campaignId]", String.valueOf(campaignId))
                    .add("withNumberInfo", "1")
                    .build();
            return sendAndGetAPIResponse(getSMSMessageURL.toString(), formBody);
        } catch (Exception e) {
            throw new MobizonException(e);
        }
    }

    /**
     * @throws MobizonException
     */
    public String getSMSMessagesByPhoneNumber(long phoneNumber) throws MobizonException {
        URL getSMSMessageURL = getAPIURL("service/message/list");
        RequestBody formBody = null;
        try {
            formBody = new FormBody.Builder()
                    .add("criteria[to]", String.valueOf(phoneNumber))
                    .add("withNumberInfo", "1")
                    .build();
            return sendAndGetAPIResponse(getSMSMessageURL.toString(), formBody);
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
        return this.sendSMSMessage(txtMessage, phoneNumber, new SMSMessageParams());
    }

    /**
     * @param txtMessage  Text message to recipient.
     * @param phoneNumber Phone number of recipient
     * @param params      Extra SMS params
     * @throws MobizonException
     */
    public String sendSMSMessage(String txtMessage, String phoneNumber, SMSMessageParams params) throws MobizonException {
        URL sendSMSMessageURL = getAPIURL("service/message/sendsmsmessage");
        FormBody.Builder formBuilder = new FormBody.Builder();
        try {
            formBuilder.add("text", txtMessage);
            formBuilder.add("recipient", getValidPhoneNumber(phoneNumber));
            if (!(params.getName().isEmpty())) {
                formBuilder.add("params[name]", params.getName());
            }
            if (!(params.getDeferredToTs().isEmpty())) {
                formBuilder.add("params[deferredToTs]", params.getDeferredToTs());
            }
            formBuilder.add("params[mclass]", String.valueOf(params.getMclass()));
            formBuilder.add("params[validity]", String.valueOf(params.getValidity()));
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
     * @param params Extra SMS params
     * @throws MobizonException
     */
    public String sendSMSMessage(String txtMessage, String phoneNumber, String alphaname, SMSMessageParams params) throws MobizonException {
        URL sendSMSMessageURL = getAPIURL("service/message/sendsmsmessage");
        FormBody.Builder formBuilder = new FormBody.Builder();
        try {
            formBuilder.add("recipient", getValidPhoneNumber(phoneNumber));
            formBuilder.add("from", alphaname);
            formBuilder.add("text", txtMessage);
            if (!(params.getName().isEmpty())) {
                formBuilder.add("params[name]", params.getName());
            }
            if (!(params.getDeferredToTs().isEmpty())) {
                formBuilder.add("params[deferredToTs]", params.getDeferredToTs());
            }
            formBuilder.add("params[mclass]", String.valueOf(params.getMclass()));
            formBuilder.add("params[validity]", String.valueOf(params.getValidity()));
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
        return this.sendSMSMessage(txtMessage, phoneNumber, alphaname, new SMSMessageParams());
    }

    /**
     * @param messageId SMS message id
     * @throws MobizonException
     */
    public String getSMSStatus(int messageId) throws MobizonException {
        int[] message = {messageId};
        return this.getSMSStatus(message);
    }

    public String getSMSStatus(int[] messageId) throws MobizonException {
        if (messageId.length == 0 || messageId.length > 100) {
            throw new IllegalArgumentException("Incorrect size of messageId array");
        }
        URL getSMSStatusURL = getAPIURL("service/message/getsmsstatus");
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

    public String createCampaign(CreateCampaignParams params) throws MobizonException {
        URL createCampaignURL = getAPIURL("service/campaign/create");
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("data[name]", params.getName());
        formBuilder.add("data[text]", params.getText());
        formBuilder.add("data[type]", String.valueOf(params.getType()));
        if (!params.getFrom().isEmpty()) formBuilder.add("data[from]", params.getFrom());
        if (params.getRateLimit() != 0) formBuilder.add("data[rateLimit]", String.valueOf(params.getRateLimit()));
        if (params.getRatePeriod() != 0) formBuilder.add("data[ratePeriod]", String.valueOf(params.getRatePeriod()));
        if (!params.getDeferredToTs().isEmpty()) formBuilder.add("data[deferredToTs]", params.getDeferredToTs());
        // does not work
        //formBuilder.add("data[mclass]", String.valueOf(params.getMclass()));
        if (params.getTtl() != 0) formBuilder.add("data[ttl]", String.valueOf(params.getTtl()));
        // does not work either
        //formBuilder.add("data[trackShortLinkRecipients]", String.valueOf(params.getTrackShortLinkRecipients()));
        RequestBody formBody = formBuilder.build();
        try {
            return sendAndGetAPIResponse(createCampaignURL.toString(), formBody);
        } catch (Exception e) {
            throw new MobizonException(e);
        }
    }

    public String addCampaignRecipient(int campaignId, long phoneNumber) throws MobizonException {
        URL deleteCampaignURL = getAPIURL("service/campaign/addrecipients");
        RequestBody formBody = new FormBody.Builder()
                .add("id", String.valueOf(campaignId))
                .add("recipients", String.valueOf(phoneNumber))
                .build();
        try {
            return sendAndGetAPIResponse(deleteCampaignURL.toString(), formBody);
        } catch (Exception e) {
            throw new MobizonException(e);
        }
    }

    public String addCampaignRecipients(int campaignId, long[] phoneNumbers) throws MobizonException {
        if (phoneNumbers.length > 500) {
            throw new IllegalArgumentException("Too many phone numbers");
        }
        URL deleteCampaignURL = getAPIURL("service/campaign/addrecipients");
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("id", String.valueOf(campaignId));
        StringBuilder sb = new StringBuilder();
        for (long number : phoneNumbers) {
            if (sb.length() > 0) sb.append(',');
            sb.append(String.valueOf(number));
        }
        formBuilder.add("recipients", sb.toString());
        RequestBody formBody = formBuilder.build();
        try {
            return sendAndGetAPIResponse(deleteCampaignURL.toString(), formBody);
        } catch (Exception e) {
            throw new MobizonException(e);
        }
    }

    public String deleteCampaign(int campaignId) throws MobizonException {
        URL deleteCampaignURL = getAPIURL("service/campaign/delete");
        RequestBody formBody = new FormBody.Builder()
                .add("id", String.valueOf(campaignId))
                .build();
        try {
            return sendAndGetAPIResponse(deleteCampaignURL.toString(), formBody);
        } catch (Exception e) {
            throw new MobizonException(e);
        }
    }

    public String getCampaign(int campaignId) throws MobizonException {
        URL getCampaignURL = getAPIURL("service/campaign/get");
        RequestBody formBody = new FormBody.Builder()
                .add("id", String.valueOf(campaignId))
                .build();
        try {
            return sendAndGetAPIResponse(getCampaignURL.toString(), formBody);
        } catch (Exception e) {
            throw new MobizonException(e);
        }
    }

    public String getCampaignLinks(int campaignId) throws MobizonException {
        URL getCampaignLinksURL = getAPIURL("service/campaign/getlinks");
        RequestBody formBody = new FormBody.Builder()
                .add("campaignId", String.valueOf(campaignId))
                .build();
        try {
            return sendAndGetAPIResponse(getCampaignLinksURL.toString(), formBody);
        } catch (Exception e) {
            throw new MobizonException(e);
        }
    }

    public String getCampaignInfo(int campaignId) throws MobizonException {
        URL getCampaignInfoURL = getAPIURL("service/campaign/getinfo");
        RequestBody formBody = new FormBody.Builder()
                .add("id", String.valueOf(campaignId))
                .build();
        try {
            return sendAndGetAPIResponse(getCampaignInfoURL.toString(), formBody);
        } catch (Exception e) {
            throw new MobizonException(e);
        }
    }

    public String startCampaign(int campaignId) throws MobizonException {
        URL getStartCampaignURL = getAPIURL("service/campaign/send");
        RequestBody formBody = new FormBody.Builder()
                .add("id", String.valueOf(campaignId))
                .build();
        try {
            return sendAndGetAPIResponse(getStartCampaignURL.toString(), formBody);
        } catch (Exception e) {
            throw new MobizonException(e);
        }
    }

    public String createLink(CreateLinkParams params) throws MobizonException {
        URL getStartCampaignURL = getAPIURL("service/link/create");
        FormBody.Builder formBuilder = new FormBody.Builder();
        if (!params.getFullLink().isEmpty()) formBuilder.add("data[fullLink]", params.getFullLink());
        if (params.getStatus() != 1) formBuilder.add("data[status]", String.valueOf(params.getStatus()));
        if (params.getExpirationDate().after(new Date(0))) {
            Format formatter = new SimpleDateFormat("yyyy-MM-dd");
            String expirationDate = formatter.format(params.getExpirationDate());
            formBuilder.add("data[expirationDate]", expirationDate);
        }
        if (!params.getComment().isEmpty()) formBuilder.add("data[comment]", params.getComment());
        RequestBody formBody = formBuilder.build();
        try {
            return sendAndGetAPIResponse(getStartCampaignURL.toString(), formBody);
        } catch (Exception e) {
            throw new MobizonException(e);
        }
    }

    public String getLinkById(int linkId) throws MobizonException {
        URL getStartCampaignURL = getAPIURL("service/link/get");
        RequestBody formBody = new FormBody.Builder()
                .add("id", String.valueOf(linkId))
                .build();
        try {
            return sendAndGetAPIResponse(getStartCampaignURL.toString(), formBody);
        } catch (Exception e) {
            throw new MobizonException(e);
        }
    }

    public String deleteLinkById(int linkId) throws MobizonException {
        URL getStartCampaignURL = getAPIURL("service/link/delete");
        RequestBody formBody = new FormBody.Builder()
                .add("ids[0]", String.valueOf(linkId))
                .build();
        try {
            return sendAndGetAPIResponse(getStartCampaignURL.toString(), formBody);
        } catch (Exception e) {
            throw new MobizonException(e);
        }
    }

    /**
     * @param taskId Background task id
     * @throws MobizonException
     */
    public String getTaskQueueStatus(int taskId) throws MobizonException {
        if (taskId <= 0) {
            throw new IllegalArgumentException("Incorrect taskId identifier");
        }
        URL getTaskQueueStatusURL = getAPIURL("service/taskqueue/getstatus");
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("id", String.valueOf(taskId))
                    .build();
            return sendAndGetAPIResponse(getTaskQueueStatusURL.toString(), formBody);
        } catch (Exception e) {
            throw new MobizonException(e);
        }
    }

    private String sendAndGetAPIResponse(String url) throws KeyManagementException, NoSuchAlgorithmException, IOException {
        OkHttpClient okHttpClient = getOkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful())
            throw new IOException("Unexpected response " + response);
        return response.body().string();
    }

    private String sendAndGetAPIResponse(String url, RequestBody requestBody) throws KeyManagementException, NoSuchAlgorithmException, IOException {
        OkHttpClient okHttpClient = getOkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .addHeader("cache-control", "no-cache")
                .post(requestBody)
                .build();
        Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful())
            throw new IOException("Unexpected response " + response);
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

