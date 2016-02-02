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
def test1():
    
    abnormalNodes = []
    trueAbnormalNodes = []
    resultNodes = []
    fp = open("pruning_1.txt")
    edges = []
    for i, line in enumerate(fp):
        if i == 0:
            count = 0
            for item in line.rstrip().split(' '):
                if float(item.rstrip()) != 0.0:
                    abnormalNodes.append(count)
                count = count + 1
        if i == 1:
            for item in line.rstrip().split(' '):
                    trueAbnormalNodes.append(int(item))
        if i == 2:
            for item in line.rstrip().split(' '):
                resultNodes.append(int(item.rstrip()))
        if i >= 3:
            l0 = int(line.rstrip().split(' ')[0])
            l1 = int(line.rstrip().split(' ')[1])
            if l0 - l1 > 50 or l0 - l1 < -50:
                x = -1
            elif l0%50 == 0 or l1%50 == 0:
                if l0%50 == 0 and (l1 - l0 == 49 or l1 - l0 == -49 ):
                    x = -1
                elif l1%50 == 0 and (l1 - l0 == 49 or l1 - l0 == -49 ):
                    x = -1
                else:  
                    edges.append((l0,l1))
            else:
                edges.append((l0,l1))
    fp.close()
    normalNodes = [item for item in range(2500) if item not in abnormalNodes]        
    
    count = 0 
    for item in resultNodes:
        if item in trueAbnormalNodes:
            count = count + 1
    print 'Precision is ',count*1.0 / len(resultNodes)*1.0, ' and recall is ',count*1.0 / len(trueAbnormalNodes)*1.0
    
    pos = dict()
    count = 0
    for i in range(50):
        for j in range(50):
            t = (i,j)
            pos[count] = t
            count = count + 1
    G = nx.Graph()
    for item in range(2500):
        G.add_node(item)
    nx.draw_networkx_nodes(G,pos,node_size=50,nodelist=normalNodes,node_color='w')
    nx.draw_networkx_nodes(G,pos,node_size=50,nodelist=abnormalNodes,node_color='r')
    nx.draw_networkx_nodes(G,pos,node_size=10,nodelist=resultNodes,node_color='g')
    nx.draw_networkx_edges(G,pos,edgelist=edges, alpha=0.5,width=5,edge_color = 'm')
    plt.axis('off')
    plt.show() # display
    plt.close()

def test2500(fileName):
    #all abnormal nodes ; true abnormal nodes ; result nodes ; edges
    AllAbnormalNodes = []
    trueAbnormalNodes = []
    components = dict()
    resultNodes = []
    fp = open(fileName)
    edges = dict()
    for i, line in enumerate(fp):
        if i == 0:
            for item in line.rstrip().split(' '):
                AllAbnormalNodes.append(int(item.rstrip()))
        if i == 1:
            for item in line.rstrip().split(' '):
                trueAbnormalNodes.append(int(item.rstrip()))
        if i == 2:
            for item in line.rstrip().split(' '):
                resultNodes.append(int(item.rstrip()))
        if i == 3:
            count = 0
            for items in line.rstrip().split('#'):
                edges[count] = []
                if items == '' or items == ' ':
                    continue
                for item in items.rstrip().split(';'):
                    if item == '':
                        continue
                    print item
                    l0 = int(item.rstrip().split(' ')[0])
                    l1 = int(item.rstrip().split(' ')[1])
                    if l0 - l1 > 50 or l0 - l1 < -50:
                        x = -1
                    elif l0%50 == 0 or l1%50 == 0:
                        if l0%50 == 0 and (l1 - l0 == 49 or l1 - l0 == -49 ):
                            x = -1
                        elif l1%50 == 0 and (l1 - l0 == 49 or l1 - l0 == -49 ):
                            x = -1
                        else:  
                            edges[count].append((l0,l1))
                    else:
                        edges[count].append((l0,l1))
                count = count + 1
    fp.close()
    
    for item in edges:
        print item, edges[item]
    normalNodes = [item for item in range(2500) if item not in AllAbnormalNodes]        

    count = 0 
    for item in resultNodes:
        if item in trueAbnormalNodes:
            count = count + 1
    print 'Precision is ',count*1.0 / len(resultNodes)*1.0, ' and recall is ',count*1.0 / len(trueAbnormalNodes)*1.0
    
    pos = dict()
    count = 0
    for i in range(50):
        for j in range(50):
            t = (i,j)
            pos[count] = t
            count = count + 1
    G = nx.Graph()
    for item in range(2500):
        G.add_node(item)
    nx.draw_networkx_nodes(G,pos,node_size=60,nodelist=resultNodes,node_color='r',node_shape="s")
    nx.draw_networkx_nodes(G,pos,node_size=30,nodelist=normalNodes,node_color='w')
    #nx.draw_networkx_nodes(G,pos,node_size=40,nodelist=trueAbnormalNodes,node_color='k')
    nx.draw_networkx_nodes(G,pos,node_size=30,nodelist=AllAbnormalNodes,node_color='k')
    #nx.draw_networkx_edges(G,pos,edgelist=edges[0], alpha=0.5,width=5,edge_color = 'm')
    #nx.draw_networkx_edges(G,pos,edgelist=edges[1], alpha=0.5,width=5,edge_color = 'y')
    #nx.draw_networkx_edges(G,pos,edgelist=edges[2], alpha=0.5,width=5,edge_color = 'b')
    #nx.draw_networkx_edges(G,pos,edgelist=edges[3], alpha=0.5,width=5,edge_color = 'c')
    plt.axis('off')
    plt.show() # display
    plt.close()
    
    
