package languagerecognition;

import java.util.HashMap;
import java.util.Map;

import static java.lang.System.out;
import static java.util.Map.Entry;
import static languagerecognition.Utilities.normalizeVector;

class LanguageRecognizer {
    private static final int MAXIMUM_NUMBER_OF_EPOCHS;
    private static final double LEARNING_RATE;
    private static final double PERMITTED_ERROR;
    private static final Map<String, double[]> normalizedTrainingObservations;

    static {
        MAXIMUM_NUMBER_OF_EPOCHS = 750;
        LEARNING_RATE = 0.25;
        PERMITTED_ERROR = 0.4;
        normalizedTrainingObservations = new HashMap<>();
    }

    /*
     * Language, in which this perceptron specializes.
     */
    final String language;
    private double[] weightsAndThreshold;

    private LanguageRecognizer(final String language) {
        this.language = language;
    }

    static LanguageRecognizer specializingIn(final String language) {
        return new LanguageRecognizer(language);
    }

    static void
    putNewTrainingObservation(final String language,
                              final int[] englishLettersAccumulations) {
        if (englishLettersAccumulations.length != 26) {
            throw new IllegalArgumentException(
                "There must be 26 accumulations of English letters!"
            );
        }

        final double[] normalizedEnglishLettersAccumulations = new double[27];

        int index = 0;
        while (index < 26) {
            normalizedEnglishLettersAccumulations[index] =
                englishLettersAccumulations[index];

            ++(index);
        }

        normalizedEnglishLettersAccumulations[26] = (-1);

        Utilities.normalizeVector(normalizedEnglishLettersAccumulations);

        normalizedTrainingObservations.put(
            language, normalizedEnglishLettersAccumulations
        );
    }

    public void train() {
        initializeAndRandomizeWeightsAndThreshold();

        int nextEpochNumber = 1;
        double currentError = Double.MAX_VALUE;

        while (nextEpochNumber < MAXIMUM_NUMBER_OF_EPOCHS) {
            out.println("Training " + language + " recognizer...");
            for (final Entry<String, double[]> trainingObservation:
                     normalizedTrainingObservations.entrySet()) {
                final double[] vectorOfInputs = trainingObservation.getValue();
                normalizeVector(weightsAndThreshold);
                double net = countNet(trainingObservation.getValue());

                out.println("net == " + net + ", epoch == " + nextEpochNumber);

                boolean shouldActivate = trainingObservation.getKey().equals(
                    this.language
                );

                if ((net >= 0) && shouldActivate) {
                    out.print("Activated and should! ");
                    currentError = 1 - net;


                    out.println("After training net == " + net);
                } else if ((net < 0) && !shouldActivate) {
                    /* is ok */out.print("Not activated and should not! ");
                    currentError = (-1) - net;


                    out.println("After training net == " + net);
                } else if ((net >= 0) && !shouldActivate) {
                    out.print("Activated, but should not. ");
                    currentError = (-1) - net;

                    while (net >= 0) {
                        recalculateWeightsAndThreshold(vectorOfInputs, (0 - 1));

                        normalizeVector(weightsAndThreshold);

                        net = countNet(vectorOfInputs);

                        currentError = (-1) - net;
                    }
                    out.println("After training net == " + net);
                } else /* ((net < 0) && shouldActivate) */ {
                    out.print("Not activated, but should. ");
                    currentError = 1 - net;

                    while (net < 0) {
                        recalculateWeightsAndThreshold(vectorOfInputs, (1 - 0));

                        normalizeVector(weightsAndThreshold);

                        net = countNet(vectorOfInputs);

                        currentError = 1 - net;
                    }
                    out.println("After training net == " + net);
                }
            }

            ++(nextEpochNumber);
        }

        for (final Entry<String, double[]> e:
                 normalizedTrainingObservations.entrySet()) {
            out.println(e.getKey() + ": " + countNet(e.getValue()));
        }
    }

    private void initializeAndRandomizeWeightsAndThreshold() {
        weightsAndThreshold = new double[27];

        for (int index = 0; index < weightsAndThreshold.length; ++(index)) {
            weightsAndThreshold[index] = Math.random() * 24 - 12; //[-12, 12)
        }
    }

    private double countNet(final double[] vectorOfInputs) {
        double net = 0;

        for (int index = 0; index < weightsAndThreshold.length; ++(index)) {
            net += vectorOfInputs[index] * weightsAndThreshold[index];
        }

        return net;
    }

    private void recalculateWeightsAndThreshold(final double[] vectorOfInputs,
                                                final int dMinusY) {
        for (int index = 0; index < weightsAndThreshold.length; ++(index)) {
            weightsAndThreshold[index] +=
                dMinusY * LEARNING_RATE * vectorOfInputs[index];
        }
    }

    public double classify(final double[] vectorOfInputs) {
        return countNet(vectorOfInputs);
    }
}