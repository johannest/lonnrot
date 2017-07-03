package org.vaadin.lonnrot.dl4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Copied and modified from: https://github.com/deeplearning4j/dl4j-examples/
 * blob/master/dl4j-examples/src/main/java/org/deeplearning4j/examples/
 * recurrent/character/GravesLSTMCharModellingExample.java
 */
public class LonnrotModel {

    private String modelFile = "LonnrotNetwork.zip";

    // Hyper parameters
    // Number of units in each GravesLSTM layer
    int lstmLayerSize = 600;
    // Size of mini batch to use when training
    int miniBatchSize = 50;
    // Length of each training example sequence to use
    int exampleLength = 1600;
    // Length for truncated backpropagation through time.
    // i.e., do parameter updates every 75 characters
    int tbpttLength = 75;
    // Total number of training epochs
    int numEpochs = 5;
    // How frequently to generate
    // samples from the network?
    int generateSamplesEveryNMinibatches = 10;
    // Number of samples to generate after each training epoch
    int nSamplesToGenerate = 2;
    // Length of each sample to generate
    int nCharactersToSample = 500;
    // Optional character
    // initialization; a random
    // character is used if null
    String generationInitialization = null;

    // Above is Used to 'prime' the LSTM with a character sequence to
    // continue/complete.
    // Initialization characters must all be in
    // CharacterIterator.getMinimalCharacterSet() by default
    private Random rng = new Random(12345);
    private MultiLayerNetwork model;
    private CharacterIterator characterIterator;

