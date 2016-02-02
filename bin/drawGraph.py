#!/usr/bin/env python
"""
Draw a graph with matplotlib.
You must have matplotlib for this to work.
"""
__author__ = """Aric Hagberg (hagberg@lanl.gov)"""
try:
    import matplotlib.pyplot as plt
except:
    raise
import time 
import networkx as nx

def drawGraph(pos,G,abnormalNodes,normalNodes):
    # explicitly set positions
    print G
    nx.draw_networkx_nodes(G,pos,node_size=100,nodelist=normalNodes,node_color='b')
    nx.draw_networkx_nodes(G,pos,node_size=100,nodelist=abnormalNodes,node_color='r')
    nx.draw_networkx_edges(G,pos,alpha=0.5,width=6)
    plt.axis('off')
    plt.show() # display
    plt.close()
    
def draw_100(abnormalNodes):
    pos = dict()
    count = 0
    for i in range(10):
        for j in range(10):
            t = (i,j)
            pos[count] = t
            count = count + 1
    for item in pos:
        print item,pos[item]
    G = nx.Graph()
    for i in range(100):
        G.add_node(i)
    normalNodes = [i for i in range(100) if i not in abnormalNodes]
    drawGraph(pos,G,abnormalNodes,normalNodes)
    
def draw_400(abnormalNodes):
    pos = dict()
    count = 0
    for i in range(20):
        for j in range(20):
            t = (i,j)
            pos[count] = t
            count = count + 1
    for item in pos:
        print item,pos[item]
    G = nx.Graph()
    for i in range(400):
        G.add_node(i)
    normalNodes = [i for i in range(400) if i not in abnormalNodes]
    drawGraph(pos,G,abnormalNodes,normalNodes)
    
    
def draw_400(abnormalNodes,resultNodes):
    pos = dict()
    count = 0
    for i in range(20):
        for j in range(20):
            t = (i,j)
            pos[count] = t
            count = count + 1
    G = nx.Graph()
    for i in range(400):
        G.add_node(i)
    normalNodes = [i for i in range(400) if i not in abnormalNodes]
     # explicitly set positions
    nx.draw_networkx_nodes(G,pos,node_size=100,nodelist=normalNodes,node_color='w')
    nx.draw_networkx_nodes(G,pos,node_size=100,nodelist=abnormalNodes,node_color='r')
    nx.draw_networkx_nodes(G,pos,node_size=20,nodelist=resultNodes,node_color='g')
    nx.draw_networkx_edges(G,pos,alpha=0.5,width=6)
    plt.axis('off')
    plt.show() # display
    plt.close()
    
       
def draw_100(abnormalNodes,resultNodes):
    pos = dict()
    count = 0
    for i in range(10):
        for j in range(10):
            t = (i,j)
            pos[count] = t
            count = count + 1
    for item in pos:
        print item,pos[item]
    G = nx.Graph()
    for i in range(100):
        G.add_node(i)
    normalNodes = [i for i in range(100) if i not in abnormalNodes]
     # explicitly set positions
    print G
    nx.draw_networkx_nodes(G,pos,node_size=100,nodelist=normalNodes,node_color='w')
    nx.draw_networkx_nodes(G,pos,node_size=100,nodelist=abnormalNodes,node_color='r')
    nx.draw_networkx_nodes(G,pos,node_size=20,nodelist=resultNodes,node_color='g')
    nx.draw_networkx_edges(G,pos,alpha=0.5,width=6)
    plt.axis('off')
    plt.show() # display
    plt.close()
   
     
def draw_900(abnormalNodes,resultNodes):
    pos = dict()
    count = 0
    for i in range(30):
        for j in range(30):
            t = (i,j)
            pos[count] = t
            count = count + 1
    for item in pos:
        print item,pos[item]
    G = nx.Graph()
    for i in range(900):
        G.add_node(i)
    normalNodes = [i for i in range(900) if i not in abnormalNodes]
     # explicitly set positions
    print G
    nx.draw_networkx_nodes(G,pos,node_size=100,nodelist=normalNodes,node_color='w')
    nx.draw_networkx_nodes(G,pos,node_size=100,nodelist=abnormalNodes,node_color='r')
    nx.draw_networkx_nodes(G,pos,node_size=20,nodelist=resultNodes,node_color='g')
    nx.draw_networkx_edges(G,pos,alpha=0.5,width=6)
    plt.axis('off')
    plt.show() # display
    plt.close()
