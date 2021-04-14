package languagerecognition;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.out;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardOpenOption.READ;

class Main {
    public static void main(final String... mainArguments) {
        final FileVisitor<Path> characterFilterAndCounter;
    }

    private static class CounterOfEnglishLetters implements FileVisitor<Path> {
        private final Map<String, double[]> languageToCharacterParts;
        private final int[] numberOfOccurrences;
        private int totalNumberOfOccurrences;

        {
            languageToCharacterParts = new HashMap<>();
            numberOfOccurrences = new int[26];
        }

        @Override
        public FileVisitResult
        preVisitDirectory(final Path language,
                          final BasicFileAttributes attributes) {
            Arrays.fill(numberOfOccurrences, 0);
            totalNumberOfOccurrences = 0;

            return CONTINUE;
        }

        @Override
        public FileVisitResult
        visitFile(final Path contents, final BasicFileAttributes attributes) {
            try (final FileChannel reader = FileChannel.open(contents, READ)) {
                final ByteBuffer bytes = ByteBuffer.allocateDirect(
                    (int) reader.size()
                );

                reader.read(bytes);

                bytes.flip();

                final CharBuffer characters = UTF_8.decode(bytes);

                totalNumberOfOccurrences += characters.capacity();

                for (int index = 0; index < characters.capacity(); ++(index)) {
                    final char character = characters.get(index);

                    if ((character >= 'A') && (character <= 'Z')) {
                        ++(numberOfOccurrences[character - 'A']);
                    } else if ((character >= 'a') && (character <= 'z')) {
                        ++(numberOfOccurrences[character - 'a']);
                    }
                }
            } catch (final IOException ioe) {
                out.println(ioe.getMessage());
            }

            return CONTINUE;
        }
    }
}