def test3600(fileName):
    #all abnormal nodes ; true abnormal nodes ; result nodes ; edges
    AllAbnormalNodes = []
    trueAbnormalNodes = []
    components = dict()
    resultNodes = []
    fp = open(fileName)
    edges = dict()
    for i, line in enumerate(fp):
        if i == 0:
            for item in line.rstrip().split(' '):
                AllAbnormalNodes.append(int(item.rstrip()))
        if i == 1:
            for item in line.rstrip().split(' '):
                trueAbnormalNodes.append(int(item.rstrip()))
        if i == 2:
            for item in line.rstrip().split(' '):
                resultNodes.append(int(item.rstrip()))
        if i == 3:
            count = 0
            for items in line.rstrip().split('#'):
                edges[count] = []
                if items == '' or items == ' ':
                    continue
                for item in items.rstrip().split(';'):
                    if item == '':
                        continue
                    print item
                    l0 = int(item.rstrip().split(' ')[0])
                    l1 = int(item.rstrip().split(' ')[1])
                    if l0 - l1 > 60 or l0 - l1 < -60:
                        x = -1
                    elif l0%60 == 0 or l1%60 == 0:
                        if l0%60 == 0 and (l1 - l0 == 59 or l1 - l0 == -59 ):
                            x = -1
                        elif l1%60 == 0 and (l1 - l0 == 59 or l1 - l0 == -59 ):
                            x = -1
                        else:  
                            edges[count].append((l0,l1))
                    else:
                        edges[count].append((l0,l1))
                count = count + 1
    fp.close()
    
    for item in edges:
        print item, edges[item]
    normalNodes = [item for item in range(3600) if item not in AllAbnormalNodes]        

    count = 0 
    for item in resultNodes:
        if item in trueAbnormalNodes:
            count = count + 1
    print 'Precision is ',count*1.0 / len(resultNodes)*1.0, ' and recall is ',count*1.0 / len(trueAbnormalNodes)*1.0
    
    pos = dict()
    count = 0
    for i in range(60):
        for j in range(60):
            t = (i,j)
            pos[count] = t
            count = count + 1
    G = nx.Graph()
    for item in range(3600):
        G.add_node(item)
    nx.draw_networkx_nodes(G,pos,node_size=60,nodelist=resultNodes,node_color='r',node_shape="s")
    nx.draw_networkx_nodes(G,pos,node_size=30,nodelist=normalNodes,node_color='w')
    #nx.draw_networkx_nodes(G,pos,node_size=60,nodelist=AllAbnormalNodes,node_color='r')
    nx.draw_networkx_nodes(G,pos,node_size=30,nodelist=AllAbnormalNodes,node_color='k')
    #nx.draw_networkx_edges(G,pos,edgelist=edges[0], alpha=0.5,width=5,edge_color = 'y')
    #nx.draw_networkx_edges(G,pos,edgelist=edges[1], alpha=0.5,width=5,edge_color = 'm')
    #nx.draw_networkx_edges(G,pos,edgelist=edges[2], alpha=0.5,width=5,edge_color = 'b')
    #nx.draw_networkx_edges(G,pos,edgelist=edges[3], alpha=0.5,width=5,edge_color = 'c')
    plt.axis('off')
    plt.show() # display
    plt.close()
    
if __name__ == '__main__':
    #test2500("grid_2500_precen_0.05_numCC_4_0.02_trueS.txt")
    test3600("grid_3600_precen_0.05_numCC_4_0.02_trueS.txt")
    