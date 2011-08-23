BEGIN {
	FS="|";
	print "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	print "<data>";
}

{
	split($3, date, ".");
	if(month == date[3]date[2]) {
		print "\t<entry date=\""date[3]date[2]date[1]"\">";
		print "\t\t<verse>"$5"</verse>";
		print "\t\t<header>"$7"</header>";
		print "\t\t<text>"$6"</text>";
		print "\t\t<author>"$2"</author>";
		print "\t\t<weektext>"$8"</weektext>";
		print "\t\t<weekverse>"$9"</weekverse>";
		print "\t\t<monthtext>"$10"</monthtext>";
		print "\t\t<monthverse>"$11"</monthverse>";
		print "\t</entry>";
	}
}

END {
	print "</data>";
}