# SETS
Smart Energy Trading System

## How the codebase is structured
SET-Final is simply a Java project directory which can be imported into Eclipse via the method provided below. Within SET-Final are 5 sample scenario .bat runnables. Required .jar files are stored in the `SET-Final\lib\jarfiles` directory. If these are moved, the .bat scenarios will not function. 

## How to build and run the code
### Eclipse method
1. Launch Eclipse
2. File > Open Projects from File System...
3. Set 'Import source:' to the unzipped SET-Final folder.
4. Ensure SET-Final is ticked to import as an Eclipse project.
5. Press 'Finish'
6. Run > Run Configurations...
7. Create a new Java Application Configuration
8. Set 'Project:' to SET-Final
9. Set 'Main class:' to jade.Boot
10. Switch to the Arguments tab and enter the sample run configuration provided in this README.
11. Press 'Run'

### Sample run configuration
```-gui -agents RA1:set.RetailerAgent("RA1","RA","2","25","35","10","percent5");RA2:set.RetailerAgent("RA2","RA","1","25","35","10","crement2");RA3:set.RetailerAgent("RA3","RA","3","25","35","10","weightedavg");HA:set.HomeAgent("50","10","35","25","multi");FGE:set.ApplianceAgent("FGE","AAConsumer");HTE:set.ApplianceAgent("HTE","AAConsumer");TVE:set.ApplianceAgent("TVE","AAConsumer");WOE:set.ApplianceAgent("WOE","AAConsumer");SPA:set.ApplianceAgent("SPA","AAGenerator")```

**NOTE:** Please see the parameter reference at the end of this README for explanations regarding each Agent's parameters.

### Command prompt
The system can be ran from the command prompt by setting the current directory to the unzipped SET-Final and entering the following:
```java -cp libs\jarfiles\jade.jar;bin;libs\jarfiles\commons-logging-1.2.1.1.jar;libs\jarfiles\hamcrest-core-1.3.jar;libs\jarfiles\httpclient-4.5.6.jar;libs\jarfiles\httpclient-cache-4.5.6.jar;libs\jarfiles\httpclient-win-4.5.6.jar;libs\jarfiles\httpcore-4.4.10.jar;libs\jarfiles\java-util-1.3.1.jar;libs\jarfiles\jcommon-1.0.23.jar;libs\jarfiles\jcommon-xml-1.0.23.jar;libs\jarfiles\jdbc-api-1.4-1.jar;libs\jarfiles\jfreechart-1.0.19.jar;libs\jarfiles\jfreechart-1.0.19-demo.jar;libs\jarfiles\jfreechart-1.0.19-experimental.jar;libs\jarfiles\jfreechart-1.0.19-swt.jar;libs\jarfiles\jfreesvg-2.0.jar;libs\jarfiles\junit-4.11.jar;libs\jarfiles\opencsv-2.4.jar;libs\jarfiles\orsoncharts-1.4-eval-nofx.jar;libs\jarfiles\orsonpdf-1.6-eval.jar;libs\jarfiles\servlet.jar;libs\jarfiles\swtgraphics2d.jar; jade.Boot -gui -agents RA1:set.RetailerAgent("RA1","RA","2","25","35","10","percent5");RA2:set.RetailerAgent("RA2","RA","1","25","35","10","crement2");RA3:set.RetailerAgent("RA3","RA","3","25","35","10","weightedavg");HA:set.HomeAgent("50","10","35","25","single");FGE:set.ApplianceAgent("FGE","AAConsumer");HTE:set.ApplianceAgent("HTE","AAConsumer");TVE:set.ApplianceAgent("TVE","AAConsumer");WOE:set.ApplianceAgent("WOE","AAConsumer");SPA:set.ApplianceAgent("SPA","AAGenerator")```

The -agents declarations can be tuned to your liking, provided it adheres by the Agent parameter reference at the end of this README.

## How to run specific scenarios
A variety of .bat files are present in the SET-Final directory. These can be opened to automatically run a variety of scenarios.
### Scenario overview
* `scenario1.bat` A scenario where the HomeAgent requests prices from the RetailerAgents, and chooses the best price out of those received without sending a counter offer.
* `scenario2.bat` A scenario where the HomeAgent negotiates with the RetailerAgents, who negotiate using their respective initial prices and the percent5 strategy.
* `scenario3.bat` A scenario where the HomeAgent negotiates with the RetailerAgents, who negotiate using their respective initial prices and the crement2 strategy.
* `scenario4.bat` A scenario where the HomeAgent negotiates with the RetailerAgents, who negotiate using their respective initial prices and the weightedavg strategy.
* `scenario5.bat` A scenario where the HomeAgent negotiates with the RetailerAgents, who negotiate using their respective initial prices and negotiation strategies. Identical to the run config provided in this README.

## Agent parameter reference
![agent parameter reference](https://i.imgur.com/6Z2yBpO.png)

### Implemented negotiation techniques
* `percent5` - increases/decreases price by 5%
* `crement2` - increases/decreases price by 2c/KwH
* `weightedavg` - a greedy compromise
