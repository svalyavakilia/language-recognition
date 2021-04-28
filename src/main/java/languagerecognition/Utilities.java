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

    static int[] addMinusOne(final int[] vector) {
        final int[] expanded = new int[27];

        System.arraycopy(vector, 0, expanded, 0, 26);

        expanded[26] = (-1);

        return expanded;
    }
}