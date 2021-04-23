package languagerecognition;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.FileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;

import static java.lang.System.out;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardOpenOption.READ;
import static java.util.Map.Entry;
import static java.util.regex.Pattern.compile;

class Main {
    static final List<LanguageRecognizer> lrs = new ArrayList<>();

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

        final List<EvaluationMeasurer> ems = new ArrayList<>();

        for (final LanguageRecognizer lr: lrs) {
            lr.train();
            ems.add(new EvaluationMeasurer(lr));
        }

        final Path directoryWithTestingTexts = Path.of(
            "." + fileSeparator +
                "src" + fileSeparator +
                "main" + fileSeparator +
                "resources" + fileSeparator +
                "languages" + fileSeparator +
                "testing" + fileSeparator
        );

        for (EvaluationMeasurer em: ems) {
            Files.walkFileTree(directoryWithTestingTexts, em);
            em.evaluateLanguageRecognizer();
        }

        JFrame window = new JFrame("Language recognition");
        JPanel panel = new JPanel();
        panel.setPreferredSize(new java.awt.Dimension(600, 400));
        JTextArea jTextArea = new JTextArea();
        jTextArea.setPreferredSize(new java.awt.Dimension(200, 200));
        jTextArea.setLineWrap(true);
        JLabel label = new JLabel();
        JButton recognize = new JButton("Recognize!");
        recognize.addActionListener(listener -> {
            final String text = jTextArea.getText();

            LanguageRecognizer max = lrs.get(0);

            final double[] vectorOfInputs = new double[27];
            vectorOfInputs[26] = (-1);

            int index = 0;
            for (final int coordinate:
                     CounterOfEnglishLetters.quantitiesOfEnglishLetters(text)) {
                vectorOfInputs[(index)++] = coordinate;
            }

            double currentMaxNet = max.classify(vectorOfInputs);

            index = 1;
            for (; index < lrs.size(); ++(index)) {
                final double currentNet = lrs.get(index).classify(
                                              vectorOfInputs
                                          );

                if (lrs.get(index).classify(vectorOfInputs) > currentMaxNet) {
                    currentMaxNet = currentNet;
                    max = lrs.get(index);
                }
            }

            label.setText(max.language);
        });

        panel.add(jTextArea);
        panel.add(label);
        panel.add(recognize);

        window.setContentPane(panel);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);
        window.pack();
        window.setVisible(true);
    }
}