package uwaterloo.enghack.edtalks;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import uwaterloo.enghack.edtalks.MapXmlParser.Building;
import uwaterloo.enghack.edtalks.MapXmlParser.Floor;
import uwaterloo.enghack.edtalks.MapXmlParser.Path;
import uwaterloo.enghack.edtalks.MapXmlParser.Venue;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class HomeScreenXml extends Activity {

	Spinner fromFloorSpinner;
	Spinner toFloorSpinner;
	Spinner fromSpinner;
	Spinner toSpinner;
	ListView directionsList;
	
	Venue venue;
	
	void CustomToast (String message) {
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(HomeScreenXml.this, message, duration);
		toast.show();
	}
	
	private OnItemSelectedListener itemSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			String selected = parent.getItemAtPosition(pos).toString();
			List<String> SpinnerArray =  new ArrayList<String>();
			if (selected.equalsIgnoreCase("Select a building") != true) {
				for(Floor f:venue.buildings[pos].floors){
					SpinnerArray.add(f.toString());
				}
			}
			else {
				SpinnerArray.add ("No building");
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(HomeScreenXml.this, android.R.layout.simple_spinner_item, SpinnerArray);
		    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			if (parent.getId() == R.id.fromSpinner) {
				fromFloorSpinner.setAdapter(adapter);
			} else {
				toFloorSpinner.setAdapter(adapter);
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {

		}
	};
	
	protected WeightedGraph<String, DefaultWeightedEdge> getGraph() {
		WeightedGraph<String, DefaultWeightedEdge> g =
                 new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
    	
		for(Building b:venue.buildings)
			for(Floor f:b.floors)
				g.addVertex(b.id+"/"+f.id);
		
		for(Path p:venue.paths){
			DefaultWeightedEdge edge = g.addEdge(p.a[0]+"/"+p.a[1], p.b[0]+"/"+p.b[1]);
			g.setEdgeWeight(edge, p.distance);
		}

		return g;
	}
    
    void populateListView (ListView lv, WeightedGraph g) {
    	final Pattern p=Pattern.compile("\\((.*)/(.*) : (.*)/(.*)\\)");
    	DijkstraShortestPath<String,DefaultWeightedEdge> path = new DijkstraShortestPath<String, DefaultWeightedEdge>(g, "1/3", "3/2");
    	String[] directions =  new String[path.getPathEdgeList().size()];
    	int i=0;
    	for(DefaultWeightedEdge dwe:path.getPath().getEdgeList()){
	    	final Matcher m=p.matcher(dwe.toString());
	    	m.find();
	    	Building b_1 = venue.buildingByID(Integer.valueOf(m.group(1)));
	    	Floor f_1 = b_1.floorById(Integer.valueOf(m.group(2)));
	    	Building b_2 = venue.buildingByID(Integer.valueOf(m.group(3)));
	    	Floor f_2 = b_1.floorById(Integer.valueOf(m.group(4)));

	    	directions[i++]=(b_1.shortname+" (Floor "+f_1.name+") to "+b_2.shortname+" (Floor "+f_2.name+")");
    	}
    	lv.setAdapter(new ArrayAdapter<String>(HomeScreenXml.this,android.R.layout.simple_list_item_1,directions));
    }

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_screen);

		MapXmlParser parser = new MapXmlParser();
		List<Venue> venues = null;
		try {
			venues = parser.parse(getResources().openRawResource(R.raw.maps2));
		} catch (Exception e){
			e.printStackTrace();
		}
		if(venues != null){
			venue = venues.get(0);
			CustomToast ("Venue is NOT null");
		} else {
			CustomToast ("Venue is null"); 
		}
		
		//WeightedGraph<String, DefaultWeightedEdge> g = getGraph();
		
		// Find Views
		fromSpinner = (Spinner) findViewById(R.id.fromSpinner);
		toSpinner = (Spinner) findViewById(R.id.toSpinner);
		fromFloorSpinner = (Spinner) findViewById(R.id.fromFloorSpinner);
		toFloorSpinner = (Spinner) findViewById(R.id.toFloorSpinner);
		directionsList = (ListView) findViewById(R.id.directionListView);

		// Set the Spinner Values
		//ArrayAdapter<Building> adapter = new ArrayAdapter<Building>(HomeScreenXml.this,android.R.layout.simple_spinner_item,venue.buildings);
		
		//adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		//fromSpinner.setAdapter(adapter);
		//toSpinner.setAdapter(adapter);

		fromSpinner.setOnItemSelectedListener(itemSelectedListener);
		toSpinner.setOnItemSelectedListener(itemSelectedListener);
	
		//populateListView (directionsList, g);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home_screen, menu);
		return true;
	}

}
