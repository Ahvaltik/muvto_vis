#!/usr/bin/env python2

import sys
import random

def generate_edge(id1, id2):
    return "{} {}\n".format(id1, id2)

def generate_vertex(id, vertices, density):
    neigh = range(vertices)
    random.shuffle(neigh)
    neigh.remove(id)
    return ''.join(map(lambda id2: generate_edge(id, id2), neigh[:density]))

def generate_graph(vertices, density):

    with open('generated-{}-{}.txt'.format(vertices, density), 'w') as f:
        f.writelines(map(lambda id: generate_vertex(id, vertices, density), xrange(vertices)))

if __name__ == '__main__':
    if len(sys.argv) > 2:
        generate_graph(int(sys.argv[1]), int(sys.argv[2]))
        sys.exit(0)
    else:
        print "usage", sys.argv[0], "<vertices> <density>"
        sys.exit(1)
