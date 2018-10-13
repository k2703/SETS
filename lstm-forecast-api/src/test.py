from controller.lstm import LSTMSeries
import numpy as np


def test(appliance, version, new_observations, expected_forecast):

    lstm = LSTMSeries(appliance + '_train_values',appliance + '_model_' +  version, appliance + '_scaler')
    lstm.init_state(appliance + '_train_normalized')

    lstm_forecast = []

    lstm_forecast.append(lstm.predict())

    for obs in new_observations:
        lstm.append(obs)
        lstm_forecast.append(lstm.predict())

    print(lstm_forecast)

    np.testing.assert_almost_equal(expected_forecast, [x[0] for x in lstm_forecast], 5)


if __name__ == "__main__":
    #test(appliance='FGE', version='1', new_observations=np.asarray([59.51666667,19.75,55.38333333,167.46666667]), expected_forecast =[40.865450922648108,30.071747080485025,40.229114532470703,30.222007878621419,47.404639689127606])
    test(appliance='HTE', version='1', new_observations=np.asarray([5.,5.,5.,5.]), expected_forecast =[9.819407320022583,5.408371716737747,5.639165282249451,5.4748483300209045,5.5674134492874146])
