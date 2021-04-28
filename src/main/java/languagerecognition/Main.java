package languagerecognition;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import static java.lang.System.out;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Map.Entry;

class Main {
    static final List<LanguageRecognizer> recognizers = new ArrayList<>();

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

        for (final LanguageRecognizer recognizer: recognizers) {
            recognizer.train();
            ems.add(new EvaluationMeasurer(recognizer));
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

        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final int rootWidth = screen.width;
        final int rootHeight = screen.height;

        JFrame window = new JFrame("Language recognition");
        JPanel root = new JPanel();
        root.setPreferredSize(new Dimension(rootWidth, rootHeight));
        JTextArea jTextArea = new JTextArea();
        jTextArea.setPreferredSize(new Dimension(rootWidth / 2, rootHeight));
        jTextArea.setLineWrap(true);
        JLabel classification = new JLabel();
        classification.setPreferredSize(
            new Dimension(rootWidth / 8, rootHeight / 12)
        );
        JButton recognize = new JButton("Recognize!");
        recognize.setPreferredSize(
            new Dimension(rootWidth / 8, rootHeight / 12)
        );
        recognize.addActionListener(listener -> {
            final String text = jTextArea.getText();

            LanguageRecognizer max = recognizers.get(0);

            final int[] vectorOfInputs = Utilities.addMinusOne(
                CounterOfEnglishLetters.quantitiesOfEnglishLetters(text)
            );

            double currentMaxNet = max.classify(vectorOfInputs);

            int index = 1;
            for (; index < recognizers.size(); ++(index)) {
                final double currentNet = recognizers.get(index).classify(
                                              vectorOfInputs
                                          );

                if (recognizers.get(index).classify(vectorOfInputs) > currentMaxNet) {
                    currentMaxNet = currentNet;
                    max = recognizers.get(index);
                }
            }

            classification.setText(
                LocalTime.now().truncatedTo(SECONDS) + " " + max.language
            );
        });

        root.add(jTextArea);
        root.add(classification);
        root.add(recognize);

        window.setContentPane(root);
        window.pack();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}