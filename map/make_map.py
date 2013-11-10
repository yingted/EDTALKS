#!/usr/bin/python
from json import load
from sys import argv
from os import listdir
from collections import namedtuple,defaultdict
from itertools import combinations,chain
import re
import Image
data_re=re.compile(r'(\d{3}([^_]*)_(\d{2}|B\d)FLR)\.json$')
Place=namedtuple('Place','bldg floor x y name')
Place.__str__=lambda self:'Place(..., name=\'%s\')'%self.name.encode('string-escape')
Link=namedtuple('Link','a b d')
def key(place):
	return place.floor
def floor_key(floor):
	return int(floor.lstrip('B'))*(-1)**floor.startswith('B')
def item_key((floor,_)):
	return floor_key(floor)
def dist(a,b):
	if a.bldg==b.bldg and a.floor==b.floor:
		return abs(a.x-b.x)+abs(a.y-b.y)
	return float('inf')
def sortargs(*args):
	return tuple(sorted(args))
data={}
places=defaultdict(lambda:defaultdict(list))
joints=defaultdict(lambda:defaultdict(list))
tunnels=defaultdict(lambda:defaultdict(list))
links=[]
for x in sorted(listdir(argv[1])):
	m=data_re.match(x)
	if m:
		with open(argv[1]+'/'+m.group(0))as f:
			data.update(load(f))#carry over
			size=Image.open('%s/%s.gif'%(argv[1],m.group(1))).size
			if data['rotate']:
				size=size[1],size[0]
			def fix(x):
				return float(x)*data['feet']/data['dx']
			ctr=Place(m.group(2),m.group(3),fix(size[0])/2,fix(size[1])/2,'%s/%s'%(m.group(2),m.group(3)))
			floor=places[ctr.bldg][ctr.floor]
			floor.append(ctr)
			for pt in data['points']:
				joint=ctr._replace(x=fix(pt['x']),y=fix(pt['y']),name='%s.%d'%(ctr.name,len(floor)))
				floor.append(joint)
				dst=pt['name'].upper()
				(tunnels if dst.startswith('/')else joints)[joint.bldg][dst.lstrip('/')].append(joint)
			links.extend(Link(a,b,dist(a,b))for a,b in combinations(floor,2))
			links.append(Link(joint,ctr,dist(joint,ctr)))
with open('tunnel_lengths.json')as f:
	tunnel_lengths={sortargs(a.upper(),b.upper()):d for a,b,d in load(f)}
for a in places.iterkeys():
	for b in places.iterkeys():
		for c in joints,tunnels:
			ab=c[a][b]
			ba=c[b][a]
			if len(ab)!=len(ba):
				if len(ab)+len(ba)==1:
					ab[:]=[]
					ba[:]=[]
					continue
				print'tunnel'if c is tunnels else 'joint ',a,b,[x.floor for x in ab],[x.floor for x in ba]#should not happen
				assert False
			links.extend([Link(u,v,0 if c is joints else tunnel_lengths[sortargs(u.bldg,v.bldg)])for u,v in zip(sorted(ab,key=key),sorted(ba,key=key))])
with open('gen/Campus.java','w')as f:
	f.write('''
	package uwaterloo.enghack.edtalks;
	public final class Campus{
		public static String[]buildings=%s;
		public static String[][]floors=%s;
		public static String[]vertices=%s;
		public static String[]a=%s;
		public static String[]a_floor=%s;
		public static String[]a_vertex=%s;
		public static String[]b=%s;
		public static String[]b_floor=%s;
		public static String[]b_vertex=%s;
		public static float[]weight=%s;
	}
	'''%tuple(str(x).replace("'",'"').replace('[','{').replace(']','}').replace(' ','')for x in(
		places.keys(),
		[sorted(places[y].keys(),key=floor_key)for y in places.iterkeys()],
		[w.name for y in places.itervalues()for z in sorted(y.iteritems(),key=item_key)for w in z[1]],
		[link.a.bldg for link in links],
		[link.a.floor for link in links],
		[link.b.name for link in links],
		[link.b.bldg for link in links],
		[link.b.floor for link in links],
		[link.a.name for link in links],
		[link.d for link in links],
	)))
from lxml.builder import E
flattened_places={bldg:[subfloor.name[len(bldg)+1:]for(floor,subfloors)in sorted(bldg_floors.iteritems(),key=item_key)for subfloor in subfloors]for bldg,bldg_floors in places.iteritems()}
xml=E.venues(
	E.venue(
		E.name('University of Waterloo'),
		id='1',
		*[
			E.building(
				E.name(bldg),
				E.shortname(bldg),
				id=str(i+1),
				*[
					(E.hiddenfloor if'.'in v else E.floor)(v,id=str(j+1))
					for j,v in enumerate(bldg_floors)
				]
			)
			for i,(bldg,bldg_floors)in enumerate(flattened_places.iteritems())
		]+[
			E.path(
				E.type('Indoor'),
				E.place(
					E.building(str(flattened_places.keys().index(link.a.bldg))),
					E.floor(str(flattened_places[link.a.bldg].index(link.a.name[len(link.a.bldg)+1:]))),
				),
				E.place(
					E.building(str(flattened_places.keys().index(link.b.bldg))),
					E.floor(str(flattened_places[link.b.bldg].index(link.b.name[len(link.b.bldg)+1:]))),
				),
				E.distance(str(link.d)),
			)
			for link in links
		]
	),
)
with open('gen/maps.xml','w')as f:
	xml.getroottree().write(f,encoding='utf-8',xml_declaration=True)
	#xml.getroottree().write(f,encoding='utf-8',xml_declaration=True,pretty_print=True)
