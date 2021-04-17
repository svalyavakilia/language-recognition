package languagerecognition;

class Utilities {
    static void normalizeVector(final double[] vector) {
        double divider = 0;

        for (final double coordinate: vector) {
            divider += Math.pow(coordinate, 2);
        }

        divider = Math.sqrt(divider);

        for (int index = 0; index < vector.length; ++(index)) {
            vector[index] /= divider;
        }
    }
}