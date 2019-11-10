import controller.preprocess as preprocess
from keras.models import model_from_json
# from sklearn.externals import joblib
import joblib
import numpy as np
import os


class LSTMSeries:
    """
        LSTMSeries class handles online usage of lstm model.
        It automatically predicts the next value of a time series, manages
        observations and inverts normalizations as expected.
    """

    def __init__(self, opts, train_values_name, model_name, scaler_name):
        """
            Constructor inits series of true observations, the lstm model and
            the corresponding scaler object
        """
        self.model_path = opts.model_path
        self.data_path = opts.data_path
        self.series = self.load_ndarray(train_values_name)
        self.lstm_model = self.load_model(model_name)
        self.scaler = self.load_scaler(scaler_name)

    def load_model(self, model_name):
        """
            Loads model
        """
        home_dir = os.path.dirname(os.getcwd()).replace(' ', '\ ') + \
            self.model_path

        # load json and create model
        json_file = open(home_dir+model_name+".json", 'r')
        loaded_model_json = json_file.read()
        json_file.close()
        lstm_model = model_from_json(loaded_model_json)
        lstm_model.load_weights(home_dir+model_name+".h5")
        return lstm_model

    def load_scaler(self, scaler_name):
        """
            Loads scaler object which was used for preprocessing for data
            in order to train model
        """
        home_dir = os.path.dirname(os.getcwd()).replace(' ', '\ ') + \
            self.data_path
        scaler_filename = home_dir+scaler_name
        scaler = joblib.load(scaler_filename)
        return scaler

    def load_ndarray(self, ndarray_name):
        """
            Loads any ndarray object
        """
        home_dir = os.path.dirname(os.getcwd()).replace(
            ' ', '\ ') + self.data_path
        ndarray_filename = home_dir + ndarray_name+".npy"
        ndarray = np.load(ndarray_filename)
        return ndarray

    def init_state(self, train_norm_name):
        """
            Predicts the entire training set in order to update hidden state
            for forecasts on test data
        """
        train_normalized = self.load_ndarray(train_norm_name)
        train_normalized_reshaped = train_normalized[:, 0].reshape(
            len(train_normalized), 1, 1)
        self.lstm_model.predict(train_normalized_reshaped, batch_size=1)

    def append(self, value):
        """
            Adds current true observation to series
        """
        self.series = np.concatenate(
            (self.series, np.asarray([[value]])), axis=0)

    def predict(self):
        """
            Takes last true observations from series, normalizes them, predicts
            and finally inverts the normalization
        """
        series_head = self.series[-3:]
        diff_input = preprocess.difference(series_head, 1)
        # build supervised training set
        supervised_input = preprocess.timeseries_to_supervised(diff_input, 1)
        supervised_input_values = supervised_input.values
        input_normalized = np.concatenate(
            (self.scaler.transform(supervised_input_values)[:, 0],
            self.scaler.transform(supervised_input_values)[-1, 1]),
            axis=None)
        input_normalized_reshaped = np.asarray(
            [input_normalized[-1]]).reshape(1, 1, 1)
        normalized_predictions = self.lstm_model.predict(
            input_normalized_reshaped, batch_size=1)

        return preprocess.invert_normalize(self.scaler,
                                           series_head[-1],
                                           normalized_predictions)
