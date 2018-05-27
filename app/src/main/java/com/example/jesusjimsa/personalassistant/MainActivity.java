package com.example.jesusjimsa.personalassistant;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

	public boolean asking_phone_number = false;
	public boolean creating_event_date = false;
	public boolean creating_event_title = false;
	public boolean email_to_who = false;
	public boolean email_about_what = false;
	public boolean email_saying_what = false;

	public int num_date;
	public int month_date;
	public String email_address;
	public String email_subject;
	public String email_content;


	@SuppressLint("SimpleDateFormat")
	DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	Date currentTime;
	String reportDate;

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
	public String open_web = "abre (.*)(\\.com|\\.es|\\.ro|\\.org)";
	public String open_app = "abre ([a-zA-Z]+)";
	public String new_email = "nuevo correo|escribir correo|nuevo email|escribir email";
	public String email_to = "(.*) arroba (.*)(\\.com|\\.es|\\.ro)";
	public String email_subject_re = "el asunto es (.*)";
	public String email_content_re = "el contenido es (.*)";
	public String weather = "qué tiempo hace(.*)|hace sol(.*)|está lloviendo(.*)|está nevando(.*)|necesito el paraguas(.*)";
	public String route = "llévame a (.*)|muéstrame la ruta a (.*)|cómo se va a (.*)";
	public String route_from = "llévame de (.*) a (.*)|muéstrame la ruta de (.*) a (.*)|cómo se va de (.*) a (.*)";

	// Patterns and matchers
	//// Phone calls
	public Pattern num_call_pattern = Pattern.compile(number_call);
	public Matcher num_call_matcher;
	//// Calendar
	public Pattern event_date_pattern = Pattern.compile(event_date);
	public Matcher event_date_matcher;
	public Pattern event_title_pattern = Pattern.compile(event_title);
	public Matcher event_title_matcher;
	//// Apps
	public Pattern open_app_pattern = Pattern.compile(open_app);
	public Matcher open_app_matcher;
	//// E-mail
	public Pattern email_to_pattern = Pattern.compile(email_to);
	public Matcher email_to_matcher;
	public Pattern email_subject_pattern = Pattern.compile(email_subject_re);
	public Matcher email_subject_matcher;
	public Pattern email_content_pattern = Pattern.compile(email_content_re);
	public Matcher email_content_matcher;
	//// Webs
	public Pattern open_web_pattern = Pattern.compile(open_web);
	public Matcher open_web_matcher;
	//// Maps
	public Pattern route_pattern = Pattern.compile(route);
	public Matcher route_matcher;
	public Pattern route_from_pattern = Pattern.compile(route_from);
	public Matcher route_from_matcher;

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

	protected void phoneCall(String number){
		launchIntent = new Intent(Intent.ACTION_CALL);
		launchIntent.setData(Uri.parse("tel:" + number));
		startActivity(launchIntent);
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

	protected String openApp(String app_name){
		String response;

		switch(app_name){
			case "Facebook":
				response = "Abriendo Facebook...";

				launchIntent = getPackageManager().getLaunchIntentForPackage("com.facebook.katana");
				startActivity(launchIntent);
				break;
			case "Twitter":
				response = "Abriendo Twitter...";

				launchIntent = getPackageManager().getLaunchIntentForPackage("com.twitter.android");
				startActivity(launchIntent);
				break;
			case "WhatsApp":
				response = "Abriendo WhatsApp...";

				launchIntent = getPackageManager().getLaunchIntentForPackage("com.whatsapp");
				startActivity(launchIntent);
				break;
			case "telegram":
				response = "Abriendo Telegram...";

				launchIntent = getPackageManager().getLaunchIntentForPackage("org.telegram.messenger");
				startActivity(launchIntent);
				break;
			case "Wunderlist":
				response = "Abriendo Wunderlist...";

				launchIntent = getPackageManager().getLaunchIntentForPackage("com.wunderkinder.wunderlistandroid");
				startActivity(launchIntent);
				break;
			case "Outlook":
				response = "Abriendo Outlook...";

				launchIntent = getPackageManager().getLaunchIntentForPackage("com.microsoft.office.outlook");
				startActivity(launchIntent);
			case "Google fotos":
				response = "Abriendo Google Fotos...";

				launchIntent = getPackageManager().getLaunchIntentForPackage("com.google.android.apps.photos");
				startActivity(launchIntent);
				break;
			case "Flipboard":
				response = "Abriendo Flipboard...";

				launchIntent = getPackageManager().getLaunchIntentForPackage("flipboard.app");
				startActivity(launchIntent);
				break;
			case "Chrome":
				response = "Abriendo Google Chrome...";

				launchIntent = getPackageManager().getLaunchIntentForPackage("com.android.chrome");
				startActivity(launchIntent);
				break;
			case "Instagram":
				response = "Abriendo Instagram...";

				launchIntent = getPackageManager().getLaunchIntentForPackage("com.instagram.android");
				startActivity(launchIntent);
				break;
			case "Messenger":
				response = "Abriendo Facebook Messenger...";

				launchIntent = getPackageManager().getLaunchIntentForPackage("com.facebook.orca");
				startActivity(launchIntent);
				break;
			case "Shazam":
				response = "Abriendo Shazam...";

				launchIntent = getPackageManager().getLaunchIntentForPackage("com.shazam.android");
				startActivity(launchIntent);
				break;
			case "Spotify":
				response = "Abriendo Spotify...";

				launchIntent = getPackageManager().getLaunchIntentForPackage("com.spotify.music");
				startActivity(launchIntent);
				break;
			case "Gmail":
				response = "Abriendo Gmail...";

				launchIntent = getPackageManager().getLaunchIntentForPackage("com.google.android.gm");
				startActivity(launchIntent);
				break;
			case "calendar":
				response = "Abriendo Google Calendar...";

				launchIntent = getPackageManager().getLaunchIntentForPackage("com.google.android.calendar");
				startActivity(launchIntent);
				break;
			case "drive":
				response = "Abriendo Google Drive...";

				launchIntent = getPackageManager().getLaunchIntentForPackage("com.google.android.apps.docs");
				startActivity(launchIntent);
				break;
			case "Maps":
				response = "Abriendo Google Maps...";

				launchIntent = getPackageManager().getLaunchIntentForPackage("com.google.android.apps.maps");
				startActivity(launchIntent);
				break;
			case "traductor":
				response = "Abriendo Google Translate...";

				launchIntent = getPackageManager().getLaunchIntentForPackage("com.google.android.apps.translate");
				startActivity(launchIntent);
				break;
			case "YouTube":
				response = "Abriendo YouTube...";

				launchIntent = getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
				startActivity(launchIntent);
				break;
			case "OneNote":
				response = "Abriendo OneNote...";

				launchIntent = getPackageManager().getLaunchIntentForPackage("com.microsoft.office.onenote");
				startActivity(launchIntent);
				break;
			case "PayPal":
				response = "Abriendo PayPal...";

				launchIntent = getPackageManager().getLaunchIntentForPackage("com.paypal.android.p2pmobile");
				startActivity(launchIntent);
				break;
			case "LastPass":
				response = "Abriendo LastPass...";

				launchIntent = getPackageManager().getLaunchIntentForPackage("com.lastpass.lpandroid");
				startActivity(launchIntent);
				break;
			case "Netflix":
				response = "Abriendo Netflix...";

				launchIntent = getPackageManager().getLaunchIntentForPackage("com.netflix.mediaclient");
				startActivity(launchIntent);
				break;
			case "Dropbox":
				response = "Abriendo Drobpox...";

				launchIntent = getPackageManager().getLaunchIntentForPackage("com.dropbox.android");
				startActivity(launchIntent);
				break;
			default:
				response = "No puedo abrir esa aplicación todavía";
				break;
		}

		return response;
	}

	protected void sendEmail(String address, String subject, String content){
		Log.i("Send email", "");

		String[] TO = {address};
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setData(Uri.parse("mailto:"));
		emailIntent.setType("text/plain");


		emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(Intent.EXTRA_TEXT, content);

		try {
			startActivity(Intent.createChooser(emailIntent, "Send mail..."));
			finish();
			Log.i("Finished sending email", "");
		}
		catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(MainActivity.this,
					"There is no email client installed.", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		String text_for_assistant = DEFAULT_MESSAGE;

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
			text_for_assistant = "Hola";
		}

		/*
		* Phone calls
		*
		*
		* */
		if(text.get(0).matches(phone_call)){
			text_for_assistant = "Dime el número al que quieres llamar";
			asking_phone_number = true;
		}

		if(text.get(0).matches(phone_number) && asking_phone_number){
			text_for_assistant = "Llamando a " + text.get(0);
			asking_phone_number = false;

			phoneCall(text.get(0));
		}

		num_call_matcher = num_call_pattern.matcher(text.get(0));

		if(num_call_matcher.find()){
			text_for_assistant = "Llamando a " + num_call_matcher.group(2);

			phoneCall(num_call_matcher.group(2));
		}

		/*
		* Events
		*
		*
		* */
		if(text.get(0).matches(event)){
			text_for_assistant = "¿Qué día quieres hacerlo?";
			creating_event_date = true;
		}

		event_date_matcher = event_date_pattern.matcher(text.get(0));

		if(event_date_matcher.find() && creating_event_date) {
			text_for_assistant = "De acuerdo, el" + event_date_matcher.group(2) + " de " + event_date_matcher.group(3) + ", ¿qué título le pongo?";

			num_date = Integer.parseInt(event_date_matcher.group(2));
			month_date = monthToNumber(event_date_matcher.group(3));

			creating_event_date = false;
			creating_event_title = true;
		}

		event_title_matcher = event_title_pattern.matcher(text.get(0));

		if(event_title_matcher.find() && creating_event_title){
			text_for_assistant = "El evento " + event_title_matcher.group(1) + " se ha añadido al calendario";

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

			text_for_assistant = splitDateTime(reportDate, false);
		}

		// Date
		if(text.get(0).matches(what_day)){
			currentTime = Calendar.getInstance().getTime();
			reportDate = df.format(currentTime);

			text_for_assistant = splitDateTime(reportDate, true);
		}

		/*
		* Open apps
		*
		*
		* */
		open_app_matcher = open_app_pattern.matcher(text.get(0));

		if(open_app_matcher.find()){
			text_for_assistant = openApp(open_app_matcher.group(1));
		}

		/*
		* E-mail
		*
		*
		* */
		if(text.get(0).matches(new_email)){
			text_for_assistant = "¿A quién se lo quieres enviar?";

			email_to_who = true;
		}

		email_to_matcher = email_to_pattern.matcher(text.get(0));

		if(email_to_matcher.find() && email_to_who){
			// email = user + @ + provider + domain
			email_address = email_to_matcher.group(1).toLowerCase() + "@" + email_to_matcher.group(2).toLowerCase() + email_to_matcher.group(3).toLowerCase();

			text_for_assistant = "¿Cuál es el asunto?";

			email_to_who = false;
			email_about_what = true;
		}

		email_subject_matcher = email_subject_pattern.matcher(text.get(0));

		if(email_subject_matcher.find() && email_about_what){
			email_subject = email_subject_matcher.group(1);

			text_for_assistant = "¿Qué quieres decir en el correo?";

			email_about_what = false;
			email_saying_what = true;
		}

		email_content_matcher = email_content_pattern.matcher(text.get(0));

		if(email_content_matcher.find() && email_saying_what){
			email_content = email_content_matcher.group(1);

			text_for_assistant = "Correo para " + email_to + "enviado";

			email_saying_what = false;

			sendEmail(email_address, email_subject, email_content);
		}

		/*
		* Weather
		*
		*
		* */
		if(text.get(0).matches(weather)){
			launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?&q=what%27s%20the%20weather%20like"));
			startActivity(launchIntent);

			text_for_assistant = "Google lo sabrá mejor que yo";
		}

		/*
		* Open web
		*
		*
		* */
		open_web_matcher = open_web_pattern.matcher(text.get(0));

		if(open_web_matcher.find()){
			launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www." + open_app_matcher.group(1) + open_web_matcher.group(2)));
			startActivity(launchIntent);

			text_for_assistant = "Eso está hecho";
		}

		/*
		* Maps
		*
		*
		* */
		route_matcher = route_pattern.matcher(text.get(0));

		if(route_matcher.find()){
			text_for_assistant = "A la orden";

			if(route_matcher.group(1) != null){
				launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.es/maps?&q=ruta a " + route_matcher.group(1)));
			}
			else{
				if(route_matcher.group(2) != null){
					launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.es/maps?&q=ruta a " + route_matcher.group(2)));
				}
				else{
					if(route_matcher.group(3) != null){
						launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.es/maps?&q=ruta a " + route_matcher.group(3)));
					}
				}
			}

			startActivity(launchIntent);
		}

		route_from_matcher = route_from_pattern.matcher(text.get(0));

		if(route_from_matcher.find()){
			text_for_assistant = "A la orden";

			if(route_from_matcher.group(1) != null){
				launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.es/maps?&q=ruta de " + route_from_matcher.group(1) + " a " + route_from_matcher.group(2)));
			}
			else{
				if(route_from_matcher.group(3) != null){
					launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.es/maps?&q=ruta de " + route_from_matcher.group(3) + " a " + route_from_matcher.group(4)));
				}
				else{
					if(route_from_matcher.group(5) != null){
						launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.es/maps?&q=ruta de " + route_from_matcher.group(5) + " a " + route_from_matcher.group(6)));
					}
				}
			}

			startActivity(launchIntent);
		}

		/*
		* Display results
		*
		*
		* */
		text_assistant.add(text_for_assistant);
		text_user.add("");

		if(text_user.size() > 9){
			text_user.remove(0);
		}

		if(text_assistant.size() > 9){
			text_assistant.remove(0);
		}

		adapter_assistant.notifyDataSetChanged();
	}

	@Override
	public void onStart(){
		// call the superclass method first
		super.onStart();
	}

	@Override
	public void onResume() {
		// Always call the superclass method first
		super.onResume();
	}

	@Override
	public void onPause() {
		// Always call the superclass method first
		super.onPause();
	}

	@Override
	protected void onStop() {
		// Always call the superclass method first
		super.onStop();
	}

	@Override
	public void onDestroy(){
		// Always call the superclass method first
		super.onDestroy();
	}
}
