package com.example.jesusjimsa.personalassistant;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
	public int clickCounter = 0;
	public ArrayAdapter<String> adapter;
	public ArrayList<String> text_user = new ArrayList<>();
	public static final int REQ_CODE_SPEECH_INPUT = 1;
	public Intent recognizer_intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	public ArrayList<String> text = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button speak = findViewById(R.id.speak_button);
		ListView sent_to_assistant = findViewById(R.id.sent_to_assistant);

		// Adapter: You need three parameters 'the context, id of the layout (it will be where the data is shown),
		// and the array that contains the data
		adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.black_text_list, text_user);

		// Here, you set the data in your ListView
		sent_to_assistant.setAdapter(adapter);

		//SpeechRecognizer speech_recognizer = createSpeechRecognizer(this);

		recognizer_intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

		speak.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v){
				try {
					startActivityForResult(recognizer_intent, REQ_CODE_SPEECH_INPUT);
					//onActivityResult(REQ_CODE_SPEECH_INPUT, Activity.RESULT_OK, recognizer_intent);
				}
				catch (ActivityNotFoundException a) {
					Toast.makeText(getApplicationContext(), "Opps! Your device doesn’t support Speech to Text", Toast.LENGTH_SHORT).show();
				}


			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
			case REQ_CODE_SPEECH_INPUT: {
				if (resultCode == RESULT_OK && null != data) {
					text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

					text_user.add(text.get(0));

					if(text_user.size() > 9){
						text_user.remove(0);
					}

					// next thing you have to do is check if your adapter has changed
					adapter.notifyDataSetChanged();
				}

				break;
			}
		}
	}
}
