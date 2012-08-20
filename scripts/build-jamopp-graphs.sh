#!/bin/bash

scriptname=${0}

function usage () {
    echo "Usage:"
    echo "======"
    echo
    echo "  ${scriptname} /path/to/101repo/contributions"
    echo
    exit 1
}

function jamoppc () {
    java -Xmx1G \
	-jar ../lib/jamoppc.jar \
	$*
}

function jars-in () {
    find $1 -name '*.jar' -printf "%p "
}

if [[ -z ${*} ]]; then
    usage
fi

dir101=${1}

cd ${dir101}
declare -a pdirs
pdirs=`find . -name '*.java' | cut -d/ -f2 | sort | uniq`
cd -

for pdir in ${pdirs}; do
    echo -n "Building JaMoPP model for ${pdir}... "
    jamoppc ${dir101}/${pdir} \
	../models/${pdir}.xmi \
	`jars-in ${dir101}/${pdir}` &> /dev/null
    if [[ $? -ne 0 ]]; then
	echo "failed!"
	if [[ -f ../models/${pdir}.xmi ]]; then
	    rm ../models/${pdir}.xmi
	fi
    else
	echo "done!"
    fi
done

echo "Done."
