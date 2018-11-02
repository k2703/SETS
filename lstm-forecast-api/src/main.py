import logging
import view.app as app_rest
import argparse


# Configure logger
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s %(message)s"
    )


# main method
def main():
    app_rest.start()
    

# start main method
if __name__ == '__main__':
    main()




