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
			if(!floor[i].contains(".")){
				if(!floors.containsKey(building[i]))
					floors.put(building[i],new ArrayList<Floor>());
				floors.get(building[i]).add(floor_cache[i]=new Floor(i));
			}
		}
		final List<Building>_buildings=new ArrayList<Building>();
		for(String bldg:floors.keySet()){
			floors.put(bldg,Collections.unmodifiableList(floors.get(bldg)));
			_buildings.add(new Building(bldg));
		}
		buildings=Collections.unmodifiableList(_buildings);
		while(neighbours.size()<Campus.vertices.length)
			neighbours.add(new ArrayList<Integer>());
		for(int i=0;i<Campus.a_vertex.length;++i){
			final int u=vertexId.get(Campus.a_vertex[i]),v=vertexId.get(Campus.b_vertex[i]);
			if(u<v){
				neighbours.get(u).add(v);
				weights.get(u).add(Campus.weight[i]);
				neighbours.get(v).add(u);
				weights.get(v).add(Campus.weight[i]);
			}
		}
	}
	public static class Floor{
		protected final int id;
		protected Floor(int _id){
			id=_id;
		}
		public String getBuilding(){
			return longNames.get(building[id]);
		}
		public String getShortName(){
			return building[id];
		}
		public String getFloor(){
			return floor[id];
		}
	}
	public static class Building extends ArrayList<Floor>{
		private static final long serialVersionUID=-4240221880279433922L;
		protected Building(String shortName){
			super(floors.get(shortName));
		}
	}
	public List<Building>getBuildings(){
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
	public List<Floor>getPath(Floor start,Floor end){
		final ArrayList<Floor>ret=new ArrayList<Floor>();
		if(start.id!=end.id){
			float[]dist=new float[Campus.vertices.length];
			boolean[]done=new boolean[Campus.vertices.length];
			int[]prev=new int[Campus.vertices.length];
			for(int i=0;i<dist.length;++i)
				dist[i]=Float.MAX_VALUE;
			dist[start.id]=0;
			PriorityQueue<Pair>q=new PriorityQueue<Pair>();
			q.add(new Pair(0,start.id));
			while(!q.isEmpty()){
				final Pair p=q.remove();
				if(done[p.id])
					continue;
				done[p.id]=true;
				final List<Integer>neigh=neighbours.get(p.id);
				final List<Float>w=weights.get(p.id);
				for(int i=0,sz=neigh.size();i<sz;++i){
					final int u=neigh.get(i);
					if(!done[u]){
						float d=p.d+w.get(i);
						if(d<dist[u]){
							dist[u]=d;
							prev[u]=p.id;
							q.add(new Pair(d,u));
						}
					}
				}
			}
			for(int i=prev[end.id];i!=start.id;i=prev[i])
				ret.add(floor_cache[i]);
		}
		return Collections.unmodifiableList(ret);
	}
	public static void main(String[]args){
		
	}
}
