import pandas as pd 
import os
from numpy import genfromtxt

relative_input_path = "/data/Electricity_P_small.csv"


filename = os.path.dirname(os.path.realpath(__file__))+relative_input_path
print(os.path.join(os.path.dirname(os.path.realpath(__file__))))
print(filename)
electricity_data_small = pd.read_csv(filename,sep=",",usecols=['UNIX_TS','DWE','CDE','FGE'])



my_data = genfromtxt(filename, delimiter=',')
print(my_data.shape)

electricity_data_small['UNIX_TS'] = pd.to_datetime(electricity_data_small['UNIX_TS'],unit='s')
print(electricity_data_small['UNIX_TS'].values)


#print(type(electricity_data_small[['UNIX_TS']].values))



from plotly import __version__
from plotly.offline import download_plotlyjs, init_notebook_mode, plot, iplot

print(__version__)# requires version >= 1.9.0

import plotly
import plotly.graph_objs as go

plotly.offline.init_notebook_mode(connected=True)



N = 100
date = electricity_data_small['UNIX_TS'].values
kitchen_dishwasher = electricity_data_small['DWE']
clothes_dryer = electricity_data_small['CDE']
kitchen_fridge = electricity_data_small['FGE']


# Create traces
trace0 = go.Scatter(
    x = date,
    y = kitchen_dishwasher,
    mode = 'lines',
    name = 'lines'
)
trace1 = go.Scatter(
    x = date,
    y = clothes_dryer,
    mode = 'lines+markers',
    name = 'lines+markers'
)
trace2 = go.Scatter(
    x = date,
    y = kitchen_fridge,
    mode = 'markers',
    name = 'markers'
)
data = [trace0, trace1, trace2]

plotly.offline.iplot(data, filename='line-mode')