import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnonymisePatients {
	
	// patterns to detect names, dates, and addresses
    private static final Pattern NAME_PATTERN = Pattern.compile("\\b(Mr|Mrs|Ms|Dr)\\.?\\s+[A-Z][a-z]*\\s*([A-Z][a-z]+)?\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{2}-\\d{2}-\\d{4}");
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("\\d+\\s+[\\w\\s]+\\b(Street|Road|Lane|Avenue),?\\s+[\\w\\s]+,\\s+[A-Z]{2}", Pattern.CASE_INSENSITIVE);

    private static final HashMap<String, Integer> idCounters = new HashMap<>();

    public static void main(String[] args) {
        String inputFilePath = "src/PatientNotes"; // the source of the file
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            HashMap<String, String> patientMapping = new HashMap<>();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) { 
                    printMapping(patientMapping);
                    patientMapping.clear(); 
                } else {
                    String anonymizedLine = anonymizeLine(line, patientMapping).trim();
                    System.out.println(anonymizedLine);
                }
            }

            if (!patientMapping.isEmpty()) {
                printMapping(patientMapping);
            }

        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
        }
    }

    private static String anonymizeLine(String line, HashMap<String, String> mapping) {
        line = replaceWithMatcher(line, NAME_PATTERN, "name", mapping);
        line = replaceWithMatcher(line, DATE_PATTERN, "date", mapping);
        line = replaceWithMatcher(line, ADDRESS_PATTERN, "address", mapping);
        return line;
    }

    private static String replaceWithMatcher(String line, Pattern pattern, String category, HashMap<String, String> mapping) {
        Matcher matcher = pattern.matcher(line);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String found = matcher.group();
            String replacement = mapping.computeIfAbsent(found, k -> generateAnonymizedId(category));
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String generateAnonymizedId(String category) {
        int count = idCounters.getOrDefault(category, 1);
        String id = category.equals("address") ? "1." + count : count + ".0"; // Example categorisation
        idCounters.put(category, count + 1);
        return id;
    }

    private static void printMapping(HashMap<String, String> mapping) {
        System.out.println("Mapping:");
        for (String originalData : mapping.keySet()) {
            System.out.println(mapping.get(originalData) + " " + originalData);
        }
        System.out.println(); // Extra line for readability
    }
}
