import pandas as pd 
from sklearn.metrics import mean_squared_error
from math import sqrt
from sklearn.metrics import mean_squared_error
from sklearn.preprocessing import MinMaxScaler
import numpy as np
import os
from datetime import datetime
from keras.models import model_from_json
from sklearn.externals import joblib


def read_electricity_p(appliances = None, 
                       limit = None, 
                       filename = 'Electricity_P.csv'):
    
    """Reads Electricity_p dataset

    Args:
        appliances: devices to read.
        limit: limit the number of rows to read.
        rel_file_path: relative path to file to read.

    Returns:
        in-memory read pandas dataframe along with the appliances 
    """
    
    # NOTE: data will be provided in google cloud storage
    filename = os.path.dirname(os.path.dirname(os.getcwd()).replace(' ','\ ')) + "/data/" + filename
    
    # define appliance which shall be displayed
    if appliances is None and limit is None:
        electricity_data = pd.read_csv(filename,sep=",")
    elif appliances is None and limit is not None:
        electricity_data = pd.read_csv(filename,sep=",", nrows = limit)
    elif appliances is not None and limit is None:
        electricity_data = pd.read_csv(filename,sep=",", usecols=(appliances+['UNIX_TS']))
    else:
        electricity_data = pd.read_csv(filename,sep=",",nrows = limit, usecols=(appliances+['UNIX_TS']))

    # conert date column into plotly-interpretable format
    electricity_data['UNIX_TS'] = pd.to_datetime(electricity_data['UNIX_TS'],unit='s').astype(datetime)
    
    # set index to date
    #electricity_data.set_index('UNIX_TS')

    # aggregate on an hour basis
    electricity_data = electricity_data \
        .groupby(electricity_data.UNIX_TS.map(lambda x: x.strftime('%Y-%m-%d %H'))) \
        .mean()

    appliances = electricity_data.columns.values
    return electricity_data, appliances


def timeseries_to_supervised(data, lag=1):
    df = pd.DataFrame(data)
    columns = [df.shift(i) for i in range(1, lag+1)]
    columns.append(df)
    df = pd.concat(columns, axis=1)
    df.fillna(0, inplace=True)
    return df


# create a differenced series
def difference(dataset, interval=1):
    diff = list()
    for i in range(interval, len(dataset)):
        value = dataset[i] - dataset[i - interval]
        diff.append(value)
    return pd.Series(diff)


'''# invert differenced value
def inverse_difference(history, differenced):
    # invert transform
    inverted = list()
    for i in range(len(differenced)):
        value = differenced[i] + history[-(len(history)-i)]
        inverted.append(value)
    inverted = pd.Series(inverted)
    return inverted'''

# invert differenced value
def inverse_difference(history, yhat, interval=1):
    return yhat + history[-interval]
 
# scale train and test data to [-1, 1]
def scale(train, test):
    # fit scale
    scaler = MinMaxScaler(feature_range=(-1, 1))
    scaler = scaler.fit(train)
    # transform train
    train = train.reshape(train.shape[0], train.shape[1])
    series_scaled = scaler.transform(train)
    # transform test
    test = test.reshape(test.shape[0], test.shape[1])
    test_scaled = scaler.transform(test)
    return scaler, series_scaled, test_scaled
 
'''# inverse scaling for a forecasted value
def invert_scale(scaler, X, value):
    new_row = [x for x in X] + [value]
    array = numpy.array(new_row)
    array = array.reshape(1, len(array))
    inverted = scaler.inverse_transform(array)
    return inverted[0, -1]'''


def invert_scale(scaler, predictions):
    array = [[a[0]] + [b[0]] for a, b in zip(predictions, predictions)]
    array = np.array(array)
    #array = array.reshape(1, len(array))
    inverted = scaler.inverse_transform(array)
    return inverted#[:,-1]

 
def invert_normalize(scaler, series_raw, series_predictions_normalized):
    series_predictions_scaled = invert_scale(scaler, series_predictions_normalized)
    #print(series_predictions_scaled[0:10])
    # invert transform
    tmp = list()
    #print('>>>>>>>')
    #print(series_raw)
    for i in range(len(series_predictions_scaled)):
        #if i < 10:
        #    print("step " + str(i))
        #    print("series_predictions[i]" + str(series_predictions_scaled[i]))
        #    print(series_raw[-(len(series_raw)-i)])
        value = inverse_difference(series_raw, series_predictions_scaled[i], len(series_raw)-i)
        #if i < 10:
        #    print(value)
        tmp.append(value)
    series_predictions = pd.Series(tmp)
    series_predictions = [x[0]  for x in series_predictions]
    return series_predictions


