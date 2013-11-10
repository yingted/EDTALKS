#!/usr/bin/python
from Tkinter import *
import Image, ImageTk
from sys import argv
from tkSimpleDialog import askstring,askfloat
from json import dump,load
def show(data):
	lab=Label(root,text=data['name'],font=('Verdana',18),foreground='#0f0')
	labels.append(lab)
	lab.place(x=data['x'],y=data['y'])
def add(e):
	data={'x':e.x,'y':e.y,'name':askstring(argv[1],'destination')}
	out['points'].append(data)
	show(data)
def quit(e):
	with open(argv[2],'w')as f:
		dump(out,f)
	root.destroy()
def clear(e):
	out['points'][:]=[]
	for lab in labels:
		lab.place_forget()
		lab.destroy()
	labels[:]=[]
def showall():
	for data in out['points']:
		show(data)
x=None
def scale_start(e):
	global x
	x=e.x
def scale_end(e):
	global y
	out['dx']=e.x-x
	out['feet']=askfloat(argv[1],'feet')
tkimage=None
def rotate(e,user=True):
	global tkimage
	if user:
		out['rotate']=(out['rotate']+1)%4
	im=Image.open(argv[1]).rotate(out['rotate']*90)
	if user:
		w,h=im.size
		new=[{'x':pt['y'],'y':h-pt['x'],'name':pt['name']}for pt in out['points']]
		clear(None)
		out['points']=new
		showall()
	tkimage=ImageTk.PhotoImage(im)
	lab.configure(image=tkimage)
	lab.pack() 

root=Tk()
lab=Label(root)
labels=[]
try:
	with open(argv[2])as f:
		out=load(f)
except IOError:
	out={'rotate':0,'points':[]}
rotate(None,False)
showall()
root.bind("<Return>",add)
root.bind("<space>",quit)
root.bind("<Delete>",clear)
root.bind("<Delete>",clear)
root.bind("r",rotate)
root.bind("<Button-1>",scale_start)
root.bind("<ButtonRelease-1>",scale_end)
root.mainloop()
