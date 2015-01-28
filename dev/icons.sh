#!/bin/sh

for FILE in dev/ic_*.svg; do
	ICON=`basename $FILE .svg`
	inkscape -z dev/$ICON.svg -e res/drawable-mdpi/$ICON.png -w 32 -h 32
	inkscape -z dev/$ICON.svg -e res/drawable-hdpi/$ICON.png -w 48 -h 48
	inkscape -z dev/$ICON.svg -e res/drawable-xhdpi/$ICON.png -w 64 -h 64
	inkscape -z dev/$ICON.svg -e res/drawable-xxhdpi/$ICON.png -w 96 -h 96
done
