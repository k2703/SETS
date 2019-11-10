import argparse


def parse_args():
    parser = argparse.ArgumentParser(description='Process some integers.')
    parser.add_argument('--model_path', type=str, default="/src/model/models/",
                        help='define location of models')
    parser.add_argument('--data_path', type=str, default="/src/model/data/",
                        help='define location of data')
    return parser.parse_args()
