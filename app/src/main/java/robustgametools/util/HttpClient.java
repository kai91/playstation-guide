package robustgametools.util;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.SyncHttpClient;

import org.apache.http.Header;

import java.util.ArrayList;

/**
 * HttpClient to process all http requests for the application
 */
public class HttpClient {

    private static AsyncHttpClient mAsyncHttpClient = null;
    private static ArrayList<RequestHandle> mRequestHandles = new ArrayList<RequestHandle>();

    private static RequestHandle mSignInRequest;

    // This is the domain name/ip address of the server
    private static String serverUrl = "http://boiling-bastion-9577.herokuapp.com/";


    private static void init() {
        if (mAsyncHttpClient == null) {
            mAsyncHttpClient = new AsyncHttpClient();
        }
    }

    /**
     * Sends a GET request with username. Error 'No such PSN id will
     * be returned in case of incorrect id. If the PSN id is valid user's profile
     * data will be returned.
     * @param username
     * @param responseHandler
     */
    public static void signIn(String username, AsyncHttpResponseHandler responseHandler) {
        init();
        String url = serverUrl + "psn/" + username;
        mSignInRequest = mAsyncHttpClient.get(url, null, responseHandler);
    }

    /**
     * Sends a GET request with username and returns the JSON file for
     * the first 100 games
     * @param responseHandler
     */
    public static void getRecentlyPlayedGames(String username,
                                              AsyncHttpResponseHandler responseHandler) {
        init();
        String url = serverUrl + "psn/" + username + "/" + "trophies";
        mRequestHandles.add(mAsyncHttpClient.get(url, null, responseHandler));
    }

    public static void getGames(String username, int offset,
                                AsyncHttpResponseHandler responseHandler) {
        init();
        String url = serverUrl + "psn/" + username + "/" + "trophies/offset/" + Integer.toString(offset);
        Log.i("GET request: " + url);
        mRequestHandles.add(mAsyncHttpClient.get(url, null, responseHandler));
    }

    /**
     * Cancel all ongoing requests
     */
    public static void cancelRequests() {
        if (!mRequestHandles.isEmpty()) {
            int size = mRequestHandles.size();
            for (int j = 0; j < size; j++ ) {
                mRequestHandles.get(j).cancel(true);
            }
        }
    }

    /**
     * Cancel sign in request
     */
    public static void cancelSignInRequest() {
        if (mSignInRequest != null) {
            mSignInRequest.cancel(true);
        }
    }
}
