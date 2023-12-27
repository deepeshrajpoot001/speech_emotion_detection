package com.deepesh.speechemotiondetection;
import android.app.DownloadManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;



import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

public class MainActivity extends AppCompatActivity {

    // Initializing all variables........
    private TextView startTV, playTV, uploadTV, statusTV,resultTV;
    ProgressBar progressBar;

    MediaRecorder mediaRecorder;
    String audioFilePath;
    boolean uploadCond = false;

    private static final int PICK_AUDIO = 101;
    Uri AudioUri;



    // creating a variable for media recorder object class.
    private MediaRecorder mRecorder;
    int recodeCond,playCond;

    // creating a variable for mediaplayer class
    private MediaPlayer mPlayer;

    // string variable is created for storing a file name
    private static String mFileName = null;

    // constant for storing audio permission
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;

    wavClass wavObj;



    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // Example: 30 seconds
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    public interface ApiService {
        @Multipart
        @POST("predict")
        Call<ResponseBody> sendAudioFile(@Part MultipartBody.Part audio);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recodeCond=1;




        playCond=3;

        ContextWrapper contextWrapper = new ContextWrapper(this);
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);

        wavObj = new wavClass(MainActivity.this);
         mFileName = wavObj.getFilePath("final_record.wav");


        //audioFilePath = musicDirectory.getPath()+"/"+"/recorded_audio.wav";







        // initialize all variables with their layout items.
        statusTV = findViewById(R.id.idTVstatus);
        startTV = findViewById(R.id.btnRecord);
        playTV = findViewById(R.id.btnPlay);
        uploadTV = findViewById(R.id.btnUpload);
        resultTV = findViewById(R.id.idTVresult);
        progressBar = findViewById(R.id.progress_bar);





        startTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recodeCond==1) {
                    playTV.setBackgroundColor(getColor(R.color.gray));
                    startTV.setBackgroundColor(getColor(R.color.purple_200));
                    uploadTV.setBackgroundColor(getColor(R.color.gray));


                    // start recording method will
                    // start the recording of audio.
                    uploadCond= false;
                    statusTV.setText("recording...");
                    startRecording();
                    startTV.setText("Stop Recording");
                    startTV.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.round_mic_off_24,0,0);
                    recodeCond=0;

                }else if(recodeCond==0){
                    pauseRecording();

                    statusTV.setText("");
                    startTV.setText("Start Recording");
                    startTV.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.round_mic_24,0,0);
                    recodeCond=1;
                    playCond=1;
                    playTV.setBackgroundColor(getColor(R.color.green_500));
                    startTV.setBackgroundColor(getColor(R.color.green_500));
                    uploadTV.setBackgroundColor(getColor(R.color.green_500));
                    uploadCond = true;

                }
            }
        });

        playTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playCond==1) {
                    uploadCond=false;
                    playTV.setBackgroundColor(getColor(R.color.purple_200));
                    startTV.setBackgroundColor(getColor(R.color.gray));
                    uploadTV.setBackgroundColor(getColor(R.color.gray));
                    recodeCond=3;
                    startTV.setBackgroundColor(getColor(R.color.gray));

                    // play audio method will play
                    // the audio which we have recorded
                    statusTV.setText("play...");
                    playAudio();
                    playTV.setText("Stop Recording");
                    playTV.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.round_stop_24,0,0);
                    playCond=0;
                }else if(playCond==0){
                    uploadCond= true;
                    playTV.setBackgroundColor(getColor(R.color.green_500));
                    startTV.setBackgroundColor(getColor(R.color.green_500));
                    uploadTV.setBackgroundColor(getColor(R.color.green_500));

                    recodeCond=1;
                    pausePlaying();
                    statusTV.setText("");
                    playTV.setText("Play Recording");
                    playTV.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.round_play_arrow_24,0,0);
                    playCond=1;
                }
            }
        });
        uploadTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(uploadCond) {
                    playTV.setBackgroundColor(getColor(R.color.gray));
                    startTV.setBackgroundColor(getColor(R.color.gray));
                    uploadCond= false;
                    uploadTV.setBackgroundColor(getColor(R.color.purple_200));

                    // pause play method will
                    // pause the play of audio
                    try {
                        getEmotion();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }


            }
        });

    }


    private void getEmotion() throws IOException {
        progressBar.setVisibility(View.VISIBLE);

        statusTV.setText("emotion...");

       // File audioFile = FileUtil.from(MainActivity.this,AudioUri);

        File audioFile;


            //audioFile = FileUtil.from(MainActivity.this,AudioUri);

            audioFile = new File(mFileName);

        Log.d("hideepesh1",""+1);



        // Add logging

       // Log.d("hideepesh","Audio file path: " + audioFile.getAbsolutePath());


        // Create a Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://ec2-13-211-110-79.ap-southeast-2.compute.amazonaws.com:8080/") // Replace with your base URL
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create an instance of the ApiService interface
        ApiService apiService = retrofit.create(ApiService.class);

        // Create a request body with the audio file and the correct media type
        RequestBody requestFile = RequestBody.create(MediaType.parse("audio/wav"), audioFile);
        MultipartBody.Part audioPart = MultipartBody.Part.createFormData("audio", audioFile.getName(), requestFile);

        Log.d("hideepesh1",""+2);

        Call<ResponseBody> call = apiService.sendAudioFile(audioPart);
        call.enqueue(new Callback<ResponseBody>() {




            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                Log.d("hideepesh1",""+7);

                progressBar.setVisibility(View.INVISIBLE);
                playTV.setBackgroundColor(getColor(R.color.green_500));
                startTV.setBackgroundColor(getColor(R.color.green_500));
                uploadTV.setBackgroundColor(getColor(R.color.gray));
                if (response.isSuccessful()) {
                    Log.d("hideepesh1",""+3);
                    try {
                        Log.d("hideepesh1",""+4);
                        String emotionString = response.body().string();
                        JSONObject jsonObject = new JSONObject(emotionString);
                        String data = jsonObject.getString("emotion");
                        resultTV.setText(data);

                    } catch (Exception e) {
                        Log.d("hideepesh1",""+5);
                        e.printStackTrace();
                    }
                } else {
                    Log.d("hideepesh1",""+6);
                    // Handle the error response here
                    resultTV.setText("Error: " + response.code() + " - " + response.message());
                    Toast.makeText(MainActivity.this,recodeCond+"  "+response.message(),Toast.LENGTH_SHORT).show();
                    System.out.println("Error: " + response.code() + " - " + response.message());
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Handle the failure here
                resultTV.setText(t.getMessage());
                t.printStackTrace();
                Log.d("hideepesh1",""+6);
            }
        });

        Log.d("hideepesh1",""+8);

    }



    private void startRecording() {
        // check permission method is used to check
        // that the user has granted permission
        // to record and store the audio.
        if (CheckPermissions()) {

            // setbackgroundcolor method will change
            // the background color of text view.



            // we are here initializing our filename variable
            // with the path of the recorded audio file.

              //****************************************
               wavObj.startRecording();
              //***************************************






            /*
            // below method is used to initialize
            // the media recorder class
            mRecorder = new MediaRecorder();

            // below method is used to set the audio
            // source which we are using a mic.
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

            // below method is used to set
            // the output format of the audio.
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

            // below method is used to set the
            // audio encoder for our recorded audio.
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            // below method is used to set the
            // output file location for our recorded audio
            mRecorder.setOutputFile(mFileName);
            try {
                // below method will prepare
                // our audio recorder class
                mRecorder.prepare();
            } catch (IOException e) {
                Log.e("TAG", "prepare() failed");
            }
            // start method will start
            // the audio recording.
            mRecorder.start();

             */

        } else {
            // if audio recording permissions are
            // not granted by user below method will
            // ask for runtime permission for mic and storage.
            RequestPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // this method is called when user will
        // grant the permission for audio recording.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length > 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord && permissionToStore) {
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean CheckPermissions() {
        // this method is used to check permission
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestPermissions() {
        // this method is used to request the
        // permission for audio recording and storage.
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }


    public void playAudio() {
        mPlayer = new MediaPlayer();
        try {
            // below method is used to set the
            // data source which will be our file name
           // mPlayer.setDataSource(wavObj.getPath("final_record.wav"));

            mPlayer.setDataSource(mFileName);


            // below method will prepare our media player
            mPlayer.prepare();

            // below method will start our media player.
            mPlayer.start();



        } catch (IOException e) {
            Log.e("TAG", "prepare() failed");
        }

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                uploadCond= true;
                playTV.setBackgroundColor(getColor(R.color.green_500));
                startTV.setBackgroundColor(getColor(R.color.green_500));
                uploadTV.setBackgroundColor(getColor(R.color.green_500));

                recodeCond=1;
                pausePlaying();
                statusTV.setText("");
                playTV.setText("Play Recording");
                playTV.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.round_play_arrow_24,0,0);
                playCond=1;
            }
        });
    }
    public void pauseRecording() {
        // below method will stop
        // the audio recording.

        //****************************
        wavObj.stopRecording();
        //****************************



        /*
        mRecorder.stop();
        // below method will release
        // the media recorder class.
        mRecorder.release();
        mRecorder = null;

         */

    }

    public void pausePlaying() {
        // this method will release the media player
        // class and pause the playing of our recorded audio.
        mPlayer.release();
        mPlayer = null;
    }


    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_AUDIO && resultCode == RESULT_OK) {
            // Audio is Picked in format of URI
            AudioUri = data.getData();
            Log.d("hideepesh",AudioUri.getPath());

        }
    }


}
