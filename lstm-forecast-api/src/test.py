from controller.lstm import LSTMSeries
import numpy as np


if __name__ == "__main__":


    lstm = LSTMSeries('FGE_train_values','FGE_model_1','FGE_scaler')

    lstm.init_state('FGE_train_normalized')

    new_observations = np.asarray([59.51666667,19.75,55.38333333,167.46666667])

    lstm_forecast = []

    lstm_forecast.append(lstm.predict())

    for obs in new_observations:
        lstm.append(obs)
        lstm_forecast.append(lstm.predict())

    print(lstm_forecast)

    expected_forecast = [40.865450922648108,30.071747080485025,40.229114532470703,30.222007878621419,47.404639689127606]
    np.testing.assert_almost_equal(expected_forecast, [x[0] for x in lstm_forecast], 5)