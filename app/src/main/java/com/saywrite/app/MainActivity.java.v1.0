package com.saywrite.app;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int REQUEST_RECORD_AUDIO = 1001;

    private EditText textBox;
    private Spinner languageSpinner;
    private SeekBar speedBar;

    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;

    private boolean speechReady = false;
    private boolean listening = false;

    private final String[] languages = {
            "English",
            "Sinhala",
            "Tamil"
    };

    private final Locale[] locales = {
            Locale.US,
            new Locale("si", "LK"),
            new Locale("ta", "LK")
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buildUserInterface();

        setupTextToSpeech();

        setupSpeechRecognizer();

        if (android.os.Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO
            );
        }
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView makeText(String text, float size, int gravity) {

        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(size);
        tv.setTextColor(getColor(com.saywrite.app.R.color.text_dark));
        tv.setGravity(gravity);

        return tv;
    }

    private Button makeButton(String text) {

        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(15);
        button.setAllCaps(false);
        button.setTextColor(getColor(R.color.white));
        button.setBackgroundResource(R.drawable.button_bg);

        return button;
    }

    private Button makeSecondaryButton(String text) {

        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setTextColor(getColor(R.color.blue_dark));
        button.setBackgroundResource(R.drawable.secondary_button_bg);

        return button;
    }

    private void buildUserInterface() {

        ScrollView scrollView = new ScrollView(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(24));
        root.setBackgroundColor(getColor(R.color.background));

        scrollView.addView(root);

        TextView title = makeText(
                "🎙️  Say & Write",
                28,
                Gravity.CENTER
        );

        title.setTypeface(null, android.graphics.Typeface.BOLD);

        root.addView(
                title,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(60)
                )
        );

        TextView subtitle = makeText(
                "Speak it. Write it. Listen to it.",
                15,
                Gravity.CENTER
        );

        subtitle.setTextColor(getColor(R.color.gray));

        root.addView(
                subtitle,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(40)
                )
        );

        TextView languageLabel = makeText(
                "Language",
                15,
                Gravity.START
        );

        languageLabel.setTypeface(null, android.graphics.Typeface.BOLD);

        root.addView(
                languageLabel,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(30)
                )
        );

        languageSpinner = new Spinner(this);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        languages
                );

        languageSpinner.setAdapter(adapter);

        root.addView(
                languageSpinner,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(50)
                )
        );

        textBox = new EditText(this);

        textBox.setHint("Type something or tap SPEAK...");
        textBox.setTextSize(18);
        textBox.setGravity(Gravity.TOP | Gravity.START);
        textBox.setMinLines(8);
        textBox.setPadding(dp(16), dp(16), dp(16), dp(16));
        textBox.setBackgroundResource(R.drawable.edit_text_bg);
        textBox.setTextColor(getColor(R.color.text_dark));
        textBox.setHintTextColor(getColor(R.color.gray));
        textBox.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        );

        LinearLayout.LayoutParams editParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(220)
                );

        editParams.setMargins(0, dp(14), 0, dp(12));

        root.addView(textBox, editParams);

        Button speakButton = makeButton("🎤  SPEAK");

        LinearLayout.LayoutParams speakParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(58)
                );

        speakParams.setMargins(0, 0, 0, dp(10));

        root.addView(speakButton, speakParams);

        speakButton.setOnClickListener(v -> startListening());

        Button readButton = makeButton("🔊  READ ALOUD");

        root.addView(
                readButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(58)
                )
        );

        readButton.setOnClickListener(v -> speakText());

        Button stopButton = makeSecondaryButton("⏹  STOP");

        LinearLayout.LayoutParams stopParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(52)
                );

        stopParams.setMargins(0, dp(10), 0, dp(10));

        root.addView(stopButton, stopParams);

        stopButton.setOnClickListener(v -> stopEverything());

        TextView speedLabel = makeText(
                "Speech speed",
                15,
                Gravity.START
        );

        speedLabel.setTypeface(null, android.graphics.Typeface.BOLD);

        root.addView(
                speedLabel,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(30)
                )
        );

        speedBar = new SeekBar(this);
        speedBar.setMax(150);
        speedBar.setProgress(50);

        root.addView(
                speedBar,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(50)
                )
        );

        LinearLayout actionRow =
                new LinearLayout(this);

        actionRow.setOrientation(LinearLayout.HORIZONTAL);

        Button copyButton = makeSecondaryButton("📋 Copy");
        Button shareButton = makeSecondaryButton("📤 Share");
        Button clearButton = makeSecondaryButton("🗑 Clear");

        LinearLayout.LayoutParams actionParams =
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                );

        actionParams.setMargins(dp(3), 0, dp(3), 0);

        actionRow.addView(copyButton, actionParams);
        actionRow.addView(shareButton, actionParams);
        actionRow.addView(clearButton, actionParams);

        root.addView(
                actionRow,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(60)
                )
        );

        copyButton.setOnClickListener(v -> copyText());
        shareButton.setOnClickListener(v -> shareText());
        clearButton.setOnClickListener(v -> textBox.setText(""));

        TextView adPlaceholder = makeText(
                "ADVERTISEMENT",
                11,
                Gravity.CENTER
        );

        adPlaceholder.setTextColor(getColor(R.color.gray));

        LinearLayout.LayoutParams adParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(55)
                );

        adParams.setMargins(0, dp(22), 0, 0);

        root.addView(adPlaceholder, adParams);

        setContentView(scrollView);
    }

    private void setupTextToSpeech() {

        textToSpeech = new TextToSpeech(
                this,
                status -> {

                    if (status == TextToSpeech.SUCCESS) {

                        speechReady = true;

                        setSpeechLanguage();

                    } else {

                        Toast.makeText(
                                this,
                                "Text-to-Speech is not available.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );

        textToSpeech.setOnUtteranceProgressListener(
                new UtteranceProgressListener() {

                    @Override
                    public void onStart(String utteranceId) {
                    }

                    @Override
                    public void onDone(String utteranceId) {
                    }

                    @Override
                    public void onError(String utteranceId) {
                    }
                }
        );
    }

    private void setSpeechLanguage() {

        if (!speechReady) {
            return;
        }

        int position = languageSpinner.getSelectedItemPosition();

        Locale locale = locales[position];

        int result = textToSpeech.setLanguage(locale);

        if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {

            Toast.makeText(
                    this,
                    "This voice is not installed. Android may use another available voice.",
                    Toast.LENGTH_LONG
            ).show();

            textToSpeech.setLanguage(Locale.US);
        }
    }

    private void setupSpeechRecognizer() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {

            Toast.makeText(
                    this,
                    "Speech recognition is not available on this phone.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        speechRecognizer =
                SpeechRecognizer.createSpeechRecognizer(this);

        speechRecognizer.setRecognitionListener(
                new RecognitionListener() {

                    @Override
                    public void onReadyForSpeech(Bundle params) {

                        listening = true;

                        Toast.makeText(
                                MainActivity.this,
                                "Listening...",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onBeginningOfSpeech() {
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
                    }

                    @Override
                    public void onError(int error) {

                        listening = false;

                        String message = "Could not understand speech.";

                        if (error ==
                                SpeechRecognizer.ERROR_NETWORK) {

                            message = "Network error.";

                        } else if (error ==
                                SpeechRecognizer.ERROR_NO_MATCH) {

                            message = "No speech detected.";

                        } else if (error ==
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {

                            message = "Microphone permission is required.";
                        }

                        Toast.makeText(
                                MainActivity.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onResults(Bundle results) {

                        listening = false;

                        ArrayList<String> matches =
                                results.getStringArrayList(
                                        SpeechRecognizer.RESULTS_RECOGNITION
                                );

                        if (matches != null &&
                                !matches.isEmpty()) {

                            String result = matches.get(0);

                            String oldText =
                                    textBox.getText().toString().trim();

                            if (oldText.isEmpty()) {

                                textBox.setText(result);

                            } else {

                                textBox.append(" " + result);
                            }

                            textBox.setSelection(
                                    textBox.length()
                            );
                        }
                    }

                    @Override
                    public void onPartialResults(Bundle partialResults) {
                    }

                    @Override
                    public void onEvent(int eventType, Bundle params) {
                    }
                }
        );
    }

    private void startListening() {

        if (android.os.Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO
            );

            return;
        }

        if (speechRecognizer == null) {

            Toast.makeText(
                    this,
                    "Speech recognition is unavailable.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (listening) {

            speechRecognizer.stopListening();
            listening = false;

            return;
        }

        Intent intent =
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        int position =
                languageSpinner.getSelectedItemPosition();

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                locales[position].toLanguageTag()
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                locales[position].toLanguageTag()
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                3
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                false
        );

        try {

            speechRecognizer.startListening(intent);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to start microphone.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void speakText() {

        if (!speechReady) {

            Toast.makeText(
                    this,
                    "Text-to-Speech is still starting.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String text =
                textBox.getText().toString().trim();

        if (text.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please enter some text first.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        setSpeechLanguage();

        float speed =
                0.5f +
                ((float) speedBar.getProgress() / 100.0f);

        textToSpeech.setSpeechRate(speed);

        textToSpeech.setPitch(1.0f);

        textToSpeech.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "say_write_utterance"
        );
    }

    private void stopEverything() {

        if (speechRecognizer != null) {

            speechRecognizer.stopListening();

            listening = false;
        }

        if (textToSpeech != null) {

            textToSpeech.stop();
        }
    }

    private void copyText() {

        String text =
                textBox.getText().toString();

        if (text.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Nothing to copy.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        ClipboardManager clipboard =
                (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);

        clipboard.setPrimaryClip(
                ClipData.newPlainText(
                        "Say & Write",
                        text
                )
        );

        Toast.makeText(
                this,
                "Copied.",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void shareText() {

        String text =
                textBox.getText().toString();

        if (text.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Nothing to share.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Intent shareIntent =
                new Intent(Intent.ACTION_SEND);

        shareIntent.setType("text/plain");

        shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                text
        );

        startActivity(
                Intent.createChooser(
                        shareIntent,
                        "Share text"
                )
        );
    }

    @Override
    protected void onDestroy() {

        if (speechRecognizer != null) {

            speechRecognizer.destroy();
        }

        if (textToSpeech != null) {

            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        super.onDestroy();
    }
}
