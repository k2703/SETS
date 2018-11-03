# Projectinformations for user

## Using code base

To start the web service on your current hosted server the following commands need to be executed:

```bash

# Change into source directory
cd src/

# Execute main to start web server
python3 main.py

```

Note: The project on host `115.146.92.150` is located in `/home/ubuntu/Notebooks`

To build models the following commands need to be executed:

```bash

# Change into source directory
cd src/model/

# Execute main to start web server
jupyter notebook 

# Open the following notebook to buid models
model-builder-template.ipynb

```

## Access to API

Once the web server is up and running one can access the API using the following calls:

```bash

# Deploys pretrained LSTM models for a passed list of appliances  
curl -H "Content-Type: application/json" -X POST -d '{"appliances":["FGE","HTE","TVE"]}' http://115.146.92.150:5000/lstmforecast/deploy-model

# Pulls back all currently deployed LSTM models 
curl -H "Content-Type: application/json" -X POST -d '' http://115.146.92.150:5000/lstmforecast/pullback-model

# Conveys current observation to model and model returns corresponding prediction
curl -H "Content-Type: application/json" -X POST -d '{"observation":"59.51666667"}' http://115.146.92.150:5000/lstmforecast/FGE/predict
```
![](warning.png? =250x)

Be aware that you can change the appliance as needed! (Hence you might replace 'FGE' in the url in the example above).


## Results 

| Appliance Model  | Description      | Baseline Model Train RMSE / Test RMSE | LSTM Model Train RMSE / Test RMSE | Relative RMSE change for Train / Test |
| --------------|------|-------------|-----|-----|
| B1E     |  north bedroom plugs and lights | 5.068 / 5.249 | 4.167 / 4.181 | - 17.78 % / - 20.35 % |
| B2E     |  master and south bedroom plugs and lights | 24.504 / 33.194 | 23.357 / 31.542 | - 4.68 % / - 4.98 % |
| BME     |  some of the basement plugs and lights | 88.206 / 82.306 | 77.472 / 74.938 | - 12.17 % / - 9.61 % |
| CDE     |  clothes dryer | 19.295 / 25.929 | 16.862 / 20.031 | - 16.28 % / - 22.75 % |
| CWE     |  front loading clothes washer | 24.051 / 24.802 | 20.006 / 20.949 | - 16.82 % / - 15.54 % |
| DNE     |  dining room plugs | 1.978 / 1.621 | 5.111 / 4.988 | + 158.39 % / + 207.71 % |
| DWE     |  kitchen dishwasher | 70.233 / 83.242 | 58.946 / 69.807 | - 16.07 % / - 16.14 % |
| EBE     |  electronics workbench | 15.974 / 0.013 | 13.670 / 0.023 | - 14.42 % / + 76.92 % |
| EQE     |  security and network equipment | 0.369 / 0.391 | 0.318 / 0.345 | - 13.82 % / - 11.76 % |
| FGE     |  kitchen fridge | 35.436 / 33.755 | 25.138 / 23.900 | - 28.88 % / - 29.20 % |
| FRE     |  forced air furnace fan and thermostat | 19.295 / 25.929 | 18.071 / 21.687 | - 6.34 % / - 16.36 % |
| HPE     |  heat pump | 388.431 / 462.472 | 330.201 / 384.378 | - 14.99 % / - 16.36 % |
| HTE     |  instant hot water unit | 3.827 / 4.074 | 3.170 / 3.394 | - 17.17 % / - 16.69 % |
| MHE     |  main house | 652.131 / 689.465 | 631.638 / 632.270 | - 3.14 % / - 8.30 % |
| OFE     |  home office lights and plugs | 20.410 / 14.118 | 103.833 / 106.577 | + 408.74 % / 654.90 % |
| OUE     |  outside plug | 0.123 / 0.048 | 0.104 / 0.043 | - 15.45 % / - 10.42 % |
| RSE     |  basement rental suite | 284.186 / 298.731 | 254.582 / 267.898 | - 10.42 % / -10.32 % |
| SPA     |  solar panel  | 0.309 / 0.318 | 0.250 / 0.260 | - 19.09 % / - 18.24 % |
| TVE     |  entertainment equipment (TV, PVR, amplifier, and Blu-Ray)  | 50.180 / 53.954 | 44.171 / 47.873 | - 11.97 % / - 11.27 % |
| UNE     |  unmetered soft-meter amount   | 179.775 / 189.837 | 163.362 / 167.464 | - 9.13 % / - 11.79 % | 
| UTE     |  utility room plug  | 0.565 / 0.242 | 0.810 / 0.629 | + 43.36 % / + 159.92 % |
| WHE     | whole-house power | 710.427 / 739.275 | 935.764 / 931.615 | + 31.72 % / + 26.02 % |
| WOE     | kitchen convection wall oven | 104.574 / 110.679 | 82.980 / 88.151 | - 20.65 % / - 20.35 % |

## Data Source Information

#### Data of energy consumption appliances

Data can be downloaded at 
https://dataverse.harvard.edu/file.xhtml?persistentId=doi:10.7910/DVN/FIE0S4/FS26TX&version=1.2.

Information to table column names are provided in the following table: 
https://www.nature.com/articles/sdata201637/tables/4.

The Paper corresponding to the data set:
https://www.nature.com/articles/sdata201637#t3.

#### Data of energy providing appliances

The solar panel data set download link is: 
http://smart.cs.umass.edu/download.php?t=solar-panels&y=2017. 

It can be found at: 
http://traces.cs.umass.edu/index.php/Smart/Smart.

Unfortunately data is not provided for the entire day, only in the interval from 5am - 21pm. The remaining values are filled up automatically by using the last observed value.


## Useful Blog for deploying your application as a RESTful service


https://blog.miguelgrinberg.com/post/designing-a-restful-api-with-python-and-flask
