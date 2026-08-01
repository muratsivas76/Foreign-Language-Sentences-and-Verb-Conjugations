import java.io.*;
import java.util.*;

/**
 * MergeFiles - Java 6/7/8 uyumlu dosya birleştirme aracı
 * Kullanım: java MergeFiles outputFile fileOrDirectory...
 */
public class MergeFiles {
    
    // Text dosyası olarak kabul edilen uzantılar (case insensitive)
    private static final Set<String> TEXT_EXTENSIONS = new HashSet<String>(Arrays.asList(
        "java", "txt", "c", "cpp", "h", "hpp", "m", "mf", "js", "css", 
        "htm", "html", "sh", "bash", "bat", "cmd", "cs", "php", 
        "py", "rb", "pl", "pm", "sql", "xml", "json", "yml", "yaml",
        "properties", "gradle", "xml", "xsd", "dtd", "conf", "config",
        "ini", "cfg", "log", "out", "err", "template"
    ));
    
    private static final String START_MARKER = "/** Starts ";
    private static final String END_MARKER = "/** Ends ";
    private static final String MARKER_SUFFIX = " */";
    
    private PrintWriter outputWriter;
    private int fileCount = 0;
    private int errorCount = 0;
    private List<String> processedFiles = new ArrayList<String>();
    private List<String> errorFiles = new ArrayList<String>();
    
    public static void main(String[] args) {
        if (args.length < 2) {
            printUsage();
            System.exit(1);
        }
        
        String outputFileName = args[0];
        File cnt = new File(outputFileName);
        if (cnt.exists()) {
			System.out.println ("\nReturning because " + cnt.getName() + " already exists.\nFirst argument is output file.\n");
			System.exit(0);
		}
		
        List<String> inputs = new ArrayList<String>();
        for (int i = 1; i < args.length; i++) {
            inputs.add(args[i]);
        }
        
        MergeFiles merger = new MergeFiles();
        boolean success = merger.mergeFiles(outputFileName, inputs);
        
        if (!success) {
            System.exit(1);
        }
        System.exit(0);
    }
    
    private static void printUsage() {
        System.out.println("MergeFiles - Dosya birleştirme aracı");
        System.out.println("Kullanım: java MergeFiles outputFile fileOrDirectory...");
        System.out.println();
        System.out.println("Örnekler:");
        System.out.println("  java MergeFiles merged.java A.java src/net/math B.java C.txt");
        System.out.println("  java MergeFiles all.txt src/ doc/ notes.txt");
        System.out.println();
        System.out.println("Desteklenen uzantılar: " + TEXT_EXTENSIONS);
        System.out.println("Dosya başlangıcına /** Starts dosyaAdı */ eklenir");
        System.out.println("Dosya sonuna /** Ends dosyaAdı */ eklenir");
    }
    
    /**
     * Ana birleştirme işlemini gerçekleştirir
     * @param outputFileName Çıktı dosyası adı
     * @param inputs Girdi dosya/dizin listesi
     * @return Başarılı ise true, hata varsa false
     */
    public boolean mergeFiles(String outputFileName, List<String> inputs) {
        try {
            outputWriter = new PrintWriter(new BufferedWriter(new FileWriter(outputFileName)));
            System.out.println("Birleştirme başladı: " + outputFileName);
            System.out.println("========================================");
            
            for (String input : inputs) {
                processInput(input);
            }
            
            outputWriter.close();
            
            // Özet bilgileri göster
            System.out.println("========================================");
            System.out.println("Birleştirme tamamlandı!");
            System.out.println("Toplam dosya: " + fileCount);
            System.out.println("Başarılı dosya: " + (fileCount - errorCount));
            System.out.println("Hatalı dosya: " + errorCount);
            
            if (errorCount > 0) {
                System.out.println("Hatalı dosyalar:");
                for (String errorFile : errorFiles) {
                    System.out.println("  - " + errorFile);
                }
                return false;
            }
            
            return true;
            
        } catch (IOException e) {
            System.err.println("HATA: Çıktı dosyası açılamadı: " + outputFileName);
            System.err.println("  Nedeni: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Bir girdiyi işler (dosya veya dizin)
     */
    private void processInput(String path) {
        File file = new File(path);
        
        if (!file.exists()) {
            System.err.println("UYARI: Dosya/dizin bulunamadı: " + path);
            errorCount++;
            errorFiles.add(path);
            return;
        }
        
        if (file.isDirectory()) {
            processDirectory(file);
        } else {
            processFile(file);
        }
    }
    
    /**
     * Bir dizini işler (sadece birinci seviye, alt dizinlere girmez)
     */
    private void processDirectory(File directory) {
        System.out.println("Dizin taranıyor: " + directory.getPath());
        
        File[] files = directory.listFiles();
        if (files == null) {
            System.err.println("UYARI: Dizin okunamadı: " + directory.getPath());
            errorCount++;
            errorFiles.add(directory.getPath());
            return;
        }
        
        for (File file : files) {
            if (file.isFile() && isTextFile(file)) {
                processFile(file);
            }
        }
    }
    
    /**
     * Bir dosyayı işler
     */
    private void processFile(File file) {
        if (!isTextFile(file)) {
            System.out.println("Atlanıyor (desteklenmeyen uzantı): " + file.getName());
            return;
        }
        
        System.out.println("İşleniyor: " + file.getPath());
        
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            
            // Dosya başlangıç işareti
            outputWriter.println(START_MARKER + file.getName() + MARKER_SUFFIX);
            
            // Dosya içeriğini oku ve yaz
            String line;
            while ((line = reader.readLine()) != null) {
                outputWriter.println(line);
            }
            
            // Dosya bitiş işareti
            outputWriter.println(END_MARKER + file.getName() + MARKER_SUFFIX);
            outputWriter.println(); // Boş satır
            
            fileCount++;
            processedFiles.add(file.getPath());
            
        } catch (IOException e) {
            System.err.println("HATA: Dosya okunamadı: " + file.getPath());
            System.err.println("  Nedeni: " + e.getMessage());
            errorCount++;
            errorFiles.add(file.getPath());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // Ignore close error
                }
            }
        }
    }
    
    /**
     * Dosyanın text dosyası olup olmadığını kontrol eder
     */
    private boolean isTextFile(File file) {
        if (!file.isFile() || file.isHidden()) {
            return false;
        }
        
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex == -1) {
            // Uzantısız dosyaları text olarak kabul et
            return true;
        }
        
        String extension = name.substring(dotIndex + 1).toLowerCase();
        return TEXT_EXTENSIONS.contains(extension);
    }
    
    /**
     * Desteklenen uzantıları döndürür (yardımcı metod)
     */
    public static Set<String> getTextExtensions() {
        return new HashSet<String>(TEXT_EXTENSIONS);
    }
    
    /**
     * İşlenen dosyaların listesini döndürür
     */
    public List<String> getProcessedFiles() {
        return new ArrayList<String>(processedFiles);
    }
    
    /**
     * Hata alınan dosyaların listesini döndürür
     */
    public List<String> getErrorFiles() {
        return new ArrayList<String>(errorFiles);
    }
}
