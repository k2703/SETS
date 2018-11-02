# SETS
Smart Energy Trading System

## How the codebase is structured
HomeAgent will search for all retailers/appliances and store them.
HomeAgent will request to buy with an internal set price, will begin negotiation.
HomeAgent and RetailAgent will negotiate...
* HomeAgent does not change its price - this needs to be implemented.
* RetailAgent will set and change its price via mechanism/strategy specified in the run config.
Negotiation will continue until HomeAgent is satisfied.
* This only occurs once, needs to repeat hourly.

## How to build and run the code

### Sample Run Configuration
`-gui -agents RA1:set.RetailerAgent("RA1","RA","2","25","35","10","percent5");RA2:set.RetailerAgent("RA2","RA","1","25","35","10","crement2");RA3:set.RetailerAgent("RA3","RA","3","25","35","10","weightedavg");HA:set.HomeAgent("50","10","35","25","multi");FGE:set.ApplianceAgent("FGE","AAConsumer");HTE:set.ApplianceAgent("HTE","AAConsumer");TVE:set.ApplianceAgent("TVE","AAConsumer");WOE:set.ApplianceAgent("WOE","AAConsumer");SPA:set.ApplianceAgent("SPA","AAGenerator")`

With Main class jade.Boot

**NOTE:** RetailAgent now requires two arguments
* arg0: Pricing Mechanism
* arg1: Negotiation Strategy
Please ensure you adjust the Run Config as required.

## How to run specific scenarios
* random

## List of Implemented Negotiation Strategies
* reduce10


