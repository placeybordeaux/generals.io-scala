# generals.io-scala

This is the code you would end up with if you followed the tutorial for writing a bot for the game generals.io.

I saw this project porting things to java:  https://github.com/cyntran/generals.io-java

I wanted to use scala, so I went and followed the tutorial: http://dev.generals.io/api#tutorial, and mimicked the javascript as closely as I could.  JSON is not native, so there is some parsing, and the socket library is java rather than scala, so I had to use some implicit conversions to let you pass functions for the listeners.  It's not perfect, but I think it's pretty good.

I plan to wrap up the basic functionality also, so that a person could just focus on the logic if the bot if that's the part they're most interested in.
