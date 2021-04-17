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

            LanguageRecognizer.putNewTrainingObservation(
                entry.getKey(), entry.getValue()
            );
        }

        final LanguageRecognizer forSpanish =
            LanguageRecognizer.specializingIn("Spanish");
        forSpanish.train();

        final String text = "Designa-se por embarcação, grande barco e/ou \"nave\" (utilizado no Brasil muitas vezes como sendo de mesma espécie, segundo a maioria dos dicionários), porém para os especialistas existe distinção a todas as construções de grande porte, cujo objetivo é navegar, tanto no mar, como em lagos, rios, etc, independentemente do tamanho, forma de propulsão, calado função ou material de construção, dai a contradição dos dicionários com a técnica de construção naval.\n" +
            "\n" +
            "As embarcações, dividem-se por vários tipos entre os quais se destacam: barcos de grande porte, navios, botes de uso Militar, que se transformam em Pontes flutuantes, pela Engenharia Militar, e outros como o da figura, que podem ser estendidos, com ligaduras, formando um grande plano de transporte, como os catamarãs de dois, três ou mais cascos. Estes ainda se subdividem em grupos, sub-grupos, famílias, com base em inúmeros critérios navais.\n" +
            "\n" +
            "Dada a utilização generalizada do termo \"barco\" como unidades de boca (convés a quilha), ponte (boreste a bombordo) e pequeno calado (área submersa), vemos muitas vezes de forma errada à divisão entre embarcação e barco, ou entre embarcação e navio, devido ao pequeno navio e nave (soma de navios). Na realidade tanto um como o outro são primeiramente embarcações, devido a possibilidade de se processar nas suas unidades, dessa forma, e só depois Barco ou pequeno Navio, sendo veleiro, moto propulsado ou velomotor. Da mesma forma que um submarino convencional e um submarino atômico, às vezes do tamanho de pequenos porta-aviões ou balsas que possibilitam as justas posições, são embarcações. Ou seja, todos os barcos são embarcações, mas nem todas as embarcações são barcos, pois embarcação é maior que barco, no Brasil.\n" +
            "\n" +
            "O que distingue um pequeno Navio de um barco, segundo os dicionários é o seu uso especifico ou seja, o porta-aviões é um grande navio, como o é também o submarino, os pequenos e grandes barcos tanto grandes como pequenos, de uso genérico como os chamados Iates em que existem verdadeiros Transatlânticos, são barcos grandes, embora haja Iates com o tamanho de pequenos navios.";
        final int[] i =
            CounterOfEnglishLetters.quantitiesOfEnglishLetters(text);

        final double[] d = new double[27];

        for (int in = 0; in < 26; ++(in)) {
            d[in] = i[in];
        }

        d[26] = -1;

        Utilities.normalizeVector(d);

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