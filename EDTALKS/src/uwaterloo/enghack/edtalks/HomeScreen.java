package uwaterloo.enghack.edtalks;

import java.util.ArrayList;
import java.util.List;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import uwaterloo.enghack.edtalks.R;

public class HomeScreen extends Activity implements OnItemSelectedListener {

	Spinner fromFloorSpinner;
	Spinner toFloorSpinner;
	Spinner fromSpinner;
	Spinner toSpinner;
	ListView directionsList;

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		if(true)return;
		String selected = parent.getItemAtPosition(pos).toString();
		List<String> SpinnerArray =  new ArrayList<String>();
		if (selected.equalsIgnoreCase("Select a building") != true) {
			int floors = 0;//getResources().getIntArray(R.array.building_floors)[pos];
		}
		else {
			SpinnerArray.add ("No building");
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(HomeScreen.this, android.R.layout.simple_spinner_item, SpinnerArray);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		if (parent.getId() == R.id.fromSpinner) {
			fromFloorSpinner.setAdapter(adapter);
		} else {
			toFloorSpinner.setAdapter(adapter);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_screen);

		// Find Views
		fromSpinner = (Spinner) findViewById(R.id.fromSpinner);
		toSpinner = (Spinner) findViewById(R.id.toSpinner);
		fromFloorSpinner = (Spinner) findViewById(R.id.fromFloorSpinner);
		toFloorSpinner = (Spinner) findViewById(R.id.toFloorSpinner);
		directionsList = (ListView) findViewById(R.id.directionListView);
if(true)return;
		// Set the Spinner Values
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
		
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		fromSpinner.setAdapter(adapter);
		toSpinner.setAdapter(adapter);

		fromSpinner.setOnItemSelectedListener(this);
		toSpinner.setOnItemSelectedListener(this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home_screen, menu);
		return true;
	}

}
