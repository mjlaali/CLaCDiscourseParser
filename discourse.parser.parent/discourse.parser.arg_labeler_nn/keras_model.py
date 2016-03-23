from __future__ import print_function
import argparse, sys
from keras.utils import np_utils
import numpy as np
import socket


def validate_sample(data):
    '''
    A function to verify if data is being sent or not
    '''
    if 'exit' in data:
        return False
    else:
        return True


def get_labels(y_hat):
    print("This is what y_hat looks like:")
    print(y_hat.shape)
    labels = []
    for i in range(len(y_hat[0])):
        k = int(numpy.floor(y_hat[0][i][0]))
        k = label_classes_inv[k+1]
        labels.append(k)
    return labels

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("-tr" ,"--train", help="Initailize the training process with given training data")
    parser.add_argument("-m", "--model-file", help="Serialize or load the model from the given file")
    parser.add_argument("-t", "--test", help="Test the given data and produce results")
    if len(sys.argv)==1:
            parser.print_help()
            sys.exit()
    args = parser.parse_args()

    print("Building Model")
    from model import *

    if args.train is not None:
        print("Training Process Initiated")
        X_train, Y_train = getData(args.train, "training")
        print('X_train: {0}'.format(X_train.shape))
        print('Y_train: {0}'.format(Y_train.shape))
        model.fit(X_train, Y_train, batch_size=batch_size,
                  nb_epoch=nb_epoch, show_accuracy=True)
        print("Training Complete!")

        if args.model_file is not None:
            print("Storing weights for the model at {0}".format(args.model_file))
            model.save_weights(args.model_file)

        exit(0)

    if args.test is not None:
        print("Testing Process Initiated...")

        if args.model_file is not None:
            print("Loading Model...")
            model.load_weights(args.model_file)
            print("Model loaded!")
        else:
            print("Model Weights not found! Quitting.")
            exit(1)

        if 'trial' not in args.test:
            print("Initializing Testing Server...")
            sock = socket.socket()
            [host, port] = args.test.split(':')
            sock.bind((host, int(port)))
            sock.listen(5)
            print("Testing Server created. Waiting for input")
            c, addr = sock.accept()
            print("Accepted connection from " + str(addr))
            while True:
                data = c.recv(1024)
                if validate_sample(data):
                    y = 'GOT DATA:' + str(data) + ' Predicted:'
                    print('Recieved: ', data)
                    data = data.split(' ')[:-1]
                    print('Length of data is:', len(data))
                    output_size = len(data)
                    data, _ = preprocess([data])
                    y_hat = model.predict(data, batch_size=1, verbose=1)
                    y_hat = ' '.join(get_labels(y_hat)[:output_size])
                    print('length of y_hat is: ', len(y_hat.split(' ')))
                    print('y_hat is:', y_hat)
                    c.send(y_hat + '\n')
                    print(y + y_hat)
                else:
                    c.send('Quitting...')
                    print('Quitting...')
                    sock.close()
        else:
            with open('test.data', 'r') as f:
                data = f.read()
            test_data = data.split('\n')
            for data in test_data:
                if validate_sample(data):
                    y = 'GOT DATA:' + str(data) + ' Predicted:'
                    print('Recieved: ', data)
                    data = data.split(' ')
                    data, _ = preprocess([data])
                    y_hat = model.predict(data, batch_size=1, verbose=1)
                    y_hat = get_labels(y_hat)
                    print(y + str(y_hat))
                print('Quitting...')
        exit(0)
