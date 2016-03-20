from __future__ import print_function
import argparse, sys
from keras.utils import np_utils
import numpy as np
import socket

def validate_sample(data):
    '''A function to verify if data is being sent or not
    '''
    if 'exit' in data:
        return False
    else:
        return True

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
                y = 'GOT DATA:' + str(data) #model.predict(data, batch_size=1, verbose=1)
                c.send(y)
                print(y)
            else:
                c.send('Quitting...')
                print('Quitting...')
                sock.close()

                exit(0)


