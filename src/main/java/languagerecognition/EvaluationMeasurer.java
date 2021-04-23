package languagerecognition;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static java.lang.System.out;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardOpenOption.READ;
import static java.util.regex.Pattern.compile;

public class EvaluationMeasurer implements FileVisitor<Path> {
    private final LanguageRecognizer languageRecognizer;
    private int activatedAndShouldTo;
    private int activatedButShouldNotTo;
    private int notActivatedButShouldTo;
    private int notActivatedAndShouldNotTo;
    private String currentLanguage;

    public EvaluationMeasurer(final LanguageRecognizer lr) {
        languageRecognizer = lr;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir,
                                             final BasicFileAttributes attrs) {
        final Pattern languageInThePath = compile("^.*([A-Z][a-z]*)$");
        final Matcher matcher = languageInThePath.matcher(dir.toString());

        if (matcher.find()) {
            currentLanguage = matcher.group(1);
        }
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file,
                                     final BasicFileAttributes attrs) {
        try (final FileChannel reader = FileChannel.open(file, READ)) {
            final ByteBuffer bytes = ByteBuffer.allocateDirect(
                (int) reader.size()
            );

            reader.read(bytes);

            bytes.flip();

            final String text = UTF_8.decode(bytes).toString();

            final int[] occurrences =
                CounterOfEnglishLetters.quantitiesOfEnglishLetters(text);

            final double[] toDouble = new double[27];

            for (int i = 0; i < 26; ++(i)) {
                toDouble[i] = occurrences[i];
            }

            toDouble[26] = -1;

            final double net = languageRecognizer.classify(toDouble);

            final boolean shouldActivate = languageRecognizer.language.equals(
                currentLanguage
            );

            if (net >= 0 && shouldActivate) {
                ++activatedAndShouldTo;
            } else if (net >= 0 && !shouldActivate) {
                ++activatedButShouldNotTo;
            } else if (net < 0 && !shouldActivate) {
                ++notActivatedAndShouldNotTo;
            } else if (net < 0 && shouldActivate) {
                ++notActivatedButShouldTo;
            }
        } catch (final IOException ioe) {
            out.println(ioe.getMessage());
        }

        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(final Path file,
                                           final IOException exc) {
        return CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(final Path dir,
                                              final IOException exc) {
        return CONTINUE;
    }

    public void evaluateLanguageRecognizer() {
        final String language = languageRecognizer.language;

        out.println(
            "Evaluating language recognizer for the language: " + language
        );

        final double p =
            (double) activatedAndShouldTo
                     /
                (activatedAndShouldTo + activatedButShouldNotTo);
        final double r =
            (double) activatedAndShouldTo
                     /
                (activatedAndShouldTo + notActivatedButShouldTo);
        final double f = 2 * p * r / (p + r);

        final double accuracy =
            (double) (activatedAndShouldTo + notActivatedAndShouldNotTo)
                /
                (activatedAndShouldTo
                    +
                    activatedButShouldNotTo
                 + notActivatedButShouldTo + notActivatedAndShouldNotTo);

        final int p0 = 18;
        final int p1 = 1 + language.length() + 1;
        final int p2 = 16;

        printLineDivider(p0, p1, p2);
        out.println("| classified as -> | " + language + " | other language |");
        printLineDivider(p0, p1, p2);
        out.printf(
            "| %-17s| %" + language.length() + "d | %" + (p2 - 2) +"d |%n",
            language,
            activatedAndShouldTo,
            notActivatedButShouldTo
        );
        printLineDivider(p0, p1, p2);
        out.printf(
            "| other language   | %" + language.length() +
            "d | %" + (p2 - 2) + "d |%n",
            activatedButShouldNotTo,
            notActivatedAndShouldNotTo
        );
        printLineDivider(p0, p1, p2);

        out.println("Accuracy = " + accuracy);
        out.println("P == " + p);
        out.println("R == " + r);
        out.println("F == " + f);
    }


    private void printLineDivider(int p0, int p1, int p2) {
        out.print('+');
        for (int i = 0; i < p0; ++(i)) {
            out.print('-');
        }
        out.print('+');
        for (int i = 0; i < p1; ++(i)) {
            out.print('-');
        }
        out.print('+');
        for (int i = 0; i < p2; ++(i)) {
            out.print('-');
        }
        out.println('+');
    }
}