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
    #test(appliance='HTE', version='1', new_observations=np.asarray([5.,5.,5.,5.]), expected_forecast =[9.819407320022583,5.408371716737747,5.639165282249451,5.4748483300209045,5.5674134492874146])
    #test(appliance='TVE', version='1', new_observations=np.asarray([ 21.98333333,22.06666667,22.03333333,21.88333333]), expected_forecast =[76.84343070983887,35.84340928395589,37.813823763529456,29.949236901601157,26.86280215581258])
    #test(appliance='SPA', version='1', new_observations=np.asarray([0.01281917,0.36086278,1.3846975,1.77238694]), expected_forecast = [0.18501268227955628,0.25355432507424164,0.7563742261333313,1.8776022226905822,1.8466655378133545])
    test(appliance='WOE', version='1', new_observations=np.asarray([0.03333333,0.01666667,0.01666667,0.]), expected_forecast = [0.012650190914670626,0.026142760769774516,0.01801042101966838,0.017337764617210875,0.005197514314204454])


    
