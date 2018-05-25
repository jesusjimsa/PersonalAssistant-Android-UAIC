package com.example.jesusjimsa.personalassistant;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.CalendarContract;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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

	@SuppressLint("SimpleDateFormat")
	DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	Date currentTime;
	String reportDate;

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
	public String event_title = "el título es (.*)";    // The title of an event can be anything
	public String what_time = "qué hora es";
	public String what_day = "qué día es( hoy)?";


	// Patterns and matchers
	//// Phone calls
	public Pattern num_call_pattern = Pattern.compile(number_call);
	public Matcher num_call_matcher;
	//// Calendar
	public Pattern event_date_pattern = Pattern.compile(event_date);
	public Matcher event_date_matcher;
	public Pattern event_title_pattern = Pattern.compile(event_title);
	public Matcher event_title_matcher;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
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
			public void onClick(View v) {
				try {
					startActivityForResult(recognizer_intent, REQ_CODE_SPEECH_INPUT);
				} catch (ActivityNotFoundException a) {
					Toast.makeText(getApplicationContext(), "Opps! Your device doesn’t support Speech to Text", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	protected void createEvent(String title, int month, int day) {
		Intent calIntent = new Intent(Intent.ACTION_INSERT);
		calIntent.setType("vnd.android.cursor.item/event");
		calIntent.putExtra(CalendarContract.Events.TITLE, title);
		calIntent.putExtra(CalendarContract.Events.DESCRIPTION, "Event created using PersonalAssistant");

		GregorianCalendar calDate = new GregorianCalendar(2018, month, day);
		calIntent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
		calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, calDate.getTimeInMillis());
		calIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, calDate.getTimeInMillis());

		startActivity(calIntent);
	}

	protected int monthToNumber(String month){
		int value = 0;

		switch(month){
			case "enero":
				value = 1;
				break;
			case "febrero":
				value = 2;
				break;
			case "marzo":
				value = 3;
				break;
			case "abril":
				value = 4;
				break;
			case "mayo":
				value = 5;
				break;
			case "junio":
				value = 6;
				break;
			case "julio":
				value = 7;
				break;
			case "agosto":
				value = 8;
				break;
			case "septiembre":
				value = 9;
				break;
			case "octubre":
				value = 10;
				break;
			case "noviembre":
				value = 11;
				break;
			case "diciembre":
				value = 12;
				break;
		}

		return value;
	}

	protected String splitDateTime(String date_time, boolean return_date){
		String[] splitted = date_time.split(" ");
		String result = "";

		if(return_date){
			result = splitted[0];
		}
		else{
			result = splitted[1];
		}

		return result;
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
		*
		*
		* */
		if(text.get(0).matches(hello)){
			text_assistant.add("Hola");
		}
		else{
			elses++;
		}

		/*
		* Phone calls
		*
		*
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
		*
		*
		* */
		if(text.get(0).matches(event)){
			text_assistant.add("¿Qué día quieres hacerlo?");
			creating_event_date = true;
		}
		else{
			elses++;
		}

		event_date_matcher = event_date_pattern.matcher(text.get(0));

		if(event_date_matcher.find() && creating_event_date) {
			text_assistant.add("De acuerdo, el" + event_date_matcher.group(2) + " de " + event_date_matcher.group(3) + ", ¿qué título le pongo?");

			num_date = Integer.parseInt(event_date_matcher.group(2));
			month_date = monthToNumber(event_date_matcher.group(3));

			creating_event_date = false;
			creating_event_title = true;
		}

		event_title_matcher = event_title_pattern.matcher(text.get(0));

		if(event_title_matcher.find() && creating_event_title){
			text_assistant.add("El evento " + event_title_matcher.group(1) + " se ha añadido al calendario");

			createEvent(event_title_matcher.group(1), month_date, num_date);

			creating_event_title = false;
		}

		/*
		* Time and date
		*
		*
		* */
		// Time
		if(text.get(0).matches(what_time)){
			currentTime = Calendar.getInstance().getTime();
			reportDate = df.format(currentTime);

			text_assistant.add(splitDateTime(reportDate, false));
		}

		// Date
		if(text.get(0).matches(what_day)){
			currentTime = Calendar.getInstance().getTime();
			reportDate = df.format(currentTime);

			text_assistant.add(splitDateTime(reportDate, true));
		}

		/*
		* Default answer
		*
		*
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