def test3():
    abnormalNodes = []
    with open('./APDM-GridData-900-precen_0.07-noise_0-numCC_4.txt_trueNodes.txt') as f:
        for eachLine in f.readlines():
            abnormalNodes.append(int(eachLine.rstrip()))    
    resultNodes = []
    with open('./APDM-GridData-900-precen_0.07-noise_0-numCC_4.txtresultNodes.txt') as f:
        for eachLine in f.readlines():
            resultNodes.append(int(eachLine.rstrip()))    
    draw_900(abnormalNodes,resultNodes) 
    
def test4():
    resultNodes = [7, 8, 51, 69, 70, 71, 72, 89, 90, 91, 109, 111, 177, 178, 179, 195, 196, 197, 206, 208, 215, 216, 226, 227, 228, 235, 236, 247, 248, 267, 268, 297, 308, 326, 327, 328, 347, 367, 387, 388]
    abnormalNodes = [7, 8, 51, 69, 70, 71, 72, 89, 90, 91, 109, 111, 177, 178, 179, 195, 196, 197, 206, 207, 208, 215, 216, 226, 227, 228, 235, 236, 247, 248, 267, 268, 308, 326, 327, 328, 347, 367, 387, 388]
    resultNodes = [347, 367, 387, 388]
    print len(abnormalNodes),len(resultNodes)
    draw_400(abnormalNodes,resultNodes)

def test5():
    
    abnormalNodes = [7, 8, 51, 69, 70, 71, 72, 89, 90, 91, 109, 111, 177, 178, 179, 195, 196, 197, 206, 207, 208, 215, 216, 226, 227, 228, 235, 236, 247, 248, 267, 268, 308, 326, 327, 328, 347, 367, 387, 388]
    normalNodes = [item for item in range(400) if item not in abnormalNodes]
    headNodes = [7, 8, 9, 10, 28, 48, 49, 50, 51, 69, 70, 71, 72, 89, 90, 91, 92, 109, 110, 111, 112, 130, 131, 132, 133, 134, 151, 152, 153, 154, 173, 174, 175, 176, 177, 187, 188, 189, 190, 193, 194, 195, 196, 197, 206, 207, 208, 209, 210, 211, 212, 213, 215, 216, 217, 226, 227, 228, 235, 236, 237, 246, 247, 248, 255, 256, 257, 267, 268, 308, 327, 328, 347, 348, 367, 387, 388, 389, 390]
    tailNodes = [7, 8, 9, 10, 49, 50, 51, 69, 70, 71, 72, 89, 90, 91, 92, 109, 110, 111, 112, 175, 176, 177, 178, 179, 195, 196, 197, 206, 207, 208, 215, 216, 217, 226, 227, 228, 235, 236, 247, 248, 255, 256, 267, 268, 308, 326, 327, 328, 347, 348, 367, 387, 388, 389, 390]
    pos = dict()
    count = 0
    for i in range(20):
        for j in range(20):
            t = (i,j)
            pos[count] = t
            count = count + 1
    G = nx.Graph()
    for item in normalNodes:
        G.add_node(item)
    nx.draw_networkx_nodes(G,pos,node_size=100,nodelist=normalNodes,node_color='w')
    nx.draw_networkx_nodes(G,pos,node_size=100,nodelist=abnormalNodes,node_color='r')
    nx.draw_networkx_nodes(G,pos,node_size=40,nodelist=tailNodes,node_color='g')
    nx.draw_networkx_edges(G,pos,alpha=0.5,width=1)
    plt.axis('off')
    plt.show() # display
    plt.close()
if __name__ == '__main__':
    
    
    abnormalNodes = [35, 45, 34, 44, 46]
    normalNodes = [item for item in range(100) if item not in abnormalNodes]
    headNodes = []
    tailNodes = [2, 34, 3, 35, 37, 38, 70, 71, 72, 73, 44, 13, 45, 14, 46, 47, 48, 83, 24, 56, 93, 94, 95]
    pos = dict()
    count = 0
    for i in range(10):
        for j in range(10):
            t = (i,j)
            pos[count] = t
            count = count + 1
    G = nx.Graph()
    for item in normalNodes:
        G.add_node(item)
    nx.draw_networkx_nodes(G,pos,node_size=100,nodelist=normalNodes,node_color='w')
    nx.draw_networkx_nodes(G,pos,node_size=100,nodelist=abnormalNodes,node_color='r')
    nx.draw_networkx_nodes(G,pos,node_size=40,nodelist=tailNodes,node_color='g')
    nx.draw_networkx_edges(G,pos,alpha=0.5,width=1)
    plt.axis('off')
    plt.show() # display
    plt.close()
    