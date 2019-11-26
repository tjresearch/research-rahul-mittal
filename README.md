# Verifying Combinatorial Proofs through Automated Translation to a Formal Proof Language

## Overview

This project aims to use NLP (natural language processing) techniques to programmatically translate combinatorial proofs written in English into a formal proof language so that they can be computationally verified.

## Requirements

The main components of this project are written in Kotlin 1.3. One component is written in Haskell, compiled using GHC 8.0.1.

The Kotlin component of this project requires the Stanford CoreNLP and JSON.simple libraries. If you are using Gradle, you can use the following code to import these libraries:
```groovy
dependencies {
    compile group: 'edu.stanford.nlp', name: 'stanford-parser', version: '3.9.2'
    compile group: 'edu.stanford.nlp', name: 'stanford-corenlp', version: '3.9.2'
    compile group: 'edu.stanford.nlp', name: 'stanford-corenlp', version: '3.9.2', classifier: 'models'
    compile group: 'com.googlecode.json-simple', name: 'json-simple', version: '1.1'
}
```

## Installation

I recommend using Gradle and IntelliJ IDEA to install this project. You can download IntelliJ at https://www.jetbrains.com/idea/download/. Once you have IntelliJ installed, clone this repository and then open the repository folder with IntelliJ.

## Running

This project is not currently in a state to produce its final intended output. 

To run this project in its current state, assuming you installed it using IntelliJ, open the file `ComboProver.kt`, right click on the `main` function, and click 'Run us.tlatoani.combopro...'. You will be prompted to provide some sort of input, and will be provided with some sort of output, both of which will vary before the project reaches completion.