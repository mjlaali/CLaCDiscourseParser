#!/bin/bash

rm -r outputs/resources

mkdir outputs/resources

cp src/main/resources/clacParser/model/dcHeadList.txt outputs/resources
cp src/main/resources/clacParser/model/eng_sm5.gr outputs/resources

java -cp target/*:target/dependency/* ca.concordia.clac.discourse.parser.dc.disambiguation.DiscourseVsNonDiscourseClassifier
java -cp target/*:target/dependency/* ca.concordia.clac.discourse.parser.dc.disambiguation.DiscourseSenseLabeler
java -cp target/*:target/dependency/* ca.concordia.clac.discourse.parser.dc.disambiguation.DiscourseConnectiveDisambiguator -m outputs/resources -c "$1"

cp -rf outputs/resources/ src/main/resources/clacParser/model
