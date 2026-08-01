package net.murat.elang;

public class WordsInfo {
  
  private String langA;
  private String langB;
  
  public WordsInfo() {
    this.langA = "";
    this.langB = "";
  }
  
  public WordsInfo(String langA, String langB) {
    this.langA = langA;
    this.langB = langB;
  }
  
  public String getLangA() {
    return langA;
  }
  
  public void setLangA(String langA) {
    this.langA = langA;
  }
  
  public String getLangB() {
    return langB;
  }
  
  public void setLangB(String langB) {
    this.langB = langB;
  }
  
  public void setWords(String langA, String langB) {
    this.langA = langA;
    this.langB = langB;
  }
  
  public String getWord(boolean fromA) {
    return fromA ? langA : langB;
  }
  
  public String toString() {
    return langA + " : " + langB;
  }
  
  public boolean isEmpty() {
    return langA == null || langA.trim().length() == 0 ||
    langB == null || langB.trim().length() == 0;
  }
  
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    
    WordsInfo other = (WordsInfo) obj;
    
    if (langA == null) {
      if (other.langA != null) return false;
      } else if (!langA.equals(other.langA)) {
      return false;
    }
    
    if (langB == null) {
      if (other.langB != null) return false;
      } else if (!langB.equals(other.langB)) {
      return false;
    }
    
    return true;
  }
  
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((langA == null) ? 0 : langA.hashCode());
    result = prime * result + ((langB == null) ? 0 : langB.hashCode());
    return result;
  }
  
}
