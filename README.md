EPGMatcher match channels from m3u file with channels from tv guide 
App has gui and console modes.
Gui - run java -jar EPGMather.jar
Console - run EPGMatcher.jar with args:  
	-m3u:"iptv playlist url in m3u format" 
	-epg:"EPG url in xml or xml.gz format" 
	-output:"new m3u filename" 
	[-loglevel:"ALL|SEVERE|OFF"]
