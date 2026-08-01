# Foreign Language Sentences & Verb Conjugations

## About

This application was originally designed by Murat Inan years ago for Turkish speakers learning Spanish verb conjugations. At that time, programming was more challenging, and Murat Inan single-handedly created 170 short verb conjugation UTF files by manually typing each one.

Years passed, technology advanced, and artificial intelligence entered our world. Murat Inan presented his Spanish verb conjugation application to DeepSeek AI. The AI generated the necessary verb conjugation files and updated the program to Android 29 level. This is how the current **Persian Sentences & Verb Conjugations** program came to be.

## Customization

You can modify this application to learn any language you want by editing the source code and data files.

### Example Scenario

Suppose you are an American learning Turkish and want to create a sample application with:
- **2 verbs**: "okumak" (to read) and "yazmak" (to write)
- **2 tenses**: "geçmiş" (past) and "şimdiki" (present)

In this case, update the final class variables in the `ForeignVerbsWords` class according to your needs.

### How to Generate Your Own Verb Files

1. Examine the `frsverbos.json` and `FileGenerator` files in the `tool/` directory
2. Create your own `frsverbos.json` file with your target verbs and tenses
3. Run `FileGenerator` to create the `verbs/` directory with `.utf` files
4. Move the generated `verbs/` folder to the `assets/` directory in the project root

Example generated files for the Turkish example:
```
verbs/okumak_gechmish.utf
verbs/okumak_shimdiki.utf
verbs/yazmak_gechmish.utf
verbs/yazmak_shimdiki.utf
```

## Word/Sentence Database

The application includes an `otherlang/` directory with a sample `words.txt` file.

### Setup Instructions

1. Create an `otherlang/` folder in your device's `InternalStorage/Download/` directory
2. Copy `words.txt` into this folder
3. Modify the file content according to your target language

**Important**: The application requires manual permission granting for file read/write operations. Go to App Settings and enable storage permissions.

### File Format

Each line in the `.txt` files should follow this format:
```
langA:langB
```

Example:
```
school:okul
book:kitap
house:ev
```

### Adding More Content

You can add as many `.txt` files as you want to the `otherlang/` directory, such as:
- `fruits.txt`
- `vegetables.txt`
- `animals.txt`

**Important Notes**:
- All files must be plain text (`.txt`)
- Files must be saved in **UTF-8** format
- Each line must follow the `langA:langB` pattern
- Lines without `:` or with empty content are ignored

### Performance Considerations

The application loads all words from the `otherlang/` directory at startup. For optimal performance:
- Keep the total size of all `.txt` files under **1-2 MB**
- The application will load and run smoothly with this size
- If the total word count reaches millions, the application may become slow or unresponsive

## License

This project is released under the **GPL v3** license as open-source software. You are free to use, modify, and distribute it according to the terms of the license.

## Requirements

- Android 10+ (API level 29+)
- Storage permissions enabled

## Contributions

Feel free to fork this repository and submit pull requests. All contributions are welcome!

## Credits

- **Original Design**: Murat Inan
- **AI Assistance**: DeepSeek AI
- **Android 29 Update**: DeepSeek AI

---

*We hope this tool serves you well in your language learning journey!*

*With love and respect.*
