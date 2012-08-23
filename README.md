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
represented as EMF models.  The largest model, the one for the swing
implementation, contains 115520 objects and 134479 references.

To solve the two problems stated above, the
[FunnyQT library](https://github.com/jgralab/funnyqt) is used.  FunnyQT is a
model querying and transformation library for TGraphs and EMF models written in
Clojure.

For every class in each syntax graph, all six **Kemerer and Chidamber
complexity metrics** are calculated.  All metric values are aggregated over all
101 implementations, and so you'll also get a ranking of the 10 most complex
classes in the complete 101 corpus concerning each metric.

Furthermore, for each 101 implementation, a **UML-alike diagram** is generated
showing all classes and interfaces including generalization, implementation,
and usage relationships.  Here, a classifier uses another classifier if it has
a field of the other's type or if it has a method whose return type is the
other classifier.

See below how to run that stuff.

## Prerequisites

You only need the [Leiningen](http://leiningen.org) build management tool, and
it'll take care of every other dependency needed.  Simply download the `lein`
script [here](https://raw.github.com/technomancy/leiningen/preview/bin/lein),
make it executable, and place it somewhere on your `PATH`.

Oh, and for the diagram generation, you also need the
[GraphViz](http://www.graphviz.org/) program `dot` somewhere on your `PATH`.
The parser invocation script `build-jamopp-graphs.sh` is a BASH script, so
it'll only run on unices or windows with cygwin.

## Usage

Parse every 101companies implementation that contains at least one java file
into a java graph using EMFText's [JaMoPPC](http://www.jamopp.org) parser
(included in `lib/`).  That's the job of the `build-jamopp-graphs.sh` bash
script.  If an implementation needs to be built beforehand using ant or make,
this script will do that for you.  However, some implementations encode
absolute paths in their build scripts or rely on external plugins or libraries
which may or man not be installed.  Thus, for some of them building might
fail...

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
Running make in antlrAcceptor...done!
Building JaMoPP model for antlrAcceptor... failed!
Running make in antlrLexer...done!
Building JaMoPP model for antlrLexer... failed!
...
Running ant in jgralab...done!
Building JaMoPP model for jgralab... done!
Building JaMoPP model for jsf... done!
Building JaMoPP model for sax... done!
...
```

Then, calculate the metrics and do the architecture recovery!

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

In the `output/` directory, you'll get one...

- ... txt file per 101 implementation which lists the Kemerer and Chidamber
  metric values for any class of that implementation.  Actually, the *Depth Of
  Inheritance Tree* and the *Number Of Children* metrics are calculated for
  interfaces, too.

- ... `OVERALL_SCORES.txt` file which contains a ranking of the 10 most complex
  classes according to each metric.  The ranking shows the metric value, the
  class with that value, and the 101 implementation that contains that class.

- ... pdf file per 101 implementation which is a poor-man's UML class diagram.
  Generalizations and realizations are shown in the usual UML style, i.e.,
  arrows with empty heads and dashed arrows with empty heads.  Usage
  relationships are shown as arrows with open heads.


Well, that's it.  Have fun!

## License

Copyright © 2012 Tassilo Horn <horn@uni-koblenz.de>

Distributed under the General Public License, Version 3 or later.
