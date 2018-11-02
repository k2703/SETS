# SETS
Smart Energy Trading System. This repository contains the components of a smart home trading system. 

# System architecture

The used jade environment is shown in the following figure.

![](jade-env.png?raw=true)

For an overview at the project hava a look at: https://github.com/k2703/SETS/blob/master/SET-Final/JADEREADME.md

All its components (Agent Classes) can be found in: https://github.com/k2703/SETS/tree/master/SET-Final

The prediction forecasting is provided via a fitted LSTM model provided via a web service. For an overview see the following picture:

![](forecast-api.png?raw=true)

The forecast service itself is provided as a standalone microservice and hence decoupled from the jade environment.
The project is locate here: https://github.com/k2703/SETS/tree/master/lstm-forecast-api
A more detailed overview of the project is given here: https://github.com/k2703/SETS/blob/master/lstm-forecast-api/FORECASTREADME.md

