# Tassilo's SoTeSoLa 2012 Hackathon Contribution

## Problems and Solutions

This SoTeSoLa hackathon contribution deals with two reverse engineering
problems:

  1. metrics
  2. architecture recovery

It tackles any 101 implementation that contains at least one java file.  Every
such implementation is parsed to a java syntax graph by using
[EMFText](http://www.emftext.org/index.php/EMFText)'s
[JaMoPPC](http://www.jamopp.org) parser.  Thus, abstract java syntax graph are
represented as huge EMF models.

To solve the two problems stated above, the
[FunnyQT library](https://github.com/jgralab/funnyqt) is used.  FunnyQT is a
model querying and transformation library for TGraphs and EMF models written in
Clojure.

For every class in each syntax graph, all six Kemerer and Chidamber complexity
metrics are calculated, and a uml-alike diagram is generated showing all
classes and interfaces including generalization, implementation, and usage
relationships (a classifier uses another classifier if it has a field of the
other's type or if it has a method whose return type is the other classifier).

See below how to run that stuff.

## Prerequisites

You only need the [Leiningen](http://leiningen.org) build management tool, and
it'll take care of every other dependency needed.  Simply download the `lein`
script [here](https://raw.github.com/technomancy/leiningen/preview/bin/lein),
make it executable, and place it somewhere on your `PATH`.

## Usage

Parse every 101companies implementation that contains at least one java file
into a java graph using EMFText's [JaMoPPC](http://www.jamopp.org) parser
(included in `lib/`).  That's the job of the `build-jamopp-graphs.sh` bash
script.

Thereafter, the folder `models/` contains one xmi file per 101 implementation
containing java files.  You'll get an error for some projects, e.g., all AntLR
implementation, because the generated AntLR parsers contain characters that are
not valid in XML and thus the XMI serialization fails.  There might be more
errors, but at least I can parse 46 implementations here...

Do it like so:

```
$ pwd
/path/to/sotesola2012
$ cd scripts/
$ ./build-jamopp-graphs.sh ~/path/to/101repo/contributions
Building JaMoPP model for antlrAcceptor... failed!
Building JaMoPP model for antlrLexer... failed!
Building JaMoPP model for antlrObjects... failed!
Building JaMoPP model for antlrParser... failed!
Building JaMoPP model for antlrTrees... failed!
Building JaMoPP model for aspectJ... done!
Building JaMoPP model for atl... done!
Building JaMoPP model for dom... done!
...
```

Calculate the metrics and do the architecture recovery!  In `output/`, you'll
get one...

- ... txt file per 101 implementation which lists the Kemerer and Chidamber
  metric values for any class of that implementation.  Actually, the *Depth Of
  Inheritance Tree* and the *Number Of Children* metrics are calculated for
  interfaces, too.

- ... pdf file per 101 implementation which is a poor-man's UML class diagram.
  Generalizations and realizations are shown in the usual UML style, i.e.,
  arrows with empty heads and dashed arrows with empty heads.  Usage
  relationships are shown as arrows with open heads.

```
$ pwd
/path/to/sotesola2012
$ lein run
Processing #<File models/jena2.xmi>
    Calculating metrics...done!
    Generating DOT visualization...done!
    Generating PDF...done!
Processing #<File models/javaParseLib.xmi>
    Calculating metrics...done!
    Generating DOT visualization...done!
    Generating PDF...done!
...
```

That's it...

## License

Copyright © 2012 Tassilo Horn

Distributed under the General Public License, Version 3 or later.
