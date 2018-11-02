# SETS
Smart Energy Trading System. This repository contains the components of a smart home trading system. 

# System architecture

The used jade environment is shown in the following figure.

![](jade-env.png?raw=true)

All its components (Agent Classes) can be found in the `jade-env` folder.

The prediction forecasting is provided via a fitted LSTM model provided via a web service. For an overview see the following picture:

![](forecast-api.png?raw=true)

Note that the server ip address would need to be replaced accordingly. A more detailed overview of the project is given here https://github.com/k2703/SETS/blob/master/lstm-forecast-api/FORECASTREADME.md

# Use of CodeBase

