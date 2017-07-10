#!/bin/bash

java -cp target/*:target/dependency/* ca.concordia.clac.parser.evaluation.ConllEvaluation $@
