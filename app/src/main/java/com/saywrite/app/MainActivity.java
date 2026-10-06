package com.saywrite.app;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {

    private static final int REQUEST_RECORD_AUDIO = 1001;
    private static final int MAX_HISTORY = 10;

    private EditText textBox;
    private Spinner languageSpinner;
    private SeekBar speedBar;

    private Button speakButton;
    private Button readButton;
    private Button stopButton;

    private TextView statusText;
    private LinearLayout historyContainer;

    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;

    private boolean speechReady = false;
    private boolean listening = false;
    private boolean processing = false;

    private final String[] languageNames = {
            "English",
            "Sinhala",
            "Tamil"
    };

    private final Locale[] languageLocales = {
            Locale.US,
            new Locale("si", "LK"),
            new Locale("ta", "LK")
    };

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences("saywrite_history", MODE_PRIVATE);

        buildUserInterface();
        setupTextToSpeech();
        setupSpeechRecognizer();
        updateStatus("Ready", Color.rgb(46, 125, 50));
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView makeText(String text, float size) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(Color.rgb(23, 32, 42));
        return view;
    }

    private Button makeButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(14);
        button.setAllCaps(false);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(50));

        params.setMargins(0, dp(5), 0, dp(5));
        button.setLayoutParams(params);

        return button;
    }

    private void buildUserInterface() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(16), dp(18), dp(16));
        root.setBackgroundColor(Color.rgb(245, 247, 250));

        TextView title = makeText("Say & Write", 28);
        title.setTextColor(Color.rgb(13, 71, 161));
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, android.graphics.Typeface.BOLD);

        root.addView(title,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(50)));

        TextView subtitle = makeText(
                "Speak, write, listen and share",
                15);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setTextColor(Color.rgb(102, 112, 133));

        root.addView(subtitle,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(35)));

        statusText = makeText("Ready", 15);
        statusText.setGravity(Gravity.CENTER);
        statusText.setTypeface(null, android.graphics.Typeface.BOLD);

        LinearLayout.LayoutParams statusParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(40));
        statusParams.setMargins(0, dp(4), 0, dp(6));

        root.addView(statusText, statusParams);

        TextView languageLabel = makeText("Language", 15);
        languageLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(languageLabel);

        languageSpinner = new Spinner(this);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        languageNames);

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        languageSpinner.setAdapter(adapter);

        root.addView(languageSpinner,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(50)));

        textBox = new EditText(this);
        textBox.setHint("Write something or tap SPEAK...");
        textBox.setTextSize(17);
        textBox.setGravity(Gravity.TOP | Gravity.START);
        textBox.setPadding(dp(12), dp(12), dp(12), dp(12));
        textBox.setBackgroundColor(Color.WHITE);
        textBox.setSingleLine(false);
        textBox.setMinLines(6);

        LinearLayout.LayoutParams textParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(150));
        textParams.setMargins(0, dp(8), 0, dp(8));

        root.addView(textBox, textParams);

        speakButton = makeButton("🎤 SPEAK");
        readButton = makeButton("🔊 READ ALOUD");
        stopButton = makeButton("⏹ STOP");

        root.addView(speakButton);
        root.addView(readButton);
        root.addView(stopButton);

        TextView speedLabel = makeText("Speech Speed", 15);
        speedLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(speedLabel);

        speedBar = new SeekBar(this);
        speedBar.setMax(20);
        speedBar.setProgress(10);

        root.addView(speedBar,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(45)));

        Button copyButton = makeButton("📋 COPY");
        Button shareButton = makeButton("📤 SHARE");
        Button clearButton = makeButton("🗑 CLEAR");

        root.addView(copyButton);
        root.addView(shareButton);
        root.addView(clearButton);

        TextView adText = makeText("ADVERTISEMENT", 12);
        adText.setGravity(Gravity.CENTER);
        adText.setTextColor(Color.rgb(150, 150, 150));

        LinearLayout.LayoutParams adParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(45));
        adParams.setMargins(0, dp(8), 0, dp(8));

        root.addView(adText, adParams);

        TextView historyTitle = makeText("🕘 Recent Texts", 18);
        historyTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(historyTitle);

        historyContainer = new LinearLayout(this);
        historyContainer.setOrientation(LinearLayout.VERTICAL);

        root.addView(historyContainer);

        Button clearHistoryButton = makeButton("🧹 Clear History");
        root.addView(clearHistoryButton);

        speakButton.setOnClickListener(v -> startListening());

        readButton.setOnClickListener(v -> {
            String text = textBox.getText().toString().trim();

            if (text.isEmpty()) {
                Toast.makeText(
                        MainActivity.this,
                        "Please enter some text first.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            saveToHistory(text);
            speakText(text);
        });

        stopButton.setOnClickListener(v -> stopEverything());

        copyButton.setOnClickListener(v -> copyText());

        shareButton.setOnClickListener(v -> shareText());

        clearButton.setOnClickListener(v ->
                textBox.setText(""));

        clearHistoryButton.setOnClickListener(v -> {
            preferences.edit().clear().apply();
            loadHistory();
            Toast.makeText(
                    MainActivity.this,
                    "History cleared",
                    Toast.LENGTH_SHORT).show();
        });

        languageSpinner.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        setSpeechLanguage(position);
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {
                    }
                });

        setContentView(root);

        loadHistory();
    }

    private void updateStatus(String message, int color) {

        if (statusText != null) {
            statusText.setText(message);
            statusText.setTextColor(color);
        }
    }

    private void setButtonsForListening(boolean active) {

        if (speakButton == null) return;

        speakButton.setEnabled(!active);
        readButton.setEnabled(!active);
        stopButton.setEnabled(active);

        if (active) {
            speakButton.setText("🎤 LISTENING...");
        } else {
            speakButton.setText("🎤 SPEAK");
        }
    }

    private void setButtonsForProcessing(boolean active) {

        if (speakButton == null) return;

        if (active) {
            speakButton.setEnabled(false);
            readButton.setEnabled(false);
            stopButton.setEnabled(true);
        } else {
            speakButton.setEnabled(true);
            readButton.setEnabled(true);
            stopButton.setEnabled(false);
        }
    }

    private void setupTextToSpeech() {

        textToSpeech = new TextToSpeech(
                this,
                status -> {

                    if (status == TextToSpeech.SUCCESS) {

                        speechReady = true;

                        int position = languageSpinner.getSelectedItemPosition();

                        if (position >= 0 &&
                                position < languageLocales.length) {

                            textToSpeech.setLanguage(
                                    languageLocales[position]);
                        }

                        updateStatus(
                                "Ready",
                                Color.rgb(46, 125, 50));

                    } else {

                        speechReady = false;

                        updateStatus(
                                "Text-to-speech unavailable",
                                Color.rgb(198, 40, 40));
                    }
                });
    }

    private void setSpeechLanguage(int position) {

        if (textToSpeech == null || !speechReady) {
            return;
        }

        if (position < 0 ||
                position >= languageLocales.length) {
            return;
        }

        Locale locale = languageLocales[position];

        int result = textToSpeech.setLanguage(locale);

        if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {

            Toast.makeText(
                    this,
                    languageNames[position] +
                            " voice data is not available.",
                    Toast.LENGTH_LONG).show();

            updateStatus(
                    "Voice language unavailable",
                    Color.rgb(198, 40, 40));
        } else {

            updateStatus(
                    "Ready",
                    Color.rgb(46, 125, 50));
        }
    }

    private void setupSpeechRecognizer() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {

            updateStatus(
                    "Speech recognition unavailable",
                    Color.rgb(198, 40, 40));

            return;
        }

        speechRecognizer =
                SpeechRecognizer.createSpeechRecognizer(this);

        speechRecognizer.setRecognitionListener(
                new RecognitionListener() {

                    @Override
                    public void onReadyForSpeech(Bundle params) {

                        processing = false;
                        listening = true;

                        updateStatus(
                                "🔴 Listening...",
                                Color.rgb(198, 40, 40));

                        setButtonsForListening(true);
                    }

                    @Override
                    public void onBeginningOfSpeech() {

                        listening = true;

                        updateStatus(
                                "🔴 Listening...",
                                Color.rgb(198, 40, 40));
                    }

                    @Override
                    public void onRmsChanged(float rmsdB) {
                    }

                    @Override
                    public void onBufferReceived(byte[] buffer) {
                    }

                    @Override
                    public void onEndOfSpeech() {

                        listening = false;
                        processing = true;

                        updateStatus(
                                "⏳ Processing...",
                                Color.rgb(245, 124, 0));

                        setButtonsForProcessing(true);
                    }

                    @Override
                    public void onError(int error) {

                        listening = false;
                        processing = false;

                        setButtonsForProcessing(false);

                        String message;

                        switch (error) {

                            case SpeechRecognizer.ERROR_AUDIO:
                                message = "Audio recording error";
                                break;

                            case SpeechRecognizer.ERROR_CLIENT:
                                message = "Speech recognition stopped";
                                break;

                            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                                message = "Microphone permission required";
                                break;

                            case SpeechRecognizer.ERROR_NETWORK:
                                message = "Network error";
                                break;

                            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                                message = "Network timeout";
                                break;

                            case SpeechRecognizer.ERROR_NO_MATCH:
                                message = "No speech detected";
                                break;

                            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                                message = "Recognizer is busy";
                                break;

                            case SpeechRecognizer.ERROR_SERVER:
                                message = "Recognition server error";
                                break;

                            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                                message = "No speech detected";
                                break;

                            default:
                                message = "Speech recognition error";
                                break;
                        }

                        updateStatus(
                                message,
                                Color.rgb(198, 40, 40));

                        Toast.makeText(
                                MainActivity.this,
                                message,
                                Toast.LENGTH_SHORT).show();

                        if (error != SpeechRecognizer.ERROR_CLIENT) {
                            updateStatusDelayed();
                        }
                    }

                    @Override
                    public void onResults(Bundle results) {

                        listening = false;
                        processing = false;

                        ArrayList<String> matches =
                                results.getStringArrayList(
                                        SpeechRecognizer.RESULTS_RECOGNITION);

                        if (matches != null &&
                                !matches.isEmpty()) {

                            String result = matches.get(0);

                            textBox.setText(result);
                            textBox.setSelection(
                                    textBox.length());

                            saveToHistory(result);

                            updateStatus(
                                    "✓ Text received",
                                    Color.rgb(46, 125, 50));

                        } else {

                            updateStatus(
                                    "No speech detected",
                                    Color.rgb(198, 40, 40));
                        }

                        setButtonsForProcessing(false);
                    }

                    @Override
                    public void onPartialResults(Bundle partialResults) {
                    }

                    @Override
                    public void onEvent(int eventType, Bundle params) {
                    }
                });
    }

    private void updateStatusDelayed() {

        statusText.postDelayed(
                () -> {

                    if (!listening && !processing) {

                        updateStatus(
                                "Ready",
                                Color.rgb(46, 125, 50));
                    }

                },
                1800);
    }

    private void startListening() {

        if (listening || processing) {

            Toast.makeText(
                    this,
                    "Speech recognition is already running.",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        if (speechRecognizer == null) {

            Toast.makeText(
                    this,
                    "Speech recognition is unavailable.",
                    Toast.LENGTH_LONG).show();

            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(
                        Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.RECORD_AUDIO
                    },
                    REQUEST_RECORD_AUDIO);

            return;
        }

        int position =
                languageSpinner.getSelectedItemPosition();

        if (position < 0 ||
                position >= languageLocales.length) {
            position = 0;
        }

        Intent intent =
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                languageLocales[position].toLanguageTag());

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                languageLocales[position].toLanguageTag());

        intent.putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                5);

        intent.putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                false);

        listening = true;

        updateStatus(
                "🔴 Starting microphone...",
                Color.rgb(198, 40, 40));

        setButtonsForListening(true);

        try {

            speechRecognizer.startListening(intent);

        } catch (Exception e) {

            listening = false;
            processing = false;

            setButtonsForProcessing(false);

            updateStatus(
                    "Could not start microphone",
                    Color.rgb(198, 40, 40));

            Toast.makeText(
                    this,
                    "Could not start speech recognition.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void speakText(String text) {

        if (!speechReady || textToSpeech == null) {

            Toast.makeText(
                    this,
                    "Text-to-speech is not ready.",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        float speed =
                0.5f + (speedBar.getProgress() / 10.0f);

        textToSpeech.setSpeechRate(speed);

        int position =
                languageSpinner.getSelectedItemPosition();

        if (position >= 0 &&
                position < languageLocales.length) {

            textToSpeech.setLanguage(
                    languageLocales[position]);
        }

        updateStatus(
                "🔊 Reading aloud...",
                Color.rgb(21, 101, 192));

        textToSpeech.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "saywrite");

        statusText.postDelayed(
                () -> {

                    if (!listening && !processing) {

                        updateStatus(
                                "Ready",
                                Color.rgb(46, 125, 50));
                    }

                },
                1200);
    }

    private void stopEverything() {

        if (speechRecognizer != null) {

            try {
                speechRecognizer.stopListening();
                speechRecognizer.cancel();
            } catch (Exception ignored) {
            }
        }

        if (textToSpeech != null) {

            try {
                textToSpeech.stop();
            } catch (Exception ignored) {
            }
        }

        listening = false;
        processing = false;

        setButtonsForProcessing(false);

        updateStatus(
                "Ready",
                Color.rgb(46, 125, 50));
    }

    private void copyText() {

        String text = textBox.getText().toString();

        if (text.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Nothing to copy.",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        ClipboardManager clipboard =
                (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);

        ClipData clip =
                ClipData.newPlainText(
                        "Say & Write",
                        text);

        clipboard.setPrimaryClip(clip);

        Toast.makeText(
                this,
                "Copied",
                Toast.LENGTH_SHORT).show();
    }

    private void shareText() {

        String text = textBox.getText().toString();

        if (text.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Nothing to share.",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        Intent shareIntent =
                new Intent(Intent.ACTION_SEND);

        shareIntent.setType("text/plain");
        shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                text);

        startActivity(
                Intent.createChooser(
                        shareIntent,
                        "Share text"));
    }

    private void saveToHistory(String text) {

        if (text == null) return;

        text = text.trim();

        if (text.isEmpty()) return;

        List<String> history = getHistory();

        history.remove(text);
        history.add(0, text);

        while (history.size() > MAX_HISTORY) {
            history.remove(history.size() - 1);
        }

        Set<String> set = new HashSet<>(history);

        preferences.edit()
                .putStringSet("history", set)
                .putString("order", joinHistory(history))
                .apply();

        loadHistory();
    }

    private List<String> getHistory() {

        String order =
                preferences.getString("order", "");

        List<String> history =
                new ArrayList<>();

        if (!order.isEmpty()) {

            String[] parts =
                    order.split(
                            "\\Q|||\\E",
                            -1);

            for (String item : parts) {

                if (!item.trim().isEmpty()) {
                    history.add(item);
                }
            }
        }

        return history;
    }

    private String joinHistory(List<String> history) {

        StringBuilder builder =
                new StringBuilder();

        for (String item : history) {

            if (builder.length() > 0) {
                builder.append("|||");
            }

            builder.append(item);
        }

        return builder.toString();
    }

    private void loadHistory() {

        if (historyContainer == null) return;

        historyContainer.removeAllViews();

        List<String> history = getHistory();

        if (history.isEmpty()) {

            TextView empty =
                    makeText(
                            "No recent texts yet.",
                            14);

            empty.setTextColor(
                    Color.rgb(102, 112, 133));

            empty.setPadding(
                    0,
                    dp(8),
                    0,
                    dp(8));

            historyContainer.addView(empty);

            return;
        }

        for (String text : history) {

            addHistoryItem(text);
        }
    }

    private void addHistoryItem(String text) {

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL);

        row.setGravity(Gravity.CENTER_VERTICAL);

        row.setPadding(
                dp(8),
                dp(6),
                dp(4),
                dp(6));

        TextView item =
                makeText(text, 15);

        item.setTextColor(
                Color.rgb(23, 32, 42));

        item.setMaxLines(3);

        Button delete =
                new Button(this);

        delete.setText("✕");
        delete.setTextSize(13);
        delete.setAllCaps(false);

        LinearLayout.LayoutParams textParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1);

        LinearLayout.LayoutParams deleteParams =
                new LinearLayout.LayoutParams(
                        dp(55),
                        dp(48));

        row.addView(item, textParams);
        row.addView(delete, deleteParams);

        item.setOnClickListener(v -> {

            textBox.setText(text);
            textBox.setSelection(
                    textBox.length());

            updateStatus(
                    "Text restored",
                    Color.rgb(21, 101, 192));
        });

        delete.setOnClickListener(v ->
                deleteHistoryItem(text));

        historyContainer.addView(row);
    }

    private void deleteHistoryItem(String text) {

        List<String> history = getHistory();

        history.remove(text);

        if (history.isEmpty()) {

            preferences.edit()
                    .remove("history")
                    .remove("order")
                    .apply();

        } else {

            preferences.edit()
                    .putStringSet(
                            "history",
                            new HashSet<>(history))
                    .putString(
                            "order",
                            joinHistory(history))
                    .apply();
        }

        loadHistory();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults);

        if (requestCode == REQUEST_RECORD_AUDIO) {

            if (grantResults.length > 0 &&
                    grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED) {

                updateStatus(
                        "Microphone ready",
                        Color.rgb(46, 125, 50));

                startListening();

            } else {

                updateStatus(
                        "Microphone permission denied",
                        Color.rgb(198, 40, 40));

                Toast.makeText(
                        this,
                        "Microphone permission is required for SPEAK.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {

        if (speechRecognizer != null) {

            try {
                speechRecognizer.destroy();
            } catch (Exception ignored) {
            }
        }

        if (textToSpeech != null) {

            try {
                textToSpeech.stop();
                textToSpeech.shutdown();
            } catch (Exception ignored) {
            }
        }

        super.onDestroy();
    }
}
