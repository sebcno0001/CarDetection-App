package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_VIDEO = 1;

    private VideoView videoView;
    private DrawBoxes drawBoxes;
    private YoloV8 yoloV8;
    private TextView statusText, detectionsListText;
    private Button detectButton, playButton, pauseButton;
    private Uri selectedVideoUri;
    private MediaMetadataRetriever retriever;
    private boolean isProcessed = false;
    private List<List<DetectionResult>> frameDetections = new ArrayList<>();
    private VideoProcessor videoProcessor;
    private Handler handler = new Handler(Looper.getMainLooper());

    private int videoWidth = 0, videoHeight = 0;
    private int displayW = 0, displayH = 0, offsetX = 0, offsetY = 0;
    private long frameIntervalMs = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoView = findViewById(R.id.videoView);
        drawBoxes = findViewById(R.id.overlayView);
        detectButton = findViewById(R.id.detectButton);
        playButton = findViewById(R.id.playButton);
        pauseButton = findViewById(R.id.pauseButton);
        Button clearButton = findViewById(R.id.clearButton);
        Button selectVideoButton = findViewById(R.id.selectVideoButton);
        statusText = findViewById(R.id.statusText);
        detectionsListText = findViewById(R.id.detectionsListText);

        yoloV8 = new YoloV8(this);
        retriever = new MediaMetadataRetriever();
        videoProcessor = new VideoProcessor();

        detectButton.setEnabled(false);
        playButton.setEnabled(false);
        pauseButton.setEnabled(false);

        statusText.setText("Wybierz film aby rozpocząć wykrywanie samochodów");

        selectVideoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "Wybierz film"), PICK_VIDEO);
        });

        detectButton.setOnClickListener(v -> {
            if (selectedVideoUri == null) {
                Toast.makeText(this, "Musisz wybrać film!", Toast.LENGTH_SHORT).show();
                return;
            }
            startVideoDetection();
        });

        playButton.setOnClickListener(v -> playVideo());
        pauseButton.setOnClickListener(v -> {
            videoView.pause();
            videoProcessor.stopOverlay();
        });

        clearButton.setOnClickListener(v -> reset());
    }

    private void showVideoPreview() {
        if (selectedVideoUri == null) return;

        videoView.setVideoURI(selectedVideoUri);
        videoView.setOnPreparedListener(mp -> {
            videoWidth = mp.getVideoWidth();
            videoHeight = mp.getVideoHeight();
            updateVideoScale();

            videoView.seekTo(1);
        });
    }

    private void playVideo() {
        if (!isProcessed || selectedVideoUri == null) return;

        videoView.setVideoURI(selectedVideoUri);

        videoView.setOnPreparedListener(mp -> {
            videoWidth = mp.getVideoWidth();
            videoHeight = mp.getVideoHeight();

            updateVideoScale();

            videoView.start();
            videoProcessor.startOverlay();
        });
    }

    private void updateVideoScale() {
        int viewW = videoView.getWidth();
        int viewH = videoView.getHeight();

        if (viewW == 0 || viewH == 0 || videoWidth == 0 || videoHeight == 0) return;
        float videoAspect = (float) videoWidth / videoHeight;
        float viewAspect = (float) viewW / viewH;

        if (videoAspect > viewAspect) {
            displayW = viewW;
            displayH = (int) (viewW / videoAspect);
            offsetX = 0;
            offsetY = (viewH - displayH) / 2;
        } else {
            displayH = viewH;
            displayW = (int) (viewH * videoAspect);
            offsetY = 0;
            offsetX = (viewW - displayW) / 2;
        }

        drawBoxes.setVideoOriginalSize(videoWidth, videoHeight);
        drawBoxes.setVideoDisplayInfo(displayW, displayH, offsetX, offsetY);
    }

    private void startVideoDetection() {
        statusText.setText("Przetwarzanie filmu...");
        detectButton.setEnabled(false);
        frameDetections.clear();
        drawBoxes.clearDetections();

        new Thread(() -> {
            try {
                retriever.setDataSource(this, selectedVideoUri);
                long durationMs = Long.parseLong(
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                );

                int totalFrames = (int) Math.ceil(durationMs / (double) frameIntervalMs);

                for (long t = 0; t < durationMs; t += frameIntervalMs) {
                    Bitmap frame = retriever.getFrameAtTime(t * 1000L, MediaMetadataRetriever.OPTION_CLOSEST);
                    if (frame != null) {
                        frameDetections.add(yoloV8.detect(frame));
                    } else {
                        frameDetections.add(new ArrayList<>());
                    }

                    int progress = (int) ((frameDetections.size() * 100.0) / totalFrames);
                    runOnUiThread(() -> statusText.setText("Przetwarzanie: " + progress + "%"));
                }

                runOnUiThread(() -> {
                    isProcessed = true;
                    statusText.setText("Film przetworzony — kliknij Odtwórz");
                    playButton.setEnabled(true);
                    pauseButton.setEnabled(true);
                    detectButton.setEnabled(true);

                    int total = frameDetections.stream().mapToInt(List::size).sum();
                    detectionsListText.setText("Łącznie wykryto: " + total + " obiektów");
                });

            } catch (Exception e) {
                runOnUiThread(() -> statusText.setText("Błąd podczas analizy filmu"));
                e.printStackTrace();
            }
        }).start();
    }

    private void reset() {
        videoView.stopPlayback();
        drawBoxes.clearDetections();
        selectedVideoUri = null;
        frameDetections.clear();
        isProcessed = false;
        detectButton.setEnabled(false);
        playButton.setEnabled(false);
        pauseButton.setEnabled(false);
        statusText.setText("Wybierz nowy film");
        detectionsListText.setText("");
        videoProcessor.stopOverlay();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_VIDEO && resultCode == RESULT_OK && data != null) {
            selectedVideoUri = data.getData();
            if (selectedVideoUri != null) {
                detectButton.setEnabled(true);
                statusText.setText("Film wybrany — kliknij Przetwórz");
                detectionsListText.setText("");
                isProcessed = false;

                showVideoPreview();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoProcessor.stopOverlay();
    }

    private class VideoProcessor {
        private Runnable overlayRunnable;
        private boolean running = false;

        void startOverlay() {
            if (running) return;
            running = true;

            overlayRunnable = new Runnable() {
                @Override
                public void run() {
                    if (!running || !videoView.isPlaying()) return;

                    int currentPos = videoView.getCurrentPosition();
                    int index = (int) (currentPos / frameIntervalMs);
                    float factor = (currentPos % frameIntervalMs) / (float) frameIntervalMs;

                    if (index < 0 || index >= frameDetections.size()) return;

                    List<DetectionResult> a = frameDetections.get(index);

                    if (index + 1 < frameDetections.size()) {
                        List<DetectionResult> b = frameDetections.get(index + 1);

                        List<DetectionResult> blended =
                                BoxInterpolator.interpolate(a, b, factor);

                        drawBoxes.setDetections(blended);
                    } else {
                        drawBoxes.setDetections(a);
                    }

                    handler.postDelayed(this, 16); // ~60 FPS
                }
            };

            handler.post(overlayRunnable);
        }

        void stopOverlay() {
            running = false;
            if (overlayRunnable != null) handler.removeCallbacks(overlayRunnable);
        }
    }
}
