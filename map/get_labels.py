#!/usr/bin/python
from Tkinter import *
import Image, ImageTk
from sys import argv
from tkSimpleDialog import askstring
from json import dump,load
def show(data):
	lab=Label(root,text=data['name'],font=('Verdana',18),foreground='#0f0')
	labels.append(lab)
	lab.place(x=data['x'],y=data['y'])
def add(e):
	data={'x':e.x,'y':e.y,'name':askstring(argv[1],'destination')}
	out.append(data)
	show(data)
def quit(e):
	with open(argv[2],'w')as f:
		dump(out,f)
	root.destroy()
def clear(e):
	out[:]=[]
	for lab in labels:
		lab.place_forget()
		lab.destroy()
	labels[:]=[]
if __name__=="__main__":
	im=Image.open(argv[1])
	root=Tk()  
	tkimage=ImageTk.PhotoImage(im)
	Label(root,image=tkimage).pack() 
	labels=[]
	try:
		with open(argv[2])as f:
			out=load(f)
	except IOError:
		out=[]
	for data in out:
		show(data)
	root.bind("<Return>",add)
	root.bind("<space>",quit)
	root.bind("<Delete>",clear)
	root.mainloop()
