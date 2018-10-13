# SETS
Smart Energy Trading System

## Current Features
HomeAgent will search for all retailers/appliances and store them.

HomeAgent will request to buy with an internal set price, will begin negotiation.

HomeAgent and RetailAgent will negotiate...
* HomeAgent does not change its price - this needs to be implemented.  
* RetailAgent will set and change its price via mechanism/strategy specified in the run config.

Negotiation will continue until HomeAgent is satisfied.
* This only occurs once, needs to repeat hourly.

## Run Configuration
`-gui -agents Home:agentspackage.HomeAgent;AirCon:agentspackage.ApplianceAgent;ElectricCar:agentspackage.ApplianceAgent;SolarPanel:agentspackage.ApplianceAgent;ErgonEnergy:agentspackage.RetailAgent(random,reduce10);SumoPower:agentspackage.RetailAgent(random,reduce10);Powerdirect:agentspackage.RetailAgent(random,reduce10)`

With Main class jade.Boot

**NOTE:** RetailAgent now requires two arguments
* arg0: Pricing Mechanism
* arg1: Negotiation Strategy
Please ensure you adjust the Run Config as required.

## List of Implemented Pricing Mechanisms
* random

## List of Implemented Negotiation Strategies
* reduce10


