package com.deepesh.speechemotiondetection;

import android.content.Context;
import android.content.ContextWrapper;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class wavClass {

    private static final String TAG = "WavRecorder";
    private static final String TEMP_RAW_FILE = "temp_record.raw";
    private static final String FINAL_WAV_FILE = "final_record.wav";
    private static final int BITS_PER_SAMPLE = 16;
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord audioRecord;
    private int bufferSize;
    private Thread recordingThread;
    private boolean isRecording = false;
    Context context;

    public wavClass(Context context1) {
        this.context = context1;
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
    }

    public String getFilePath(String name) {
        ContextWrapper contextWrapper = new ContextWrapper(context);
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        return musicDirectory.getPath() + "/" + name;
    }

    private void writeRawData() {
        try {
            byte[] data = new byte[bufferSize];
            String path = getFilePath(TEMP_RAW_FILE);
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            if (fileOutputStream != null) {
                int read;
                while (isRecording) {
                    read = audioRecord.read(data, 0, bufferSize);
                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                        try {
                            fileOutputStream.write(data);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                fileOutputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startRecording() {
        try {
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    bufferSize
            );
            int status = audioRecord.getState();
            if (status == AudioRecord.STATE_INITIALIZED) {
                audioRecord.startRecording();
                isRecording = true;
                recordingThread = new Thread(this::writeRawData);
                recordingThread.start();
            } else {
                Log.e(TAG, "AudioRecord initialization failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        try {
            if (audioRecord != null) {
                isRecording = false;
                int status = audioRecord.getState();
                if (status == AudioRecord.STATE_INITIALIZED) {
                    audioRecord.stop();
                }
                audioRecord.release();
                recordingThread = null;
                createWavFile(getFilePath(TEMP_RAW_FILE), getFilePath(FINAL_WAV_FILE));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createWavFile(String tempPath, String wavPath) {
        try {
            FileInputStream fileInputStream = new FileInputStream(tempPath);
            FileOutputStream fileOutputStream = new FileOutputStream(wavPath);
            byte[] data = new byte[bufferSize];
            long byteRate = (long) BITS_PER_SAMPLE * SAMPLE_RATE * 1 / 8;
            long totalAudioLen = fileInputStream.getChannel().size();
            long totalDataLen = totalAudioLen + 36;
            wavHeader(fileOutputStream, totalAudioLen, totalDataLen, 1, byteRate);
            while (fileInputStream.read(data) != -1) {
                fileOutputStream.write(data);
            }
            fileInputStream.close();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void wavHeader(FileOutputStream fileOutputStream, long totalAudioLen, long totalDataLen, int channels, long byteRate) {
        try {
            byte[] header = new byte[44];
            header[0] = 'R'; // RIFF/WAVE header
            header[1] = 'I';
            header[2] = 'F';
            header[3] = 'F';
            header[4] = (byte) (totalDataLen & 0xff);
            header[5] = (byte) ((totalDataLen >> 8) & 0xff);
            header[6] = (byte) ((totalDataLen >> 16) & 0xff);
            header[7] = (byte) ((totalDataLen >> 24) & 0xff);
            header[8] = 'W';
            header[9] = 'A';
            header[10] = 'V';
            header[11] = 'E';
            header[12] = 'f'; // 'fmt ' chunk
            header[13] = 'm';
            header[14] = 't';
            header[15] = ' ';
            header[16] = 16; // 4 bytes: size of 'fmt ' chunk
            header[17] = 0;
            header[18] = 0;
            header[19] = 0;
            header[20] = 1; // format = 1
            header[21] = 0;
            header[22] = (byte) channels;
            header[23] = 0;
            header[24] = (byte) (SAMPLE_RATE & 0xff);
            header[25] = (byte) ((SAMPLE_RATE >> 8) & 0xff);
            header[26] = (byte) ((SAMPLE_RATE >> 16) & 0xff);
            header[27] = (byte) ((SAMPLE_RATE >> 24) & 0xff);
            header[28] = (byte) (byteRate & 0xff);
            header[29] = (byte) ((byteRate >> 8) & 0xff);
            header[30] = (byte) ((byteRate >> 16) & 0xff);
            header[31] = (byte) ((byteRate >> 24) & 0xff);
            header[32] = (byte) (2 * BITS_PER_SAMPLE / 8); // block align
            header[33] = 0;
            header[34] = (byte) BITS_PER_SAMPLE; // bits per sample
            header[35] = 0;
            header[36] = 'd';
            header[37] = 'a';
            header[38] = 't';
            header[39] = 'a';
            header[40] = (byte) (totalAudioLen & 0xff);
            header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
            header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
            header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
            fileOutputStream.write(header, 0, 44);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
