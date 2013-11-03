
package org.grammaticalframework.ui.android;

import java.io.Serializable;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import org.grammaticalframework.ui.android.ASR.State;
import org.grammaticalframework.ui.android.LanguageSelector.OnLanguageSelectedListener;
import org.grammaticalframework.ui.android.ConversationView.OnWordSelectedListener;
import org.grammaticalframework.pgf.MorphoAnalysis;

public class MainActivity extends Activity {

    private static final boolean DBG = true;
    private static final String TAG = "MainActivity";

    private static final boolean FAKE_SPEECH = false;

    private ImageView mStartStopButton;

    private ConversationView mConversationView;

    private LanguageSelector mSourceLanguageView;

    private LanguageSelector mTargetLanguageView;

    private ImageView mSwitchLanguagesButton;

    private ASR mAsr;

    private TTS mTts;

    private Translator mTranslator;
    
    private boolean input_mode;

    private SpeechInputListener mSpeechListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStartStopButton = (ImageView) findViewById(R.id.start_stop);
        mConversationView = (ConversationView) findViewById(R.id.conversation);
        mSourceLanguageView = (LanguageSelector) findViewById(R.id.source_language);
        mTargetLanguageView = (LanguageSelector) findViewById(R.id.target_language);
        mSwitchLanguagesButton = (ImageView) findViewById(R.id.switch_languages);
        
        mStartStopButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAsr.isRunning()) {
                    stopRecognition();
                } else {
                    startRecognition();
                }
            }
        });

        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        input_mode = pref.getBoolean("input_mode", true);
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
        	input_mode = false;
        }
        mStartStopButton.setImageResource(input_mode ? R.drawable.ic_mic : R.drawable.ic_keyboard);

        mSpeechListener = new SpeechInputListener();
        
        mConversationView.setOnWordSelectedListener(new OnWordSelectedListener() {
            @Override
            public void onWordSelected(CharSequence word, Object lexicon) {
            	Intent myIntent = new Intent(MainActivity.this, LexicalEntryActivity.class);
            	myIntent.putExtra("source", word);
            	myIntent.putExtra("analyses", (Serializable) lexicon);
            	MainActivity.this.startActivity(myIntent);
            }
        });
        mConversationView.setSpeechInputListener(mSpeechListener);

        mAsr = new ASR(this);
        mAsr.setListener(mSpeechListener);

        mTts = new TTS(this);

        mTranslator = ((GFTranslator) getApplicationContext()).getTranslator();

        mSourceLanguageView.setLanguages(mTranslator.getAvailableSourceLanguages());
        mSourceLanguageView.setOnLanguageSelectedListener(new OnLanguageSelectedListener() {
            @Override
            public void onLanguageSelected(Language language) {
                onSourceLanguageSelected(language);
            }
        });
        mTargetLanguageView.setLanguages(mTranslator.getAvailableTargetLanguages());
        mTargetLanguageView.setOnLanguageSelectedListener(new OnLanguageSelectedListener() {
            @Override
            public void onLanguageSelected(Language language) {
                onTargetLanguageSelected(language);
            }
        });

        mSwitchLanguagesButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onSwitchLanguages();
            }
        });
    }

	@Override
	protected void onResume() {
		super.onResume();

		mSourceLanguageView.setSelectedLanguage(mTranslator.getSourceLanguage());
		mTargetLanguageView.setSelectedLanguage(mTranslator.getTargetLanguage());
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putBoolean("input_mode", input_mode);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);

	    menu.getItem(0).setTitle(input_mode ? R.string.keyboard_input : R.string.mic_input);

	    if (!SpeechRecognizer.isRecognitionAvailable(this)) {
	    	menu.getItem(0).setEnabled(false);
	    }
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.input_mode:
	        	if (input_mode) {
	        		item.setTitle(R.string.mic_input);
	        		mStartStopButton.setImageResource(R.drawable.ic_keyboard);
	        		input_mode = false;
	        	} else {
	        		item.setTitle(R.string.keyboard_input);
	        		mStartStopButton.setImageResource(R.drawable.ic_mic);
	        		input_mode = true;
	        	}

	        	SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
	        	editor.putBoolean("input_mode", input_mode);
	        	editor.commit();

	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

    @Override
    protected void onDestroy() {
        if (mAsr != null) {
            mAsr.destroy();
            mAsr = null;
        }
        if (mTts != null) {
            mTts.destroy();
            mTts = null;
        }
        super.onDestroy();
    }

    void onSourceLanguageSelected(Language language) {
        mTranslator.setSourceLanguage(language);
    }

    void onTargetLanguageSelected(Language language) {
        mTranslator.setTargetLanguage(language);
    }

    public String getSourceLanguageCode() {
        return mTranslator.getSourceLanguage().getLangCode();
    }

    public String getTargetLanguageCode() {
        return mTranslator.getTargetLanguage().getLangCode();
    }

    void onSwitchLanguages() {
        Language newSource = mTranslator.getTargetLanguage();
        Language newTarget = mTranslator.getSourceLanguage();
        mSourceLanguageView.setSelectedLanguage(newSource);
        mTargetLanguageView.setSelectedLanguage(newTarget);
    }

    private void startRecognition() {
    	if (FAKE_SPEECH) {
            handleSpeechInput("where is the hotel");
            return;
    	}

    	if (input_mode) {
    		mConversationView.addFirstPersonUtterance("...");
            mAsr.setLanguage(getSourceLanguageCode());
            mAsr.startRecognition();
    	} else {
    		mConversationView.addInputBox();
    	}
    }

    private void stopRecognition() {
        mAsr.stopRecognition();
    }

    private void handlePartialSpeechInput(String input) {
        mConversationView.updateLastUtterance(input, null);
    }

    private void handleSpeechInput(final String input) {
    	List<MorphoAnalysis> list = mTranslator.lookupMorpho(input);
    	if (list.size() == 0)
    		list = null;

        mConversationView.updateLastUtterance(input, list);
        new AsyncTask<Void,Void,String>() {
            @Override
            protected String doInBackground(Void... params) {
                return mTranslator.translate(input);
            }

            @Override
            protected void onPostExecute(String result) {
                outputText(result);
            }
        }.execute();
    }

    private void outputText(String text) {
        if (DBG) Log.d(TAG, "Speaking: " + text);
        mConversationView.addSecondPersonUtterance(text);
        if (!FAKE_SPEECH) {
            mTts.setLanguage(getTargetLanguageCode());
            mTts.speak(text);
        }
    }

    private class SpeechInputListener implements ASR.Listener {

        @Override
        public void onPartialInput(String input) {
            handlePartialSpeechInput(input);
        }

        @Override
        public void onSpeechInput(String input) {
            handleSpeechInput(input);
        }

        @Override
        public void onStateChanged(State newState) {
//            if (newState == ASR.State.IDLE) {
//                mStartStopButton.setImageResource(R.drawable.mic_idle);
//            } else {
//                mStartStopButton.setImageResource(R.drawable.mic_open);
//            }
        }
    }
}