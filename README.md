# SETS
Smart Energy Trading System

## Run Configuration
`-gui -agents Home:agentspackage.HomeAgent;AirCon:agentspackage.ApplianceAgent;ElectricCar:agentspackage.ApplianceAgent;SolarPanel:agentspackage.ApplianceAgent;ErgonEnergy:agentspackage.RetailAgent;SumoPower:agentspackage.RetailAgent;Powerdirect:agentspackage.RetailAgent`

With Main class jade.Boot

**NOTE:** RetailAgent now requires two arguments
* arg0: Pricing Mechanism
* arg1: Negotiation Strategy
Please ensure you adjust the Run Config as required.

## Implemented Pricing Mechanisms
* None

## List of Implemented Negotiation Strategies
* None


