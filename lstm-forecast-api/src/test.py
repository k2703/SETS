from controller.lstm import LSTMSeries
from controller.options import parse_args
import numpy as np
import unittest


class TestLstmForecast(unittest.TestCase):

    def get_n_prediction(self, appliance, version, n):
        opts = parse_args()
        lstm = LSTMSeries(opts, appliance + '_train_values',
                          appliance + '_model_' + version,
                          appliance + '_scaler')
        lstm.init_state(appliance + '_train_normalized')
        lstm_forecast = []
        lstm_forecast.append(lstm.predict()[0])
        for i in range(n):
            lstm.append(lstm_forecast[-1])
            lstm_forecast.append(lstm.predict()[0])
        return lstm_forecast

    def test_prediction_type(self):
        lstm_forecast = self.get_n_prediction(
            appliance='FGE', version='1', n=3)
        print(lstm_forecast)
        for pred in lstm_forecast:
            self.assertIsInstance(pred, np.float32)


if __name__ == '__main__':
    unittest.main()
