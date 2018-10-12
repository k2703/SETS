from controller.helpers import *
import numpy as np


class LSTMSeries:


    def __init__(self, train_values_name, model_name, scaler_name):
        self.series = load_ndarray(train_values_name)
        self.lstm_model = load_model(model_name)
        self.scaler = load_scaler(scaler_name)


    def init_state(self, train_norm_name):
        train_normalized = load_ndarray(train_norm_name)
        train_normalized_reshaped = train_normalized[:, 0].reshape(len(train_normalized), 1, 1)
        self.lstm_model.predict(train_normalized_reshaped, batch_size=1)


    def append(self, value):
        self.series = np.concatenate((self.series,np.asarray([[value]])),axis=0)


    def predict(self):
        series_head = self.series[-3:]
        diff_input = difference(series_head, 1)
        # transform data to be supervised learning
        supervised_input = timeseries_to_supervised(diff_input, 1)
        supervised_input_values = supervised_input.values
        input_normalized = np.concatenate((self.scaler.transform(supervised_input_values)[:,0], self.scaler.transform(supervised_input_values)[-1,1]),axis=None)
        input_normalized_reshaped = np.asarray([input_normalized[-1]]).reshape(1, 1, 1)
        normalized_predictions = self.lstm_model.predict(input_normalized_reshaped, batch_size=1)
        return invert_normalize(self.scaler, series_head[-1], normalized_predictions)






'''[40.865450922648108,
 30.071747080485025,
 40.229114532470703,
 30.222007878621419,
 47.404639689127606]'''