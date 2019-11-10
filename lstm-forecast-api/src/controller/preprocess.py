import pandas as pd
from sklearn.metrics import mean_squared_error
from math import sqrt
from sklearn.preprocessing import MinMaxScaler
import numpy as np
import os
from datetime import datetime


def read_electricity_p(appliances=None,
                       limit=None,
                       filename='Electricity_P.csv'):
    """
        Reads Electricity_p dataset
    """
    filename = os.path.dirname(os.path.dirname(
        os.getcwd()).replace(' ', '\ ')) + "/data/" + filename
    if appliances is None and limit is None:
        electricity_data = pd.read_csv(filename, sep=",")
    elif appliances is None and limit is not None:
        electricity_data = pd.read_csv(filename, sep=",", nrows=limit)
    elif appliances is not None and limit is None:
        electricity_data = pd.read_csv(
            filename, sep=",", usecols=(appliances+['UNIX_TS']))
    else:
        electricity_data = pd.read_csv(
            filename, sep=",", nrows=limit, usecols=(appliances+['UNIX_TS']))

    # convert date column into plotly-interpretable format
    electricity_data['UNIX_TS'] = pd.to_datetime(
        electricity_data['UNIX_TS'], unit='s').astype(datetime)

    # aggregate on an hour basis
    electricity_data = electricity_data \
        .groupby(electricity_data.UNIX_TS.map(lambda x: x.strftime('%Y-%m-%d %H'))) \
        .mean()

    appliances = electricity_data.columns.values
    return electricity_data, appliances


def timeseries_to_supervised(data, lag=1):
    """
        Create supervised data by concatenating input values at time t
        with their corresponding output values at time t+1
    """
    df = pd.DataFrame(data)
    columns = [df.shift(i) for i in range(1, lag+1)]
    columns.append(df)
    df = pd.concat(columns, axis=1)
    df.fillna(0, inplace=True)
    return df


def difference(dataset, interval=1):
    """
        Converts absolut values into differenced values
    """
    diff = list()
    for i in range(interval, len(dataset)):
        value = dataset[i] - dataset[i - interval]
        diff.append(value)
    return pd.Series(diff)


def inverse_difference(history, yhat, interval=1):
    """
        Inverses differencing
    """
    return yhat + history[-interval]


def scale(train, test):
    """
        Scales train and test data to [-1,1] range
    """
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


def invert_scale(scaler, predictions):
    """
        Inverses scaling
    """
    array = [[a[0]] + [b[0]] for a, b in zip(predictions, predictions)]
    array = np.array(array)
    inverted = scaler.inverse_transform(array)
    return inverted


def invert_normalize(scaler, series_raw, series_predictions_normalized):
    """
        Inverses normalizing by inversing scaling and differencing
    """
    series_predictions_scaled = invert_scale(
        scaler, series_predictions_normalized)
    tmp = list()
    for i in range(len(series_predictions_scaled)):
        value = inverse_difference(
            series_raw, series_predictions_scaled[i], len(series_raw)-i)
        tmp.append(value)
    series_predictions = pd.Series(tmp)
    series_predictions = [x[0]
                          for x in series_predictions]  # index input column
    return series_predictions


def compute_rmse(raw, prediction):
    """
        Computes root mean squared error for two vector
    """
    if len(raw) != len(prediction):
        raise ValueError(
            "raw series and predicition series do not have same input length!")
    rmse = sqrt(mean_squared_error(raw, prediction))
    return rmse
