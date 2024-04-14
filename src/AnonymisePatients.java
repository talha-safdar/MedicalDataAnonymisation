import java.io.*;
import java.nio.file.*;
import java.util.regex.*;

public class AnonymisePatients {
    private static int idCounter = 1; // ID counter for global tracking

    public static void main(String[] args) throws IOException {
        String filePath = "src/PatientNotes";
        String outputText = anonymisePatientData(filePath);
        System.out.println(outputText);
    }

    private static String anonymisePatientData(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        String[] patients = content.split("\n\n\\d+\\.\\d+"); // Split by patient identifier

        StringBuilder anonymisedText = new StringBuilder();
        StringBuilder mappingText = new StringBuilder();

        for (String patient : patients) {
            String[] lines = patient.split("\n");
            String patientName = lines[0]; // Assumes the first line contains patient identifier

            // Anonymize names, DOB, and addresses
            for (int i = 1; i < lines.length; i++) {
                String paragraph = lines[i];
                paragraph = replaceAndMap(paragraph, "\\b(?:Mr|Mrs|Ms|Dr)\\.\\s+[A-Z][a-z]+\\s+[A-Z][a-z]+", ".1", mappingText);
                paragraph = replaceAndMap(paragraph, "\\(DOB:\\s+\\d{2}-\\d{2}-\\d{4}\\)", ".2", mappingText);
                paragraph = replaceAndMap(paragraph, "\\d+\\s+[\\w\\s]+,\\s+[\\w\\s]+,\\s+[A-Z]{2}", ".3", mappingText);
                anonymisedText.append(paragraph).append("\n");
            }

            anonymisedText.append("\n"); // Separate patient paragraphs
        }

        writeToFile("src/AnonymisedNotes.txt", anonymisedText.toString());
        writeToFile("src/MappingDocument.txt", mappingText.toString());

        return anonymisedText.toString();
    }

    private static String replaceAndMap(String text, String regex, String suffix, StringBuilder mapping) {
        Matcher matcher = Pattern.compile(regex).matcher(text);
        while (matcher.find()) {
            String found = matcher.group();
            text = text.replaceFirst(Pattern.quote(found), idCounter + suffix + " ");
            mapping.append(idCounter).append(suffix).append(" ").append(found).append("\n");
            idCounter++;
        }
        return text;
    }

    private static void writeToFile(String filePath, String content) throws IOException {
        Files.write(Paths.get(filePath), content.getBytes());
    }
}
