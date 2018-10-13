from flask import Flask, jsonify, request
from controller.lstm import LSTMSeries

import numpy as np
import logging


# set global vars
app = Flask(__name__)

def start(appliances):
    """ Starts a REST server with Cognitive Tagging API """
    # debug
    logging.info("--- Start LSTM Forecast API ---")
    logging.info("Load model ...")

    global lstm_models
    lstm_models = {}

    for appliance in appliances:
        logging.info("Initialize model for appliance: " + appliance)
        lstm_models[appliance] = LSTMSeries(appliance + '_train_values',appliance+'_model_1',appliance+'_scaler')
        lstm_models[appliance].init_state(appliance+'_train_normalized')
    
    logging.info("Start web service ...")
    app.run(host='0.0.0.0')


"""global lstm
lstm = LSTMSeries('FGE_train_values','FGE_model_1','FGE_scaler')

logging.info("Initialize model ...")

lstm.init_state('FGE_train_normalized')

logging.info("Start web service ...")
app.run(host='0.0.0.0')"""


# handle error 500  Internal Server Error
@app.errorhandler(500)
def internal_error(error):
    return jsonify({'error': "Internal Server Error. Bitte die Logdatei für Details anschauen."}), 500


# handle error 400 Bad Response  
@app.errorhandler(400)
def internal_error_400(error):
    return jsonify({'error': "Die Anfrage wurde syntaktisch falsch erstellt."}), 400



@app.route('/lstmforecast/<string:appliance>/append', methods=['POST'])
def append(appliance):

    # Handle optional params
    if request.json.get('observation') == None:
        observation = None
    else:
        observation = request.json.get('observation')
    
    print(observation)
    # append value
    lstm_models[appliance].append(float(observation))

    # logging
    logging.info("Response for "+ str(request.remote_addr) + ": observation: '"+ observation + "' appended to series.")

    return jsonify({'observation': observation}), 201



@app.route('/lstmforecast/<string:appliance>/predict', methods=['GET'])
def do_lstm_forecast(appliance):

    value = lstm_models[appliance].predict()   

    # logging
    logging.info("Response for "+ str(request.remote_addr) + ": Forecast: "+ str(value))

    return jsonify({'tasks': str(value)}), 200
