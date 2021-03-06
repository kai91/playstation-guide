package robustgametools.util;

import android.content.Context;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;

import java.util.ArrayList;

import robustgametools.guide.GuideFactory;
import robustgametools.model.Guide;
import robustgametools.model.TrophyGuide;

public class GuideDownloader {

    private static GuideDownloader mDownloader;
    private static Storage mStorage;
    private Context mContext;

    private GuideDownloader(Context context) {
        mContext = context.getApplicationContext();
        mStorage = Storage.getInstance(mContext);
    }

    public static GuideDownloader getInstance(Context context) {
        if (mDownloader == null) {
            mDownloader = new GuideDownloader(context);
        }
        return mDownloader;
    }

    public void downloadGuide(final String title, final AsyncHttpResponseHandler handler) {
        HttpClient.getGameGuide(title, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] guideBytes) {
                String guideData = new String(guideBytes);
                String offlineGuide = convertToOffline(guideData);
                mStorage.persistGuideData(title, offlineGuide);
                TrophyGuide guide = new Gson().fromJson(guideData, TrophyGuide.class);
                GuideFactory factory = GuideFactory.getInstance(mContext);
                ArrayList<String> imageUrls = factory.extractImageUrl(guide);
                downloadImages(imageUrls, title, handler);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                  Throwable error) {
                handler.onFailure(statusCode, headers, responseBody, error);
            }
        });
    }

    private void downloadImages(final ArrayList<String> urls, final String title,
                                final AsyncHttpResponseHandler handler) {
        final ArrayList<Integer> completed = new ArrayList<Integer>();
        for (int i = 0; i < urls.size(); i++) {
            final String url = urls.get(i);
            HttpClient.getImage(url, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    completed.add(1);
                    mStorage.persistGuideImage(url, title, responseBody);
                    if (completed.size() == urls.size()) {
                        handler.onSuccess(200, null, null);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    completed.add(0);
                    if (completed.size() == urls.size()) {
                        handler.onFailure(statusCode, headers, responseBody, error);
                        deleteGuideFolder(title);
                    }
                }
            });
        }
    }

    private String convertToOffline(String content) {
        TrophyGuide guide = new  Gson().fromJson(content, TrophyGuide.class);
        ArrayList<Guide> guides = guide.getGuides();
        for (int i = guides.size()-1; i >= 0; i--) {
            Guide temp = guides.get(i);
            String uri = mStorage.convertUrlToOfflineUri(guide.getTitle(), temp.url);
            temp.url = "file://" + uri;
        }
        return new Gson().toJson(guide);
    }

    public void cancelOngoingDownload() {
        HttpClient.cancelGuideRequest();
        HttpClient.cancelImageRequests();
    }

    private void deleteGuideFolder(String title) {
        Storage storage = Storage.getInstance(mContext);
        storage.deleteGuide(title);
    }

}
