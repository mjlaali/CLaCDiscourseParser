from keras.models import Sequential
from keras.layers import LSTM
from gensim.models.word2vec import Word2Vec
import numpy
# from pudb import set_trace; set_trace()
###############################################
######### ADD PREPROCESSING INFO HERE #########
###############################################
batch_size = 32
nb_classes = 10
nb_epoch = 1
data_augmentation = False
idim = 5
ilen = 3
maxlen = 100
w2v_dim = 300

###############################################
############## DEFINE MODEL HERE ##############
###############################################
model = Sequential()

# Skipping in preference of  Word2Vec pre-trained data
# model.add(Embedding(max_features, 256, input_length=maxlen))
# Input_Length = Length of an instance. Specified only if its a constant
# Input_dim = Dimensionality of the input..?
# Output_dim =
model.add(LSTM(input_length=maxlen, input_dim=w2v_dim, output_dim=1, activation='sigmoid', inner_activation='hard_sigmoid', return_sequences=True))
# model.add(Dropout(0.5))
# model.add(Dense(1))
# model.add(Activation('sigmoid'))


###############################################
####### DEFINE LOSS AND OPTIMIZER HERE  #######
###############################################
model.compile(loss='binary_crossentropy', optimizer='rmsprop')


###############################################
###### DEFINE HOW TO GET THE DATA LOADED ######
###############################################
label_classes = {'arg1': 1, 'arg2': 2, 'dc': 3, 'non': 4}


def getData(data_file, data_type=None):
    # X is a 3D Tensor in the form = (number_of_samples, timesteps, input_dim)
    # timestep in this case is the length of a discourse (number of words per instance)
    # input_dim in this case is the number of dimensions for the word (from W2V Embedding)
    # X, Y = numpy.random.rand(5, ilen, idim), numpy.random.rand(5, ilen, 1)
    # print ("X:{0}".format(X))
    # print ("Y:{0}".format(Y))
    # return X,Y
    print("Loading up Word2Vec")
    w2v = Word2Vec.load_word2vec_format('GoogleNews-vectors-negative300.bin', binary=True)
    print("Acquiring {0} data from {1}...".format(data_type, data_file))
    discourses, labels = readDataFile(data_file, data_type)
    print("Data Acquired")
    print("Acquiring Word2Vec Vectors")

    w2v_discourses= numpy.zeros((len(discourses), maxlen, w2v_dim))
    w2v_labels = numpy.zeros((len(discourses), maxlen, 1))

    for discourse in xrange(len(discourses)):
        for word in xrange(len(discourses[discourse])):
            try:
                text = discourses[discourse][word]
                if len(text) > 2:
                    w2v_discourses[discourse][word] = w2v[text]
                    w2v_labels[discourse][word] = labels[discourse][word]
            except KeyError:
                print('Word2Vec does not have an embedding for {0}'.format(text))

    X = w2v_discourses
    y = w2v_labels
    print('Input Data shape:', X.shape)
    print(X.shape, 'train samples')
    print(y.shape, 'test samples')

    # convert class vectors to binary class matrices
    # '''PROBLEMS HERE IN THE NEXT LINE SINCE y IS NOW 3D'''
    Y = y  # np_utils.to_categorical(y, nb_classes)
    X = X  # .astype('float32')

    return X, Y


def readDataFile(data_file, data_type):
    labels = []
    if 'train' in data_type.lower():
        with open(data_file) as f:
            data = f.read()
            data = data.split('\n\n')
        for discourse in xrange(len(data)):
            data[discourse] = data[discourse].split('\n')
            labels.append([])
            for w_l in xrange(len(data[discourse])):
                if len(data[discourse][w_l]) > 6:
                    word_label = data[discourse][w_l].split(' ')
                    data[discourse][w_l] = word_label[0][5:]
                    labels[discourse].append(label_classes[word_label[1]])
    else:
        print('Did not understand data_type={0}'.format(data_type))
    return data, labels
