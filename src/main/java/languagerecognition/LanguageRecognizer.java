package languagerecognition;

import java.util.HashMap;
import java.util.Map;

import static java.util.Map.Entry;

class LanguageRecognizer {
    private static final int MAXIMUM_NUMBER_OF_EPOCHS;
    private static final double LEARNING_RATE;
    private static final Map<String, int[]> trainingObservations;

    static {
        MAXIMUM_NUMBER_OF_EPOCHS = 1000;
        LEARNING_RATE = 0.025;
        trainingObservations = new HashMap<>();
    }
    
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
                              int[] englishLettersAccumulations) {
        if (englishLettersAccumulations.length != 26) {
            throw new IllegalArgumentException(
                "There must be 26 accumulations of English letters!"
            );
        }

        englishLettersAccumulations = Utilities.addMinusOne(
            englishLettersAccumulations
        );

        trainingObservations.put(language, englishLettersAccumulations);
    }

    public void train() {
        initializeAndRandomizeWeightsAndThreshold();

        int nextEpochNumber = 1;

        while (nextEpochNumber < MAXIMUM_NUMBER_OF_EPOCHS) {
            for (final Entry<String, int[]> trainingObservation:
                     trainingObservations.entrySet()) {
                final int[] vectorOfInputs = trainingObservation.getValue();
                double net = countNet(trainingObservation.getValue());

                boolean shouldActivate = trainingObservation.getKey().equals(
                    this.language
                );

                if ((net >= 0) && !shouldActivate) {
                    while (net >= 0) {
                        recalculateWeightsAndThreshold(vectorOfInputs, (0 - 1));

                        net = countNet(vectorOfInputs);
                    }
                } else /* ((net < 0) && shouldActivate) */ {
                    while (net < 0) {
                        recalculateWeightsAndThreshold(vectorOfInputs, (1 - 0));

                        net = countNet(vectorOfInputs);
                    }
                }
            }

            ++(nextEpochNumber);
        }
    }

    private void initializeAndRandomizeWeightsAndThreshold() {
        weightsAndThreshold = new double[27];

        for (int index = 0; index < weightsAndThreshold.length; ++(index)) {
            weightsAndThreshold[index] = Math.random(); //[0, 1)
        }
    }

    private double countNet(final int[] vectorOfInputs) {
        double net = 0;

        for (int index = 0; index < weightsAndThreshold.length; ++(index)) {
            net += vectorOfInputs[index] * weightsAndThreshold[index];
        }

        return net;
    }

    private void recalculateWeightsAndThreshold(final int[] vectorOfInputs,
                                                final int dMinusY) {
        for (int index = 0; index < weightsAndThreshold.length; ++(index)) {
            weightsAndThreshold[index] +=
                dMinusY * LEARNING_RATE * vectorOfInputs[index];
        }
    }

    public double classify(final int[] vectorOfInputs) {
        return countNet(vectorOfInputs);
    }
}