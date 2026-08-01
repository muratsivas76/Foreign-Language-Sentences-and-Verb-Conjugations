//package net.murat.elang.tool;

import java.io.*;
import java.util.*;

public class FileGenerator {
    
    private static final String JSON_FILE = "frsverbos.json";
    private static final String OUTPUT_DIR = "verbs";
    private static final String ENCODING = "UTF-8";
    
    public static void main(String[] args) {
        FileGenerator generator = new FileGenerator();
        generator.generateFiles();
    }
    
    public void generateFiles() {
        try {
            String jsonContent = readFile(JSON_FILE);
            
            Map<String, Map<String, List<String>>> data = parseJson(jsonContent);
            
            File dir = new File(OUTPUT_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            int totalFiles = 0;
            
            for (String verbName : data.keySet()) {
                Map<String, List<String>> tenses = data.get(verbName);
                
                for (String tenseName : tenses.keySet()) {
                    List<String> conjugations = tenses.get(tenseName);
                    
                    String fileName = verbName + "_" + tenseName + ".utf";
                    String filePath = OUTPUT_DIR + File.separator + fileName;
                    
                    writeFile(filePath, conjugations);
                    totalFiles++;
                    
                    System.out.println("Created: " + fileName);
                }
            }
            
            System.out.println("\nTotal " + totalFiles + " files created!");
            System.out.println("Directory: " + new File(OUTPUT_DIR).getAbsolutePath());
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String readFile(String fileName) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        
        try {
            br = new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(fileName), ENCODING
                )
            );
            
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            
        } finally {
            if (br != null) {
                try { br.close(); } catch (IOException e) {}
            }
        }
        
        return sb.toString();
    }
    
    private void writeFile(String filePath, List<String> conjugations) throws IOException {
        BufferedWriter bw = null;
        
        try {
            bw = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(filePath), ENCODING
                )
            );
            
            for (String conjugation : conjugations) {
                bw.write(conjugation);
                bw.newLine();
            }
            
            bw.flush();
            
        } finally {
            if (bw != null) {
                try { bw.close(); } catch (IOException e) {}
            }
        }
    }
    
    private Map<String, Map<String, List<String>>> parseJson(String json) {
        Map<String, Map<String, List<String>>> result = new HashMap<String, Map<String, List<String>>>();
        
        json = json.trim();
        
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1);
        }
        
        List<String> topLevel = splitByComma(json);
        
        for (String item : topLevel) {
            int colonIndex = item.indexOf(":");
            if (colonIndex == -1) continue;
            
            String verbName = item.substring(0, colonIndex).trim();
            verbName = clean(verbName);
            
            String tensesStr = item.substring(colonIndex + 1).trim();
            
            Map<String, List<String>> tenseMap = parseTenses(tensesStr);
            result.put(verbName, tenseMap);
        }
        
        return result;
    }
    
    private Map<String, List<String>> parseTenses(String tensesStr) {
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        
        tensesStr = tensesStr.trim();
        
        if (tensesStr.startsWith("{") && tensesStr.endsWith("}")) {
            tensesStr = tensesStr.substring(1, tensesStr.length() - 1);
        }
        
        List<String> tenseList = splitByComma(tensesStr);
        
        for (String tenseItem : tenseList) {
            int colonIndex = tenseItem.indexOf(":");
            if (colonIndex == -1) continue;
            
            String tenseName = tenseItem.substring(0, colonIndex).trim();
            tenseName = clean(tenseName);
            
            String conjugationsStr = tenseItem.substring(colonIndex + 1).trim();
            List<String> conjugations = parseConjugations(conjugationsStr);
            
            result.put(tenseName, conjugations);
        }
        
        return result;
    }
    
    private List<String> parseConjugations(String conjugationsStr) {
        List<String> result = new ArrayList<String>();
        
        conjugationsStr = conjugationsStr.trim();
        
        if (conjugationsStr.startsWith("[") && conjugationsStr.endsWith("]")) {
            conjugationsStr = conjugationsStr.substring(1, conjugationsStr.length() - 1);
        }
        
        List<String> items = splitByComma(conjugationsStr);
        
        for (String item : items) {
            String conjugation = clean(item);
            if (conjugation.length() > 0) {
                result.add(conjugation);
            }
        }
        
        return result;
    }
    
    private List<String> splitByComma(String str) {
        List<String> result = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        int braceLevel = 0;
        int bracketLevel = 0;
        boolean insideQuotes = false;
        
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            
            if (c == '"' && (i == 0 || str.charAt(i-1) != '\\')) {
                insideQuotes = !insideQuotes;
            }
            
            if (!insideQuotes) {
                if (c == '{') braceLevel++;
                else if (c == '}') braceLevel--;
                else if (c == '[') bracketLevel++;
                else if (c == ']') bracketLevel--;
                else if (c == ',' && braceLevel == 0 && bracketLevel == 0) {
                    result.add(current.toString().trim());
                    current = new StringBuilder();
                    continue;
                }
            }
            
            current.append(c);
        }
        
        if (current.length() > 0) {
            result.add(current.toString().trim());
        }
        
        return result;
    }
    
    private String clean(String str) {
        str = str.trim();
        if (str.startsWith("\"") && str.endsWith("\"")) {
            str = str.substring(1, str.length() - 1);
        }
        str = str.replace("\\\"", "\"");
        str = str.replace("\\\\", "\\");
        return str;
    }
    
}
