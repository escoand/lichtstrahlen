#!/bin/sh

IMG=$1

convert "$IMG" -trim -resize 30x30 -repage 32x32+3+3 ../res/drawable-ldpi/icon.png
convert "$IMG" -trim -resize 40x40 -repage 48x48+4+4 ../res/drawable-mdpi/icon.png
convert "$IMG" -trim -resize 60x60 -repage 72x72+6+6 ../res/drawable-hdpi/icon.png
convert "$IMG" -trim -resize 80x80 -repage 92x92+8+8 ../res/drawable-xhdpi/icon.png
