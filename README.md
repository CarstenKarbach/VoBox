# VoBox

An Android app for learning vocabulary. You can add your own list of dictionaries to learn.
Each word mapping is saved in a card. The cards are organized in card boxes. You can test
yourself. If your answers are right, the cards are placed in upper boxes, otherwise they
are moved downward.

![VoBox landing page](docs/start_small.png)

## Use Cases

This app can be used for

- offline vocabularies for different languages
- learning sets of technical terms for exams

## Features

The app supports the following features

- add own word pairs
- search in word list 
- sharing vocabulary via file export and import
- import dictionary from CSV file
- create own dictionaries with arbitrary language mapping
- train vocabulary without effect on word levels
- test vocabulary, right answers lead to level up, wrong answers to level down
- localization in English and German
 

## Getting Started

This project is an Android Studio (`>=3.3`) project using Gradle as build tool.
Clone the repository and open it with Android Studio to start with your own modifications.

```
git clone https://github.com/CarstenKarbach/VoBox
```

In Android Studio click on `Build->Make Module ... app` and wait for Gradle to build the app.
Afterward, run the app on your target device by using the play button in the top menu bar.

This project and all its resources are licensed under the [GNU General Public License v2.0](./LICENSE).
