from flask import Flask, jsonify, request
from controller.lstm import LSTMSeries
from keras import backend as K
import logging


# set global vars
app = Flask(__name__)
opts = None


def start(parse_opts):
    """
        Starts a REST server
    """
    global opts
    opts = parse_opts
    app.run(host='0.0.0.0')


@app.route('/lstmforecast/deploy-model', methods=['POST'])
def deploymodel():
    """
        Deploys a list of lstm model ready for online evaluation
    """
    appliances = request.json.get('appliances')
    print("APPLIANCES  :    " + str(appliances))
    if len(appliances) != 0:

        # debug
        logging.info("--- Start LSTM Forecast API ---")
        logging.info("Load model ...")

        global lstm_models
        lstm_models = {}

        for appliance in appliances:
            logging.info("Initialize model for appliance: " + appliance)
            lstm_models[appliance] = LSTMSeries(
                opts, appliance + '_train_values',
                appliance+'_model_1', appliance+'_scaler')
            lstm_models[appliance].init_state(appliance+'_train_normalized')
        logging.info("Start web service ...")
    else:
        print("Please provide some appliance "
              "for which models shall be started")
    return jsonify({'currently-deployed-models': str(appliances)}), 200


@app.route('/lstmforecast/pullback-model', methods=['POST'])
def pullbackmodel():
    """
        Pulls back all current deployed models
    """
    global lstm_models
    lstm_models = {}
    K.clear_session()
    return jsonify({'currently-deployed-models':
                    str(list(lstm_models.keys()))}), 200


@app.errorhandler(500)
def internal_error(error):
    """
        Handles error 500  Internal Server Error
    """
    return jsonify({'error': "Internal Server Error. "
                    "Bitte die Logdatei für Details anschauen."}), 500


@app.errorhandler(400)
def internal_error_400(error):
    """
        Handles error 400  Internal Server Error
    """
    return jsonify({'error':
                    "Die Anfrage wurde syntaktisch falsch erstellt."}), 400


@app.route('/lstmforecast/<string:appliance>/predict', methods=['POST'])
def append(appliance):
    """
        Appends a current true observation to one of the current
        deployed LSTM models
    """

    # Handle optional params
    if request.json.get('observation') != '':
        observation = request.json.get('observation')

        # append value
        lstm_models[appliance].append(float(observation))

        # logging
        logging.info("Response for " + str(request.remote_addr) +
                     ": observation: '" + observation +
                     "' appended to series.")

    value = lstm_models[appliance].predict()

    # logging
    logging.info("Response for " + str(request.remote_addr) +
                 ": Forecast: " + str(value))

    return jsonify({'prediction': str(value)}), 200
