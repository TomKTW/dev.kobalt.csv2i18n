### Purpose

This program converts your CSV string data to ZIP file containing string resources for Android and iOS platform with
each language.

### Mechanism

CSV file is parsed in a way where first row are languages and first rows are string keys. All other cells are treated as
string values for specific language.

### Example

With following input:

    xx,en,hr
    k1,e1,h1
    k2,e2,h2

You should get a ZIP file containing following files:

- Android-en.xml

    <?xml version="1.0" encoding="utf-8"?>
    <resources>
     <string name="k1">e1</string>
     <string name="k2">e2</string>
    </resources>

- Android-hr.xml

    <?xml version="1.0" encoding="utf-8"?>
    <resources>
     <string name="k1">h1</string>
     <string name="k2">h2</string>
    </resources>

- iOS-en.xml

  "k1" = "e1";
  "k2" = "e2";

- iOS-hr.xml

  "k1" = "h1";
  "k2" = "h2";

### Limitations

- This converter won't convert arguments.