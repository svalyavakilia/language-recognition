package languagerecognition;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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
import static java.util.regex.Pattern.compile;

class TrainingMapInitializer implements FileVisitor<Path> {
            final Map<String, int[]> languagesToEnglishLettersAccumulations;
            final Map<String, LanguageRecognizer> languagesToRecognizers;
    private final int[] englishLettersAccumulations;

    {
        languagesToEnglishLettersAccumulations = new HashMap<>();
        languagesToRecognizers = new HashMap<>();
        englishLettersAccumulations = new int[26];
    }

    @Override
    public FileVisitResult
    preVisitDirectory(final Path language,
                      final BasicFileAttributes attributes) {
        Arrays.fill(englishLettersAccumulations, 0);

        return CONTINUE;
    }

    @Override
    public FileVisitResult
    visitFile(final Path file, final BasicFileAttributes attributes) {
        try (final FileChannel reader = FileChannel.open(file, READ)) {
            /* prepare a buffer for the text */
            final ByteBuffer bytes = ByteBuffer.allocateDirect(
                (int) reader.size()
            );

            /* read the text */
            reader.read(bytes);

            /* prepare the buffer for decoding */
            bytes.flip();

            /* decode the buffer and get the text */
            final String text = UTF_8.decode(bytes).toString();

            /* count quantities of English letters in this text */
            final int[] quantitiesOfEnglishLettersInTheText =
                CounterOfEnglishLetters.quantitiesOfEnglishLetters(text);

            /* add these quantities to array of accumulated quantities */
            int index = 0;
            while (index < englishLettersAccumulations.length) {
                englishLettersAccumulations[index]
                    +=
                    quantitiesOfEnglishLettersInTheText[index];

                ++(index);
            }

            /*
            this text is processed; go to another text or to another language
            */
        } catch (final IOException ioe) {
            out.println(ioe.getMessage());
        }

        return CONTINUE;
    }

    @Override
    public FileVisitResult
    visitFileFailed(final Path file, final IOException ioe) {
        out.println(
            "Visit to " + file + " failed. Message: " + ioe.getMessage()
        );

        return CONTINUE;
    }

    @Override
    public FileVisitResult
    postVisitDirectory(final Path directory, final IOException ioe) {
        final Pattern languageInThePath = compile("^.*([A-Z][a-z]*)$");
        final Matcher matcher = languageInThePath.matcher(directory.toString());

        if (matcher.find()) {
            final String language = matcher.group(1);

            languagesToEnglishLettersAccumulations.put(
                matcher.group(1),
                Arrays.copyOf(
                    englishLettersAccumulations,
                    englishLettersAccumulations.length
                )
            );

            languagesToRecognizers.put(
                matcher.group(1),
                LanguageRecognizer.specializingIn(language)
            );
        }

        return CONTINUE;
    }
}