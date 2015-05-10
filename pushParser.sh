#!/bin/bash

mvn dependency:copy-dependencies package -DskipTests
rm -r target/model_train/
cp -r outputs/parser/model_train target/
rm -r target/analysisResults/
cp -r data/analysisResults target/
cd target/
git commit -a -m 'a new upadate'
git push origin master
