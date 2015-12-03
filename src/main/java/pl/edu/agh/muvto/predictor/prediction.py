from pybrain.datasets import SupervisedDataSet
from pybrain.tools.shortcuts import buildNetwork
from pybrain.supervised import BackpropTrainer
import sys


class PythonPredictor(object):
    def __init__(self, id, learningrate, momentum, epochs, test_verbose):
        self.id = id
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
            elif old_value == None:
                old_value = val
        return dataSet

    def read_data_from_file(self):
        with open ("src/main/java/pl/edu/agh/muvto/predictor/prediction_data/data_"+self.id+".csv", "r") as myfile:
            file_data=myfile.read().replace('\n', '')

        return file_data.split(',')

    def train(self, dataSet):
        """
        Builds a network and trains it.
        """
        self.network = buildNetwork(dataSet.indim, 4, dataSet.outdim,recurrent=True)
        t = BackpropTrainer(self.network, dataSet, learningrate = self.learningrate, momentum = self.momentum, verbose = False)
        for epoch in range(0, self.epochs):
            t.train()

        return t

    def predict(self, val):
        return (self.network.activate([val]))[0]


    def test(self, trained):
        """
        Builds a new test dataset and tests the trained network on it.
        """
        testdata = SupervisedDataSet(1,1)

        values = self.read_data_from_file()
        old_value = None

        for val in values:
            if old_value != None and val != "":
                testdata.addSample([old_value],[val])
            elif old_value == None:
                old_value = val

        trained.testOnData(testdata, verbose= self.test_verbose)


if len(sys.argv) < 7:
    print("Arguments missed. Use: python prediction.py edge_id learningrate momentum epochs_amount if_test_data value_to_predict")
    exit(-1)


predictor = PythonPredictor(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5])
predictor.test(predictor.train(predictor.make_dataset()))


print(predictor.predict(sys.argv[6]))