def compute_rmse(raw, prediction):
    if len(raw) != len(prediction):
        raise ValueError("raw series and predicition series do not have same input length!")
    rmse = sqrt(mean_squared_error(raw, prediction))
    return rmse


def print_series(raw, prediction, ind_start = 0, ind_end = None, legend_1 = 'raw series', legend_2 = 'prediction series'):
    if ind_end is None:
        ind_end = max(len(raw),len(prediction))
    plt.plot(list(raw)[ind_start:ind_end],'o-')
    plt.plot(list(prediction)[ind_start:ind_end],'o-')
    plt.gca().legend((legend_1,legend_2), prop={'size': 18})
    plt.show()
    
    
# fit an LSTM network to training data
def fit_lstm(train, batch_size, nb_epoch, neurons, raw_normalized, scaler):
    
    # create input features and corresponding output
    X, y = train[:, 0:-1], train[:, -1]
    X = X.reshape(X.shape[0], 1, X.shape[1])

    # initialize train and test error histories
    train_err_hist = []
    test_err_hist = []
    
    # reeshape normalized input raw values for model prediction
    raw_normalized_reshaped = raw_normalized[:, 0].reshape(len(raw_normalized), 1, 1)
    
    # build model
    model = Sequential()
    model.add(LSTM(neurons, batch_input_shape=(batch_size, X.shape[1], X.shape[2]), stateful=True))
    model.add(Dense(1))
    model.compile(loss='mean_squared_error', optimizer='adam')
    
    for i in range(nb_epoch):
        model.fit(X, y, epochs=1, batch_size=batch_size, verbose=1, shuffle=False)
        model.reset_states()
        
        raw_normalized_predictions = model.predict(raw_normalized_reshaped, batch_size=1)
        model_prediction = invert_normalize(scaler, raw_values, raw_normalized_predictions)
        
        train_err_hist.append(compute_rmse(raw_values[1:][:n_train], model_prediction[:n_train]))
        test_err_hist.append(compute_rmse(raw_values[1:][n_train:n_train+n_test], model_prediction[n_train:n_train+n_test]))
        
    return model, train_err_hist, test_err_hist



def save_model(lstm_model, model_name):

    # serialize model to JSON
    lstm_model_json = lstm_model.to_json()
    home_dir = os.path.dirname(os.getcwd().replace(' ','\ '))+ "/src/model/models/"
    with open(home_dir+model_name +".json" , "w") as json_file:
        json_file.write(lstm_model_json)

    # serialize weights to HDF5
    lstm_model.save_weights(home_dir+model_name+".h5")

    
def save_scaler(scaler, scaler_name):
    home_dir = os.path.dirname(os.getcwd()).replace(' ','\ ')+ "/src/model/data/"
    scaler_filename = home_dir +scaler_name
    joblib.dump(scaler, scaler_filename) 
    
    
def save_ndarray(ndarray, ndarray_name):
    home_dir = os.path.dirname(os.getcwd()).replace(' ','\ ')+ "/src/model/data/"
    ndarray_filename = home_dir +ndarray_name + ".npy"
    np.save(ndarray_filename, ndarray) 
    

def load_model(model_name):
    home_dir = os.path.dirname(os.getcwd()).replace(' ','\ ')+ "/src/model/models/"

    # load json and create model
    json_file = open(home_dir+model_name+".json", 'r')
    loaded_model_json = json_file.read()
    json_file.close()
    lstm_model = model_from_json(loaded_model_json)

    # load weights into new model
    lstm_model.load_weights(home_dir+model_name+".h5")
    print("Loaded model from disk")
    return lstm_model

    
def load_scaler(scaler_name):
    # load scaler model
    home_dir =os.path.dirname(os.getcwd()).replace(' ','\ ')+ "/src/model/data/"
    scaler_filename = home_dir+scaler_name
    scaler = joblib.load(scaler_filename) 
    return scaler


def load_ndarray(ndarray_name):
    home_dir = os.path.dirname(os.getcwd()).replace(' ','\ ')+ "/src/model/data/"
    ndarray_filename = home_dir +ndarray_name+".npy"
    ndarray = np.load(ndarray_filename) 
    return ndarray