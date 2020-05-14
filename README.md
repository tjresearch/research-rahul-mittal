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

To run this project, you will also need to install Haskell, which you can do at https://www.haskell.org/platform/. You do not need to know how to actually use Haskell, as Haskell only needs to be installed so that the part of the program written in Kotlin can run the part written in Haskell.

## Running

This project is not currently in a state to produce its final intended output. 

To run this project in its current state, assuming you installed it using IntelliJ, open the file `ComboProver.kt`, right click on the `main` function, and click 'Run us.tlatoani.combopro...'. You will be prompted to provide some sort of input, and will be provided with some sort of output, both of which will vary before the project reaches completion.

In the project's current state, you will need to write a combinatorial proof in a plaintext file, then provide the path of the file containing the proof as input. You will also need to prove the path of the file in which you would like output to be stored. The output will consist of each statement considered by the program to be contained in your proof decomposed into a structure according to the mathematical terms it uses.

You may find sample input proofs in the folder named `input`.
## Formula Syntax

Assuming that you write formulae in your input proof, you will need to use a certain syntax in order for the formulae to be understood by the program, which is described below:

All formulae need to be enclosed in between two dollar signs (ex. `$3 + 4$`).

Numbers are written normally in decimal notation (ex. `$37$`).

Addition, subtraction, multiplication, and division are denoted using the usual `+ - * /` operator symbols.

You may (and should) use parentheses `()` to clarify ambiguous formulae. Note that the order of operations used by the program is not guaranteed to be the usual one used by humans (ex. `$3 + 4 * 5$` may evaluate to 35 instead of 23) so you should use parentheses to clarify these kinds of expressions.

The operator `>>` is used for combinations (ex. `$10 >> 5$` evaluates to 252).

You can write factorials by writing `fact` then the number which you would like to take the factorial of (ex. `$fact 6$` evaluates to 120). Note that `fact` has higher precedence than all operators, so for example `$fact 2 * 4$` evaluates to 8 while `$fact (2 * 4)$` evaluates to 40320.

Lastly, your formulae may also contain variables. A variables is denoted by any letter (uppercase or lowercase) that is not part of a substring equal to `fact`, so `$x + 3$` evaluates to the sum of `x` and `3`, and `$abc$` evaluates to the product of `a`, `b`, and `c`, but `$factj$` would actually evaluate to the factorial of `j`.

