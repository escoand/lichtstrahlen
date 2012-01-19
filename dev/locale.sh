#!/bin/sh

DIR=~/workspace/lichtstrahlen

# english
rm -rf $DIR-en/ 2>/dev/null
cp -r $DIR/ $DIR-en/
rm -rf $DIR-en/gen/
rm -rf $DIR-en/res/*-de/
sed -e 's| package="[^"]*"|package="com.escoand.android.mobiledevotions_2012"|g' -i $DIR-en/AndroidManifest.xml
sed -e 's|import com\.escoand\.android\.lichtstrahlen_2012\.R;|import com.escoand.android.mobiledevotions_2012.R;|g' -i $DIR-en/src/com/escoand/android/lichtstrahlen/*

# german
rm -rf $DIR-de/ 2>/dev/null
cp -r $DIR/ $DIR-de/
rm -rf $DIR-de/gen/
for FILE in `IFS=$'\n' find $DIR-de/res/ -path "*/res/*-de/*"`; do
	mv "$FILE" "`echo "$FILE" | sed "s|\(/res/.*\)-de/|\1/|g"`"
done
rmdir  $DIR-de/res/*-de/