    public void trainModel() {
        // Get a DataSetIterator that handles vectorization of text into
        // something we can use to train
        // our GravesLSTM network.
        try {
            characterIterator = getLonnrotIterator(miniBatchSize,
                    exampleLength);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int nOut = characterIterator.totalOutcomes();

        // Set up network configuration:
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .optimizationAlgo(
                        OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .iterations(1).learningRate(0.15).rmsDecay(0.95).seed(12345)
                .regularization(true).dropOut(0.6).weightInit(WeightInit.XAVIER)
                .updater(Updater.RMSPROP).list()
                .layer(0,
                        new GravesLSTM.Builder()
                                .nIn(characterIterator.inputColumns())
                                .nOut(lstmLayerSize).activation(Activation.TANH)
                                .build())
                .layer(1,
                        new GravesLSTM.Builder().nIn(lstmLayerSize)
                                .nOut(lstmLayerSize).activation(Activation.TANH)
                                .build())
                .layer(2,
                        new RnnOutputLayer.Builder(
                                LossFunctions.LossFunction.MCXENT)
                                        .activation(Activation.SOFTMAX)
                                        .nIn(lstmLayerSize).nOut(nOut).build())

                .backpropType(BackpropType.TruncatedBPTT)
                .tBPTTForwardLength(tbpttLength)
                .tBPTTBackwardLength(tbpttLength).pretrain(false).backprop(true)
                .build();

        model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(50));

        // Print the number of parameters in the network (and for each layer)
        Layer[] layers = model.getLayers();
        int totalNumParams = 0;
        for (int i = 0; i < layers.length; i++) {
            int nParams = layers[i].numParams();
            System.out.println(
                    "Number of parameters in layer " + i + ": " + nParams);
            totalNumParams += nParams;
        }
        System.out.println(
                "Total number of network parameters: " + totalNumParams);

        // Do training, and then generate and print samples from network
        int miniBatchNumber = 0;
        for (int i = 0; i < numEpochs; i++) {
            miniBatchNumber = doTrainEpoch(miniBatchNumber);

            System.out.println("Saving the model");
            // Save the model
            File locationToSave = new File(modelFile);
            try {
                ModelSerializer.writeModel(model, locationToSave, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("\n\nExample complete");
    }

    private int doTrainEpoch(int miniBatchNumber) {
        while (characterIterator.hasNext()) {
            DataSet ds = characterIterator.next();
            model.fit(ds);
            if (++miniBatchNumber % generateSamplesEveryNMinibatches == 0) {
                System.out.println("--------------------");
                System.out.println("Completed " + miniBatchNumber
                        + " minibatches of size " + miniBatchSize + "x"
                        + exampleLength + " characters");
                System.out.println(
                        "Sampling characters from network given initialization \""
                                + (generationInitialization == null ? ""
                                        : generationInitialization)
                                + "\"");
                String[] samples = sampleCharactersFromNetwork(
                        generationInitialization, nCharactersToSample,
                        nSamplesToGenerate);
                for (int j = 0; j < samples.length; j++) {
                    System.out.println("----- Sample " + j + " -----");
                    System.out.println(samples[j]);
                    System.out.println();
                }
            }
        }

        characterIterator.reset();
        return miniBatchNumber;
    }

    public void loadModel() throws IOException {
        model = ModelSerializer.restoreMultiLayerNetwork(System.getProperty("user.home")+"/"+modelFile);
    }

    public void trainMore() {
        int miniBatchNumber = 0;
        for (int i = 0; i < numEpochs; i++) {
            miniBatchNumber = doTrainEpoch(miniBatchNumber);

            System.out.println("Saving the model");
            // Save the model
            File locationToSave = new File(modelFile);
            try {
                ModelSerializer.writeModel(model, locationToSave, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public CharacterIterator getLonnrotIterator(int miniBatchSize,
            int sequenceLength) throws Exception {
        // poems collected by Elias Lonnrot
        String fileLocation = System.getProperty("user.home")+"/raw.txt";

        File f = new File(fileLocation);

        if (!f.exists())
            throw new IOException("File does not exist: " + fileLocation);

        char[] validCharacters = getMinimalCharacterSetWithBasicScaduinavianLetters();

        return new CharacterIterator(fileLocation, Charset.forName("UTF-8"),
                miniBatchSize, sequenceLength, validCharacters,
                new Random(12345));
    }

    /**
     * Generate a sample from the network, given an (optional, possibly null)
     * initialization. Initialization can be used to 'prime' the RNN with a
     * sequence you want to extend/continue.<br>
     * Note that the initalization is used for all samples
     * 
     * @param initialization
     *            String, may be null. If null, select a random character as
     *            initialization for all samples
     * @param charactersToSample
     *            Number of characters to sample from network (excluding
     *            initialization)
     */
    public String[] sampleCharactersFromNetwork(String initialization,
            int charactersToSample, int numSamples) {
        // Set up initialization. If no initialization: use a random character

        if (characterIterator==null) {
            try {
                characterIterator = getLonnrotIterator(miniBatchSize,
                        exampleLength);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (initialization == null || initialization.isEmpty()) {
            initialization = String
                    .valueOf(characterIterator.getRandomCharacter());
        }

        // Create input for initialization
        INDArray initializationInput = Nd4j.zeros(numSamples,
                characterIterator.inputColumns(), initialization.length());
        char[] init = initialization.toCharArray();
        for (int i = 0; i < init.length; i++) {
            int idx = characterIterator.convertCharacterToIndex(init[i]);
            for (int j = 0; j < numSamples; j++) {
                initializationInput.putScalar(new int[] { j, idx, i }, 1.0f);
            }
        }

        StringBuilder[] sb = new StringBuilder[numSamples];
        for (int i = 0; i < numSamples; i++)
            sb[i] = new StringBuilder(initialization);

        // Sample from network (and feed samples back into input) one character
        // at a time (for all samples)
        // Sampling is done in parallel here
        model.rnnClearPreviousState();
        INDArray output = model.rnnTimeStep(initializationInput);

        // Gets the last time step output
        output = output.tensorAlongDimension(output.size(2) - 1, 1, 0);

        for (int i = 0; i < charactersToSample; i++) {
            // Set up next input (single time step) by sampling from previous
            // output
            INDArray nextInput = Nd4j.zeros(numSamples,
                    characterIterator.inputColumns());
            // Output is a probability distribution. Sample from this for each
            // example we want to generate, and add it to the new input
            for (int s = 0; s < numSamples; s++) {
                double[] outputProbDistribution = new double[characterIterator
                        .totalOutcomes()];
                for (int j = 0; j < outputProbDistribution.length; j++)
                    outputProbDistribution[j] = output.getDouble(s, j);
                int sampledCharacterIdx = sampleFromDistribution(
                        outputProbDistribution, rng);

                // Prepare next time step input
                nextInput.putScalar(new int[] { s, sampledCharacterIdx }, 1.0f);
                // Add sampled character to StringBuilder (human readable
                // output)
                sb[s].append(characterIterator
                        .convertIndexToCharacter(sampledCharacterIdx));
            }

            // Do one time step of forward pass
            output = model.rnnTimeStep(nextInput);
        }

        String[] out = new String[numSamples];
        for (int i = 0; i < numSamples; i++)
            out[i] = sb[i].toString();
        return out;
    }

    /**
     * Given a probability distribution over discrete classes, sample from the
     * distribution and return the generated class index.
     * 
     * @param distribution
     *            Probability distribution over classes. Must sum to 1.0
     */
    public int sampleFromDistribution(double[] distribution, Random rng) {
        double d = 0.0;
        double sum = 0.0;
        for (int t = 0; t < 10; t++) {
            d = rng.nextDouble();
            sum = 0.0;
            for (int i = 0; i < distribution.length; i++) {
                sum += distribution[i];
                if (d <= sum)
                    return i;
            }
            // If we haven't found the right index yet, maybe the sum is
            // slightly
            // lower than 1 due to rounding error, so try again.
        }
        // Should be extremely unlikely to happen if distribution is a valid
        // probability distribution
        throw new IllegalArgumentException(
                "Distribution is invalid? d=" + d + ", sum=" + sum);
    }

    /**
     * A minimal character set, with a-z, A-Z, 0-9 and common punctuation, ä, ö,
     * å etc
     */
    public char[] getMinimalCharacterSetWithBasicScaduinavianLetters() {
        List<Character> validChars = new LinkedList<>();
        for (char c = 'a'; c <= 'z'; c++)
            validChars.add(c);
        for (char c = 'A'; c <= 'Z'; c++)
            validChars.add(c);
        for (char c = '0'; c <= '9'; c++)
            validChars.add(c);
        char[] temp = { '!', '&', '(', ')', '?', '-', '\'', '"', ',', '.', ':',
                ';', ' ', '\n', '\t', 'ä', 'Ä', 'ö', 'Ö', 'å', 'Å' };
        for (char c : temp)
            validChars.add(c);
        char[] out = new char[validChars.size()];
        int i = 0;
        for (Character c : validChars)
            out[i++] = c;
        return out;
    }

}
