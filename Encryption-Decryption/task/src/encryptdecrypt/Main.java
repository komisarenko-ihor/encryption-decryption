package encryptdecrypt;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    private static final int UNICODE_BOUND = 255;
    private static final int ALPHABET_LENGTH = 26;
    private static final int UPPERCASE_BEGIN = 65;
    private static final int UPPERCASE_END = 91;
    private static final int LOWERCASE_BEGIN = 97;
    private static final int LOWERCASE_END = 123;

    public static void main(String[] args) {
        try {
            InputData inputData = createInputData(args);

            AlgorithmImpl algorithm = new AlgorithmImpl(inputData);

            String transformed = algorithm.run();

            if (inputData.getOut() != null) {
                Files.writeString(Paths.get(inputData.getOut()), transformed);
            } else {
                System.out.println(transformed);
            }

        } catch (Exception ex) {
            System.out.println("Error");
        }
    }

    private interface Algorithm {
        String run();
    }

    private static class AlgorithmImpl implements Algorithm {

        private final InputData inputData;

        public AlgorithmImpl(InputData inputData) {
            this.inputData = inputData;
        }

        @Override
        public String run() {
            CodeData codeData;
            String transformed;

            if ("unicode".equals(inputData.getAlg())) {
                codeData = new Unicode();
            } else {
                codeData = new Shift();
            }

            switch (inputData.getType()) {
                case "enc":
                    transformed = codeData.encrypt(inputData);
                    break;
                case "dec":
                    transformed = codeData.decrypt(inputData);
                    break;
                default:
                    transformed = "Unsupported operation";
                    break;
            }
            return transformed;
        }
    }

    private static InputData createInputData(String[] args) throws IOException {
        InputData result = new InputData();

        for (int i = 0; i < args.length; i += 2) {
            switch (args[i]) {
                case "-mode":
                    result.setType(args[i + 1]);
                    break;
                case "-key":
                    result.setKey(Integer.parseInt(args[i + 1]));
                    break;
                case "-data":
                    if (result.getText() != null) {
                        throw new RuntimeException();
                    }
                    result.setText(args[i + 1]);
                    break;
                case "-in":
                    if (result.getText() != null) {
                        throw new RuntimeException();
                    }
                    Files.readString(Paths.get(args[i + 1]));
                    result.setText(Files.readString(Paths.get(args[i + 1])));
                    break;
                case "-out":
                    result.setOut(args[i + 1]);
                    break;
                case "-alg":
                    result.setAlg(args[i + 1]);
            }
        }

        if (result.getText() == null) {
            result.setText("");
        }
        if (result.getType() == null) {
            result.setType("enc");
        }
        if (result.getAlg() == null) {
            result.setAlg("shift");
        }

        return result;
    }

    private interface CodeData {
        String encrypt(InputData inputData);

        String decrypt(InputData inputData);
    }

    private static class Shift implements CodeData {
        @Override
        public String encrypt(InputData inputData) {
            char[] source = inputData.getText().toCharArray();
            char[] target = new char[source.length];
            Util util = new Util();
            for (int i = 0; i < source.length; i++) {
                char sourceIndex = source[i];
                char targetIndex;
                if (util.isUpperCaseLetter(sourceIndex)) {
                    targetIndex = (char) ((sourceIndex - UPPERCASE_BEGIN + inputData.getKey()) % ALPHABET_LENGTH + UPPERCASE_BEGIN);
                } else if (util.isLowerCaseLetter(sourceIndex)) {
                    targetIndex = (char) ((sourceIndex - LOWERCASE_BEGIN + inputData.getKey()) % ALPHABET_LENGTH + LOWERCASE_BEGIN);
                } else {
                    targetIndex = sourceIndex;
                }
                target[i] = targetIndex;
            }
            return String.valueOf(target);
        }

        @Override
        public String decrypt(InputData inputData) {
            char[] source = inputData.getText().toCharArray();
            char[] target = new char[source.length];
            Util util = new Util();
            for (int i = 0; i < source.length; i++) {
                char sourceIndex = source[i];
                char targetIndex;
                if (util.isUpperCaseLetter(sourceIndex)) {
                    if (inputData.getKey() > (sourceIndex - UPPERCASE_BEGIN)) {
                        targetIndex = (char) ((ALPHABET_LENGTH + sourceIndex - UPPERCASE_BEGIN) - (inputData.getKey() % UNICODE_BOUND) + UPPERCASE_BEGIN);
                    } else {
                        targetIndex = (char) (sourceIndex - (inputData.getKey() % UNICODE_BOUND));
                    }
                } else if (util.isLowerCaseLetter(sourceIndex)) {
                    if (inputData.getKey() > (sourceIndex - LOWERCASE_BEGIN)) {
                        targetIndex = (char) ((ALPHABET_LENGTH + sourceIndex - UPPERCASE_BEGIN) - (inputData.getKey() % UNICODE_BOUND) + UPPERCASE_BEGIN);
                    } else {
                        targetIndex = (char) (sourceIndex - (inputData.getKey() % UNICODE_BOUND));
                    }
                } else {
                    targetIndex = sourceIndex;
                }
                target[i] = targetIndex;
            }
            return String.valueOf(target);
        }
    }

    private static class Util {
        boolean isUpperCaseLetter(char ch) {
            return ch >= UPPERCASE_BEGIN && ch < UPPERCASE_END;
        }

        boolean isLowerCaseLetter(char ch) {
            return ch >= LOWERCASE_BEGIN && ch < LOWERCASE_END;
        }
    }

    private static class Unicode implements CodeData {
        @Override
        public String encrypt(InputData inputData) {
            char[] source = inputData.getText().toCharArray();
            char[] target = new char[source.length];
            for (int i = 0; i < source.length; i++) {
                char sourceIndex = source[i];
                char targetIndex = (char) ((sourceIndex + inputData.getKey()) % UNICODE_BOUND);
                target[i] = targetIndex;
            }
            return String.valueOf(target);
        }

        @Override
        public String decrypt(InputData inputData) {
            char[] source = inputData.getText().toCharArray();
            char[] target = new char[source.length];
            for (int i = 0; i < source.length; i++) {
                char sourceIndex = source[i];
                char targetIndex;
                if (inputData.getKey() > sourceIndex) {
                    targetIndex = (char) ((UNICODE_BOUND + sourceIndex) - (inputData.getKey() % UNICODE_BOUND));
                } else {
                    targetIndex = (char) (sourceIndex - (inputData.getKey() % UNICODE_BOUND));
                }
                target[i] = targetIndex;
            }
            return String.valueOf(target);
        }
    }

    private static class InputData {
        private String type;
        private String text;
        private int key;
        private String out;
        private String alg;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getKey() {
            return key;
        }

        public void setKey(int key) {
            this.key = key;
        }

        public String getOut() {
            return out;
        }

        public void setOut(String out) {
            this.out = out;
        }

        public String getAlg() {
            return alg;
        }

        public void setAlg(String alg) {
            this.alg = alg;
        }
    }
}
