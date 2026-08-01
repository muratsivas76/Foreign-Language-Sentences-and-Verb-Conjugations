import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * This class recursively searches for .java files in a given directory
 * and writes their absolute paths to a file named 'fileNames.txt'.
 * Compatible with Java 6 and later.
 */
public class CollectFileNames {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java CollectFileNames <directory_path>");
            return;
        }

        File rootDir = new File(args[0]);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            System.out.println("Invalid directory path provided.");
            return;
        }

        List<File> javaFiles = new ArrayList<File>();
        findJavaFiles(rootDir, javaFiles);

        writeResultsToFile(javaFiles, "fileNames.txt");
    }

    /**
     * Recursively traverses the directory tree to collect .java files.
     */
    private static void findJavaFiles(File directory, List<File> resultList) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isDirectory()) {
                    findJavaFiles(file, resultList);
                } else if (file.getName().endsWith(".java")) {
                    resultList.add(file);
                }
            }
        }
    }

    /**
     * Writes the list of file paths to the output file.
     */
    private static void writeResultsToFile(List<File> fileList, String outputFilename) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileWriter(outputFilename));
            for (int i = 0; i < fileList.size(); i++) {
                writer.println(fileList.get(i).getAbsolutePath());
            }
            System.out.println("Successfully found " + fileList.size() + " .java files.");
            System.out.println("Results written to: " + outputFilename);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
    
}
