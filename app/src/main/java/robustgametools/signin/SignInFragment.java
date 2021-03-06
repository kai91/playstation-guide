package robustgametools.signin;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import robustgametools.playstation_guide.R;
import robustgametools.util.HttpClient;
import robustgametools.util.JsonFactory;
import robustgametools.util.Log;
import robustgametools.util.Storage;


public class SignInFragment extends Fragment {
    @InjectView(R.id.username) EditText mUsername;

    private onSignInListener mListener;
    private ProgressDialog mProgressDialog;
    private Storage mStorage;
    private JsonFactory jsonFactory = JsonFactory.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_sign_in_form, container, false);
        ButterKnife.inject(this, rootView);
        mStorage = Storage.getInstance(getActivity());
        return rootView;
    }

    @OnClick(R.id.sign_in)
    public void signInButtonClicked() {
        signInClicked();
    }


    @OnEditorAction(R.id.username)
    public boolean signInClicked() {

        final String username = mUsername.getText().toString();
        showLoadingDialog();
        HttpClient.signIn(username, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] responseBody) {

                String response = new String(responseBody);
                JsonObject responseJson = new JsonParser().parse(response).getAsJsonObject();
                if (responseJson.has("error")) {
                    Toast.makeText(getActivity(), responseJson.get("message").getAsString(),
                            Toast.LENGTH_LONG).show();
                    mProgressDialog.dismiss();
                } else {
                    persistUserData(response);
                    getGames(username);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  byte[] responseBody, Throwable error) {

                hideLoadingDialog();
                Toast.makeText(getActivity(), "Error signing in: " +
                        Integer.toString(statusCode), Toast.LENGTH_LONG).show();
            }
        });
        return true;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (onSignInListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement onSignInListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        HttpClient.cancelSignInRequest();
    }

    private void getGames(final String username) {
        HttpClient.getRecentlyPlayedGames(username, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                int gameCount = jsonFactory.parseGameCount(response);
                updateList(response, username, 100, gameCount);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getActivity(), "Error retrieving information: " +
                        Integer.toString(statusCode), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateList(String data, String username, int offset, int total) {
        if (total <= offset) {
            // Finished updating all games
            persistRecentGames(data);
            successfullySignedIn();
        } else {
            retrieveList(data, username, offset, total);
        }
    }

    private void retrieveList(final String data, final String username, final int offset, final int total) {
        HttpClient.getGames(username, offset,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String newData = new String(responseBody);
                        newData = jsonFactory.appendJson(data, newData);
                        updateList(newData, username, offset + 128, total);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.i("Updating game list: Failed");
                        Toast.makeText(getActivity(), "Failed updating data", Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Called upon successfully logged in. Clean up
     * and finally pass on to the activity to handle
     */
    private void successfullySignedIn() {
        hideKeyboard();
        mProgressDialog.dismiss();
        mListener.onSignInSuccess();
    }

    /**
     * Save player's data to the internal storage
     * @param data
     */
    private void persistUserData(String data) {
        mStorage.persistUserData(data);
    }

    private void persistRecentGames(String data) {
        mStorage.persistRecentGames(data);
    }

    private void showLoadingDialog() {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage("Signing in...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                HttpClient.cancelSignInRequest();
                mProgressDialog.dismiss();
            }
        });
        mProgressDialog.show();
    }

    private void hideLoadingDialog() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mUsername.getWindowToken(), 0);
    }

    public interface onSignInListener {
        public void onSignInSuccess();
    }

}
