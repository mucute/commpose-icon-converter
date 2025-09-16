# Compose Icon Converter

![Build](https://github.com/mucute/commpose-icon-converter/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)

A powerful IntelliJ IDEA plugin that converts SVG files to Jetpack Compose ImageVector format, making it easy for Android developers to integrate vector icons into their Compose applications.

## ✨ Features

- **🎯 Easy SVG to ImageVector Conversion**: Right-click on any SVG file and convert it to Compose ImageVector format
- **🔄 Automatic File Naming**: Automatically processes file names by removing special characters and converting to PascalCase
- **⚙️ Customizable Output**: Configure package name, icon parent class, and icon style
- **🎨 Multiple Icon Styles**: Support for Default, Outline, Bold, Twotone, Bulk, Broken, and Linear styles
- **📦 Batch Processing**: Convert multiple SVG files at once from directory selection
- **🧠 Smart Path Detection**: Automatically detects appropriate output paths and package names
- **🚀 Seamless Integration**: Works directly within IntelliJ IDEA and Android Studio

## 🚀 Quick Start

### Installation

- **Using IDE Plugin Manager:**
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Compose Icon Converter"</kbd> > <kbd>Install</kbd>

- **Using JetBrains Marketplace:**

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID) and install it by clicking the <kbd>Install to ...</kbd> button.

- **Manual Installation:**

  Download the [latest release](https://github.com/mucute/commpose-icon-converter/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

### How to Use

1. **Right-click** on a directory containing SVG files in your project explorer
2. Select **"Convert to Compose Icon"** from the context menu
3. **Configure** the conversion settings in the dialog:
   - **Package Name**: Specify the target package for the generated Kotlin files
   - **Icon Parent Class**: Choose the parent class (e.g., Icons.Default, Icons.Outlined)
   - **Icon Style**: Select from Default, Outline, Bold, Twotone, Bulk, Broken, or Linear
   - **Output Path**: Choose where to save the generated files
4. Click **OK** to generate Kotlin files with ImageVector definitions for all SVG files in the directory

## 📋 Example Output

Converting an SVG file named `home-icon.svg` will generate a Kotlin file like:

```kotlin
package com.example.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

public val Icons.Default.HomeIcon: ImageVector
    get() {
        if (_homeIcon != null) {
            return _homeIcon!!
        }
        _homeIcon = materialIcon(name = "Default.HomeIcon") {
            materialPath {
                // SVG path data converted to Compose path commands
            }
        }
        return _homeIcon!!
    }

private var _homeIcon: ImageVector? = null
```

## 🛠️ Configuration Options

### Icon Styles
- **Default**: Standard filled icons
- **Outline**: Outlined/stroke-based icons
- **Bold**: Bold/thick stroke icons
- **Twotone**: Two-tone color icons
- **Bulk**: Bulk/filled with outline icons
- **Broken**: Broken line style icons
- **Linear**: Linear/thin stroke icons

### File Naming
The plugin automatically processes file names:
- Removes special characters (`-`, `_`, spaces, etc.)
- Converts to PascalCase (e.g., `home-icon.svg` → `HomeIcon`)
- Ensures valid Kotlin identifier names

## 🎯 Perfect For

- **Android Developers** working with Jetpack Compose
- **UI/UX Teams** converting design assets to code
- **Projects** migrating from traditional drawable resources to Compose
- **Icon Libraries** that need programmatic vector definitions

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Built with the [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
- Inspired by the need for seamless SVG to Compose integration

---

<!-- Plugin description -->
A powerful IntelliJ IDEA plugin that converts SVG files to Jetpack Compose ImageVector format. Features include easy SVG to ImageVector conversion, automatic file naming, customizable output settings, multiple icon styles support, batch processing capability, and smart path detection. Perfect for Android developers working with Jetpack Compose who need to convert SVG icons to native ImageVector format.
<!-- Plugin description end -->
