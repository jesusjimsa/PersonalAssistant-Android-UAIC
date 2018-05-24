package com.example.jesusjimsa.personalassistant;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
	public ArrayAdapter<String> adapter_user, adapter_assistant;
	public ArrayList<String> text_user = new ArrayList<>();
	public ArrayList<String> text_assistant = new ArrayList<>();
	public static final int REQ_CODE_SPEECH_INPUT = 1;
	public Intent recognizer_intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	public ArrayList<String> text = new ArrayList<>();
	public Intent launchIntent = new Intent();

	public Boolean asking_phone_number = false;
	public Boolean creating_event_date = false;
	public Boolean creating_event_title = false;

	public int num_date;
	public int month_date;

	/*
	* Each time a regex is not matched elses increases by 1.
	* When the method gets to the end, checks the value of elses with the number of
	* if expressions that have been ignored, if it's the maximum number, the default
	* message is sent to the user.
	* */
	public int elses = 0;
	public static final String DEFAULT_MESSAGE = "Lo siento, no te he entendido";

	// Regular expressions
	public String hello = "(hola|saludos)(.*)";
	public String phone_call = "(.*)(quiero hacer una llamada)(.*)";
	public String phone_number = "[0-9]{9}|[0-9]{3} [0-9]{3} [0-9]{3}";
	public String number_call = "(llama al )([0-9]{9}|[0-9]{3} [0-9]{3} [0-9]{3})";
	public String event = "añade un evento|crea un evento|nuevo evento";
	public String months = "(enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|octubre|noviembre|diciembre)";
	public String event_date = "(el )?([1-9]|1[0-9]|2[0-9]|30|31) de " + months;
	public String event_title = ".*";	// The title of an event can be anything


	// Patterns and matchers
	//// Phone calls
	public Pattern num_call_pattern = Pattern.compile(number_call);
	public Matcher num_call_matcher;
	//// Calendar

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button speak = findViewById(R.id.speak_button);
		ListView sent_to_assistant = findViewById(R.id.sent_to_assistant);
		ListView assistant_response = findViewById(R.id.assitant_response);

		// Adapter: You need three parameters 'the context, id of the layout (it will be where the data is shown),
		// and the array that contains the data
		adapter_user = new ArrayAdapter<>(getApplicationContext(), R.layout.black_text_list, text_user);
		adapter_assistant = new ArrayAdapter<>(getApplicationContext(), R.layout.black_text_list, text_assistant);

		// Here, you set the data in your ListView
		sent_to_assistant.setAdapter(adapter_user);
		assistant_response.setAdapter(adapter_assistant);

		recognizer_intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

		speak.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v){
				try {
					startActivityForResult(recognizer_intent, REQ_CODE_SPEECH_INPUT);
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

					// The element 0 is the one Google thinks is the best option of all of what it has recognized
					text_user.add(text.get(0));
					text_assistant.add("");

					// next thing you have to do is check if your adapter_user has changed
					adapter_user.notifyDataSetChanged();
				}

				break;
			}
		}

		/*
		* Saying hello
		* */
		if(text.get(0).matches(hello)){
			text_assistant.add("Hola" + hello);
		}
		else{
			elses++;
		}

		/*
		* Phone calls
		* */
		if(text.get(0).matches(phone_call)){
			text_assistant.add("Dime el número al que quieres llamar");
			asking_phone_number = true;
		}
		else{
			elses++;
		}

		if(text.get(0).matches(phone_number) && asking_phone_number){
			text_assistant.add("Llamando a " + text.get(0));
			asking_phone_number = false;

			launchIntent = new Intent(Intent.ACTION_CALL);
			launchIntent.setData(Uri.parse("tel:" + text.get(0)));
			startActivity(launchIntent);
		}

		num_call_matcher = num_call_pattern.matcher(text.get(0));

		if(num_call_matcher.find()){
			text_assistant.add("Llamando a " + num_call_matcher.group(2));

			launchIntent = new Intent(Intent.ACTION_CALL);
			launchIntent.setData(Uri.parse("tel:" + num_call_matcher.group(2)));
			startActivity(launchIntent);
		}
		else{
			elses++;
		}

		/*
		* Events
		* */
		if(text.get(0).matches(event)){
			text_assistant.add("¿Qué día quieres hacerlo?");
			creating_event_date = true;
		}
		else{
			elses++;
		}

		if(text.get(0).matches(event_date) && creating_event_date) {
			text_assistant.add(text.get(0));
			creating_event_date = false;
			creating_event_title = true;
		}

		if(text.get(0).matches(event_title) && creating_event_title){
			text_assistant.add("El evento " + text.get(0) + "se ha añadido al calendario");
			creating_event_title = false;
		}

		/*
		* Default answer
		* */
		if(elses == 555555550){
			text_assistant.add(DEFAULT_MESSAGE);
		}
		else{
			elses = 0;
		}

		text_user.add("");

		if(text_user.size() > 9){
			text_user.remove(0);
		}

		if(text_assistant.size() > 9){
			text_assistant.remove(0);
		}

		adapter_assistant.notifyDataSetChanged();
	}
}
