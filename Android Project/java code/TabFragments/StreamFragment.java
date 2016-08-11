package com.example.vato.opguitar.TabFragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.vato.opguitar.MainActivity;
import com.example.vato.opguitar.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import cz.msebera.android.httpclient.Header;

/**
 * Created by GM on 7/21/2016.
 */
public class StreamFragment extends Fragment {

    String UPLOAD_URL = "https://upload.clyp.it/upload";

    private String uploadedFileLink = null;

    public StreamFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.stream_fragment, container, false);
        final ImageView recordButton = (ImageView) view.findViewById(R.id.record_button);
        ImageView playButton = (ImageView) view.findViewById(R.id.play_button);
        final ImageView stopButton = (ImageView) view.findViewById(R.id.stop_button);
        ImageView uploadButton = (ImageView) view.findViewById(R.id.upload_button);
        ImageView sharerButton = (ImageView) view.findViewById(R.id.sharer);


        final Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(500);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);


        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.startRecording();
                recordButton.startAnimation(animation);
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordButton.clearAnimation();
                getView().findViewById(R.id.upload_progress).setVisibility(View.VISIBLE);
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        uploadedFileLink = MainActivity.stopRecording();
                    }
                };
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getView().findViewById(R.id.upload_progress).setVisibility(View.GONE);
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                if (uploadedFileLink == null || uploadedFileLink.equals("")) {
                    Toast.makeText(getContext(), "No records found", Toast.LENGTH_LONG).show();
                } else {
                    File file = new File(uploadedFileLink);
                    intent.setDataAndType(Uri.fromFile(file), "audio/*");
                    startActivity(intent);
                }
            }
        });
        sharerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Debug", "starting sharing");
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = uploadedFileLink;
                sharingIntent.putExtra(
                        android.content.Intent.EXTRA_SUBJECT, "Just Played Music of Mine");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Debug", "starting upload process");
                assert getView() != null;
                getView().findViewById(R.id.sharer).setVisibility(View.GONE);
                getView().findViewById(R.id.upload_complete).setVisibility(View.GONE);
                getView().findViewById(R.id.upload_progress).setVisibility(View.VISIBLE);
                doFileUpload();
            }
        });


        return view;
    }

    private void showShareLink(String shareUrl) {
        Log.d("Upload file url", shareUrl);
        assert getView() != null;
        getView().findViewById(R.id.upload_progress).setVisibility(View.GONE);
        getView().findViewById(R.id.sharer).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.upload_complete).setVisibility(View.VISIBLE);
        uploadedFileLink = shareUrl;
    }

    private String doFileUpload() {
        if (uploadedFileLink == null || uploadedFileLink.equals("")) {
            return "";
        }
        String existingFileName = uploadedFileLink;
//                Environment.getExternalStorageDirectory().getAbsolutePath() + "/lastAudio.mp3";
        String responseFromServer = "";
        String urlString = "http://upload.clyp.it/upload";

        try {
            AsyncHttpClient client = new AsyncHttpClient();
            client.addHeader("Content-Disposition", "form-data; name=\"audioFile\"; filename=\"" + existingFileName + "\"");
            RequestParams params = new RequestParams();
            params.put("audioFile", new File(existingFileName));
            client.post(urlString, params, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        showShareLink(response.getString("Url"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.e("Response", responseString);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.e("dzalian cudio ikam", "" + errorResponse);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return responseFromServer;
    }
}
