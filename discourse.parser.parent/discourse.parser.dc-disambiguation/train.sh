#!/bin/bash

rm -r outputs/resources

mkdir outputs/resources

cp src/main/resources/clacParser/model/dcHeadList.txt outputs/resources
cp src/main/resources/clacParser/model/eng_sm5.gr outputs/resources

java -cp target/discourse.parser.dc-disambiguation-2.1.0-SNAPSHOT.jar:target/dependency/* ca.concordia.clac.discourse.parser.dc.disambiguation.DiscourseVsNonDiscourseClassifier 
java -cp target/discourse.parser.dc-disambiguation-2.1.0-SNAPSHOT.jar:target/dependency/* ca.concordia.clac.discourse.parser.dc.disambiguation.DiscourseSenseLabeler
java -cp target/discourse.parser.dc-disambiguation-2.1.0-SNAPSHOT.jar:target/dependency/* ca.concordia.clac.discourse.parser.dc.disambiguation.DiscourseConnectiveDisambiguator -m outputs/resources

 
