package uwaterloo.enghack.edtalks;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.res.XmlResourceParser;
import android.graphics.Path;
import android.util.Xml;

public class MapXmlParser {
	private static final String ns = null;
	
	public List<Venue> parse(InputStream in) throws XmlPullParserException,
			IOException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return readVenues(parser);
		} finally {
			in.close();
		}
	}

	private List<Venue> readVenues(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		List<Venue> venues = new ArrayList<Venue>();
		parser.require(XmlPullParser.START_TAG, ns, "venues");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equals("venue")) {
				venues.add(readVenue(parser));
			} else {
				skip(parser);
			}
		}
		parser.require(XmlPullParser.END_TAG, ns, "venues");
		return venues;
	}

	public static class Venue {
		public final int id;
		public final String name;
		public final Building[] buildings;
		public final Path[] paths;

		private Venue(int id, String name, Building[] buildings, Path[] paths) {
			this.id = id;
			this.name = name;
			this.buildings = buildings;
			this.paths = paths;
		}

		public Building buildingByID(int id) {
			for (Building b : buildings) {
				if (b.id == id)
					return b;
			}
			return null;
		}
	}

	private Venue readVenue(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "venue");

		int id = Integer.valueOf(parser.getAttributeValue(null, "id"));
		String name = null;
		List<Building> buildings = new ArrayList<Building>();
		List<Path> paths = new ArrayList<Path>();

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String n = parser.getName();
			if (n.equals("name"))
				name = readText(parser);
			else if (n.equals("building"))
				buildings.add(readBuilding(parser));
			else if (n.equals("path"))
				paths.add(readPath(parser));
			else
				skip(parser);
		}
		Building[] buildings_array = new Building[buildings.size()];
		buildings_array = buildings.toArray(buildings_array);

		Path[] paths_array = new Path[paths.size()];
		paths_array = paths.toArray(paths_array);

		return new Venue(id, name, buildings_array, paths_array);
	}

	private String readText(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}

	public static class Building {
		public final int id;
		public final String name;
		public final String shortname;
		public final Floor[] floors;

		private Building(int id, String name, String shortname, Floor[] floors) {
			this.id = id;
			this.name = name;
			this.shortname = shortname;
			this.floors = floors;
		}

		public Floor floorById(int id) {
			for (Floor f : floors)
				if (f.id == id)
					return f;
			return null;
		}
		
		public String toString(){
			return shortname + " (" + name + ")";
		}
	}

	private Building readBuilding(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "building");

		int id = Integer.valueOf(parser.getAttributeValue(null, "id"));
		String name = null;
		String shortname = null;
		List<Floor> floors = new ArrayList<Floor>();

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String n = parser.getName();
			if (n.equals("name"))
				name = readText(parser);
			else if (n.equals("shortname"))
				shortname = readText(parser);
			else if (n.equals("floor")) {
				floors.add(readFloor(parser));
			} else
				skip(parser);
		}

		Floor[] floors_array = new Floor[floors.size()];
		floors_array = floors.toArray(floors_array);

		return new Building(id, name, shortname, floors_array);
	}

	public static class Floor {
		public final int id;
		public final String name;

		private Floor(int id, String name) {
			this.id = id;
			this.name = name;
		}
		public String toString(){
			return name;
		}
	}

	private Floor readFloor(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "floor");

		int id = Integer.valueOf(parser.getAttributeValue(null, "id"));
		String name = readText(parser);

		parser.require(XmlPullParser.END_TAG, ns, "floor");

		return new Floor(id, name);
	}

	public static class Path {
		String type;
		int [] a, b;
		double distance;

		private Path(String type, int [] a, int [] b, double distance) {
			this.type = type;
			this.a = a;
			this.b = b;
			this.distance = distance;
		}
	}

	private Path readPath(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "path");

		String type = null;
		boolean start = true;
		int[] a = null, b = null;
		double distance = 0.0;

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String n = parser.getName();
			if (n.equals("type"))
				type = readText(parser);
			else if (n.equals("place")) {
				int [] p = readPlace(parser);
				if (start) {
					a = p;
					start = false;
				} else {
					b = p;
				}
			} else if (n.equals("distance"))
				distance = Double.valueOf(readText(parser));
			else
				skip(parser);
		}

		parser.require(XmlPullParser.END_TAG, ns, "path");
		return new Path(type, a, b, distance);
	}
	
	private int [] readPlace(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "place");
		int building = 0, floor = 0;
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String n = parser.getName();
			if (n.equals("building"))
				building = Integer.valueOf(readText(parser));
			else if (n.equals("floor"))
				floor = Integer.valueOf(readText(parser));
			else
				skip(parser);
		}
		parser.require(XmlPullParser.END_TAG, ns, "place");
		int [] p = {building, floor};
		return p;
	}

	private void skip(XmlPullParser parser) throws XmlPullParserException,
			IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}

}
