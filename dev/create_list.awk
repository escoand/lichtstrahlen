BEGIN {
	FS="|";
	printf "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	printf "<list>";
}

{
	gsub(/(^ +| +$)/, "", $1);
	gsub(/(^ +| +$)/, "", $2);
	gsub(/(^ +| +$)/, "", $3);
	gsub(/(^ +| +$)/, "", $4);
	
	split($3, date, ".");
	split($4, until, ".");

	printf "<entry date=\"%04d%02d%02d\" until=\"%04d%02d%02d\">", date[3], date[2], date[1], until[3], until[2], until[1];
	printf "<verse>%s %s</verse>", $1, $2;
	printf "</entry>";
}

END {
	printf "</list>";
}
