import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnonymisePatients {
    private static final Pattern NAME_PATTERN = Pattern.compile("\\bMr\\.?\\s+[A-Z][a-z]+(\\s[A-Z][a-z]+)?\\s+[A-Z][a-z]+\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern TITLE_PATTERN = Pattern.compile("\\b(Mr|Mrs|Ms|Dr)\\.\\b");
    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{2}-\\d{2}-\\d{4}");
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("\\d+\\s+\\w+\\s+(Lane|Road|Street),\\s*\\w+,\\s*\\w+");

    public static void main(String[] args) {
        String inputFilePath = "src/PatientNotes"; 
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            HashMap<String, String> patientMapping = new HashMap<>(); 
            int idCounter = 1;
            StringBuilder anonymizedText = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    System.out.println(anonymizedText.toString());
                    System.out.println();
                    anonymizedText.setLength(0); 
                } else {
                    anonymizedText.append(anonymizeLine(line, patientMapping, idCounter)).append("\n");
                }
            }

            printMapping(patientMapping); 
        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
        }
    }

    private static String anonymizeLine(String line, HashMap<String, String> mapping, int idCounter) {
        line = replaceWithMatcher(line, NAME_PATTERN, mapping, idCounter);
        line = replaceWithMatcher(line, TITLE_PATTERN, mapping, idCounter);
        line = replaceWithMatcher(line, DATE_PATTERN, mapping, idCounter);
        line = replaceWithMatcher(line, ADDRESS_PATTERN, mapping, idCounter);
        return line;
    }

    private static String replaceWithMatcher(String line, Pattern pattern, HashMap<String, String> mapping, int idCounter) {
        Matcher matcher = pattern.matcher(line);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {    
            matcher.appendReplacement(sb, " " + generateAnonymizedId(mapping, matcher.group(0), idCounter));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String generateAnonymizedId(HashMap<String, String> mapping, String originalData, int idCounter) {
        if (!mapping.containsKey(originalData)) {
            String id = idCounter + "." + mapping.size(); 
            mapping.put(originalData, id);
            idCounter++; 
            return id;
        } else {
            return mapping.get(originalData);
        }
    }

    private static void printMapping(HashMap<String, String> mapping) { 
        System.out.println("Mapping:");
        for (String originalData : mapping.keySet()) {
            System.out.println(mapping.get(originalData) + " " + originalData);
        }
    }
}
