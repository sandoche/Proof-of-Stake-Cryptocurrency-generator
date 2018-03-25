#!/bin/sh
echo "***********************************************************************"
echo "* This shell script will compact and reorganize the Nxt NRS database. *"
echo "* This process can take a long time.  Do not interrupt the script     *"
echo "* or shutdown the computer until it finishes.                         *"
echo "*                                                                     *"
echo "* To compact the database used while in a desktop mode, i.e. located  *"
echo "* under ~/.nxt/ , invoke this script as:                              *"
echo "* ./compact.sh -Dnxt.runtime.mode=desktop                             *"
echo "***********************************************************************"

java -Xmx1024m -cp "classes:lib/*:conf" $@ nxt.tools.CompactDatabase
exit $?
