package com.example.jesusjimsa.personalassistant;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";

	public int clickCounter = 0;
	public ArrayAdapter<String> adapter;
	public ArrayList<String> arrayList = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.v(TAG, "onCreate: 20");

		Button speak = findViewById(R.id.speak_button);
		ListView sent_to_assistant = findViewById(R.id.sent_to_assistant);
		Log.v(TAG, "onCreate: 24");

		// Adapter: You need three parameters 'the context, id of the layout (it will be where the data is shown),
		// and the array that contains the data
		adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.black_text_list, arrayList);
		Log.v(TAG, "onCreate: 29");

		// Here, you set the data in your ListView
		sent_to_assistant.setAdapter(adapter);
		Log.v(TAG, "onCreate: 33");

		speak.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v){
				clickCounter++;

				arrayList.add("Clicked " + clickCounter + " times");

				if(arrayList.size() > 9){
					arrayList.remove(0);
				}

				// next thing you have to do is check if your adapter has changed
				adapter.notifyDataSetChanged();
			}
		});
		Log.v(TAG, "onCreate: 45");
	}
}
