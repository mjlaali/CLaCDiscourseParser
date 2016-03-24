from keras.models import Sequential
from keras.layers import LSTM
from gensim.models.word2vec import Word2Vec
import numpy


class model:
    def __init__(self):
        self.batch_size = 32
        self.nb_classes = 10
        self.nb_epoch = 1
        self.data_augmentation = False
        self.maxlen = 100
        self.w2v_dim = 300
        self._model = None
        self.X = None
        self.Y = None
        self.w2v = None
        self.label_classes = {'arg1': 1, 'arg2': 2, 'dc': 3, 'non': 4}
        self.label_classes_inv = {v: k for k, v in
                                  self.label_classes.iteritems()}

    def build_model(self):
        self._model = Sequential()
        # Skipping in preference of Word2Vec pre-trained data
        # self._model.add(Embedding(max_features, 256, input_length=maxlen))
        self._model.add(LSTM(
            input_length=self.maxlen,
            input_dim=self.w2v_dim,
            output_dim=1,
            activation='sigmoid',
            inner_activation='hard_sigmoid',
            return_sequences=True))
        # self._model.add(Dropout(0.5))
        # self._model.add(Dense(1))
        # self._model.add(Activation('sigmoid'))

        self._model.compile(loss='binary_crossentropy', optimizer='rmsprop')

    def get_data(self, data_file, data_type=None):
        print("Acquiring {0} data from {1}...".format(data_type, data_file))
        discourses, labels = self._read_data_file(data_file, data_type)
        self.X, self.Y = self._preprocess(discourses, labels)
        print('Input Data shape:', self.X.shape)
        print(self.X.shape[0], 'train samples')

        return self.X, self.Y

    def _preprocess(self, discourses, labels=None):
        if labels is None:
            labels = numpy.zeros((len(discourses), self.maxlen, 1))
        if self.w2v is None:
            print("Loading up Word2Vec")
            self.w2v = Word2Vec.load_word2vec_format(
                    'GoogleNews-vectors-negative300.bin', binary=True)
            print("Acquiring Word2Vec Vectors")

        w2v_discourses = numpy.zeros((len(discourses), self.maxlen,
                                      self.w2v_dim))
        w2v_labels = numpy.zeros((len(discourses), self.maxlen, 1))

        for discourse in xrange(len(discourses)):
            for word in xrange(len(discourses[discourse])):
                try:
                    text = discourses[discourse][word]
                    if len(text) >= 1:
                        w2v_discourses[discourse][word] = self.w2v[text]
                        w2v_labels[discourse][word] = labels[discourse][word]
                except KeyError:
                    w2v_discourses[discourse][word] = numpy.random.randn(
                            self.w2v_dim)
                    w2v_labels[discourse][word] = labels[discourse][word]

        return w2v_discourses, w2v_labels

    def _read_data_file(self, data_file, data_type):
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
                        labels[discourse].append(
                                self.label_classes[word_label[1]])
        else:
            print('Did not understand data_type={0}'.format(data_type))
        return data, labels

    def train(self):
        self._model.fit(self.X, self.Y, batch_size=self.batch_size,
                        nb_epoch=self.nb_epoch, show_accuracy=True)

    def save(self, filepath):
        self._model.save_weights(filepath)

    def load(self, filepath):
        if self._model is None:
            self.build_model()
        self._model.load_weights(filepath)

    def test(self, X):
        output_size = len(X)
        X, _ = self._preprocess([X])
        y_hat = self._model.predict(X, batch_size=1, verbose=1)
        y_hat = ' '.join(self._get_labels(y_hat)[:output_size])
        return y_hat

    def _get_labels(self, y_hat):
        labels = []
        for i in range(len(y_hat[0])):
            k = int(numpy.floor(y_hat[0][i][0]))
            k = self.label_classes_inv[k+1]
            labels.append(k)
        return labels
