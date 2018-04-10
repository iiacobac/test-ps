#!/bin/bash
FILE=products.json.gz
if [ ! -e $FILE ]; then
	wget https://s3-eu-west-1.amazonaws.com/pricesearcher-code-tests/software-engineer/products.json.gz
fi
java -jar ps.jar
