# Tassilo's SoTeSoLa 2012 Contribution

## Prerequisites

You need the [Leiningen](http://leiningen.org) build management tool.  Simply
download the `lein` script
[here](https://raw.github.com/technomancy/leiningen/preview/bin/lein), make it
executable, and place it somewhere on your `PATH`.

## Usage

- Parse every 101companies implementation that contains at least one java file
  into a java graph using EMFText's [JaMoPPC](http://www.jamopp.org) parser
  (included in `lib/`).  That's the job of the `build-jamopp-graphs.sh` bash
  script.

  Thereafter, the folder `models/` contains one xmi file per 101 implementation
  containing java files.  You'll get an error for some projects, e.g., all
  AntLR implementation, because the generated AntLR parsers contain characters
  that are not valid in XML and thus the XMI serialization fails.  There might
  be more errors, but at least I can parse 46 implementations here...

  Do it like so:

```
$ pwd
/path/to/sotesola2012
$ cd scripts/
$ ./build-jamopp-graphs.sh ~/path/to/101repo/contributions
```

- Calculate the metrics!  You'll get one txt file in `output/` per 101
  implementation which lists the metric values for any class of that
  implementation.
  
```
$ pwd
/path/to/sotesola2012
$ lein run
```


## License

Copyright © 2012 Tassilo Horn

Distributed under the General Public License, Version 3 or later.
