package com.recipex;

import com.appspot.recipex_1281.recipexServerApi.RecipexServerApi;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import javax.annotation.Nullable;


public class AppConstants {
    public static final String WEB_CLIENT_ID = "1077668244667-v42n91q6av4tlub6rh3dffbdqa0pncj0.apps.googleusercontent.com";
    public static final String AUDIENCE = "server:client_id:" + WEB_CLIENT_ID;

    // Codici server
    public static final String CREATED = "201 Created";
    public static final String OK = "200 OK";
    public static final String PRECONDITION_FAILED = "412 Precondition Failed";
    public static final String BAD_REQUEST = "400 Bad Request";
    public static final String NOT_FOUND = "404 Not Found";

    /**
     * Class instance of the JSON factory.
     */
    public static final JsonFactory JSON_FACTORY = new AndroidJsonFactory();

    /**
     * Class instance of the HTTP transport.
     */
    public static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();


    /**
     * Retrieve a RecipexServerApi api service handle to access the API.
     */

    public static RecipexServerApi getApiServiceHandle(@Nullable GoogleAccountCredential credential) {
        // Use a builder to help formulate the API request.
        RecipexServerApi.Builder recipexServerApi = new RecipexServerApi.Builder(AppConstants.HTTP_TRANSPORT,
                                                                           AppConstants.JSON_FACTORY,
                                                                           credential);

        recipexServerApi.setRootUrl("https://recipex-1281.appspot.com/_ah/api");
        return recipexServerApi.build();
    }
}
