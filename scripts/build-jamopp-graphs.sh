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
cd - &> /dev/null

for pdir in ${pdirs}; do
    cd ${dir101}/${pdir}
    if [[ -f build.xml ]]; then
	echo -n "Running ant in ${pdir}..."
	ant &> /dev/null
	if [[ $? -ne 0 ]]; then
	    echo "failed!"
	else
	    echo "done!"
	fi
    elif [[ -f Makefile ]]; then
	echo -n "Running make in ${pdir}..."
	make &> /dev/null
	if [[ $? -ne 0 ]]; then
	    echo "failed!"
	else
	    echo "done!"
	fi
    fi
    cd - &> /dev/null

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
