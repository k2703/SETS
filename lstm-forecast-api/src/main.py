import logging
import view.app as app_rest
from controller.lstm import LSTMSeries
#import view.kafkastreams.app as app_kafkastreams

# Configure logger
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s %(message)s"
    )

# main method
def main():
    
    app_rest.start(appliances = ['FGE','HTE','TVE'])
    
    
# start main method
if __name__ == '__main__':
    main()




