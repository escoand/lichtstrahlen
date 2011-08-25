BEGIN {
	FS="|";
	printf "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	printf "<data>";
}

{
	gsub(/(^ +| +$)/, "", $1);
	gsub(/(^ +| +$)/, "", $2);
	gsub(/(^ +| +$)/, "", $3);
	gsub(/(^ +| +$)/, "", $4);
	gsub(/(^ +| +$)/, "", $5);
	gsub(/(^ +| +$)/, "", $6);
	gsub(/(^ +| +$)/, "", $7);
	gsub(/(^ +| +$)/, "", $8);
	gsub(/(^ +| +$)/, "", $9);
	gsub(/(^ +| +$)/, "", $10);
	gsub(/(^ +| +$)/, "", $11);
	
	split($3, date, ".");
	if(month == date[3]date[2]) {
		printf "<entry date=\"%04d%02d%02d\">", date[3], date[2], date[1];
		printf "<verse>%s</verse>", $5;
		printf "<header>%s</header>", $7;
		printf "<text>%s</text>", $6;
		printf "<author>%s</author>", $2;
		printf "<weektext>%s</weektext>", $8;
		printf "<weekverse>%s</weekverse>", $9;
		printf "<monthtext>%s</monthtext>", $10;
		printf "<monthverse>%s</monthverse>", $11;
		printf "</entry>";
	}
}

END {
	printf "</data>";
}