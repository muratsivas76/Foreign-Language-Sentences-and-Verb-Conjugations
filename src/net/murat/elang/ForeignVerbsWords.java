package net.murat.elang;

import java.io.*;
import java.util.*;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import android.view.inputmethod.InputMethodManager;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ForeignVerbsWords extends Activity {
  
  // UI Components
  private Spinner spinnerFiles;
  private Spinner spinnerVerbs;
  private Spinner spinnerTenses;
  private TextView resultTextView;
  private ScrollView scrollView;
  
  private Button generateButton;
  private Button timeButton;
  private Button startButton;
  private Button stopButton;
  private Button exitButton;
  
  // Threading components
  private ExecutorService executor;
  private Future<?> currentTask;
  private Handler mainHandler;
  private volatile boolean isCancelled;
  
  // String resources
  private String enterTimeStr;
  private String msc;
  private String waitingTime;
  private final String errorStr = "Error";
  private String verbsStr;
  private String tensesStr;
  
  // Input method manager for keyboard control
  private InputMethodManager imm = null;
  
  // Pronoun arrays for conjugation display
  private final String[] PRONOUNS = {
    "\u0645\u0646: ",
    "\u062A\u0648: ",
    "\u0627\u0648: ",
    "\u0645\u0627: ",
    "\u0634\u0645\u0627: ",
    "\u0622\u0646\u0647\u0627: "
  };
  
  private final String[][] PRONOUNS_BY_TENSE = {
    PRONOUNS,
    PRONOUNS,
    PRONOUNS,
    PRONOUNS,
    PRONOUNS,
    PRONOUNS,
    PRONOUNS,
    PRONOUNS,
    PRONOUNS,
    PRONOUNS,
    PRONOUNS,
    PRONOUNS,
    PRONOUNS,
    PRONOUNS,
    PRONOUNS,
    PRONOUNS,
    PRONOUNS,
    PRONOUNS,
    {"\u0627\u0648: ", "\u0634\u0645\u0627: ", "\u0622\u0646\u0647\u0627: "},
    PRONOUNS,
    {"\u0627\u0648: ", "\u0622\u0646\u0647\u0627: "},
    {"\u062A\u0648: ", "\u0634\u0645\u0627: "},
    {"\u062A\u0648: ", "\u0634\u0645\u0627: "}
  };
  
  private static final int CERO = 0x0000;
  private static int PAUSE_TIME = 1000;
  
  // Selected verb and tense tracking
  private String selectedVerb;
  private String selectedTense;
  private int verbIndex;
  private int tenseIndex;
  
  // Data structures for random words
  private Vector<WordsInfo> wordsList;
  private Random randomGenerator;
  
  // File tracking for Generate button
  private String selectedFileName;
  private Vector<String> fileNamesList;
  
  // Verb arrays
  private final String[] VERB_OPTIONS = new String[]{
    "\u0628\u0648\u062F\u0646",
    "\u062F\u0627\u0634\u062A\u0646",
    "\u0631\u0641\u062A\u0646",
    "\u0622\u0645\u062F\u0646",
    "\u06AF\u0641\u062A\u0646",
    "\u062F\u06CC\u062F\u0646",
    "\u0634\u0646\u06CC\u062F\u0646",
    "\u0646\u0648\u0634\u062A\u0646",
    "\u062E\u0648\u0627\u0646\u062F\u0646",
    "\u062E\u0648\u0631\u062F\u0646"
  };
  
  private final String[] VERB_FILES = new String[]{
    "budan",
    "dashtan",
    "raftan",
    "amadan",
    "goftan",
    "didan",
    "shenidan",
    "neveshtan",
    "khandan",
    "khordan"
  };
  
  private final String[] TENSE_OPTIONS = new String[]{
    "\u06AF\u0630\u0634\u062A\u0647 \u0633\u0627\u062F\u0647",
    "\u062D\u0627\u0644 \u06A9\u0627\u0645\u0644",
    "\u062D\u0627\u0644 \u0627\u0633\u062A\u0645\u0631\u0627\u0631\u06CC",
    "\u062D\u0627\u0644 \u0633\u0627\u062F\u0647",
    "\u0622\u06CC\u0646\u062F\u0647 \u0633\u0627\u062F\u0647",
    "\u06AF\u0630\u0634\u062A\u0647 \u062F\u0648\u0631",
    "\u06AF\u0630\u0634\u062A\u0647 \u06A9\u0627\u0645\u0644",
    "\u06AF\u0630\u0634\u062A\u0647 \u0627\u0633\u062A\u0645\u0631\u0627\u0631\u06CC",
    "\u06AF\u0630\u0634\u062A\u0647 \u0627\u0644\u062A\u0632\u0627\u0645\u06CC",
    "\u06AF\u0630\u0634\u062A\u0647 \u0622\u06CC\u0646\u062F\u0647",
    "\u0646\u0642\u0644 \u0642\u0648\u0644 \u062D\u0627\u0644 \u0627\u0633\u062A\u0645\u0631\u0627\u0631\u06CC",
    "\u0646\u0642\u0644 \u0642\u0648\u0644 \u062D\u0627\u0644 \u0633\u0627\u062F\u0647",
    "\u0646\u0642\u0644 \u0642\u0648\u0644 \u0622\u06CC\u0646\u062F\u0647",
    "\u0634\u0631\u0637\u06CC \u06AF\u0630\u0634\u062A\u0647",
    "\u0634\u0631\u0637\u06CC \u06A9\u0627\u0645\u0644",
    "\u0634\u0631\u0637\u06CC \u062D\u0627\u0644 \u0627\u0633\u062A\u0645\u0631\u0627\u0631\u06CC",
    "\u0634\u0631\u0637\u06CC \u0622\u06CC\u0646\u062F\u0647",
    "\u0627\u0644\u062A\u0632\u0627\u0645\u06CC \u062D\u0627\u0644",
    "\u0627\u0645\u0631\u06CC",
    "\u0628\u0627\u06CC\u062F",
    "\u0627\u0645\u0631\u06CC \u063A\u0627\u06CC\u0628",
    "\u0627\u0645\u0631\u06CC \u062D\u0627\u0636\u0631",
    "\u0646\u0647\u06CC",
    "Random Words/Sentences"
  };
  
  private final String[] CONTROLS = new String[]{
    "gozashtesade",
    "kamel",
    "estemrari",
    "halesade",
    "ayendesade",
    "gozashtedor",
    "gozashtekamel",
    "gozashteestemrari",
    "gozashteeltezami",
    "gozashteayende",
    "naghlestemrari",
    "naghlesade",
    "naghlayende",
    "shertigozashte",
    "shertikamel",
    "shertiestemrari",
    "shertiayende",
    "haleeltezami",
    "amri",
    "bayad",
    "amrigayeb",
    "amrihazir",
    "nahy",
    "random"
  };
  
  private String PATH = "";
  
  // Color spans for text highlighting
  private final ForegroundColorSpan REDFORE = new ForegroundColorSpan(0xFFCC0000);
  private final ForegroundColorSpan BLUFORE = new ForegroundColorSpan(0xFF0000EF);
  private final int EXCSPN = android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE;
  
  @Override
  public void onCreate(Bundle paramBundle) {
    super.onCreate(paramBundle);
    setContentView(R.layout.main);
    
    PATH = getMainPathName() + "/otherlang/";
    
    // Initialize input method manager
    imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    
    // Initialize data structures - wordsList starts empty
    wordsList = new Vector<WordsInfo>();
    fileNamesList = new Vector<String>();
    randomGenerator = new Random();
    
    // Initialize executor service and handler
    executor = Executors.newSingleThreadExecutor();
    mainHandler = new Handler(Looper.getMainLooper());
    isCancelled = false;
    
    // Initialize selected values
    selectedVerb = VERB_OPTIONS[0];
    selectedTense = TENSE_OPTIONS[0];
    verbIndex = 0;
    tenseIndex = 0;
    selectedFileName = "";
    
    // Load string resources
    loadStrings();
    
    // Initialize UI components
    initViews();
    
    // Setup spinners and buttons
    setupFileSpinner();
    setupSpinners();
    setupButtons();
    
    // Set text view height
    setPaneHeight(timeButton, resultTextView, 0x0003);
  }
  
  /**
   * Load string resources from XML
   */
  private void loadStrings() {
    final android.content.res.Resources resources = getResources();
    enterTimeStr = resources.getString(R.string.enterTimeStr);
    waitingTime = resources.getString(R.string.waitingTimeStr);
    msc = resources.getString(R.string.mscStr);
    verbsStr = resources.getString(R.string.verbsStr);
    tensesStr = resources.getString(R.string.tensesStr);
  }
  
  /**
   * Initialize all view components
   */
  private void initViews() {
    spinnerFiles = (Spinner) findViewById(R.id.spinner_files);
    spinnerVerbs = (Spinner) findViewById(R.id.spinner_verbs);
    spinnerTenses = (Spinner) findViewById(R.id.spinner_tenses);
    resultTextView = (TextView) findViewById(R.id.tasrif_tv);
    scrollView = (ScrollView) findViewById(R.id.scroll_tv);
    
    resultTextView.setMovementMethod(new android.text.method.ScrollingMovementMethod());
    
    generateButton = (Button) findViewById(R.id.generate_button);
    timeButton = (Button) findViewById(R.id.time_button);
    startButton = (Button) findViewById(R.id.start_button);
    stopButton = (Button) findViewById(R.id.stop_button);
    exitButton = (Button) findViewById(R.id.exit_button);
  }
  
  /**
   * Close the soft keyboard
   */
  private void closeKeyboard(EditText editText) {
    if (imm != null) {
      imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }
  }
  
  /**
   * Setup the file spinner with .txt files from otherlang directory
   */
  private void setupFileSpinner() {
    // Clear and reload file names from directory
    fileNamesList.clear();
    loadFileNamesFromDirectory();
    
    // Create adapter for file spinner
    ArrayAdapter<String> fileAdapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_spinner_item, fileNamesList) {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView text = (TextView) view;
            text.setTextSize(18);
            return view;
        }
        
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = super.getDropDownView(position, convertView, parent);
            TextView text = (TextView) view;
            text.setTextSize(18);
            return view;
        }
    };
    fileAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinnerFiles.setAdapter(fileAdapter);
    
    // Set default selection
    if (fileNamesList.size() > 0) {
      selectedFileName = fileNamesList.get(0);
    } else {
      selectedFileName = "";
    }
    
    // Set item selected listener
    spinnerFiles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
          if (position >= 0 && position < fileNamesList.size()) {
            selectedFileName = fileNamesList.get(position);
          }
        }
        public void onNothingSelected(AdapterView<?> parent) {
          selectedFileName = "";
        }
    });
  }
  
  /**
   * Load file names from Download/otherlang directory
   * Only .txt files are considered, extension is removed for display
   */
  private void loadFileNamesFromDirectory() {
    try {
      //String path = getMainPathName();
      File dir = new File(PATH);//path + "/otherlang");
      
      if (!dir.exists() || !dir.isDirectory()) {
        return;
      }
      
      File[] files = dir.listFiles(new FilenameFilter() {
          public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".txt");
          }
      });
      
      if (files == null || files.length == 0) {
        return;
      }
      
      for (File file : files) {
        String fileName = file.getName();
        // Remove .txt extension for display
        if (fileName.endsWith(".txt")) {
          fileName = fileName.substring(0, fileName.length() - 4);
        }
        fileNamesList.add(fileName);
      }
      
    } catch (Exception e) {
      // Ignore errors - file list will be empty
    }
  }
  
  /**
   * Get the main path for external storage
   */
  private String getMainPathName() {
    File path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
    String px = path.getAbsolutePath();
    String exclude = "Android/data/" + (getPackageName()) + "/files/";
    px = px.replace(exclude, "");
    return px;
  }
  
  /**
   * Setup verb and tense spinners
   */
  private void setupSpinners() {
    // Verb spinner adapter
    ArrayAdapter<String> verbAdapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_spinner_item, VERB_OPTIONS) {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView text = (TextView) view;
            text.setTextSize(18);
            return view;
        }
        
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = super.getDropDownView(position, convertView, parent);
            TextView text = (TextView) view;
            text.setTextSize(18);
            return view;
        }
    };
    verbAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinnerVerbs.setAdapter(verbAdapter);
    
    // Tense spinner adapter
    ArrayAdapter<String> tenseAdapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_spinner_item, TENSE_OPTIONS) {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView text = (TextView) view;
            text.setTextSize(18);
            return view;
        }
        
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = super.getDropDownView(position, convertView, parent);
            TextView text = (TextView) view;
            text.setTextSize(18);
            return view;
        }
    };
    tenseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinnerTenses.setAdapter(tenseAdapter);
    
    // Verb spinner listener
    spinnerVerbs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            selectedVerb = VERB_OPTIONS[position];
            verbIndex = position;
            resultTextView.setText("\n" + selectedVerb + ": " + selectedTense);
        }
        public void onNothingSelected(AdapterView<?> parent) {}
    });
    
    // Tense spinner listener
    spinnerTenses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            selectedTense = TENSE_OPTIONS[position];
            tenseIndex = position;
            resultTextView.setText("\n" + selectedVerb + ": " + selectedTense);
        }
        public void onNothingSelected(AdapterView<?> parent) {}
    });
  }
  
  /**
   * Setup all button click listeners
   */
  private void setupButtons() {
    // Generate button - loads words from selected file
    generateButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View view) {
          generateWordsFromFile();
        }
    });
    
    // Time button - shows dialog to set pause time
    timeButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View view) {
          showTimeDialog();
        }
    });
    
    // Start button - begins conjugation display
    startButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View view) {
          resultTextView.setText("");
          cancelCurrentTask();
          resultTextView.setText("\n");
          startConjugation();
        }
    });
    
    // Stop button - cancels current task
    stopButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View view) {
          cancelCurrentTask();
          resultTextView.setText("Cancelled...");
        }
    });
    
    // Exit button - cleans up and exits
    exitButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View view) {
          cancelCurrentTask();
          executor.shutdownNow();
          if (wordsList != null) wordsList.removeAllElements();
          finishAndRemoveTask();
          
          new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
              @Override
              public void run() {
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
              }
          }, 250);
        }
    });
  }
  
  /**
   * Generate words from the selected file
   * Reads langA:langB format lines and populates wordsList
   */
  private void generateWordsFromFile() {
    // Clear existing words
    if (wordsList != null) {
      wordsList.removeAllElements();
    }
    
    // Check if a file is selected
    if (selectedFileName == null || selectedFileName.trim().length() == 0) {
      resultTextView.setText("\nNo file selected. Please select a file from the spinner.");
      return;
    }
    
    try {
      //String path = getMainPathName();
      File dir = new File(PATH);//path + "/otherlang");
      
      if (!dir.exists() || !dir.isDirectory()) {
        resultTextView.setText("\nDirectory not found: " + PATH);
        return;
      }
      
      String fullFileName = selectedFileName + ".txt";
      File file = new File(dir, fullFileName);
      
      if (!file.exists()) {
        resultTextView.setText("\nFile not found: " + fullFileName);
        return;
      }
      
      BufferedReader br = null;
      int lineCount = 0;
      
      try {
        br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String line;
        
        while ((line = br.readLine()) != null) {
          line = line.trim();
          if (line.length() == 0) {
            continue; // Skip empty lines
          }
          
          int colonIndex = line.indexOf(":");
          if (colonIndex == -1) {
            continue; // Skip lines without colon separator
          }
          
          String langA = line.substring(0, colonIndex).trim();
          String langB = line.substring(colonIndex + 1).trim();
          
          if (langA.length() == 0 || langB.length() == 0) {
            continue; // Skip if either side is empty
          }
          
          WordsInfo word = new WordsInfo(langA, langB);
          wordsList.add(word);
          lineCount++;
        }
        
        // Show success message
        final int count = lineCount;
        resultTextView.setText("\nLoaded " + count + " words from: " + selectedFileName);
        
      } catch (Exception e) {
        resultTextView.setText("\nError reading file: " + e.toString());
      } finally {
        if (br != null) {
          try { br.close(); } catch (IOException e) {}
        }
      }
      
    } catch (Exception e) {
      resultTextView.setText("\nError: " + e.toString());
    }
  }
  
  /**
   * Show dialog to set pause time between conjugations
   */
  private void showTimeDialog() {
    resultTextView.setText("\n");
    
    AlertDialog.Builder alert = new AlertDialog.Builder(ForeignVerbsWords.this);
    alert = alert.setTitle(enterTimeStr);
    
    final EditText htime = new EditText(ForeignVerbsWords.this);
    htime.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(2)});
    htime.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
    
    alert.setView(htime);
    
    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int wh) {
          closeKeyboard(htime);
          
          String res = htime.getText().toString();
          if (res == null || res.length() < 1) return;
          
          try {
            int c = Integer.parseInt(res);
            if (c < 1) c = 1;
            if (c > 10) c = 10;
            PAUSE_TIME = c * 1000;
            resultTextView.setText("\n" + waitingTime + " " + PAUSE_TIME + " " + msc);
          } catch (Exception e) {
            // Ignore parse errors
          }
        }
    });
    
    alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int wh) {
          closeKeyboard(htime);
          PAUSE_TIME = 1000;
          resultTextView.setText("\n" + waitingTime + " " + PAUSE_TIME + " " + msc);
        }
    });
    
    AlertDialog pane = alert.create();
    pane.show();
  }
  
  /**
   * Set the height of the result text view
   */
  @SuppressWarnings("deprecation")
  private void setPaneHeight(Button b, TextView t, int margin) {
    android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(metrics);
    
    int hp = metrics.heightPixels;
    int hpn = hp / 2;
    int hph = hp / 11;
    int minmaxheight = hpn + hph;
    
    t.setMinHeight(minmaxheight);
    t.setMaxHeight(minmaxheight);
  }
  
  /**
   * Start the conjugation display process
   */
  private void startConjugation() {
    isCancelled = false;
    
    currentTask = executor.submit(new Runnable() {
        @Override
        public void run() {
          try {
            // Check if Random mode is selected
            if (tenseIndex == 23) {
              showRandomWord();
              return;
            }
            
            String fileName = VERB_FILES[verbIndex] + "_" + CONTROLS[tenseIndex] + ".utf";
            
            AssetManager assetManager = getAssets();
            InputStream is = assetManager.open("verbs/" + fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            
            List<String> lines = new ArrayList<String>();
            String line;
            while ((line = br.readLine()) != null) {
              lines.add(line);
            }
            br.close();
            is.close();
            
            if (lines.size() == 0) {
              mainHandler.post(new Runnable() {
                  @Override
                  public void run() {
                    resultTextView.setText("\n" + errorStr + ": Empty file!");
                  }
              });
              return;
            }
            
            // Display each conjugation line with pause
            for (int i = 0; i < lines.size() && !isCancelled; i++) {
              final String conjugation = lines.get(i);
              final int index = i;
              mainHandler.post(new Runnable() {
                  @Override
                  public void run() {
                    if (index == 0) {
                      showTitle(conjugation);
                    } else {
                      updateText(conjugation, index - 1);
                    }
                  }
              });
              
              try {
                Thread.sleep(PAUSE_TIME);
              } catch (InterruptedException e) {
                break;
              }
            }
            
          } catch (Exception e) {
            final String error = e.toString();
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                  resultTextView.setText("\n" + errorStr + ": " + error);
                }
            });
          }
        }
    });
  }
  
  /**
   * Display the title line in red
   */
  private void showTitle(String title) {
    SpannableStringBuilder builder = new SpannableStringBuilder();
    SpannableString spn = new SpannableString(title);
    spn.setSpan(REDFORE, 0, title.length(), EXCSPN);
    builder.append(spn);
    builder.append("\n\n");
    resultTextView.setText(builder);
  }
  
  /**
   * Display a random word from the loaded word list
   */
  private void showRandomWord() {
    if (wordsList == null || wordsList.size() == 0) {
      mainHandler.post(new Runnable() {
          @Override
          public void run() {
            resultTextView.setText("\nNo words loaded! Please use Generate button first to load words from a file.");
          }
      });
      return;
    }
    
    int index = randomGenerator.nextInt(wordsList.size());
    final WordsInfo word = wordsList.get(index);
    
    final boolean showLangAFirst = randomGenerator.nextBoolean();
    
    mainHandler.post(new Runnable() {
        @Override
        public void run() {
          SpannableStringBuilder builder = new SpannableStringBuilder();
          
          String first, second;
          if (showLangAFirst) {
            first = word.getLangA();
            second = word.getLangB();
          } else {
            first = word.getLangB();
            second = word.getLangA();
          }
          
          SpannableString spn1 = new SpannableString(first);
          spn1.setSpan(REDFORE, 0, first.length(), EXCSPN);
          builder.append(spn1);
          builder.append("\n\n\n\n\n\n\n\n");
          SpannableString spn2 = new SpannableString(second);
          spn2.setSpan(BLUFORE, 0, second.length(), EXCSPN);
          builder.append(spn2);
          
          resultTextView.setText(builder);
        }
    });
  }
  
  /**
   * Update the text view with a new conjugation line
   */
  private void updateText(String conjugation, int lineIndex) {
    SpannableStringBuilder builder = new SpannableStringBuilder();
    String[] pronouns = PRONOUNS_BY_TENSE[tenseIndex];
    String pronoun = (lineIndex < pronouns.length) ? pronouns[lineIndex] : pronouns[0];
    SpannableString spn = new SpannableString(pronoun + conjugation);
    spn.setSpan(BLUFORE, 0, pronoun.length(), EXCSPN);
    builder.append(spn);
    builder.append("\n");
    
    resultTextView.setText(builder);
  }
  
  /**
   * Cancel the currently running task
   */
  private void cancelCurrentTask() {
    isCancelled = true;
    if (currentTask != null) {
      currentTask.cancel(true);
      currentTask = null;
    }
    executor.shutdownNow();
    executor = Executors.newSingleThreadExecutor();
  }
  
  @Override
  public void onResume() {
    super.onResume();
  }
  
  @Override
  public void onPause() {
    super.onPause();
  }
  
  @Override
  protected void onDestroy() {
    super.onDestroy();
  }
  
}
