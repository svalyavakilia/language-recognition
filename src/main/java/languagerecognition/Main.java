package languagerecognition;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.FileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.out;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardOpenOption.READ;
import static java.util.Map.Entry;
import static java.util.regex.Pattern.compile;

class Main {
    public static void main(final String... mainArguments) throws IOException {
        final TrainingMapInitializer trainingMapInitializer =
                    new TrainingMapInitializer();

        final String fileSeparator = System.getProperty("file.separator");

        final Path directoryWithTrainingTexts = Path.of(
            "." + fileSeparator +
            "src" + fileSeparator +
            "main" + fileSeparator +
            "resources" + fileSeparator +
            "languages" + fileSeparator +
            "training" + fileSeparator
        );

        Files.walkFileTree(directoryWithTrainingTexts, trainingMapInitializer);

        final Map<String, int[]> languagesToEnglishLettersAccumulations =
            trainingMapInitializer.languagesToEnglishLettersAccumulations;

        for (final Entry<String, int[]> entry:
                 languagesToEnglishLettersAccumulations.entrySet()) {
            out.println(
                entry.getKey() + ": " + Arrays.toString(entry.getValue())
            );
            out.println(CounterOfEnglishLetters.totalQuantityOfEnglishLetters(
                entry.getValue()
            ));

            LanguageRecognizer.putNewTrainingObservation(
                entry.getKey(), entry.getValue()
            );
        }

        final LanguageRecognizer forSpanish =
            LanguageRecognizer.specializingIn("Spanish");
        forSpanish.train();

        final String text = "Un plebiscito constitucional fue realizado en Brasil el 21 de abril de 1993\n" +
            "tras la restauración de la democracia y la redacción de una nueva constitución\n" +
            "por la Asamblea Constituyente. En el proceso los brasileños escogieron entre dos\n" +
            "posibles formas de gobierno; la monarquía o la república y el sistema\n" +
            "presidencialista o parlamentarista. El referéndum fue regulado por la ley\n" +
            "federal 8.624.\n" +
            "\n" +
            "Brasil contó con una monarquía durante un período de su historia, el Imperio de\n" +
            "Brasil cuyo último emperador fue Pedro II. La monarquía fue abolida en 1889.\n" +
            "El diputado federal Antônio Henrique Bittencourt da Cunha Bueno, monarquista y\n" +
            "miembro del conservador Partido Social Demócrata propuso la restauración de la\n" +
            "monarquía ante el Congreso argumentando que la deposición del monarca no se\n" +
            "hizo por medios democráticos sino por un golpe de estado. Bueno formó el\n" +
            "Movimiento Monárquico Parlamentario para hacer campaña a favor de la monarquía";
        final int[] i =
            CounterOfEnglishLetters.quantitiesOfEnglishLetters(text);

        final double[] d = new double[27];

        for (int in = 0; in < 26; ++(in)) {
            d[in] = i[in];
        }

        d[26] = -1;

        //Utilities.normalizeVector(d);

        final LanguageRecognizer forFrench =
            LanguageRecognizer.specializingIn("French");
        forFrench.train();
        final LanguageRecognizer forItalian =
            LanguageRecognizer.specializingIn("Italian");
        forItalian.train();
        final LanguageRecognizer forPortuguese =
            LanguageRecognizer.specializingIn("Portuguese");
        forPortuguese.train();

        out.println(forFrench.classify(d));
        out.println(forItalian.classify(d));
        out.println(forPortuguese.classify(d));
        out.println(forSpanish.classify(d));
    }
}