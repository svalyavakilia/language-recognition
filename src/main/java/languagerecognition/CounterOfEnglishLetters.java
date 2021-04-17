package languagerecognition;

class CounterOfEnglishLetters {
    static int[] quantitiesOfEnglishLetters(final String text) {
        final int[] quantitiesOfEnglishLetters = new int[26];

        for (final char character: text.toCharArray()) {
            if ((character >= 'A') && (character <= 'Z')) {
                ++(quantitiesOfEnglishLetters[character - 'A']);
            } else if ((character >= 'a') && (character <= 'z')) {
                ++(quantitiesOfEnglishLetters[character - 'a']);
            }
        }

        return quantitiesOfEnglishLetters;
    }

    static int
    totalQuantityOfEnglishLetters(final int[] quantitiesOfEnglishLetters) {
        int totalQuantityOfEnglishLetters = 0;

        for (final int quantity: quantitiesOfEnglishLetters) {
            totalQuantityOfEnglishLetters += quantity;
        }

        return totalQuantityOfEnglishLetters;
    }
}