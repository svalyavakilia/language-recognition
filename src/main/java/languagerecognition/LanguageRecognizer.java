package languagerecognition;

import java.util.HashMap;
import java.util.Map;

class LanguageRecognizer {
    /*
     * This map contains training observations for the perceptrons.
     */
    private static final Map<String, double[]> languageToCharacterProportions;

    static {
        languageToCharacterProportions = new HashMap<>();
    }

    /*
     * Language, in which this perceptron specializes.
     */
    final String language;

    LanguageRecognizer(final String language) {
        this.language = language;
    }

    static void
    addLanguageAndCharacterProportions(final String language,
                                       final double[] characterProportions) {
        if (characterProportions.length != 26) {
            throw new IllegalArgumentException(
                "There must be 26 proportions of characters " +
                "(as there are 26 letters in the English alphabet)!"
            );
        }

        languageToCharacterProportions.put(language, characterProportions);
    }
}