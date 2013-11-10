package uwaterloo.enghack.edtalks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import uwaterloo.enghack.edtalks.CampusNavigator.Direction;
import uwaterloo.enghack.edtalks.CampusNavigator.Floor;
import uwaterloo.enghack.edtalks.R;
import uwaterloo.enghack.edtalks.CampusNavigator.Building;

public class HomeScreen extends Activity implements OnItemSelectedListener {

	Spinner fromFloorSpinner;
	Spinner toFloorSpinner;
	Spinner fromSpinner;
	Spinner toSpinner;
	ListView directionsList;
	private int nr_selected=0;

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		Building building = (Building)parent.getItemAtPosition(pos);
		ArrayAdapter<Floor> adapter = new ArrayAdapter<Floor>(HomeScreen.this, android.R.layout.simple_spinner_item, building);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    final Spinner sp=parent.getId() == R.id.fromSpinner?fromFloorSpinner:toFloorSpinner;
		sp.setAdapter(adapter);
		if(nr_selected>1)
			sp.performClick();
		else
			++nr_selected;
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_screen);

		// Find Views
		fromSpinner = (Spinner) findViewById(R.id.fromSpinner);
		toSpinner = (Spinner) findViewById(R.id.toSpinner);
		fromFloorSpinner = (Spinner) findViewById(R.id.fromFloorSpinner);
		toFloorSpinner = (Spinner) findViewById(R.id.toFloorSpinner);
		directionsList = (ListView) findViewById(R.id.directionListView);

		// Set the Spinner Values
		final ArrayList<Building>items=new ArrayList<Building>(CampusNavigator.getBuildings());
		Collections.sort(items,new Comparator<Building>(){
			@Override public int compare(Building lhs,Building rhs){
				return lhs.toString().compareTo(rhs.toString());
			}
		});
		
		ArrayAdapter<Building> adapter = new ArrayAdapter<Building>(this, android.R.layout.simple_spinner_item, items);
		
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		fromSpinner.setOnItemSelectedListener(this);
		toSpinner.setOnItemSelectedListener(this);

		fromSpinner.setAdapter(adapter);
		toSpinner.setAdapter(adapter);

		final OnItemSelectedListener updateListener=new OnItemSelectedListener(){
			@Override public void onItemSelected(AdapterView<?> viewParent,View view,int pos,long id){
				Floor from=(Floor)fromFloorSpinner.getSelectedItem(),to=(Floor)toFloorSpinner.getSelectedItem();
				final List<Direction>path=CampusNavigator.getPath(from,to);
				if(path==null){
					directionsList.setAdapter(new ArrayAdapter<CharSequence>(HomeScreen.this,android.R.layout.simple_list_item_1,new String[]{"sorry!"}));
					return;
				}
				directionsList.setAdapter(new ArrayAdapter<Direction>(HomeScreen.this,android.R.layout.simple_list_item_1,path));
			}

			@Override public void onNothingSelected(AdapterView<?> arg0){}
		};
		fromFloorSpinner.setOnItemSelectedListener(updateListener);
		toFloorSpinner.setOnItemSelectedListener(updateListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home_screen, menu);
		return true;
	}

}
