from keras.models import Sequential
from keras.layers import GRU, Activation, Dropout, TimeDistributedDense
from keras.callbacks import ModelCheckpoint
from gensim.models.word2vec import Word2Vec
import numpy
import pickle
import os
import theano


class model:
    def __init__(self):
        numpy.random.seed(1337)
        self.batch_size = 32
        self.nb_epoch = 1
        self.data_augmentation = False
        self.maxlen = 300
        self.w2v_dim = 300
        self.callback = None
        self._model = None
        self.X = None
        self.Y = None
        self.w2v = None
        self.non_w2v = {}
        self.save_non_w2v = False
        self.label_classes = {'arg1': 1,
                              'arg2': 2,
                              'non':  0,
                              'dc':   0}
        self.output_dim = max(self.label_classes.itervalues()) + 1
        self.label_classes_inv = {v: k for k, v in
                                  self.label_classes.iteritems()}
        self.label_classes_inv[0] = 'non'
        if os.path.isfile('non_w2v.pkl'):
            self.non_w2v = pickle.load(open('non_w2v.pkl', 'rb'))

    def build_model(self):
        self._model = Sequential()

        #self._model.add(Embedding(
        #    input_dim=self.w2v_dim,
        #    output_dim=self.w2v_dim,
        #    input_length=self.maxlen))
        self._model.add(GRU(
            input_dim=self.w2v_dim,
            input_length=self.maxlen,
            output_dim=100,
            activation='sigmoid',
            inner_activation='hard_sigmoid',
            return_sequences=True))
        self._model.add(Dropout(0.5))
        self._model.add(TimeDistributedDense(
            output_dim=self.output_dim))
        self._model.add(Activation('softmax'))

        def loss_function(coding_dist, true_dist):
#            from pudb import set_trace; set_trace()
            return -theano.tensor.sum(true_dist - coding_dist, axis=coding_dist.ndim - 1)

        self._model.compile(loss='categorical_crossentropy', optimizer='adam')

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
        w2v_labels = numpy.zeros((len(discourses), self.maxlen,
                                 self.output_dim))

        for discourse in xrange(len(discourses)):
            for word in xrange(len(discourses[discourse])):
                try:
                    text = discourses[discourse][word]
                    if len(text) >= 1:
                        w2v_discourses[discourse][word] = self.w2v[text]
                        w2v_labels[discourse][word] = labels[discourse][word]
                except KeyError:
                    if text not in self.non_w2v:
                        self.save_non_w2v = True
                        self.non_w2v[text] = numpy.random.randn(self.w2v_dim)
                    w2v_discourses[discourse][word] = self.non_w2v[text]
                    w2v_labels[discourse][word] = labels[discourse][word]

        if self.save_non_w2v:
            pickle.dump(self.non_w2v, open("non_w2v.pkl", "wb"))

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
                                self._label_to_array(word_label[1]))
        else:
            print('Did not understand data_type={0}'.format(data_type))
        return data, labels

    def set_callbacks(self, path):
        self.callback = [ModelCheckpoint(
                            filepath=path + '/weights.{epoch:02d}.hdf5',
                            monitor='val_loss',
                            save_best_only=False,
                            verbose=1,
                            mode='auto')]

    def train(self, validation_sample=None):
        # Remove references to word2vec to release some memory for training
        self.w2v = None

        if self.callback is None:
            self._model.fit(self.X, self.Y, batch_size=self.batch_size,
                            nb_epoch=self.nb_epoch, show_accuracy=True,
                            validation_data=validation_sample)
        else:
            self._model.fit(self.X, self.Y, batch_size=self.batch_size,
                            nb_epoch=self.nb_epoch, show_accuracy=True,
                            validation_data=validation_sample,
                            callbacks=self.callback)

    def save(self, filepath):
        self._model.save_weights(filepath)

    def load(self, filepath):
        if self._model is None:
            self.build_model()
        self._model.load_weights(filepath)

    def test(self, X):
        # from pudb import set_trace; set_trace()
        output_size = len(X)
        X, _ = self._preprocess([X])
        y_hat = self._model.predict(X, batch_size=1, verbose=1)
        y_hat = ' '.join(self._get_labels(y_hat[0])[:output_size])
        return y_hat

    def _get_labels(self, y_hat):
        labels = []
        for i in range(len(y_hat)):
            k = numpy.argmax(y_hat[i])
            k = self.label_classes_inv[k]
            labels.append(k)
        return labels

    def _label_to_array(self, label):
        arr = numpy.zeros(self.output_dim)
        arr[self.label_classes[label]] += 1
        return arr
