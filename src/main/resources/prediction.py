from pybrain.datasets import SupervisedDataSet
from pybrain.tools.shortcuts import buildNetwork
from pybrain.supervised import BackpropTrainer
import sys
import pickle
import os.path

from pybrain.tools.shortcuts import buildNetwork
from pybrain.tools.customxml import NetworkWriter
from pybrain.tools.customxml import NetworkReader


class PythonPredictor(object):
    def __init__(self, path, predictor_path, learningrate, momentum, epochs, test_verbose):
        self.path = path
        self.predictor_path = predictor_path
        self.network = None
        self.learningrate = float(learningrate)
        self.momentum = float(momentum)
        self.epochs = int(epochs)
        
        if test_verbose == "true":
            self.test_verbose = True
        else:
            self.test_verbose = False


    def make_dataset(self):
        """
        Creates a set of training data.
        """
        dataSet = SupervisedDataSet(1,1)

        values = self.read_data_from_file()
        old_value = None

        for val in values:
            if old_value != None and val != "":
                dataSet.addSample([old_value],[val])
                old_value = val
            elif old_value == None:
                    old_value = val
        return dataSet

    def read_data_from_file(self):
        with open (self.path, "r") as myfile:
            file_data=myfile.read().replace('\n', '')

        return file_data.split(',')

    def train(self, dataSet):
        """
        Builds a network and trains it.
        """
        if os.stat(self.predictor_path).st_size != 0:
            self.network = NetworkReader.readFrom(self.predictor_path)
        else:
            self.network = buildNetwork(dataSet.indim, 4, dataSet.outdim,recurrent=True)

        t = None

        if len(dataSet) > 0:
            t = BackpropTrainer(self.network, dataSet, learningrate = self.learningrate, momentum = self.momentum, verbose = False)
            for epoch in range(0, self.epochs):
                t.train()

        NetworkWriter.writeToFile(self.network, self.predictor_path)

        return t

    def predict(self, val, steps):
        result = val
        for step in range(0, int(steps)):
            result = (self.network.activate([result]))[0]
            print(result)

    def test(self, trained):
        """
        Builds a new test dataset and tests the trained network on it.
        """
        if trained != None:
            testdata = SupervisedDataSet(1,1)

            values = self.read_data_from_file()
            old_value = None

            for val in values:
                if old_value != None and val != "":
                    testdata.addSample([old_value],[val])
                elif old_value == None:
                    old_value = val

            trained.testOnData(testdata, verbose= self.test_verbose)


if __name__ == '__main__':
    if len(sys.argv) < 9:
        print("Arguments missed. Use: python prediction.py path_to_file path_to_predictor_file learningrate momentum epochs_amount if_test_data value_to_predict steps")
        exit(-1)


    predictor = PythonPredictor(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5], sys.argv[5])
    predictor.test(predictor.train(predictor.make_dataset()))

    predictor.predict(sys.argv[7], sys.argv[8])




