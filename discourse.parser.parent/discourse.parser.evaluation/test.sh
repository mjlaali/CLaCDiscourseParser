#!/bin/bash

java -cp target/*:target/dependency/* ca.concordia.clac.parser.evaluation.ConllEvaluation -i $1 -m $2 -o $3
