import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExtractMethods {

    private static Set<String> results = new LinkedHashSet<String>();
    private static int fileCount = 0;
    private static int memberCount = 0;
    private static final String OUTPUT_FILE = "methods_export.txt";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java ExtractMethods <path1> [path2] [path3] ...");
            System.err.println("       Each path can be a .java file or a directory.");
            System.exit(1);
        }

        for (String arg : args) {
            File root = new File(arg);
            if (!root.exists()) {
                System.err.println("Warning: Path does not exist, skipping: " + arg);
                continue;
            }
            scanDirectory(root);
        }

        if (fileCount == 0) {
            System.err.println("No Java files were found in the provided paths.");
            System.exit(1);
        }

        writeResults(OUTPUT_FILE);

        System.out.println("Scanning completed.");
        System.out.println("Total files processed: " + fileCount);
        System.out.println("Total unique members found: " + memberCount);
        System.out.println("Results saved to: " + OUTPUT_FILE);
    }

    private static void scanDirectory(File dir) {
        if (dir.isFile()) {
            if (dir.getName().endsWith(".java")) {
                processJavaFile(dir);
            } else {
                System.err.println("Warning: Not a Java file, skipping: " + dir.getPath());
            }
            return;
        }

        File[] files = dir.listFiles();
        if (files == null) return;

        Arrays.sort(files);

        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                scanDirectory(files[i]);
            } else if (files[i].getName().endsWith(".java")) {
                processJavaFile(files[i]);
            }
        }
    }

    private static void processJavaFile(File javaFile) {
        try {
            String content = readFile(javaFile);
            results.add("\n--- File: " + javaFile.getPath() + " ---");
            extractMembers(content, javaFile.getName());
            fileCount++;
        } catch (IOException e) {
            System.err.println("Error reading file: " + javaFile.getPath() + " — " + e.getMessage());
        }
    }

    private static String readFile(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } finally {
            reader.close();
        }
        return sb.toString();
    }

    // Detects whether the top-level type declaration is an interface
    private static boolean isInterface(String content) {
        Pattern p = Pattern.compile("(?:^|\\s)interface\\s+\\w+", Pattern.MULTILINE);
        return p.matcher(content).find();
    }

    private static void extractMembers(String content, String fileName) {
        String className = extractClassName(content, fileName);
        boolean isIface = isInterface(content);

        if (isIface) {
            extractInterfaceMembers(content);
        } else {
            extractClassMembers(content, className);
        }
    }

    // -------------------------------------------------------------------------
    // Interface extraction
    // -------------------------------------------------------------------------
    private static void extractInterfaceMembers(String content) {

        // 1. Interface constants — public static final Type NAME = ...;
        //    Also catches bare "Type NAME;" field declarations inside interfaces
        Pattern fieldP = Pattern.compile(
            "(?:public\\s+)?(?:static\\s+)?(?:final\\s+)?([\\w<>\\[\\]]+)\\s+(\\w+)(?:\\s*=.*?)?;"
        );
        Matcher fm = fieldP.matcher(content);
        while (fm.find()) {
            String type = fm.group(1);
            String name = fm.group(2);
            if (isKeyword(type) || isKeyword(name)) continue;
            String entry = "[FIELD] " + type + " " + name;
            if (results.add(entry)) memberCount++;
        }

        // 2. Interface method signatures — with or without "public" / "default" / "static"
        //    Covers:
        //      void foo(int x);
        //      ReturnType bar(ParamType p);
        //      default void baz() { ... }
        //      static ReturnType qux() { ... }
        //      Type method() throws SomeException;
        Pattern mP = Pattern.compile(
            "(?:public\\s+)?(?:default\\s+)?(?:static\\s+)?" +
            "([\\w<>\\[\\]]+)\\s+" +        // return type
            "(\\w+)\\s*" +                   // method name
            "\\(([^)]*)\\)\\s*" +            // params
            "(?:throws\\s+[\\w,\\s]+)?\\s*" +
            "(?:\\{|;)"
        );
        Matcher mm = mP.matcher(content);
        while (mm.find()) {
            String returnType = mm.group(1);
            String methodName = mm.group(2);
            String params     = mm.group(3).trim();
            if (isKeyword(returnType) || isKeyword(methodName)) continue;
            String entry = "[INTERFACE METHOD] " + returnType + " " + methodName
                         + "(" + params + ")";
            if (results.add(entry)) memberCount++;
        }
    }

    // -------------------------------------------------------------------------
    // Class extraction (unchanged logic, just extracted to its own method)
    // -------------------------------------------------------------------------
    private static void extractClassMembers(String content, String className) {

        // 1. Fields — public [static] [final] Type fieldName [= ...];
        Pattern fieldP = Pattern.compile(
            "public\\s+(?:static\\s+)?(?:final\\s+)?([\\w<>\\[\\]]+)\\s+(\\w+)(?:\\s*=.*?)?;"
        );
        Matcher fm = fieldP.matcher(content);
        while (fm.find()) {
            String type = fm.group(1);
            String name = fm.group(2);
            if (isKeyword(type) || isKeyword(name)) continue;
            String entry = "[FIELD] " + type + " " + name;
            if (results.add(entry)) memberCount++;
        }

        // 2. Constructors — public ClassName([params]) { | throws | ;
        String ctorRegex = "public\\s+" + Pattern.quote(className)
                         + "\\s*\\(([^)]*)\\)\\s*(?:\\{|throws|;)";
        Pattern ctorP = Pattern.compile(ctorRegex);
        Matcher cm = ctorP.matcher(content);
        while (cm.find()) {
            String params = cm.group(1).trim();
            String entry = "[CONSTRUCTOR] " + className + "(" + params + ")";
            if (results.add(entry)) memberCount++;
        }

        // 3. Methods — (public|protected) [static] [abstract] [final] ReturnType name([params]) { | throws | ;
        Pattern mP = Pattern.compile(
            "(?:@Override\\s+)?" +
            "(public|protected)\\s+" +
            "(?:static\\s+)?(?:abstract\\s+)?(?:final\\s+)?" +
            "([\\w<>\\[\\]]+)\\s+" +
            "(\\w+)\\s*" +
            "\\(([^)]*)\\)\\s*" +
            "(?:\\{|throws|;)"
        );
        Matcher mm = mP.matcher(content);
        while (mm.find()) {
            String visibility = mm.group(1).toUpperCase();
            String returnType = mm.group(2);
            String methodName = mm.group(3);
            String params     = mm.group(4).trim();
            if (isKeyword(returnType) || isKeyword(methodName)) continue;
            String entry = "[" + visibility + " METHOD] " + returnType + " " + methodName
                         + "(" + (params.isEmpty() ? "" : params) + ")";
            if (results.add(entry)) memberCount++;
        }
    }

    // -------------------------------------------------------------------------
    // Shared helpers
    // -------------------------------------------------------------------------

    // Guards against Java keywords leaking through as type or method names
    private static final Set<String> KEYWORDS = new HashSet<String>(Arrays.asList(
        "class", "interface", "enum", "extends", "implements",
        "return", "import", "package", "new", "if", "else",
        "for", "while", "do", "switch", "case", "break",
        "continue", "try", "catch", "finally", "throw", "throws",
        "static", "final", "abstract", "synchronized", "native",
        "public", "protected", "private", "void", "boolean",
        "default", "instanceof"
    ));

    private static boolean isKeyword(String word) {
        return KEYWORDS.contains(word);
    }

    private static String extractClassName(String content, String fileName) {
        Pattern p = Pattern.compile("(?:class|interface|enum)\\s+(\\w+)");
        Matcher m = p.matcher(content);
        return m.find() ? m.group(1) : fileName.replace(".java", "");
    }

    private static void writeResults(String outputFile) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(outputFile));
            String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());
            writer.write("Java Public API & Member Extractor Report\n");
            writer.write("Generated on: " + timestamp + "\n");
            writer.write("------------------------------------------\n");
            for (String line : results) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing output file: " + e.getMessage());
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (IOException ignored) {}
            }
        }
    }
    
}
