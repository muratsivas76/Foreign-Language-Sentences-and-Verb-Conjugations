//package net.murat.esfiil;

import java.io.*;
import java.util.*;

public class DosyaUretici {
    
    private static final String JSON_FILE = "frsverbos.json";
    private static final String OUTPUT_DIR = "verbs";
    private static final String ENCODING = "UTF-8";
    
    public static void main(String[] args) {
        DosyaUretici uretici = new DosyaUretici();
        uretici.uretimYap();
    }
    
    public void uretimYap() {
        try {
            String jsonContent = dosyaOku(JSON_FILE);
            
            // JSON'u parse et
            Map<String, Map<String, List<String>>> veriler = jsonParse(jsonContent);
            
            File dir = new File(OUTPUT_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            int toplamDosya = 0;
            
            for (String fiilAdi : veriler.keySet()) {
                Map<String, List<String>> zamanlar = veriler.get(fiilAdi);
                
                for (String zamanAdi : zamanlar.keySet()) {
                    List<String> cekimler = zamanlar.get(zamanAdi);
                    
                    String dosyaAdi = fiilAdi + "_" + zamanAdi + ".utf";
                    String dosyaYolu = OUTPUT_DIR + File.separator + dosyaAdi;
                    
                    dosyaYaz(dosyaYolu, cekimler);
                    toplamDosya++;
                    
                    System.out.println("Oluşturuldu: " + dosyaAdi);
                }
            }
            
            System.out.println("\nToplam " + toplamDosya + " dosya oluşturuldu!");
            System.out.println("Klasör: " + new File(OUTPUT_DIR).getAbsolutePath());
            
        } catch (Exception e) {
            System.err.println("Hata: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String dosyaOku(String dosyaAdi) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        
        try {
            br = new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(dosyaAdi), ENCODING
                )
            );
            
            String satir;
            while ((satir = br.readLine()) != null) {
                sb.append(satir);
            }
            
        } finally {
            if (br != null) {
                try { br.close(); } catch (IOException e) {}
            }
        }
        
        return sb.toString();
    }
    
    private void dosyaYaz(String dosyaYolu, List<String> cekimler) throws IOException {
        BufferedWriter bw = null;
        
        try {
            bw = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(dosyaYolu), ENCODING
                )
            );
            
            for (String cekim : cekimler) {
                bw.write(cekim);
                bw.newLine();
            }
            
            bw.flush();
            
        } finally {
            if (bw != null) {
                try { bw.close(); } catch (IOException e) {}
            }
        }
    }
    
    // Basit JSON parser - sadece ihtiyacımız olan yapıyı çözümler
    private Map<String, Map<String, List<String>>> jsonParse(String json) {
        Map<String, Map<String, List<String>>> result = new HashMap<String, Map<String, List<String>>>();
        
        // Boşlukları temizle
        json = json.trim();
        
        // Dıştaki süslü parantezleri at
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1);
        }
        
        // Ana seviyeyi parse et (fiiller)
        List<String> anaSeviye = splitByComma(json);
        
        for (String anaItem : anaSeviye) {
            int colonIndex = anaItem.indexOf(":");
            if (colonIndex == -1) continue;
            
            String fiilAdi = anaItem.substring(0, colonIndex).trim();
            fiilAdi = temizle(fiilAdi);
            
            String zamanlarStr = anaItem.substring(colonIndex + 1).trim();
            
            // Zamanları parse et
            Map<String, List<String>> zamanMap = parseZamanlar(zamanlarStr);
            result.put(fiilAdi, zamanMap);
        }
        
        return result;
    }
    
    private Map<String, List<String>> parseZamanlar(String zamanlarStr) {
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        
        zamanlarStr = zamanlarStr.trim();
        
        // Süslü parantezleri at
        if (zamanlarStr.startsWith("{") && zamanlarStr.endsWith("}")) {
            zamanlarStr = zamanlarStr.substring(1, zamanlarStr.length() - 1);
        }
        
        List<String> zamanList = splitByComma(zamanlarStr);
        
        for (String zamanItem : zamanList) {
            int colonIndex = zamanItem.indexOf(":");
            if (colonIndex == -1) continue;
            
            String zamanAdi = zamanItem.substring(0, colonIndex).trim();
            zamanAdi = temizle(zamanAdi);
            
            String cekimlerStr = zamanItem.substring(colonIndex + 1).trim();
            List<String> cekimler = parseCekimler(cekimlerStr);
            
            result.put(zamanAdi, cekimler);
        }
        
        return result;
    }
    
    private List<String> parseCekimler(String cekimlerStr) {
        List<String> result = new ArrayList<String>();
        
        cekimlerStr = cekimlerStr.trim();
        
        // Köşeli parantezleri at
        if (cekimlerStr.startsWith("[") && cekimlerStr.endsWith("]")) {
            cekimlerStr = cekimlerStr.substring(1, cekimlerStr.length() - 1);
        }
        
        List<String> items = splitByComma(cekimlerStr);
        
        for (String item : items) {
            String cekim = temizle(item);
            if (cekim.length() > 0) {
                result.add(cekim);
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
    
    private String temizle(String str) {
        str = str.trim();
        if (str.startsWith("\"") && str.endsWith("\"")) {
            str = str.substring(1, str.length() - 1);
        }
        // Escape karakterleri düzelt
        str = str.replace("\\\"", "\"");
        str = str.replace("\\\\", "\\");
        return str;
    }
    
}
