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

        final LanguageRecognizer forFrench = LanguageRecognizer.specializingIn(
            "French");
        forFrench.train();

        final LanguageRecognizerEvaluationMeasurer measurerForFrench =
            new LanguageRecognizerEvaluationMeasurer(forFrench);

        final LanguageRecognizer forItalian = LanguageRecognizer.specializingIn(
            "Italian");
        forItalian.train();

        final LanguageRecognizerEvaluationMeasurer measurerForItalian =
            new LanguageRecognizerEvaluationMeasurer(forItalian);

        final LanguageRecognizer forPortuguese =
            LanguageRecognizer.specializingIn(
            "Portuguese");
        forPortuguese.train();

        final LanguageRecognizerEvaluationMeasurer measurerForPortuguese =
            new LanguageRecognizerEvaluationMeasurer(forPortuguese);

        final LanguageRecognizer forSpanish = LanguageRecognizer.specializingIn(
            "Spanish");
        forSpanish.train();

        final LanguageRecognizerEvaluationMeasurer measurerForSpanish =
            new LanguageRecognizerEvaluationMeasurer(forSpanish);

        final Path directoryWithTestingTexts = Path.of(
            "." + fileSeparator +
                "src" + fileSeparator +
                "main" + fileSeparator +
                "resources" + fileSeparator +
                "languages" + fileSeparator +
                "testing" + fileSeparator
        );

        Files.walkFileTree(directoryWithTestingTexts, measurerForFrench);
        Files.walkFileTree(directoryWithTestingTexts, measurerForItalian);
        Files.walkFileTree(directoryWithTestingTexts, measurerForPortuguese);
        Files.walkFileTree(directoryWithTestingTexts, measurerForSpanish);

        measurerForFrench.evaluateLanguageRecognizer();
        measurerForItalian.evaluateLanguageRecognizer();
        measurerForPortuguese.evaluateLanguageRecognizer();
        measurerForSpanish.evaluateLanguageRecognizer();
    }
}