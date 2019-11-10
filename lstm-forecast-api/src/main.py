import logging
import view.app as app_rest
from controller.options import parse_args


# Configure logger
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s %(message)s"
    )


# main method
def main():
    opts = parse_args()
    app_rest.start(opts)


# start main method
if __name__ == '__main__':
    main()
