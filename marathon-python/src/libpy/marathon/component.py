# Utilities to support accessing values from JComponents

from marathon.playback import *
from pawt import swing
import string
#import java

# JToggleButton

def getEnabledState(componentName):
	c = getComponent (componentName)
	return c.isEnabled()

def getTreeInfo(componentName):
	c = getComponent(componentName)
	treeNodes = getContent(componentName)
	model =  c.getModel()
	tree = swing.JTree(model)
	treeList = []
	treeStructList = []
	for i in range(len(treeNodes[0])):
		treeStruct = c.getPathForRow(i)
		Strg = treeStruct
		if type(treeStruct) != type(None):
			Strg = string.replace(str(treeStruct)[1:-1],", ","/")
		treeStructList.append(Strg)
	for i in range(len(treeNodes[0])):
		tree.expandRow(i)
		treeStruct = tree.getPathForRow(i)
		Strg = string.replace(str(treeStruct)[1:-1],", ","/")
		if Strg in treeStructList:
			Strg = '/' + Strg
		treeList.append(Strg)
	return treeList

def getColumnName(componentName, index):
    	c = getComponent(componentName)
	return c.getColumnName(index)
