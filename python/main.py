# import plotly.plotly as py
# import plotly.graph_objs as go
import datetime
import time
import sys
import numpy
import math
from bokeh.models import HoverTool
from bokeh.plotting import figure, output_file, show,  vplot
from smoothcurve import smoothcurve
import csv

timeaxis = []
accelaxis = []
timeaxis1 = []
accelaxis1 = []
sampledeviation = []
WINDOW_SIZE = 5
temp5 = []


with open('./data/ChiragT3.csv', 'rb') as csvfile:
    data = csv.reader(csvfile, delimiter=',', quotechar='|')


    for i, row in enumerate(data):
    	normalvalue = math.sqrt( float( row[1] ) ** 2 + float( row[2]  ) ** 2 + float(row[3]) ** 2 ) 
    	temp5.append(normalvalue)

    	if( i % WINDOW_SIZE == 0):
    		#Operate on data
    		sampledeviation.append(numpy.std(temp5))
    		del temp5[:]

    	timeaxis.append(row[0])
    	accelaxis.append( normalvalue )


print "Populated " + str(len(timeaxis))
intervalnumbers = [ i for i in xrange( 0, len(timeaxis) / WINDOW_SIZE ) ]

hover = HoverTool(
        tooltips=[
            ("(x,y)", "($x, $y)")
        ]
    )

# data = [
# 	go.Scatter(
#     	x=timeaxis,
#     	y=accelaxis,
#     	name='Original',
#     	line=dict(
#             width=1
#             )
#     	)
# ]
print "Smoothened.. Plotting..."

output_file("test.html")
p = figure(plot_width=1300, plot_height=600)
p.line(timeaxis, accelaxis, line_width=1)
# p.line([i for i,v in enumerate(sampledeviation)], sampledeviation, line_width=1)

# s1 = figure(plot_width=1300, plot_height=600)
# s1.line([i for i,v in enumerate(sampledeviation)], sampledeviation, line_width=1)


# # s1 = figure(width=250, plot_height=250, title=None)
# # s1.circle([1,2,3], [3,4,5], size=10, color="navy", alpha=0.5)

# # # create another one
# # s2 = figure(width=250, height=250, title=None)
# # s2.triangle(x, y1, size=10, color="firebrick", alpha=0.5)

# # # create and another
# # s3 = figure(width=250, height=250, title=None)
# # s3.square(x, y2, size=10, color="olive", alpha=0.5)

# p.multi_line([timeaxis, timeaxis1],[accelaxis, accelaxis1], color=["red", "blue"], line_width=1)

# cump = vplot(p, stdp)
show(p)

# plot_url = py.plot(data, filename='python-datetime')





