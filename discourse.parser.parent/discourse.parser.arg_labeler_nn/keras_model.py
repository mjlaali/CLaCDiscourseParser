from __future__ import print_function
import argparse
import os
import sys
import socket
from model import model


def printn(string_to_print):
    print(string_to_print)
    sys.stdout.flush()


def validate_sample(data):
    '''
    A function to verify if data is being sent or not
    '''
    if 'exit' in data:
        return False
    else:
        return True


def prepare_test_data(test_data):
    with open(test_data, 'r') as f:
        test_data = f.read()
    test_data = test_data.split('\n')
    for i in range(len(test_data)):
        test_data[i] = test_data[i].split(' ')
    return test_data


def run_tests(model, test_set, save_to_file):
    for data in prepare_test_data(test_set):
        y_hats.append(model.test(data))
    with open('results/{0}'.format(save_to_file), 'w') as f:
        for y_hat in y_hats:
            f.write(y_hat + '\n')

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("-tr", "--train", help="Initailize the training process with given training data")
    parser.add_argument("-m", "--model-file", help="Serialize or load the model from the given file")
    parser.add_argument("-t", "--test", help="Test the given data and produce results")
    parser.add_argument("-l", "--log", help="Logs test set for future direct runs")
    parser.add_argument("-s", "--save-models", help="Saves models at every epoch in provided folder. Not in socket mode")
    if len(sys.argv) == 1:
            parser.printn_help()
            sys.exit()
    args = parser.parse_args()

    printn("Building Model")
    m = model()

    if args.train is not None:
        m.build_model()
        printn("Training Process Initiated")
        m.get_data(args.train, "training")

        if args.save_models is not None:
            m.set_callbacks(args.save_models)

        m.train()
        printn("Training Complete!")

        if args.model_file is not None:
            printn("Storing weights for the model at {0}".format(
                args.model_file))
            m.save(args.model_file)

        exit(0)

    if args.test is not None:
        printn("Testing Process Initiated...")
        m = model()

        if '.' in args.model_file:
            printn("Loading Model...")
            m.load(args.model_file)
            printn("Model loaded!")
        elif args.model_file is not None and os.path.isdir(args.model_file):
            printn("Testing multiple models")
            args.model_file = [m_file for m_file in os.listdir(
                args.model_file) if '.hdf5' in m_file]
            if len(args.model_file) == 0:
                printn("Model Weights not found in directory! Quitting.")
                exit(1)
        else:
            printn("Model Weights not found! Quitting.")
            exit(1)

        if ':' in args.test:
            if args.log is not None:
                open(args.log, 'w').close()

            printn("Initializing Testing Server...")
            sock = socket.socket()
            [host, port] = args.test.split(':')
            sock.bind((host, int(port)))
            sock.listen(5)
            printn("Testing Server created. Waiting for input")
            c, addr = sock.accept()
            printn("Accepted connection from " + str(addr))
            while True:
                data = c.recv(1024)
                if validate_sample(data):
                    printn ('Recieved: {0}'.format(data))
                    data = data.split(' ')[:-1]
                    printn ('Length: {0}'.format(len(data)))
                    with open(args.log, 'a') as f:
                        f.write(' '.join(data) + '\n')
                    y_hat = m.test(data)
                    printn ('Result: {0}'.format(y_hat + '\n'))
                    printn ('Result Length {0}'.format(len(y_hat.split(' '))))
                    printn ('---------------------------------------------------------------')
                    c.send(y_hat + '\n')
                else:
                    printn('Quitting...')
                    sock.close()
        elif args.test is not None:
            y_hats = []
            if isinstance(args.model_file, list):
                if not os.path.isdir('results'):
                    os.makedirs('results')
                for model_file in args.model_file:
                    m.load(model_file)
                    printn('Model {0} loaded.'.format(model_file))
                    run_tests(model=m,
                              test_set=args.test,
                              save_to_file=args.model_file)
                    printn('Results computed for {0}'.format(model_file))
            else:
                    run_tests(model=m,
                              test_set=args.test,
                              save_to_file=args.model_file.split('/')[-1])

        printn('Quitting...')
        exit(0)
