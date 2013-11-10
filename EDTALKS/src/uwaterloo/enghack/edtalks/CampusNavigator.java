package uwaterloo.enghack.edtalks;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import uwaterloo.enghack.edtalks.Campus;
public final class CampusNavigator{
	protected static final Map<String,Integer>vertexId=new HashMap<String,Integer>();
	protected static final Map<String,String>longNames=new HashMap<String,String>();
	protected static final Map<String,List<Floor>>floors=new HashMap<String,List<Floor>>();
	protected static final List<Building>buildings;
	protected static final String[]building,floor;
	protected static final Floor[]floor_cache;
	protected static final ArrayList<ArrayList<Integer>>neighbours=new ArrayList<ArrayList<Integer>>();
	protected static final ArrayList<ArrayList<Float>>weights=new ArrayList<ArrayList<Float>>();
	protected static final ArrayList<ArrayList<String>>types=new ArrayList<ArrayList<String>>();
	protected static final float FLOOR_WEIGHT=50f;
	static{
		for(int i=0;i<Campus.shortNames.length;++i)
			longNames.put(Campus.shortNames[i],Campus.longNames[i]);
		building=new String[Campus.vertices.length];
		floor=new String[Campus.vertices.length];
		floor_cache=new Floor[Campus.vertices.length];
		for(int i=0;i<Campus.vertices.length;++i){
			final String str=Campus.vertices[i];
			vertexId.put(str,i);
			building[i]=str.substring(0,str.indexOf('/'));
			floor[i]=str.substring(str.indexOf('/')+1);
			floor_cache[i]=new Floor(i);
			if(!floor[i].contains(".")){
				if(!floors.containsKey(building[i]))
					floors.put(building[i],new ArrayList<Floor>());
				floors.get(building[i]).add(floor_cache[i]);
			}
		}
		final List<Building>_buildings=new ArrayList<Building>();
		for(String bldg:floors.keySet()){
			floors.put(bldg,Collections.unmodifiableList(floors.get(bldg)));
			_buildings.add(new Building(bldg));
		}
		buildings=Collections.unmodifiableList(_buildings);
		while(neighbours.size()<Campus.vertices.length){
			neighbours.add(new ArrayList<Integer>());
			weights.add(new ArrayList<Float>());
			types.add(new ArrayList<String>());
		}
		for(int i=0;i<Campus.a_vertex.length;++i){
			final int u=vertexId.get(Campus.a_vertex[i]),v=vertexId.get(Campus.b_vertex[i]);
			neighbours.get(u).add(v);
			weights.get(u).add(Campus.weight[i]);
			types.get(u).add(Campus.weight[i]==0?"tunnel":"direct");
			neighbours.get(v).add(u);
			weights.get(v).add(Campus.weight[i]);
			types.get(v).add(Campus.weight[i]==0?"tunnel":"direct");
		}
		for(List<Floor>a:floors.values())
			for(int i=0;i+1<a.size();++i){
				final int u=a.get(i).id,v=a.get(i+1).id;
				neighbours.get(u).add(v);
				weights.get(u).add(FLOOR_WEIGHT);
				types.get(u).add("upstairs");
				neighbours.get(v).add(u);
				weights.get(v).add(FLOOR_WEIGHT);
				types.get(v).add("downstairs");
			}
	}
	public static class Floor{
		protected final int id;
		protected Floor(int _id){
			id=_id;
		}
		public String getLongName(){
			return longNames.get(building[id]);
		}
		public String getShortName(){
			return building[id];
		}
		public String getFloor(){
			return floor[id];
		}
		public String getName(){
			return getShortName()+"/"+getFloor();
		}
		public String toString(){
			return getFloor();
		}
	}
	public static class Direction{
		protected final Floor a,b;
		protected final String type;//direct,tunnel,upstairs,downstairs
		protected Direction(Floor _a,Floor _b,String _type){
			a=_a;
			b=_b;
			type=_type;
		}
		@Override public String toString(){
			StringBuilder instruction = new StringBuilder("     ");

			if("upstairs".equals(type)||"downstairs".equals(type)){ // same building
				instruction.append("Go "+type);
			}
			else { // different buildings
				if(type.equals("tunnel"))
					instruction.append("Take the tunnel");
				else instruction.append("Walk");
			}
			instruction.append(" to " + b.getShortName() + " floor "+ b.getFloor());
			return instruction.toString();
		}
	}
	public static class Building extends ArrayList<Floor>{
		private static final long serialVersionUID=-4240221880279433922L;
		protected final String name;
		protected Building(String shortName){
			super(floors.get(shortName));
			name=longNames.get(shortName);
		}
		@Override public String toString(){
			return name;
		}
	}
	public static List<Building>getBuildings(){
		return buildings;
	}
	protected static final class Pair implements Comparable<Pair>{
		protected final float d;
		protected final int id;
		protected Pair(float _d,int _id){
			d=_d;
			id=_id;
		}
		public boolean equals(Object obj){
			return obj instanceof Pair&&(d!=d?((Pair)obj).d!=((Pair)obj).d:((Pair)obj).d==d);
		}
		public int hashCode(){
			return Float.floatToRawIntBits(d);
		};
		public int compareTo(Pair o){
			return(d>o.d?1:0)-(d<o.d?1:0);
		}
	}
	public static List<Direction>getPath(Floor start,Floor end){
		final ArrayList<Direction>ret=new ArrayList<Direction>();
		float[]dist=new float[Campus.vertices.length];
		boolean[]done=new boolean[Campus.vertices.length];
		int[]prev=new int[Campus.vertices.length];
		String[]type=new String[Campus.vertices.length];
		for(int i=0;i<dist.length;++i)
			dist[i]=Float.POSITIVE_INFINITY;
		dist[start.id]=0;
		prev[start.id]=start.id;
		PriorityQueue<Pair>q=new PriorityQueue<Pair>();
		q.add(new Pair(0,start.id));
		while(!q.isEmpty()){
			final Pair p=q.remove();
			if(done[p.id])
				continue;
			done[p.id]=true;
			final List<Integer>neigh=neighbours.get(p.id);
			final List<Float>w=weights.get(p.id);
			final List<String>t=types.get(p.id);
			for(int i=0,sz=neigh.size();i<sz;++i){
				final int u=neigh.get(i);
				if(!done[u]){
					float d=p.d+w.get(i);
					if(d<dist[u]){
						dist[u]=d;
						prev[u]=p.id;
						type[u]=t.get(i);
						q.add(new Pair(d,u));
					}
				}
			}
		}
		if(Float.isInfinite(dist[end.id]))
			return null;
		String lastPos=end.getName();
		String lastType="direct";
		for(int i=end.id;i!=start.id;i=prev[i]){
			final int u=prev[i],v=i;
			String pos=Campus.vertices[u];
			if(pos.contains("."))
				pos=pos.substring(0,pos.indexOf('.'));
			if(type[v]!="direct")
				lastType=type[v];
			if(pos.equals(lastPos))
				continue;
			ret.add(new Direction(floor_cache[vertexId.get(pos)],floor_cache[vertexId.get(lastPos)],lastType));
			lastPos=pos;
			lastType="direct";
		}
		Collections.reverse(ret);
		return Collections.unmodifiableList(ret);
	}
	public static void main(String[]args){
		final List<Building>buildings=getBuildings();
		for(Building building:buildings)
			System.out.println(building);
		System.out.println("===========");
		final Floor src=buildings.get(39).get(1),dst=new Floor(vertexId.get("M3/01"));
		System.out.println(src+" to "+dst+": "+getPath(src,dst));
	}
}
