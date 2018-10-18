# Projectinformations for user
***

## Access to API
***



```bash

# deploy model 
curl -H "Content-Type: application/json" -X POST -d '{"appliances":["FGE","HTE","TVE"]}' http://115.146.92.150:5000/lstmforecast/deploy-model

# pullback model 
curl -H "Content-Type: application/json" -X POST -d '' http://115.146.92.150:5000/lstmforecast/pullback-model

# append current observation and retrieve prediction
curl -H "Content-Type: application/json" -X POST -d '{"observation":"59.51666667"}' http://115.146.92.150:5000/lstmforecast/FGE/predict
```

Be aware that you can change the appliance as needed! (Hence you might replace 'FGE' in the url in the example above).

## Data Source Information
***

Data can be downloaded at 

https://dataverse.harvard.edu/file.xhtml?persistentId=doi:10.7910/DVN/FIE0S4/FS26TX&version=1.2.

Information to table column names are provided in the following table: 

https://www.nature.com/articles/sdata201637/tables/4.

The Paper corresponding to the data set:

https://www.nature.com/articles/sdata201637#t3.


## Useful Blog for deploying your application as a RESTful service
***

https://blog.miguelgrinberg.com/post/designing-a-restful-api-with-python-and-flask


