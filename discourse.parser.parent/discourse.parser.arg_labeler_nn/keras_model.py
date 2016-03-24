from __future__ import print_function
import argparse
import sys
import socket
from model import model


def validate_sample(data):
    '''
    A function to verify if data is being sent or not
    '''
    if 'exit' in data:
        return False
    else:
        return True


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("-tr", "--train", help="Initailize the training process with given training data")
    parser.add_argument("-m", "--model-file", help="Serialize or load the model from the given file")
    parser.add_argument("-t", "--test", help="Test the given data and produce results")
    if len(sys.argv) == 1:
            parser.print_help()
            sys.exit()
    args = parser.parse_args()

    print("Building Model")
    m = model()

    if args.train is not None:
        m.build_model()
        print("Training Process Initiated")
        m.get_data(args.train, "training")
        m.train()
        print("Training Complete!")

        if args.model_file is not None:
            print("Storing weights for the model at {0}".format(
                args.model_file))
            m.save(args.model_file)

        exit(0)

    if args.test is not None:
        print("Testing Process Initiated...")
        m = model()

        if args.model_file is not None:
            print("Loading Model...")
            m.load(args.model_file)
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
                    data = data.split(' ')[:-1]
                    y_hat = m.test(data)
                    c.send(y_hat + '\n')
                else:
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
                    y_hat = m.test(data)
                    print(y + str(y_hat))
                print('Quitting...')
        exit(0)
